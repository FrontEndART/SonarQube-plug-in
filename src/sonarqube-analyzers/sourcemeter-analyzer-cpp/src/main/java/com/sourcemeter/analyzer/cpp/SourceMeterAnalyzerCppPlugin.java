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

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import com.sourcemeter.analyzer.base.batch.decorators.ComplexityPostDecorator;
import com.sourcemeter.analyzer.base.batch.decorators.ProjectDecorator;
import com.sourcemeter.analyzer.cpp.batch.SourceMeterCppInitializer;
import com.sourcemeter.analyzer.cpp.batch.SourceMeterCppSensor;
import com.sourcemeter.analyzer.cpp.batch.decorators.DefaultDecoratorCpp;
import com.sourcemeter.analyzer.cpp.batch.decorators.FileMetricsDecoratorCpp;
import com.sourcemeter.analyzer.cpp.batch.decorators.LicenseDecoratorCpp;
import com.sourcemeter.analyzer.cpp.colorizer.CppColorizer;
import com.sourcemeter.analyzer.cpp.colorizer.CppKeywords;
import com.sourcemeter.analyzer.cpp.core.Cpp;
import com.sourcemeter.analyzer.cpp.profile.SourceMeterCppProfile;
import com.sourcemeter.analyzer.cpp.profile.SourceMeterCppRuleRepository;

@Properties({
             @Property(
                 key = "sm.cpp.hardFilter",
                 name = "Hard filter",
                 description = "Hard filter file's path for SourceMeter CPP analyzer.",
                 category = SourceMeterAnalyzerCppPlugin.CPP_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.cpp.buildfile",
                 name = "Build script",
                 description = "This is the build script(.bat or .sh), which will build the project and may contain \"msbuild\", \"devenv\", \"cl\", \"lib\" or \"link\" in Windows, \"make\", \"g++\", \"gcc\", \"c++\", \"ld\" or \"ar\" in Linux without the quotes.",
                 category = SourceMeterAnalyzerCppPlugin.CPP_GENERAL_CATEGORY,
                 global = false
             ),
             @Property(
                 key = "sm.cpp.uploadMethods",
                 name = "Upload methods to the database.",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = "true"
             ),
             @Property(
                 key = "sm.cpp.skip",
                 name = "Skip SourceMeter C++ analyze on multi language projects.",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = SourceMeterAnalyzerCppPlugin.FALSE
             ),
             @Property(
                 key = "sm.cpp.skipToolchain",
                 name = "Skip SourceMeter C++ toolchain (only upload results from existing result directory).",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = SourceMeterAnalyzerCppPlugin.FALSE
             ),
             @Property(
                 key = "sm.cpp.skipTUID",
                 name = "Skip elements which does not have a TUID in result graph.",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = SourceMeterAnalyzerCppPlugin.TRUE
             ),
             @Property(
                 key = "sm.cpp.toolchainOptions",
                 name = "Add additional parameters for running SourceMeter C++ toolchain.",
                 global = false,
                 project = false,
                 type = PropertyType.STRING,
                 defaultValue = SourceMeterAnalyzerCppPlugin.FALSE
             )
})
public class SourceMeterAnalyzerCppPlugin extends SonarPlugin {

    public static final String SOURCE_FILE_SUFFIXES_KEY = "sm.cpp.suffixes.sources";
    public static final String HEADER_FILE_SUFFIXES_KEY = "sm.cpp.suffixes.headers";

    public static final String CLASS_BASELINE_KEY = "sm.cpp.class.baseline.";
    public static final String METHOD_BASELINE_KEY = "sm.cpp.method.baseline.";
    public static final String CLONE_CLASS_BASELINE_KEY = "sm.cpp.cloneclass.baseline.";
    public static final String CLONE_INSTANCE_BASELINE_KEY = "sm.cpp.cloneinstance.baseline.";

