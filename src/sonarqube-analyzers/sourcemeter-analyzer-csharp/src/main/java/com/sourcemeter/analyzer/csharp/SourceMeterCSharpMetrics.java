/**
 * Copyright (c) 2014-2017, FrontEndART Software Ltd.
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

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.Builder;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreMetrics;

/**
 * Class containing SourceMeter metrics definitions
 */
public final class SourceMeterCSharpMetrics extends SourceMeterCoreMetrics {

    public static final String CSHARP_LICENSE_KEY = "SM:csharp_license";
    public static final Metric CSHARP_LICENSE = new Builder(
            CSHARP_LICENSE_KEY, "C# license",
            ValueType.STRING)
            .setHidden(true)
            .create();

    public static final String SM_CSHARP_LOGICAL_LEVEL1_KEY = "SM_CSHARP_LOGICAL_LEVEL1";
    public static final Metric SM_CSHARP_LOGICAL_LEVEL1 = new Builder(SM_CSHARP_LOGICAL_LEVEL1_KEY, "SourceMeter level1 Logical Tree for C# language", ValueType.DATA)
            .setDomain(COLUMBUS_DOMAIN)
            .setDescription("Stores SM Logical Tree for C# in JSON format")
            .setHidden(true)
            .create();

    public static final String SM_CSHARP_LOGICAL_LEVEL2_KEY = "SM_CSHARP_LOGICAL_LEVEL2";
    public static final Metric SM_CSHARP_LOGICAL_LEVEL2 = new Builder(SM_CSHARP_LOGICAL_LEVEL2_KEY, "SourceMeter level2 Logical Tree for C# language", ValueType.DATA)
            .setDomain(COLUMBUS_DOMAIN)
            .setDescription("Stores SM Logical Tree for C# in JSON format")
            .setHidden(true)
            .create();

    public static final String SM_CSHARP_LOGICAL_LEVEL3_KEY = "SM_CSHARP_LOGICAL_LEVEL3";
    public static final Metric SM_CSHARP_LOGICAL_LEVEL3 = new Builder(SM_CSHARP_LOGICAL_LEVEL3_KEY, "SourceMeter level3 Logical Tree for C# language", ValueType.DATA)
            .setDomain(COLUMBUS_DOMAIN)
            .setDescription("Stores SM Logical Tree for C# in JSON format")
            .setHidden(true)
            .create();

    public static final String SM_CSHARP_CLONE_TREE_KEY = "SM_CSHARP_CLONE_TREE";
    public static final Metric SM_CSHARP_CLONE_TREE = new Builder(SM_CSHARP_CLONE_TREE_KEY, "SourceMeter Clone Tree for CSharp language", ValueType.DATA)
            .setDomain(COLUMBUS_DOMAIN)
            .setDescription("Stores SM Clone Tree for CSharp in JSON format")
            .setHidden(true)
            .create();

    /* getMetrics() method is defined in the ClassMetrics interface and
     * it is used by SonarQube to retrieve the list of new ClassMetrics */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Metric> getMetrics() {
        return Arrays.asList(CSHARP_LICENSE, SM_CSHARP_CLONE_TREE,
                SM_CSHARP_LOGICAL_LEVEL1, SM_CSHARP_LOGICAL_LEVEL2, SM_CSHARP_LOGICAL_LEVEL3);
    }
}
