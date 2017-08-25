/**
 * Copyright (c) 2014-2016, FrontEndART Software Ltd.
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
package com.sourcemeter.gui.ui;

import org.sonar.api.web.Dashboard;
import org.sonar.api.web.Dashboard.Widget;
import org.sonar.api.web.DashboardLayout;
import org.sonar.api.web.DashboardTemplate;
import org.sonar.api.web.NavigationSection;
import org.sonar.api.web.UserRole;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreMetrics;

@NavigationSection(NavigationSection.RESOURCE)
@UserRole(UserRole.CODEVIEWER)
public class SourceMeterDashboard extends DashboardTemplate {

    private final String METRIC = "Metric";
    private final String LIMIT = "Limit";
    private final String LIMIT_VALUE = "5";
    private final String NUMBER_OF_COLUMNS = "5";

    private Widget w;
    private Dashboard dashboard;

    @Override
    public Dashboard createDashboard() {
        dashboard = Dashboard.create();
        dashboard.setLayout(DashboardLayout.TWO_COLUMNS);


        dashboard.setActivated(true);

        w = dashboard.addWidget("UserTextDisplayWidget", 1);
        String usetText1 = "<p><b><u><a target=\"_blank\" href=\"https://sourcemeter.com\">SourceMeter</a></u></b> is a source code analyzer tool, which can perform deep static analysis of the source code of Java, C/C++, C#, Python, and RPG  systems. SourceMeter contains 100+ useful source code metrics, powerful clone detection capabilities, and professionally prioritized and selected coding rule violations. To access more sophisticated static analysis features, please check other products of <b><u><a target=\"_blank\" href=\"https://frontendart.com\">FrontEndART</a></u></b>.</p><br><p>SourceMeter is based on the <b><u><a target=\"_blank\" href=\"http://www.sed.inf.u-szeged.hu/softwarequality\">Columbus technology</a></u></b> researched and developed at the <u><a target=\"_blank\" href=\"http://www.sed.inf.u-szeged.hu/\">Department of Software Engineering, University of Szeged</a></u>. It received the <u><a target=\"_blank\" href=\"http://www.inf.u-szeged.hu/~ferenc/papers/ICSM2012/MIPAward.jpg\">Most Influential Paper Award</a></u> at the 28th IEEE International Conference on Software Maintenance (<u><a target=\"_blank\" href=\"http://selab.fbk.eu/icsm2012/\">ICSM 2012</a></u>).</p>";
        w.setProperty("Text", usetText1);
        w.setProperty("Title",
                "<b><a target=\"_blank\" href=\"https://sourcemeter.com\">SourceMeter</a></b>");

        w = dashboard.addWidget("timeline", 1);
        addWidgetProperty(SourceMeterCoreMetrics.TNCL_KEY,
                SourceMeterCoreMetrics.TNM_KEY,
                SourceMeterCoreMetrics.TLLOC_KEY);

        w = dashboard.addWidget("timeline", 1);
        addWidgetProperty(SourceMeterCoreMetrics.CC_KEY,
                SourceMeterCoreMetrics.CI_KEY,
                SourceMeterCoreMetrics.CLLC_KEY);

        addSourceMeterWidget("MethodHotpotsWidget", 1, SourceMeterCoreMetrics.McCC_KEY);
        addSourceMeterWidget("ClassHotpotsWidget", 1, SourceMeterCoreMetrics.WMC_KEY);
        addSourceMeterWidget("CloneClassHotpotsWidget", 1, SourceMeterCoreMetrics.CCO_KEY);
        addSourceMeterWidget("CloneInstanceHotpotsWidget", 1, SourceMeterCoreMetrics.CCO_KEY);

        w = dashboard.addWidget("UserTextDisplayWidget", 2);
        String usetText2 = "<p>The source code of a program is usually its only up-to-date documentation. At the same time, the source code is the exquisite bearer of knowledge and business processes, accumulated over a long period of time. Source code quality decrease (a.k.a. software erosion), which happens due to many quick fixes and time pressure, results in the increase of development and testing costs, and operational risks.</p><br><p>Our mission is to develop innovative, useful tools and solutions which make the measurement and management of IT developments as simple as they can get. Our tools can reduce the development costs while increasing the overall quality of the examined system.</p>";
        w.setProperty("Text", usetText2);
        w.setProperty("Title", "<b><a target=\"_blank\" href=\"https://frontendart.com\">FrontEndART -  source code analysis and quality management</a></b>");

        w = dashboard.addWidget("time_machine", 2);
        w.setProperty("title", "SourceMeter Code Metrics");
        w.setProperty("numberOfColumns", this.NUMBER_OF_COLUMNS);
        w.setProperty("displaySparkLine", "true");
        addWidgetProperty(SourceMeterCoreMetrics.TLLOC_KEY,
                SourceMeterCoreMetrics.TNCL_KEY,
                SourceMeterCoreMetrics.TNM_KEY,
                SourceMeterCoreMetrics.TAD_KEY,
                SourceMeterCoreMetrics.TNPIN_KEY,
                SourceMeterCoreMetrics.TNPA_KEY);

        w = dashboard.addWidget("time_machine", 2);
        w.setProperty("title", "SourceMeter Clone Metrics");
        w.setProperty("numberOfColumns", this.NUMBER_OF_COLUMNS);
        w.setProperty("displaySparkLine", "true");
        addWidgetProperty(SourceMeterCoreMetrics.CCL_KEY,
                SourceMeterCoreMetrics.CI_KEY,
                SourceMeterCoreMetrics.CLLC_KEY,
                SourceMeterCoreMetrics.CCO_KEY);

        addSourceMeterWidget("MethodHotpotsWidget", 2, SourceMeterCoreMetrics.NOI_KEY);
        addSourceMeterWidget("ClassHotpotsWidget", 2, SourceMeterCoreMetrics.CBO_KEY);
        addSourceMeterWidget("CloneClassHotpotsWidget", 2, SourceMeterCoreMetrics.CI_KEY);
        addSourceMeterWidget("CloneInstanceHotpotsWidget", 2, SourceMeterCoreMetrics.CLLOC_KEY);
        return dashboard;
    }

    private void addSourceMeterWidget(String name, int column, String metricKey) {
        w = dashboard.addWidget(name, column);
        w.setProperty(this.METRIC, metricKey);
        w.setProperty(this.LIMIT, this.LIMIT_VALUE);
    }

    private void addWidgetProperty(String... metrics) {
        for (int i = 0; i < metrics.length; i++) {
            w.setProperty("metric" + (i + 1), metrics[i]);
        }
    }

    @Override
    public String getName() {
        return "SourceMeter";
    }

}
