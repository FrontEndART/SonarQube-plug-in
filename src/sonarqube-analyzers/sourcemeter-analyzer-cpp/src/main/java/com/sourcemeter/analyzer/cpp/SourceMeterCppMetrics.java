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
import org.sonar.api.measures.Metric.Builder;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.measures.SumChildValuesFormula;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreMetrics;

/**
 * Class containing SourceMeter metrics definitions
 */
public final class SourceMeterCppMetrics extends SourceMeterCoreMetrics {

    public static final String CPP_LICENSE_KEY = "SM:cpp_license";
    public static final Metric CPP_LICENSE = new Builder(
            CPP_LICENSE_KEY, "C++ license",
            ValueType.STRING)
            .setHidden(true)
            .create();

    /* Rulesets metrics */
    public static final String ISSUEGROUP_API_KEY = "API Rules";
    public static final Metric ISSUEGROUP_API = new Builder(
        ISSUEGROUP_API_KEY, "API", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_BOOST_LIBRARY_KEY = "Boost Library Rules";
    public static final Metric ISSUEGROUP_BOOST_LIBRARY = new Builder(
        ISSUEGROUP_BOOST_LIBRARY_KEY, "Boost Library", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_BUFFER_OVERRUN_KEY = "Buffer Overrun Rules";
    public static final Metric ISSUEGROUP_BUFFER_OVERRUN = new Builder(
        ISSUEGROUP_BUFFER_OVERRUN_KEY, "Buffer Overrun", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_CONDITIONAL_KEY = "Conditional Rules";
    public static final Metric ISSUEGROUP_CONDITIONAL = new Builder(
        ISSUEGROUP_CONDITIONAL_KEY, "Conditional", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_DIVISION_KEY = "Division Rules";
    public static final Metric ISSUEGROUP_DIVISION = new Builder(
        ISSUEGROUP_DIVISION_KEY, "Division", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_INITIALIZATION_KEY = "Initialization Rules";
    public static final Metric ISSUEGROUP_INITIALIZATION = new Builder(
        ISSUEGROUP_INITIALIZATION_KEY, "Initialization", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_INPUT_OUTPUT_KEY = "Input Output Rules";
    public static final Metric ISSUEGROUP_INPUT_OUTPUT = new Builder(
        ISSUEGROUP_INPUT_OUTPUT_KEY, "Input Output", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_MEMORY_HANDLING_KEY = "Memory Handling Rules";
    public static final Metric ISSUEGROUP_MEMORY_HANDLING = new Builder(
        ISSUEGROUP_MEMORY_HANDLING_KEY, "Memory Handling", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_OBJECT_ORIENTEDNESS_KEY = "Object Orientedness Rules";
    public static final Metric ISSUEGROUP_OBJECT_ORIENTEDNESS = new Builder(
        ISSUEGROUP_OBJECT_ORIENTEDNESS_KEY, "Object Orientedness", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_PERFORMANCE_KEY = "Performance Rules";
    public static final Metric ISSUEGROUP_PERFORMANCE = new Builder(
        ISSUEGROUP_PERFORMANCE_KEY, "Performance", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_PORTABILITY_KEY = "Portability Rules";
    public static final Metric ISSUEGROUP_PORTABILITY = new Builder(
        ISSUEGROUP_PORTABILITY_KEY, "Portability", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_PREPROCESSOR_KEY = "Preprocessor Rules";
    public static final Metric ISSUEGROUP_PREPROCESSOR = new Builder(
        ISSUEGROUP_PREPROCESSOR_KEY, "Preprocessor", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_READABILITY_AND_CONSISTENCY_KEY = "Readability and Consistency Rules";
    public static final Metric ISSUEGROUP_READABILITY_AND_CONSISTENCY = new Builder(
        ISSUEGROUP_READABILITY_AND_CONSISTENCY_KEY, "Readability and Consistency", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_REENTRANCY_KEY = "Reentrancy Rules";
    public static final Metric ISSUEGROUP_REENTRANCY = new Builder(
        ISSUEGROUP_REENTRANCY_KEY, "Reentrancy", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_STL_KEY = "STL Rules";
    public static final Metric ISSUEGROUP_STL = new Builder(
        ISSUEGROUP_STL_KEY, "STL", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_SIDE_EFFECT_KEY = "Side Effect Rules";
    public static final Metric ISSUEGROUP_SIDE_EFFECT = new Builder(
        ISSUEGROUP_SIDE_EFFECT_KEY, "Side Effect", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_SIMPLE_TYPE_KEY = "Simple Type Rules";
    public static final Metric ISSUEGROUP_SIMPLE_TYPE = new Builder(
        ISSUEGROUP_SIMPLE_TYPE_KEY, "Simple Type", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_SIZEOF_OPERATOR_KEY = "Sizeof Operator Rules";
    public static final Metric ISSUEGROUP_SIZEOF_OPERATOR = new Builder(
        ISSUEGROUP_SIZEOF_OPERATOR_KEY, "Sizeof Operator", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_SUSPICIOUS_CONSTRUCT_KEY = "Suspicious Construct Rules";
    public static final Metric ISSUEGROUP_SUSPICIOUS_CONSTRUCT = new Builder(
        ISSUEGROUP_SUSPICIOUS_CONSTRUCT_KEY, "Suspicious Construct", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_UNREACHABLE_CODE_KEY = "Unreachable Code Rules";
    public static final Metric ISSUEGROUP_UNREACHABLE_CODE = new Builder(
        ISSUEGROUP_UNREACHABLE_CODE_KEY, "Unreachable Code", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_VARIABLE_ARGUMENT_RELATED_KEY = "Variable Argument Related Rules";
    public static final Metric ISSUEGROUP_VARIABLE_ARGUMENT_RELATED = new Builder(
        ISSUEGROUP_VARIABLE_ARGUMENT_RELATED_KEY, "Variable Argument Related", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();
    /* End of Rulesets metrics */


    // getMetrics() method is defined in the Metrics interface and is used by
    // Sonar to retrieve the list of new metrics
    @Override
    public List<Metric> getMetrics() {
        return Arrays.asList(
                CLASSES_IN_FILE, METHODS_IN_FILE, FILE_PATHS, CPP_LICENSE,

                // Rulesets metrics
                ISSUEGROUP_API, ISSUEGROUP_BOOST_LIBRARY, ISSUEGROUP_BUFFER_OVERRUN, ISSUEGROUP_CONDITIONAL,
                ISSUEGROUP_DIVISION, ISSUEGROUP_INITIALIZATION, ISSUEGROUP_INPUT_OUTPUT,
                ISSUEGROUP_MEMORY_HANDLING, ISSUEGROUP_OBJECT_ORIENTEDNESS, ISSUEGROUP_PERFORMANCE,
                ISSUEGROUP_PORTABILITY, ISSUEGROUP_PREPROCESSOR, ISSUEGROUP_READABILITY_AND_CONSISTENCY,
                ISSUEGROUP_REENTRANCY, ISSUEGROUP_SIDE_EFFECT, ISSUEGROUP_SIMPLE_TYPE,
                ISSUEGROUP_SIZEOF_OPERATOR, ISSUEGROUP_STL, ISSUEGROUP_SUSPICIOUS_CONSTRUCT,
                ISSUEGROUP_UNREACHABLE_CODE, ISSUEGROUP_VARIABLE_ARGUMENT_RELATED
        );
    }
}
