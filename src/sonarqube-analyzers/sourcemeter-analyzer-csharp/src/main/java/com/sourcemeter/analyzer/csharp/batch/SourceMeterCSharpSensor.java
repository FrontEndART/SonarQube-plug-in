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

package com.sourcemeter.analyzer.csharp.batch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.scanner.fs.InputProject;
import org.sonar.api.server.rule.RulesDefinitionXmlLoader;

import com.sourcemeter.analyzer.base.batch.MetricHunterCategory;
import com.sourcemeter.analyzer.base.batch.ProfileInitializer;
import com.sourcemeter.analyzer.base.batch.SourceMeterSensor;
import com.sourcemeter.analyzer.base.helper.FileHelper;
import com.sourcemeter.analyzer.base.helper.GraphHelper;
import com.sourcemeter.analyzer.base.helper.ThresholdPropertiesHelper;
import com.sourcemeter.analyzer.base.visitor.NodeCounterVisitor;
import com.sourcemeter.analyzer.csharp.SourceMeterCSharpMetrics;
import com.sourcemeter.analyzer.csharp.core.CSharp;
import com.sourcemeter.analyzer.csharp.profile.SourceMeterCSharpRuleRepository;
import com.sourcemeter.analyzer.csharp.visitor.CloneTreeSaverVisitorCSharp;
import com.sourcemeter.analyzer.csharp.visitor.LogicalTreeLoaderVisitorCSharp;
import com.sourcemeter.analyzer.csharp.visitor.LogicalTreeSaverVisitorCSharp;
import com.sourcemeter.analyzer.csharp.visitor.PhysicalTreeLoaderVisitorCSharp;

import graphlib.Graph;
import graphlib.GraphlibException;
import graphlib.Node;
import graphlib.Node.NodeType;
import graphlib.VisitorException;
import org.sonar.api.utils.System2;

import static com.sourcemeter.analyzer.csharp.SourceMeterCSharpMetrics.SM_CSHARP_CLONE_TREE;
import static com.sourcemeter.analyzer.csharp.SourceMeterCSharpMetrics.SM_CSHARP_LOGICAL_LEVEL1;
import static com.sourcemeter.analyzer.csharp.SourceMeterCSharpMetrics.SM_CSHARP_LOGICAL_LEVEL2;
import static com.sourcemeter.analyzer.csharp.SourceMeterCSharpMetrics.SM_CSHARP_LOGICAL_LEVEL3;

public class SourceMeterCSharpSensor extends SourceMeterSensor {

    /**
     * Command and parameters for running SourceMeter C# analyzer
     */
    private final FileSystem fileSystem;

    private static final Logger LOG = LoggerFactory.getLogger(SourceMeterCSharpSensor.class);
    private static final String THRESHOLD_PROPERTIES_PATH = "/threshold_properties.xml";
    private static final String LOGICAL_ROOT = "__LogicalRoot__";

