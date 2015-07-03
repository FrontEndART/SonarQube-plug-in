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
package com.sourcemeter.analyzer.java.helper;

import com.sourcemeter.analyzer.base.core.resources.BaseClass;
import com.sourcemeter.analyzer.base.core.resources.BaseResource;
import com.sourcemeter.analyzer.base.helper.DecoratorHelper;

import java.util.Collection;

import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Resource;

public class DecoratorHelperJava extends DecoratorHelper {

    /**
     * Constructor
     *
     * @param context
     */
    public DecoratorHelperJava(DecoratorContext context) {
        super(context);
    }

    /**
     * Summerize and save the measure values of files' classes
     *
     * @param metric
     * @param context
     */
    public void sumChildClassMeasures(Metric metric, DecoratorContext context, boolean sumAnonymousAndLocal) {
        Measure measure = context.getMeasure(metric);
        if (measure == null) {
            return;
        }

        Double sum = 0.0;
        Collection<DecoratorContext> childContexts = context.getChildren();
        for (DecoratorContext decoratorContext : childContexts) {
            Resource contextResource = decoratorContext.getResource();
            if (contextResource instanceof BaseClass) {
                if (!sumAnonymousAndLocal
                        && contextResource.getName().contains("$")) {
                    continue;
                }
                Measure childMeasure = decoratorContext.getMeasure(metric);
                if (childMeasure != null) {
                    sum += childMeasure.getValue();
                }
            }
        }

        measure.setValue(sum);
        context.saveMeasure(measure);
    }

    public void sumChildClassMeasuresAndCreateMeasure(Metric metric,
            DecoratorContext context, boolean sumAnonymousAndLocal) {
        Measure measure = context.getMeasure(metric);
        if (measure == null) {
            measure = new Measure(metric, 0.0);
            context.saveMeasure(measure);
        }

        sumChildClassMeasures(metric, context, sumAnonymousAndLocal);
    }

    /**
     * Calculate the average and save the measure values of files' classes
     *
     * @param metric
     * @param context
     */
    public void avgChildClassMeasures(Metric metric, DecoratorContext context, boolean sumAnonymousAndLocal) {
        Measure measure = context.getMeasure(metric);
        if (measure == null) {
            return;
        }

        Double sum = 0.0;
        int count = 0;
        Collection<DecoratorContext> childContexts = context.getChildren();
        for (DecoratorContext decoratorContext : childContexts) {
            Resource contextResource = decoratorContext.getResource();
            if (contextResource instanceof BaseResource) {
                if (!sumAnonymousAndLocal && contextResource.getName().contains("$")) {
                    continue;
                }
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

    public void avgChildClassMeasuresAndCreateMeasure(Metric metric,
            DecoratorContext context, boolean sumAnonymousAndLocal) {
        Measure measure = context.getMeasure(metric);
        if (measure == null) {
            measure = new Measure(metric, 0.0);
            context.saveMeasure(measure);
        }

        avgChildClassMeasures(metric, context, sumAnonymousAndLocal);
    }
}
