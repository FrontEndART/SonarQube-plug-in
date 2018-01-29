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

package com.sourcemeter.analyzer.base.batch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputModule;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Metric;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.scan.filesystem.FileExclusions;

import com.google.gson.Gson;
import com.sourcemeter.analyzer.base.core.LicenseInformation;

import graphlib.Graph;
import graphlib.GraphlibException;

public abstract class SourceMeterSensor implements Sensor {

    protected static final Logger LOG = LoggerFactory.getLogger(SourceMeterSensor.class);
    protected static final String MS = " ms";

    protected String resultGraph = "";
    protected String projectName = "";
    protected boolean isIncrementalMode;

    protected final FileExclusions fileExclusions;
    protected final FileSystem fileSystem;
    protected final ProjectDefinition projectDefinition;
    protected final RulesProfile profile;
    protected final Settings settings;

    /**
     * Constructor: Use of IoC to get Settings.
     */
    public SourceMeterSensor(FileExclusions fileExclusions, FileSystem fileSystem,
            ProjectDefinition projectDefinition, RulesProfile profile,
            Settings settings) {

        this.fileSystem = fileSystem;
        this.fileExclusions = fileExclusions;
        this.projectDefinition = projectDefinition;
        this.profile = profile;
        this.settings = settings;
    }

