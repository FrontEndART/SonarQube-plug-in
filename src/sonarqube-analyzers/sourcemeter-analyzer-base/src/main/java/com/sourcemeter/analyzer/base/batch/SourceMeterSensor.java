/**
 * Copyright (c) 2014-2020, FrontEndART Software Ltd.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicates;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.Metric;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.scanner.fs.InputProject;

import com.google.gson.Gson;
import com.sourcemeter.analyzer.base.core.LicenseInformation;
import com.sourcemeter.analyzer.base.helper.FileHelper;

import graphlib.Graph;
import graphlib.GraphlibException;
import org.sonar.api.utils.System2;

public abstract class SourceMeterSensor implements Sensor {

    protected static final Logger LOG = LoggerFactory.getLogger(SourceMeterSensor.class);
    protected static final String MS = " ms";

    protected String resultGraph = "";
    protected String projectName = "";

    protected final FileSystem fileSystem;
    protected final InputProject inputProject;
    protected final ActiveRules activeRules;
    protected final Configuration configuration;
    protected final System2 system;

    protected final List<String> commands;

    /**
     * Constructor: Use of IoC to get Settings.
     */
    public SourceMeterSensor(FileSystem fileSystem,
            InputProject inputProject, ActiveRules activeRules,
            Configuration configuration, System2 system) {

        this.fileSystem = fileSystem;
        this.inputProject = inputProject;
        this.activeRules = activeRules;
        this.configuration = configuration;
        this.system = system;

        this.commands = new ArrayList<String>();
    }

    /**
     * Load result graph binary.
     *
     * @param filename Name of the result graph file.
     * @param project Module.
     * @param sensorContext Context of the sonarQube.
     * @throws GraphlibException
     */
    protected abstract void loadDataFromGraphBin(String filename,
            InputProject project, SensorContext sensorContext) throws GraphlibException;

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
                .on(sensorContext.project())
                .save();
    }

    /**
     * Assemble filter file's content by exclusions.
     *
     * @param sensorContext Context of the sensor.
     * @param languageKey Key of the analyzed language.
     * @return Filter file's content.
     */
    protected String getFilterContent(SensorContext sensorContext, String languageKey) {
        StringBuffer filter = new StringBuffer("-.*\n");

        List<InputFile> sourceFilesForProject = getSourceFilesForProject(sensorContext, languageKey);
        for (InputFile file : sourceFilesForProject) {
            String path = file.uri().normalize().getPath();
            filter.append("+");
            if (system.isOsWindows()) {
                filter.append(path.substring(1).replace("/", "\\\\"));
            } else {
                filter.append(Pattern.quote(path));
            }
            filter.append("\n");
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
     * List of source files for project.
     *
     * @param sensorContext Context of the sensor.
     * @param languageKey Key of the analyzed language.
     * @return Source files list.
     */
    protected List<InputFile> getSourceFilesForProject(SensorContext sensorContext, String languageKey ) {
        List<InputFile> sourceFiles = new ArrayList<InputFile>();
        FileSystem fileSystem = sensorContext.fileSystem();

        FilePredicates predicates = fileSystem.predicates();
        Iterable<InputFile> inputFiles = fileSystem.inputFiles(
                predicates.and(
                        predicates.hasType(InputFile.Type.MAIN),
                        predicates.hasLanguages(languageKey)
                ));

        for (InputFile file : inputFiles) {
            sourceFiles.add(file);
        }
        return sourceFiles;
    }

    /**
     * Sets the command line options, common in all SourceMater analyzer languages.
     */
    protected void addCommonCommandlineOptions() {

        String cloneGenealogy = FileHelper.getStringFromConfiguration(this.configuration, "sm.cloneGenealogy");
        String cloneMinLines = FileHelper.getStringFromConfiguration(this.configuration, "sm.cloneMinLines");
        this.commands.add("-cloneGenealogy=" + cloneGenealogy);
        this.commands.add("-cloneMinLines=" + cloneMinLines);
    }

}
