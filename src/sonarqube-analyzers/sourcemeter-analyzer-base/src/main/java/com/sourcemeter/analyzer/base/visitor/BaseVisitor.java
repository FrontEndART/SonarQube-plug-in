/**
 * Copyright (c) 2014-2017, FrontEndART Software Ltd.
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

import java.lang.reflect.Field;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

import graphlib.Attribute;
import graphlib.AttributeFloat;
import graphlib.AttributeInt;
import graphlib.AttributeString;
import graphlib.Edge;
import graphlib.Node;

import com.sourcemeter.analyzer.base.helper.FileHelper;
import com.sourcemeter.analyzer.base.helper.VisitorHelper;
import com.sourcemeter.analyzer.base.jsontree.Position;
import com.sourcemeter.analyzer.base.jsontree.interfaces.MetricsInt;

/**
 * Base class for visitors. Stores data that is needed by almost all visitor
 * classes and some helper methods.
 */
public abstract class BaseVisitor implements graphlib.Visitor {

    protected static final Logger LOG = LoggerFactory.getLogger(BaseVisitor.class);

    private final VisitorHelper visitorHelper;

    protected SensorContext sensorContext;
    protected AbstractLanguage pluginLanguage;
    protected boolean isDebugMode;
    protected boolean uploadMethods;

    public BaseVisitor(VisitorHelper visitorHelper, Configuration configuration, AbstractLanguage pluginLanguage) {
        this.visitorHelper = visitorHelper;
        this.isDebugMode = (System.getenv("COLUMBUS_SONAR_DEBUG") != null);

        this.pluginLanguage = pluginLanguage;

        String pluginLanguageKey = pluginLanguage.getKey();

        if ("py".equals(pluginLanguageKey)) {
            pluginLanguageKey = "python";
        } else if ("cs".equals(pluginLanguageKey)) {
            pluginLanguageKey = "csharp";
        }

        this.uploadMethods = FileHelper.getBooleanFromConfiguration(configuration, "sm." + pluginLanguageKey + ".uploadMethods");
    }

    public BaseVisitor(VisitorHelper visitorHelper) {
        this.visitorHelper = visitorHelper;
        this.isDebugMode = (System.getenv("COLUMBUS_SONAR_DEBUG") != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void edgeVisitorFunc(Edge e) {
        // Do nothing
    }

    /**
     * Print progress bar and estimation time to the Console.
     *
     * @param currentNumOfNodes Current number of the nodes.
     * @param allNumOfNodes Number of all nodes.
     * @param currentTime Current time.
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
     * @param node Node of th result graph.
     * @param inputComponent Component of the input.
     */
    protected void uploadMetrics(Node node, InputComponent inputComponent) {
        List<Attribute> attributes = node.getAttributes();
        for (Attribute attribute : attributes) {
            String context = attribute.getContext();
            if (inputComponent != null && ("metric".equals(context) || "metricgroup".equals(context))) {
                visitorHelper.uploadMetrics(attribute, inputComponent);
            }
        }
    }

    /**
     * Upload warnings only to Sonar database by Sonar API calls.
     *
     * @param node Node of the result graph.
     */
    protected void uploadWarnings(Node node) {
        List<Attribute> attributes = node.getAttributes();
        for (Attribute attribute : attributes) {
            String context = attribute.getContext();
            if ("warning".equals(context)) {
                visitorHelper.uploadWarnings(attribute);
            }
        }
    }

    /**
     * Reads the metrics from the attribute and sets their values.
     *
     * @param attribute Contains metrics.
     * @param metrics Metrics to be set.
     */
    protected void readMetrics(Attribute attribute, MetricsInt metrics) {
        for (Field field : metrics.getClass().getFields()) {
            if (!field.getName().equals(attribute.getName())) {
                continue;
            }
            if (attribute.getType().equals(Attribute.aType.atInt)) {
                int value = ((AttributeInt)attribute).getValue();
                try {
                    field.setInt(metrics, value);
                } catch (IllegalAccessException e) {
                    LOG.error("Error during reading metrics from graph!");
                }
            } else if (attribute.getType().equals(Attribute.aType.atFloat)) {
                float value = ((AttributeFloat)attribute).getValue();
                if (Double.isNaN(value)) {
                    value = 0;
                }
                try {
                    field.setFloat(metrics, value);
                } catch (IllegalAccessException e) {
                    LOG.error("Error during reading metrics from graph!");
                }
            }
        }
    }

    /**
     * Reads the positions from the attribute and sets their values.
     *
     * @param positionAttribute Contains positions.
     * @param positionsList List of positions to be set.
     */
    protected void readPosition(Attribute positionAttribute, List positionsList) {
        String path = "";
        int line = 0;
        List positionsListTemp = positionAttribute.getAttributes();
        ListIterator posIter = positionsListTemp.listIterator();
        while (posIter.hasNext()) {
            Attribute tempPos = (Attribute) posIter.next();
            if ("Path".equals(tempPos.getName())) {
                path = ((AttributeString)tempPos).getValue();
                FileSystem fs = visitorHelper.getFileSystem();
                InputFile file = fs.inputFile(fs.predicates().hasPath(path));
                if (file != null) {
                    path = file.relativePath();
                }
            } else if ("Line".equals(tempPos.getName())) {
                line = ((AttributeInt)tempPos).getValue();
            }
        }
        positionsList.add(new Position(path, line));
    }

}
