/**
 * Copyright (c) 2014-2015, FrontEndART Software Ltd.
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
package com.sourcemeter.analyzer.python.visitor;

import graphlib.Edge;
import graphlib.Node;
import graphsupportlib.Metric.Position;
import com.sourcemeter.analyzer.base.helper.GraphHelper;
import com.sourcemeter.analyzer.base.visitor.LogicalTreeLoaderVisitor;
import com.sourcemeter.analyzer.python.core.resources.PythonClass;
import com.sourcemeter.analyzer.python.core.resources.PythonFunction;
import com.sourcemeter.analyzer.python.core.resources.PythonMethod;
import com.sourcemeter.analyzer.python.helper.VisitorHelperPython;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.SonarException;

public class LogicalTreeLoaderVisitorPython extends LogicalTreeLoaderVisitor {

    public static final Map<Resource, Integer> FUNCTIONS_FOR_FILES = new HashMap<Resource, Integer>();
    private final boolean skipTUID;

    public LogicalTreeLoaderVisitorPython(FileSystem fileSystem, Settings settings, ResourcePerspectives perspectives, Project project,
            SensorContext sensorContext, long numOfNodes) {

        super(fileSystem, settings, perspectives, project, sensorContext,
                numOfNodes, new VisitorHelperPython(project, sensorContext,
                perspectives, settings));

        this.skipTUID = settings.getBoolean("sm.cpp.skipTUID");
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

        long startLogicalTime = System.currentTimeMillis();
        Position nodePosition = graphsupportlib.Metric.getFirstPositionAttribute(node);
        if (nodePosition == null) {
            return;
        }

        String nodeLongName = GraphHelper.getNodeLongNameAttribute(node);
        String nodeName = GraphHelper.getNodeNameAttribute(node);

        Resource resource = null;
        Resource parentResource = null;
        Resource file = org.sonar.api.resources.File.fromIOFile(new File(nodePosition.path), this.project);
        String nodeType = node.getType().getType();
        String nodeTUID = GraphHelper.getNodeTUID(node);

        if (nodeTUID == null) {
            String warningMessage = "A node has no TUID attribute: "
                    + nodeLongName + ", UID: " + node.getUID();

            if (skipTUID) {
                LOG.warn(warningMessage);
                return;
            } else {
                throw new SonarException(warningMessage);
            }
        }

        if (GraphHelper.isClass(node)) {
            resource = new PythonClass(nodeTUID, nodeName, nodeLongName).setParent(file);
            Resource indexedResource = this.sensorContext.getResource(resource);

            if (null == indexedResource) {
                indexResource(node, resource, file, nodePosition);
            } else {
                resource = indexedResource;
            }
        } else if ("Method".equals(nodeType)) {
            if (this.uploadMethods) {
                Node parentNode = GraphHelper.getParentNode(node);
                if (GraphHelper.isClass(parentNode)) {
                    String parentNodeName = GraphHelper
                            .getNodeNameAttribute(parentNode);
                    parentResource = new PythonClass(
                            GraphHelper.getNodeTUID(parentNode),
                            parentNodeName,
                            GraphHelper.getNodeLongNameAttribute(parentNode));
                    resource = new PythonMethod(nodeTUID,
                            nodeName, nodeLongName).setParent(parentResource);

                    indexResource(node, resource, parentResource, nodePosition);
                }
            }
            incFunctionToFile(file);
        } else if ("Function".equals(nodeType)) {
            if (this.uploadMethods) {
                resource = new PythonFunction(nodeTUID,
                        nodeName, nodeLongName).setParent(file);
                indexResource(node, resource, file, nodePosition);
            }
            incFunctionToFile(file);
        }

        uploadMetricsAndWarnings(node, resource, nodePosition, true);
        this.logicalTime += (System.currentTimeMillis() - startLogicalTime);
    }

    private void incFunctionToFile(Resource file) {
        if (FUNCTIONS_FOR_FILES.containsKey(file)) {
            FUNCTIONS_FOR_FILES.put(file, FUNCTIONS_FOR_FILES.get(file) + 1);
        } else {
            FUNCTIONS_FOR_FILES.put(file, 1);
        }
    }
}
