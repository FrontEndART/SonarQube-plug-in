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
package com.sourcemeter.analyzer.base.helper;

import graphlib.Attribute;
import graphlib.Attribute.aType;
import graphlib.AttributeFloat;
import graphlib.AttributeInt;
import graphlib.Node;
import graphsupportlib.Metric.Position;
import com.sourcemeter.analyzer.base.batch.ProfileInitializer;
import com.sourcemeter.analyzer.base.visitor.BaseVisitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterMetricFinder;

/**
 * Helper class for Visitor classes to upload metrics and warnings.
 */
public abstract class VisitorHelper {

    protected static final Logger LOG = LoggerFactory.getLogger(VisitorHelper.class);
    protected static final String METRIC_PREFIX = "MET_";

    protected final Project project;
    protected final SensorContext sensorContext;
    protected final ResourcePerspectives perspectives;
    protected final Settings settings;
    protected final SourceMeterMetricFinder metricFinder;

    public VisitorHelper(Project project, SensorContext sensorContext,
            ResourcePerspectives perspectives, Settings settings,
            SourceMeterMetricFinder metricFinder) {
        this.project = project;
        this.sensorContext = sensorContext;
        this.perspectives = perspectives;
        this.settings = settings;
        this.metricFinder = metricFinder;
    }

    /**
     * Upload warnings for the given resource
     *
     * @param attribute
     * @param node
     * @param resource
     * @param nodePosition
     */
    public abstract void uploadWarnings(Attribute attribute, Node node,
            Position nodePosition);

    /**
     * If a Ruleset metric for the current language is not uploaded, it will be
     * uploaded with 0 value.
     *
     * @param metrics
     */
    public void uploadEmptyRulesetMetricsByZero(Resource resource) {
        for (Metric metric : this.metricFinder.findLanguageSpecificRulesetMetrics()) {
            Measure measure = this.sensorContext.getMeasure(resource, metric);
            if (measure != null) {
                continue;
            }
            measure = new Measure(metric);
            measure.setValue(0.0);
            this.sensorContext.saveMeasure(resource, measure);
        }
    }

    /**
     * Upload metrics from the given attribute to the given resource.
     *
     * @param metricAttribute
     * @param resource
     */
    public void uploadMetrics(Attribute metricAttribute, Resource resource) {
        try {
            Measure measure = createMeasureFromAttribute(metricAttribute, resource);
            if (measure != null) {
                this.sensorContext.saveMeasure(resource, measure);
            }
        } catch (SonarException e) {
            LOG.warn(e.getMessage());
            if (resource != null) {
                LOG.warn("Resource: " + resource.getName());
            }
        }
    }

    /**
     * Creates and returns a measure from the given metric attribute for the
     * given resource.
     *
     * @param metricAttribute
     * @param resource
     * @return
     */
    public Measure createMeasureFromAttribute(Attribute metricAttribute, Resource resource) {
        aType metricType = metricAttribute.getType();
        Metric metric = this.metricFinder.findByKey(metricAttribute.getName());
        Measure measure = null;
        if (metric != null) {
            measure = this.sensorContext.getMeasure(resource, metric);
            if (measure == null) {
                measure = new Measure(metric);
            }
            if (metricType == aType.atInt) {
                int value = ((AttributeInt) metricAttribute).getValue();
                measure.setIntValue(value);
            } else if (metricType == aType.atFloat) {
                Double value = (double) ((AttributeFloat) metricAttribute)
                        .getValue();
                if (!value.isNaN() && !value.isInfinite()) {
                    if (metric.getType().equals(ValueType.PERCENT)) {
                        value = value * 100;
                    }
                    measure.setValue(value, BaseVisitor.DEFUALT_PRECISION);
                }
            }
        }

        return measure;
    }

    /**
     * If a rule key is a threshold violation in a graph, the key rule differs
     * from the stored rule in SonarQube. This method corrects it.
     *
     * @param ruleKey
     * @return
     */
    public String getCorrectedRuleKey(String ruleKey) {
        if (ruleKey.contains(ProfileInitializer.CLASS_TRESHOLD_VIOLATION_SUFFIX)) {
            // class treshold violation
            ruleKey = METRIC_PREFIX + ruleKey.replaceAll(ProfileInitializer.CLASS_TRESHOLD_VIOLATION_SUFFIX, "");
        } else if (ruleKey.contains(ProfileInitializer.METHOD_TRESHOLD_VIOLATION_SUFFIX)) {
            // method treshold violation
            ruleKey = METRIC_PREFIX + ruleKey.replaceAll(ProfileInitializer.METHOD_TRESHOLD_VIOLATION_SUFFIX, "");
        } else if (ruleKey.contains(ProfileInitializer.FUNCTION_TRESHOLD_VIOLATION_SUFFIX)) {
            // function treshold violation
            ruleKey = METRIC_PREFIX + ruleKey.replaceAll(ProfileInitializer.FUNCTION_TRESHOLD_VIOLATION_SUFFIX, "");
        } else if (ruleKey.contains(ProfileInitializer.ClONE_CLASS_TRESHOLD_VIOLATION_SUFFIX)) {
            // CloneClass treshold violation
            ruleKey = METRIC_PREFIX + ruleKey.replaceAll(ProfileInitializer.ClONE_CLASS_TRESHOLD_VIOLATION_SUFFIX, "");
        } else if (ruleKey.contains(ProfileInitializer.CLONE_INSTANCE_TRESHOLD_VIOLATION_SUFFIX)) {
            // CloneInstance treshold violation
            ruleKey = METRIC_PREFIX + ruleKey.replaceAll(ProfileInitializer.CLONE_INSTANCE_TRESHOLD_VIOLATION_SUFFIX, "");
        } else {
            String[] splittedKey = ruleKey.split("_");
            ruleKey = splittedKey[splittedKey.length - 1];
        }

        return ruleKey;
    }

    /**
     * Method reads path attribute from the given node. In some analyzer
     * plugins, this method should be overridden for correcting the path
     * attribute.
     *
     * @return
     */
    public String getPathFromNode(Node node) {
        String path = null;
        Position position = graphsupportlib.Metric.getFirstPositionAttribute(node);

        if (null != position) {
            path = position.path;
        }

        return path;
    }
}
