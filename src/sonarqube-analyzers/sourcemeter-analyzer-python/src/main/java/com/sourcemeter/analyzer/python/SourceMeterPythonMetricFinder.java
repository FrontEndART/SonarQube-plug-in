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
package com.sourcemeter.analyzer.python;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.measures.Metric;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterMetricFinder;

/**
 * Metric finder class for get SourceMeter Metric objects
 */
public class SourceMeterPythonMetricFinder extends SourceMeterMetricFinder {

    /**
     * Default Constructor
     */
    public SourceMeterPythonMetricFinder() {
        super();
        SourceMeterPythonMetrics PythonMetrics = new SourceMeterPythonMetrics();
        for (Metric metric : PythonMetrics.getMetrics()) {
            this.metricMap.put(metric.getKey(), metric);
        }
    }

    @Override
    public List<Metric> findLanguageSpecificRulesetMetrics() {
        return Arrays.asList(
                SourceMeterPythonMetrics.ISSUEGROUP_BASIC,
                SourceMeterPythonMetrics.ISSUEGROUP_CLASS,
                SourceMeterPythonMetrics.ISSUEGROUP_DESIGN,
                SourceMeterPythonMetrics.ISSUEGROUP_EXCEPTION,
                SourceMeterPythonMetrics.ISSUEGROUP_FORMAT,
                SourceMeterPythonMetrics.ISSUEGROUP_IMPORT,
                SourceMeterPythonMetrics.ISSUEGROUP_LOGGING,
                SourceMeterPythonMetrics.ISSUEGROUP_MISCELLANEOUS,
                SourceMeterPythonMetrics.ISSUEGROUP_NEWSTYLE,
                SourceMeterPythonMetrics.ISSUEGROUP_PYLINT_CHECKER,
                SourceMeterPythonMetrics.ISSUEGROUP_PYTHON3,
                SourceMeterPythonMetrics.ISSUEGROUP_SIMILARITY,
                SourceMeterPythonMetrics.ISSUEGROUP_SPELLING,
                SourceMeterPythonMetrics.ISSUEGROUP_STDLIB,
                SourceMeterPythonMetrics.ISSUEGROUP_STRING,
                SourceMeterPythonMetrics.ISSUEGROUP_STRING_CONSTANT,
                SourceMeterPythonMetrics.ISSUEGROUP_TYPECHECK,
                SourceMeterPythonMetrics.ISSUEGROUP_VARIABLE);
    }
}
