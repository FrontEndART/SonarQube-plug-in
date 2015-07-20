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
package com.sourcemeter.analyzer.base.batch;

import graphlib.Graph;
import graphlib.GraphlibException;
import graphlib.VisitorException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Initializer;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.FileExclusions;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreInitializer;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreMetrics;

import com.sourcemeter.analyzer.base.core.AbstractSMLanguage;
import com.sourcemeter.analyzer.base.helper.FileHelper;
import com.sourcemeter.analyzer.base.helper.GraphHelper;
import com.sourcemeter.analyzer.base.helper.OSValidator;
import com.sourcemeter.analyzer.base.visitor.IncludedFilesVisitor;

public abstract class SourceMeterInitializer extends Initializer {

    private static AbstractSMLanguage pluginLanguage;
    protected boolean isIncrementalMode;
    protected boolean isDebugMode;

    protected final FileSystem fileSystem;
    protected final Settings settings;
    protected final ProjectDefinition projectDefinition;
    protected final PathResolver pathResolver;
    protected final RulesProfile profile;

    private static final Logger LOG = LoggerFactory
            .getLogger(SourceMeterInitializer.class);

    public SourceMeterInitializer(FileSystem fileSystem, Settings settings,
            ProjectDefinition projectDefinition, PathResolver pathResolver,
            RulesProfile profile) {
        this.fileSystem = fileSystem;
        this.settings = settings;
        this.projectDefinition = projectDefinition;
        this.pathResolver = pathResolver;
        this.profile = profile;
        this.isDebugMode = (System.getenv("COLUMBUS_SONAR_DEBUG") != null);
        this.isIncrementalMode = "incremental".equals(this.settings
                .getString("sonar.analysis.mode"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldExecuteOnProject(Project project) {
        // This needs to be set, whether or not the current initializer is
        // needed to run on the current language.
        SourceMeterCoreInitializer.updateDetectedLanguages(fileSystem);

        boolean shouldExecute = false;

        // On top level of a module analyzation, SM toolchain
        // does not need to run.
        if (!getSourcesDirectoriesForProject().isEmpty()) {
            if (pluginLanguage.getKey().equals(this.settings.getString("sonar.language"))) {
                shouldExecute = true;
            } else {
                shouldExecute = SourceMeterCoreInitializer.allDetectedLanguages
                        .contains(pluginLanguage.getKey());
            }

            // if the actual plugin is needed to be executed,
            // the actual language is an SM specific language.
            if (shouldExecute) {
                SourceMeterCoreInitializer.SM_SPECIFIC_LANGUAGES.add(pluginLanguage.getKey());
            }
        }

        return shouldExecute;
    }

    /**
     * Updates the current language of the plug-in to the given language.
     *
     * @param language
     */
    public static void updatePluginLanguage(AbstractSMLanguage language) {
        pluginLanguage = language;
    }

    /**
     * Creates file exclusions for not analyzed files (e.g.
     *
     * @throws IOException
     * @throws GraphlibException
     * @throws VisitorException
     */
    protected void createExcludesFromNotAnalyzedFiles(char dateSeparator)
            throws IOException, GraphlibException, VisitorException {
        String resultsDir = FileHelper.getSMSourcePath(settings, fileSystem, dateSeparator);
        String projectName = settings.getString("sonar.projectKey");
        projectName = StringUtils.replace(projectName, ":", "_");
        String resultGraph = resultsDir + File.separator + projectName + ".graph";

        Graph graph = new Graph();
        graph.loadBinary(resultGraph);

        IncludedFilesVisitor fileVisitor = new IncludedFilesVisitor(fileSystem);
        GraphHelper.processGraph(graph, "__PhysicalRoot__", "PhysicalTree", fileVisitor);

        Set<String> includedFiles = fileVisitor.getIncludedFiles();
        Set<String> excludedFiles = new HashSet<String>();

        FilePredicate predicate = this.fileSystem.predicates().hasType(InputFile.Type.MAIN);
        Iterable<File> files = this.fileSystem.files(predicate);
        String baseDir = this.fileSystem.baseDir().getCanonicalPath()
                .replace("\\", "/").toLowerCase(Locale.getDefault());
        for (File file : files) {
            String path = file.getAbsolutePath().replace("\\", "/");
            String lowerCasePath = path.toLowerCase(Locale.getDefault());
            if (pluginLanguage.isFileForCurrentLanguage(file) && !includedFiles.contains(lowerCasePath)) {
                String excludedPath = "**" + path.substring(baseDir.length(), path.length());
                excludedFiles.add(excludedPath);
            }
        }

        if (!excludedFiles.isEmpty()) {
            String[] exclusions = (String[]) ArrayUtils.addAll(
                    this.settings.getStringArray("sonar.exclusions"),
                    excludedFiles.toArray());
            this.settings.setProperty("sonar.exclusions", exclusions);
        }
    }

    /**
     * Run SourceMeter toolchain
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws SonarException
     */
    protected void runSourceMeter(List<String> commands) throws SonarException {
        LOG.debug("    Calling SourceMeter toolchain:");
        StringBuffer commandLineOptions = new StringBuffer();
        for (String command : commands) {
            commandLineOptions.append(command + " ");
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
            String line = null;
            while ((line = br.readLine()) != null) {
                smLogContent.append(line);
                smLogContent.append(System.getProperty("line.separator"));
            }
            sourceMeterProcess.waitFor();

            String logFilePath = this.fileSystem.workDir().getAbsolutePath()
                    + File.separator + "sourcemeter.log";
            File smLogFile = new File(logFilePath);
            if (!smLogFile.exists() && !smLogFile.createNewFile()) {
                throw new SonarException(
                    "SourceMeter log file could not be created: "
                    + logFilePath);
            }

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(smLogFile.getAbsolutePath()),
                    Charset.defaultCharset()));
            bw.write(smLogContent.toString());
            bw.close();

            if (sourceMeterProcess.exitValue() != 0) {
                LOG.error("SourceMeter toolchain could not be executed properly. Please check the log file for more information: "
                        + smLogFile.getAbsolutePath());
                throw new SonarException();
            }
        } catch (IOException | InterruptedException e) {
            LOG.error("An exception occured while trying to run SourceMeter toolchain. Perhaps the permissions were not set correctly. Please check the trace for more information.", e);
            throw new SonarException(e);
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
     * List of source directories for project.
     *
     * @return source directories list
     */
    protected List<File> getSourcesDirectoriesForProject() {
        List<File> sourceDirectories = new ArrayList<File>();

        for (String sourcePath : this.projectDefinition.getSourceDirs()) {
            File file = pathResolver.relativeFile(this.projectDefinition.getBaseDir(), sourcePath);
            if (file.exists() && file.isDirectory()) {
                sourceDirectories.add(file);
            }
        }

        return sourceDirectories;
    }

    private String writeFilterToFile(String filter, String fileName) throws IOException {
        String workDir = this.fileSystem.workDir().getCanonicalPath();
        if (workDir.endsWith("\\.") || workDir.endsWith("/.")) {
            workDir = workDir.substring(0, workDir.length() - 2);
        }
        String filterFilePath = workDir + File.separator + fileName;
        File filterFile = new File(filterFilePath);
        if (!filterFile.exists() && !filterFile.createNewFile()) {
            throw new SonarException("Filter file could not be created: "
                    + filterFilePath);
        }

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filterFile.getAbsolutePath()),
                Charset.defaultCharset()));
        bw.write(filter);
        bw.close();

        return filterFilePath;
    }

