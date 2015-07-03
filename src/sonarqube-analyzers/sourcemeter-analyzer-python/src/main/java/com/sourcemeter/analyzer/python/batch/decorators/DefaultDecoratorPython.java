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
package com.sourcemeter.analyzer.python.batch.decorators;

import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreMetrics;

import com.sourcemeter.analyzer.base.batch.decorators.DefaultDecorator;
import com.sourcemeter.analyzer.base.helper.DecoratorHelper;

/**
 * Decorator class for matching SourceMeter and SonarQube metrics
 */
public class DefaultDecoratorPython extends DefaultDecorator {

    public DefaultDecoratorPython(FileSystem fileSystem) {
        super(fileSystem);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    protected DecoratorHelper getDecoratorHelper(DecoratorContext context) {
        return new DecoratorHelper(context);
    }

    @Override
    protected void decorateClassMetrics(DecoratorHelper helper) {
        helper.copyMetricResult(SourceMeterCoreMetrics.NLM, CoreMetrics.FUNCTIONS);
        helper.copyMetricResult(SourceMeterCoreMetrics.WMC, CoreMetrics.COMPLEXITY);
    }

    @Override
    protected void decorateClassAndMethodMetrics(DecoratorHelper helper) {
        helper.copyMetricResult(SourceMeterCoreMetrics.LLOC, CoreMetrics.NCLOC);
        helper.copyMetricResult(SourceMeterCoreMetrics.LOC, CoreMetrics.LINES);
        helper.copyMetricResult(SourceMeterCoreMetrics.NOS, CoreMetrics.STATEMENTS);
    }
}
