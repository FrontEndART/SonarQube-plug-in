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
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreMetrics;

import com.sourcemeter.analyzer.base.batch.SourceMeterInitializer;
import com.sourcemeter.analyzer.base.core.resources.BaseClass;
import com.sourcemeter.analyzer.base.core.resources.BaseMethod;
import com.sourcemeter.analyzer.base.core.resources.CloneClass;
import com.sourcemeter.analyzer.base.core.resources.CloneInstance;
import com.sourcemeter.analyzer.base.helper.DecoratorHelper;

/**
 * Decorator class for matching SourceMeter and SonarQube metrics
 */
public abstract class DefaultDecorator extends BaseDecorator {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultDecorator.class);

    public DefaultDecorator(FileSystem fileSystem) {
        super(fileSystem);
    }

    @Override
    public void decorate(Resource resource, DecoratorContext context) {
        if (resource.getLanguage() != null
            && !SourceMeterInitializer.pluginLanguage.getKey().equals(resource.getLanguage().getKey())) {
            return;
        }

        if (resource instanceof CloneClass || resource instanceof CloneInstance) {
            return;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(getClass().getSimpleName() + ": " + resource.getLongName() + " ["
                      + Runtime.getRuntime().totalMemory() / (1024 * 1024) + " MB]");
        }

        DecoratorHelper helper = getDecoratorHelper(context);

        try {
            if (resource instanceof BaseClass || resource instanceof BaseMethod) {
                // Class + method
                decorateClassAndMethodMetrics(helper);
                helper.copyMetricResult(SourceMeterCoreMetrics.CLOC, CoreMetrics.COMMENT_LINES);
                helper.copyMetricResult(SourceMeterCoreMetrics.CD, CoreMetrics.COMMENT_LINES_DENSITY);
                helper.copyMetricResult(SourceMeterCoreMetrics.CI, CoreMetrics.DUPLICATED_BLOCKS);
                helper.copyMetricResult(SourceMeterCoreMetrics.LDC, CoreMetrics.DUPLICATED_LINES);
                helper.copyMetricResult(SourceMeterCoreMetrics.DPF, CoreMetrics.DUPLICATED_FILES);

                if (resource instanceof BaseClass) { // Only class
                    decorateClassMetrics(helper);
                } else if (resource instanceof BaseMethod) { // Only method/function
                    decorateMethodMetrics(helper);
                    saveFunctionsComplexityDistribution(context);
                }
            }
        } catch (SonarException e) {
            LOG.error("Cannot decorate complexity metrics! Cause: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * Decorates metrics specified only for classes
     *
     * @param helper
     */
    protected void decorateClassMetrics(DecoratorHelper helper) {
        helper.copyMetricResult(SourceMeterCoreMetrics.NLM, CoreMetrics.FUNCTIONS);

        // public_api = NPM+NPA
        helper.sumAndCopyMeasures(CoreMetrics.PUBLIC_API, SourceMeterCoreMetrics.NLPM, SourceMeterCoreMetrics.NLPA);

        helper.copyMetricResult(SourceMeterCoreMetrics.AD, CoreMetrics.PUBLIC_DOCUMENTED_API_DENSITY);
        helper.copyMetricResult(SourceMeterCoreMetrics.PUA, CoreMetrics.PUBLIC_UNDOCUMENTED_API);
        helper.copyMetricResult(SourceMeterCoreMetrics.WMC, CoreMetrics.COMPLEXITY);
    }

    /**
     * Decorates metrics specified only for methods
     * 
     * @param helper
     */
    protected void decorateMethodMetrics(DecoratorHelper helper) {
        helper.copyMetricResult(SourceMeterCoreMetrics.McCC, CoreMetrics.COMPLEXITY);
    }

    /**
     * Decorates metrics for both classes and methods
     *
     * @param helper
     */
    protected void decorateClassAndMethodMetrics(DecoratorHelper helper) {
        helper.copyMetricResult(SourceMeterCoreMetrics.LLOC, CoreMetrics.NCLOC);
        helper.copyMetricResult(SourceMeterCoreMetrics.LOC, CoreMetrics.LINES);
        helper.copyMetricResult(SourceMeterCoreMetrics.NOS, CoreMetrics.STATEMENTS);
        helper.sumAndCopyMeasures(CoreMetrics.ACCESSORS, SourceMeterCoreMetrics.TNG, SourceMeterCoreMetrics.TNS);
    }

    protected abstract DecoratorHelper getDecoratorHelper(DecoratorContext context);
}
