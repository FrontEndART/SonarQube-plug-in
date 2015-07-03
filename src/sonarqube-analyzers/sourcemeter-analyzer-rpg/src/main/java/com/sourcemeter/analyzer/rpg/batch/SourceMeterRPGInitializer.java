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
package com.sourcemeter.analyzer.rpg.batch;

import com.sourcemeter.analyzer.base.batch.MetricHunterCategory;
import com.sourcemeter.analyzer.base.batch.ProfileInitializer;
import com.sourcemeter.analyzer.base.batch.SourceMeterInitializer;
import com.sourcemeter.analyzer.base.helper.FileHelper;
import com.sourcemeter.analyzer.rpg.SourceMeterRPGMetrics;
import com.sourcemeter.analyzer.rpg.core.RPG;
import com.sourcemeter.analyzer.rpg.profile.SourceMeterRPGRuleRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.api.scan.filesystem.FileExclusions;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.api.utils.SonarException;

public class SourceMeterRPGInitializer extends SourceMeterInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(SourceMeterRPGInitializer.class);
    private static final String GENERATED_EXCLUSION = "**/generated/**";
    private static final int PROGRAM_NAME_COLUMN = 5;

    private String projectName;
    private String resultsDir;
    private String baseDir;

    /**
     * Command and parameters for running SourceMeter RPG analyzer
     */
    private final List<String> commands;

    public SourceMeterRPGInitializer(FileSystem fileSystem,
            Settings settings, ProjectDefinition projectDefinition,
            PathResolver pathResolver, RulesProfile profile) {
        super(fileSystem, settings, projectDefinition, pathResolver, profile);

        this.commands = new ArrayList<String>();
        SourceMeterInitializer.updatePluginLanguage(RPG.INSTANCE);
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        boolean skipRPG = this.settings.getBoolean("sm.rpg.skip");
        if (skipRPG) {
            RPG.removeSuffixesForCurrentAnalyze();
            return false;
        }

        return super.shouldExecuteOnProject(project);
    }

    @Override
    public void execute(Project project) {
        // add former generated folder to exclusions
        String[] exclusions = (String[]) ArrayUtils.add(
                this.settings.getStringArray(CoreProperties.PROJECT_EXCLUSIONS_PROPERTY),
                GENERATED_EXCLUSION);
        this.settings.setProperty(CoreProperties.PROJECT_EXCLUSIONS_PROPERTY, exclusions);

        this.settings.setProperty(CoreProperties.CPD_SKIP_PROPERTY, true); // Disable built in CPD plugin
        this.projectName = this.settings.getString("sonar.projectKey");
        this.projectName = StringUtils.replace(projectName, ":", "_");

        if (this.settings.getBoolean("sm.rpg.skipToolchain")) {
            LOG.info("    SourceMeter toolchain is skipped for RPG. Results will be uploaded from former results directory, if it exists.");
        } else {
            if (!checkProperties()) {
                throw new SonarException("Cannot run SourceMeterPlugin!");
            }
            LOG.info("    Running SourceMeter toolchain...");
            long startTime = System.currentTimeMillis();

            runSourceMeter(this.commands);

            LOG.info("    Running SourceMeter toolchain done: "
                    + (System.currentTimeMillis() - startTime) + " ms");

            checkFileSuffixes();
        }

        copyGeneratedSources();
    }

    /**
     * Method checks the generated Program.csv file after SM analyzation for file suffixes.
     */
    private void checkFileSuffixes() {
        String programsFilePath = FileHelper.getSMSourcePath(settings, fileSystem, '_');
        File file = new File(programsFilePath);

        // check for Program.csv file
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean accept = false;
                if (name.matches(".*-Program.csv")) {
                    accept = true;
                }
                return accept;
            }
        });
        Arrays.sort(directories);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(programsFilePath + File.separator + directories[directories.length - 1]),
                Charset.defaultCharset()))) {
            reader.readLine();
            String line;
            Set<String> foundSuffixes = new HashSet<String>();
            while ((line = reader.readLine()) != null) {
                String[] splittedLine = line.split(",");
                foundSuffixes.add(FilenameUtils.getExtension(splittedLine[PROGRAM_NAME_COLUMN]).replace("\"", ""));
            }

            RPG.updateSuffixes(foundSuffixes.toArray(new String[foundSuffixes.size()]));
        } catch (IOException e) {
            LOG.warn("Could not read Program.csv file from results folder! Default file suffixes will be used.");
        }
    }

    private boolean checkResultsDir() {
        this.resultsDir = this.settings.getString("sm.resultsdir");
        if (resultsDir == null) {
            LOG.error("Results directory must be set! Check it on the settings page of your SonarQube!");
            return false;
        }
        return true;
    }

    private void copyGeneratedSources() {
        if (!checkResultsDir()) {
            return;
        }

        String spoolSourceDirPath = FileHelper.getSMSourcePath(settings, fileSystem, '_')
                + File.separator + "sourcemeter" + File.separator + "source";

        File spoolSourceDir = new File(spoolSourceDirPath);

        if (spoolSourceDir.exists() && spoolSourceDir.list().length > 0) {
            String spoolDestDir = this.baseDir + File.separator + "generated";

            try {
                FileUtils.copyDirectoryStructure(new File(spoolSourceDirPath), new File(spoolDestDir));
            } catch (IOException e) {
                throw new SonarException(
                        "Could not copy generated spool source directory from "
                         + spoolSourceDirPath + " to " + spoolDestDir, e);
            }
        }

        // remove generated folder from exclusions
        List<String> exclusions = new LinkedList<String>(Arrays.asList(this.settings
                .getStringArray(CoreProperties.PROJECT_EXCLUSIONS_PROPERTY)));
        exclusions.remove(GENERATED_EXCLUSION);
        this.settings.setProperty(CoreProperties.PROJECT_EXCLUSIONS_PROPERTY,
                exclusions.toArray(new String[exclusions.size()]));
    }

    /**
     * Check for property settings
     *
     * @return true if every property set
     */
    private boolean checkProperties() {
        String pathToCA = this.settings.getString("sm.toolchaindir");
        if (pathToCA == null) {
            LOG.error("RPG SourceMeter path must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        if (!checkResultsDir()) {
            return false;
        }

        List<File> sourceDirectories = getSourcesDirectoriesForProject();

        if (sourceDirectories.isEmpty()) {
            LOG.error("No source directories found!");
            return false;
        }

        String cleanResults = this.settings.getString("sm.cleanresults");
        String spoolPattern = this.settings.getString("sm.rpg.spoolPattern");
        String rpg3Pattern = this.settings.getString("sm.rpg.rpg3Pattern");
        String rpg4Pattern = this.settings.getString("sm.rpg.rpg4Pattern");
        String additionalParameters = this.settings.getString("sm.rpg.toolchainOptions");

        baseDir = "";
        try {
            baseDir = sourceDirectories.get(0).getCanonicalPath();
        } catch (IOException e) {
            LOG.warn("Could not get base directory's canonical path. Absolute path is used.");
            baseDir = sourceDirectories.get(0).getAbsolutePath();
        }

        this.commands.add(pathToCA + File.separator
                + RPG.KEY.toUpperCase(Locale.ENGLISH)
                + File.separator + "SourceMeterRPG");
        this.commands.add("-projectBaseDir=" + baseDir);
        this.commands.add("-resultsDir=" + this.resultsDir);
        this.commands.add("-projectName=" + this.projectName);
        this.commands.add("-runChangeTracker=true");
        this.commands.add("-cleanResults=" + cleanResults);
        this.commands.add("-spoolFileNamePattern=" + spoolPattern);
        this.commands.add("-rpg3FileNamePattern=" + rpg3Pattern);
        this.commands.add("-rpg4FileNamePattern=" + rpg4Pattern);

        String cloneGenealogy = this.settings.getString("sm.cloneGenealogy");
        String cloneMinLines = this.settings.getString("sm.cloneMinLines");
        this.commands.add("-cloneGenealogy=" + cloneGenealogy);
        this.commands.add("-cloneMinLines=" + cloneMinLines);

        if (additionalParameters != null) {
            this.commands.add(additionalParameters);
        }

        String hardFilter = "";
        String hardFilterFilePath = null;

        try {
            hardFilter = getFilterContent();
            hardFilterFilePath = writeHardFilterToFile(hardFilter);
        } catch (IOException e) {
            LOG.warn(
                    "Cannot create hardFilter file for toolchain! No hardFilter is used during analyzis.",
                    e);
        }

        if (hardFilterFilePath != null) {
            this.commands.add("-externalHardFilter=" + hardFilterFilePath);
        }

        ProfileInitializer profileInitializer = new ProfileInitializer(
                this.settings, getMetricHunterCategories(), this.profile,
                new SourceMeterRPGRuleRepository(new XMLRuleParser()));

        String thresholdPath = this.fileSystem.workDir() + File.separator
                + "SM-Profile.xml";
        try {
            profileInitializer.generatePofileFile(thresholdPath);
            this.commands.add("-profileXML=" + thresholdPath);
        } catch (IOException e) {
            LOG.warn("An error occured while creating SourceMeter profile file. Default profile is used!!", e);
        }

        return true;
    }

    @Override
    protected List<MetricHunterCategory> getMetricHunterCategories() {
        List<MetricHunterCategory> categories = new ArrayList<MetricHunterCategory>();

        categories.add(new MetricHunterCategory("Program",
                SourceMeterRPGMetrics.getProgramThresholdMetrics()));

        categories.add(new MetricHunterCategory("Procedure",
                SourceMeterRPGMetrics.getProcedureThresholdMetrics()));

        categories.add(new MetricHunterCategory("Subroutine",
                SourceMeterRPGMetrics.getSubroutineThresholdMetrics()));

        categories.addAll(super.getMetricHunterCategories());

        return categories;
    }

    /**
     * Assemble filter file's content by exclusions.
     *
     * @return filter file's content
     * @throws IOException
     */
    @Override
    protected String getFilterContent() throws IOException {
        StringBuffer filter = new StringBuffer("-.*\n");
        FileExclusions fe = new FileExclusions(this.settings);
        if (ArrayUtils.isNotEmpty(fe.sourceExclusions()) || ArrayUtils.isNotEmpty(fe.sourceInclusions()) || isIncrementalMode) {
            FilePredicate predicate = this.fileSystem.predicates().hasType(InputFile.Type.MAIN);
            Iterable<File> files = this.fileSystem.files(predicate);
            if (!files.iterator().hasNext()) {
                appendBaseDirFilters(filter);
            }
            for (File file : files) {
                String filePath = file.getCanonicalPath();
                filter.append("+");
                filter.append(Pattern.quote(filePath));
                filter.append("\n");
            }
        } else {
            appendBaseDirFilters(filter);
        }

        return filter.toString();
    }

    private void appendBaseDirFilters(StringBuffer filter) throws IOException {
        List<File> sourceDirectories = getSourcesDirectoriesForProject();
        for (File file : sourceDirectories) {
            String dirPath = file.getCanonicalPath();
            filter.append("+" + Pattern.quote(dirPath + File.separator) + "\n");
        }

        // remove generated folder with SM filter
        filter.append("-");
        filter.append(Pattern.quote(this.baseDir + File.separator + "generated"));
        filter.append("\n");
    }
}
