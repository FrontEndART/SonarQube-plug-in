/**
 * Copyright (c) 2014-2017, FrontEndART Software Ltd.
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

package com.sourcemeter.analyzer.csharp;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;

import com.sourcemeter.analyzer.base.core.VersionChecker;
import com.sourcemeter.analyzer.base.helper.ThresholdPropertiesHelper;
import com.sourcemeter.analyzer.csharp.batch.SourceMeterCSharpInitializer;
import com.sourcemeter.analyzer.csharp.batch.SourceMeterCSharpSensor;
import com.sourcemeter.analyzer.csharp.profile.SourceMeterCSharpProfile;
import com.sourcemeter.analyzer.csharp.profile.SourceMeterCSharpRuleRepository;

@Properties({
            @Property(
                 key = "sm.csharp.hardFilter",
                 name = "Hard filter",
                 description = "Hard filter file's path for SourceMeter C# analyzer.",
                 category = SourceMeterAnalyzerCSharpPlugin.CSHARP_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.csharp.runFxCop",
                 name = "Run FxCop",
                 description = "If true, run FxCop.",
                 category = SourceMeterAnalyzerCSharpPlugin.CSHARP_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.BOOLEAN,
                 defaultValue = "true"
             ),
             @Property(
                 key = "sm.csharp.fxCopPath",
                 name = "FxCop location",
                 description = "Path to the folder of FxCop.",
                 category = SourceMeterAnalyzerCSharpPlugin.CSHARP_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.csharp.configuration",
                 name = "Configuration",
                 description = "The name of the project configuration.",
                 category = SourceMeterAnalyzerCSharpPlugin.CSHARP_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.csharp.platform",
                 name = "Platform",
                 description = "The name of the target platform.",
                 category = SourceMeterAnalyzerCSharpPlugin.CSHARP_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.csharp.uploadMethods",
                 name = "Upload methods to the database.",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = "true"
             ),
             @Property(
                 key = "sm.csharp.skipToolchain",
                 name = "Skip SourceMeter C# toolchain (only upload results from existing result directory).",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = SourceMeterAnalyzerCSharpPlugin.FALSE
             ),
             @Property(
                 key = "sm.csharp.skipTUID",
                 name = "Skip elements which does not have a TUID in result graph.",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = SourceMeterAnalyzerCSharpPlugin.TRUE
             ),
             @Property(
                 key = "sm.csharp.toolchainOptions",
                 name = "Add additional parameters for running SourceMeter C# toolchain.",
                 global = false,
                 project = false,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.csharp.input",
                 name = "Solution file of the project.",
                 global = false,
                 project = false,
                 type = PropertyType.STRING
             )
})
public class SourceMeterAnalyzerCSharpPlugin implements Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(SourceMeterAnalyzerCSharpPlugin.class);

    public static final String CSHARP_GENERAL_CATEGORY = "SourceMeter C#";

    public static final String FALSE = "false";
    public static final String TRUE = "true";

    /**
     * {@inheritDoc}
     */
    @Override
    public void define(Plugin.Context context) {

        context.addExtensions(
                // profile
                SourceMeterCSharpProfile.class,
                SourceMeterCSharpRuleRepository.class,
                SourceMeterCSharpMetrics.class,
                VersionChecker.class
        );
        context.addExtensions(
                // batch
                SourceMeterCSharpInitializer.class,
                SourceMeterCSharpSensor.class
        );

        try (InputStream xmlFile = getClass().getResourceAsStream("/threshold_properties.xml")) {
            context.addExtensions(ThresholdPropertiesHelper.readPropertiesFromXML(xmlFile));
        } catch (IOException e) {
            LOG.error("Can not load properties for thresholds!");
        }
    }
}