    /**
     * Writes hard filter file's content to disk
     *
     * @param filter
     *            the filter file's content
     * @return filter file's path
     * @throws IOException
     */
    protected String writeHardFilterToFile(String filter) throws IOException {
        return writeFilterToFile(filter, "hardFilterFile");
    }

    /**
     * Writes hard filter file's content to disk
     *
     * @param filter
     *            the filter file's content
     * @return filter file's path
     * @throws IOException
     */
    protected String writeSoftFilterToFile(String filter) throws IOException {
        return writeFilterToFile(filter, "softFilterFile");
    }

    /**
     * Assemble filter file's content by exclusions.
     *
     * @return filter file's content
     * @throws IOException
     */
    protected String getFilterContent() throws IOException {
        StringBuffer filter = new StringBuffer("-.*\n");
        FileExclusions fe = new FileExclusions(this.settings);
        if (ArrayUtils.isNotEmpty(fe.sourceExclusions()) || ArrayUtils.isNotEmpty(fe.sourceInclusions()) || isIncrementalMode) {
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
     * Assemble a filter file by only using the source exclusions.
     *
     * @return filter file's content
     */
    protected String getHardFilterForSourceExclusions() {
        StringBuffer filter = new StringBuffer();
        FileExclusions fe = new FileExclusions(this.settings);

        for (String exclusion : fe.sourceExclusions()) {
            filter.append("-").append(exclusion).append("\n");
        }

        return filter.toString();
    }

    /**
     * Removes "\." or "/." suffixes from file paths.
     *
     * @param path
     * @return corrected path
     */
    protected String getCorrectedFilePath(String path) {
        if (OSValidator.isWindows()) {
            path = path.substring(0, 2).toLowerCase(Locale.ENGLISH) + path.substring(2, path.length());
        }

        if (path.endsWith("\\.") || path.endsWith("/.")) {
            path = path.substring(0, path.length() - 2);
        }

        return path;
    }

    protected List<MetricHunterCategory> getMetricHunterCategories() {
        List<MetricHunterCategory> categories = new ArrayList<MetricHunterCategory>();

        categories.add(new MetricHunterCategory("CloneClass",
                SourceMeterCoreMetrics.getCloneClassThresholdMetrics()));

        categories.add(new MetricHunterCategory("CloneInstance",
                SourceMeterCoreMetrics.getCloneInstanceThresholdMetrics()));

        return categories;
    }

    public static AbstractSMLanguage getPluginLanguage() {
        return pluginLanguage;
    }
}
