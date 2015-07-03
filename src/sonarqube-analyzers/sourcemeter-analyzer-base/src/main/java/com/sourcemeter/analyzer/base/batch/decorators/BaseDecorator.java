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
import com.sourcemeter.analyzer.base.core.resources.BaseMethod;

import org.sonar.api.batch.Decorator;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.measures.RangeDistributionBuilder;
import org.sonar.api.resources.Project;

public abstract class BaseDecorator implements Decorator {

    private static final Number[] FUNCTIONS_DISTRIB_BOTTOM_LIMITS = {1, 2, 4, 6, 8, 10, 12, 20, 30};
    private static final Number[] FILES_DISTRIB_BOTTOM_LIMITS = {0, 5, 10, 20, 30, 60, 90};

    private final FileSystem fileSystem;

    public BaseDecorator(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        return fileSystem.hasFiles(fileSystem.predicates().hasLanguage(
                SourceMeterInitializer.pluginLanguage.getKey()));
    }

    protected void saveFunctionsComplexityDistribution(DecoratorContext context) {
        RangeDistributionBuilder complexityDistribution = new RangeDistributionBuilder(
                CoreMetrics.FUNCTION_COMPLEXITY_DISTRIBUTION,
                FUNCTIONS_DISTRIB_BOTTOM_LIMITS);

        // FUNCTION_COMPLEXITY_DISTRIBUTION metric is aggregated automatically
        // for parent resources therefore uploading is only needed for functions
        if (!(context.getResource() instanceof BaseMethod)) {
            return;
        }
        Measure complexity = context.getMeasure(CoreMetrics.COMPLEXITY);
        if (complexity != null) {
            complexityDistribution.add(complexity.getValue());
            context.saveMeasure(complexityDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
        }
    }

    protected void saveFilesComplexityDistribution(DecoratorContext context) {
        RangeDistributionBuilder complexityDistribution = new RangeDistributionBuilder(
                CoreMetrics.FILE_COMPLEXITY_DISTRIBUTION,
                FILES_DISTRIB_BOTTOM_LIMITS);
        Measure measure = context.getMeasure(CoreMetrics.COMPLEXITY);
        if (measure != null) {
            complexityDistribution.add(measure.getValue());
            context.saveMeasure(complexityDistribution.build().setPersistenceMode(PersistenceMode.MEMORY));
        }
    }
}
