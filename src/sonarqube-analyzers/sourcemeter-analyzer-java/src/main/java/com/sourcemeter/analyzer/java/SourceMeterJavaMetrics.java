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
import org.sonar.api.measures.Metric.Builder;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.measures.SumChildValuesFormula;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreMetrics;

/**
 * Class containing SourceMeter metrics definitions
 */
public final class SourceMeterJavaMetrics extends SourceMeterCoreMetrics {

    /**
     * Stacktrace is stored in JSON in the given format: "extrainfos" :
     * "resource" : [resource_key], "trace" : [{ "line" : [line], "sourcelink" :
     * [{ "resource" : [resource_key], "line" : [line] }, ... { "resource" :
     * [resource_key], "line" : [line] }], "issue-key" : [issue-key] }, ... {
     * ... }] }
     */
    public static final String TRACE_KEY = "SM:stacktrace";
    public static final Metric TRACE = new Builder(TRACE_KEY, "Stacktrace",
            ValueType.DATA)
            .setHidden(true)
            .create();

    /**
     * SourceMeter tools' license informations are stored in a JSON array in the
     * given format: {"full" : ["tool1", "tool2", ...], "limited" : ["tool3",
     * "tool4", ... ], "inactive" : ["tool4", ... ] }
     */
    public static final String JAVA_LICENSE_KEY = "SM:java_license";
    public static final Metric JAVA_LICENSE = new Builder(
            JAVA_LICENSE_KEY, "Java license",
            ValueType.STRING)
            .setHidden(true)
            .create();

    /* Rulesets metrics */
    public static final String ISSUEGROUP_ANDROID_KEY = "Android Rules";
    public static final Metric ISSUEGROUP_ANDROID = new Builder(
        ISSUEGROUP_ANDROID_KEY, "Android", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_BAD_PRACTICE_KEY = "Bad Practice Rules";
    public static final Metric ISSUEGROUP_BAD_PRACTICE = new Builder(
        ISSUEGROUP_BAD_PRACTICE_KEY, "Bad Practice", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_CODE_SIZE_KEY = "Code Size Rules";
    public static final Metric ISSUEGROUP_CODE_SIZE = new Builder(
        ISSUEGROUP_CODE_SIZE_KEY, "Code Size", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_COMMENT_KEY = "Comment Rules";
    public static final Metric ISSUEGROUP_COMMENT = new Builder(
        ISSUEGROUP_COMMENT_KEY, "Comment", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_CORRECTNESS_KEY = "Correctness Rules";
    public static final Metric ISSUEGROUP_CORRECTNESS = new Builder(
        ISSUEGROUP_CORRECTNESS_KEY, "Correctness", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_COUPLING_KEY = "Coupling Rules";
    public static final Metric ISSUEGROUP_COUPLING = new Builder(
        ISSUEGROUP_COUPLING_KEY, "Coupling", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_DODGY_CODE_KEY = "Dodgy Code Rules";
    public static final Metric ISSUEGROUP_DODGY_CODE = new Builder(
        ISSUEGROUP_DODGY_CODE_KEY, "Dodgy Code", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_EXPERIMENTAL_KEY = "Experimental Rules";
    public static final Metric ISSUEGROUP_EXPERIMENTAL = new Builder(
        ISSUEGROUP_EXPERIMENTAL_KEY, "Experimental", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_INTERNATIONALIZATION_KEY = "Internationalization Rules";
    public static final Metric ISSUEGROUP_INTERNATIONALIZATION = new Builder(
        ISSUEGROUP_INTERNATIONALIZATION_KEY, "Internationalization", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_MIGRATION_KEY = "Migration Rules";
    public static final Metric ISSUEGROUP_MIGRATION = new Builder(
        ISSUEGROUP_MIGRATION_KEY, "Migration", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_MULTITHREADED_CORRECTNESS_KEY = "Multithreaded Correctness Rules";
    public static final Metric ISSUEGROUP_MULTITHREADED_CORRECTNESS = new Builder(
        ISSUEGROUP_MULTITHREADED_CORRECTNESS_KEY, "Multithreaded Correctness", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_VULNERABILITY_KEY = "Vulnerability Rules";
    public static final Metric ISSUEGROUP_VULNERABILITY = new Builder(
        ISSUEGROUP_VULNERABILITY_KEY, "Vulnerability", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();


    /* End of rulesets metrics */

    @Override
    public List<Metric> getMetrics() {
        return Arrays.asList(
                TRACE,
                JAVA_LICENSE,

                // Rulesets metrics
                ISSUEGROUP_ANDROID, ISSUEGROUP_BAD_PRACTICE,
                ISSUEGROUP_CODE_SIZE, ISSUEGROUP_COMMENT,
                ISSUEGROUP_CORRECTNESS, ISSUEGROUP_COUPLING,
                ISSUEGROUP_DODGY_CODE, ISSUEGROUP_EXPERIMENTAL,
                ISSUEGROUP_INTERNATIONALIZATION,
                ISSUEGROUP_MIGRATION, ISSUEGROUP_MULTITHREADED_CORRECTNESS,
                ISSUEGROUP_VULNERABILITY);
    }
}
