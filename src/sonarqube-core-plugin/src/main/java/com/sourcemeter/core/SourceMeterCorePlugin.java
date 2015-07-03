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
package com.sourcemeter.core;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreInitializer;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreMetrics;

@Properties({
    @Property(
            key = "sm.toolchaindir",
            name = "SourceMeter location",
            description = "Installation directory of the SourceMeter source code analyzer tool (example: /home/Somebody/Sonar/extensions/plugins/SourceMeter-RPG-0.5-x64-linux)",
            category = SourceMeterCorePlugin.SM_GENERAL_CATEGORY
    ),
    @Property(
            key = "sm.resultsdir",
            name = "Results directory",
            description = "Relative or absolute path name of the directory where the results of the analysis will be stored. The directory will be created automatically if it does not exist.",
            category = SourceMeterCorePlugin.SM_GENERAL_CATEGORY,
            defaultValue = "result"
    ),
    @Property(
            key = "sm.cleanresults",
            name = "Clean SourceMeter Results Directory",
            description = "Keep the following number of SourceMeter analysis results in the results directory.",
            category = SourceMeterCorePlugin.SM_GENERAL_CATEGORY,
            type = PropertyType.INTEGER,
            defaultValue = "5"
    ),
    @Property(
            key = "sm.cloneGenealogy",
            name = "Clone Genealogy",
            description = "Run code duplication genealogy.",
            category = SourceMeterCorePlugin.SM_GENERAL_CATEGORY,
            type = PropertyType.BOOLEAN,
            defaultValue = "false",
            project = true
    ),
    @Property(
            key = "sm.cloneMinLines",
            name = "Clone min. lines",
            description = "Minimum code duplication lines.",
            category = SourceMeterCorePlugin.SM_GENERAL_CATEGORY,
            type = PropertyType.INTEGER,
            defaultValue = "10",
            project = true
    )
})
public class SourceMeterCorePlugin extends SonarPlugin{
    public static final String SM_GENERAL_CATEGORY = "SourceMeter";

    @Override
    public List getExtensions() {
    	return Arrays.asList(

    	        // Metrics
                SourceMeterCoreMetrics.class,
                SourceMeterCoreInitializer.class
        );
    }
}
