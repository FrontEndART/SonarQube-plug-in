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
@Description("Hotspots widget for Clone Instance.")
@WidgetCategory("SourceMeter")
@WidgetProperties({
        @WidgetProperty(
                key = "Metric",
                type = WidgetPropertyType.METRIC,
                defaultValue = SourceMeterCoreMetrics.CA_KEY,
                options = {
                        MetricOptionConstants.METRIC_OPTION_START
                                + SourceMeterCoreMetrics.CLLOC_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.CCO_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.CE_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.CV_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
                                + SourceMeterCoreMetrics.CA_KEY + MetricOptionConstants.METRIC_OPTION_SEPARATOR
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
public class CloneInstanceHotspotWidget extends AbstractRubyTemplate implements RubyRailsWidget {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return "CloneInstanceHotpotsWidget";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        return "SourceMeter Clone Instance Hotspots";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getTemplatePath() {
        return "/widgets/sourcemeter-cloneinstance-hotspot-widget.html.erb";
    }
}
