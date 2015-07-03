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
package com.sourcemeter.analyzer.cpp.batch;

import graphlib.GraphlibException;
import graphlib.VisitorException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
import com.sourcemeter.analyzer.cpp.SourceMeterCppMetrics;
import com.sourcemeter.analyzer.cpp.core.Cpp;
import com.sourcemeter.analyzer.cpp.profile.SourceMeterCppRuleRepository;

public class SourceMeterCppInitializer extends SourceMeterInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(SourceMeterCppInitializer.class);

    /**
     * Command and parameters for running SourceMeter C++ analyzer
     */
    private final List<String> commands;

    public SourceMeterCppInitializer(FileSystem fileSystem, Settings settings,
            ProjectDefinition projectDefinition, PathResolver pathResolver,
            RulesProfile profile) {
        super(fileSystem, settings, projectDefinition, pathResolver, profile);
        this.commands = new ArrayList<String>();
        SourceMeterInitializer.updatePluginLanguage(Cpp.INSTANCE);
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        boolean skipCpp = this.settings.getBoolean("sm.cpp.skip");
        if (skipCpp) {
            Cpp.removeSuffixesForCurrentAnalyze();
            return false;
        }

        Cpp.updateSuffixes(settings);
        return super.shouldExecuteOnProject(project);
    }

    @Override
    public void execute(Project project) {
        this.settings.setProperty(CoreProperties.CPD_SKIP_PROPERTY, true); // Disable built in CPD plugin

        if (this.settings.getBoolean("sm.cpp.skipToolchain")) {
            LOG.info("    SourceMeter toolchain is skipped for C++. Results will be uploaded from former results directory, if it exists.");
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
    private boolean checkProperties() {
        String pathToCA = this.settings.getString("sm.toolchaindir");
        if (pathToCA == null) {
            LOG.error("C++ SourceMeter path must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String pathToBuild = this.settings.getString("sm.cpp.buildfile");
        if (pathToBuild == null) {
            LOG.error("Build script path must be set! (sm.cpp.buildfile)");
            return false;
        }

        String resultsDir = this.settings.getString("sm.resultsdir");
        if (resultsDir == null) {
            LOG.error("Results directory must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String cleanResults = this.settings.getString("sm.cleanresults");
        String projectName = this.settings.getString("sonar.projectKey");
        projectName = StringUtils.replace(projectName, ":", "_");

        String softFilter = "";
        String softFilterFilePath = null;
        String hardFilter = this.settings.getString("sm.cpp.hardFilter");
        String additionalParameters = this.settings.getString("sm.cpp.toolchainOptions");

        try {
            softFilter = getFilterContent();
            softFilterFilePath = writeSoftFilterToFile(softFilter);
        } catch (IOException e) {
            LOG.warn("Cannot create softFilter file for toolchain! No softFilter is used during analyzis.", e);
        }

        ProfileInitializer profileInitializer = new ProfileInitializer(
                this.settings, getMetricHunterCategories(), profile,
                new SourceMeterCppRuleRepository(new XMLRuleParser()));

        String profilePath = this.fileSystem.workDir() + File.separator
                + "SM-Profile.xml";

        this.commands.add(pathToCA + File.separator
                + Cpp.KEY.toUpperCase(Locale.ENGLISH)
                + File.separator + "SourceMeterCPP");

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

        // Setting command and parameters for SourceMeter C++ analyzer
        this.commands.add("-resultsDir=" + resultsDir);
        this.commands.add("-projectName=" + projectName);
        this.commands.add("-projectBaseDir=" + baseDir);
        this.commands.add("-buildScript=" + pathToBuild);
        this.commands.add("-cleanResults=" + cleanResults);
        this.commands.add("-runChangeTracker=true");

        String cloneGenealogy = this.settings.getString("sm.cloneGenealogy");
        String cloneMinLines = this.settings.getString("sm.cloneMinLines");
        this.commands.add("-cloneGenealogy=" + cloneGenealogy);
        this.commands.add("-cloneMinLines=" + cloneMinLines);

        if (null != softFilterFilePath) {
            this.commands.add("-externalSoftFilter=" + softFilterFilePath);
        }

        if (null != hardFilter) {
            this.commands.add("-externalHardFilter=" + hardFilter);
        }

        if (null != additionalParameters) {
            this.commands.add(additionalParameters);
        }

        return true;
    }

    @Override
    protected List<MetricHunterCategory> getMetricHunterCategories() {
        List<MetricHunterCategory> categories = new ArrayList<MetricHunterCategory>();

        categories.add(new MetricHunterCategory("Class", SourceMeterCppMetrics
                .getClassThresholdMetrics()));

        categories.add(new MetricHunterCategory("Method",
                SourceMeterCppMetrics.getMethodThresholdMetrics()));
        categories.add(new MetricHunterCategory("Function", "method",
                SourceMeterCppMetrics.getFunctionThresholdMetrics()));

        categories.addAll(super.getMetricHunterCategories());

        return categories;
    }

}
