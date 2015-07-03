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
package com.sourcemeter.gui.ui.widget;

import com.sourcemeter.analyzer.base.core.MetricOptionConstants;

import org.sonar.api.web.AbstractRubyTemplate;
import org.sonar.api.web.Description;
import org.sonar.api.web.RubyRailsWidget;
import org.sonar.api.web.UserRole;
import org.sonar.api.web.WidgetCategory;
import org.sonar.api.web.WidgetProperties;
import org.sonar.api.web.WidgetProperty;
import org.sonar.api.web.WidgetPropertyType;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreMetrics;

@UserRole(UserRole.USER)
@Description("Hotspots widget for Method.")
@WidgetCategory("SourceMeter")
@WidgetProperties({
        @WidgetProperty(
                key = "Metric",
                type = WidgetPropertyType.METRIC,
                defaultValue = SourceMeterCoreMetrics.LOC_KEY,
                options = {
                        MetricOptionConstants.METRIC_OPTION_START
                                + SourceMeterCoreMetrics.LOC_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.TLOC_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.LLOC_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.TLLOC_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.NOS_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.TNOS_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.NUMPAR_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.DLOC_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.CLOC_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.TCLOC_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.CD_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.TCD_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.McCC_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.NL_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.NLE_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.NOI_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.NII_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.CI_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.CCL_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.CCO_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.CC_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.CLC_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.CLLC_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.LDC_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.LLDC_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.WARNINGP1_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.WARNINGP2_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.WARNINGP3_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR

                                // ISSUEGROUP metrics
                                + SourceMeterCoreMetrics.ISSUEGROUP_BASIC_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_BRACE_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_CLONE_IMPLEMENTATION_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_CONTROVERSIAL_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_DESIGN_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_EMPTY_CODE_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_FINALIZER_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_IMPORT_STATEMENT_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_J2EE_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_JAKARTA_COMMONS_LOGGING_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_JAVABEAN_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_JAVA_LOGGING_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_JUNIT_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_NAMING_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_OPTIMIZATION_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_SECURITY_CODE_GUIDELINE_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_STRICT_EXCEPTION_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_STRING_AND_STRINGBUFFER_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_TYPE_RESOLUTION_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR

                                + MetricOptionConstants.METRIC_OPTION_END
                }
        ),
        @WidgetProperty(
                key = "Title",
                type = WidgetPropertyType.STRING
        ),
        @WidgetProperty(
                key = "Limit",
                type = WidgetPropertyType.INTEGER,
                defaultValue = "10"
        )
})
public class MethodHotspotWidget extends AbstractRubyTemplate implements RubyRailsWidget {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return "MethodHotpotsWidget";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "SourceMeter Method Hotspots";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTemplatePath() {
        return "/widgets/sourcemeter-method-hotspot-widget.html.erb";
    }
}
