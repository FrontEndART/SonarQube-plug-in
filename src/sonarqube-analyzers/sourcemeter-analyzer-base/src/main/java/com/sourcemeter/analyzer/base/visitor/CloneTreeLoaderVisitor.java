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
package com.sourcemeter.analyzer.base.visitor;

import graphlib.AttributeString;
import graphlib.Edge.eDirectionType;
import graphlib.Node;
import graphsupportlib.Metric.Position;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;

import com.sourcemeter.analyzer.base.core.resources.CloneClass;
import com.sourcemeter.analyzer.base.core.resources.CloneInstance;
import com.sourcemeter.analyzer.base.helper.FileHelper;
import com.sourcemeter.analyzer.base.helper.GraphHelper;
import com.sourcemeter.analyzer.base.helper.VisitorHelper;

/**
 * Visitor class for reading clone informations from the result graph.
 */
public abstract class CloneTreeLoaderVisitor extends BaseVisitor {

    private final Map<Resource, Set<String>> duplicationsMap = new HashMap<Resource, Set<String>>();

    private long cloneTime;

    private long numOfVisitedNodes;
    private final long numOfNodes;
    private boolean emptyProject = false;
    private final VisitorHelper visitorHelper;
    private final FileSystem fileSystem;

    /**
     * Initializes a CloneTreeLoaderVisitor by all necessary attributes
     *
     * @param fileSystem
     * @param perspectives
     * @param project
     * @param sensorContext
     * @param numOfNodes
     * @param visitorHelper
     * @param countThresholdViolations
     */
    public CloneTreeLoaderVisitor(FileSystem fileSystem,
            ResourcePerspectives perspectives, Project project,
            SensorContext sensorContext, long numOfNodes,
            VisitorHelper visitorHelper) {
        super(perspectives, visitorHelper);
        this.project = project;
        this.sensorContext = sensorContext;
        this.visitorHelper = visitorHelper;
        this.fileSystem = fileSystem;

        this.cloneTime = 0;
        this.numOfVisitedNodes = 0;
        this.numOfNodes = numOfNodes;

        FilePredicate mainFilePredicate = fileSystem.predicates().hasType(InputFile.Type.MAIN);

        if (!fileSystem.hasFiles(mainFilePredicate)) {
            this.emptyProject = true;
        }
    }

    /**
     * Different toolchains can have different keys. This method should return
     * the used key in every analyzer plugin.
     *
     * @param node
     * @return
     */
    protected abstract String getKeyFromNode(Node node);

    /**
     * @return Duplications container0
     */
    public Map<Resource, Set<String>> getDuplicationsMap() {
        return this.duplicationsMap;
    }

    /**
     * @return execution time.
     */
    public long getCloneTime() {
        return this.cloneTime;
    }

    /**
     * Clone instance to string
     *
     * @param node
     * @param sensorContext
     * @param projectKey
     * @return Pair of the containing class key and clone instance in xml format
     */
    private SimpleEntry<org.sonar.api.resources.File, String> cloneInstanceToString(
            Node node, SensorContext sensorContext, Project project) {
        SimpleEntry<org.sonar.api.resources.File, String> result = null;
        String projectKey = project.getKey();
        org.sonar.api.resources.File fileResource = (org.sonar.api.resources.File) FileHelper
                .getIndexedFileForFilePath(fileSystem, sensorContext, project, visitorHelper.getPathFromNode(node));

        if (fileResource != null) {
            Position pos = graphsupportlib.Metric.getFirstPositionAttribute(node);
            String value = "<b c=\"" + projectKey + ":" + getKeyFromNode(node)
                    + "\" s=\"" + pos.line + "\" l=\""
                    + (pos.endline - pos.line)
                    + "\" r=\"" + projectKey + ":" + fileResource.getKey() + "\"/>";
            result = new SimpleEntry<org.sonar.api.resources.File, String>(fileResource, value);
        }
        return result;
    }

    /**
     * Check Clone class or instance is Disappearing clone smell or not.
     *
     * @param node (CloneClass or CloneInstance)
     * @return true if disappearing
     */
    private boolean isDisappearing(Node node) {
        AttributeString cloneSmellType = (AttributeString) graphsupportlib.Metric.getNodeAttribute(node, "CloneSmellType");
        if (cloneSmellType != null && "cstDisappearing".equals(cloneSmellType.getValue())) {
            return true;
        }

        return false;
    }

    /**
     * Collect clone instances of a CloneClass node
     *
     * @param node
     * @param project
     * @param sensorContext
     *
     * @return first CI node
     */
    private Node collectCloneClass(Node node, Project project, SensorContext sensorContext) {
        List<Resource> resources = new ArrayList<Resource>();
        List<Node> nodes = graphsupportlib.Metric.getNodesByEdgeType(node, "CloneTree", eDirectionType.edtDirectional);
        StringBuffer duplicationsData = new StringBuffer("<g c=\"" + project.getKey() + ":" + getKeyFromNode(node) + "\">");

        for (Node n : nodes) {
            if (n != null && !isDisappearing(n)) {
                SimpleEntry<org.sonar.api.resources.File, String> pair = cloneInstanceToString(n, sensorContext, project);
                if (pair != null) {
                    duplicationsData.append(pair.getValue());
                    resources.add(pair.getKey());
                }
            }
        }
        duplicationsData.append("</g>");
        for (Resource resource : resources) {
            Set<String> list = this.duplicationsMap.get(resource);
            if (list == null) {
                list = new HashSet<String>();
            }
            list.add(duplicationsData.toString());
            this.duplicationsMap.put(resource, list);
        }

        if (!nodes.isEmpty()) {
            return nodes.get(0);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postNodeVisitorFunc(Node node) {
        if (this.isDebugMode) {
            this.numOfVisitedNodes++;
            printProgress(this.numOfVisitedNodes, this.numOfNodes, this.cloneTime);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preNodeVisitorFunc(Node node) {
        if (this.emptyProject) {
            return;
        }

        long startTime = System.currentTimeMillis();
        String nodeName = GraphHelper.getNodeNameAttribute(node);
        Position nodePosition = graphsupportlib.Metric.getFirstPositionAttribute(node);
        if (null != nodePosition) {
            nodePosition.path = visitorHelper.getPathFromNode(node);
        }

        Resource resource = null;
        org.sonar.api.resources.File parentResource = null;
        String nodeType = node.getType().getType();

        boolean disappearing = isDisappearing(node);
        if ("CloneClass".equals(nodeType) && !disappearing) {
            Node ci = collectCloneClass(node, this.project, this.sensorContext);
            parentResource = (org.sonar.api.resources.File) FileHelper
                    .getIndexedFileForFilePath(fileSystem, sensorContext, project, visitorHelper.getPathFromNode(ci));
            if (parentResource != null) {
                resource = new CloneClass(getKeyFromNode(node), nodeName, parentResource);
                this.sensorContext.index(resource);
            } else {
                return;
            }
        } else if ("CloneInstance".equals(nodeType) && !disappearing && nodePosition != null) {
            parentResource = (org.sonar.api.resources.File) FileHelper
                    .getIndexedFileForFilePath(fileSystem, sensorContext, project, nodePosition.path);
            if (parentResource != null) {
                resource = new CloneInstance(getKeyFromNode(node), nodeName, parentResource);
                this.sensorContext.index(resource);
            } else {
                return;
            }
        } else {
            return;
        }

        uploadMetricsAndWarnings(node, resource, nodePosition, false);
        this.cloneTime += (System.currentTimeMillis() - startTime);
    }
}
