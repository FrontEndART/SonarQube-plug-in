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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.MeasureUtils;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.SonarException;

@Phase(name = Phase.Name.POST)
public class ComplexityPostDecorator extends BaseDecorator {

    private static final Logger LOG = LoggerFactory.getLogger(ComplexityPostDecorator.class);

    public ComplexityPostDecorator(FileSystem fileSystem) {
        super(fileSystem);
    }

    @Override
    public void decorate(Resource resource, DecoratorContext context) {
        try {

            Measure measure;
            Double files = MeasureUtils.getValue(context.getMeasure(CoreMetrics.FILES), null);
            Double complexity = MeasureUtils.getValue(context.getMeasure(CoreMetrics.COMPLEXITY), null);
            if (complexity != null && files != null && files > 0.0) {
                measure = context.getMeasure(CoreMetrics.FILE_COMPLEXITY);
                if (measure != null) {
                    measure.setValue(complexity / files);
                    context.saveMeasure(measure);
                }
            }

            Double functions = MeasureUtils.getValue(context.getMeasure(CoreMetrics.FUNCTIONS), null);
            complexity = MeasureUtils.getValue(context.getMeasure(CoreMetrics.COMPLEXITY), null);
            if (complexity != null && functions != null && functions > 0.0) {
                measure = context.getMeasure(CoreMetrics.FUNCTION_COMPLEXITY);
                if (measure != null) {
                    measure.setValue(complexity / functions);
                    context.saveMeasure(measure);
                }
            }

            Double classes = MeasureUtils.getValue(context.getMeasure(CoreMetrics.CLASSES), null);
            complexity = MeasureUtils.getValue(context.getMeasure(CoreMetrics.COMPLEXITY), null);
            if (complexity != null && classes != null && classes > 0.0) {
                measure = context.getMeasure(CoreMetrics.CLASS_COMPLEXITY);
                if (measure != null) {
                    measure.setValue(complexity / classes);
                    context.saveMeasure(measure);
                }
            }

        } catch (SonarException e) {
            LOG.error("Cannot decorate complexity metrics! Cause: " + e.getMessage());
        }

    }

}
