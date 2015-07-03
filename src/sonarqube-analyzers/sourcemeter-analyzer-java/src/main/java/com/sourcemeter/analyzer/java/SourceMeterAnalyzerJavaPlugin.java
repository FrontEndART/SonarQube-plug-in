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

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;

import com.sourcemeter.analyzer.base.batch.decorators.ComplexityPostDecorator;
import com.sourcemeter.analyzer.base.batch.decorators.ProjectDecorator;
import com.sourcemeter.analyzer.java.batch.DependenciesCollector;
import com.sourcemeter.analyzer.java.batch.SourceMeterJavaInitializer;
import com.sourcemeter.analyzer.java.batch.SourceMeterJavaSensor;
import com.sourcemeter.analyzer.java.batch.decorators.DefaultDecoratorJava;
import com.sourcemeter.analyzer.java.batch.decorators.FileMetricsDecoratorJava;
import com.sourcemeter.analyzer.java.batch.decorators.LicenseDecoratorJava;
import com.sourcemeter.analyzer.java.core.VersionChecker;
import com.sourcemeter.analyzer.java.profile.SourceMeterJavaProfile;
import com.sourcemeter.analyzer.java.profile.SourceMeterJavaRuleRepository;

/**
 * This class is the entry point for all extensions
 */
@Properties({
             @Property(
                 key = "sm.java.vhMaxDepth",
                 name = "Max depth for VulnerabilityHunter",
                 description = "Sets the maximum searching depth limit of the VulnerabilityHunter.",
                 category = SourceMeterAnalyzerJavaPlugin.JAVA_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.INTEGER
             ),
             @Property(
                 key = "sm.java.vhTimeOut",
                 name = "Timeout for VulnerabilityHunter",
                 description = "Sets the execution time-limit for the VulnerabilityHunter in minutes. After the given time the VulnerabilityHunter stops.",
                 category = SourceMeterAnalyzerJavaPlugin.JAVA_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.INTEGER
             ),
             @Property(
                 key = "sm.java.maxMem",
                 name = "JVM max memory",
                 description = "Max memory usage in megabytes.",
                 category = SourceMeterAnalyzerJavaPlugin.JAVA_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.INTEGER
             ),
             @Property(
                 key = "sm.java.runVulnerabilityHunter",
                 name = "Run VulnerabilityHunter",
                 description = "If true, then run VulnerabilityHunter. This may cause performance issues. License needed to run.",
                 category = SourceMeterAnalyzerJavaPlugin.JAVA_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.BOOLEAN,
                 defaultValue = "true"
             ),
             @Property(
                 key = "sm.java.hardFilter",
                 name = "Hard filter",
                 description = "Hard filter file's path for SourceMeter Java analyzer.",
                 category = SourceMeterAnalyzerJavaPlugin.JAVA_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.java.javacOptions",
                 name = "Javac Options",
                 global = false,
                 project = false,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.java.pmdOptions",
                 name = "PMD Options",
                 global = false,
                 project = false,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.java.csvSeparator",
                 name = "CSV Separator Character",
                 global = false,
                 project = false,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.java.uploadMethods",
                 name = "Upload methods to the database.",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = "true"
             ),
             @Property(
                 key = "sm.java.skip",
                 name = "Skip SourceMeter Java analyze on multi language projects.",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = SourceMeterAnalyzerJavaPlugin.FALSE
             ),
             @Property(
                 key = "sm.java.skipToolchain",
                 name = "Skip SourceMeter Java toolchain (only upload results from existing result directory).",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = SourceMeterAnalyzerJavaPlugin.FALSE
             ),
             @Property(
                 key = "sm.java.fbOptions",
                 name = "Add additional parameters for running FindBugs in SourceMeter toolchain.",
                 global = false,
                 project = false,
                 type = PropertyType.STRING,
                 defaultValue = SourceMeterAnalyzerJavaPlugin.FALSE
             ),
             @Property(
                 key = "sm.java.toolchainOptions",
                 name = "Add additional parameters for running SourceMeter Java toolchain.",
                 global = false,
                 project = false,
                 type = PropertyType.STRING,
                 defaultValue = SourceMeterAnalyzerJavaPlugin.FALSE
             )
})
public final class SourceMeterAnalyzerJavaPlugin extends SonarPlugin {

