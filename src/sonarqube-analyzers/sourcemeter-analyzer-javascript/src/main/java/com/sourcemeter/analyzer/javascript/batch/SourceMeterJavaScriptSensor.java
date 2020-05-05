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

package com.sourcemeter.analyzer.javascript.batch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
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
import com.sourcemeter.analyzer.javascript.SourceMeterJavaScriptMetrics;
import com.sourcemeter.analyzer.javascript.core.JavaScript;
import com.sourcemeter.analyzer.javascript.profile.SourceMeterJavaScriptRuleRepository;
import com.sourcemeter.analyzer.javascript.visitor.CloneTreeSaverVisitorJavaScript;
import com.sourcemeter.analyzer.javascript.visitor.LogicalTreeLoaderVisitorJavaScript;
import com.sourcemeter.analyzer.javascript.visitor.LogicalTreeSaverVisitorJavaScript;
import com.sourcemeter.analyzer.javascript.visitor.PhysicalTreeLoaderVisitorJavaScript;

import graphlib.Graph;
import graphlib.GraphlibException;
import graphlib.Node;
import graphlib.Node.NodeType;
import graphlib.VisitorException;

import static com.sourcemeter.analyzer.javascript.SourceMeterJavaScriptMetrics.SM_JAVASCRIPT_CLONE_TREE;
import static com.sourcemeter.analyzer.javascript.SourceMeterJavaScriptMetrics.SM_JAVASCRIPT_LOGICAL_LEVEL1;
import static com.sourcemeter.analyzer.javascript.SourceMeterJavaScriptMetrics.SM_JAVASCRIPT_LOGICAL_LEVEL2;
import static com.sourcemeter.analyzer.javascript.SourceMeterJavaScriptMetrics.SM_JAVASCRIPT_LOGICAL_LEVEL3;

public class SourceMeterJavaScriptSensor extends SourceMeterSensor {

    /**
     * Command and parameters for running SourceMeter JavaScript analyzer
     */
    private final FileSystem fileSystem;

    private static final Logger LOG = LoggerFactory.getLogger(SourceMeterJavaScriptSensor.class);
    private static final String THRESHOLD_PROPERTIES_PATH = "/threshold_properties.xml";
    private static final String LOGICAL_ROOT = "__LogicalRoot__";

