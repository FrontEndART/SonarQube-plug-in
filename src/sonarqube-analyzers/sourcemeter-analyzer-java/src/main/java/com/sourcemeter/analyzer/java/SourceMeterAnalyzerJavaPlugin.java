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

package com.sourcemeter.analyzer.java;

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
import com.sourcemeter.analyzer.java.batch.SourceMeterJavaSensor;
import com.sourcemeter.analyzer.java.profile.SourceMeterJavaProfile;
import com.sourcemeter.analyzer.java.profile.SourceMeterJavaRuleRepository;

/**
 * This class is the entry point for all extensions
 */
@Properties({@Property(
                 key = "sm.java.runRTEHunter",
                 name = "Run RTEHunter",
                 description = "If true, run RTEHunter. This may cause performance issues. License is needed to run.",
                 category = SourceMeterAnalyzerJavaPlugin.JAVA_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.BOOLEAN
             ),
             @Property(
                 key = "sm.java.RHMaxState",
                 name = "State number limit of RTEHunter",
                 description = "Sets the maximum number of states for the RTEHunter module. The default value is 500.",
                 category = SourceMeterAnalyzerJavaPlugin.JAVA_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.INTEGER,
                 defaultValue = "500"
             ),
             @Property(
                 key = "sm.java.RHMaxDepth",
                 name = "State tree depth limit of RTEHunter",
                 description = "Sets the maximum depth limit of states in the RTEHunter module. The default value is 200.",
                 category = SourceMeterAnalyzerJavaPlugin.JAVA_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.INTEGER,
                 defaultValue = "200"
             ),
             @Property(
                 key = "sm.java.vhMaxDepth",
                 name = "Max depth for VulnerabilityHunter",
                 description = "Sets the maximum searching depth limit of the VulnerabilityHunter.",
                 category = SourceMeterAnalyzerJavaPlugin.JAVA_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.INTEGER
             ),
             @Property(
                 key = "sm.java.vhTimeOut",
                 name = "Timeout for VulnerabilityHunter",
                 description = "Sets the execution time-limit for the VulnerabilityHunter in minutes. After the given time the VulnerabilityHunter stops.",
                 category = SourceMeterAnalyzerJavaPlugin.JAVA_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.INTEGER
             ),
             @Property(
                 key = "sm.java.maxMem",
                 name = "JVM max memory",
                 description = "Max memory usage in megaBytes.",
                 category = SourceMeterAnalyzerJavaPlugin.JAVA_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.INTEGER
             ),
             @Property(
                 key = "sm.java.runVulnerabilityHunter",
                 name = "Run VulnerabilityHunter",
                 description = "If true, run VulnerabilityHunter. This may cause performance issues. License is needed to run.",
                 category = SourceMeterAnalyzerJavaPlugin.JAVA_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.BOOLEAN,
                 defaultValue = "true"
             ),
             @Property(
                 key = "sm.java.hardFilter",
                 name = "Hard filter",
                 description = "Hard filter file's path for SourceMeter Java analyzer.",
                 category = SourceMeterAnalyzerJavaPlugin.JAVA_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.java.javacOptions",
                 name = "Javac Options",
                 global = false,
                 project = false,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.java.pmdOptions",
                 name = "PMD Options",
                 global = false,
                 project = false,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.java.csvSeparator",
                 name = "CSV Separator Character",
                 global = false,
                 project = false,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.java.uploadMethods",
                 name = "Upload methods to the database.",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = "true"
             ),
             @Property(
                 key = "sm.java.skipToolchain",
                 name = "Skip SourceMeter Java toolchain (only upload results from existing result directory).",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = SourceMeterAnalyzerJavaPlugin.FALSE
             ),
             @Property(
                 key = "sm.java.fbOptions",
                 name = "Add additional parameters for running FindBugs in SourceMeter toolchain.",
                 global = false,
                 project = false,
                 type = PropertyType.STRING,
                 defaultValue = SourceMeterAnalyzerJavaPlugin.FALSE
             ),
             @Property(
                 key = "sm.java.toolchainOptions",
                 name = "Add additional parameters for running SourceMeter Java toolchain.",
                 global = false,
                 project = false,
                 type = PropertyType.STRING
             ),
})
public final class SourceMeterAnalyzerJavaPlugin implements Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(SourceMeterAnalyzerJavaPlugin.class);

    public static final String JAVA_GENERAL_CATEGORY = "SourceMeter Java";

    public static final String FALSE = "false";
    public static final String TRUE = "true";

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "rawtypes" })
    @Override
    public void define(Context context) {
        // Core
        context.addExtension(VersionChecker.class);

        // PMD
        context.addExtensions(
                SourceMeterJavaRuleRepository.class,
                SourceMeterJavaProfile.class
        );

        // Metrics definitions
        context.addExtension(
                SourceMeterJavaMetrics.class
        );

        // Batch
        context.addExtension(
                SourceMeterJavaSensor.class
        );

        try (InputStream xmlFile = getClass().getResourceAsStream("/threshold_properties.xml")) {
            context.addExtensions(ThresholdPropertiesHelper.readPropertiesFromXML(xmlFile));
        } catch (IOException e) {
            LOG.error("Can not load properties for thresholds!");
        }
    }
}
