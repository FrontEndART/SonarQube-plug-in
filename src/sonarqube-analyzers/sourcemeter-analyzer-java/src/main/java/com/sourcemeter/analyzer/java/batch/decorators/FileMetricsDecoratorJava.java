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
package com.sourcemeter.analyzer.java.batch.decorators;

import com.sourcemeter.analyzer.base.batch.decorators.FileMetricsDecorator;
import com.sourcemeter.analyzer.base.helper.DecoratorHelper;
import com.sourcemeter.analyzer.java.helper.DecoratorHelperJava;

import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.resources.Resource;

public class FileMetricsDecoratorJava extends FileMetricsDecorator {

    public FileMetricsDecoratorJava(FileSystem fileSystem) {
        super(fileSystem);
    }

    @Override
    protected void decorateMetricsWithHelper(Resource resource,
            DecoratorContext context, DecoratorHelper helper) {
        DecoratorHelperJava decoratorHelperJava = (DecoratorHelperJava) helper;
        decoratorHelperJava.sumChildClassMeasures(CoreMetrics.STATEMENTS, context, true);
        decoratorHelperJava.sumChildClassMeasures(CoreMetrics.ACCESSORS, context, true);

        decoratorHelperJava.sumChildClassMeasures(CoreMetrics.COMMENT_LINES, context, true);
        decoratorHelperJava.avgChildClassMeasures(CoreMetrics.COMMENT_LINES_DENSITY, context, true);
        decoratorHelperJava.sumChildClassMeasures(CoreMetrics.PUBLIC_API, context, true);
        decoratorHelperJava.sumChildClassMeasures(CoreMetrics.PUBLIC_UNDOCUMENTED_API, context, true);
        decoratorHelperJava.avgChildClassMeasures(CoreMetrics.PUBLIC_DOCUMENTED_API_DENSITY, context, true);

        decoratorHelperJava.sumChildClassMeasures(CoreMetrics.DUPLICATED_BLOCKS, context, false);
        decoratorHelperJava.sumChildClassMeasures(CoreMetrics.DUPLICATED_LINES, context, false);

        decoratorHelperJava.avgChildClassMeasures(CoreMetrics.FUNCTION_COMPLEXITY, context, true);
        decoratorHelperJava.avgChildClassMeasures(CoreMetrics.CLASS_COMPLEXITY, context, true);
        decoratorHelperJava.sumChildClassMeasures(CoreMetrics.FUNCTIONS, context, true);
    }

    @Override
    protected DecoratorHelper getDecoratorHelper(DecoratorContext context) {
        return new DecoratorHelperJava(context);
    }

    @Override
    protected void saveFilesComplexityDistribution(DecoratorContext context) {
        // default Java plugin handles the file complexity distribution
    }
}