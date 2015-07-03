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
package com.sourcemeter.analyzer.cpp.batch.decorators;

import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.Phase;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreMetrics;

import com.sourcemeter.analyzer.base.batch.decorators.FileMetricsDecorator;
import com.sourcemeter.analyzer.base.core.resources.BaseResource;
import com.sourcemeter.analyzer.base.helper.DecoratorHelper;
import com.sourcemeter.analyzer.cpp.SourceMeterCppMetrics;
import com.sourcemeter.analyzer.cpp.visitor.LogicalTreeLoaderVisitorCpp;

@Phase(name = Phase.Name.POST)
public class FileMetricsDecoratorCpp extends FileMetricsDecorator {

    public FileMetricsDecoratorCpp(FileSystem fileSystem) {
        super(fileSystem);
    }

    @Override
    protected void decorateMetricsWithHelper(Resource resource,
                                             DecoratorContext context,
                                             DecoratorHelper helper) {
        Measure functionsMeasure = context.getMeasure(CoreMetrics.FUNCTIONS);
        if (functionsMeasure == null) {
            functionsMeasure = new Measure(CoreMetrics.FUNCTIONS);
        }
        double numOfFunctions = 0.0;
        if (LogicalTreeLoaderVisitorCpp.FUNCTIONS_FOR_FILES.get(resource) != null) {
            numOfFunctions = LogicalTreeLoaderVisitorCpp.FUNCTIONS_FOR_FILES.get(resource);
        }
        functionsMeasure.setValue(numOfFunctions);
        context.saveMeasure(functionsMeasure);

        helper.copyMetricResult(SourceMeterCppMetrics.LLOC, CoreMetrics.NCLOC);
        helper.copyMetricResult(SourceMeterCoreMetrics.LOC, CoreMetrics.LINES);
        helper.copyMetricResult(SourceMeterCppMetrics.CLOC, CoreMetrics.COMMENT_LINES);
        helper.copyMetricResult(SourceMeterCoreMetrics.NOS, CoreMetrics.STATEMENTS);
        helper.sumAndCopyMeasures(CoreMetrics.PUBLIC_API, SourceMeterCoreMetrics.PDA, SourceMeterCoreMetrics.PUA);
        helper.copyMetricResult(SourceMeterCoreMetrics.PUA, CoreMetrics.PUBLIC_UNDOCUMENTED_API);

        Measure publicDocAPIDensityMeasure = context.getMeasure(CoreMetrics.PUBLIC_DOCUMENTED_API_DENSITY);
        if (publicDocAPIDensityMeasure == null) {
            publicDocAPIDensityMeasure = new Measure(CoreMetrics.PUBLIC_DOCUMENTED_API_DENSITY);
        }
        Measure publicAPIMeasure = context.getMeasure(CoreMetrics.PUBLIC_API);
        Measure publicDocAPIMeasure = context.getMeasure(SourceMeterCoreMetrics.PDA);
        if (publicAPIMeasure != null
            && publicDocAPIMeasure != null
            && publicAPIMeasure.getIntValue() > 0) {

            publicDocAPIDensityMeasure.setValue((publicDocAPIMeasure.getValue() / publicAPIMeasure.getValue()) * 100);
        }

        Measure commentLinesDensityMeasure = context.getMeasure(CoreMetrics.COMMENT_LINES_DENSITY);
        if (commentLinesDensityMeasure == null) {
            commentLinesDensityMeasure = new Measure(CoreMetrics.COMMENT_LINES_DENSITY);
        }
        Measure clocMeasure = context.getMeasure(SourceMeterCppMetrics.CLOC);
        Measure llocMeasure = context.getMeasure(SourceMeterCppMetrics.LLOC);
        if (clocMeasure != null && llocMeasure != null) {
            int cloc = clocMeasure.getIntValue();
            int lloc = llocMeasure.getIntValue();
            if ((cloc + lloc > 0)) {
                commentLinesDensityMeasure.setValue((clocMeasure.getValue() / (llocMeasure.getValue() + clocMeasure.getValue())) * 100);
            } else {
                LOG.warn("Cannot calculate comment lines density [CLOC: " + cloc + ", LLOC: " + lloc + "]");
            }
        }
        helper.sumChildClassMeasures(CoreMetrics.ACCESSORS);

        helper.sumChildCloneClassMeasures(SourceMeterCoreMetrics.CLLOC);
        helper.sumChildCloneClassMeasures(SourceMeterCoreMetrics.CI);
        helper.copyMetricResult(SourceMeterCoreMetrics.CLLOC, CoreMetrics.DUPLICATED_LINES);
        helper.copyMetricResult(SourceMeterCoreMetrics.CI, CoreMetrics.DUPLICATED_BLOCKS);

        helper.avgChildMeasures(CoreMetrics.FUNCTION_COMPLEXITY, BaseResource.class);
        helper.avgChildClassMeasuresAndCreateMeasure(CoreMetrics.CLASS_COMPLEXITY);
    }

    @Override
    protected DecoratorHelper getDecoratorHelper(DecoratorContext context) {
        return new DecoratorHelper(context);
    }

    @Override
    protected void decorateComplexity(DecoratorHelper helper) {
        helper.copyMetricResult(SourceMeterCoreMetrics.McCC, CoreMetrics.COMPLEXITY);
    }
}