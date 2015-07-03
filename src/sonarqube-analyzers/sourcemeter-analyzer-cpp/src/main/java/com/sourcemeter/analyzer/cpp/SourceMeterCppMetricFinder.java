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
package com.sourcemeter.analyzer.cpp;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.measures.Metric;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterMetricFinder;

/**
 * Metric finder class for get SourceMeter Metric objects
 */
public class SourceMeterCppMetricFinder extends SourceMeterMetricFinder {

    /**
     * Default Constructor
     */
    public SourceMeterCppMetricFinder() {
        super();
        SourceMeterCppMetrics cppMetrics = new SourceMeterCppMetrics();
        for (Metric metric : cppMetrics.getMetrics()) {
            this.metricMap.put(metric.getKey(), metric);
        }
    }

    @Override
    public List<Metric> findLanguageSpecificRulesetMetrics() {
        return Arrays.asList(SourceMeterCppMetrics.ISSUEGROUP_API,
                             SourceMeterCppMetrics.ISSUEGROUP_BOOST_LIBRARY,
                             SourceMeterCppMetrics.ISSUEGROUP_BUFFER_OVERRUN,
                             SourceMeterCppMetrics.ISSUEGROUP_CONDITIONAL,
                             SourceMeterCppMetrics.ISSUEGROUP_DIVISION,
                             SourceMeterCppMetrics.ISSUEGROUP_EXCEPTION,
                             SourceMeterCppMetrics.ISSUEGROUP_INITIALIZATION,
                             SourceMeterCppMetrics.ISSUEGROUP_INPUT_OUTPUT,
                             SourceMeterCppMetrics.ISSUEGROUP_MEMORY_HANDLING,
                             SourceMeterCppMetrics.ISSUEGROUP_OBJECT_ORIENTEDNESS,
                             SourceMeterCppMetrics.ISSUEGROUP_PERFORMANCE,
                             SourceMeterCppMetrics.ISSUEGROUP_PORTABILITY,
                             SourceMeterCppMetrics.ISSUEGROUP_PREPROCESSOR,
                             SourceMeterCppMetrics.ISSUEGROUP_READABILITY_AND_CONSISTENCY,
                             SourceMeterCppMetrics.ISSUEGROUP_REENTRANCY,
                             SourceMeterCppMetrics.ISSUEGROUP_SIDE_EFFECT,
                             SourceMeterCppMetrics.ISSUEGROUP_SIMPLE_TYPE,
                             SourceMeterCppMetrics.ISSUEGROUP_SIZEOF_OPERATOR,
                             SourceMeterCppMetrics.ISSUEGROUP_STL,
                             SourceMeterCppMetrics.ISSUEGROUP_SUSPICIOUS_CONSTRUCT,
                             SourceMeterCppMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE,
                             SourceMeterCppMetrics.ISSUEGROUP_UNREACHABLE_CODE,
                             SourceMeterCppMetrics.ISSUEGROUP_VARIABLE_ARGUMENT_RELATED
                     );
    }
}
