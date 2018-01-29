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

package com.sourcemeter.analyzer.rpg;

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
import com.sourcemeter.analyzer.rpg.batch.SourceMeterRPGInitializer;
import com.sourcemeter.analyzer.rpg.batch.SourceMeterRPGSensor;
import com.sourcemeter.analyzer.rpg.core.RPG;
import com.sourcemeter.analyzer.rpg.profile.SourceMeterRPGProfile;
import com.sourcemeter.analyzer.rpg.profile.SourceMeterRPGRuleRepository;

@Properties({
             @Property(
                     key = "sm.rpg.spoolPattern",
                     name = "Spool pattern",
                     description = "File name pattern for spool (compiler listing) files.",
                     category = SourceMeterAnalyzerRPGPlugin.RPG_GENERAL_CATEGORY,
                     defaultValue = ".*\\.txt",
                     project = true
             ),
             @Property(
                     key = "sm.rpg.rpg3Pattern",
                     name = "RPG3 pattern",
                     description = "File name pattern for RPG/400 files.",
                     category = SourceMeterAnalyzerRPGPlugin.RPG_GENERAL_CATEGORY,
                     defaultValue = ".*\\.rpg",
                     project = true
             ),
             @Property(
                     key = "sm.rpg.rpg4Pattern",
                     name = "RPG4 pattern",
                     description = "File name pattern for ILE RPG files.",
                     category = SourceMeterAnalyzerRPGPlugin.RPG_GENERAL_CATEGORY,
                     defaultValue = ".*\\.rpgle",
                     project = true
             ),
             @Property(
                     key = "sm.rpg.uploadMethods",
                     name = "Upload methods to the database.",
                     global = false,
                     project = false,
                     type = PropertyType.BOOLEAN,
                     defaultValue = "true"
             ),
             @Property(
                     key = "sm.rpg.skipToolchain",
                     name = "Skip SourceMeter RPG toolchain (only upload results from existing result directory).",
                     global = false,
                     project = false,
                     type = PropertyType.BOOLEAN,
                     defaultValue = "false"
             ),
             @Property(
                 key = "sm.python.skipTUID",
                 name = "Skip elements which does not have a TUID in result graph.",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = SourceMeterAnalyzerRPGPlugin.TRUE
             ),
             @Property(
                     key = "sm.rpg.toolchainOptions",
                     name = "Add additional parameters for running SourceMeter RPG toolchain.",
                     global = false,
                     project = false,
                     type = PropertyType.STRING
             )
})
public class SourceMeterAnalyzerRPGPlugin implements Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(SourceMeterAnalyzerRPGPlugin.class);

    public static final String RPG_GENERAL_CATEGORY = "SourceMeter RPG";

    public static final String TRUE = "true";

    /**
     * {@inheritDoc}
     */
    @Override
    public void define(Context context) {

        context.addExtensions(
                RPG.class,
                SourceMeterRPGMetrics.class,
                VersionChecker.class
        );
        context.addExtensions(
                // profile
                SourceMeterRPGProfile.class,
                SourceMeterRPGRuleRepository.class
        );
        context.addExtensions(
                // Batch
                SourceMeterRPGInitializer.class,
                SourceMeterRPGSensor.class
        );

        try (InputStream xmlFile = getClass().getResourceAsStream("/threshold_properties.xml")) {
            context.addExtensions(ThresholdPropertiesHelper.readPropertiesFromXML(xmlFile));
        } catch (IOException e) {
            LOG.error("Can not load properties for thresholds!");
        }
    }
}
