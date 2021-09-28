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

package com.sourcemeter.analyzer.java.batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
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
import com.sourcemeter.analyzer.java.SourceMeterJavaMetrics;
import com.sourcemeter.analyzer.java.core.Java;
import com.sourcemeter.analyzer.java.profile.SourceMeterJavaRuleRepository;
import com.sourcemeter.analyzer.java.visitor.CloneTreeSaverVisitorJava;
import com.sourcemeter.analyzer.java.visitor.LogicalTreeLoaderVisitorJava;
import com.sourcemeter.analyzer.java.visitor.LogicalTreeSaverVisitorJava;
import com.sourcemeter.analyzer.java.visitor.PhysicalTreeLoaderVisitorJava;

import graphlib.Graph;
import graphlib.GraphlibException;
import graphlib.Node;
import graphlib.Node.NodeType;
import graphlib.VisitorException;
import org.sonar.api.utils.System2;

import static com.sourcemeter.analyzer.java.SourceMeterJavaMetrics.SM_JAVA_CLONE_TREE;
import static com.sourcemeter.analyzer.java.SourceMeterJavaMetrics.SM_JAVA_LOGICAL_LEVEL1;
import static com.sourcemeter.analyzer.java.SourceMeterJavaMetrics.SM_JAVA_LOGICAL_LEVEL2;
import static com.sourcemeter.analyzer.java.SourceMeterJavaMetrics.SM_JAVA_LOGICAL_LEVEL3;

public class SourceMeterJavaSensor extends SourceMeterSensor {

    /**
     * Command and parameters for running SourceMeter Java analyzer
     */
    private final FileSystem fileSystem;

    private static final Logger LOG = LoggerFactory.getLogger(SourceMeterJavaSensor.class);
    private static final String THRESHOLD_PROPERTIES_PATH = "/threshold_properties.xml";
    private static final String LOGICAL_ROOT = "__LogicalRoot__";

    /**
     * Constructor: Use of IoC to get Settings
     */
    public SourceMeterJavaSensor(FileSystem fileSystem,
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
        boolean skipJava = FileHelper.getBooleanFromConfiguration(this.configuration, "sm.java.skipToolchain");
        if (skipJava) {
            LOG.info("SourceMeter toolchain is skipped for Java. Results will be uploaded from former results directory, if it exists.");
        } else {
            if (!checkProperties(sensorContext)) {
                throw new RuntimeException("Failed to initialize the SourceMeter plugin. Some mandatory properties are not set properly.");
            }
            runSourceMeter(commands);
        }

        this.projectName = FileHelper.getStringFromConfiguration(this.configuration, "sonar.projectKey");
        this.projectName = StringUtils.replace(this.projectName, ":", "_");

        try {
            this.resultGraph = FileHelper.getSMSourcePath(configuration, fileSystem, '-', new Java())
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
        headerLicenseInformations.put("FaultHunter", "FaultHunter");
        headerLicenseInformations.put("RTEHunter", "RTEHunter");
        headerLicenseInformations.put("VulnerabilityHunter", "VulnerabilityHunter");
        headerLicenseInformations.put("MetricHunter", "MetricHunter");
        headerLicenseInformations.put("AndroidHunter", "AndroidHunter");
        headerLicenseInformations.put("DuplicatedCodeFinder", "Duplicated Code");
        headerLicenseInformations.put("LIM2Metrics", "Metrics");
        headerLicenseInformations.put("PMD2Graph", "PMD");
        headerLicenseInformations.put("FindBugs2Graph", "FindBugs");

        super.saveLicense(graph, sensorContext, headerLicenseInformations, SourceMeterJavaMetrics.JAVA_LICENSE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadDataFromGraphBin(String filename, InputProject project,
            SensorContext sensorContext) throws GraphlibException {
        Graph graph = new Graph();
        graph.loadBinary(filename);

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

            saveLicense(graph, sensorContext);

            if (componentRoot != null) {
                nodeCounter = new NodeCounterVisitor();
                GraphHelper.processGraph(graph, componentRoot, "ComponentTree", nodeCounter);
            }

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, LOGICAL_ROOT, "LogicalTree", nodeCounter);
            LogicalTreeLoaderVisitorJava logicalVisitor = new LogicalTreeLoaderVisitorJava(
                    this.fileSystem, this.configuration, sensorContext,
                    nodeCounter.getNumberOfNodes());

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, "__PhysicalRoot__", "PhysicalTree", nodeCounter);
            PhysicalTreeLoaderVisitorJava physicalVisitor = new PhysicalTreeLoaderVisitorJava(
                    this.fileSystem, sensorContext, nodeCounter.getNumberOfNodes());

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, LOGICAL_ROOT, "logicalTree", nodeCounter);
            LogicalTreeSaverVisitorJava logicalSaver = new LogicalTreeSaverVisitorJava(sensorContext, this.fileSystem, configuration);

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, "__CloneRoot__", "CloneTree", nodeCounter);
            CloneTreeSaverVisitorJava cloneSaver = new CloneTreeSaverVisitorJava(sensorContext, this.fileSystem);


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
            logicalSaver.saveLogicalTreeToDatabase(SM_JAVA_LOGICAL_LEVEL1, SM_JAVA_LOGICAL_LEVEL2, SM_JAVA_LOGICAL_LEVEL3);
            LOG.info("      * Saving LogicalTree done: " + logicalSaver.getLogicalTime() + MS);
            logicalSaver = null;

