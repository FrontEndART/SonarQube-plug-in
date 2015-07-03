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
package com.sourcemeter.analyzer.rpg.visitor;

import graphlib.Edge;
import graphlib.Node;
import com.sourcemeter.analyzer.base.helper.GraphHelper;
import com.sourcemeter.analyzer.base.helper.VisitorHelper;
import com.sourcemeter.analyzer.base.visitor.ComponentTreeLoaderVisitor;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;


public class ComponentTreeLoaderVisitorRPG extends ComponentTreeLoaderVisitor {

    public ComponentTreeLoaderVisitorRPG(Settings settings, ResourcePerspectives perspectives,
            Project project, SensorContext sensorContext, long numOfNodes,
            VisitorHelper visitorHelper) {
        super(perspectives, project, sensorContext, numOfNodes, visitorHelper);
    }

    /**
     * @return execution time.
     */
    @Override
    public long getComponentTime() {
        return this.componentTime;
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
    public void postNodeVisitorFunc(Node node) {
        if (this.isDebugMode) {
            this.numOfVisitedNodes++;
            printProgress(this.numOfVisitedNodes, this.numOfNodes, this.componentTime);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preNodeVisitorFunc(Node node) {
        long startTime = System.currentTimeMillis();

        String nodeName = GraphHelper.getNodeNameAttribute(node);
        String nodeType = node.getType().getType();

        if ("System".equals(nodeType) && "System".equals(nodeName)) {
            Resource resource = this.project;
            createMetricsForProject(node, resource);
            this.componentTime += (System.currentTimeMillis() - startTime);
        }
    }
}