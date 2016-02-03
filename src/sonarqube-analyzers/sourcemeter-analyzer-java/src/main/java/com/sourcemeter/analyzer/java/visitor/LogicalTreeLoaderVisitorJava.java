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
package com.sourcemeter.analyzer.java.visitor;

import graphlib.Edge;
import graphlib.Node;
import graphsupportlib.Metric.Position;

import java.io.File;
import java.util.Locale;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.SonarException;

import com.sourcemeter.analyzer.base.helper.GraphHelper;
import com.sourcemeter.analyzer.base.visitor.LogicalTreeLoaderVisitor;
import com.sourcemeter.analyzer.java.core.resources.JavaClass;
import com.sourcemeter.analyzer.java.core.resources.JavaMethod;
import com.sourcemeter.analyzer.java.helper.VisitorHelperJava;

public class LogicalTreeLoaderVisitorJava extends LogicalTreeLoaderVisitor {

    public LogicalTreeLoaderVisitorJava(FileSystem fileSystem, Settings settings,
            ResourcePerspectives perspectives, Project project, SensorContext sensorContext, long numOfNodes) {

        super(fileSystem, settings, perspectives, project, sensorContext, numOfNodes,
                new VisitorHelperJava(project, sensorContext, perspectives, fileSystem));
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
        String nodeLongName = GraphHelper.getNodeLongNameAttribute(node);
        Position nodePosition = graphsupportlib.Metric.getFirstPositionAttribute(node);

        Resource resource = null;
        Resource parentResource = null;
        String nodeType = node.getType().getType();
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
            resource = new JavaClass(nodeTUID, nodeName, nodeLongName,
                    nodeType.toLowerCase(Locale.ENGLISH));
            parentResource = org.sonar.api.resources.File.fromIOFile(new File(nodePosition.path), this.project);

            indexResource(node, resource, parentResource, nodePosition);
        } else if (this.uploadMethods && "Method".equals(nodeType)) {
            Node parentNode = GraphHelper.getParentNode(node);
            if (GraphHelper.isClass(parentNode)) {
                String parentNodeType = parentNode.getType().getType().toLowerCase(Locale.ENGLISH);
                parentResource = new JavaClass(GraphHelper.getNodeTUID(parentNode),
                        GraphHelper.getNodeNameAttribute(parentNode),
                        GraphHelper.getNodeLongNameAttribute(parentNode),
                        parentNodeType);
                resource = new JavaMethod(nodeTUID, nodeName, nodeLongName)
                        .setParent(parentResource);

                indexResource(node, resource, parentResource, nodePosition);
            }
        }

        uploadMetricsAndWarnings(node, resource, nodePosition, true);

        this.logicalTime += (System.currentTimeMillis() - startLogicalTime);
    }
}
