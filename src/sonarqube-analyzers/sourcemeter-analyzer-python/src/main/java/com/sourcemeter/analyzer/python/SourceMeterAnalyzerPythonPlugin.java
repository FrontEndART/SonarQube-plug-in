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

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.PropertyType;
import org.sonar.api.SonarPlugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreMetrics;

import com.sourcemeter.analyzer.base.batch.decorators.ComplexityPostDecorator;
import com.sourcemeter.analyzer.base.batch.decorators.ProjectDecorator;
import com.sourcemeter.analyzer.python.batch.SourceMeterPythonInitializer;
import com.sourcemeter.analyzer.python.batch.SourceMeterPythonSensor;
import com.sourcemeter.analyzer.python.batch.decorators.DefaultDecoratorPython;
import com.sourcemeter.analyzer.python.batch.decorators.FileMetricsDecoratorPython;
import com.sourcemeter.analyzer.python.batch.decorators.LicenseDecoratorPython;
import com.sourcemeter.analyzer.python.colorizer.PythonColorizer;
import com.sourcemeter.analyzer.python.core.Python;
import com.sourcemeter.analyzer.python.profile.SourceMeterPythonProfile;
import com.sourcemeter.analyzer.python.profile.SourceMeterPythonRuleRepository;

@Properties({
             @Property(
                 key = "sm.python.binary",
                 name = "Python 2.7 binary",
                 description = "Path of the Python 2.7 binary.",
                 category = SourceMeterAnalyzerPythonPlugin.PYTHON_GENERAL_CATEGORY,
                 project = true,
                 type = PropertyType.STRING
             ),
             @Property(
                 key = "sm.python.uploadMethods",
                 name = "Upload methods to the database.",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = "true"
             ),
             @Property(
                 key = "sm.python.skip",
                 name = "Skip SourceMeter Python analyze on multi language projects.",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = "false"
             ),
             @Property(
                 key = "sm.python.skipToolchain",
                 name = "Skip SourceMeter Python toolchain (only upload results from existing result directory).",
                 global = false,
                 project = false,
                 type = PropertyType.BOOLEAN,
                 defaultValue = "false"
             ),
             @Property(
                 key = "sm.python.toolchainOptions",
                 name = "Add additional parameters for running SourceMeter Python toolchain.",
                 global = false,
                 project = false,
                 type = PropertyType.STRING,
                 defaultValue = "false"
             )
})
public class SourceMeterAnalyzerPythonPlugin extends SonarPlugin {

    public static final String CLASS_BASELINE_KEY = "sm.python.class.baseline.";
    public static final String METHOD_BASELINE_KEY = "sm.python.method.baseline.";
    public static final String CLONE_CLASS_BASELINE_KEY = "sm.python.cloneclass.baseline.";
    public static final String CLONE_INSTANCE_BASELINE_KEY = "sm.python.cloneinstance.baseline.";

    public static final String PYTHON_GENERAL_CATEGORY = "SourceMeter Python";
    public static final String BASELINE_CATEGORY = "SourceMeter Python: Class Thresholds";
    public static final String CLONE_BASELINE_CATEGORY = "SourceMeter Python: Clone Thresholds";
    public static final String METHOD_BASELINE_CATEGORY = "SourceMeter Python: Method Thresholds";

    public static final String CLONE_CLASS_SUBCATEGORY = "Clone Class";
    public static final String CLONE_INSTANCE_SUBCATEGORY = "Clone Instance";

