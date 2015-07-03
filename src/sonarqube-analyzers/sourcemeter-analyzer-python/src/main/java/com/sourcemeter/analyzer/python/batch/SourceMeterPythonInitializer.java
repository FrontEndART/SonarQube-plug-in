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
package com.sourcemeter.analyzer.python.batch;

import graphlib.GraphlibException;
import graphlib.VisitorException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import com.sourcemeter.analyzer.python.SourceMeterPythonMetrics;
import com.sourcemeter.analyzer.python.core.Python;
import com.sourcemeter.analyzer.python.profile.SourceMeterPythonRuleRepository;

public class SourceMeterPythonInitializer extends SourceMeterInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(SourceMeterPythonInitializer.class);

    /**
     * Command and parameters for running SourceMeter Python analyzer
     */
    private final List<String> commands;

    public SourceMeterPythonInitializer(FileSystem fileSystem,
            Settings settings, ProjectDefinition projectDefinition,
            PathResolver pathResolver, RulesProfile profile) {
        super(fileSystem, settings, projectDefinition, pathResolver, profile);
        this.commands = new ArrayList<String>();
        SourceMeterInitializer.updatePluginLanguage(Python.INSTANCE);
    }

    @Override
    public boolean shouldExecuteOnProject(Project project) {
        boolean skipPython = this.settings.getBoolean("sm.python.skip");
        if (skipPython) {
            Python.removeSuffixesForCurrentAnalyze();
            return false;
        }

        return super.shouldExecuteOnProject(project);
    }

    @Override
    public void execute(Project project) {
        this.settings.setProperty(CoreProperties.CPD_SKIP_PROPERTY, true); // Disable built in CPD plugin

        if (this.settings.getBoolean("sm.python.skipToolchain")) {
            LOG.info("    SourceMeter toolchain is skipped for Python. Results will be uploaded from former results directory, if it exists.");
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
            createExcludesFromNotAnalyzedFiles('_');
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
            LOG.error("Python SourceMeter path must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String resultsDir = this.settings.getString("sm.resultsdir");
        if (resultsDir == null) {
            LOG.error("Results directory must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String pythonBinary = this.settings.getString("sm.python.binary");
        if (pythonBinary == null) {
            LOG.error("Python 2.7 binary path must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String projectName = this.settings.getString("sonar.projectKey");
        projectName = StringUtils.replace(projectName, ":", "_");
        String cleanResults = this.settings.getString("sm.cleanresults");
        String additionalParameters = this.settings.getString("sm.python.toolchainOptions");

        String filter = "";
        String filterFilePath = null;

        try {
            filter = getFilterContent();
            filterFilePath = writeHardFilterToFile(filter.toString());
        } catch (IOException e) {
            LOG.warn("Cannot create filter file for toolchain! No filter is used during analyzis.", e);
        }

        this.commands.add(pathToCA + File.separator
                + Python.NAME + File.separator + "SourceMeterPython");

        ProfileInitializer profileInitializer = new ProfileInitializer(
                this.settings, getMetricHunterCategories(), this.profile,
                new SourceMeterPythonRuleRepository(new XMLRuleParser()));

        String profilePath = this.fileSystem.workDir() + File.separator
                + "SM-Profile.xml";
        try {
            profileInitializer.generatePofileFile(profilePath);
            this.commands.add("-profileXML=" + profilePath);
        } catch (IOException e) {
            LOG.warn("An error occured while creating SourceMeter profile file. Default profile is used!!", e);
        }

        // Setting command and parameters for SourceMeter Python analyzer
        String baseDir = "";
        try {
            baseDir = this.fileSystem.baseDir().getCanonicalPath();
        } catch (IOException e) {
            LOG.warn("Could not get base directory's canonical path. Absolute path is used.");
            baseDir = this.fileSystem.baseDir().getAbsolutePath();
        }

        this.commands.add("-projectBaseDir=" + baseDir);
        this.commands.add("-resultsDir=" + resultsDir);
        this.commands.add("-projectName=" + projectName);
        this.commands.add("-python27binary=" + pythonBinary);
        this.commands.add("-runChangeTracker=true");
        this.commands.add("-cleanResults=" + cleanResults);

        String cloneGenealogy = this.settings.getString("sm.cloneGenealogy");
        String cloneMinLines = this.settings.getString("sm.cloneMinLines");
        this.commands.add("-cloneGenealogy=" + cloneGenealogy);
        this.commands.add("-cloneMinLines=" + cloneMinLines);

        if (null != filterFilePath) {
            this.commands.add("-externalHardFilter=" + filterFilePath);
        }

        if (additionalParameters != null) {
            this.commands.add(additionalParameters);
        }

        return true;
    }

    @Override
    protected List<MetricHunterCategory> getMetricHunterCategories() {
        List<MetricHunterCategory> categories = new ArrayList<MetricHunterCategory>();

        categories.add(new MetricHunterCategory("Class",
                SourceMeterPythonMetrics.getClassThresholdMetrics()));

        categories.add(new MetricHunterCategory("Method",
                SourceMeterPythonMetrics.getMethodThresholdMetrics()));

        categories.add(new MetricHunterCategory("Function",
                SourceMeterPythonMetrics.getFunctionThresholdMetrics()));

        categories.addAll(super.getMetricHunterCategories());

        return categories;
    }
}
