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
package com.sourcemeter.analyzer.rpg;

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
import com.sourcemeter.analyzer.rpg.batch.SourceMeterRPGInitializer;
import com.sourcemeter.analyzer.rpg.batch.SourceMeterRPGSensor;
import com.sourcemeter.analyzer.rpg.batch.decorators.DefaultDecoratorRPG;
import com.sourcemeter.analyzer.rpg.batch.decorators.FileMetricsDecoratorRPG;
import com.sourcemeter.analyzer.rpg.batch.decorators.LicenseDecoratorRPG;
import com.sourcemeter.analyzer.rpg.colorizer.RPGColorizer;
import com.sourcemeter.analyzer.rpg.core.RPG;
import com.sourcemeter.analyzer.rpg.profile.SourceMeterRPGProfile;
import com.sourcemeter.analyzer.rpg.profile.SourceMeterRPGRuleRepository;

@Properties({
             @Property(
                     key = "sm.rpg.spoolPattern",
                     name = "Spool pattern",
                     description = "File name pattern for spool (compiler listing) files.",
                     category = SourceMeterAnalyzerRPGPlugin.RPG_GENERAL_CATEGORY,
                     defaultValue = ".*\\.txt",
                     project = true
             ),
             @Property(
                     key = "sm.rpg.rpg3Pattern",
                     name = "RPG3 pattern",
                     description = "File name pattern for RPG/400 files.",
                     category = SourceMeterAnalyzerRPGPlugin.RPG_GENERAL_CATEGORY,
                     defaultValue = ".*\\.rpg",
                     project = true
             ),
             @Property(
                     key = "sm.rpg.rpg4Pattern",
                     name = "RPG4 pattern",
                     description = "File name pattern for ILE RPG files.",
                     category = SourceMeterAnalyzerRPGPlugin.RPG_GENERAL_CATEGORY,
                     defaultValue = ".*\\.rpgle",
                     project = true
             ),
             @Property(
                     key = "sm.rpg.uploadMethods",
                     name = "Upload methods to the database.",
                     global = false,
                     project = false,
                     type = PropertyType.BOOLEAN,
                     defaultValue = "true"
             ),
             @Property(
                     key = "sm.rpg.skip",
                     name = "Skip SourceMeter RPG analyze on multi language projects.",
                     global = false,
                     project = false,
                     type = PropertyType.BOOLEAN,
                     defaultValue = "false"
             ),
             @Property(
                     key = "sm.rpg.skipToolchain",
                     name = "Skip SourceMeter RPG toolchain (only upload results from existing result directory).",
                     global = false,
                     project = false,
                     type = PropertyType.BOOLEAN,
                     defaultValue = "false"
             ),
             @Property(
                     key = "sm.rpg.toolchainOptions",
                     name = "Add additional parameters for running SourceMeter RPG toolchain.",
                     global = false,
                     project = false,
                     type = PropertyType.STRING,
                     defaultValue = "false"
             )
})
public class SourceMeterAnalyzerRPGPlugin extends SonarPlugin {

    public static final String PROGRAM_BASELINE_KEY = "sm.rpg.program.baseline.";
    public static final String PROCEDURE_BASELINE_KEY = "sm.rpg.procedure.baseline.";
    public static final String SUBROUTINE_BASELINE_KEY = "sm.rpg.subroutine.baseline.";
    public static final String CLONE_CLASS_BASELINE_KEY = "sm.rpg.cloneclass.baseline.";
    public static final String CLONE_INSTANCE_BASELINE_KEY = "sm.rpg.cloneinstance.baseline.";

    public static final String RPG_GENERAL_CATEGORY = "SourceMeter RPG";
    public static final String BASELINE_CATEGORY = "SourceMeter RPG: Program Thresholds";
    public static final String CLONE_BASELINE_CATEGORY = "SourceMeter RPG: Clone Thresholds";
    public static final String PROCEDURE_BASELINE_CATEGORY = "SourceMeter RPG: Procedure Thresholds";
    public static final String SUBROUTINE_BASELINE_CATEGORY = "SourceMeter RPG: Subroutine Thresholds";

    public static final String CLONE_CLASS_SUBCATEGORY = "Clone Class";
    public static final String CLONE_INSTANCE_SUBCATEGORY = "Clone Instance";

