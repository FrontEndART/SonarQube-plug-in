/**
 * Copyright (c) 2014-2016, FrontEndART Software Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by FrontEndART Software Ltd.
 * 4. Neither the name of FrontEndART Software Ltd. nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY FrontEndART Software Ltd. ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL FrontEndART Software Ltd. BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.sourcemeter.analyzer.csharp.visitor;

import graphlib.Edge;
import graphlib.Node;
import graphsupportlib.Metric;
import graphsupportlib.Metric.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.SonarException;

import com.sourcemeter.analyzer.base.core.resources.ClassData;
import com.sourcemeter.analyzer.base.helper.FileHelper;
import com.sourcemeter.analyzer.base.helper.GraphHelper;
import com.sourcemeter.analyzer.base.visitor.LogicalTreeLoaderVisitor;
import com.sourcemeter.analyzer.csharp.core.resources.CSharpClass;
import com.sourcemeter.analyzer.csharp.core.resources.CSharpMethod;
import com.sourcemeter.analyzer.csharp.helper.VisitorHelperCSharp;

public class LogicalTreeLoaderVisitorCSharp extends LogicalTreeLoaderVisitor {

    private final Map<Resource, List<ClassData>> filePathsForClasses;

    public static final Map<Resource, Integer> FUNCTIONS_FOR_FILES = new HashMap<Resource, Integer>();

    public LogicalTreeLoaderVisitorCSharp(FileSystem fileSystem,
            Settings settings, ResourcePerspectives perspectives,
            Project project, SensorContext sensorContext, long numOfNodes) {

        super(fileSystem, settings, perspectives, project, sensorContext,
                numOfNodes, new VisitorHelperCSharp(project, sensorContext,
                perspectives, fileSystem));

        this.filePathsForClasses = new HashMap<Resource, List<ClassData>>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void edgeVisitorFunc(Edge e) {
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void preNodeVisitorFunc(Node node) {
        if (this.emptyProject) {
            return;
        }

        String nodeName = GraphHelper.getNodeNameAttribute(node);
        if (nodeName == null || "__LogicalRoot__".equals(nodeName)) {
            return;
        }

        long startLogicalTime = System.currentTimeMillis();
        List<Position> nodePositions = Metric
                .getPositionAttributes(node);

        if (nodePositions == null || nodePositions.isEmpty()) {
            return;
        }

        Position nodePosition = nodePositions.get(0);
        String nodeType = node.getType().getType();
        String nodeLongName = GraphHelper.getNodeLongNameAttribute(node);
        String nodeTUID = GraphHelper.getNodeTUID(node);

        if (nodeTUID == null) {
            String warningMessage = "A " + nodeType + " node has no TUID attribute: "
                    + nodeLongName + ", UID: " + node.getUID();

            if (skipTUID) {
                LOG.warn(warningMessage);
                return;
            } else {
                throw new SonarException(warningMessage);
            }
        }

        if (GraphHelper.isClass(node)) {
            uploadClass(nodePositions, nodeTUID, node, nodeName, nodeLongName);
        } else if (this.uploadMethods && "Method".equals(nodeType)) {
            uploadMethod(nodePosition, nodeTUID, node, nodeName, nodeLongName);
        }

        this.logicalTime += (System.currentTimeMillis() - startLogicalTime);
    }

    /**
     * Uploads a method to it's corresponding parent class.
     *
     * @param nodePosition
     * @param nodeTUID
     * @param node
     * @param nodeName
     * @param nodeLongName
     */
    private void uploadMethod(Position nodePosition, String nodeTUID,
            Node node, String nodeName, String nodeLongName) {
        Resource file = FileHelper.getIndexedFileForFilePath(this.fileSystem,
                this.sensorContext, this.project, nodePosition.path);
        if (file == null) {
            return;
        }

        Node parentNode = GraphHelper.getParentNode(node);

        if (GraphHelper.isClass(parentNode)) {
            String parentNodeName = GraphHelper
                    .getNodeNameAttribute(parentNode);
            String parentNodeLongName = GraphHelper
                    .getNodeLongNameAttribute(parentNode);
            List<Position> parentNodePositions = Metric
                    .getPositionAttributes(parentNode);
            String parentNodeTUID = null;
            int i = 1;

            for (Position parentPosition : parentNodePositions) {
                if (nodePosition.path != null
                        && nodePosition.path.equals(parentPosition.path)
                        && nodePosition.line >= parentPosition.line
                        && nodePosition.endline <= parentPosition.endline) {
                    parentNodeTUID = GraphHelper.getNodeTUID(parentNode) + "^"
                            + i;
                    break;
                }
                i++;
            }

            if (parentNodeTUID == null) {
                return;
            }

            CSharpClass parentClass = new CSharpClass(parentNodeTUID,
                    parentNodeName, parentNodeLongName, (i == 1));
            Resource resource = new CSharpMethod(nodeTUID, nodeName,
                    nodeLongName).setParent(parentClass);

            if (!indexResource(node, resource, parentClass, nodePosition)
                    || resource.getId() == null) {
                // resource's id is null if it is indexed previously.
                // In the graph, multiple edges can point from parent to child element.
                // This prevents uploading them multiple times.
                return;
            }

            uploadMetricsAndWarnings(node, resource, nodePosition, true);
            incFunctionToFile(file);
        }
    }

    /**
     * Uploads a C# class. If it has more position attributes (it is a partial
     * class) then multiple classes will be uploaded.
     *
     * @param nodePositions
     * @param nodeTUID
     * @param node
     * @param nodeName
     * @param nodeLongName
     */
    private void uploadClass(List<Position> nodePositions, String nodeTUID,
            Node node, String nodeName, String nodeLongName) {
        int i = 1;
        List<CSharpClass> classesForPosition = new ArrayList<>();
        List<ClassData> classDatas = new ArrayList<>();
        for (Position position : nodePositions) {
            String tmpNodeTUID = nodeTUID + "^" + i;
            Resource file = FileHelper.getIndexedFileForFilePath(
                    this.fileSystem, this.sensorContext, this.project,
                    position.path);
            if (file == null) {
                continue;
            } else {
                file = this.sensorContext.getResource(file);
            }

            Resource resource = new CSharpClass(tmpNodeTUID, nodeName,
                    nodeLongName, (i == 1)).setParent(file);

            if (!indexResource(node, resource, file, position)
                    || resource.getId() == null) {
                continue;
            }

            ClassData classData = new ClassData(file.getId(), nodeName,
                    resource.getQualifier(), position.line, position.endline,
                    resource.getId(), resource.getDescription());

            classDatas.add(classData);

            classesForPosition.add((CSharpClass) resource);
            uploadMetricsAndWarnings(node, resource, position, true);
            i++;
        }

        if (nodePositions.size() > 1) {
            for (CSharpClass csharpClass : classesForPosition) {
                for (ClassData classData : classDatas) {
                    putClassDataInResourceMap(this.filePathsForClasses,
                            csharpClass, classData);
                }
            }
        }
    }

    private void incFunctionToFile(Resource file) {
        if (FUNCTIONS_FOR_FILES.containsKey(file)) {
            FUNCTIONS_FOR_FILES.put(file, FUNCTIONS_FOR_FILES.get(file) + 1);
        } else {
            FUNCTIONS_FOR_FILES.put(file, 1);
        }
    }

    private void putClassDataInResourceMap(
            Map<Resource, List<ClassData>> resourceMap, Resource key,
            ClassData value) {
        List<ClassData> classDatas;
        if (resourceMap.containsKey(key)) {
            classDatas = resourceMap.get(key);
            classDatas.add(value);
        } else {
            classDatas = new ArrayList<ClassData>();
            classDatas.add(value);
            resourceMap.put(key, classDatas);
        }
    }

    public Map<Resource, List<ClassData>> getFilePathsForClasses() {
        return filePathsForClasses;
    }
}
