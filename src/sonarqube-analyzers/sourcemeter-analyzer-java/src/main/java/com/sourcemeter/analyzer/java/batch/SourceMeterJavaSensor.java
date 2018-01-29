/**
 * Copyright (c) 2014-2017, FrontEndART Software Ltd.
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
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.bootstrap.ProjectDefinition;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputModule;
import org.sonar.api.batch.rule.Rules;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Settings;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.scan.filesystem.FileExclusions;
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

import static com.sourcemeter.analyzer.java.SourceMeterJavaMetrics.SM_JAVA_CLONE_TREE;
import static com.sourcemeter.analyzer.java.SourceMeterJavaMetrics.SM_JAVA_LOGICAL_LEVEL1;
import static com.sourcemeter.analyzer.java.SourceMeterJavaMetrics.SM_JAVA_LOGICAL_LEVEL2;
import static com.sourcemeter.analyzer.java.SourceMeterJavaMetrics.SM_JAVA_LOGICAL_LEVEL3;

public class SourceMeterJavaSensor extends SourceMeterSensor {

    /**
     * Command and parameters for running SourceMeter Java analyzer
     */
    private final List<String> commands;
    private final Rules rules;
    private final FileSystem fileSystem;

    private static final Logger LOG = LoggerFactory.getLogger(SourceMeterJavaSensor.class);
    private static final String THRESHOLD_PROPERTIES_PATH = "/threshold_properties.xml";
    private static final String LOGICAL_ROOT = "__LogicalRoot__";

    /**
     * Constructor: Use of IoC to get Settings
     */
    public SourceMeterJavaSensor(FileExclusions fileExclusions, FileSystem fileSystem,
            ProjectDefinition projectDefinition, Rules rules, RulesProfile profile,
            Settings settings) {

        super(fileExclusions, fileSystem, projectDefinition, profile, settings);

        this.commands = new ArrayList<String>();
        this.rules = rules;
        this.fileSystem = fileSystem;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(SensorContext sensorContext) {
        boolean skipJava = this.settings.getBoolean("sm.java.skipToolchain");
        if (skipJava) {
            LOG.info("SourceMeter toolchain is skipped for Java. Results will be uploaded from former results directory, if it exists.");
        } else {
            if (!checkProperties()) {
                throw new RuntimeException("Failed to initialize the SourceMeter plugin. Some mandatory properties are not set properly.");
            }
            runSourceMeter(commands);
        }

        this.projectName = this.settings.getString("sonar.projectKey");
        this.projectName = StringUtils.replace(this.projectName, ":", "_");
        String analyseMode = this.settings.getString("sonar.analysis.mode");

        if ("incremental".equals(analyseMode)) {
            LOG.warn("Incremental mode is on. There are no metric based (INFO level) issues in this mode.");
            this.isIncrementalMode = true;
        }
        try {
            this.resultGraph = FileHelper.getSMSourcePath(settings, fileSystem, '-')
                    + File.separator + this.projectName + ".graph";
        } catch (IOException e) {
            LOG.error("Error during loading result graph path!", e);
        }

        long startTime = System.currentTimeMillis();
        LOG.info("      Graph: " + resultGraph);

        try {
            loadDataFromGraphBin(this.resultGraph, sensorContext.module(), sensorContext);
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
    protected void loadDataFromGraphBin(String filename, InputModule project,
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
                    this.fileSystem, this.settings, sensorContext,
                    nodeCounter.getNumberOfNodes());

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, "__PhysicalRoot__", "PhysicalTree", nodeCounter);
            PhysicalTreeLoaderVisitorJava physicalVisitor = new PhysicalTreeLoaderVisitorJava(
                    this.fileSystem, sensorContext, nodeCounter.getNumberOfNodes());

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, LOGICAL_ROOT, "logicalTree", nodeCounter);
            LogicalTreeSaverVisitorJava logicalSaver = new LogicalTreeSaverVisitorJava(sensorContext, this.fileSystem, settings);

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
     * @return True if the properties were set correctly.
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
                } else if (to.exists() && !to.delete()) {
                    LOG.error("Cannot delete " + to.getName() + "! " + this.toString() + " was cancelled.");
                    return false;
                }
            } catch (IOException e) {
                LOG.error("Cannot copy .gsi file! " + this.toString() + " was cancelled.");
            }
        }

        String filter = "";
        String softFilterFilePath = null;
        try {
            filter = getFilterContent();
            softFilterFilePath = writeSoftFilterToFile(filter);
        } catch (IOException e) {
            LOG.warn("Cannot create filter file for toolchain! No filter is used during analysis.", e);
        }

        String[] binaries = this.settings.getStringArray("sonar.java.binaries");
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
                new SourceMeterJavaRuleRepository(new RulesDefinitionXmlLoader()), rules);

        String profilePath = this.fileSystem.workDir() + File.separator
                + "SM-Profile.xml";
        try {
            profileInitializer.generatePofileFile(profilePath);
            this.commands.add("-profileXML=" + profilePath);
        } catch (IOException e) {
            LOG.warn("An error occured while creating SourceMeter profile file. Default profile is used!", e);
        }

        String baseDir = "";
        try {
            baseDir = this.fileSystem.baseDir().getCanonicalPath();
        } catch (IOException e) {
            LOG.warn("Could not get base directory's canonical path. Absolute path is used.");
            baseDir = this.fileSystem.baseDir().getAbsolutePath();
        }

        String buildScript = this.settings.getString("sm.java.buildscript");
        if (buildScript != null) {
            this.commands.add("-buildScript=" + buildScript);
        }

        this.commands.add("-projectBaseDir=" + baseDir);

        if (softFilterFilePath != null) {
            this.commands.add("-externalSoftFilter=" + softFilterFilePath);
        }

        String hardFilter = this.settings.getString("sm.java.hardFilter");
        if (hardFilter != null) {
            this.commands.add("-externalHardFilter=" + hardFilter);
        }

        String javacOptions = this.settings.getString("sm.java.javacOptions");
        String externalLibraries = this.settings.getString("sonar.java.libraries");
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

        String vhMaxDepth = this.settings.getString("sm.java.vhMaxDepth");
        if (vhMaxDepth != null) {
            this.commands.add("-VHMaxDepth=" + vhMaxDepth);
        }

        String maxMem = this.settings.getString("sm.java.maxMem");
        if (maxMem != null) {
            this.commands.add("-JVMOptions=" + "-Xmx" + maxMem + "M");
        }

        String vhTimeout = this.settings.getString("sm.java.vhTimeOut");
        if (vhTimeout != null) {
            this.commands.add("-VHTimeout=" + vhTimeout);
        }

        String runVul = this.settings.getString("sm.java.runVulnerabilityHunter");
        if (runVul != null) {
            this.commands.add("-runVLH=" + runVul);
        }

        String runRTE = this.settings.getString("sm.java.runRTEHunter");
        if (runRTE != null) {
            this.commands.add("-runRTEHunter=" + runRTE);
        }

        String rhMaxState = this.settings.getString("sm.java.RHMaxState");
        if (rhMaxState != null) {
            this.commands.add("-RHMaxState=" + rhMaxState);
        }

        String rhMaxDepth = this.settings.getString("sm.java.RHMaxDepth");
        if (rhMaxDepth != null) {
            this.commands.add("-RHMaxDepth=" + rhMaxDepth);
        }

        String pmdOptions = this.settings.getString("sm.java.pmdOptions");
        if (pmdOptions != null) {
            this.commands.add("-pmdOptions=" + pmdOptions);
        }

        String csvSeparator = this.settings.getString("sm.java.csvSeparator");
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

        String findBugsOptions = this.settings.getString("sm.java.fbOptions");
        if (findBugsOptions != null) {
            this.commands.add("-FBOptions=" + findBugsOptions);
        }

        String additionalParameters = this.settings.getString("sm.java.toolchainOptions");
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
