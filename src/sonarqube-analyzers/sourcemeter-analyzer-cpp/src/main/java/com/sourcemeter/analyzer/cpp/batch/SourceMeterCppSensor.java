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

package com.sourcemeter.analyzer.cpp.batch;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
import com.sourcemeter.analyzer.cpp.SourceMeterCppMetrics;
import com.sourcemeter.analyzer.cpp.core.Cpp;
import com.sourcemeter.analyzer.cpp.profile.SourceMeterCppRuleRepository;
import com.sourcemeter.analyzer.cpp.visitor.CloneTreeSaverVisitorCpp;
import com.sourcemeter.analyzer.cpp.visitor.LogicalTreeLoaderVisitorCpp;
import com.sourcemeter.analyzer.cpp.visitor.LogicalTreeSaverVisitorCpp;
import com.sourcemeter.analyzer.cpp.visitor.PhysicalTreeLoaderVisitorCpp;

import graphlib.Graph;
import graphlib.GraphlibException;
import graphlib.Node;
import graphlib.Node.NodeType;
import graphlib.VisitorException;
import org.sonar.api.utils.System2;

import static com.sourcemeter.analyzer.cpp.SourceMeterCppMetrics.SM_CPP_CLONE_TREE;
import static com.sourcemeter.analyzer.cpp.SourceMeterCppMetrics.SM_CPP_LOGICAL_LEVEL1;
import static com.sourcemeter.analyzer.cpp.SourceMeterCppMetrics.SM_CPP_LOGICAL_LEVEL2;
import static com.sourcemeter.analyzer.cpp.SourceMeterCppMetrics.SM_CPP_LOGICAL_LEVEL3;

public class SourceMeterCppSensor extends SourceMeterSensor {

    /**
     * Command and parameters for running SourceMeter C/C++ analyzer
     */
    private final FileSystem fileSystem;

    private static final Logger LOG = LoggerFactory.getLogger(SourceMeterCppSensor.class);
    private static final String THRESHOLD_PROPERTIES_PATH = "/threshold_properties.xml";
    private static final String LOGICAL_ROOT = "__LogicalRoot__";

