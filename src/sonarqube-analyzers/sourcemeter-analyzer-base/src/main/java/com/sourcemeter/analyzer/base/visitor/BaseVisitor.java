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

import graphlib.Attribute;
import graphlib.Node;
import graphsupportlib.Metric.Position;

import java.util.List;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.resources.Resource;

import com.sourcemeter.analyzer.base.helper.VisitorHelper;

/**
 * Base class for visitors. Stores data that is needed by almost all visitor
 * classes and some helper methods.
 */
public abstract class BaseVisitor implements graphlib.Visitor {

    public static final int DEFUALT_PRECISION = 3;

    protected final ResourcePerspectives perspectives;
    protected final VisitorHelper visitorHelper;

    protected SensorContext sensorContext;
    protected Project project;

    protected boolean isDebugMode;

    public BaseVisitor(ResourcePerspectives p, VisitorHelper visitorHelper) {
        this.perspectives = p;
        this.visitorHelper = visitorHelper;
        this.isDebugMode = (System.getenv("COLUMBUS_SONAR_DEBUG") != null);
    }

    /**
     * Print progress bar and estimation time to the Console.
     *
     * @param currentNumOfNodes
     * @param allNumOfNodes
     * @param currentTime
     */
    protected void printProgress(double currentNumOfNodes, double allNumOfNodes, long currentTime) {
        final int width = 30; // progress bar width in chars

        double progressPercentage = currentNumOfNodes / allNumOfNodes;
        long remainingTime = Math.round((currentTime / currentNumOfNodes) * (allNumOfNodes - currentNumOfNodes));
        System.out.print("        [");
        int i = 0;
        for (; i <= (int) (progressPercentage * width); i++) {
            System.out.print("=");
        }
        for (; i < width; i++) {
            System.out.print(" ");
        }
        long second = (remainingTime / 1000) % 60;
        long minute = (remainingTime / (1000 * 60)) % 60;
        long hour = remainingTime / (1000 * 60 * 60);

        String time = String.format("%02d:%02d:%02d", hour, minute, second);
        if (progressPercentage < 1) {
            System.out.print("] " + Math.round(progressPercentage * 100) + "% [" + Runtime.getRuntime().totalMemory() / (1024 * 1024) + " MB] Time left: "
                    + time + "\r");
        } else {
            System.out.print("] done...                                                                              \r");
        }
    }


    /**
     * Upload metrics and warnings to Sonar database by Sonar API calls.
     *
     * @param node
     * @param resource
     * @param nodePosition
     */
    protected void uploadMetricsAndWarnings(Node node, Resource resource,
            Position nodePosition, boolean uploadEmptyRulesetRules) {
        List<Attribute> attributes = node.getAttributes();
        for (Attribute attribute : attributes) {
            String context = attribute.getContext();
            // Upload a Violation
            if ("warning".equals(context)) {
                visitorHelper.uploadWarnings(attribute, node, nodePosition);

            } // Upload a metric
            else if (resource != null && ("metric".equals(context) || "metricgroup".equals(context))) {
                visitorHelper.uploadMetrics(attribute, resource);
            }
        }

        if (resource != null && !Qualifiers.isProject(resource, true)
                && uploadEmptyRulesetRules) {
            visitorHelper.uploadEmptyRulesetMetricsByZero(resource);
        }
    }

    /**
     * Upload warnings only to Sonar database by Sonar API calls.
     *
     * @param node
     * @param resource
     * @param nodePosition
     * @param countThresholdViolations
     */
    protected void uploadWarnings(Node node, Position nodePosition) {
        List<Attribute> attributes = node.getAttributes();
        for (Attribute attribute : attributes) {
            String context = attribute.getContext();
            if ("warning".equals(context)) {
                visitorHelper.uploadWarnings(attribute, node, nodePosition);
            }
        }
    }
}
