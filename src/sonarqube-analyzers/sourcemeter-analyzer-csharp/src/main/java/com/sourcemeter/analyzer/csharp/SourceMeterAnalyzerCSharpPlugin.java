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

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;

import com.sourcemeter.analyzer.base.batch.decorators.ComplexityPostDecorator;
import com.sourcemeter.analyzer.base.batch.decorators.ProjectDecorator;
import com.sourcemeter.analyzer.csharp.batch.SourceMeterCSharpInitializer;
import com.sourcemeter.analyzer.csharp.batch.SourceMeterCSharpSensor;
import com.sourcemeter.analyzer.csharp.batch.decorators.DefaultDecoratorCSharp;
import com.sourcemeter.analyzer.csharp.batch.decorators.FileMetricsDecoratorCSharp;
import com.sourcemeter.analyzer.csharp.batch.decorators.LicenseDecoratorCSharp;
import com.sourcemeter.analyzer.csharp.colorizer.CSharpColorizer;
import com.sourcemeter.analyzer.csharp.core.CSharp;
import com.sourcemeter.analyzer.csharp.profile.SourceMeterCSharpProfile;
import com.sourcemeter.analyzer.csharp.profile.SourceMeterCSharpRuleRepository;

@Properties({
            @Property(
                 key = "sm.csharp.hardFilter",
                 name = "Hard filter",
                 description = "Hard filter file's path for SourceMeter C# analyzer.",
                 category = SourceMeterAnalyzerCSharpPlugin.CSHARP_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.csharp.runFxCop",
                 name = "Run FxCop",
                 description = "If true, then run FxCop.",
                 category = SourceMeterAnalyzerCSharpPlugin.CSHARP_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.BOOLEAN,
                 defaultValue = "true"
             ),
             @Property(
                 key = "sm.csharp.fxCopPath",
                 name = "FxCop location",
                 description = "Path to the folder of FxCop.",
                 category = SourceMeterAnalyzerCSharpPlugin.CSHARP_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.csharp.configuration",
                 name = "Configuration",
                 description = "The name of the project configuration.",
                 category = SourceMeterAnalyzerCSharpPlugin.CSHARP_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.csharp.platform",
                 name = "Platform",
                 description = "The name of the target platform.",
                 category = SourceMeterAnalyzerCSharpPlugin.CSHARP_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.csharp.uploadMethods",
                 name = "Upload methods to the database.",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = "true"
             ),
             @Property(
                 key = "sm.csharp.skip",
                 name = "Skip SourceMeter C# analyze on multi language projects.",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = SourceMeterAnalyzerCSharpPlugin.FALSE
             ),
             @Property(
                 key = "sm.csharp.skipToolchain",
                 name = "Skip SourceMeter C# toolchain (only upload results from existing result directory).",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = SourceMeterAnalyzerCSharpPlugin.FALSE
             ),
             @Property(
                 key = "sm.csharp.skipTUID",
                 name = "Skip elements which does not have a TUID in result graph.",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = SourceMeterAnalyzerCSharpPlugin.TRUE
             ),
             @Property(
                 key = "sm.csharp.toolchainOptions",
                 name = "Add additional parameters for running SourceMeter C# toolchain.",
                 global = false,
                 project = false,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.csharp.input",
                 name = "Solution file of the project.",
                 global = false,
                 project = false,
                 type = PropertyType.STRING
             )
})
public class SourceMeterAnalyzerCSharpPlugin extends SonarPlugin {

    public static final String SOURCE_FILE_SUFFIXES_KEY = "sm.csharp.suffixes.sources";
    public static final String HEADER_FILE_SUFFIXES_KEY = "sm.csharp.suffixes.headers";

    public static final String CLASS_BASELINE_KEY = "sm.csharp.class.baseline.";
    public static final String METHOD_BASELINE_KEY = "sm.csharp.method.baseline.";
    public static final String CLONE_CLASS_BASELINE_KEY = "sm.csharp.cloneclass.baseline.";
    public static final String CLONE_INSTANCE_BASELINE_KEY = "sm.csharp.cloneinstance.baseline.";

