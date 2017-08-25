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
package com.sourcemeter.analyzer.java.batch;

import graphlib.GraphlibException;
import graphlib.VisitorException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.CoreProperties;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rules.XMLRuleParser;
import org.sonar.api.scan.filesystem.PathResolver;
import org.sonar.api.utils.SonarException;

import com.sourcemeter.analyzer.base.batch.MetricHunterCategory;
import com.sourcemeter.analyzer.base.batch.ProfileInitializer;
import com.sourcemeter.analyzer.base.batch.SourceMeterInitializer;
import com.sourcemeter.analyzer.java.SourceMeterJavaMetrics;
import com.sourcemeter.analyzer.java.core.Java;
import com.sourcemeter.analyzer.java.profile.SourceMeterJavaRuleRepository;

public class SourceMeterJavaInitializer extends SourceMeterInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(SourceMeterJavaInitializer.class);

    /**
     * Command and parameters for running SourceMeter Java analyzer
     */
    private final List<String> commands;

    public SourceMeterJavaInitializer(FileSystem fileSystem, Settings settings,
            ProjectDefinition projectDefinition, PathResolver pathResolver,
            RulesProfile profile) {
        super(fileSystem, settings, projectDefinition, pathResolver, profile);
        this.commands = new ArrayList<String>();
        SourceMeterInitializer.updatePluginLanguage(Java.INSTANCE);
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        boolean skipJava = this.settings.getBoolean("sm.java.skip");
        if (skipJava) {
            Java.removeSuffixesForCurrentAnalyze();
            return false;
        }

        return super.shouldExecuteOnProject(project);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(Project project) {
        this.settings.setProperty(CoreProperties.CPD_SKIP_PROPERTY, true); // Disable built in CPD plugin

        if (this.settings.getBoolean("sm.java.skipToolchain")) {
            LOG.info("    SourceMeter toolchain is skipped for Java. Results will be uploaded from former results directory, if it exists.");
        } else {
            if (!checkProperties()) {
                throw new SonarException("Cannot run SourceMeterPlugin!");
            }
            LOG.info("    Running SourceMeter toolchain...");
            long startTime = System.currentTimeMillis();

            runSourceMeter(this.commands);

            LOG.info("    Running SourceMeter toolchain done: "
                    + (System.currentTimeMillis() - startTime) + " ms");
        }

        try {
            long startTime = System.currentTimeMillis();
            LOG.info("    Creating exclusions for analyzed files...");
            createExcludesFromNotAnalyzedFiles('-');
            LOG.info("    Creating exclusions done: "
                    + (System.currentTimeMillis() - startTime) + " ms");
        } catch (IOException | GraphlibException | VisitorException e) {
            LOG.warn("Could not create exlcusions from result graph! All cpp files are indexed by SonarQube automatically.", e);
        }
    }

    /**
     * Check for property settings
     *
     * @return true if every property set
     */
    protected boolean checkProperties() {
        String cleanResults = this.settings.getString("sm.cleanresults");
        if (cleanResults == null) {
            LOG.error("sonar.sourcemeter.cleanresults property cannot be null!");
            return false;
        }

        String pathToCA = this.settings.getString("sm.toolchaindir");
        if (pathToCA == null) {
            LOG.error("JAVA SourceMeter path must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String resultsDir = this.settings.getString("sm.resultsdir");
        if (resultsDir == null) {
            LOG.error("Results directory must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String buildScript = this.settings.getString("sm.java.buildscript");

        String projectName = this.settings.getString("sonar.projectKey");
        projectName = StringUtils.replace(projectName, ":", "_");

        String analyseMode = this.settings.getString("sonar.analysis.mode");
        String projectNameSuffix = "";
        if ("incremental".equals(analyseMode)) {
            LOG.warn("Incremental mode is on. There are no metric based (INFO level) issues in this mode.");
            projectNameSuffix = "-incremental";
        } else if ("preview".equals(analyseMode)) {
            projectNameSuffix = "-preview";
            cleanResults = "1";
            // Copy the <projectName>.gsi file to "<projectName>-preview".
            // Override if exists.
            try {
                File from = new File(resultsDir + File.separator + projectName
                        + File.separator + projectName
                        + ".gsi");
                File to = new File(resultsDir + File.separator + projectName
                        + projectNameSuffix + File.separator + projectName
                        + projectNameSuffix + ".gsi");
                if (from.exists()) {
                    FileUtils.copyFile(from, to);
                } else if (to.exists()) {
                    if (!to.delete()) {
                        LOG.error("Cannot delete " + to.getName() + "! " + this.toString() + " was cancelled.");
                        return false;
                    }
                }
            } catch (IOException e) {
                LOG.error("Cannot copy .gsi file! " + this.toString() + " was cancelled.");
                throw (SonarException) new SonarException(e.getMessage()).initCause(e);
            }
        }

        String filter = "";
        String softFilterFilePath = null;
        try {
            filter = getFilterContent();
            softFilterFilePath = writeSoftFilterToFile(filter.toString());
        } catch (IOException e) {
            LOG.warn("Cannot create filter file for toolchain! No filter is used during analyzis.", e);
        }

        String externalLibraries = this.settings.getString("sonar.java.libraries");
        String javacOptions = this.settings.getString("sm.java.javacOptions");
        String pmdOptions = this.settings.getString("sm.java.pmdOptions");
        String csvSeparator = this.settings.getString("sm.java.csvSeparator");
        String vhMaxDepth = this.settings.getString("sm.java.vhMaxDepth");
        String vhTimeout = this.settings.getString("sm.java.vhTimeOut");
        String maxMem = this.settings.getString("sm.java.maxMem");
        String runVul = this.settings.getString("sm.java.runVulnerabilityHunter");
        String runRTE = this.settings.getString("sm.java.runRTEHunter");
        String rhMaxState = this.settings.getString("sm.java.RHMaxState");
        String rhMaxDepth = this.settings.getString("sm.java.RHMaxDepth");
        String hardFilter = this.settings.getString("sm.java.hardFilter");
        String[] binaries = this.settings.getStringArray("sonar.java.binaries");
        String findBugsOptions = this.settings.getString("sm.java.fbOptions");
        String additionalParameters = this.settings.getString("sm.java.toolchainOptions");

        String fbFile = null;

        if (binaries != null && !ArrayUtils.isEmpty(binaries)) {
            try {
                fbFile = generateFindBugsFile(binaries);
            } catch (IOException e) {
                LOG.warn("Could not generate input file for FindBugs. Binaries are not used during the analyzis.", e);
            }
        }

        this.commands.add(pathToCA + File.separator + Java.NAME + File.separator + "SourceMeterJava");

        this.commands.add("-resultsDir=" + resultsDir);
        this.commands.add("-projectName=" + projectName + projectNameSuffix);

        ProfileInitializer profileInitializer = new ProfileInitializer(
                this.settings, getMetricHunterCategories(), this.profile,
                new SourceMeterJavaRuleRepository(new XMLRuleParser()));

        String profilePath = this.fileSystem.workDir() + File.separator
                + "SM-Profile.xml";
        try {
            profileInitializer.generatePofileFile(profilePath);
            this.commands.add("-profileXML=" + profilePath);
        } catch (IOException e) {
            LOG.warn("An error occured while creating SourceMeter profile file. Default profile is used!!", e);
        }

        String baseDir = "";
        try {
            baseDir = this.fileSystem.baseDir().getCanonicalPath();
        } catch (IOException e) {
            LOG.warn("Could not get base directory's canonical path. Absolute path is used.");
            baseDir = this.fileSystem.baseDir().getAbsolutePath();
        }

        if (buildScript != null) {
            this.commands.add("-buildScript=" + buildScript);
        }

        this.commands.add("-projectBaseDir=" + baseDir);

        if (softFilterFilePath != null) {
            this.commands.add("-externalSoftFilter=" + softFilterFilePath);
        }

        if (hardFilter != null) {
            this.commands.add("-externalHardFilter=" + hardFilter);
        }
        if (externalLibraries != null && !externalLibraries.isEmpty()) {
            externalLibraries = StringUtils.replace(externalLibraries, ",",
                    File.pathSeparator);
            if (javacOptions == null) {
                javacOptions = "";
            }
            this.commands.add("-javacOptions=" + javacOptions
                    + " -cp \"." + File.pathSeparator + externalLibraries + "\"");
        } else if (javacOptions != null) {
            this.commands.add("-javacOptions=" + javacOptions);
        } else {
            javacOptions = DependenciesCollector.getClassPath();
            if (javacOptions != null) {
                this.commands.add("-javacOptions=" + javacOptions);
            }
        }
        if (vhMaxDepth != null) {
            this.commands.add("-VHMaxDepth=" + vhMaxDepth);
        }
        if (maxMem != null) {
            this.commands.add("-JVMOptions=" + "-Xmx" + maxMem + "M");
        }
        if (vhTimeout != null) {
            this.commands.add("-VHTimeout=" + vhTimeout);
        }
        if (runVul != null) {
            this.commands.add("-runVLH=" + runVul);
        }
        if (runRTE != null) {
            this.commands.add("-runRTEHunter=" + runRTE);
        }
        if (rhMaxState != null) {
            this.commands.add("-RHMaxState=" + rhMaxState);
        }
        if (rhMaxDepth != null) {
            this.commands.add("-RHMaxDepth=" + rhMaxDepth);
        }
        if (pmdOptions != null) {
            this.commands.add("-pmdOptions=" + pmdOptions);
        }
        if (csvSeparator != null) {
            this.commands.add("-csvSeparator=" + csvSeparator);
        }

        String cloneGenealogy = this.settings.getString("sm.cloneGenealogy");
        String cloneMinLines = this.settings.getString("sm.cloneMinLines");
        this.commands.add("-cloneGenealogy=" + cloneGenealogy);
        this.commands.add("-cloneMinLines=" + cloneMinLines);

        this.commands.add("-cleanProject=true");
        this.commands.add("-cleanResults=" + cleanResults);

        if (this.isIncrementalMode) {
            this.commands.add("-runDCF=false");
            this.commands.add("-runMET=false");
        }

        if (fbFile != null) {
            this.commands.add("-FBFileList=" + fbFile);
            this.commands.add("-runFB=true");
        }

        this.commands.add("-runChangeTracker=true");

        if (findBugsOptions != null) {
            this.commands.add("-FBOptions=" + findBugsOptions);
        }

        if (additionalParameters != null) {
            this.commands.add(additionalParameters);
        }

        return true;
    }

    @Override
    protected List<MetricHunterCategory> getMetricHunterCategories() {
        List<MetricHunterCategory> categories = new ArrayList<MetricHunterCategory>();

        categories.add(new MetricHunterCategory("Class", SourceMeterJavaMetrics
                .getClassThresholdMetrics()));
        categories.add(new MetricHunterCategory("Interface", "class", SourceMeterJavaMetrics
                .getClassThresholdMetrics()));
        categories.add(new MetricHunterCategory("Enum", "class", SourceMeterJavaMetrics
                .getClassThresholdMetrics()));

        categories.add(new MetricHunterCategory("Method",
                SourceMeterJavaMetrics.getMethodThresholdMetrics()));

        categories.add(new MetricHunterCategory("CloneClass",
                SourceMeterJavaMetrics.getCloneClassThresholdMetrics()));

        categories.add(new MetricHunterCategory("CloneInstance",
                SourceMeterJavaMetrics.getCloneInstanceThresholdMetrics()));

        return categories;
    }

    private String generateFindBugsFile(String[] binaries) throws IOException {
        String findBugsFilePath = this.fileSystem.workDir().getAbsolutePath()
                + File.separator + "FBFile.txt";
        File findBugsFile = new File(findBugsFilePath);
        if (!findBugsFile.exists() && !findBugsFile.createNewFile()) {
            throw new SonarException("FindBugs file could not be created: "
                    + findBugsFilePath);
        }

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(findBugsFile.getAbsolutePath()),
                Charset.defaultCharset()));

        for (String binary : binaries) {
            bw.write(binary + "\n");
        }
        bw.close();

        return findBugsFile.getCanonicalPath();
    }

}
