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

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.Builder;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.measures.SumChildValuesFormula;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreMetrics;

/**
 * Class containing SourceMeter metrics definitions
 */
public final class SourceMeterRPGMetrics extends SourceMeterCoreMetrics {

    public static final String PC_KEY = "PC";
    public static final Metric PC = new Builder(PC_KEY, "Program Complexity", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_COMPLEXITY)
            .create();

    public static final String NF_KEY = "NF";
    public static final Metric NF = new Builder(NF_KEY, "Number of Files", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_COUPLING)
            .create();

    public static final String NIR_KEY = "NIR";
    public static final Metric NIR = new Builder(NIR_KEY, "Number of Input Records", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_COUPLING)
            .create();

    public static final String NOR_KEY = "NOR";
    public static final Metric NOR = new Builder(NOR_KEY, "Number of Output Records", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_COUPLING)
            .create();

    public static final String TNF_KEY = "TNF";
    public static final Metric TNF = new Builder(TNF_KEY, "Total Number of Files", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_COUPLING)
            .create();

    public static final String TNOI_KEY = "TNOI";
    public static final Metric TNOI = new Builder(TNOI_KEY, "Total Number of Outgoing Invocations", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_COUPLING)
            .create();

    public static final String TDLOC_KEY = "TDLOC";
    public static final Metric TDLOC = new Builder(TDLOC_KEY, "Total Documentation Lines of Code", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_DOCUMENTATION)
            .create();

    public static final String NDS_KEY = "NDS";
    public static final Metric NDS = new Builder(NDS_KEY, "Number of Data Structures", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NNC_KEY = "NNC";
    public static final Metric NNC = new Builder(NNC_KEY, "Number of Named Constants", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String NSF_KEY = "NSF";
    public static final Metric NSF = new Builder(NSF_KEY, "Number of Standalone Fields", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNDS_KEY = "TNDS";
    public static final Metric TNDS = new Builder(TNDS_KEY, "Total Number of Data Structures", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNNC_KEY = "TNNC";
    public static final Metric TNNC = new Builder(TNNC_KEY, "Total Number of Named Constants", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNPC_KEY = "TNPC";
    public static final Metric TNPC = new Builder(TNPC_KEY, "Total Number of Procedures", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNSF_KEY = "TNSF";
    public static final Metric TNSF = new Builder(TNSF_KEY, "Total Number of Standalone Fields", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNSR_KEY = "TNSR";
    public static final Metric TNSR = new Builder(TNSR_KEY, "Total Number of Subroutines", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String TNPG_KEY = "TNPG";
    public static final Metric TNPG = new Builder(TNPG_KEY, "Total Number of Programs", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN_SIZE)
            .create();

    public static final String RPG_LICENSE_KEY = "SM:rpg_license";
    public static final Metric RPG_LICENSE = new Builder(
            RPG_LICENSE_KEY, "RPG license",
            ValueType.STRING)
            .setHidden(true)
            .create();

    /* Rulesets metrics */
    public static final String ISSUEGROUP_DOCUMENTATION_KEY = "Documentation Rules";
    public static final Metric ISSUEGROUP_DOCUMENTATION = new Builder(
        ISSUEGROUP_DOCUMENTATION_KEY, "Documentation", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_SECURITY_KEY = "Security Rules";
    public static final Metric ISSUEGROUP_SECURITY = new Builder(
        ISSUEGROUP_SECURITY_KEY, "Security", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_SIZE_KEY = "Size Rules";
    public static final Metric ISSUEGROUP_SIZE = new Builder(
        ISSUEGROUP_SIZE_KEY, "Size", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();

    public static final String ISSUEGROUP_TYPE_KEY = "Type Rules";
    public static final Metric ISSUEGROUP_TYPE = new Builder(
        ISSUEGROUP_TYPE_KEY, "Type", ValueType.INT)
        .setDomain(COLUMBUS_DOMAIN_ISSUEGROUP)
        .setFormula(new SumChildValuesFormula(false))
        .create();


    /* End of Rulesets metrics */

    // getMetrics() method is defined in the Metrics interface and is used by
    // Sonar to retrieve the list of new metrics
    @Override
    public List<Metric> getMetrics() {
        return Arrays.asList(PC, NF, NIR, NOR, TNF, TNOI, TDLOC, NDS, NNC, NSF,
                TNDS, TNNC, TNPC, TNSF, TNSR, TNPG, RPG_LICENSE,

                // Rulesets metrics
                ISSUEGROUP_DOCUMENTATION, ISSUEGROUP_SECURITY, ISSUEGROUP_SIZE,
                ISSUEGROUP_TYPE);
    }

    /**
     * Get metrics for Program threshold violations.
     *
     * @return Program threshold metrics
     */
    public static List<Metric> getProgramThresholdMetrics() {
        return Arrays.asList(NUMPAR, LOC, LLOC, TNSR, TNPC, TLOC, TLLOC, TNOS,
                CD, TCD, NL, NLE, PC, NOI, CCL, CCO, CC, CI, CLC, CLLC, LDC,
                LLDC, WARNINGP1, WARNINGP2, WARNINGP3);
    }

    /**
     * Get metrics for Procedure threshold violations.
     *
     * @return Procedure threshold metrics
     */
    public static List<Metric> getProcedureThresholdMetrics() {
        return Arrays.asList(LOC, LLOC, NUMPAR, NOS, TLOC, TLLOC, TNOS, CD,
                TCD, McCC, NL, NLE, NOI, CCL, CCO, CC, CI, CLC, CLLC, LDC,
                LLDC, WARNINGP1, WARNINGP2, WARNINGP3
            );
    }

    /**
     * Get metrics for Subroutine threshold violations.
     *
     * @return Subroutine threshold metrics
     */
    public static List<Metric> getSubroutineThresholdMetrics() {
        return Arrays.asList(LOC, LLOC, NOS, CD, McCC, NL, NLE, NOI, CCL, CCO,
                CC, CI, CLC, CLLC, LDC, LLDC, WARNINGP1, WARNINGP2, WARNINGP3);
    }
}