    @Override
    public List getExtensions() {

        return Arrays.asList(
                             RPG.class,
                             SourceMeterRPGMetrics.class,
                             RPGColorizer.class,

                             // profile
                             SourceMeterRPGProfile.class,
                             SourceMeterRPGRuleRepository.class,

                             // Batch
                             SourceMeterRPGInitializer.class,
                             SourceMeterRPGSensor.class,

                             // decorators
                             DefaultDecoratorRPG.class,
                             FileMetricsDecoratorRPG.class,
                             ComplexityPostDecorator.class,
                             LicenseDecoratorRPG.class,
                             ProjectDecorator.class,

                             // metric threshold properties
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.LOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LOC.getDomain())
                                               .name("Lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1000")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.LLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LLOC.getDomain())
                                               .name("Logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("600")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.NOS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NOS.getDomain())
                                               .name("Number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("900")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.TLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TLOC.getDomain())
                                               .name("Total lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1600")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.TLLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TLLOC.getDomain())
                                               .name("Total logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("960")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.TNOS_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TNOS.getDomain())
                                               .name("Total number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("1440")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.CD_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CD.getDomain())
                                               .name("Comment density")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.CLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CLOC.getDomain())
                                               .name("Comment lines of code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.DLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.DLOC.getDomain())
                                               .name("Documentation Lines")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.TCLOC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TCLOC.getDomain())
                                               .name("Total Comment Lines of Code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.TCD_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TCD.getDomain())
                                               .name("Total Comment Density")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.NL_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NL.getDomain())
                                               .name("Nesting Level")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.NLE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NLE.getDomain())
                                               .name("Nesting Level Else-If")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("3")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.NII_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NII.getDomain())
                                               .name("Number of Incoming Invocations")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.NOI_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NOI.getDomain())
                                               .name("Number of Outgoing Invocations")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("50")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.CCL_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CCL.getDomain())
                                               .name("Clone Classes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.CCO_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CCO.getDomain())
                                               .name("Clone Complexity")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("57")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.CC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CC.getDomain())
                                               .name("Clone Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.CI_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CI.getDomain())
                                               .name("Clone Instances")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.CLC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CLC.getDomain())
                                               .name("Clone Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.CLLC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CLLC.getDomain())
                                               .name("Clone Logical Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.LDC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LDC.getDomain())
                                               .name("Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("150")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.LLDC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LLDC.getDomain())
                                               .name("Logical Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("90")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP0_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP0.getDomain())
                                               .name("Blocker")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP1_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP1.getDomain())
                                               .name("Critical")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP2_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP2.getDomain())
                                               .name("Major")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP3_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP3.getDomain())
                                               .name("Minor")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP4_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP4.getDomain())
                                               .name("Info")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),

                             // Thresholds for procedures
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.LOC_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LOC.getDomain())
                                               .name("Lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("100")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.LLOC_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LLOC.getDomain())
                                               .name("Logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("65")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.NUMPAR_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NUMPAR.getDomain())
                                               .name("Number of Parameters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.NOS_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NOS.getDomain())
                                               .name("Number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("90")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.TLOC_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TLOC.getDomain())
                                               .name("Total lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("160")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.TLLOC_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TLLOC.getDomain())
                                               .name("Total logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("104")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.TNOS_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TNOS.getDomain())
                                               .name("Total number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("144")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.CD_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CD.getDomain())
                                               .name("Comment density")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.CLOC_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CLOC.getDomain())
                                               .name("Comment lines of code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.DLOC_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.DLOC.getDomain())
                                               .name("Documentation Lines")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.TCLOC_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TCLOC.getDomain())
                                               .name("Total Comment Lines of Code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.TCD_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TCD.getDomain())
                                               .name("Total Comment Density")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.McCC_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.McCC.getDomain())
                                               .name("McCabe's Cyclomatic Complexity")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.NL_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NL.getDomain())
                                               .name("Nesting Level")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.NLE_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NLE.getDomain())
                                               .name("Nesting Level Else-If")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("3")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.NII_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NII.getDomain())
                                               .name("Number of Incoming Invocations")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.NOI_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NOI.getDomain())
                                               .name("Number of Outgoing Invocations")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.CCL_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CCL.getDomain())
                                               .name("Clone Classes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.CCO_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CCO.getDomain())
                                               .name("Clone Complexity")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.CC_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CC.getDomain())
                                               .name("Clone Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.CI_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CI.getDomain())
                                               .name("Clone Instances")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.CLC_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CLC.getDomain())
                                               .name("Clone Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.CLLC_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CLLC.getDomain())
                                               .name("Clone Logical Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.LDC_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LDC.getDomain())
                                               .name("Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.LLDC_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LLDC.getDomain())
                                               .name("Logical Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("9")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP0_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP0.getDomain())
                                               .name("Blocker")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP1_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP1.getDomain())
                                               .name("Critical")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP2_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP2.getDomain())
                                               .name("Major")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP3_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP3.getDomain())
                                               .name("Minor")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP4_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP4.getDomain())
                                               .name("Info")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),

                             // Thresholds for subroutines
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.LOC_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LOC.getDomain())
                                               .name("Lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("100")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.LLOC_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LLOC.getDomain())
                                               .name("Logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("65")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.NUMPAR_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NUMPAR.getDomain())
                                               .name("Number of Parameters")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.NOS_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NOS.getDomain())
                                               .name("Number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("90")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.TLOC_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TLOC.getDomain())
                                               .name("Total lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("160")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.TLLOC_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TLLOC.getDomain())
                                               .name("Total logical lines of code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("104")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.TNOS_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TNOS.getDomain())
                                               .name("Total number of Statements")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("144")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.CD_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CD.getDomain())
                                               .name("Comment density")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.CLOC_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CLOC.getDomain())
                                               .name("Comment lines of code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.DLOC_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.DLOC.getDomain())
                                               .name("Documentation Lines")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.TCLOC_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TCLOC.getDomain())
                                               .name("Total Comment Lines of Code")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.TCD_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.TCD.getDomain())
                                               .name("Total Comment Density")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("25")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.McCC_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.McCC.getDomain())
                                               .name("McCabe's Cyclomatic Complexity")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.NL_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NL.getDomain())
                                               .name("Nesting Level")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("5")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.NLE_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NLE.getDomain())
                                               .name("Nesting Level Else-If")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("3")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.NII_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NII.getDomain())
                                               .name("Number of Incoming Invocations")
                                               .type(PropertyType.INTEGER)
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.NOI_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.NOI.getDomain())
                                               .name("Number of Outgoing Invocations")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.CCL_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CCL.getDomain())
                                               .name("Clone Classes")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.CCO_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CCO.getDomain())
                                               .name("Clone Complexity")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("10")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.CC_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CC.getDomain())
                                               .name("Clone Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.CI_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CI.getDomain())
                                               .name("Clone Instances")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.CLC_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CLC.getDomain())
                                               .name("Clone Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.CLLC_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.CLLC.getDomain())
                                               .name("Clone Logical Line Coverage")
                                               .type(PropertyType.FLOAT)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.LDC_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LDC.getDomain())
                                               .name("Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("15")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.LLDC_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.LLDC.getDomain())
                                               .name("Logical Lines of Duplicated Code")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("9")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP0_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP0.getDomain())
                                               .name("Blocker")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP1_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP1.getDomain())
                                               .name("Critical")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP2_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP2.getDomain())
                                               .name("Major")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP3_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterCoreMetrics.WARNINGP3.getDomain())
                                               .name("Minor")
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY + SourceMeterCoreMetrics.WARNINGP4_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
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

                             // Program thresholds
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_BASIC_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_BASIC.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_BASIC.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_DESIGN_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_DESIGN.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_DESIGN.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_DOCUMENTATION_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_DOCUMENTATION.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_DOCUMENTATION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_NAMING_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_NAMING.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_NAMING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_SECURITY_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_SECURITY.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_SECURITY.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_SIZE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_SIZE.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_SIZE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_TYPE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_TYPE.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_TYPE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROGRAM_BASELINE_KEY
                                                                + SourceMeterRPGMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE_KEY)
                                               .category(BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),

                             // Procedure thresholds
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_BASIC_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_BASIC.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_BASIC.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_DESIGN_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_DESIGN.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_DESIGN.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_DOCUMENTATION_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_DOCUMENTATION.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_DOCUMENTATION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_NAMING_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_NAMING.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_NAMING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_SECURITY_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_SECURITY.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_SECURITY.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_SIZE_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_SIZE.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_SIZE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_TYPE_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_TYPE.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_TYPE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(PROCEDURE_BASELINE_KEY
                                                                + SourceMeterRPGMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE_KEY)
                                               .category(PROCEDURE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),

                             // Subroutine thresholds
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_BASIC_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_BASIC.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_BASIC.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_DESIGN_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_DESIGN.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_DESIGN.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_DOCUMENTATION_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_DOCUMENTATION.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_DOCUMENTATION.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_NAMING_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_NAMING.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_NAMING.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_SECURITY_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_SECURITY.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_SECURITY.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_SIZE_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_SIZE.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_SIZE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_TYPE_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_TYPE.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_TYPE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build(),
                             PropertyDefinition.builder(SUBROUTINE_BASELINE_KEY
                                                        + SourceMeterRPGMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE_KEY)
                                               .category(SUBROUTINE_BASELINE_CATEGORY)
                                               .subCategory(SourceMeterRPGMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE.getDomain())
                                               .name(SourceMeterRPGMetrics.ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE.getName())
                                               .type(PropertyType.INTEGER)
                                               .defaultValue("0")
                                               .build()
                     );
    }
}