    public SourceMeterCppSensor(FileSystem fileSystem,
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
        boolean skipCpp = FileHelper.getBooleanFromConfiguration(this.configuration, "sm.cpp.skipToolchain");
        if (skipCpp) {
            LOG.info("SourceMeter toolchain is skipped for C/C++. Results will be uploaded from former results directory, if it exists.");
        } else {
            if (!checkProperties(sensorContext)) {
                throw new RuntimeException("Failed to initialize the SourceMeter plugin. Some mandatory properties are not set properly.");
            }
            runSourceMeter(commands);
        }

        this.projectName = FileHelper.getStringFromConfiguration(this.configuration, "sonar.projectKey");
        this.projectName = StringUtils.replace(projectName, ":", "_");

        try {
            this.resultGraph = FileHelper.getSMSourcePath(configuration, fileSystem, '-', new Cpp())
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
        headerLicenseInformations.put("FaultHunterCPP", "FaultHunter");
        headerLicenseInformations.put("MetricHunter", "MetricHunter");
        headerLicenseInformations.put("DuplicatedCodeFinder", "Duplicated Code");
        headerLicenseInformations.put("LIM2Metrics", "Metrics");
        headerLicenseInformations.put("Cppcheck2Graph", "CPPCheck");

        super.saveLicense(graph, sensorContext, headerLicenseInformations, SourceMeterCppMetrics.CPP_LICENSE);
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
            LogicalTreeLoaderVisitorCpp logicalVisitor = new LogicalTreeLoaderVisitorCpp(
                    this.fileSystem, this.configuration, sensorContext,
                    nodeCounter.getNumberOfNodes());

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, "__PhysicalRoot__", "PhysicalTree", nodeCounter);
            PhysicalTreeLoaderVisitorCpp physicalVisitor = new PhysicalTreeLoaderVisitorCpp(
                    this.fileSystem, sensorContext, nodeCounter.getNumberOfNodes());

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, LOGICAL_ROOT, "logicalTree", nodeCounter);
            LogicalTreeSaverVisitorCpp logicalSaver = new LogicalTreeSaverVisitorCpp(sensorContext, this.fileSystem, configuration);

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, "__CloneRoot__", "CloneTree", nodeCounter);
            CloneTreeSaverVisitorCpp cloneSaver = new CloneTreeSaverVisitorCpp(sensorContext, this.fileSystem);

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
            logicalSaver.saveLogicalTreeToDatabase(SM_CPP_LOGICAL_LEVEL1, SM_CPP_LOGICAL_LEVEL2, SM_CPP_LOGICAL_LEVEL3);
            LOG.info("      * Saving LogicalTree done: " + logicalSaver.getLogicalTime() + MS);
            logicalSaver = null;

            LOG.info("      * Saving CloneTree...");
            GraphHelper.processGraph(graph, "__CloneRoot__", "CloneTree", cloneSaver);
            cloneSaver.saveCloneTreeToDatabase(SM_CPP_CLONE_TREE);
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
        descriptor.onlyOnLanguage(Cpp.KEY);
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
            LOG.error("C/C++ SourceMeter path must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String pathToBuild = FileHelper.getStringFromConfiguration(configuration, "sm.cpp.buildfile");
        if (pathToBuild == null) {
            LOG.error("Build script path must be set! (sm.cpp.buildfile)");
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
                new SourceMeterCppRuleRepository(new RulesDefinitionXmlLoader()), new Cpp());

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

        // Setting command and parameters for SourceMeter C/C++ analyzer
        String cleanResults = FileHelper.getStringFromConfiguration(configuration, "sm.cleanresults");
        this.commands.add("-cleanResults=" + cleanResults);
        this.commands.add("-resultsDir=" + resultsDir);
        this.commands.add("-projectName=" + projectName);
        this.commands.add("-buildScript=" + pathToBuild);

        String softFilter = "";
        String softFilterFilePath = null;
        softFilter = getFilterContent(sensorContext, Cpp.KEY);
        try {
            softFilterFilePath = writeSoftFilterToFile(softFilter);
        } catch (IOException e) {
            LOG.warn("Cannot create softFilter file for toolchain! No softFilter is used during analyzis.", e);
        }

        if (null != softFilterFilePath) {
            this.commands.add("-externalSoftFilter=" + softFilterFilePath);
        }

        String hardFilter = FileHelper.getStringFromConfiguration(configuration, "sm.cpp.hardFilter");
        if (null != hardFilter) {
            this.commands.add("-externalHardFilter=" + hardFilter);
        }

        String additionalParameters = FileHelper.getStringFromConfiguration(configuration, "sm.cpp.toolchainOptions");
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
            categories.add(new MetricHunterCategory("Interface", "class", ThresholdPropertiesHelper
                    .getClassThresholdMetrics(xmlFile)));
            xmlFile = getClass().getResourceAsStream(THRESHOLD_PROPERTIES_PATH);
            categories.add(new MetricHunterCategory("Structure", "class", ThresholdPropertiesHelper
                    .getClassThresholdMetrics(xmlFile)));
            xmlFile = getClass().getResourceAsStream(THRESHOLD_PROPERTIES_PATH);
            categories.add(new MetricHunterCategory("Union", "class", ThresholdPropertiesHelper
                    .getClassThresholdMetrics(xmlFile)));
            xmlFile = getClass().getResourceAsStream(THRESHOLD_PROPERTIES_PATH);
            categories.add(new MetricHunterCategory("Enum", "class", ThresholdPropertiesHelper
                    .getClassThresholdMetrics(xmlFile)));

            xmlFile = getClass().getResourceAsStream(THRESHOLD_PROPERTIES_PATH);
            categories.add(new MetricHunterCategory("Method",
                    ThresholdPropertiesHelper.getMethodThresholdMetrics(xmlFile)));
            xmlFile = getClass().getResourceAsStream(THRESHOLD_PROPERTIES_PATH);
            categories.add(new MetricHunterCategory("Function", "method",
                    ThresholdPropertiesHelper.getFunctionThresholdMetrics(xmlFile)));

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
