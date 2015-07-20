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
package com.sourcemeter.analyzer.base.batch.decorators;

import com.sourcemeter.analyzer.base.batch.SourceMeterInitializer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sonar.api.batch.Decorator;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.Phase;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreInitializer;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreMetrics;

@Phase(name = Phase.Name.POST)
public class ProjectDecorator implements Decorator {

    private static final Map<Metric, Metric> weightedMetrics;
    static {
        weightedMetrics = new HashMap<>();
        weightedMetrics.put(SourceMeterCoreMetrics.TAD, SourceMeterCoreMetrics.TLOC);
        weightedMetrics.put(SourceMeterCoreMetrics.TCD, SourceMeterCoreMetrics.TCLOC);
        weightedMetrics.put(SourceMeterCoreMetrics.TPDA, SourceMeterCoreMetrics.TLOC);
        weightedMetrics.put(SourceMeterCoreMetrics.TPUA, SourceMeterCoreMetrics.TLOC);
        weightedMetrics.put(SourceMeterCoreMetrics.CLC, SourceMeterCoreMetrics.TLOC);
        weightedMetrics.put(SourceMeterCoreMetrics.CLLC, SourceMeterCoreMetrics.TLLOC);
        weightedMetrics.put(SourceMeterCoreMetrics.CC, SourceMeterCoreMetrics.TLOC);
        weightedMetrics.put(SourceMeterCoreMetrics.CCO, SourceMeterCoreMetrics.CI);
        weightedMetrics.put(SourceMeterCoreMetrics.NCR, SourceMeterCoreMetrics.CCL);
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        return  SourceMeterCoreInitializer.allDetectedLanguages
                .contains(SourceMeterInitializer.getPluginLanguage().getKey());
    }

    @Override
    public void decorate(Resource resource, DecoratorContext context) {
        //execute only on projects, not modules
        if (Qualifiers.isProject(resource, false)) {
            Set<Metric> allMetrics = new HashSet<>();
            List<Set<Measure>> measuresForLanguages = SourceMeterCoreInitializer.MEASURES_FOR_LANGUAGES;

            // get all the metrics from each plugin's stored measures
            for (Set<Measure> measures : measuresForLanguages) {
                for (Measure measure : measures) {
                    allMetrics.add(measure.getMetric());
                }
            }

            for (Metric metric : allMetrics) {
                if (context.getMeasure(metric) != null) {
                    continue;
                }
                if (weightedMetrics.containsKey(metric)
                        || metric.getType() == ValueType.PERCENT) {
                    avgTargetMetrics(context, metric, measuresForLanguages);
                } else {
                    sumTargetMetrics(context, metric, measuresForLanguages);
                }
            }
        }
    }

    /**
     * Sums the target metric from the given Sets and saves it to the context.
     *
     * @param context
     * @param metric
     * @param measuresForLanguages
     */
    private void sumTargetMetrics(DecoratorContext context, Metric metric,
            List<Set<Measure>> measuresForLanguages) {
        int sum = 0;

        for (Set<Measure> measures : measuresForLanguages) {
            sum += (int) getMetricValueFromMeasures(measures, metric);
        }

        Measure measure = new Measure(metric);
        measure.setIntValue(sum);
        context.saveMeasure(measure);
    }

    /**
     * Averages the target metric from the given Sets and saves it to the
     * context. If a metric's average should be weighted, it's value is weighted
     * by another metric.
     *
     * @param context
     * @param metric
     * @param measuresForLanguages
     */
    private void avgTargetMetrics(DecoratorContext context, Metric metric,
            List<Set<Measure>> measuresForLanguages) {
        double avg = 0;
        double sumWeightedByMetrics = 0.0;
        boolean shouldBeWeighted = weightedMetrics.containsKey(metric);

        for (Set<Measure> measures : measuresForLanguages) {
            double currentValue = getMetricValueFromMeasures(measures, metric);
            if (shouldBeWeighted) {
                double weightBy = getMetricValueFromMeasures(measures,
                        weightedMetrics.get(metric));
                if (weightBy > 0.0) {
                    sumWeightedByMetrics += weightBy;
                    currentValue *= weightBy;
                } else {
                    sumWeightedByMetrics++;
                }
            } else {
                sumWeightedByMetrics++;
            }
            avg += currentValue;
        }

        if (sumWeightedByMetrics > 0.0) {
            avg /= sumWeightedByMetrics;
        }

        Measure measure = new Measure(metric, avg);
        context.saveMeasure(measure);
    }

    /**
     * Searches for a metric in the given Set and returns it's value.
     *
     * @param measures
     * @param metric
     * @return the metric's value.
     */
    private double getMetricValueFromMeasures(Set<Measure> measures,
            Metric metric) {
        double val = 0.0;
        for (Measure measure : measures) {
            if (measure.getMetric().equals(metric)) {
                if (measure.getValue() != null) {
                    val = measure.getValue();
                } else if (measure.getIntValue() != null) {
                    val = measure.getIntValue();
                }
                break;
            }
        }

        return val;
    }
}