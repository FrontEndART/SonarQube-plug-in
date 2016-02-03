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
package com.sourcemeter.analyzer.base.helper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.SonarException;

import com.sourcemeter.analyzer.base.batch.SourceMeterInitializer;

/**
 * File helper methods for plugin.
 *
 */
public class FileHelper {

    /**
     * Searches an indexed file for the given filePath in lowercase.
     *
     * @param fileSystem
     * @param sensorContext
     * @param project
     * @param filePath
     * @return the indexed resource
     */
    public static Resource getIndexedFileForFilePath(FileSystem fileSystem,
            SensorContext sensorContext, Project project, String filePath) {
        if (OSValidator.isWindows()) {
            FilePredicate filePredicate = fileSystem.predicates().hasLanguage(
                    SourceMeterInitializer.getPluginLanguage().getKey());
            Iterator<File> fileIterator = fileSystem.files(filePredicate).iterator();

            while (fileIterator.hasNext()) {
                File file = fileIterator.next();
                if (file.getAbsolutePath().toLowerCase(Locale.ENGLISH).replace("\\", "/")
                        .contains(filePath.toLowerCase(Locale.ENGLISH).replace("\\", "/"))) {
                    return org.sonar.api.resources.File.fromIOFile(file, project);
                }
            }
        } else {
            return org.sonar.api.resources.File.fromIOFile(new File(filePath), project);
        }

        return null;
    }

    /**
     * Returns the newest results directory for the actual language.
     *
     * @param settings Sonar settings
     * @param dateSeparator separator character between date and time in results directory
     * @return results directory's relative path
     */
    public static String getSMSourcePath(Settings settings, FileSystem fileSystem, final char dateSeparator) {
        String projectName = settings.getString("sonar.projectKey");
        projectName = StringUtils.replace(projectName, ":", "_");
        String resultsDir = settings.getString("sm.resultsdir")
                + File.separator + projectName + File.separator
                + SourceMeterInitializer.getPluginLanguage().getKey().toLowerCase(Locale.ENGLISH);

        File file = new File(resultsDir);
        if (!file.exists()) {
            resultsDir = fileSystem.baseDir().getAbsolutePath()
                    + File.separator + resultsDir;
            file = new File(resultsDir);
            if (!file.exists()) {
                throw new SonarException("Could not load results directory: " + resultsDir);
            }
        }
        String[] directories = file.list(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                boolean accept = false;

                File directory = new File(dir, name);
                if (directory.isDirectory() && name.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}" + dateSeparator + "[0-9]{2}-[0-9]{2}-[0-9]{2}")) {
                    accept = true;
                }
                return accept;
            }
        });
        Arrays.sort(directories);

        return resultsDir + File.separator + directories[directories.length - 1];
    }
}
