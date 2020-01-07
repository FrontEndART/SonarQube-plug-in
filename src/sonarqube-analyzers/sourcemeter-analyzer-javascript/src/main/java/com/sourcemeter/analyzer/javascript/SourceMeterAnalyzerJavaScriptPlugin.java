/**
 * Copyright (c) 2014-2020, FrontEndART Software Ltd.
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

package com.sourcemeter.analyzer.javascript;

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
import com.sourcemeter.analyzer.javascript.batch.SourceMeterJavaScriptSensor;
import com.sourcemeter.analyzer.javascript.profile.SourceMeterJavaScriptProfile;
import com.sourcemeter.analyzer.javascript.profile.SourceMeterJavaScriptRuleRepository;

/**
 * This class is the entry point for all extensions
 */
@Properties({
             @Property(
                key = "sm.javascript.uploadMethods",
                name = "Upload methods to the database.",
                global = false,
                project = false,
                type = PropertyType.BOOLEAN,
                defaultValue = "true"
             ),
             @Property(
                key = "sm.javascript.skipToolchain",
                name = "Skip SourceMeter Python toolchain (only upload results from existing result directory).",
                global = false,
                project = false,
                type = PropertyType.BOOLEAN,
                defaultValue = "false"
             ),
             @Property(
                key = "sm.javascript.toolchainOptions",
                name = "Add additional parameters for running SourceMeter Python toolchain.",
                global = false,
                project = false,
                type = PropertyType.STRING
             ),
})
public final class SourceMeterAnalyzerJavaScriptPlugin implements Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(SourceMeterAnalyzerJavaScriptPlugin.class);

    public static final String JAVASCRIPT_GENERAL_CATEGORY = "SourceMeter JavaScript";

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
                SourceMeterJavaScriptRuleRepository.class,
                SourceMeterJavaScriptProfile.class
        );

        // Metrics definitions
        context.addExtension(
                SourceMeterJavaScriptMetrics.class
        );

        // Batch
        context.addExtension(
                SourceMeterJavaScriptSensor.class
        );

        try (InputStream xmlFile = getClass().getResourceAsStream("/threshold_properties.xml")) {
            context.addExtensions(ThresholdPropertiesHelper.readPropertiesFromXML(xmlFile));
        } catch (IOException e) {
            LOG.error("Can not load properties for thresholds!");
        }
    }
}