    /**
     * Load result graph binary.
     *
     * @param filename Name of the result graph file.
     * @param module Module.
     * @param sensorContext Context of the sonarQube.
     * @throws GraphlibException
     */
    protected abstract void loadDataFromGraphBin(String filename,
            InputModule module, SensorContext sensorContext) throws GraphlibException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void execute(SensorContext sensorContext);

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * Run SourceMeter toolchain.
     *
     * @throws IOException
     */
    protected void runSourceMeter(List<String> commands) throws RuntimeException {
        LOG.debug("    Calling SourceMeter toolchain:");
        StringBuffer commandLineOptions = new StringBuffer();
        for (String command : commands) {
            commandLineOptions.append(command)
                              .append(" ");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("        " + commandLineOptions.toString());
        }
        ProcessBuilder sourceMeterProcessBuilder = new ProcessBuilder(commands);
        sourceMeterProcessBuilder.directory(this.fileSystem.baseDir());

        Process sourceMeterProcess = null;
        BufferedReader br = null;
        try {
            sourceMeterProcess = sourceMeterProcessBuilder.start();

            br = new BufferedReader(new InputStreamReader(
                    sourceMeterProcess.getInputStream(),
                    Charset.defaultCharset()));
            StringBuilder smLogContent = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                line = br.readLine();
                smLogContent.append(line);
                smLogContent.append(System.getProperty("line.separator"));
            }
            sourceMeterProcess.waitFor();

            String logFilePath = this.fileSystem.workDir().getAbsolutePath()
                    + File.separator + "sourcemeter.log";
            File smLogFile = new File(logFilePath);
            if (!smLogFile.exists() && !smLogFile.createNewFile()) {
                throw new RuntimeException("SourceMeter log file could not be created: " + logFilePath);
            }

            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(smLogFile.getAbsolutePath()),
                    Charset.defaultCharset()))) {
                bw.write(smLogContent.toString());
            }

            if (sourceMeterProcess.exitValue() != 0) {
                LOG.error("SourceMeter toolchain could not be executed properly. Please check the log file for more information: "
                        + smLogFile.getAbsolutePath());
                throw new RuntimeException();
            }
        } catch (IOException | InterruptedException e) {
            LOG.error("An exception occured while trying to run SourceMeter toolchain. Perhaps the permissions were not set correctly. Please check the trace for more information.", e);
            throw new RuntimeException(e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    LOG.error("Could not close opened resource!", e);
                }
            }
        }
    }

    /**
     * Saves sourceMeter's licence in a target metric in JSON format.
     *
     * @param graph Result graph.
     * @param sensorContext Context of the sonarQube.
     * @param headerLicenseInformations Informations from sourceMeter's result graph's header file.
     * @param targetMetric Information will be saved in this metric.
     */
    protected void saveLicense(Graph graph,
                               SensorContext sensorContext,
                               Map<String, String> headerLicenseInformations,
                               Metric targetMetric) {
        Gson gson = new Gson();

        LicenseInformation licenseInformation = new LicenseInformation();
        for (Map.Entry<String, String> entry : headerLicenseInformations.entrySet()) {
            String modeInfo = entry.getKey() + "-mode";
            String value = graph.getHeaderInfo(modeInfo);

            if (value == null) {
                continue;
            }

            licenseInformation.addTool(entry.getValue(), value);
        }

        sensorContext.newMeasure()
                .forMetric(targetMetric)
                .withValue(gson.toJson(licenseInformation).toString())
                .on(sensorContext.module())
                .save();
    }

    /**
     * Assemble filter file's content by exclusions.
     *
     * @return Filter file's content.
     * @throws IOException
     */
    protected String getFilterContent() throws IOException {
        StringBuffer filter = new StringBuffer("-.*\n");

        if (ArrayUtils.isNotEmpty(fileExclusions.sourceExclusions())
                || ArrayUtils.isNotEmpty(fileExclusions.sourceInclusions())
                || isIncrementalMode) {

            FilePredicate predicate = this.fileSystem.predicates().hasType(InputFile.Type.MAIN);
            Iterable<File> files = this.fileSystem.files(predicate);
            for (File file : files) {
                String filePath = file.getCanonicalPath();
                filter.append("+");
                filter.append(Pattern.quote(filePath));
                filter.append("\n");
            }
        } else {
            List<File> sourceDirectories = getSourcesDirectoriesForProject();
            for (File file : sourceDirectories) {
                String dirPath = file.getCanonicalPath();
                filter.append("+" + Pattern.quote(dirPath + File.separator) + "\n");
            }
        }

        return filter.toString();
    }

    /**
     * Writes general filter file's content to disk
     *
     * @param filter The filter file's content.
     * @param fileName The name of the filter.
     * @return Filter file's path.
     * @throws IOException
     */
    private String writeFilterToFile(String filter, String fileName) throws IOException {
        String workDir = this.fileSystem.workDir().getCanonicalPath();
        if (workDir.endsWith("\\.") || workDir.endsWith("/.")) {
            workDir = workDir.substring(0, workDir.length() - 2);
        }
        String filterFilePath = workDir + File.separator + fileName;
        File filterFile = new File(filterFilePath);
        if (!filterFile.exists() && !filterFile.createNewFile()) {
            throw new IOException("Filter file could not be created: "
                    + filterFilePath);
        }

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filterFile.getAbsolutePath()),
                Charset.defaultCharset()))) {
            bw.write(filter);
        }

        return filterFilePath;
    }

    /**
     * Writes soft filter file's content to disk
     *
     * @param filter The filter file's content.
     * @return Filter file's path.
     * @throws IOException
     */
    protected String writeSoftFilterToFile(String filter) throws IOException {
        return writeFilterToFile(filter, "softFilterFile");
    }

    /**
     * Writes hard filter file's content to disk.
     *
     * @param filter The filter file's content.
     * @return Filter file's path.
     * @throws IOException
     */
    protected String writeHardFilterToFile(String filter) throws IOException {
        return writeFilterToFile(filter, "hardFilterFile");
    }

    /**
     * List of source directories for project.
     *
     * @return Source directories list.
     */
    protected List<File> getSourcesDirectoriesForProject() {
        List<File> sourceDirectories = new ArrayList<File>();

        for (String sourcePath : this.projectDefinition.sources()) {
            File file = fileSystem.resolvePath(sourcePath);
            if (file.exists() && file.isDirectory()) {
                sourceDirectories.add(file);
            }
        }
        return sourceDirectories;
    }

}
