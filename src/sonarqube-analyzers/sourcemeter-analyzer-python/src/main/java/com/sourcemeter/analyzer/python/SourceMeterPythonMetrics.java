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
import org.sonar.api.measures.Metric.Builder;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.measures.SumChildValuesFormula;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreMetrics;

/**
 * Class containing SourceMeter metrics definitions
 */
public final class SourceMeterPythonMetrics extends SourceMeterCoreMetrics {

    public static final String PYTHON_LICENSE_KEY = "SM:python_license";
    public static final Metric PYTHON_LICENSE = new Builder(
            PYTHON_LICENSE_KEY, "Python license",
            ValueType.STRING)
            .setHidden(true)
            .create();

    /* Rulesets metrics */
    public static final String ISSUEGROUP_CLASS_KEY = "Class Rules";
    public static final Metric ISSUEGROUP_CLASS = new Builder(
        ISSUEGROUP_CLASS_KEY, "Class", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_FORMAT_KEY = "Format Rules";
    public static final Metric ISSUEGROUP_FORMAT = new Builder(
        ISSUEGROUP_FORMAT_KEY, "Format", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_IMPORT_KEY = "Import Rules";
    public static final Metric ISSUEGROUP_IMPORT = new Builder(
        ISSUEGROUP_IMPORT_KEY, "Import", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_LOGGING_KEY = "Logging Rules";
    public static final Metric ISSUEGROUP_LOGGING = new Builder(
        ISSUEGROUP_LOGGING_KEY, "Logging", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_MISCELLANEOUS_KEY = "Miscellaneous Rules";
    public static final Metric ISSUEGROUP_MISCELLANEOUS = new Builder(
        ISSUEGROUP_MISCELLANEOUS_KEY, "Miscellaneous", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_NEWSTYLE_KEY = "Newstyle Rules";
    public static final Metric ISSUEGROUP_NEWSTYLE = new Builder(
        ISSUEGROUP_NEWSTYLE_KEY, "Newstyle", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_PYLINT_CHECKER_KEY = "Pylint Checker Rules";
    public static final Metric ISSUEGROUP_PYLINT_CHECKER = new Builder(
        ISSUEGROUP_PYLINT_CHECKER_KEY, "Pylint Checker", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_PYTHON3_KEY = "Python3 Rules";
    public static final Metric ISSUEGROUP_PYTHON3 = new Builder(
        ISSUEGROUP_PYTHON3_KEY, "Python3", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_SIMILARITY_KEY = "Similarity Rules";
    public static final Metric ISSUEGROUP_SIMILARITY = new Builder(
        ISSUEGROUP_SIMILARITY_KEY, "Similarity", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_SPELLING_KEY = "Spelling Rules";
    public static final Metric ISSUEGROUP_SPELLING = new Builder(
        ISSUEGROUP_SPELLING_KEY, "Spelling", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_STDLIB_KEY = "Stdlib Rules";
    public static final Metric ISSUEGROUP_STDLIB = new Builder(
        ISSUEGROUP_STDLIB_KEY, "Stdlib", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_STRING_CONSTANT_KEY = "String Constant Rules";
    public static final Metric ISSUEGROUP_STRING_CONSTANT = new Builder(
        ISSUEGROUP_STRING_CONSTANT_KEY, "String Constant", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_STRING_KEY = "String Rules";
    public static final Metric ISSUEGROUP_STRING = new Builder(
        ISSUEGROUP_STRING_KEY, "String", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_TYPECHECK_KEY = "Typecheck Rules";
    public static final Metric ISSUEGROUP_TYPECHECK = new Builder(
        ISSUEGROUP_TYPECHECK_KEY, "Typecheck", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_VARIABLE_KEY = "Variable Rules";
    public static final Metric ISSUEGROUP_VARIABLE = new Builder(
        ISSUEGROUP_VARIABLE_KEY, "Variable", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();


    /* End of Rulesets metrics */

    @Override
    public List<Metric> getMetrics() {
        return Arrays.asList(PYTHON_LICENSE,

                // Rulesets metrics
                ISSUEGROUP_CLASS, ISSUEGROUP_FORMAT, ISSUEGROUP_IMPORT,
                ISSUEGROUP_LOGGING, ISSUEGROUP_MISCELLANEOUS,
                ISSUEGROUP_NEWSTYLE, ISSUEGROUP_PYLINT_CHECKER,
                ISSUEGROUP_PYTHON3,  ISSUEGROUP_SIMILARITY,
                ISSUEGROUP_SPELLING, ISSUEGROUP_STDLIB, ISSUEGROUP_STRING,
                ISSUEGROUP_STRING_CONSTANT, ISSUEGROUP_TYPECHECK, ISSUEGROUP_VARIABLE
            );
    }
}
