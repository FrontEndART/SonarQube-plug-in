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

package com.sourcemeter.analyzer.base.helper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Metric;

import com.sourcemeter.analyzer.base.batch.SourceMeterInitializer;

/**
 * File helper methods for plugin.
 *
 */
public class FileHelper {

    /**
     * Returns the newest results directory for the actual language.
     *
     * @param settings Sonar settings.
     * @param dateSeparator Separator character between date and time in results directory.
     * @return results Directory's relative path.
     */
    public static String getSMSourcePath(Settings settings, FileSystem fileSystem, final char dateSeparator) throws IOException {
        String pluginLanguage = SourceMeterInitializer.getPluginLanguage().getKey().toLowerCase(Locale.ENGLISH);
        StringBuilder buffer = new StringBuilder("");
        String projectName = settings.getString("sonar.projectKey");
        projectName = StringUtils.replace(projectName, ":", "_");
        String resultsDir = settings.getString("sm.resultsdir")
                + File.separator + projectName + File.separator;

        if ("cs".equals(pluginLanguage)) {
            pluginLanguage = "csharp";
        } else if ("py".equals(pluginLanguage)) {
            pluginLanguage = "python";
        }

        resultsDir += pluginLanguage;

        File file = new File(resultsDir);
        if (!file.exists()) {
            resultsDir = buffer
                    .append(fileSystem.baseDir().getAbsolutePath())
                    .append(File.separator)
                    .append(resultsDir)
                    .toString();
            file = new File(resultsDir);
            if (!file.exists()) {
                throw new IOException("Could not load results directory: " + resultsDir);
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

    /**
     * Saving the graph in a database, for a given metrics.
     *
     * @param sensorContext Context of the sensor.
     * @param data In this case the graph in JSON format.
     * @param metric Stores the data.
     */
    public static void saveGraphToDataBase(SensorContext sensorContext, String data, Metric metric) {
        sensorContext.newMeasure().forMetric(metric).withValue(data).on(sensorContext.module()).save();
    }
}