    @Override
    public List getExtensions() {

        return Arrays.asList(
                             Python.class,
                             PythonColorizer.class,
                             SourceMeterPythonMetrics.class,

                             // profile
                             SourceMeterPythonProfile.class,
                             SourceMeterPythonRuleRepository.class,

                             // Batch
                             SourceMeterPythonInitializer.class,
                             SourceMeterPythonSensor.class,

                             // decorators
                             DefaultDecoratorPython.class,
                             FileMetricsDecoratorPython.class,
                             ComplexityPostDecorator.class,
                             LicenseDecoratorPython.class,
                             ProjectDecorator.class,

                             // metric threshold properties
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.LOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LOC.getDomain())
                                               .name("Lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1000")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.LLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LLOC.getDomain())
                                               .name("Logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("600")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NA.getDomain())
                                               .name("Number of Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NG_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NG.getDomain())
                                               .name("Number of Getters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NLA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NLA.getDomain())
                                               .name("Number of Local Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NLG_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NLG.getDomain())
                                               .name("Number of Local Getters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NLM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NLM.getDomain())
                                               .name("Number of Local Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("20")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NLPA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NLPA.getDomain())
                                               .name("Number of Local Public Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NLPM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NLPM.getDomain())
                                               .name("Number of Local Public Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NLS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NLS.getDomain())
                                               .name("Number of Local Setters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NM.getDomain())
                                               .name("Number of Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("60")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NPA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NPA.getDomain())
                                               .name("Number of Public Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NPM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NPM.getDomain())
                                               .name("Number of Public Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("45")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NS.getDomain())
                                               .name("Number of Setters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NOS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NOS.getDomain())
                                               .name("Number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("900")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.TNA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TNA.getDomain())
                                               .name("Total Number of Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("24")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.TNG_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TNG.getDomain())
                                               .name("Total Number of Getters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("24")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.TNLA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TNLA.getDomain())
                                               .name("Total Number of Local Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("8")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.TNLG_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TNLG.getDomain())
                                               .name("Total Number of Local Getters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("8")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.TNLPA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TNLPA.getDomain())
                                               .name("Total Number of Local Public Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.TNLPM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TNLPM.getDomain())
                                               .name("Total Number of Local Public Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("24")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.TNLS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TNLS.getDomain())
                                               .name("Total Number of Local Setters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("8")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.TNLM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TNLM.getDomain())
                                               .name("Total Number of Local Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("32")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.TNM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TNM.getDomain())
                                               .name("Total Number of Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("96")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.TNPA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TNPA.getDomain())
                                               .name("Total Number of Public Attributes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.TNPM_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TNPM.getDomain())
                                               .name("Total Number of Public Methods")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("72")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.TNS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TNS.getDomain())
                                               .name("Total Number of Setters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("24")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.TLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TLOC.getDomain())
                                               .name("Total lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1600")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.TLLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TLLOC.getDomain())
                                               .name("Total logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("960")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.TNOS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TNOS.getDomain())
                                               .name("Total number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1440")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.PUA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.PUA.getDomain())
                                               .name("Public undocumented API")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.AD_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.AD.getDomain())
                                               .name("API documentation")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("100")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.CD_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CD.getDomain())
                                               .name("Comment density")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.CLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CLOC.getDomain())
                                               .name("Comment lines of code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.DLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.DLOC.getDomain())
                                               .name("Documentation Lines")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.TCLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TCLOC.getDomain())
                                               .name("Total Comment Lines of Code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.TCD_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TCD.getDomain())
                                               .name("Total Comment Density")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NL_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NL.getDomain())
                                               .name("Nesting Level")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NLE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NLE.getDomain())
                                               .name("Nesting Level Else-If")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("3")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.WMC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WMC.getDomain())
                                               .name("Weighted Methods per Class")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("115")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.CBO_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CBO.getDomain())
                                               .name("Coupling Between Object classes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.CBOI_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CBOI.getDomain())
                                               .name("Coupling Between Object classes Inverse")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NII_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NII.getDomain())
                                               .name("Number of Incoming Invocations")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NOI_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NOI.getDomain())
                                               .name("Number of Outgoing Invocations")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("50")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.RFC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.RFC.getDomain())
                                               .name("Response set For Class")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("70")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.LCOM5_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LCOM5.getDomain())
                                               .name("Lack of Cohesion in Methods 5")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.CCL_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CCL.getDomain())
                                               .name("Clone Classes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.CCO_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CCO.getDomain())
                                               .name("Clone Complexity")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("57")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.CC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CC.getDomain())
                                               .name("Clone Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.CI_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CI.getDomain())
                                               .name("Clone Instances")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.CLC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CLC.getDomain())
                                               .name("Clone Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.CLLC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CLLC.getDomain())
                                               .name("Clone Logical Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.LDC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LDC.getDomain())
                                               .name("Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("150")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.LLDC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LLDC.getDomain())
                                               .name("Logical Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("90")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP0_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP0.getDomain())
                                               .name("Blocker")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP1_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP1.getDomain())
                                               .name("Critical")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP2_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP2.getDomain())
                                               .name("Major")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP3_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP3.getDomain())
                                               .name("Minor")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP4_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP4.getDomain())
                                               .name("Info")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.DIT_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.DIT.getDomain())
                                               .name("Depth of Inheritance Tree")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NOA_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NOA.getDomain())
                                               .name("Number of Ancestors")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NOC.getDomain())
                                               .name("Number of Children")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NOD_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NOD.getDomain())
                                               .name("Number of Descendants")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("50")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NOP_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NOP.getDomain())
                                               .name("Number of Parents")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1")
                                               .build(),

                             // Thresholds for methods
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.LOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LOC.getDomain())
                                               .name("Lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("100")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.LLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LLOC.getDomain())
                                               .name("Logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("65")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.NUMPAR_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NUMPAR.getDomain())
                                               .name("Number of Parameters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.NOS_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NOS.getDomain())
                                               .name("Number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("90")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.TLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TLOC.getDomain())
                                               .name("Total lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("160")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.TLLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TLLOC.getDomain())
                                               .name("Total logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("104")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.TNOS_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TNOS.getDomain())
                                               .name("Total number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("144")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.CD_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CD.getDomain())
                                               .name("Comment density")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.CLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CLOC.getDomain())
                                               .name("Comment lines of code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.DLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.DLOC.getDomain())
                                               .name("Documentation Lines")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.TCLOC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TCLOC.getDomain())
                                               .name("Total Comment Lines of Code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.TCD_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TCD.getDomain())
                                               .name("Total Comment Density")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.McCC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.McCC.getDomain())
                                               .name("McCabe's Cyclomatic Complexity")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.NL_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NL.getDomain())
                                               .name("Nesting Level")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.NLE_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NLE.getDomain())
                                               .name("Nesting Level Else-If")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("3")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.NII_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NII.getDomain())
                                               .name("Number of Incoming Invocations")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.NOI_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NOI.getDomain())
                                               .name("Number of Outgoing Invocations")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.CCL_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CCL.getDomain())
                                               .name("Clone Classes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.CCO_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CCO.getDomain())
                                               .name("Clone Complexity")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.CC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CC.getDomain())
                                               .name("Clone Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.CI_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CI.getDomain())
                                               .name("Clone Instances")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.CLC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CLC.getDomain())
                                               .name("Clone Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.CLLC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CLLC.getDomain())
                                               .name("Clone Logical Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.LDC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LDC.getDomain())
                                               .name("Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.LLDC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LLDC.getDomain())
                                               .name("Logical Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("9")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP0_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP0.getDomain())
                                               .name("Blocker")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP1_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP1.getDomain())
                                               .name("Critical")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP2_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP2.getDomain())
                                               .name("Major")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP3_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP3.getDomain())
                                               .name("Minor")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP4_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP4.getDomain())
                                               .name("Info")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),

                             // Clone metrics
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCoreMetrics.CA_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterCoreMetrics.CA.getName())
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCoreMetrics.CCO_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterCoreMetrics.CCO.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCoreMetrics.CE_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterCoreMetrics.CE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("101")
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCoreMetrics.CI_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterCoreMetrics.CI.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("2")
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCoreMetrics.CLLOC_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterCoreMetrics.CLLOC.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("33")
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCoreMetrics.CV_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterCoreMetrics.CV.getName())
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLONE_CLASS_BASELINE_KEY + SourceMeterCoreMetrics.NCR_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_CLASS_SUBCATEGORY)
                                               .name(SourceMeterCoreMetrics.NCR.getName())
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("0.15")
                                               .build(),
                             PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterCoreMetrics.CA_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                               .name(SourceMeterCoreMetrics.CA.getName())
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterCoreMetrics.CCO_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                               .name(SourceMeterCoreMetrics.CCO.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterCoreMetrics.CE_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                               .name(SourceMeterCoreMetrics.CE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("50")
                                               .build(),
                             PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterCoreMetrics.CLLOC_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                               .name(SourceMeterCoreMetrics.CLLOC.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("33")
                                               .build(),
                             PropertyDefinition.builder(CLONE_INSTANCE_BASELINE_KEY + SourceMeterCoreMetrics.CV_KEY)
                                               .category(CLONE_BASELINE_CATEGORY)
                                               .subCategory(CLONE_INSTANCE_SUBCATEGORY)
                                               .name(SourceMeterCoreMetrics.CV.getName())
                                               .type(PropertyType.INTEGER)
                                               .build(),

                             // Class thresholds
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_BASIC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_BASIC.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_BASIC.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_CLASS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_CLASS.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_CLASS.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_DESIGN_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_DESIGN.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_DESIGN.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_EXCEPTION_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_EXCEPTION.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_EXCEPTION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_FORMAT_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_FORMAT.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_FORMAT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_IMPORT_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_IMPORT.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_IMPORT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_LOGGING_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_LOGGING.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_LOGGING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_MISCELLANEOUS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_MISCELLANEOUS.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_MISCELLANEOUS.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_NEWSTYLE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_NEWSTYLE.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_NEWSTYLE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_PYLINT_CHECKER_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_PYLINT_CHECKER.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_PYLINT_CHECKER.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_PYTHON3_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_PYTHON3.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_PYTHON3.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_SIMILARITY_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_SIMILARITY.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_SIMILARITY.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_SPELLING_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_SPELLING.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_SPELLING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_STDLIB_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_STDLIB.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_STDLIB.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_STRING_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_STRING.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_STRING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_STRING_CONSTANT_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_STRING_CONSTANT.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_STRING_CONSTANT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_TYPECHECK_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_TYPECHECK.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_TYPECHECK.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(CLASS_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_VARIABLE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_VARIABLE.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_VARIABLE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),

                             // Method thresholds
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_BASIC_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_BASIC.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_BASIC.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_CLASS_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_CLASS.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_CLASS.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_DESIGN_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_DESIGN.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_DESIGN.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_EXCEPTION_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_EXCEPTION.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_EXCEPTION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_FORMAT_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_FORMAT.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_FORMAT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_IMPORT_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_IMPORT.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_IMPORT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_LOGGING_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_LOGGING.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_LOGGING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_MISCELLANEOUS_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_MISCELLANEOUS.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_MISCELLANEOUS.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_NEWSTYLE_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_NEWSTYLE.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_NEWSTYLE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_PYLINT_CHECKER_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_PYLINT_CHECKER.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_PYLINT_CHECKER.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_PYTHON3_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_PYTHON3.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_PYTHON3.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_SIMILARITY_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_SIMILARITY.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_SIMILARITY.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_SPELLING_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_SPELLING.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_SPELLING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_STDLIB_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_STDLIB.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_STDLIB.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_STRING_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_STRING.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_STRING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_STRING_CONSTANT_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_STRING_CONSTANT.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_STRING_CONSTANT.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_TYPECHECK_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_TYPECHECK.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_TYPECHECK.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(METHOD_BASELINE_KEY
                                                        + SourceMeterPythonMetrics.ISSUEGROUP_VARIABLE_KEY)
                                               .category(METHOD_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterPythonMetrics.ISSUEGROUP_VARIABLE.getDomain())
                                               .name(SourceMeterPythonMetrics.ISSUEGROUP_VARIABLE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build()
                     );
    }
}
