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
package org.sonar.plugins.SourceMeterCore.api;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.Builder;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.measures.Metrics;
import org.sonar.api.measures.SumChildValuesFormula;

/**
 * Class containing SourceMeter core metrics definitions
 */
public class SourceMeterCoreMetrics implements Metrics {

    public static final String COLUMBUS_DOMAIN_SIZE = "SM:Size";
    public static final String COLUMBUS_DOMAIN_COMPLEXITY = "SM:Complexity";
    public static final String COLUMBUS_DOMAIN_WARNINGS = "SM:Rule Priorities";
    public static final String COLUMBUS_DOMAIN_DOCUMENTATION = "SM:Documentation";
    public static final String COLUMBUS_DOMAIN_COUPLING = "SM:Coupling";
    public static final String COLUMBUS_DOMAIN_INHERITANCE = "SM:Inheritance";
    public static final String COLUMBUS_DOMAIN_COHESION = "SM:Cohesion";
    public static final String COLUMBUS_DOMAIN_CLONE = "SM:Clone";
    public static final String COLUMBUS_DOMAIN_ISSUEGROUP = "SM:Rulesets";

    public static final String LICENSE_KEY = "SM:license";
    public static final Metric LICENSE = new Builder(
            LICENSE_KEY, "SourceMeter license information",
            ValueType.STRING)
            .setHidden(true)
            .create();

    public static final String SM_RESOURCE_KEY = "SM:resource";
    public static final Metric SM_RESOURCE = new Builder(
            SM_RESOURCE_KEY, "SourceMeter data is uploaded for the resource.",
            ValueType.BOOL)
            .setHidden(true)
            .create();

    public static final String BEGIN_LINE_KEY = "SM:beginline";
    public static final Metric BEGIN_LINE = new Builder(BEGIN_LINE_KEY, "Begin Line", ValueType.INT)
            .setHidden(true)
            .create();

    public static final String END_LINE_KEY = "SM:endline";
    public static final Metric END_LINE = new Builder(END_LINE_KEY, "End Line", ValueType.INT)
            .setHidden(true)
            .create();

    public static final String DPF_KEY = "SM:duplicated_files";
    public static final Metric DPF = new Builder(DPF_KEY, "Duplicated Files", ValueType.INT)
            .setHidden(true)
            .setFormula(new SumChildValuesFormula(false))
            .create();

    public static final String WARNINGP0_KEY = "WarningBlocker";
    public static final Metric WARNINGP0 = new Builder(WARNINGP0_KEY, "Blocker", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_WARNINGS)
            .setFormula(new SumChildValuesFormula(false))
            .create();

    public static final String WARNINGP1_KEY = "WarningCritical";
    public static final Metric WARNINGP1 = new Builder(WARNINGP1_KEY, "Critical", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_WARNINGS)
            .setFormula(new SumChildValuesFormula(false))
            .create();

    public static final String WARNINGP2_KEY = "WarningMajor";
    public static final Metric WARNINGP2 = new Builder(WARNINGP2_KEY, "Major", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_WARNINGS)
            .setFormula(new SumChildValuesFormula(false))
            .create();

    public static final String WARNINGP3_KEY = "WarningMinor";
    public static final Metric WARNINGP3 = new Builder(WARNINGP3_KEY, "Minor", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_WARNINGS)
            .setFormula(new SumChildValuesFormula(false))
            .create();
    public static final String WARNINGP4_KEY = "WarningInfo";
    public static final Metric WARNINGP4 = new Builder(WARNINGP4_KEY, "Info", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_WARNINGS)
            .setFormula(new SumChildValuesFormula(false))
            .create();
    /* End of Rule Priority metrics */

