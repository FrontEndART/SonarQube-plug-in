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
package com.sourcemeter.analyzer.csharp;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.Builder;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.measures.SumChildValuesFormula;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreMetrics;

/**
 * Class containing SourceMeter metrics definitions
 */
public final class SourceMeterCSharpMetrics extends SourceMeterCoreMetrics {

    public static final String CSHARP_LICENSE_KEY = "SM:csharp_license";
    public static final Metric CSHARP_LICENSE = new Builder(
            CSHARP_LICENSE_KEY, "C# license",
            ValueType.STRING)
            .setHidden(true)
            .create();

    /* Rulesets metrics */
    public static final String ISSUEGROUP_DESIGN_KEY = "Microsoft.Design Rules";
    public static final Metric ISSUEGROUP_DESIGN = new Builder(
            ISSUEGROUP_DESIGN_KEY, "Design", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_GLOBALIZATION_KEY = "Microsoft.Globalization Rules";
    public static final Metric ISSUEGROUP_GLOBALIZATION = new Builder(
            ISSUEGROUP_GLOBALIZATION_KEY, "Globalization", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_INTEROPERABILITY_KEY = "Microsoft.Interoperability Rules";
    public static final Metric ISSUEGROUP_INTEROPERABILITY = new Builder(
            ISSUEGROUP_INTEROPERABILITY_KEY, "Interoperability", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_MAINTAINABILITY_KEY = "Microsoft.Maintainability Rules";
    public static final Metric ISSUEGROUP_MAINTAINABILITY = new Builder(
            ISSUEGROUP_MAINTAINABILITY_KEY, "Maintainability", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_MOBILITY_KEY = "Microsoft.Mobility Rules";
    public static final Metric ISSUEGROUP_MOBILITY = new Builder(
            ISSUEGROUP_MOBILITY_KEY, "Mobility", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_NAMING_KEY = "Microsoft.Naming Rules";
    public static final Metric ISSUEGROUP_NAMING = new Builder(
            ISSUEGROUP_NAMING_KEY, "Naming", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_PERFORMANCE_KEY = "Microsoft.Performance Rules";
    public static final Metric ISSUEGROUP_PERFORMANCE = new Builder(
            ISSUEGROUP_PERFORMANCE_KEY, "Performance", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_PORTABILITY_KEY = "Microsoft.Portability Rules";
    public static final Metric ISSUEGROUP_PORTABILITY = new Builder(
            ISSUEGROUP_PORTABILITY_KEY, "Portability", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_RELIABILITY_KEY = "Microsoft.Reliability Rules";
    public static final Metric ISSUEGROUP_RELIABILITY = new Builder(
            ISSUEGROUP_RELIABILITY_KEY, "Reliability", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_SECURITY_KEY = "Microsoft.Security Rules";
    public static final Metric ISSUEGROUP_SECURITY = new Builder(
            ISSUEGROUP_SECURITY_KEY, "Security", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_USAGE_KEY = "Microsoft.Usage Rules";
    public static final Metric ISSUEGROUP_USAGE = new Builder(
            ISSUEGROUP_USAGE_KEY, "Usage", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    /* End of Rulesets metrics */


    // getMetrics() method is defined in the Metrics interface and is used by
    // Sonar to retrieve the list of new metrics
    @Override
    public List<Metric> getMetrics() {
        return Arrays.asList(
                CSHARP_LICENSE,

                // Rulesets metrics
                ISSUEGROUP_DESIGN, ISSUEGROUP_GLOBALIZATION,
                ISSUEGROUP_INTEROPERABILITY, ISSUEGROUP_MAINTAINABILITY,
                ISSUEGROUP_MOBILITY, ISSUEGROUP_NAMING, ISSUEGROUP_PERFORMANCE,
                ISSUEGROUP_PORTABILITY, ISSUEGROUP_RELIABILITY,
                ISSUEGROUP_SECURITY, ISSUEGROUP_USAGE
        );
    }
}
