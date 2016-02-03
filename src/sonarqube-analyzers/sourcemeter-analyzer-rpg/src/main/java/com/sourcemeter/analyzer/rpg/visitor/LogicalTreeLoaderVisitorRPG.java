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
package com.sourcemeter.analyzer.rpg.visitor;

import graphlib.Edge;
import graphlib.Node;
import graphsupportlib.Metric.Position;

import java.io.File;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.SonarException;

import com.sourcemeter.analyzer.base.helper.GraphHelper;
import com.sourcemeter.analyzer.base.visitor.LogicalTreeLoaderVisitor;
import com.sourcemeter.analyzer.rpg.core.resources.RPGProcedure;
import com.sourcemeter.analyzer.rpg.core.resources.RPGProgram;
import com.sourcemeter.analyzer.rpg.core.resources.RPGSubroutine;
import com.sourcemeter.analyzer.rpg.helper.FileHelperRPG;
import com.sourcemeter.analyzer.rpg.helper.GraphHelperRPG;
import com.sourcemeter.analyzer.rpg.helper.VisitorHelperRPG;

public class LogicalTreeLoaderVisitorRPG extends LogicalTreeLoaderVisitor {

    public LogicalTreeLoaderVisitorRPG(FileSystem fileSystem,
            Settings settings, ResourcePerspectives perspectives,
            Project project, SensorContext sensorContext, long numOfNodes) {

        super(fileSystem, settings, perspectives, project, sensorContext,
                numOfNodes, new VisitorHelperRPG(project, sensorContext,
                perspectives, fileSystem));
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
        Position nodePosition = graphsupportlib.Metric.getFirstPositionAttribute(node);
        String nodeType = node.getType().getType();
        if (nodePosition == null) {
            return;
        }

        String nodeLongName = GraphHelperRPG.getNodeLongNameAttribute(node);
        String nodeTUID = GraphHelperRPG.getNodeTUID(node);
        Node programNode = GraphHelperRPG.getProgramNode(node);
        String spoolFile = GraphHelperRPG.getSpoolFile(programNode);

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

        if (spoolFile != null) {
            String filePath = FileHelperRPG.getCorrectedFilePath(
                    nodePosition.path, this.fileSystem);
            if (filePath == null) {
                return;
            }
            nodePosition.path = filePath;
        }

        Resource resource = null;

        Resource file = org.sonar.api.resources.File.fromIOFile(new File(
                nodePosition.path), this.project);

        if ("Program".equals(nodeType)) {
            resource = new RPGProgram(nodeTUID, nodeName, nodeLongName).setParent(file);
            indexResource(node, resource, file, nodePosition);
        } else if (this.uploadMethods && "Procedure".equals(nodeType)) {
            Resource parentResource = getParentProgram(node);
            resource = new RPGProcedure(nodeTUID, nodeName, nodeLongName)
                    .setParent(parentResource);
            indexResource(node, resource, parentResource, nodePosition);
        } else if (this.uploadMethods && "Subroutine".equals(nodeType)) {
            Resource parentResource = getParentProgram(node);
            resource = new RPGSubroutine(nodeTUID, nodeName, nodeLongName)
                    .setParent(parentResource);
            indexResource(node, resource, parentResource, nodePosition);
        }

        uploadMetricsAndWarnings(node, resource, nodePosition, true);
        this.logicalTime += (System.currentTimeMillis() - startLogicalTime);
    }

    private RPGProgram getParentProgram(Node node) {
        Node programNode = GraphHelperRPG.getProgramNode(node);

        return new RPGProgram(GraphHelper.getNodeTUID(programNode),
                GraphHelper.getNodeNameAttribute(programNode),
                GraphHelper.getNodeLongNameAttribute(programNode));
    }
}