    /* Size Metrics */
    public static final String LOC_KEY = "LOC";
    public static final Metric LOC = new Builder(LOC_KEY, "Lines of Code", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TLOC_KEY = "TLOC";
    public static final Metric TLOC = new Builder(TLOC_KEY, "Total Lines of Code", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String LLOC_KEY = "LLOC";
    public static final Metric LLOC = new Builder(LLOC_KEY, "Logical Lines of Code", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TLLOC_KEY = "TLLOC";
    public static final Metric TLLOC = new Builder(TLLOC_KEY, "Total Logical Lines of Code", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NPKG_KEY = "NPKG";
    public static final Metric NPKG = new Builder(NPKG_KEY, "Number of Packages", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNPKG_KEY = "TNPKG";
    public static final Metric TNPKG = new Builder(TNPKG_KEY, "Total Number of Packages", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NCL_KEY = "NCL";
    public static final Metric NCL = new Builder(NCL_KEY, "Number of Classes", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNCL_KEY = "TNCL";
    public static final Metric TNCL = new Builder(TNCL_KEY, "Total Number of Classes", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NIN_KEY = "NIN";
    public static final Metric NIN = new Builder(NIN_KEY, "Number of Interfaces", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNIN_KEY = "TNIN";
    public static final Metric TNIN = new Builder(TNIN_KEY, "Total Number of Interfaces", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NM_KEY = "NM";
    public static final Metric NM = new Builder(NM_KEY, "Number of Methods", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNM_KEY = "TNM";
    public static final Metric TNM = new Builder(TNM_KEY, "Total Number of Methods", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NA_KEY = "NA";
    public static final Metric NA = new Builder(NA_KEY, "Number of Attributes", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNA_KEY = "TNA";
    public static final Metric TNA = new Builder(TNA_KEY, "Total Number of Attributes", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NLA_KEY = "NLA";
    public static final Metric NLA = new Builder(NLA_KEY, "Number of Local Attributes", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNLA_KEY = "TNLA";
    public static final Metric TNLA = new Builder(TNLA_KEY, "Total Number of Local Attributes", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NOS_KEY = "NOS";
    public static final Metric NOS = new Builder(NOS_KEY, "Number of Statements", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNOS_KEY = "TNOS";
    public static final Metric TNOS = new Builder(TNOS_KEY, "Total Number of Statements", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NUMPAR_KEY = "NUMPAR";
    public static final Metric NUMPAR = new Builder(NUMPAR_KEY, "Number of Parameters", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNDI_KEY = "TNDI";
    public static final Metric TNDI = new Builder(TNDI_KEY, "Total Number of Directories", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNFI_KEY = "TNFI";
    public static final Metric TNFI = new Builder(TNFI_KEY, "Total Number of Files", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNPCL_KEY = "TNPCL";
    public static final Metric TNPCL = new Builder(TNPCL_KEY, "Total Number of Public Classes", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNPIN_KEY = "TNPIN";
    public static final Metric TNPIN = new Builder(TNPIN_KEY, "Total Number of Public Interfaces", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NEN_KEY = "NEN";
    public static final Metric NEN = new Builder(NEN_KEY, "Number of Enums", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNEN_KEY = "TNEN";
    public static final Metric TNEN = new Builder(TNEN_KEY, "Total Number of Enums", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNPEN_KEY = "TNPEN";
    public static final Metric TNPEN = new Builder(TNPEN_KEY, "Total Number of Public Enums", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NPM_KEY = "NPM";
    public static final Metric NPM = new Builder(NPM_KEY, "Number of Public Methods", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNPM_KEY = "TNPM";
    public static final Metric TNPM = new Builder(TNPM_KEY, "Total Number of Public Methods", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NLM_KEY = "NLM";
    public static final Metric NLM = new Builder(NLM_KEY, "Number of Local Methods", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNLM_KEY = "TNLM";
    public static final Metric TNLM = new Builder(TNLM_KEY, "Total Number of Local Methods", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NS_KEY = "NS";
    public static final Metric NS = new Builder(NS_KEY, "Number of Setters", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNS_KEY = "TNS";
    public static final Metric TNS = new Builder(TNS_KEY, "Total Number of Setters", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NG_KEY = "NG";
    public static final Metric NG = new Builder(NG_KEY, "Number of Getters", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNG_KEY = "TNG";
    public static final Metric TNG = new Builder(TNG_KEY, "Total Number of Getters", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NPA_KEY = "NPA";
    public static final Metric NPA = new Builder(NPA_KEY, "Number of Public Attributes", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNPA_KEY = "TNPA";
    public static final Metric TNPA = new Builder(TNPA_KEY, "Total Number of Public Attributes", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    // new metrics
    public static final String NLG_KEY = "NLG";
    public static final Metric NLG = new Builder(NLG_KEY, "Number of Local Getters", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NLPA_KEY = "NLPA";
    public static final Metric NLPA = new Builder(NLPA_KEY, "Number of Local Public Attributes", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NLPM_KEY = "NLPM";
    public static final Metric NLPM = new Builder(NLPM_KEY, "Number of Local Public Methods", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NLS_KEY = "NLS";
    public static final Metric NLS = new Builder(NLS_KEY, "Number of Local Setters", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNLG_KEY = "TNLG";
    public static final Metric TNLG = new Builder(TNLG_KEY, "Total Number of Local Getters", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNLPA_KEY = "TNLPA";
    public static final Metric TNLPA = new Builder(TNLPA_KEY, "Total Number of Local Public Attributes", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNLPM_KEY = "TNLPM";
    public static final Metric TNLPM = new Builder(TNLPM_KEY, "Total Number of Local Public Methods", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNLS_KEY = "TNLS";
    public static final Metric TNLS = new Builder(TNLS_KEY, "Total Number of Local Setters", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();
    /* END of Size Metrics */

    /* Documentation Metrics */
    public static final String DLOC_KEY = "DLOC";
    public static final Metric DLOC = new Builder(DLOC_KEY, "Documentation Lines", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_DOCUMENTATION)
            .create();

    public static final String CLOC_KEY = "CLOC";
    public static final Metric CLOC = new Builder(CLOC_KEY, "Comment Lines of Code", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_DOCUMENTATION)
            .create();

    public static final String TCLOC_KEY = "TCLOC";
    public static final Metric TCLOC = new Builder(TCLOC_KEY, "Total Comment Lines of Code", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_DOCUMENTATION)
            .create();

    public static final String CD_KEY = "CD";
    public static final Metric CD = new Builder(CD_KEY, "Comment Density", ValueType.PERCENT)
            .setDomain(COLUMBUS_DOMAIN_DOCUMENTATION)
            .setDirection(Metric.DIRECTION_BETTER)
            .create();

    public static final String TCD_KEY = "TCD";
    public static final Metric TCD = new Builder(TCD_KEY, "Total Comment Density", ValueType.PERCENT)
            .setDomain(COLUMBUS_DOMAIN_DOCUMENTATION)
            .setDirection(Metric.DIRECTION_BETTER)
            .create();

    public static final String AD_KEY = "AD";
    public static final Metric AD = new Builder(AD_KEY, "API Documentation", ValueType.PERCENT)
            .setDomain(COLUMBUS_DOMAIN_DOCUMENTATION)
            .setDirection(Metric.DIRECTION_BETTER)
            .create();

    public static final String TAD_KEY = "TAD";
    public static final Metric TAD = new Builder(TAD_KEY, "Total API Documentation", ValueType.PERCENT)
            .setDomain(COLUMBUS_DOMAIN_DOCUMENTATION)
            .create();

    public static final String PUA_KEY = "PUA";
    public static final Metric PUA = new Builder(PUA_KEY, "Public Undocumented API", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_DOCUMENTATION)
            .create();

    public static final String TPUA_KEY = "TPUA";
    public static final Metric TPUA = new Builder(TPUA_KEY, "Total Public Undocumented API", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_DOCUMENTATION)
            .create();

    public static final String PDA_KEY = "PDA";
    public static final Metric PDA = new Builder(PDA_KEY, "Public Documented API", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_DOCUMENTATION)
            .create();

    public static final String TPDA_KEY = "TPDA";
    public static final Metric TPDA = new Builder(TPDA_KEY, "Total Public Documented API", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_DOCUMENTATION)
            .create();
    /* END of Documentation Metrics */

    /* Complexity Metrics */
    public static final String McCC_KEY = "McCC";
    public static final Metric McCC = new Builder(McCC_KEY, "McCabe's Cyclomatic Complexity", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_COMPLEXITY)
            .create();

    public static final String NL_KEY = "NL";
    public static final Metric NL = new Builder(NL_KEY, "Nesting Level", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_COMPLEXITY)
            .create();

    public static final String NLE_KEY = "NLE";
    public static final Metric NLE = new Builder(NLE_KEY, "Nesting Level Else-If", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_COMPLEXITY)
            .create();

    public static final String WMC_KEY = "WMC";
    public static final Metric WMC = new Builder(WMC_KEY, "Weighted Methods per Class", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_COMPLEXITY)
            .create();
    /* END of Complexity Metrics */

    /* Join Metrics */
    public static final String CBO_KEY = "CBO";
    public static final Metric CBO = new Builder(CBO_KEY, "Coupling Between Object classes", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_COUPLING)
            .create();

    public static final String RFC_KEY = "RFC";
    public static final Metric RFC = new Builder(RFC_KEY, "Response set For Class", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_COUPLING)
            .create();

    public static final String NOI_KEY = "NOI";
    public static final Metric NOI = new Builder(NOI_KEY, "Number of Outgoing Invocations", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_COUPLING)
            .create();

    public static final String NII_KEY = "NII";
    public static final Metric NII = new Builder(NII_KEY, "Number of Incoming Invocations", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_COUPLING)
            .create();

    public static final String CBOI_KEY = "CBOI";
    public static final Metric CBOI = new Builder(CBOI_KEY, "Coupling Between Object classes Inverse", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_COUPLING)
            .create();
    /* END of Coupling Metrics */

    /* Inheritance Metrics */
    public static final String NOP_KEY = "NOP";
    public static final Metric NOP = new Builder(NOP_KEY, "Number of Parents", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_INHERITANCE)
            .create();

    public static final String NOC_KEY = "NOC";
    public static final Metric NOC = new Builder(NOC_KEY, "Number of Children", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_INHERITANCE)
            .create();

    public static final String NOA_KEY = "NOA";
    public static final Metric NOA = new Builder(NOA_KEY, "Number of Ancestors", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_INHERITANCE)
            .create();

    public static final String NOD_KEY = "NOD";
    public static final Metric NOD = new Builder(NOD_KEY, "Number of Descendants", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_INHERITANCE)
            .create();

    public static final String DIT_KEY = "DIT";
    public static final Metric DIT = new Builder(DIT_KEY, "Depth of Inheritance Tree", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_INHERITANCE)
            .create();

    /* END of Inheritance Metrics */

    /* Cohesion Metrics */
    public static final String LCOM5_KEY = "LCOM5";
    public static final Metric LCOM5 = new Builder(LCOM5_KEY, "Lack of Cohesion in Methods 5", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_COHESION)
            .create();

    /* END of Cohesion Metrics */

    /* Clone Metrics */
    public static final String CC_KEY = "CC";
    public static final Metric CC = new Builder(CC_KEY, "Clone Coverage", ValueType.PERCENT)
            .setDomain(COLUMBUS_DOMAIN_CLONE)
            .create();

    public static final String CCL_KEY = "CCL";
    public static final Metric CCL = new Builder(CCL_KEY, "Clone Classes", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_CLONE)
            .setFormula(new SumChildValuesFormula(false))
            .create();

    public static final String CCO_KEY = "CCO";
    public static final Metric CCO = new Builder(CCO_KEY, "Clone Complexity", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_CLONE)
            .create();

    public static final String CI_KEY = "CI";
    public static final Metric CI = new Builder(CI_KEY, "Clone Instances", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_CLONE)
            .create();

    public static final String CLLOC_KEY = "CLLOC";
    public static final Metric CLLOC = new Builder(CLLOC_KEY, "Clone Lines of Code", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_CLONE)
            .create();

    public static final String CE_KEY = "CE";
    public static final Metric CE = new Builder(CE_KEY, "Clone Embeddedness", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_CLONE)
            .create();

    public static final String CA_KEY = "CA";
    public static final Metric CA = new Builder(CA_KEY, "Clone Age", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_CLONE)
            .create();

    public static final String NCR_KEY = "NCR";
    public static final Metric NCR = new Builder(NCR_KEY, "Normalized Clone Radius", ValueType.FLOAT)
            .setDomain(COLUMBUS_DOMAIN_CLONE)
            .create();

    public static final String CV_KEY = "CV";
    public static final Metric CV = new Builder(CV_KEY, "Clone Variability", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_CLONE)
            .create();

    public static final String CLC_KEY = "CLC";
    public static final Metric CLC = new Builder(CLC_KEY, "Clone Line Coverage", ValueType.PERCENT)
            .setDomain(COLUMBUS_DOMAIN_CLONE)
            .setFormula(new SumChildValuesFormula(false))
            .create();

    public static final String CLLC_KEY = "CLLC";
    public static final Metric CLLC = new Builder(CLLC_KEY, "Clone Logical Line Coverage", ValueType.PERCENT)
            .setDomain(COLUMBUS_DOMAIN_CLONE)
            .create();

    public static final String LDC_KEY = "LDC";
    public static final Metric LDC = new Builder(LDC_KEY, "Lines of Duplicated Code", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_CLONE)
            .setFormula(new SumChildValuesFormula(false))
            .create();

    public static final String LLDC_KEY = "LLDC";
    public static final Metric LLDC = new Builder(LLDC_KEY, "Logical Lines of Duplicated Code", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_CLONE)
            .create();

    /* End of Clone Metrics */
    public static final String ISSUEGROUP_EXCEPTION_KEY = "Exception Rules";
    public static final Metric ISSUEGROUP_EXCEPTION = new Builder(
        ISSUEGROUP_EXCEPTION_KEY, "Exception", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_BASIC_KEY = "Basic Rules";
    public static final Metric ISSUEGROUP_BASIC = new Builder(
        ISSUEGROUP_BASIC_KEY, "Basic", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_DESIGN_KEY = "Design Rules";
    public static final Metric ISSUEGROUP_DESIGN = new Builder(
        ISSUEGROUP_DESIGN_KEY, "Design", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_NAMING_KEY = "Naming Rules";
    public static final Metric ISSUEGROUP_NAMING = new Builder(
        ISSUEGROUP_NAMING_KEY, "Naming", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE_KEY = "Unnecessary and Unused Code Rules";
    public static final Metric ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE = new Builder(
        ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE_KEY, "Unnecessary and Unused Code", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_BRACE_KEY = "Brace Rules";
    public static final Metric ISSUEGROUP_BRACE = new Builder(
        ISSUEGROUP_BRACE_KEY, "Brace", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_CLONE_IMPLEMENTATION_KEY = "Clone Implementation Rules";
    public static final Metric ISSUEGROUP_CLONE_IMPLEMENTATION = new Builder(
        ISSUEGROUP_CLONE_IMPLEMENTATION_KEY, "Clone Implementation", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_CONTROVERSIAL_KEY = "Controversial Rules";
    public static final Metric ISSUEGROUP_CONTROVERSIAL = new Builder(
        ISSUEGROUP_CONTROVERSIAL_KEY, "Controversial", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_EMPTY_CODE_KEY = "Empty Code Rules";
    public static final Metric ISSUEGROUP_EMPTY_CODE = new Builder(
        ISSUEGROUP_EMPTY_CODE_KEY, "Empty Code", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_FINALIZER_KEY = "Finalizer Rules";
    public static final Metric ISSUEGROUP_FINALIZER = new Builder(
        ISSUEGROUP_FINALIZER_KEY, "Finalizer", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_IMPORT_STATEMENT_KEY = "Import Statement Rules";
    public static final Metric ISSUEGROUP_IMPORT_STATEMENT = new Builder(
        ISSUEGROUP_IMPORT_STATEMENT_KEY, "Import Statement", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_J2EE_KEY = "J2EE Rules";
    public static final Metric ISSUEGROUP_J2EE = new Builder(
        ISSUEGROUP_J2EE_KEY, "J2EE", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_JAKARTA_COMMONS_LOGGING_KEY = "Jakarta Commons Logging Rules";
    public static final Metric ISSUEGROUP_JAKARTA_COMMONS_LOGGING = new Builder(
        ISSUEGROUP_JAKARTA_COMMONS_LOGGING_KEY, "Jakarta Commons Logging", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_JAVABEAN_KEY = "JavaBean Rules";
    public static final Metric ISSUEGROUP_JAVABEAN = new Builder(
        ISSUEGROUP_JAVABEAN_KEY, "JavaBean", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_JAVA_LOGGING_KEY = "Java Logging Rules";
    public static final Metric ISSUEGROUP_JAVA_LOGGING = new Builder(
        ISSUEGROUP_JAVA_LOGGING_KEY, "Java Logging", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_JUNIT_KEY = "JUnit Rules";
    public static final Metric ISSUEGROUP_JUNIT = new Builder(
        ISSUEGROUP_JUNIT_KEY, "JUnit", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_OPTIMIZATION_KEY = "Optimization Rules";
    public static final Metric ISSUEGROUP_OPTIMIZATION = new Builder(
        ISSUEGROUP_OPTIMIZATION_KEY, "Optimization", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_SECURITY_CODE_GUIDELINE_KEY = "Security Code Guideline Rules";
    public static final Metric ISSUEGROUP_SECURITY_CODE_GUIDELINE = new Builder(
        ISSUEGROUP_SECURITY_CODE_GUIDELINE_KEY, "Security Code Guideline", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_STRICT_EXCEPTION_KEY = "Strict Exception Rules";
    public static final Metric ISSUEGROUP_STRICT_EXCEPTION = new Builder(
        ISSUEGROUP_STRICT_EXCEPTION_KEY, "Strict Exception", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_STRING_AND_STRINGBUFFER_KEY = "String and StringBuffer Rules";
    public static final Metric ISSUEGROUP_STRING_AND_STRINGBUFFER = new Builder(
        ISSUEGROUP_STRING_AND_STRINGBUFFER_KEY, "String and StringBuffer", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_TYPE_RESOLUTION_KEY = "Type Resolution Rules";
    public static final Metric ISSUEGROUP_TYPE_RESOLUTION = new Builder(
        ISSUEGROUP_TYPE_RESOLUTION_KEY, "Type Resolution", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    /* Rulesets metrics */

    /* End of Rulesets metrics */


    // getMetrics() method is defined in the Metrics interface and is used by
    // Sonar to retrieve the list of new metrics
    @Override
    public List<Metric> getMetrics() {
        return Arrays.asList(LICENSE, BEGIN_LINE, END_LINE, DPF, SM_RESOURCE,
                             // Size metrics
                             LOC, TLOC, LLOC, TLLOC, TNDI, TNFI, NPKG, TNPKG, NCL, TNCL, TNPCL,
                             NIN, TNIN, TNPIN, NEN, TNEN, TNPEN, NM, TNM, NPM, TNPM, NLM, TNLM,
                             NS, TNS, NG, TNG, NA, TNA, NPA, TNPA, NLA, TNLA, NOS, TNOS, NUMPAR,
                             NLG, NLPA, NLPM, NLS, TNLG, TNLPA, TNLPM,
                             TNLS,
                             // Documentation
                             DLOC, CLOC, TCLOC, CD, TCD, AD, TAD, PDA, TPDA, PUA, TPUA,
                             // Complexity
                             McCC, NL, NLE,
                             WMC,
                             // Coupling
                             CBO, CBOI, RFC, NOI,
                             NII,
                             // Inheritance
                             NOP, NOC, NOA,
                             NOD,
                             DIT,
                             // Cohesion
                             LCOM5,
                             // Clone metrics
                             CLLOC, CI, CCL, CCO, CC, CLC, CLLC, LDC, LLDC, NCR, CE, CV,
                             CA, /* , CR, CEE, CEG */

                             // Rule priorities
                             WARNINGP0,
                             WARNINGP1,
                             WARNINGP2,
                             WARNINGP3,
                             WARNINGP4,

                             // Rulesets metircs
                             ISSUEGROUP_BASIC, ISSUEGROUP_BRACE, ISSUEGROUP_CLONE_IMPLEMENTATION,
                             ISSUEGROUP_CONTROVERSIAL, ISSUEGROUP_DESIGN, ISSUEGROUP_EMPTY_CODE, ISSUEGROUP_EXCEPTION,
                             ISSUEGROUP_FINALIZER, ISSUEGROUP_IMPORT_STATEMENT, ISSUEGROUP_J2EE,
                             ISSUEGROUP_JAKARTA_COMMONS_LOGGING, ISSUEGROUP_JAVA_LOGGING, ISSUEGROUP_JAVABEAN,
                             ISSUEGROUP_JUNIT, ISSUEGROUP_NAMING, ISSUEGROUP_OPTIMIZATION,
                             ISSUEGROUP_SECURITY_CODE_GUIDELINE, ISSUEGROUP_STRICT_EXCEPTION,
                             ISSUEGROUP_STRING_AND_STRINGBUFFER, ISSUEGROUP_TYPE_RESOLUTION,
                             ISSUEGROUP_UNNECESSARY_AND_UNUSED_CODE
        );
    }

    /**
     * Get metrics for class threshold violations.
     *
     * @return class threshold metrics
     */
    public static List<Metric> getClassThresholdMetrics() {
        return Arrays.asList(LOC, LLOC, NA, NG, NLA, NLG, NLM, NLPA, NLPM, NLS,
                             NM, NPA, NPM, NS, NOS, TNA, TNG, TNLA, TNLG, TNLPA, TNLPM,
                             TNLS, TNLM, TNM, TNPA, TNPM, TNS, TLOC, TLLOC, TNOS, PUA, AD,
                             CD, CLOC, DLOC, TCLOC, TCD, NL, NLE, WMC, CBOI, NII, NOI, RFC,
                             LCOM5, CCL, CCO, CC, CI, CLC, CLLC, LDC, LLDC, WARNINGP0,
                             WARNINGP1, WARNINGP2, WARNINGP3, DIT, NOA, NOC, NOD, NOP);
    }

    /**
     * Get metrics for method threshold violations.
     *
     * @return method threshold metrics
     */
    public static List<Metric> getMethodThresholdMetrics() {
        return Arrays.asList(LOC, LLOC, NUMPAR, NOS, TLOC, TLLOC, TNOS, CD,
                             CLOC, DLOC, TCLOC, TCD, McCC, NL, NLE, NII, NOI, CCL, CCO, CC,
                             CI, CLC, CLLC, LDC, LLDC, WARNINGP0, WARNINGP1, WARNINGP2,
                             WARNINGP3);
    }

    /**
     * Get metrics for function threshold violations. By default these metrics
     * are the same as the method threshold violation metrics.
     *
     * @return function threshold metrics
     */
    public static List<Metric> getFunctionThresholdMetrics() {
        return getMethodThresholdMetrics();
    }

    /**
     * Get metrics for CloneClass threshold violations.
     *
     * @return CloneClass threshold metrics
     */
    public static List<Metric> getCloneClassThresholdMetrics() {
        return Arrays.asList(CA, CCO, CE, CI, CLLOC, CV, NCR);
    }

    /**
     * Get metrics for CloneInstance threshold violations.
     *
     * @return CloneInstance threshold metrics
     */
    public static List<Metric> getCloneInstanceThresholdMetrics() {
        return Arrays.asList(CA, CCO, CE, CLLOC, CV);
    }
}