    public static final String CPP_GENERAL_CATEGORY = "SourceMeter C++";
    public static final String BASELINE_CATEGORY = "SourceMeter C++: Class Thresholds";
    public static final String CLONE_BASELINE_CATEGORY = "SourceMeter C++: Clone Thresholds";
    public static final String METHOD_BASELINE_CATEGORY = "SourceMeter C++: Method Thresholds";

    public static final String CLONE_CLASS_SUBCATEGORY = "Clone Class";
    public static final String CLONE_INSTANCE_SUBCATEGORY = "Clone Instance";

    public static final String FALSE = "false";
    public static final String TRUE = "true";

    @Override
    public List getExtensions() {

        return Arrays.asList(
                             // profile
                             Cpp.class,
                             SourceMeterCppMetrics.class,
                             SourceMeterCppProfile.class,
                             SourceMeterCppRuleRepository.class,

                             // Batch
                             SourceMeterCppInitializer.class,
                             SourceMeterCppSensor.class,

                             DefaultDecoratorCpp.class,
                             FileMetricsDecoratorCpp.class,
                             ComplexityPostDecorator.class,
                             LicenseDecoratorCpp.class,
                             ProjectDecorator.class,

                             // Syntax highlighting
                             CppColorizer.class,
                             CppKeywords.class,

                             PropertyDefinition.builder(SOURCE_FILE_SUFFIXES_KEY)
                                               .defaultValue(Cpp.DEFAULT_SOURCE_SUFFIXES)
                                               .name("Source files suffixes")
                                               .description("Comma-separated list of suffixes for source files to analyze. Leave empty to use the default.")
                                               .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
                                               .category(CPP_GENERAL_CATEGORY)
                                               .index(1)
                                               .build(),

                             PropertyDefinition.builder(HEADER_FILE_SUFFIXES_KEY)
                                               .defaultValue(Cpp.DEFAULT_HEADER_SUFFIXES)
                                               .name("Header files suffixes")
                                               .description("Comma-separated list of suffixes for header files to analyze. Leave empty to use the default.")
                                               .onQualifiers(Qualifiers.PROJECT, Qualifiers.MODULE)
                                               .category(CPP_GENERAL_CATEGORY)
                                               .index(2)
                                               .build(),

                             // Baselines
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.LOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.LOC.getDomain())
                                               .name("Lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1000")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.LLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.LLOC.getDomain())
                                               .name("Logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("600")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NA.getDomain())
                                               .name("Number of Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NG_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NG.getDomain())
                                               .name("Number of Getters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NLA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NLA.getDomain())
                                               .name("Number of Local Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NLG_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NLG.getDomain())
                                               .name("Number of Local Getters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NLM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NLM.getDomain())
                                               .name("Number of Local Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("20")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NLPA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NLPA.getDomain())
                                               .name("Number of Local Public Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NLPM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NLPM.getDomain())
                                               .name("Number of Local Public Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NLS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NLS.getDomain())
                                               .name("Number of Local Setters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NM.getDomain())
                                               .name("Number of Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("60")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NPA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NPA.getDomain())
                                               .name("Number of Public Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NPM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NPM.getDomain())
                                               .name("Number of Public Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("45")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NS.getDomain())
                                               .name("Number of Setters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NOS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NOS.getDomain())
                                               .name("Number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("900")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.TNA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TNA.getDomain())
                                               .name("Total Number of Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("24")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.TNG_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TNG.getDomain())
                                               .name("Total Number of Getters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("24")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.TNLA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TNLA.getDomain())
                                               .name("Total Number of Local Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("8")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.TNLG_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TNLG.getDomain())
                                               .name("Total Number of Local Getters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("8")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.TNLPA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TNLPA.getDomain())
                                               .name("Total Number of Local Public Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.TNLPM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TNLPM.getDomain())
                                               .name("Total Number of Local Public Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("24")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.TNLS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TNLS.getDomain())
                                               .name("Total Number of Local Setters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("8")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.TNLM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TNLM.getDomain())
                                               .name("Total Number of Local Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("32")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.TNM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TNM.getDomain())
                                               .name("Total Number of Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("96")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.TNPA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TNPA.getDomain())
                                               .name("Total Number of Public Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.TNPM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TNPM.getDomain())
                                               .name("Total Number of Public Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("72")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.TNS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TNS.getDomain())
                                               .name("Total Number of Setters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("24")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.TLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TLOC.getDomain())
                                               .name("Total lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1600")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.TLLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TLLOC.getDomain())
                                               .name("Total logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("960")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.TNOS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TNOS.getDomain())
                                               .name("Total number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1440")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.PUA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.PUA.getDomain())
                                               .name("Public undocumented API")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.AD_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.AD.getDomain())
                                               .name("API documentation")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("100")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.CD_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CD.getDomain())
                                               .name("Comment density")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.CLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CLOC.getDomain())
                                               .name("Comment lines of code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.DLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.DLOC.getDomain())
                                               .name("Documentation Lines")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.TCLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TCLOC.getDomain())
                                               .name("Total Comment Lines of Code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.TCD_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TCD.getDomain())
                                               .name("Total Comment Density")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NL_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NL.getDomain())
                                               .name("Nesting Level")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NLE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NLE.getDomain())
                                               .name("Nesting Level Else-If")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("3")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.WMC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.WMC.getDomain())
                                               .name("Weighted Methods per Class")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("115")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.CBO_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CBO.getDomain())
                                               .name("Coupling Between Object classes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.CBOI_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CBOI.getDomain())
                                               .name("Coupling Between Object classes Inverse")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NII_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NII.getDomain())
                                               .name("Number of Incoming Invocations")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NOI_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NOI.getDomain())
                                               .name("Number of Outgoing Invocations")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("50")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.RFC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.RFC.getDomain())
                                               .name("Response set For Class")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("70")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.LCOM5_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.LCOM5.getDomain())
                                               .name("Lack of Cohesion in Methods 5")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.CCL_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CCL.getDomain())
                                               .name("Clone Classes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.CCO_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CCO.getDomain())
                                               .name("Clone Complexity")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("57")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.CC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CC.getDomain())
                                               .name("Clone Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.CI_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CI.getDomain())
                                               .name("Clone Instances")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.CLC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CLC.getDomain())
                                               .name("Clone Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.CLLC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CLLC.getDomain())
                                               .name("Clone Logical Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.LDC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.LDC.getDomain())
                                               .name("Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("150")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.LLDC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.LLDC.getDomain())
                                               .name("Logical Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("90")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.WARNINGP0_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.WARNINGP0.getDomain())
                                               .name("Blocker")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.WARNINGP1_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.WARNINGP1.getDomain())
                                               .name("Critical")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.WARNINGP2_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.WARNINGP2.getDomain())
                                               .name("Major")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.WARNINGP3_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.WARNINGP3.getDomain())
                                               .name("Minor")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.WARNINGP4_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.WARNINGP4.getDomain())
                                               .name("Info")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.DIT_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.DIT.getDomain())
                                               .name("Depth of Inheritance Tree")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NOA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NOA.getDomain())
                                               .name("Number of Ancestors")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NOC.getDomain())
                                               .name("Number of Children")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NOD_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NOD.getDomain())
                                               .name("Number of Descendants")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("50")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.NOP_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NOP.getDomain())
                                               .name("Number of Parents")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1")
                                               .build(),

                             // Thresholds for methods
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.LOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.LOC.getDomain())
                                               .name("Lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("100")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.LLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.LLOC.getDomain())
                                               .name("Logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("65")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.NUMPAR_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NUMPAR.getDomain())
                                               .name("Number of Parameters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.NOS_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NOS.getDomain())
                                               .name("Number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("90")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.TLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TLOC.getDomain())
                                               .name("Total lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("160")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.TLLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TLLOC.getDomain())
                                               .name("Total logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("104")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.TNOS_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TNOS.getDomain())
                                               .name("Total number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("144")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.CD_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CD.getDomain())
                                               .name("Comment density")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.CLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CLOC.getDomain())
                                               .name("Comment lines of code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.DLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.DLOC.getDomain())
                                               .name("Documentation Lines")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.TCLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TCLOC.getDomain())
                                               .name("Total Comment Lines of Code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.TCD_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.TCD.getDomain())
                                               .name("Total Comment Density")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.McCC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.McCC.getDomain())
                                               .name("McCabe's Cyclomatic Complexity")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.NL_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NL.getDomain())
                                               .name("Nesting Level")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.NLE_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NLE.getDomain())
                                               .name("Nesting Level Else-If")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("3")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.NII_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NII.getDomain())
                                               .name("Number of Incoming Invocations")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.NOI_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.NOI.getDomain())
                                               .name("Number of Outgoing Invocations")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.CCL_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CCL.getDomain())
                                               .name("Clone Classes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.CCO_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CCO.getDomain())
                                               .name("Clone Complexity")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.CC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CC.getDomain())
                                               .name("Clone Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.CI_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CI.getDomain())
                                               .name("Clone Instances")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.CLC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CLC.getDomain())
                                               .name("Clone Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.CLLC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.CLLC.getDomain())
                                               .name("Clone Logical Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.LDC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.LDC.getDomain())
                                               .name("Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.LLDC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.LLDC.getDomain())
                                               .name("Logical Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("9")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.WARNINGP0_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.WARNINGP0.getDomain())
                                               .name("Blocker")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.WARNINGP1_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.WARNINGP1.getDomain())
                                               .name("Critical")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.WARNINGP2_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.WARNINGP2.getDomain())
                                               .name("Major")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.WARNINGP3_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.WARNINGP3.getDomain())
                                               .name("Minor")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.WARNINGP4_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.WARNINGP4.getDomain())
                                               .name("Info")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),

                             // Clone metrics
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCppMetrics.CA_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterCppMetrics.CA.getName())
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCppMetrics.CCO_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterCppMetrics.CCO.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCppMetrics.CE_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterCppMetrics.CE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("101")
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCppMetrics.CI_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterCppMetrics.CI.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("2")
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCppMetrics.CLLOC_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterCppMetrics.CLLOC.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("33")
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCppMetrics.CV_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterCppMetrics.CV.getName())
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCppMetrics.NCR_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterCppMetrics.NCR.getName())
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("0.15")
                                               .build(),
                             PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterCppMetrics.CA_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                               .name(SourceMeterCppMetrics.CA.getName())
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterCppMetrics.CCO_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                               .name(SourceMeterCppMetrics.CCO.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterCppMetrics.CE_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                               .name(SourceMeterCppMetrics.CE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("50")
                                               .build(),
                             PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterCppMetrics.CLLOC_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                               .name(SourceMeterCppMetrics.CLLOC.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("33")
                                               .build(),
                             PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterCppMetrics.CV_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                               .name(SourceMeterCppMetrics.CV.getName())
                                               .type(PropertyType.INTEGER)
                                               .build(),

                             // Class thresholds
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.ISSUEGROUP_API_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_API.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_API.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_BOOST_LIBRARY_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_BOOST_LIBRARY.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_BOOST_LIBRARY.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_BUFFER_OVERRUN_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_BUFFER_OVERRUN.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_BUFFER_OVERRUN.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_CONDITIONAL_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_CONDITIONAL.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_CONDITIONAL.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_DIVISION_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_DIVISION.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_DIVISION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_EXCEPTION_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_EXCEPTION.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_EXCEPTION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_INITIALIZATION_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_INITIALIZATION.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_INITIALIZATION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_INPUT_OUTPUT_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_INPUT_OUTPUT.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_INPUT_OUTPUT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_MEMORY_HANDLING_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_MEMORY_HANDLING.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_MEMORY_HANDLING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_OBJECT_ORIENTEDNESS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_OBJECT_ORIENTEDNESS.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_OBJECT_ORIENTEDNESS.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_PERFORMANCE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_PERFORMANCE.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_PERFORMANCE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_PORTABILITY_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_PORTABILITY.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_PORTABILITY.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_PREPROCESSOR_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_PREPROCESSOR.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_PREPROCESSOR.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                            + SourceMeterCppMetrics.ISSUEGROUP_READABILITY_AND_CONSISTENCY_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_READABILITY_AND_CONSISTENCY.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_READABILITY_AND_CONSISTENCY.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_REENTRANCY_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_REENTRANCY.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_REENTRANCY.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCppMetrics.ISSUEGROUP_STL_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_STL.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_STL.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_SIDE_EFFECT_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_SIDE_EFFECT.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_SIDE_EFFECT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_SIMPLE_TYPE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_SIMPLE_TYPE.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_SIMPLE_TYPE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_SIZEOF_OPERATOR_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_SIZEOF_OPERATOR.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_SIZEOF_OPERATOR.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_SUSPICIOUS_CONSTRUCT_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_SUSPICIOUS_CONSTRUCT.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_SUSPICIOUS_CONSTRUCT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                            + SourceMeterCppMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_UNREACHABLE_CODE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_UNREACHABLE_CODE.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_UNREACHABLE_CODE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                            + SourceMeterCppMetrics.ISSUEGROUP_VARIABLE_ARGUMENT_RELATED_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_VARIABLE_ARGUMENT_RELATED.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_VARIABLE_ARGUMENT_RELATED.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),

                             // Method thresholds
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.ISSUEGROUP_API_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_API.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_API.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_BOOST_LIBRARY_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_BOOST_LIBRARY.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_BOOST_LIBRARY.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_BUFFER_OVERRUN_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_BUFFER_OVERRUN.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_BUFFER_OVERRUN.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_CONDITIONAL_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_CONDITIONAL.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_CONDITIONAL.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_DIVISION_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_DIVISION.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_DIVISION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_EXCEPTION_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_EXCEPTION.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_EXCEPTION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_INITIALIZATION_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_INITIALIZATION.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_INITIALIZATION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_INPUT_OUTPUT_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_INPUT_OUTPUT.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_INPUT_OUTPUT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_MEMORY_HANDLING_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_MEMORY_HANDLING.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_MEMORY_HANDLING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_OBJECT_ORIENTEDNESS_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_OBJECT_ORIENTEDNESS.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_OBJECT_ORIENTEDNESS.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_PERFORMANCE_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_PERFORMANCE.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_PERFORMANCE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_PORTABILITY_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_PORTABILITY.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_PORTABILITY.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_PREPROCESSOR_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_PREPROCESSOR.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_PREPROCESSOR.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                            + SourceMeterCppMetrics.ISSUEGROUP_READABILITY_AND_CONSISTENCY_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_READABILITY_AND_CONSISTENCY.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_READABILITY_AND_CONSISTENCY.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_REENTRANCY_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_REENTRANCY.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_REENTRANCY.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCppMetrics.ISSUEGROUP_STL_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_STL.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_STL.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_SIDE_EFFECT_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_SIDE_EFFECT.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_SIDE_EFFECT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_SIMPLE_TYPE_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_SIMPLE_TYPE.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_SIMPLE_TYPE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_SIZEOF_OPERATOR_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_SIZEOF_OPERATOR.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_SIZEOF_OPERATOR.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_SUSPICIOUS_CONSTRUCT_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_SUSPICIOUS_CONSTRUCT.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_SUSPICIOUS_CONSTRUCT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                            + SourceMeterCppMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterCppMetrics.ISSUEGROUP_UNREACHABLE_CODE_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_UNREACHABLE_CODE.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_UNREACHABLE_CODE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                            + SourceMeterCppMetrics.ISSUEGROUP_VARIABLE_ARGUMENT_RELATED_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCppMetrics.ISSUEGROUP_VARIABLE_ARGUMENT_RELATED.getDomain())
                                               .name(SourceMeterCppMetrics.ISSUEGROUP_VARIABLE_ARGUMENT_RELATED.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build()
                     );
    }
}