    public static final String CLASS_BASELINE_KEY = "sm.java.class.baseline.";
    public static final String METHOD_BASELINE_KEY = "sm.java.method.baseline.";
    public static final String CLONE_CLASS_BASELINE_KEY = "sm.java.cloneclass.baseline.";
    public static final String CLONE_INSTANCE_BASELINE_KEY = "sm.java.cloneinstance.baseline.";

    public static final String JAVA_GENERAL_CATEGORY = "SourceMeter Java";
    public static final String BASELINE_CATEGORY = "SourceMeter Java: Class Thresholds";
    public static final String CLONE_BASELINE_CATEGORY = "SourceMeter Java: Clone Thresholds";
    public static final String METHOD_BASELINE_CATEGORY = "SourceMeter Java: Method Thresholds";

    public static final String CLONE_CLASS_SUBCATEGORY = "Clone Class";
    public static final String CLONE_INSTANCE_SUBCATEGORY = "Clone Instance";

    public static final String FALSE = "false";

    // This is where you're going to declare all your Sonar extensions
    @SuppressWarnings({ "rawtypes" })
    @Override
    public List getExtensions() {
        return Arrays.asList(
                             // Core
                             VersionChecker.class,

                             // PMD
                             SourceMeterJavaRuleRepository.class,
                             SourceMeterJavaProfile.class,

                             // Metrics definitions
                             SourceMeterJavaMetrics.class,

                             // Batch
                             SourceMeterJavaInitializer.class,
                             SourceMeterJavaSensor.class,
                             DefaultDecoratorJava.class,
                             FileMetricsDecoratorJava.class,
                             ComplexityPostDecorator.class,
                             DependenciesCollector.class,
                             LicenseDecoratorJava.class,
                             ProjectDecorator.class,

                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.LOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.LOC.getDomain())
                                               .name("Lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1000")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.LLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.LLOC.getDomain())
                                               .name("Logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("600")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NA.getDomain())
                                               .name("Number of Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NG_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NG.getDomain())
                                               .name("Number of Getters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NLA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NLA.getDomain())
                                               .name("Number of Local Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NLG_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NLG.getDomain())
                                               .name("Number of Local Getters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NLM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NLM.getDomain())
                                               .name("Number of Local Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("20")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NLPA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NLPA.getDomain())
                                               .name("Number of Local Public Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NLPM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NLPM.getDomain())
                                               .name("Number of Local Public Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NLS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NLS.getDomain())
                                               .name("Number of Local Setters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NM.getDomain())
                                               .name("Number of Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("60")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NPA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NPA.getDomain())
                                               .name("Number of Public Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NPM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NPM.getDomain())
                                               .name("Number of Public Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("45")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NS.getDomain())
                                               .name("Number of Setters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NOS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NOS.getDomain())
                                               .name("Number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("900")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.TNA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TNA.getDomain())
                                               .name("Total Number of Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("24")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.TNG_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TNG.getDomain())
                                               .name("Total Number of Getters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("24")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.TNLA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TNLA.getDomain())
                                               .name("Total Number of Local Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("8")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.TNLG_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TNLG.getDomain())
                                               .name("Total Number of Local Getters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("8")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.TNLPA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TNLPA.getDomain())
                                               .name("Total Number of Local Public Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.TNLPM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TNLPM.getDomain())
                                               .name("Total Number of Local Public Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("24")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.TNLS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TNLS.getDomain())
                                               .name("Total Number of Local Setters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("8")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.TNLM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TNLM.getDomain())
                                               .name("Total Number of Local Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("32")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.TNM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TNM.getDomain())
                                               .name("Total Number of Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("96")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.TNPA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TNPA.getDomain())
                                               .name("Total Number of Public Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.TNPM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TNPM.getDomain())
                                               .name("Total Number of Public Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("72")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.TNS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TNS.getDomain())
                                               .name("Total Number of Setters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("24")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.TLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TLOC.getDomain())
                                               .name("Total lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1600")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.TLLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TLLOC.getDomain())
                                               .name("Total logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("960")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.TNOS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TNOS.getDomain())
                                               .name("Total number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1440")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.PUA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.PUA.getDomain())
                                               .name("Public undocumented API")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.AD_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.AD.getDomain())
                                               .name("API documentation")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("100")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.CD_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CD.getDomain())
                                               .name("Comment density")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.CLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CLOC.getDomain())
                                               .name("Comment lines of code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.DLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.DLOC.getDomain())
                                               .name("Documentation Lines")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.TCLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TCLOC.getDomain())
                                               .name("Total Comment Lines of Code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.TCD_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TCD.getDomain())
                                               .name("Total Comment Density")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NL_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NL.getDomain())
                                               .name("Nesting Level")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NLE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NLE.getDomain())
                                               .name("Nesting Level Else-If")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("3")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.WMC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.WMC.getDomain())
                                               .name("Weighted Methods per Class")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("115")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.CBO_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CBO.getDomain())
                                               .name("Coupling Between Object classes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.CBOI_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CBOI.getDomain())
                                               .name("Coupling Between Object classes Inverse")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NII_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NII.getDomain())
                                               .name("Number of Incoming Invocations")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NOI_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NOI.getDomain())
                                               .name("Number of Outgoing Invocations")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("50")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.RFC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.RFC.getDomain())
                                               .name("Response set For Class")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("70")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.LCOM5_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.LCOM5.getDomain())
                                               .name("Lack of Cohesion in Methods 5")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.CCL_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CCL.getDomain())
                                               .name("Clone Classes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.CCO_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CCO.getDomain())
                                               .name("Clone Complexity")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("57")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.CC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CC.getDomain())
                                               .name("Clone Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.CI_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CI.getDomain())
                                               .name("Clone Instances")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.CLC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CLC.getDomain())
                                               .name("Clone Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.CLLC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CLLC.getDomain())
                                               .name("Clone Logical Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.LDC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.LDC.getDomain())
                                               .name("Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("150")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.LLDC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.LLDC.getDomain())
                                               .name("Logical Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("90")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.WARNINGP0_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.WARNINGP0.getDomain())
                                               .name("Blocker")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.WARNINGP1_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.WARNINGP1.getDomain())
                                               .name("Critical")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.WARNINGP2_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.WARNINGP2.getDomain())
                                               .name("Major")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.WARNINGP3_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.WARNINGP3.getDomain())
                                               .name("Minor")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.WARNINGP4_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.WARNINGP4.getDomain())
                                               .name("Info")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.DIT_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.DIT.getDomain())
                                               .name("Depth of Inheritance Tree")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NOA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NOA.getDomain())
                                               .name("Number of Ancestors")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NOC.getDomain())
                                               .name("Number of Children")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NOD_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NOD.getDomain())
                                               .name("Number of Descendants")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("50")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NOP_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NOP.getDomain())
                                               .name("Number of Parents")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_BASIC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_BASIC.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_BASIC.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_BRACE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_BRACE.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_BRACE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_CLONE_IMPLEMENTATION_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_CLONE_IMPLEMENTATION.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_CLONE_IMPLEMENTATION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_CONTROVERSIAL_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_CONTROVERSIAL.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_CONTROVERSIAL.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_DESIGN_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_DESIGN.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_DESIGN.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_EMPTY_CODE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_EMPTY_CODE.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_EMPTY_CODE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_FINALIZER_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_FINALIZER.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_FINALIZER.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_IMPORT_STATEMENT_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_IMPORT_STATEMENT.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_IMPORT_STATEMENT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterJavaMetrics.ISSUEGROUP_J2EE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_J2EE.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_J2EE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_JAKARTA_COMMONS_LOGGING_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_JAKARTA_COMMONS_LOGGING.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_JAKARTA_COMMONS_LOGGING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_JAVABEAN_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_JAVABEAN.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_JAVABEAN.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_JAVA_LOGGING_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_JAVA_LOGGING.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_JAVA_LOGGING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_JUNIT_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_JUNIT.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_JUNIT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_NAMING_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_NAMING.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_NAMING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_OPTIMIZATION_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_OPTIMIZATION.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_OPTIMIZATION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_SECURITY_CODE_GUIDELINE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_SECURITY_CODE_GUIDELINE.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_SECURITY_CODE_GUIDELINE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_STRICT_EXCEPTION_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_STRICT_EXCEPTION.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_STRICT_EXCEPTION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_STRING_AND_STRINGBUFFER_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_STRING_AND_STRINGBUFFER.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_STRING_AND_STRINGBUFFER.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_TYPE_RESOLUTION_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_TYPE_RESOLUTION.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_TYPE_RESOLUTION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                            + SourceMeterJavaMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_ANDROID_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_ANDROID.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_ANDROID.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_BAD_PRACTICE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_BAD_PRACTICE.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_BAD_PRACTICE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_CODE_SIZE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_CODE_SIZE.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_CODE_SIZE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_COMMENT_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_COMMENT.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_COMMENT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_CORRECTNESS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_CORRECTNESS.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_CORRECTNESS.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_COUPLING_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_COUPLING.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_COUPLING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_DODGY_CODE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_DODGY_CODE.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_DODGY_CODE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_EXPERIMENTAL_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_EXPERIMENTAL.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_EXPERIMENTAL.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_INTERNATIONALIZATION_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_INTERNATIONALIZATION.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_INTERNATIONALIZATION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_MIGRATION_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_MIGRATION.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_MIGRATION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                            + SourceMeterJavaMetrics.ISSUEGROUP_MULTITHREADED_CORRECTNESS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_MULTITHREADED_CORRECTNESS.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_MULTITHREADED_CORRECTNESS.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_VULNERABILITY_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_VULNERABILITY.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_VULNERABILITY.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),

                             // Thresholds for methods
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.LOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.LOC.getDomain())
                                               .name("Lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("100")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.LLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.LLOC.getDomain())
                                               .name("Logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("65")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.NUMPAR_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NUMPAR.getDomain())
                                               .name("Number of Parameters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.NOS_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NOS.getDomain())
                                               .name("Number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("90")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.TLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TLOC.getDomain())
                                               .name("Total lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("160")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.TLLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TLLOC.getDomain())
                                               .name("Total logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("104")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.TNOS_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TNOS.getDomain())
                                               .name("Total number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("144")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.CD_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CD.getDomain())
                                               .name("Comment density")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.CLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CLOC.getDomain())
                                               .name("Comment lines of code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.DLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.DLOC.getDomain())
                                               .name("Documentation Lines")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.TCLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TCLOC.getDomain())
                                               .name("Total Comment Lines of Code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.TCD_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.TCD.getDomain())
                                               .name("Total Comment Density")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.McCC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.McCC.getDomain())
                                               .name("McCabe's Cyclomatic Complexity")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.NL_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NL.getDomain())
                                               .name("Nesting Level")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.NLE_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NLE.getDomain())
                                               .name("Nesting Level Else-If")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("3")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.NII_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NII.getDomain())
                                               .name("Number of Incoming Invocations")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.NOI_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.NOI.getDomain())
                                               .name("Number of Outgoing Invocations")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.CCL_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CCL.getDomain())
                                               .name("Clone Classes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.CCO_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CCO.getDomain())
                                               .name("Clone Complexity")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.CC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CC.getDomain())
                                               .name("Clone Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.CI_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CI.getDomain())
                                               .name("Clone Instances")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.CLC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CLC.getDomain())
                                               .name("Clone Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.CLLC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.CLLC.getDomain())
                                               .name("Clone Logical Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.LDC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.LDC.getDomain())
                                               .name("Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.LLDC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.LLDC.getDomain())
                                               .name("Logical Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("9")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.WARNINGP0_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.WARNINGP0.getDomain())
                                               .name("Blocker")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.WARNINGP1_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.WARNINGP1.getDomain())
                                               .name("Critical")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.WARNINGP2_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.WARNINGP2.getDomain())
                                               .name("Major")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.WARNINGP3_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.WARNINGP3.getDomain())
                                               .name("Minor")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterJavaMetrics.WARNINGP4_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.WARNINGP4.getDomain())
                                               .name("Info")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_BASIC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_BASIC.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_BASIC.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_BRACE_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_BRACE.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_BRACE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_CLONE_IMPLEMENTATION_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_CLONE_IMPLEMENTATION.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_CLONE_IMPLEMENTATION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_CONTROVERSIAL_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_CONTROVERSIAL.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_CONTROVERSIAL.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_DESIGN_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_DESIGN.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_DESIGN.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_EMPTY_CODE_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_EMPTY_CODE.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_EMPTY_CODE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_FINALIZER_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_FINALIZER.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_FINALIZER.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_IMPORT_STATEMENT_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_IMPORT_STATEMENT.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_IMPORT_STATEMENT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_J2EE_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_J2EE.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_J2EE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_JAKARTA_COMMONS_LOGGING_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_JAKARTA_COMMONS_LOGGING.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_JAKARTA_COMMONS_LOGGING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_JAVABEAN_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_JAVABEAN.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_JAVABEAN.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_JAVA_LOGGING_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_JAVA_LOGGING.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_JAVA_LOGGING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_JUNIT_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_JUNIT.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_JUNIT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_NAMING_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_NAMING.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_NAMING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_OPTIMIZATION_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_OPTIMIZATION.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_OPTIMIZATION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_SECURITY_CODE_GUIDELINE_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_SECURITY_CODE_GUIDELINE.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_SECURITY_CODE_GUIDELINE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_STRICT_EXCEPTION_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_STRICT_EXCEPTION.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_STRICT_EXCEPTION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_STRING_AND_STRINGBUFFER_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_STRING_AND_STRINGBUFFER.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_STRING_AND_STRINGBUFFER.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_TYPE_RESOLUTION_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_TYPE_RESOLUTION.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_TYPE_RESOLUTION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                            + SourceMeterJavaMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_ANDROID_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_ANDROID.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_ANDROID.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_BAD_PRACTICE_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_BAD_PRACTICE.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_BAD_PRACTICE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_CODE_SIZE_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_CODE_SIZE.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_CODE_SIZE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_COMMENT_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_COMMENT.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_COMMENT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_CORRECTNESS_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_CORRECTNESS.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_CORRECTNESS.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_COUPLING_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_COUPLING.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_COUPLING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_DODGY_CODE_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_DODGY_CODE.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_DODGY_CODE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_EXPERIMENTAL_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_EXPERIMENTAL.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_EXPERIMENTAL.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_INTERNATIONALIZATION_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_INTERNATIONALIZATION.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_INTERNATIONALIZATION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_MIGRATION_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_MIGRATION.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_MIGRATION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                            + SourceMeterJavaMetrics.ISSUEGROUP_MULTITHREADED_CORRECTNESS_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_MULTITHREADED_CORRECTNESS.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_MULTITHREADED_CORRECTNESS.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterJavaMetrics.ISSUEGROUP_VULNERABILITY_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterJavaMetrics.ISSUEGROUP_VULNERABILITY.getDomain())
                                               .name(SourceMeterJavaMetrics.ISSUEGROUP_VULNERABILITY.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),

                             // Clone metrics
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterJavaMetrics.CA_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterJavaMetrics.CA.getName())
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterJavaMetrics.CCO_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterJavaMetrics.CCO.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterJavaMetrics.CE_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterJavaMetrics.CE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("101")
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterJavaMetrics.CI_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterJavaMetrics.CI.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("2")
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterJavaMetrics.CLLOC_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterJavaMetrics.CLLOC.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("33")
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterJavaMetrics.CV_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterJavaMetrics.CV.getName())
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterJavaMetrics.NCR_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterJavaMetrics.NCR.getName())
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("0.15")
                                               .build(),
                             PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterJavaMetrics.CA_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                               .name(SourceMeterJavaMetrics.CA.getName())
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterJavaMetrics.CCO_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                               .name(SourceMeterJavaMetrics.CCO.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterJavaMetrics.CE_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                               .name(SourceMeterJavaMetrics.CE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("50")
                                               .build(),
                             PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterJavaMetrics.CLLOC_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                               .name(SourceMeterJavaMetrics.CLLOC.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("33")
                                               .build(),
                             PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterJavaMetrics.CV_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                               .name(SourceMeterJavaMetrics.CV.getName())
                                               .type(PropertyType.INTEGER)
                                               .build()
                     );
    }
}