    public SourceMeterCSharpSensor(FileSystem fileSystem,
           InputProject inputProject, ActiveRules activeRules,
           Configuration configuration, System2 system) {

        super(fileSystem, inputProject, activeRules, configuration, system);

        this.fileSystem = fileSystem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(SensorContext sensorContext) {
        boolean skipCsharp = FileHelper.getBooleanFromConfiguration(configuration, "sm.csharp.skipToolchain");
        if (skipCsharp) {
            LOG.info("SourceMeter toolchain is skipped for C#. Results will be uploaded from former results directory, if it exists.");
        } else {
            if (!checkProperties(sensorContext)) {
                throw new RuntimeException("Failed to initialize the SourceMeter plugin. Some mandatory properties are not set properly.");
            }
            runSourceMeter(commands);
        }

        this.projectName = FileHelper.getStringFromConfiguration(configuration, "sonar.projectKey");
        this.projectName = StringUtils.replace(this.projectName, ":", "_");

        try {
            this.resultGraph = FileHelper.getSMSourcePath(configuration, fileSystem, '-', new CSharp())
                    + File.separator + this.projectName + ".graph";
        } catch (IOException e) {
            LOG.error("Error during loading result graph path!", e);
        }

        long startTime = System.currentTimeMillis();
        LOG.info("      Graph: " + resultGraph);

        try {
            loadDataFromGraphBin(this.resultGraph, sensorContext.project(), sensorContext);
        } catch (GraphlibException e) {
            LOG.error("Error during loading graph!", e);
        }

        LOG.info("    Load data from graph bin and save resources and metrics done: " + (System.currentTimeMillis() - startTime) + MS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * Collects the license information from result graph's header in a list,
     * and saves it to a special metric.
     *
     * @param graph Result graph.
     * @param sensorContext Context of the sensor.
     */
    private void saveLicense(Graph graph, SensorContext sensorContext) {
        Map<String, String> headerLicenseInformations = new HashMap<String, String>();
        headerLicenseInformations.put("MetricHunter", "MetricHunter");
        headerLicenseInformations.put("DuplicatedCodeFinder", "Duplicated Code");
        headerLicenseInformations.put("LIM2Metrics", "Metrics");
        headerLicenseInformations.put("FxCop2Graph", "FxCop");

        super.saveLicense(graph, sensorContext, headerLicenseInformations,
                SourceMeterCSharpMetrics.CSHARP_LICENSE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadDataFromGraphBin(String filename, InputProject inputProject, SensorContext sensorContext) throws GraphlibException {
        Graph graph = new Graph();
        graph.loadBinary(filename);

        saveLicense(graph, sensorContext);

        Node componentRoot = null;
        NodeCounterVisitor nodeCounter = null;

        List<Node> components = graph.findNodes(new NodeType("Component"));
        for (Node component : components) {
            String name = GraphHelper.getNodeNameAttribute(component);
            if (name != null && name.equals("<System>")) {
                componentRoot = component;
                break;
            }
        }

        try {
            LOG.info("      * Initialization...");
            long startTime = System.currentTimeMillis();

            if (componentRoot != null) {
                nodeCounter = new NodeCounterVisitor();
                GraphHelper.processGraph(graph, componentRoot, "ComponentTree", nodeCounter);
            }

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, LOGICAL_ROOT, "LogicalTree", nodeCounter);
            LogicalTreeLoaderVisitorCSharp logicalVisitor = new LogicalTreeLoaderVisitorCSharp(
                    this.fileSystem, configuration,
                    sensorContext, nodeCounter.getNumberOfNodes());

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, "__PhysicalRoot__", "PhysicalTree", nodeCounter);
            PhysicalTreeLoaderVisitorCSharp physicalVisitor = new PhysicalTreeLoaderVisitorCSharp(
                    fileSystem, sensorContext, nodeCounter.getNumberOfNodes());

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, LOGICAL_ROOT, "logicalTree", nodeCounter);
            LogicalTreeSaverVisitorCSharp logicalSaver = new LogicalTreeSaverVisitorCSharp(sensorContext, this.fileSystem, configuration);

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, "__CloneRoot__", "CloneTree", nodeCounter);
            CloneTreeSaverVisitorCSharp cloneSaver = new CloneTreeSaverVisitorCSharp(sensorContext, this.fileSystem);

            LOG.info("      * Initialization done: " + (System.currentTimeMillis() - startTime) + MS);

            LOG.info("      * Processing LogicalTree...");
            GraphHelper.processGraph(graph, LOGICAL_ROOT, "LogicalTree", logicalVisitor);
            LOG.info("      * Processing LogicalTree done: " + logicalVisitor.getLogicalTime() + MS);
            logicalVisitor = null;

            LOG.info("      * Processing PhysicalTree...");
            GraphHelper.processGraph(graph, "__PhysicalRoot__", "PhysicalTree", physicalVisitor);
            LOG.info("      * Processing PhysicalTree done: " + physicalVisitor.getFileTime() + MS);
            physicalVisitor = null;

            LOG.info("      * Saving LogicalTree...");
            GraphHelper.processGraph(graph, LOGICAL_ROOT, "LogicalTree", logicalSaver);
            logicalSaver.saveLogicalTreeToDatabase(SM_CSHARP_LOGICAL_LEVEL1, SM_CSHARP_LOGICAL_LEVEL2, SM_CSHARP_LOGICAL_LEVEL3);
            LOG.info("      * Saving LogicalTree done: " + logicalSaver.getLogicalTime() + MS);
            logicalSaver = null;

            LOG.info("      * Saving CloneTree...");
            GraphHelper.processGraph(graph, "__CloneRoot__", "CloneTree", cloneSaver);
            cloneSaver.saveCloneTreeToDatabase(SM_CSHARP_CLONE_TREE);
            LOG.info("      * Saving CloneTree done: " + cloneSaver.getFileTime() + MS);
            cloneSaver = null;

        } catch (VisitorException e) {
            LOG.error("Error during loading data from graph!", e);
        } finally {
            graph = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.onlyOnLanguage(CSharp.KEY);
    }

    /**
     * Checks the correctness of sourceMeter's properties.
     *
     * @param sensorContext Context of the sensor.
     * @return True if the properties were set correctly.
     */
    private boolean checkProperties(SensorContext sensorContext) {
        String pathToCA = FileHelper.getStringFromConfiguration(this.configuration, "sm.toolchaindir");
        if (pathToCA == null) {
            LOG.error("SourceMeter path must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String resultsDir = FileHelper.getStringFromConfiguration(this.configuration, "sm.resultsdir");
        if (resultsDir == null) {
            LOG.error("Results directory must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String inputFile = FileHelper.getStringFromConfiguration(this.configuration, "sm.csharp.input");
        if (inputFile == null) {
            LOG.error("Input solution file's path must be set in properties! Key: sm.csharp.input");
            return false;
        }

        String configuration = FileHelper.getStringFromConfiguration(this.configuration, "sm.csharp.configuration");
        if (configuration == null) {
            LOG.error("Project configuration must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String platform = FileHelper.getStringFromConfiguration(this.configuration, "sm.csharp.platform");
        if (platform == null) {
            LOG.error("Target platform must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String projectName = FileHelper.getStringFromConfiguration(this.configuration, "sonar.projectKey");
        projectName = StringUtils.replace(projectName, ":", "_");

        ProfileInitializer profileInitializer = new ProfileInitializer(
                this.configuration, getMetricHunterCategories(), this.activeRules,
                new SourceMeterCSharpRuleRepository(new RulesDefinitionXmlLoader()), new CSharp());

        String profilePath = this.fileSystem.workDir() + File.separator
                + "SM-Profile.xml";

        String csharpKey = CSharp.KEY.toUpperCase(Locale.ENGLISH);
        if ("CS".equals(csharpKey)) {
            csharpKey = "CSharp";
        }

        this.commands.add(pathToCA + File.separator
                + csharpKey + File.separator
                + "AnalyzerCSharp");

        try {
            profileInitializer.generatePofileFile(profilePath);
            this.commands.add("-profileXML=" + profilePath);
        } catch (IOException e) {
            LOG.warn("An error occured while creating SourceMeter profile file. Default profile is used!!", e);
        }

        // Setting command and parameters for SourceMeter C# analyzer
        String runFxCop = FileHelper.getStringFromConfiguration(this.configuration, "sm.csharp.runFxCop");
        if (runFxCop != null) {
            this.commands.add("-runFxCop=" + runFxCop);
        }

        String pathToFxCop = FileHelper.getStringFromConfiguration(this.configuration, "sm.csharp.fxCopPath");
        if (pathToFxCop != null) {
            this.commands.add("-FxCopPath=" + pathToFxCop);
        }

        String cleanResults = FileHelper.getStringFromConfiguration(this.configuration, "sm.cleanresults");
        this.commands.add("-cleanResults=" + cleanResults);
        this.commands.add("-input=" + inputFile);
        this.commands.add("-resultsDir=" + resultsDir);
        this.commands.add("-projectName=" + projectName);
        this.commands.add("-configuration=" + configuration);
        this.commands.add("-platform=" + platform);

        String softFilter = "";
        String softFilterFilePath = null;
        softFilter = getFilterContentCsharp(sensorContext, CSharp.KEY);
        try {
            softFilterFilePath = writeSoftFilterToFile(softFilter);
        } catch (IOException e) {
            LOG.warn("Cannot create softFilter file for toolchain! No softFilter is used during analyzis.", e);
        }

        if (null != softFilterFilePath) {
            this.commands.add("-externalSoftFilter=" + softFilterFilePath);
        }

        String baseDir = "";
        try {
            baseDir = this.fileSystem.baseDir().getCanonicalPath();
        } catch (IOException e) {
            LOG.warn("Could not get base directory's canonical path. Absolute path is used.");
            baseDir = this.fileSystem.baseDir().getAbsolutePath();
        }
        this.commands.add("-projectBaseDir=" + baseDir);

        String hardFilter = FileHelper.getStringFromConfiguration(this.configuration, "sm.csharp.hardFilter");
        if (null != hardFilter) {
            this.commands.add("-externalHardFilter=" + hardFilter);
        }

        addCommonCommandlineOptions();

        String additionalParameters = FileHelper.getStringFromConfiguration(this.configuration, "sm.csharp.toolchainOptions");
        if (null != additionalParameters) {
            this.commands.add(additionalParameters);
        }

        return true;
    }

    /**
     * Generate MetricHunterCategories, stored in XML file.
     *
     * @return List of MetricHunterCategories
     */
    protected List<MetricHunterCategory> getMetricHunterCategories() {
        List<MetricHunterCategory> categories = new ArrayList<MetricHunterCategory>();

        InputStream xmlFile = null;
        try {
            xmlFile = getClass().getResourceAsStream(THRESHOLD_PROPERTIES_PATH);
            categories.add(new MetricHunterCategory("Class",
                    ThresholdPropertiesHelper.getClassThresholdMetrics(xmlFile)));

            xmlFile = getClass().getResourceAsStream(THRESHOLD_PROPERTIES_PATH);
            categories.add(new MetricHunterCategory("Method",
                    ThresholdPropertiesHelper.getMethodThresholdMetrics(xmlFile)));

            xmlFile = getClass().getResourceAsStream(THRESHOLD_PROPERTIES_PATH);
            categories.add(new MetricHunterCategory("CloneClass",
                    ThresholdPropertiesHelper.getCloneClassThresholdMetrics(xmlFile)));
            xmlFile = getClass().getResourceAsStream(THRESHOLD_PROPERTIES_PATH);
            categories.add(new MetricHunterCategory("CloneInstance",
                    ThresholdPropertiesHelper.getCloneInstanceThresholdMetrics(xmlFile)));
        } finally {
            IOUtils.closeQuietly(xmlFile);
        }
        return categories;
    }

    /**
     * Assemble filter file's content by exclusions for C#.
     *
     * @param sensorContext Context of the sensor.
     * @param languageKey Key of the analyzed language.
     * @return Filter file's content.
     */
    private String getFilterContentCsharp(SensorContext sensorContext, String languageKey) {
        StringBuffer filter = new StringBuffer("-*\n");

        List<InputFile> sourceFilesForProject = super.getSourceFilesForProject(sensorContext, languageKey);
        for (InputFile file : sourceFilesForProject) {
            String path = file.uri().normalize().getPath();
            filter.append("+");
            if (system.isOsWindows()) {
                filter.append(path.substring(1).replaceAll("/", "\\\\\\\\"));
            } else {
                filter.append(Pattern.quote(path));
            }
            filter.append("\n");
        }
        return filter.toString();
    }
}
