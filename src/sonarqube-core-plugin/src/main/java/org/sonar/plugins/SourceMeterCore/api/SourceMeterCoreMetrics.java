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

package org.sonar.plugins.SourceMeterCore.api;

import java.util.Arrays;
import java.util.List;

import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.Builder;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.measures.Metrics;

/**
 * Class containing SourceMeter core metrics definitions
 */
public class SourceMeterCoreMetrics implements Metrics {

    public static final String COLUMBUS_DOMAIN = "SourceMeter";

    /**
     * Appeared classes list for files are stored in this metric in the given format:
     * [
     *   {
     *     "id" : [resource_id],
     *     "name" : [class_name],
     *     "line" : [line],
     *     "endLine" : [endline]
     *   },
     *   ...
     *   {
     *   ...
     *   }
     * ]
     */
    public static final String CLASSES_IN_FILE_KEY = "SM:ClassesInFile";
    public static final Metric CLASSES_IN_FILE = new Builder(CLASSES_IN_FILE_KEY, "Classes in file", ValueType.DATA)
            .setHidden(true)
            .create();

    /**
     * Appeared methods list for files are stored in this metric in the given format: see {@link #CLASSES_IN_FILE_KEY}
     */
    public static final String METHODS_IN_FILE_KEY = "SM:MethodsInFile";
    public static final Metric METHODS_IN_FILE = new Builder(METHODS_IN_FILE_KEY, "Methods in file", ValueType.DATA)
            .setHidden(true)
            .create();

    /**
     * Files where a resource appears are stored in this metric for the resource
     * in the given format: see {@link #CLASSES_IN_FILE_KEY}
     */
    public static final String FILE_PATHS_KEY = "SM:FilePaths";
    public static final Metric FILE_PATHS = new Builder(FILE_PATHS_KEY, "File paths", ValueType.DATA)
            .setHidden(true)
            .create();

    public static final String SM_RESOURCE_KEY = "SM:resource";
    public static final Metric SM_RESOURCE = new Builder(
            SM_RESOURCE_KEY, "SourceMeter data is uploaded for the resource.",
            ValueType.BOOL)
            .setHidden(true)
            .create();

    public static final String DPF_KEY = "SM:duplicated_files";
    public static final Metric DPF = new Builder(DPF_KEY, "Duplicated Files", ValueType.INT)
            .setHidden(true)
            .create();

    /* Size Metrics */
    public static final String LOC_KEY = "LOC";
    public static final Metric LOC = new Builder(LOC_KEY, "Lines of Code", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN)
            .create();

    public static final String LLOC_KEY = "LLOC";
    public static final Metric LLOC = new Builder(LLOC_KEY, "Logical Lines of Code", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN)
            .create();

    public static final String NOS_KEY = "NOS";
    public static final Metric NOS = new Builder(NOS_KEY, "Number of Statements", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN)
            .create();

    /* END of Size Metrics */

    /* Documentation Metrics */
    public static final String PUA_KEY = "PUA";
    public static final Metric PUA = new Builder(PUA_KEY, "Public Undocumented API", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN)
            .create();

    public static final String PDA_KEY = "PDA";
    public static final Metric PDA = new Builder(PDA_KEY, "Public Documented API", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN)
            .create();

    public static final String CLOC_KEY = "CLOC";
    public static final Metric CLOC = new Builder(CLOC_KEY, "Comment Lines of Code", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN)
            .create();
    /* END of Documentation Metrics */

    /* Complexity Metrics */
    public static final String McCC_KEY = "McCC";
    public static final Metric McCC = new Builder(McCC_KEY, "McCabe's Cyclomatic Complexity", ValueType.INT)
            .setDomain(COLUMBUS_DOMAIN)
            .create();
    /* END of Complexity Metrics */

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Metric> getMetrics() {
        return Arrays.asList(DPF, SM_RESOURCE,
                CLASSES_IN_FILE, METHODS_IN_FILE, FILE_PATHS,
                // Size metrics
                LOC, LLOC, NOS,
                // Documentation
                PDA, PUA, CLOC,
                // Complexity
                McCC
        );
    }
}
