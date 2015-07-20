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
import com.sourcemeter.analyzer.base.core.LicenseInformation;

import java.util.Collection;

import org.sonar.api.batch.Decorator;
import org.sonar.api.batch.DecoratorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Qualifiers;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreInitializer;

import com.google.gson.Gson;

public abstract class LicenseDecorator implements Decorator {

    private final Metric licenseMetric;

    public LicenseDecorator(Metric licenseMetric) {
        this.licenseMetric = licenseMetric;
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        return SourceMeterCoreInitializer.allDetectedLanguages
                .contains(SourceMeterInitializer.getPluginLanguage().getKey());
    }

    @Override
    public void decorate(Resource resource, DecoratorContext context) {
        if (Qualifiers.isProject(resource, false)) {
            Measure licenseMeasure = context.getMeasure(licenseMetric);
            if (licenseMeasure == null) {
                LicenseInformation licenseInformation = null;
                Gson gson = new Gson();

                Collection<DecoratorContext> childContexts = context.getChildren();
                for (DecoratorContext childContext : childContexts) {
                    Measure childLicenseMeasure = childContext.getMeasure(licenseMetric);
                    if (childLicenseMeasure != null) {
                        LicenseInformation childLicenses = gson.fromJson(childLicenseMeasure.getData(), LicenseInformation.class);
                        if (licenseInformation == null) {
                            licenseInformation = childLicenses;
                        } else {
                            licenseInformation.updateLicenseInformations(childLicenses);
                        }
                    }
                }
                licenseMeasure = new Measure(licenseMetric, gson.toJson(licenseInformation).toString());
                context.saveMeasure(licenseMeasure);
            }
        }
    }
}
