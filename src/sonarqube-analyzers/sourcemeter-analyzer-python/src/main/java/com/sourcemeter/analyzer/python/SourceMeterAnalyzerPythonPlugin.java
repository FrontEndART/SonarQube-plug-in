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

package com.sourcemeter.analyzer.python;

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
import com.sourcemeter.analyzer.python.batch.SourceMeterPythonSensor;
import com.sourcemeter.analyzer.python.profile.SourceMeterPythonProfile;
import com.sourcemeter.analyzer.python.profile.SourceMeterPythonRuleRepository;

@Properties({
             @Property(
                 key = "sm.python.binary",
                 name = "Python 2.7 binary",
                 description = "Sets Python 2.7 binary executable name (full path is required if its directory is not in PATH).",
                 category = SourceMeterAnalyzerPythonPlugin.PYTHON_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.python.uploadMethods",
                 name = "Upload methods to the database.",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = "true"
             ),
             @Property(
                 key = "sm.python.skipToolchain",
                 name = "Skip SourceMeter Python toolchain (only upload results from existing result directory).",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = "false"
             ),

             @Property(
                 key = "sm.python.toolchainOptions",
                 name = "Add additional parameters for running SourceMeter Python toolchain.",
                 global = false,
                 project = false,
                 type = PropertyType.STRING
             ),
})
public class SourceMeterAnalyzerPythonPlugin implements Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(SourceMeterAnalyzerPythonPlugin.class);

    public static final String PYTHON_GENERAL_CATEGORY = "SourceMeter Python";

    public static final String TRUE = "true";

    /**
     * {@inheritDoc}
     */
    @Override
    public void define(Context context) {

        context.addExtensions(
                SourceMeterPythonMetrics.class,
                VersionChecker.class
        );

        // profile
        context.addExtensions(
                SourceMeterPythonProfile.class,
                SourceMeterPythonRuleRepository.class
        );

        context.addExtension(
                // Batch
                SourceMeterPythonSensor.class
        );

        try (InputStream xmlFile = getClass().getResourceAsStream("/threshold_properties.xml")) {
            context.addExtensions(ThresholdPropertiesHelper.readPropertiesFromXML(xmlFile));
        } catch (IOException e) {
            LOG.error("Can not load properties for thresholds!");
        }
    }
}
