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
package com.sourcemeter.gui;

import com.sourcemeter.gui.resources.SourceMeterQualifiers;
import com.sourcemeter.gui.ui.LicenseChecker;
import com.sourcemeter.gui.ui.SourceMeterButtonInserter;
import com.sourcemeter.gui.ui.SourceMeterDashboard;
import com.sourcemeter.gui.ui.SourceMeterLogoInserter;
import com.sourcemeter.gui.ui.page.SourceMeterHelpPage;
import com.sourcemeter.gui.ui.widget.ClassHotspotWidget;
import com.sourcemeter.gui.ui.widget.CloneClassHotspotWidget;
import com.sourcemeter.gui.ui.widget.CloneInstanceHotspotWidget;
import com.sourcemeter.gui.ui.widget.MethodHotspotWidget;
import com.sourcemeter.gui.ui.widget.UserTextWidget;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.SonarPlugin;

/**
 * This class is the entry point for all extensions
 */
public final class SourceMeterGUIPlugin extends SonarPlugin {

    @SuppressWarnings({ "rawtypes" })
    @Override
    public List getExtensions() {
        return Arrays.asList(
                // UI
                SourceMeterDashboard.class,
                SourceMeterLogoInserter.class,
                LicenseChecker.class,
                SourceMeterButtonInserter.class,

                // UI - Page
                SourceMeterHelpPage.class,

                // UI - Widget
                MethodHotspotWidget.class,
                ClassHotspotWidget.class,
                CloneClassHotspotWidget.class,
                CloneInstanceHotspotWidget.class,
                UserTextWidget.class,

                // Qualifiers
                SourceMeterQualifiers.class
            );
    }
}
