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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sonar.api.batch.Initializer;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;

/**
 * Class for supporting SourceMeter initializers. Stores all the detected
 * languages and the SourceMeter specific languages.
 */
public class SourceMeterCoreInitializer extends Initializer {

    /**
     * Stores all the detected languages. This way, the FileSystem has to be
     * indexed only once before the sensor phase to decide whether an SM
     * initializer is needed to run.
     */
    public static final Set<String> allDetectedLanguages = new HashSet<>();

    /**
     * Stores only the SM specific languages. Every plugin's initializer stores
     * it's language in this Set. This way, each plug-in can detect if it is the
     * last running SM plug-in or not.
     */
    public static final Set<String> SM_SPECIFIC_LANGUAGES = new HashSet<>();

    /**
     * In multi-language or module analyzations, plugins can store the project
     * level SM metrics for each language in this list. It is needed to be in
     * the api package, this way plugins can access the same Map.
     */
    public static final List<Set<Measure>> MEASURES_FOR_LANGUAGES = new ArrayList<>();

    private final FileSystem fileSystem;

    public SourceMeterCoreInitializer(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    public void execute(Project project) {
        updateDetectedLanguages(fileSystem);
    }

    /**
     * Determines whether the given language is the last SM language in the
     * analysis.
     * 
     * @param languageKey
     * @return true, if the actual language is the last SM language. False
     *         otherwise.
     */
    public static boolean isLastPlugin(String languageKey) {
        SM_SPECIFIC_LANGUAGES.remove(languageKey);
        return SM_SPECIFIC_LANGUAGES.isEmpty();
    }

    /**
     * Detects the languages in the fileSystem if they are not already detected.
     *
     * @param fileSystem
     */
    public static void updateDetectedLanguages(FileSystem fileSystem) {
        if (allDetectedLanguages.isEmpty()) {
            allDetectedLanguages.addAll(fileSystem.languages());
        }
    }
}
