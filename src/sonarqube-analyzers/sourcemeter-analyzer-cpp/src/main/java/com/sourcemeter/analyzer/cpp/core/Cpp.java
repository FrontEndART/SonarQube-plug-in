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
package com.sourcemeter.analyzer.cpp.core;

import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Settings;

import com.sourcemeter.analyzer.base.core.AbstractSMLanguage;
import com.sourcemeter.analyzer.cpp.SourceMeterAnalyzerCppPlugin;

public class Cpp extends AbstractSMLanguage {

    public static final Cpp INSTANCE = new Cpp();

    /**
     * Cpp key
     */
    public static final String KEY = "cpp";

    /**
     * Cpp name
     */
    public static final String NAME = "C++";

    public static final String DEFAULT_SOURCE_SUFFIXES = ".cxx,.cpp,.cc,.c";
    public static final String DEFAULT_HEADER_SUFFIXES = ".hxx,.hpp,.hh,.h";

    /**
     * C++ files suffixes
     */
    private static String[] fileSuffixes = { ".cxx", ".cpp", ".cc", ".c",
            ".hxx", ".hpp", ".hh", ".h" };

    /**
     * Default constructor
     */
    public Cpp() {
        super(KEY, NAME);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.sonar.api.resources.AbstractLanguage#getFileSuffixes()
     */
    @Override
    public String[] getFileSuffixes() {
        return Arrays.copyOf(fileSuffixes, fileSuffixes.length);
    }

    public static void removeSuffixesForCurrentAnalyze() {
        fileSuffixes = new String[1];
    }

    @Override
    public boolean isFileForCurrentLanguage(java.io.File file) {
        String suffix = "."
                + StringUtils.substringAfterLast(file.getName(), ".");
        return ArrayUtils.contains(fileSuffixes, suffix);
    }

    public static void updateSuffixes(Settings settings) {
        String[] headerSuffixes = createStringArray(settings.getStringArray(SourceMeterAnalyzerCppPlugin.HEADER_FILE_SUFFIXES_KEY), DEFAULT_HEADER_SUFFIXES);
        String[] sourceSuffixes = createStringArray(settings.getStringArray(SourceMeterAnalyzerCppPlugin.SOURCE_FILE_SUFFIXES_KEY), DEFAULT_SOURCE_SUFFIXES);
        fileSuffixes = mergeArrays(headerSuffixes, sourceSuffixes);
    }

    private static String[] createStringArray(String[] values, String defaultValues) {
        if (values == null || values.length == 0) {
            return StringUtils.split(defaultValues, ",");
        }
        return values;
    }

    private static String[] mergeArrays(String[] array1, String[] array2) {
        String[] result = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }
}
