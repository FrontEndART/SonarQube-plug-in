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

package com.sourcemeter.analyzer.cpp;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import com.sourcemeter.analyzer.base.core.VersionChecker;
import com.sourcemeter.analyzer.base.helper.ThresholdPropertiesHelper;
import com.sourcemeter.analyzer.cpp.batch.SourceMeterCppInitializer;
import com.sourcemeter.analyzer.cpp.batch.SourceMeterCppSensor;
import com.sourcemeter.analyzer.cpp.core.Cpp;
import com.sourcemeter.analyzer.cpp.profile.SourceMeterCppProfile;
import com.sourcemeter.analyzer.cpp.profile.SourceMeterCppRuleRepository;

@Properties({
             @Property(
                 key = "sm.cpp.hardFilter",
                 name = "Hard filter",
                 description = "Hard filter file's path for SourceMeter C/C++ analyzer.",
                 category = SourceMeterAnalyzerCppPlugin.CPP_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.cpp.buildfile",
                 name = "Build script",
                 description = "This is the build script(.bat or .sh), which will build the project and may contain \"msbuild\", \"devenv\", \"cl\", \"lib\" or \"link\" in Windows, \"make\", \"g++\", \"gcc\", \"c++\", \"ld\" or \"ar\" in Linux without the quotes.",
                 category = SourceMeterAnalyzerCppPlugin.CPP_GENERAL_CATEGORY,
                 global = false
             ),
             @Property(
                 key = "sm.cpp.uploadMethods",
                 name = "Upload methods to the database.",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = "true"
             ),
             @Property(
                 key = "sm.cpp.skipToolchain",
                 name = "Skip SourceMeter C/C++ toolchain (only upload results from existing result directory).",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = SourceMeterAnalyzerCppPlugin.FALSE
             ),
             @Property(
                 key = "sm.cpp.skipTUID",
                 name = "Skip elements which does not have a TUID in result graph.",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = SourceMeterAnalyzerCppPlugin.TRUE
             ),
             @Property(
                 key = "sm.cpp.toolchainOptions",
                 name = "Add additional parameters for running SourceMeter C/C++ toolchain.",
                 global = false,
                 project = false,
                 type = PropertyType.STRING
             )
})
public class SourceMeterAnalyzerCppPlugin implements Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(SourceMeterAnalyzerCppPlugin.class);

    public static final String SOURCE_FILE_SUFFIXES_KEY = "sm.cpp.suffixes.sources";
    public static final String HEADER_FILE_SUFFIXES_KEY = "sm.cpp.suffixes.headers";

    public static final String CPP_GENERAL_CATEGORY = "SourceMeter C/C++";

    public static final String FALSE = "false";
    public static final String TRUE = "true";

    /**
     * {@inheritDoc}
     */
    @Override
    public void define(Context context) {

        context.addExtensions(
                // profile
                Cpp.class,
                SourceMeterCppMetrics.class,
                SourceMeterCppProfile.class,
                SourceMeterCppRuleRepository.class,
                VersionChecker.class
        );
        context.addExtensions(
                // Batch
                SourceMeterCppInitializer.class,
                SourceMeterCppSensor.class
        );

        try (InputStream xmlFile = getClass().getResourceAsStream("/threshold_properties.xml")) {
            context.addExtensions(ThresholdPropertiesHelper.readPropertiesFromXML(xmlFile));
        } catch (IOException e) {
            LOG.error("Can not load properties for thresholds!");
        }

        context.addExtensions(Arrays.asList(
                PropertyDefinition.builder(SOURCE_FILE_SUFFIXES_KEY)
                                  .defaultValue(Cpp.DEFAULT_SOURCE_SUFFIXES)
                                  .name("Source files suffixes")
                                  .description("Comma-separated list of suffixes for source files to analyze.")
                                  .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
                                  .category(CPP_GENERAL_CATEGORY)
                                  .index(1)
                                  .build(),
                PropertyDefinition.builder(HEADER_FILE_SUFFIXES_KEY)
                                  .defaultValue(Cpp.DEFAULT_HEADER_SUFFIXES)
                                  .name("Header files suffixes")
                                  .description("Comma-separated list of suffixes for header files to analyze.")
                                  .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
                                  .category(CPP_GENERAL_CATEGORY)
                                  .index(2)
                                  .build()
        ));
    }
}
