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

import com.sourcemeter.analyzer.base.core.resources.BaseClass;
import com.sourcemeter.analyzer.base.core.resources.CloneClass;

import java.util.Collection;

import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Resource;

public class DecoratorHelper {

    public static final double MISSING_VALUE = 0.0;

    protected final DecoratorContext context;

    /**
     * Constructor
     *
     * @param context
     */
    public DecoratorHelper(DecoratorContext context) {
        this.context = context;
    }

    /**
     * Copy source metric's measure value to target metric's measure value
     *
     * @param context
     * @param sourceMetric
     * @param targetMetric
     * @return measure value
     */
    public Double copyMetricResult(Metric sourceMetric, Metric targetMetric) {
        Measure srcMeasure = this.context.getMeasure(sourceMetric);
        Measure dstMeasure = this.context.getMeasure(targetMetric);

        if (dstMeasure == null) {
            dstMeasure = new Measure(targetMetric);
        }

        if (srcMeasure != null) {
            Double value = srcMeasure.getValue();
            if (value != null) {
                dstMeasure.setValue(value);
                this.context.saveMeasure(dstMeasure);
                return value;
            }
        } /* else { dstMeasure.setValue(MISSING_VALUE); this.context.saveMeasure(dstMeasure); } */
        return 0.0;
    }

    /**
     * Copy source metric's measure value to target metric's measure value
     *
     * @param context
     * @param sourceMetric
     * @param targetMetric
     * @return measure value
     */
    public Double sumAndCopyMeasures(Metric targetMetric, Metric... sourceMetric) {
        Double sum = 0.0;
        Measure dstMeasure = this.context.getMeasure(targetMetric);
        if (dstMeasure == null) {
            dstMeasure = new Measure(targetMetric);
        }

        for (Metric metric : sourceMetric) {
            Measure srcMeasure = this.context.getMeasure(metric);
            if (srcMeasure != null) {
                Double value = srcMeasure.getValue();
                if (value != null) {
                    sum += value;
                }
            }
        }

        dstMeasure.setValue(sum);
        this.context.saveMeasure(dstMeasure);
        return sum;
    }

    /**
     * Summerize and save the measure values of files' classes
     *
     * @param metric
     */
    public void sumChildClassMeasures(Metric metric) {
        sumChildMeasures(metric, BaseClass.class, false);
    }

    /**
     * Summerize and save the measure values of files' clone classes
     *
     * @param metric
     */
    public void sumChildCloneClassMeasures(Metric metric) {
        sumChildMeasures(metric, CloneClass.class, true);
    }

    /**
     * Summarize and save the child measure values of a context
     *
     * @param <T>
     *
     * @param metric
     * @param resourceClass
     */
    public <T> void sumChildMeasures(Metric metric, Class<T> resourceClass, boolean createMeasure) {
        Measure measure = context.getMeasure(metric);
        if (measure == null) {
            if (createMeasure) {
                measure = new Measure(metric);
            } else {
                return;
            }
        }

        Double sum = 0.0;
        Collection<DecoratorContext> childContexts = context.getChildren();
        for (DecoratorContext decoratorContext : childContexts) {
            Resource contextResource = decoratorContext.getResource();
            if (!resourceClass.isInstance(contextResource)) {
                continue;
            }
            Measure childMeasure = decoratorContext.getMeasure(metric);
            if (childMeasure != null) {
                sum += childMeasure.getValue();
            }
        }

        measure.setValue(sum);
        context.saveMeasure(measure);
    }

    /**
     * Summerize and save the child measure values of a context. If the metric
     * doesn't exist for the context, it is created.
     *
     * @param metric
     */
    public void sumChildClassMeasuresAndCreateMeasure(Metric metric) {
        Measure measure = context.getMeasure(metric);
        if (measure == null) {
            measure = new Measure(metric, 0.0);
            context.saveMeasure(measure);
        }

        sumChildClassMeasures(metric);
    }

    /**
     * Calculate the average and save the measure values of context's classes
     *
     * @param metric
     * @param context
     */
    public void avgChildClassMeasures(Metric metric) {
        avgChildMeasures(metric, BaseClass.class);
    }

    /**
     * Calculate the average and save the measure values of context's custom
     * resources
     *
     * @param metric
     * @param resourceClass
     */
    public <T> void avgChildMeasures(Metric metric, Class<T> resourceClass) {
        Measure measure = context.getMeasure(metric);
        if (measure == null) {
            return;
        }

        Double sum = 0.0;
        int count = 0;
        Collection<DecoratorContext> childContexts = context.getChildren();
        for (DecoratorContext decoratorContext : childContexts) {
            Resource contextResource = decoratorContext.getResource();
            if (resourceClass.isInstance(contextResource)) {
                Measure childMeasure = decoratorContext.getMeasure(metric);
                if (childMeasure != null && !childMeasure.getValue().isNaN()) {
                    sum += childMeasure.getValue();
                    count++;
                }
            }
        }
        if (count > 0) {
            measure.setValue(sum / count);
            context.saveMeasure(measure);
        }
    }

    /**
     * Calculate the average and save the measure values of a context's classes.
     * If the metric doesn't exist for the context, it is created. If the metric
     *
     * @param metric
     * @param context
     */
    public void avgChildClassMeasuresAndCreateMeasure(Metric metric) {
        Measure measure = context.getMeasure(metric);
        if (measure == null) {
            measure = new Measure(metric, 0.0);
            context.saveMeasure(measure);
        }

        avgChildClassMeasures(metric);
    }

    /**
     * Sums a weighted average of the target metric from the given child
     * resources
     *
     * @param targetMetric
     * @param weightedBy
     * @param resourceClass
     */
    public <T> void countWeightedAverageOfMetric(Metric targetMetric,
            Metric weightedBy, Class<T> resourceClass) {
        Measure measure = context.getMeasure(targetMetric);
        double avg = 0.0;
        double sumWeightedMetrics = 0.0;
        if (measure == null) {
            measure = new Measure(targetMetric);
        }

        Collection<DecoratorContext> childContexts = context.getChildren();
        for (DecoratorContext decoratorContext : childContexts) {
            Resource contextResource = decoratorContext.getResource();
            if (resourceClass.isInstance(contextResource)) {
                Measure childWeightedByMeasure = decoratorContext.getMeasure(weightedBy);
                Measure childTargetMeasure = decoratorContext
                        .getMeasure(targetMetric);
                if (childWeightedByMeasure != null
                        && childTargetMeasure != null
                        && childWeightedByMeasure.getValue() != null
                        && childTargetMeasure.getValue() != null
                        && !childWeightedByMeasure.getValue().isNaN()
                        && !childTargetMeasure.getValue().isNaN()) {
                    sumWeightedMetrics += childWeightedByMeasure.getValue();
                    avg += childWeightedByMeasure.getValue() * childTargetMeasure.getValue();
                }
            }
        }

        if (avg != 0.0) {
            avg /= sumWeightedMetrics;
        }

        measure.setValue(avg);
        context.saveMeasure(measure);
    }
}