            LOG.info("      * Saving CloneTree...");
            GraphHelper.processGraph(graph, "__CloneRoot__", "CloneTree", cloneSaver);
            cloneSaver.saveCloneTreeToDatabase(SM_JAVA_CLONE_TREE);
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
        descriptor.onlyOnLanguage(Java.KEY);
    }

    /**
     * Checks the correctness of sourceMeter's properties.
     *
     * @param sensorContext Context of the sensor.
     * @return True if the properties were set correctly.
     */
    protected boolean checkProperties(SensorContext sensorContext) {
        String cleanResults = FileHelper.getStringFromConfiguration(configuration, "sm.cleanresults");
        if (cleanResults == null) {
            LOG.error("sonar.sourcemeter.cleanresults property cannot be null!");
            return false;
        }

        String pathToCA = FileHelper.getStringFromConfiguration(this.configuration, "sm.toolchaindir");
        if (pathToCA == null) {
            LOG.error("JAVA SourceMeter path must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String resultsDir = FileHelper.getStringFromConfiguration(this.configuration, "sm.resultsdir");
        if (resultsDir == null) {
            LOG.error("Results directory must be set! Check it on the settings page of your SonarQube!");
            return false;
        }

        String projectName = FileHelper.getStringFromConfiguration(this.configuration, "sonar.projectKey");
        projectName = StringUtils.replace(projectName, ":", "_");

        String filter = "";
        String softFilterFilePath = null;
        filter = getFilterContent(sensorContext, Java.KEY);
        try {
            softFilterFilePath = writeSoftFilterToFile(filter);
        } catch (IOException e) {
            LOG.warn("Cannot create filter file for toolchain! No filter is used during analysis.", e);
        }

        String binaries = FileHelper.getStringFromConfiguration(this.configuration, "sonar.java.binaries");
        String fbFile = null;

        if (binaries != null) {
            try {
                fbFile = generateFindBugsFile(binaries);
            } catch (IOException e) {
                LOG.warn("Could not generate input file for FindBugs. Binaries are not used during the analyzis.", e);
            }
        }

        this.commands.add(pathToCA + File.separator + Java.NAME + File.separator + "SourceMeterJava");

        this.commands.add("-resultsDir=" + resultsDir);
        this.commands.add("-projectName=" + projectName);



        ProfileInitializer profileInitializer = new ProfileInitializer(
                this.configuration, getMetricHunterCategories(), this.activeRules,
                new SourceMeterJavaRuleRepository(new RulesDefinitionXmlLoader()), new Java());

        String profilePath = this.fileSystem.workDir() + File.separator
                + "SM-Profile.xml";
        try {
            profileInitializer.generatePofileFile(profilePath);
            this.commands.add("-profileXML=" + profilePath);
        } catch (IOException e) {
            LOG.warn("An error occured while creating SourceMeter profile file. Default profile is used!", e);
        }

        String buildScript = FileHelper.getStringFromConfiguration(this.configuration, "sm.java.buildscript");
        if (buildScript != null) {
            this.commands.add("-buildScript=" + buildScript);
        }

        if (softFilterFilePath != null) {
            this.commands.add("-externalSoftFilter=" + softFilterFilePath);
        }

        String hardFilter = FileHelper.getStringFromConfiguration(this.configuration, "sm.java.hardFilter");
        if (hardFilter != null) {
            this.commands.add("-externalHardFilter=" + hardFilter);
        }

        String javacOptions = FileHelper.getStringFromConfiguration(this.configuration, "sm.java.javacOptions");
        String externalLibraries = FileHelper.getStringFromConfiguration(this.configuration, "sonar.java.libraries");
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
        }

        String vhMaxDepth = FileHelper.getStringFromConfiguration(this.configuration, "sm.java.vhMaxDepth");
        if (vhMaxDepth != null) {
            this.commands.add("-VHMaxDepth=" + vhMaxDepth);
        }

        String maxMem = FileHelper.getStringFromConfiguration(this.configuration, "sm.java.maxMem");
        if (maxMem != null) {
            this.commands.add("-JVMOptions=" + "-Xmx" + maxMem + "M");
        }

        String vhTimeout = FileHelper.getStringFromConfiguration(this.configuration, "sm.java.vhTimeOut");
        if (vhTimeout != null) {
            this.commands.add("-VHTimeout=" + vhTimeout);
        }

        String runVul = FileHelper.getStringFromConfiguration(this.configuration, "sm.java.runVulnerabilityHunter");
        if (runVul != null) {
            this.commands.add("-runVLH=" + runVul);
        }

        String runRTE = FileHelper.getStringFromConfiguration(this.configuration, "sm.java.runRTEHunter");
        if (runRTE != null) {
            this.commands.add("-runRTEHunter=" + runRTE);
        }

        String rhMaxState = FileHelper.getStringFromConfiguration(this.configuration, "sm.java.RHMaxState");
        if (rhMaxState != null) {
            this.commands.add("-RHMaxState=" + rhMaxState);
        }

        String rhMaxDepth = FileHelper.getStringFromConfiguration(this.configuration, "sm.java.RHMaxDepth");
        if (rhMaxDepth != null) {
            this.commands.add("-RHMaxDepth=" + rhMaxDepth);
        }

        String pmdOptions = FileHelper.getStringFromConfiguration(this.configuration, "sm.java.pmdOptions");
        if (pmdOptions != null) {
            this.commands.add("-pmdOptions=" + pmdOptions);
        }

        String csvSeparator = FileHelper.getStringFromConfiguration(this.configuration, "sm.java.csvSeparator");
        if (csvSeparator != null) {
            this.commands.add("-csvSeparator=" + csvSeparator);
        }

        this.commands.add("-cleanProject=true");
        this.commands.add("-cleanResults=" + cleanResults);

        if (fbFile != null) {
            this.commands.add("-FBFileList=" + fbFile);
            this.commands.add("-runFB=true");
        }

        String findBugsOptions = FileHelper.getStringFromConfiguration(this.configuration, "sm.java.fbOptions");
        if (findBugsOptions != null) {
            this.commands.add("-FBOptions=" + findBugsOptions);
        }

        addCommonCommandlineOptions();

        String additionalParameters = FileHelper.getStringFromConfiguration(this.configuration, "sm.java.toolchainOptions");
        if (additionalParameters != null) {
            this.commands.add(additionalParameters);
        }

        return true;
    }

    /**
     * Generates files for find bugs.
     *
     * @param binaries List of String.
     * @return String with bugs.
     * @throws IOException
     */
    private String generateFindBugsFile(String... binaries) throws IOException {
        String findBugsFilePath = this.fileSystem.workDir().getAbsolutePath()
                + File.separator + "FBFile.txt";
        File findBugsFile = new File(findBugsFilePath);
        if (!findBugsFile.exists() && !findBugsFile.createNewFile()) {
            throw new IOException("FindBugs file could not be created: "
                    + findBugsFilePath);
        }
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(findBugsFile.getAbsolutePath()),
                Charset.defaultCharset()))) {
            for (String binary : binaries) {
                bw.write(binary + "\n");
            }
        } catch (IOException e) {
            LOG.warn("Could not generate input file for FindBugs. Binaries are not used during the analyzis.", e);
        }

        return findBugsFile.getCanonicalPath();
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
            categories.add(new MetricHunterCategory("Class", ThresholdPropertiesHelper
                    .getClassThresholdMetrics(xmlFile)));
            xmlFile = getClass().getResourceAsStream(THRESHOLD_PROPERTIES_PATH);
            categories.add(new MetricHunterCategory("Interface", "class", ThresholdPropertiesHelper
                    .getClassThresholdMetrics(xmlFile)));
            xmlFile = getClass().getResourceAsStream(THRESHOLD_PROPERTIES_PATH);
            categories.add(new MetricHunterCategory("Enum", "class", ThresholdPropertiesHelper
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