    public static final String CSHARP_GENERAL_CATEGORY = "SourceMeter C#";
    public static final String BASELINE_CATEGORY = "SourceMeter C#: Class Thresholds";
    public static final String CLONE_BASELINE_CATEGORY = "SourceMeter C#: Clone Thresholds";
    public static final String METHOD_BASELINE_CATEGORY = "SourceMeter C#: Method Thresholds";

    public static final String CLONE_CLASS_SUBCATEGORY = "Clone Class";
    public static final String CLONE_INSTANCE_SUBCATEGORY = "Clone Instance";

    public static final String FALSE = "false";
    public static final String TRUE = "true";

    @Override
    public List getExtensions() {

        return Arrays.asList(
                // profile
                CSharp.class,
                SourceMeterCSharpProfile.class,
                SourceMeterCSharpRuleRepository.class,
                SourceMeterCSharpMetrics.class,
                CSharpColorizer.class,

                // batch
                SourceMeterCSharpInitializer.class,
                SourceMeterCSharpSensor.class,

                // decorators
                DefaultDecoratorCSharp.class,
                FileMetricsDecoratorCSharp.class,
                ComplexityPostDecorator.class,
                LicenseDecoratorCSharp.class,
                ProjectDecorator.class,

                // metric thresholds
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.LOC_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.LOC.getDomain())
                                .name("Lines of code")
                                .type(PropertyType.INTEGER)
                                .defaultValue("1000")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.LLOC_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.LLOC.getDomain())
                                .name("Logical lines of code")
                                .type(PropertyType.INTEGER)
                                .defaultValue("600")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NA_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NA.getDomain())
                                .name("Number of Attributes")
                                .type(PropertyType.INTEGER)
                                .defaultValue("15")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NG_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NG.getDomain())
                                .name("Number of Getters")
                                .type(PropertyType.INTEGER)
                                .defaultValue("15")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NLA_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NLA.getDomain())
                                .name("Number of Local Attributes")
                                .type(PropertyType.INTEGER)
                                .defaultValue("5")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NLG_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NLG.getDomain())
                                .name("Number of Local Getters")
                                .type(PropertyType.INTEGER)
                                .defaultValue("5")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NLM_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NLM.getDomain())
                                .name("Number of Local Methods")
                                .type(PropertyType.INTEGER)
                                .defaultValue("20")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NLPA_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NLPA.getDomain())
                                .name("Number of Local Public Attributes")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NLPM_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NLPM.getDomain())
                                .name("Number of Local Public Methods")
                                .type(PropertyType.INTEGER)
                                .defaultValue("15")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NLS_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NLS.getDomain())
                                .name("Number of Local Setters")
                                .type(PropertyType.INTEGER)
                                .defaultValue("5")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NM_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NM.getDomain())
                                .name("Number of Methods")
                                .type(PropertyType.INTEGER)
                                .defaultValue("60")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NPA_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NPA.getDomain())
                                .name("Number of Public Attributes")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NPM_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NPM.getDomain())
                                .name("Number of Public Methods")
                                .type(PropertyType.INTEGER)
                                .defaultValue("45")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NS_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NS.getDomain())
                                .name("Number of Setters")
                                .type(PropertyType.INTEGER)
                                .defaultValue("15")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NOS_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NOS.getDomain())
                                .name("Number of Statements")
                                .type(PropertyType.INTEGER)
                                .defaultValue("900")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.TNA_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TNA.getDomain())
                                .name("Total Number of Attributes")
                                .type(PropertyType.INTEGER)
                                .defaultValue("24")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.TNG_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TNG.getDomain())
                                .name("Total Number of Getters")
                                .type(PropertyType.INTEGER)
                                .defaultValue("24")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.TNLA_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TNLA.getDomain())
                                .name("Total Number of Local Attributes")
                                .type(PropertyType.INTEGER)
                                .defaultValue("8")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.TNLG_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TNLG.getDomain())
                                .name("Total Number of Local Getters")
                                .type(PropertyType.INTEGER)
                                .defaultValue("8")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.TNLPA_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TNLPA.getDomain())
                                .name("Total Number of Local Public Attributes")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.TNLPM_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TNLPM.getDomain())
                                .name("Total Number of Local Public Methods")
                                .type(PropertyType.INTEGER)
                                .defaultValue("24")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.TNLS_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TNLS.getDomain())
                                .name("Total Number of Local Setters")
                                .type(PropertyType.INTEGER)
                                .defaultValue("8")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.TNLM_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TNLM.getDomain())
                                .name("Total Number of Local Methods")
                                .type(PropertyType.INTEGER)
                                .defaultValue("32")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.TNM_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TNM.getDomain())
                                .name("Total Number of Methods")
                                .type(PropertyType.INTEGER)
                                .defaultValue("96")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.TNPA_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TNPA.getDomain())
                                .name("Total Number of Public Attributes")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.TNPM_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TNPM.getDomain())
                                .name("Total Number of Public Methods")
                                .type(PropertyType.INTEGER)
                                .defaultValue("72")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.TNS_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TNS.getDomain())
                                .name("Total Number of Setters")
                                .type(PropertyType.INTEGER)
                                .defaultValue("24")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.TLOC_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TLOC.getDomain())
                                .name("Total lines of code")
                                .type(PropertyType.INTEGER)
                                .defaultValue("1600")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.TLLOC_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TLLOC.getDomain())
                                .name("Total logical lines of code")
                                .type(PropertyType.INTEGER)
                                .defaultValue("960")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.TNOS_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TNOS.getDomain())
                                .name("Total number of Statements")
                                .type(PropertyType.INTEGER)
                                .defaultValue("1440")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.PUA_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.PUA.getDomain())
                                .name("Public undocumented API")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.AD_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.AD.getDomain())
                                .name("API documentation")
                                .type(PropertyType.FLOAT)
                                .defaultValue("100")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.CD_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CD.getDomain())
                                .name("Comment density")
                                .type(PropertyType.FLOAT)
                                .defaultValue("25")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.CLOC_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CLOC.getDomain())
                                .name("Comment lines of code")
                                .type(PropertyType.INTEGER)
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.DLOC_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.DLOC.getDomain())
                                .name("Documentation Lines")
                                .type(PropertyType.INTEGER)
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.TCLOC_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TCLOC.getDomain())
                                .name("Total Comment Lines of Code")
                                .type(PropertyType.INTEGER)
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.TCD_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TCD.getDomain())
                                .name("Total Comment Density")
                                .type(PropertyType.FLOAT)
                                .defaultValue("25")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NL_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NL.getDomain())
                                .name("Nesting Level")
                                .type(PropertyType.INTEGER)
                                .defaultValue("5")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NLE_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NLE.getDomain())
                                .name("Nesting Level Else-If")
                                .type(PropertyType.INTEGER)
                                .defaultValue("3")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.WMC_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.WMC.getDomain())
                                .name("Weighted Methods per Class")
                                .type(PropertyType.INTEGER)
                                .defaultValue("115")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.CBO_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CBO.getDomain())
                                .name("Coupling Between Object classes")
                                .type(PropertyType.INTEGER)
                                .defaultValue("10")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.CBOI_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CBOI.getDomain())
                                .name("Coupling Between Object classes Inverse")
                                .type(PropertyType.INTEGER)
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NII_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NII.getDomain())
                                .name("Number of Incoming Invocations")
                                .type(PropertyType.INTEGER)
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NOI_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NOI.getDomain())
                                .name("Number of Outgoing Invocations")
                                .type(PropertyType.INTEGER)
                                .defaultValue("50")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.RFC_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.RFC.getDomain())
                                .name("Response set For Class")
                                .type(PropertyType.INTEGER)
                                .defaultValue("70")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.LCOM5_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.LCOM5.getDomain())
                                .name("Lack of Cohesion in Methods 5")
                                .type(PropertyType.INTEGER)
                                .defaultValue("1")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.CCL_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CCL.getDomain())
                                .name("Clone Classes")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.CCO_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CCO.getDomain())
                                .name("Clone Complexity")
                                .type(PropertyType.INTEGER)
                                .defaultValue("57")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.CC_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CC.getDomain())
                                .name("Clone Coverage")
                                .type(PropertyType.FLOAT)
                                .defaultValue("15")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.CI_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CI.getDomain())
                                .name("Clone Instances")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.CLC_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CLC.getDomain())
                                .name("Clone Line Coverage")
                                .type(PropertyType.FLOAT)
                                .defaultValue("15")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.CLLC_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CLLC.getDomain())
                                .name("Clone Logical Line Coverage")
                                .type(PropertyType.FLOAT)
                                .defaultValue("15")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.LDC_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.LDC.getDomain())
                                .name("Lines of Duplicated Code")
                                .type(PropertyType.INTEGER)
                                .defaultValue("150")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.LLDC_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.LLDC.getDomain())
                                .name("Logical Lines of Duplicated Code")
                                .type(PropertyType.INTEGER)
                                .defaultValue("90")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.WARNINGP0_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.WARNINGP0.getDomain())
                                .name("Blocker")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.WARNINGP1_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.WARNINGP1.getDomain())
                                .name("Critical")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.WARNINGP2_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.WARNINGP2.getDomain())
                                .name("Major")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.WARNINGP3_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.WARNINGP3.getDomain())
                                .name("Minor")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.WARNINGP4_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.WARNINGP4.getDomain())
                                .name("Info")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.DIT_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.DIT.getDomain())
                                .name("Depth of Inheritance Tree")
                                .type(PropertyType.INTEGER)
                                .defaultValue("5")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NOA_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NOA.getDomain())
                                .name("Number of Ancestors")
                                .type(PropertyType.INTEGER)
                                .defaultValue("5")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NOC_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NOC.getDomain())
                                .name("Number of Children")
                                .type(PropertyType.INTEGER)
                                .defaultValue("10")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NOD_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NOD.getDomain())
                                .name("Number of Descendants")
                                .type(PropertyType.INTEGER)
                                .defaultValue("50")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NOP_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NOP.getDomain())
                                .name("Number of Parents")
                                .type(PropertyType.INTEGER)
                                .defaultValue("1")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY
                                         + SourceMeterCSharpMetrics.ISSUEGROUP_DESIGN_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_DESIGN.getDomain())
                                .name(SourceMeterCSharpMetrics.ISSUEGROUP_DESIGN.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY
                                         + SourceMeterCSharpMetrics.ISSUEGROUP_GLOBALIZATION_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_GLOBALIZATION.getDomain())
                                .name(SourceMeterCSharpMetrics.ISSUEGROUP_GLOBALIZATION.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY
                                         + SourceMeterCSharpMetrics.ISSUEGROUP_INTEROPERABILITY_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_INTEROPERABILITY.getDomain())
                                .name(SourceMeterCSharpMetrics.ISSUEGROUP_INTEROPERABILITY.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY
                                         + SourceMeterCSharpMetrics.ISSUEGROUP_MAINTAINABILITY_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_MAINTAINABILITY.getDomain())
                                .name(SourceMeterCSharpMetrics.ISSUEGROUP_MAINTAINABILITY.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY
                                         + SourceMeterCSharpMetrics.ISSUEGROUP_MOBILITY_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_MOBILITY.getDomain())
                                .name(SourceMeterCSharpMetrics.ISSUEGROUP_MOBILITY.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY
                                         + SourceMeterCSharpMetrics.ISSUEGROUP_NAMING_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_NAMING.getDomain())
                                .name(SourceMeterCSharpMetrics.ISSUEGROUP_NAMING.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY
                                         + SourceMeterCSharpMetrics.ISSUEGROUP_PERFORMANCE_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_PERFORMANCE.getDomain())
                                .name(SourceMeterCSharpMetrics.ISSUEGROUP_PERFORMANCE.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY
                                         + SourceMeterCSharpMetrics.ISSUEGROUP_PORTABILITY_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_PORTABILITY.getDomain())
                                .name(SourceMeterCSharpMetrics.ISSUEGROUP_PORTABILITY.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.ISSUEGROUP_RELIABILITY_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_RELIABILITY.getDomain())
                                .name(SourceMeterCSharpMetrics.ISSUEGROUP_RELIABILITY.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY
                                         + SourceMeterCSharpMetrics.ISSUEGROUP_SECURITY_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_SECURITY.getDomain())
                                .name(SourceMeterCSharpMetrics.ISSUEGROUP_SECURITY.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(CLASS_BASELINE_KEY
                                         + SourceMeterCSharpMetrics.ISSUEGROUP_USAGE_KEY)
                                .category(BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_USAGE.getDomain())
                                .name(SourceMeterCSharpMetrics.ISSUEGROUP_USAGE.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),

                // Thresholds for methods
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.LOC_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.LOC.getDomain())
                                .name("Lines of code")
                                .type(PropertyType.INTEGER)
                                .defaultValue("100")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.LLOC_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.LLOC.getDomain())
                                .name("Logical lines of code")
                                .type(PropertyType.INTEGER)
                                .defaultValue("65")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.NUMPAR_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NUMPAR.getDomain())
                                .name("Number of Parameters")
                                .type(PropertyType.INTEGER)
                                .defaultValue("5")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.NOS_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NOS.getDomain())
                                .name("Number of Statements")
                                .type(PropertyType.INTEGER)
                                .defaultValue("90")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.TLOC_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TLOC.getDomain())
                                .name("Total lines of code")
                                .type(PropertyType.INTEGER)
                                .defaultValue("160")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.TLLOC_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TLLOC.getDomain())
                                .name("Total logical lines of code")
                                .type(PropertyType.INTEGER)
                                .defaultValue("104")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.TNOS_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TNOS.getDomain())
                                .name("Total number of Statements")
                                .type(PropertyType.INTEGER)
                                .defaultValue("144")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.CD_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CD.getDomain())
                                .name("Comment density")
                                .type(PropertyType.INTEGER)
                                .defaultValue("25")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.CLOC_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CLOC.getDomain())
                                .name("Comment lines of code")
                                .type(PropertyType.INTEGER)
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.DLOC_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.DLOC.getDomain())
                                .name("Documentation Lines")
                                .type(PropertyType.INTEGER)
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.TCLOC_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TCLOC.getDomain())
                                .name("Total Comment Lines of Code")
                                .type(PropertyType.INTEGER)
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.TCD_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.TCD.getDomain())
                                .name("Total Comment Density")
                                .type(PropertyType.INTEGER)
                                .defaultValue("25")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.McCC_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.McCC.getDomain())
                                .name("McCabe's Cyclomatic Complexity")
                                .type(PropertyType.INTEGER)
                                .defaultValue("10")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.NL_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NL.getDomain())
                                .name("Nesting Level")
                                .type(PropertyType.INTEGER)
                                .defaultValue("5")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.NLE_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NLE.getDomain())
                                .name("Nesting Level Else-If")
                                .type(PropertyType.INTEGER)
                                .defaultValue("3")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.NII_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NII.getDomain())
                                .name("Number of Incoming Invocations")
                                .type(PropertyType.INTEGER)
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.NOI_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.NOI.getDomain())
                                .name("Number of Outgoing Invocations")
                                .type(PropertyType.INTEGER)
                                .defaultValue("10")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.CCL_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CCL.getDomain())
                                .name("Clone Classes")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.CCO_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CCO.getDomain())
                                .name("Clone Complexity")
                                .type(PropertyType.INTEGER)
                                .defaultValue("10")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.CC_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CC.getDomain())
                                .name("Clone Coverage")
                                .type(PropertyType.FLOAT)
                                .defaultValue("15")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.CI_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CI.getDomain())
                                .name("Clone Instances")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.CLC_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CLC.getDomain())
                                .name("Clone Line Coverage")
                                .type(PropertyType.FLOAT)
                                .defaultValue("15")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.CLLC_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.CLLC.getDomain())
                                .name("Clone Logical Line Coverage")
                                .type(PropertyType.FLOAT)
                                .defaultValue("15")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.LDC_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.LDC.getDomain())
                                .name("Lines of Duplicated Code")
                                .type(PropertyType.INTEGER)
                                .defaultValue("15")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.LLDC_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.LLDC.getDomain())
                                .name("Logical Lines of Duplicated Code")
                                .type(PropertyType.INTEGER)
                                .defaultValue("9")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.WARNINGP0_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.WARNINGP0.getDomain())
                                .name("Blocker")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.WARNINGP1_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.WARNINGP1.getDomain())
                                .name("Critical")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.WARNINGP2_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.WARNINGP2.getDomain())
                                .name("Major")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.WARNINGP3_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.WARNINGP3.getDomain())
                                .name("Minor")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.WARNINGP4_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.WARNINGP4.getDomain())
                                .name("Info")
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY
                                         + SourceMeterCSharpMetrics.ISSUEGROUP_BASIC_KEY)
                                .category(METHOD_BASELINE_CATEGORY)
                                .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_BASIC.getDomain())
                                .name(SourceMeterCSharpMetrics.ISSUEGROUP_BASIC.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("0")
                                .build(),
                PropertyDefinition.builder(METHOD_BASELINE_KEY
                                        + SourceMeterCSharpMetrics.ISSUEGROUP_DESIGN_KEY)
                               .category(METHOD_BASELINE_CATEGORY)
                               .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_DESIGN.getDomain())
                               .name(SourceMeterCSharpMetrics.ISSUEGROUP_DESIGN.getName())
                               .type(PropertyType.INTEGER)
                               .defaultValue("0")
                               .build(),
               PropertyDefinition.builder(METHOD_BASELINE_KEY
                                        + SourceMeterCSharpMetrics.ISSUEGROUP_GLOBALIZATION_KEY)
                               .category(METHOD_BASELINE_CATEGORY)
                               .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_GLOBALIZATION.getDomain())
                               .name(SourceMeterCSharpMetrics.ISSUEGROUP_GLOBALIZATION.getName())
                               .type(PropertyType.INTEGER)
                               .defaultValue("0")
                               .build(),
               PropertyDefinition.builder(METHOD_BASELINE_KEY
                                        + SourceMeterCSharpMetrics.ISSUEGROUP_INTEROPERABILITY_KEY)
                               .category(METHOD_BASELINE_CATEGORY)
                               .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_INTEROPERABILITY.getDomain())
                               .name(SourceMeterCSharpMetrics.ISSUEGROUP_INTEROPERABILITY.getName())
                               .type(PropertyType.INTEGER)
                               .defaultValue("0")
                               .build(),
               PropertyDefinition.builder(METHOD_BASELINE_KEY
                                        + SourceMeterCSharpMetrics.ISSUEGROUP_MAINTAINABILITY_KEY)
                               .category(METHOD_BASELINE_CATEGORY)
                               .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_MAINTAINABILITY.getDomain())
                               .name(SourceMeterCSharpMetrics.ISSUEGROUP_MAINTAINABILITY.getName())
                               .type(PropertyType.INTEGER)
                               .defaultValue("0")
                               .build(),
               PropertyDefinition.builder(METHOD_BASELINE_KEY
                                        + SourceMeterCSharpMetrics.ISSUEGROUP_MOBILITY_KEY)
                               .category(METHOD_BASELINE_CATEGORY)
                               .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_MOBILITY.getDomain())
                               .name(SourceMeterCSharpMetrics.ISSUEGROUP_MOBILITY.getName())
                               .type(PropertyType.INTEGER)
                               .defaultValue("0")
                               .build(),
               PropertyDefinition.builder(METHOD_BASELINE_KEY
                                        + SourceMeterCSharpMetrics.ISSUEGROUP_NAMING_KEY)
                               .category(METHOD_BASELINE_CATEGORY)
                               .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_NAMING.getDomain())
                               .name(SourceMeterCSharpMetrics.ISSUEGROUP_NAMING.getName())
                               .type(PropertyType.INTEGER)
                               .defaultValue("0")
                               .build(),
               PropertyDefinition.builder(METHOD_BASELINE_KEY
                                        + SourceMeterCSharpMetrics.ISSUEGROUP_PERFORMANCE_KEY)
                               .category(METHOD_BASELINE_CATEGORY)
                               .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_PERFORMANCE.getDomain())
                               .name(SourceMeterCSharpMetrics.ISSUEGROUP_PERFORMANCE.getName())
                               .type(PropertyType.INTEGER)
                               .defaultValue("0")
                               .build(),
               PropertyDefinition.builder(METHOD_BASELINE_KEY
                                        + SourceMeterCSharpMetrics.ISSUEGROUP_PORTABILITY_KEY)
                               .category(METHOD_BASELINE_CATEGORY)
                               .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_PORTABILITY.getDomain())
                               .name(SourceMeterCSharpMetrics.ISSUEGROUP_PORTABILITY.getName())
                               .type(PropertyType.INTEGER)
                               .defaultValue("0")
                               .build(),
               PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCSharpMetrics.ISSUEGROUP_RELIABILITY_KEY)
                               .category(METHOD_BASELINE_CATEGORY)
                               .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_RELIABILITY.getDomain())
                               .name(SourceMeterCSharpMetrics.ISSUEGROUP_RELIABILITY.getName())
                               .type(PropertyType.INTEGER)
                               .defaultValue("0")
                               .build(),
               PropertyDefinition.builder(METHOD_BASELINE_KEY
                                        + SourceMeterCSharpMetrics.ISSUEGROUP_SECURITY_KEY)
                               .category(METHOD_BASELINE_CATEGORY)
                               .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_SECURITY.getDomain())
                               .name(SourceMeterCSharpMetrics.ISSUEGROUP_SECURITY.getName())
                               .type(PropertyType.INTEGER)
                               .defaultValue("0")
                               .build(),
               PropertyDefinition.builder(METHOD_BASELINE_KEY
                                        + SourceMeterCSharpMetrics.ISSUEGROUP_USAGE_KEY)
                               .category(METHOD_BASELINE_CATEGORY)
                               .subCategory(SourceMeterCSharpMetrics.ISSUEGROUP_USAGE.getDomain())
                               .name(SourceMeterCSharpMetrics.ISSUEGROUP_USAGE.getName())
                               .type(PropertyType.INTEGER)
                               .defaultValue("0")
                               .build(),

                // Clone metrics
                PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.CA_KEY)
                                .category(CLONE_BASELINE_CATEGORY)
                                .subCategory(CLONE_CLASS_SUBCATEGORY)
                                .name(SourceMeterCSharpMetrics.CA.getName())
                                .type(PropertyType.INTEGER)
                                .build(),
                PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.CCO_KEY)
                                .category(CLONE_BASELINE_CATEGORY)
                                .subCategory(CLONE_CLASS_SUBCATEGORY)
                                .name(SourceMeterCSharpMetrics.CCO.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("10")
                                .build(),
                PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.CE_KEY)
                                .category(CLONE_BASELINE_CATEGORY)
                                .subCategory(CLONE_CLASS_SUBCATEGORY)
                                .name(SourceMeterCSharpMetrics.CE.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("101")
                                .build(),
                PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.CI_KEY)
                                .category(CLONE_BASELINE_CATEGORY)
                                .subCategory(CLONE_CLASS_SUBCATEGORY)
                                .name(SourceMeterCSharpMetrics.CI.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("2")
                                .build(),
                PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.CLLOC_KEY)
                                .category(CLONE_BASELINE_CATEGORY)
                                .subCategory(CLONE_CLASS_SUBCATEGORY)
                                .name(SourceMeterCSharpMetrics.CLLOC.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("33")
                                .build(),
                PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.CV_KEY)
                                .category(CLONE_BASELINE_CATEGORY)
                                .subCategory(CLONE_CLASS_SUBCATEGORY)
                                .name(SourceMeterCSharpMetrics.CV.getName())
                                .type(PropertyType.INTEGER)
                                .build(),
                PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCSharpMetrics.NCR_KEY)
                                .category(CLONE_BASELINE_CATEGORY)
                                .subCategory(CLONE_CLASS_SUBCATEGORY)
                                .name(SourceMeterCSharpMetrics.NCR.getName())
                                .type(PropertyType.FLOAT)
                                .defaultValue("0.15")
                                .build(),
                PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterCSharpMetrics.CA_KEY)
                                .category(CLONE_BASELINE_CATEGORY)
                                .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                .name(SourceMeterCSharpMetrics.CA.getName())
                                .type(PropertyType.INTEGER)
                                .build(),
                PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterCSharpMetrics.CCO_KEY)
                                .category(CLONE_BASELINE_CATEGORY)
                                .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                .name(SourceMeterCSharpMetrics.CCO.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("5")
                                .build(),
                PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterCSharpMetrics.CE_KEY)
                                .category(CLONE_BASELINE_CATEGORY)
                                .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                .name(SourceMeterCSharpMetrics.CE.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("50")
                                .build(),
                PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterCSharpMetrics.CLLOC_KEY)
                                .category(CLONE_BASELINE_CATEGORY)
                                .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                .name(SourceMeterCSharpMetrics.CLLOC.getName())
                                .type(PropertyType.INTEGER)
                                .defaultValue("33")
                                .build(),
                PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterCSharpMetrics.CV_KEY)
                                .category(CLONE_BASELINE_CATEGORY)
                                .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                .name(SourceMeterCSharpMetrics.CV.getName())
                                .type(PropertyType.INTEGER)
                                .build()
        );
    }
}