    public SourceMeterJavaScriptSensor(FileSystem fileSystem,
            InputProject inputProject, ActiveRules activeRules,
            Configuration configuration) {

        super(fileSystem, inputProject, activeRules, configuration);

        this.fileSystem = fileSystem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(SensorContext sensorContext) {
        boolean skipJavaScript = FileHelper.getBooleanFromConfiguration(this.configuration, "sm.javascript.skipToolchain");
        if (skipJavaScript) {
            LOG.info("SourceMeter toolchain is skipped for JavaScript. Results will be uploaded from former results directory, if it exists.");
        } else {
            if (!checkProperties()) {
                throw new RuntimeException("Failed to initialize the SourceMeter plugin. Some mandatory properties are not set properly.");
            }
            runSourceMeter(commands);
        }

        this.projectName = FileHelper.getStringFromConfiguration(this.configuration, "sonar.projectKey");
        this.projectName = StringUtils.replace(projectName, ":", "_");

        try {
            this.resultGraph = FileHelper.getSMSourcePath(configuration, fileSystem, '-', new JavaScript())
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
        headerLicenseInformations.put("DuplicatedCodeFinder", "Duplicated Code");
        headerLicenseInformations.put("LIM2Metrics", "Metrics");
        headerLicenseInformations.put("ESLint2Graph", "ESLint");

        super.saveLicense(graph, sensorContext, headerLicenseInformations, SourceMeterJavaScriptMetrics.JAVASCRIPT_LICENSE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadDataFromGraphBin(String filename, InputProject project,
                    SensorContext sensorContext) throws GraphlibException {
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
            LogicalTreeLoaderVisitorJavaScript logicalVisitor = new LogicalTreeLoaderVisitorJavaScript(
                    this.fileSystem, this.configuration, sensorContext,
                    nodeCounter.getNumberOfNodes());

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, "__PhysicalRoot__", "PhysicalTree", nodeCounter);
            PhysicalTreeLoaderVisitorJavaScript physicalVisitor = new PhysicalTreeLoaderVisitorJavaScript(
                    this.fileSystem, sensorContext, nodeCounter.getNumberOfNodes());

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, LOGICAL_ROOT, "logicalTree", nodeCounter);
            LogicalTreeSaverVisitorJavaScript logicalSaver = new LogicalTreeSaverVisitorJavaScript(sensorContext, this.fileSystem, configuration);

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, "__CloneRoot__", "CloneTree", nodeCounter);
            CloneTreeSaverVisitorJavaScript cloneSaver = new CloneTreeSaverVisitorJavaScript(sensorContext, this.fileSystem);

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
            logicalSaver.saveLogicalTreeToDatabase(SM_JAVASCRIPT_LOGICAL_LEVEL1, SM_JAVASCRIPT_LOGICAL_LEVEL2, SM_JAVASCRIPT_LOGICAL_LEVEL3);
            LOG.info("      * Saving LogicalTree done: " + logicalSaver.getLogicalTime() + MS);
            logicalSaver = null;

            LOG.info("      * Saving CloneTree...");
            GraphHelper.processGraph(graph, "__CloneRoot__", "CloneTree", cloneSaver);
            cloneSaver.saveCloneTreeToDatabase(SM_JAVASCRIPT_CLONE_TREE);
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
        descriptor.onlyOnLanguage(JavaScript.KEY);
    }

    /**
     * Checks the correctness of sourceMeter's properties.
     *
     * @return True if the properties were set correctly.
     */
    private boolean checkProperties() {
        String pathToCA = FileHelper.getStringFromConfiguration(this.configuration, "sm.toolchaindir");
        if (pathToCA == null) {
            LOG.error("JavaScript SourceMeter path must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String resultsDir = FileHelper.getStringFromConfiguration(configuration, "sm.resultsdir");
        if (resultsDir == null) {
            LOG.error("Results directory must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String projectName = FileHelper.getStringFromConfiguration(configuration, "sonar.projectKey");
        projectName = StringUtils.replace(projectName, ":", "_");

        ProfileInitializer profileInitializer = new ProfileInitializer(
                this.configuration, getMetricHunterCategories(), this.activeRules,
                new SourceMeterJavaScriptRuleRepository(new RulesDefinitionXmlLoader()), new JavaScript());

        String profilePath = this.fileSystem.workDir() + File.separator
                + "SM-Profile.xml";

        this.commands.add(pathToCA + File.separator
                + JavaScript.NAME + File.separator + "SourceMeterJavaScript");

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

        // Setting command and parameters for SourceMeter JavaScript analyzer
        String cleanResults = FileHelper.getStringFromConfiguration(configuration, "sm.cleanresults");
        this.commands.add("-cleanResults=" + cleanResults);
        this.commands.add("-resultsDir=" + resultsDir);
        this.commands.add("-projectName=" + projectName);
        this.commands.add("-projectBaseDir=" + baseDir);

        String hardFilter = FileHelper.getStringFromConfiguration(configuration, "sm.javascript.hardFilter");
        if (null != hardFilter) {
            this.commands.add("-externalHardFilter=" + hardFilter);
        }

        String additionalParameters = FileHelper.getStringFromConfiguration(configuration, "sm.javascript.toolchainOptions");
        if (null != additionalParameters) {
            this.commands.add(additionalParameters);
        }

        addCommonCommandlineOptions();

        return true;
    }

    /**
     * Generate MetricHunterCategories, stored in XML file.
     *
     * @return List of MetricHunterCategories.
     */
    protected List<MetricHunterCategory> getMetricHunterCategories() {
        List<MetricHunterCategory> categories = new ArrayList<MetricHunterCategory>();

        InputStream xmlFile = null;
        try {
            xmlFile = getClass().getResourceAsStream(THRESHOLD_PROPERTIES_PATH);
            categories.add(new MetricHunterCategory("Class", ThresholdPropertiesHelper
                    .getClassThresholdMetrics(xmlFile)));

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
}
