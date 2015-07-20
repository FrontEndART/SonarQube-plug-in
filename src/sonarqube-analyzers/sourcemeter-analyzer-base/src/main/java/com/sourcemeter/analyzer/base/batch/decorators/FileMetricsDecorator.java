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

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreMetrics;

import com.sourcemeter.analyzer.base.batch.SourceMeterInitializer;
import com.sourcemeter.analyzer.base.core.resources.BaseClass;
import com.sourcemeter.analyzer.base.core.resources.BaseResource;
import com.sourcemeter.analyzer.base.helper.DecoratorHelper;

@Phase(name = Phase.Name.POST)
public abstract class FileMetricsDecorator extends BaseDecorator {

    protected static final Logger LOG = LoggerFactory.getLogger(FileMetricsDecorator.class);

    public FileMetricsDecorator(FileSystem fileSystem) {
        super(fileSystem);
    }

    @Override
    public void decorate(Resource resource, DecoratorContext context) {
        DecoratorHelper helper = getDecoratorHelper(context);

        if (resource.getLanguage() == null) {
            return;
        }

        try {
            if (Qualifiers.isFile(resource)
                && SourceMeterInitializer.getPluginLanguage()
                                         .getKey()
                                         .equals(resource.getLanguage().getKey())) {
                sumClasses(context);

                if (context.getMeasure(SourceMeterCoreMetrics.SM_RESOURCE) == null) {
                    Measure smResourceMeasure = new Measure(SourceMeterCoreMetrics.SM_RESOURCE);
                    smResourceMeasure.setValue(1.0);
                    context.saveMeasure(smResourceMeasure);
                }

                decorateComplexity(helper);
                decorateMetricsWithHelper(resource, context, helper);
                saveFilesComplexityDistribution(context);

                Measure m = context.getMeasure(CoreMetrics.DUPLICATIONS_DATA);
                if (m != null) {
                    context.saveMeasure(SourceMeterCoreMetrics.DPF, 1.0);
                }
            }
        } catch (SonarException e) {
            LOG.error("Cannot decorate file metrics! Cause: " + e.getMessage());
        }
    }

    /**
     * Summarize the number of classes in the file.
     *
     * @param context
     */
    protected void sumClasses(DecoratorContext context) {
        Measure measure = context.getMeasure(CoreMetrics.CLASSES);
        if (measure == null) {
            measure = new Measure(CoreMetrics.CLASSES);
        }
        Double classes = 0.0;
        Collection<DecoratorContext> childContexts = context.getChildren();
        for (DecoratorContext decoratorContext : childContexts) {
            if (decoratorContext.getResource() instanceof BaseClass) {
                classes++;
            }
        }
        measure.setValue(classes);
        context.saveMeasure(measure);
    }

    /**
     * Decorate language specific metrics.
     *
     * @param resource
     * @param context
     * @param helper
     */
    protected abstract void decorateMetricsWithHelper(Resource resource,
                                                      DecoratorContext context,
                                                      DecoratorHelper helper);

    /**
     * Returns the language specific DecoratorHelper object.
     *
     * @param context
     * @return
     */
    protected abstract DecoratorHelper getDecoratorHelper(DecoratorContext context);

    /**
     * Decorates complexity metric for files. By default, it sums the child
     * resources complexity value.
     *
     * @param helper
     */
    protected void decorateComplexity(DecoratorHelper helper) {
        helper.sumChildMeasures(CoreMetrics.COMPLEXITY, BaseResource.class, false);
    }
}