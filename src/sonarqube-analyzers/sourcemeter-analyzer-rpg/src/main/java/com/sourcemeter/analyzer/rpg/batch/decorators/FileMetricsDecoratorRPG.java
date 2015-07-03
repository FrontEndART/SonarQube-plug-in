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
package com.sourcemeter.analyzer.rpg.batch.decorators;

import com.sourcemeter.analyzer.base.batch.decorators.FileMetricsDecorator;
import com.sourcemeter.analyzer.base.helper.DecoratorHelper;
import com.sourcemeter.analyzer.rpg.core.RPG;

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

@Phase(name = Phase.Name.POST)
public class FileMetricsDecoratorRPG extends FileMetricsDecorator {

    private static final Logger LOG = LoggerFactory.getLogger(FileMetricsDecoratorRPG.class);


    public FileMetricsDecoratorRPG(FileSystem fileSystem) {
        super(fileSystem);
    }

    @Override
    public void decorate(Resource resource, DecoratorContext context) {
        DecoratorHelper helper = getDecoratorHelper(context);
        try {
            if (Qualifiers.isFile(resource) && RPG.KEY.equals(resource.getLanguage().getKey())) {
                Measure smResourceMeasure = new Measure(SourceMeterCoreMetrics.SM_RESOURCE);
                smResourceMeasure.setValue(1.0);
                context.saveMeasure(smResourceMeasure);

                decorateMetricsWithHelper(resource, context, helper);
                saveFunctionsComplexityDistribution(context);
                saveFilesComplexityDistribution(context);
            }
        } catch (SonarException e) {
            LOG.error("Cannot decorate file metrics! Cause: " + e.getMessage());
        }
    }

    @Override
    protected void decorateMetricsWithHelper(Resource resource,
            DecoratorContext context, DecoratorHelper helper) {
        helper.sumChildClassMeasures(SourceMeterCoreMetrics.TLLOC);
        helper.copyMetricResult(SourceMeterCoreMetrics.TLLOC, CoreMetrics.NCLOC);
        helper.sumChildClassMeasures(SourceMeterCoreMetrics.TLOC);
        helper.copyMetricResult(SourceMeterCoreMetrics.TLOC, CoreMetrics.LINES);

        helper.sumChildClassMeasures(CoreMetrics.STATEMENTS);
        helper.sumChildClassMeasures(CoreMetrics.ACCESSORS);

        helper.sumChildClassMeasures(CoreMetrics.COMMENT_LINES);
        helper.avgChildClassMeasures(CoreMetrics.COMMENT_LINES_DENSITY);
        helper.sumChildClassMeasures(CoreMetrics.PUBLIC_API);
        helper.sumChildClassMeasures(CoreMetrics.PUBLIC_UNDOCUMENTED_API);
        helper.avgChildClassMeasures(CoreMetrics.PUBLIC_DOCUMENTED_API_DENSITY);

        helper.sumChildClassMeasures(CoreMetrics.DUPLICATED_BLOCKS);
        helper.sumChildClassMeasures(CoreMetrics.DUPLICATED_LINES);
        helper.avgChildClassMeasures(CoreMetrics.DUPLICATED_LINES_DENSITY);

        helper.avgChildClassMeasuresAndCreateMeasure(CoreMetrics.FUNCTION_COMPLEXITY);
    }

    @Override
    protected DecoratorHelper getDecoratorHelper(DecoratorContext context) {
        return new DecoratorHelper(context);
    }
}