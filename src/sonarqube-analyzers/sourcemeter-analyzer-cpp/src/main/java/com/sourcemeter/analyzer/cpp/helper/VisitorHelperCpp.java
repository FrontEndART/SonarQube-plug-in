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

package com.sourcemeter.analyzer.cpp.helper;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;

import com.sourcemeter.analyzer.base.helper.VisitorHelper;
import com.sourcemeter.analyzer.cpp.SourceMeterCppMetricFinder;
import com.sourcemeter.analyzer.cpp.core.Cpp;
import com.sourcemeter.analyzer.cpp.profile.SourceMeterCppRuleRepository;

public class VisitorHelperCpp extends VisitorHelper {

    public VisitorHelperCpp(SensorContext sensorContext, FileSystem fileSystem) {
        super(sensorContext, fileSystem, new SourceMeterCppMetricFinder());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRuleKey() {
        return SourceMeterCppRuleRepository.BASE_REPOSITORY_KEY
                + Cpp.INSTANCE.getKey();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWarningTextWithPrefix(String ruleKey, String warningText) {
        if (ruleKey.startsWith("CPPCHECK_")) {
            warningText = "SourceMeter (from Cppcheck): " + warningText;
        } else if (ruleKey.startsWith("CT_")) {
            warningText = "SourceMeter (from ClangTidy): " + warningText;
        } else {
            warningText = "SourceMeter: " + warningText;
        }

        return warningText;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCorrectedRuleKey(String ruleKey) {
        if (ruleKey.contains(METRIC_VIOLATION_CONTAINS)) {
            // class treshold violation
            ruleKey = METRIC_PREFIX + ruleKey.substring(0, ruleKey.indexOf(METRIC_VIOLATION_CONTAINS));
        } else {
            String[] splittedKey = ruleKey.split("_");
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for(int i = 1; i < splittedKey.length; ++i) {
                if ( first ) {
                    first = false;
                }
                else {
                    sb.append("_");
                }
                sb.append(splittedKey[i]);
            }
            ruleKey = sb.toString();
        }

        return ruleKey;
    }
}
