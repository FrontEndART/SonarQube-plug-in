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
package com.sourcemeter.analyzer.csharp.batch;

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
import com.sourcemeter.analyzer.csharp.SourceMeterCSharpMetrics;
import com.sourcemeter.analyzer.csharp.core.CSharp;
import com.sourcemeter.analyzer.csharp.profile.SourceMeterCSharpRuleRepository;

public class SourceMeterCSharpInitializer extends SourceMeterInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(SourceMeterCSharpInitializer.class);

    /**
     * Command and parameters for running SourceMeter C# analyzer
     */
    private final List<String> commands;

    public SourceMeterCSharpInitializer(FileSystem fileSystem, Settings settings,
            ProjectDefinition projectDefinition, PathResolver pathResolver,
            RulesProfile profile) {
        super(fileSystem, settings, projectDefinition, pathResolver, profile);
        this.commands = new ArrayList<String>();
        SourceMeterInitializer.updatePluginLanguage(CSharp.INSTANCE);
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        boolean skipCSharp = this.settings.getBoolean("sm.csharp.skip");
        if (skipCSharp) {
            CSharp.removeSuffixesForCurrentAnalyze();
            return false;
        }

        return super.shouldExecuteOnProject(project);
    }

    @Override
    public void execute(Project project) {
        this.settings.setProperty(CoreProperties.CPD_SKIP_PROPERTY, true); // Disable built in CPD plugin

        if (this.settings.getBoolean("sm.csharp.skipToolchain")) {
            LOG.info("    SourceMeter toolchain is skipped for C#. Results will be uploaded from former results directory, if it exists.");
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
            LOG.warn(
                    "Could not create exlcusions from result graph! All C# files are indexed by SonarQube automatically.",
                    e);
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
            LOG.error("SourceMeter path must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String resultsDir = this.settings.getString("sm.resultsdir");
        if (resultsDir == null) {
            LOG.error("Results directory must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String inputFile = this.settings.getString("sm.csharp.input");
        if (inputFile == null) {
            LOG.error("Input solution file's path must be set in properties! Key: sm.csharp.input");
            return false;
        }

        String configuration = this.settings.getString("sm.csharp.configuration");
        if (configuration == null) {
            LOG.error("Project configuration must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String platform = this.settings.getString("sm.csharp.platform");
        if (platform == null) {
            LOG.error("Target platform must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String cleanResults = this.settings.getString("sm.cleanresults");
        String projectName = this.settings.getString("sonar.projectKey");
        projectName = StringUtils.replace(projectName, ":", "_");

        String softFilter = "";
        String softFilterFilePath = null;
        String hardFilter = this.settings.getString("sm.csharp.hardFilter");
        String additionalParameters = this.settings.getString("sm.csharp.toolchainOptions");

        try {
            softFilter = getFilterContent();
            softFilterFilePath = writeSoftFilterToFile(softFilter);
        } catch (IOException e) {
            LOG.warn("Cannot create softFilter file for toolchain! No softFilter is used during analyzis.", e);
        }

        ProfileInitializer profileInitializer = new ProfileInitializer(
                this.settings, getMetricHunterCategories(), profile,
                new SourceMeterCSharpRuleRepository(new XMLRuleParser()));

        String profilePath = this.fileSystem.workDir() + File.separator
                + "SM-Profile.xml";

        this.commands.add(pathToCA + File.separator
                + CSharp.KEY.toUpperCase(Locale.ENGLISH) + File.separator
                + "SourceMeterCSharp");

        try {
            profileInitializer.generatePofileFile(profilePath);
            this.commands.add("-profileXML=" + profilePath);
        } catch (IOException e) {
            LOG.warn("An error occured while creating SourceMeter profile file. Default profile is used!!", e);
        }

        // Setting command and parameters for SourceMeter C# analyzer
        String runFxCop = this.settings.getString("sm.csharp.runFxCop");
        if (runFxCop != null) {
            this.commands.add("-runFxCop=" + runFxCop);
        }

        String pathToFxCop = this.settings.getString("sm.csharp.fxCopPath");
        if (pathToFxCop != null) {
            this.commands.add("-FxCopPath=" + pathToFxCop);
        }

        this.commands.add("-input=" + inputFile);
        this.commands.add("-resultsDir=" + resultsDir);
        this.commands.add("-projectName=" + projectName);
        this.commands.add("-cleanResults=" + cleanResults);
        this.commands.add("-configuration=" + configuration);
        this.commands.add("-platform=" + platform);
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

        categories.add(new MetricHunterCategory("Class",
                SourceMeterCSharpMetrics.getClassThresholdMetrics()));

        categories.add(new MetricHunterCategory("Method",
                SourceMeterCSharpMetrics.getMethodThresholdMetrics()));

        categories.addAll(super.getMetricHunterCategories());

        return categories;
    }

}
