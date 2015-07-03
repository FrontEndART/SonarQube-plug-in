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
package com.sourcemeter.analyzer.java;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.measures.Metric;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterMetricFinder;

/**
 * Metric finder class for get SourceMeter Metric objects
 */
public class SourceMeterJavaMetricFinder extends SourceMeterMetricFinder {

    /**
     * Default Constructor
     */
    public SourceMeterJavaMetricFinder() {
        super();
        SourceMeterJavaMetrics javaMetrics = new SourceMeterJavaMetrics();
        for (Metric metric : javaMetrics.getMetrics()) {
            this.metricMap.put(metric.getKey(), metric);
        }
    }

    @Override
    public List<Metric> findLanguageSpecificRulesetMetrics() {
        return Arrays.asList(
                SourceMeterJavaMetrics.ISSUEGROUP_ANDROID,
                SourceMeterJavaMetrics.ISSUEGROUP_BAD_PRACTICE,
                SourceMeterJavaMetrics.ISSUEGROUP_BRACE,
                SourceMeterJavaMetrics.ISSUEGROUP_CLONE_IMPLEMENTATION,
                SourceMeterJavaMetrics.ISSUEGROUP_CODE_SIZE,
                SourceMeterJavaMetrics.ISSUEGROUP_COMMENT,
                SourceMeterJavaMetrics.ISSUEGROUP_CONTROVERSIAL,
                SourceMeterJavaMetrics.ISSUEGROUP_CORRECTNESS,
                SourceMeterJavaMetrics.ISSUEGROUP_COUPLING,
                SourceMeterJavaMetrics.ISSUEGROUP_DODGY_CODE,
                SourceMeterJavaMetrics.ISSUEGROUP_EMPTY_CODE,
                SourceMeterJavaMetrics.ISSUEGROUP_EXPERIMENTAL,
                SourceMeterJavaMetrics.ISSUEGROUP_FINALIZER,
                SourceMeterJavaMetrics.ISSUEGROUP_IMPORT_STATEMENT,
                SourceMeterJavaMetrics.ISSUEGROUP_INTERNATIONALIZATION,
                SourceMeterJavaMetrics.ISSUEGROUP_J2EE,
                SourceMeterJavaMetrics.ISSUEGROUP_JAKARTA_COMMONS_LOGGING,
                SourceMeterJavaMetrics.ISSUEGROUP_JAVA_LOGGING,
                SourceMeterJavaMetrics.ISSUEGROUP_JAVABEAN,
                SourceMeterJavaMetrics.ISSUEGROUP_JUNIT,
                SourceMeterJavaMetrics.ISSUEGROUP_MIGRATION,
                SourceMeterJavaMetrics.ISSUEGROUP_MULTITHREADED_CORRECTNESS,
                SourceMeterJavaMetrics.ISSUEGROUP_OPTIMIZATION,
                SourceMeterJavaMetrics.ISSUEGROUP_SECURITY_CODE_GUIDELINE,
                SourceMeterJavaMetrics.ISSUEGROUP_STRICT_EXCEPTION,
                SourceMeterJavaMetrics.ISSUEGROUP_STRING_AND_STRINGBUFFER,
                SourceMeterJavaMetrics.ISSUEGROUP_TYPE_RESOLUTION,
                SourceMeterJavaMetrics.ISSUEGROUP_VULNERABILITY);
    }
}
