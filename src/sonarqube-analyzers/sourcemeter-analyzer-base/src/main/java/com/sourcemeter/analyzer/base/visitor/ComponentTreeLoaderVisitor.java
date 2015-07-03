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
package com.sourcemeter.analyzer.base.visitor;

import graphlib.Attribute;
import graphlib.Node;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreInitializer;

import com.sourcemeter.analyzer.base.helper.GraphHelper;
import com.sourcemeter.analyzer.base.helper.VisitorHelper;

/**
 * Class for visiting and storing component nodes from the result graph.
 */
public abstract class ComponentTreeLoaderVisitor extends BaseVisitor {

    protected long componentTime;
    protected long numOfVisitedNodes;
    protected final long numOfNodes;

    public ComponentTreeLoaderVisitor(ResourcePerspectives perspectives,
            Project project, SensorContext sensorContext, long numOfNodes,
            VisitorHelper visitorHelper) {
        super(perspectives, visitorHelper);
        this.project = project;
        this.sensorContext = sensorContext;

        this.componentTime = 0;
        this.numOfVisitedNodes = 0;
        this.numOfNodes = numOfNodes;
    }

    /**
     * @return execution time.
     */
    public long getComponentTime() {
        return this.componentTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postNodeVisitorFunc(Node node) {
        if (this.isDebugMode) {
            this.numOfVisitedNodes++;
            printProgress(this.numOfVisitedNodes, this.numOfNodes,
                    this.componentTime);
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

        if ("Component".equals(nodeType) && "<System>".equals(nodeName)) {
            Resource resource = this.project;
            createMetricsForProject(node, resource);
            this.componentTime += (System.currentTimeMillis() - startTime);
        }
    }

    /**
     * Creates and stores metrics measures for the current language to be
     * aggregated on project level.
     *
     * @param node
     * @param resource
     */
    protected void createMetricsForProject(Node node, Resource resource) {
        List<Attribute> attributes = node.getAttributes();
        Set<Measure> measures = new HashSet<>();
        for (Attribute attribute : attributes) {
            String context = attribute.getContext();
            if (resource != null
                && ("metric".equals(context) || "metricgroup".equals(context))) {

                Measure measure = visitorHelper.createMeasureFromAttribute(attribute, resource);
                if (measure != null) {
                    measures.add(measure);
                }
            }
        }

        // put the Set for the current language's metrics
        SourceMeterCoreInitializer.MEASURES_FOR_LANGUAGES.add(measures);
    }
}