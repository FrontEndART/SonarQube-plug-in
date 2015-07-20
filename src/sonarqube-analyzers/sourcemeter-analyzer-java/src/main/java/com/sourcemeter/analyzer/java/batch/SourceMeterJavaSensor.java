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
package com.sourcemeter.analyzer.java.batch;

import graphlib.Graph;
import graphlib.GraphlibException;
import graphlib.Node;
import graphlib.Node.NodeType;
import graphlib.VisitorException;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.sonar.api.utils.SonarException;

import com.sourcemeter.analyzer.base.batch.SourceMeterSensor;
import com.sourcemeter.analyzer.base.helper.FileHelper;
import com.sourcemeter.analyzer.base.helper.GraphHelper;
import com.sourcemeter.analyzer.base.visitor.NodeCounterVisitor;
import com.sourcemeter.analyzer.java.SourceMeterJavaMetrics;
import com.sourcemeter.analyzer.java.core.Java;
import com.sourcemeter.analyzer.java.visitor.CloneTreeLoaderVisitorJava;
import com.sourcemeter.analyzer.java.visitor.ComponentTreeLoaderVisitorJava;
import com.sourcemeter.analyzer.java.visitor.LogicalTreeLoaderVisitorJava;
import com.sourcemeter.analyzer.java.visitor.PhysicalTreeLoaderVisitorJava;

public class SourceMeterJavaSensor extends SourceMeterSensor {

    private static final Logger LOG = LoggerFactory.getLogger(SourceMeterJavaSensor.class);

    /**
     * Constructor: Use of IoC to get Settings
     */
    public SourceMeterJavaSensor(ModuleFileSystem moduleFileSystem, FileSystem fileSystem, Settings settings,
            ResourcePerspectives perspectives, RulesProfile rulesProfile) {
        super(moduleFileSystem, fileSystem, settings, perspectives, rulesProfile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldExecuteOnProject(Project project) {
        return fileSystem.hasFiles(this.fileSystem.predicates().hasLanguage(Java.KEY));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void analyse(Project project, SensorContext sensorContext) {
        this.projectName = this.settings.getString("sonar.projectKey");
        this.projectName = StringUtils.replace(this.projectName, ":", "_");
        String analyseMode = this.settings.getString("sonar.analysis.mode");

        if ("incremental".equals(analyseMode)) {
            LOG.warn("Incremental mode is on. There are no metric based (INFO level) issues in this mode.");
            this.isIncrementalMode = true;
        }
        this.resultGraph = FileHelper.getSMSourcePath(settings, fileSystem, '-')
                + File.separator + this.projectName + ".graph";

        long startTime = System.currentTimeMillis();
        LOG.info("      Graph: " + resultGraph);

        try {
            loadDataFromGraphBin(this.resultGraph, project, sensorContext);
        } catch (GraphlibException e) {
            throw new SonarException("Error during graph loading!", e);
        }

        LOG.info("    Load data from graph bin and save resources and metrics done: " + (System.currentTimeMillis() - startTime) + MS);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    private void saveLicense(Graph graph, SensorContext sensorContext) {
        Map<String, String> headerLicenseInformations = new HashMap<String, String>();
        headerLicenseInformations.put("FaultHunter", "FaultHunter");
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
     * Load result graph binary
     *
     * @param filename
     * @param project
     * @param sensorContext
     * @throws GraphlibException
     */
    @Override
    protected void loadDataFromGraphBin(String filename, Project project,
            SensorContext sensorContext) throws GraphlibException {
        Graph graph = new Graph();
        graph.loadBinary(filename);

        Node componentRoot = null;
        ComponentTreeLoaderVisitorJava componentVisitor = null;
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
                componentVisitor = new ComponentTreeLoaderVisitorJava(
                        this.fileSystem, this.perspectives, project,
                        sensorContext, nodeCounter.getNumberOfNodes());
            }

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, "__LogicalRoot__", "LogicalTree", nodeCounter);
            LogicalTreeLoaderVisitorJava logicalVisitor = new LogicalTreeLoaderVisitorJava(
                    this.fileSystem, this.settings, this.perspectives, project,
                    sensorContext, nodeCounter.getNumberOfNodes());

            nodeCounter = new NodeCounterVisitor();
            GraphHelper.processGraph(graph, "__PhysicalRoot__", "PhysicalTree", nodeCounter);
            PhysicalTreeLoaderVisitorJava physicalVisitor = new PhysicalTreeLoaderVisitorJava(
                    this.fileSystem, this.perspectives, project, sensorContext,
                    nodeCounter.getNumberOfNodes());

            CloneTreeLoaderVisitorJava cloneVisitor = null;
            if (!this.isIncrementalMode) {
                nodeCounter = new NodeCounterVisitor();
                GraphHelper.processGraph(graph, "__CloneRoot__", "CloneTree", nodeCounter);
                cloneVisitor = new CloneTreeLoaderVisitorJava(this.fileSystem,
                        this.perspectives, project, sensorContext,
                        nodeCounter.getNumberOfNodes());
            }

            LOG.info("      * Initialization done: " + (System.currentTimeMillis() - startTime) + MS);

            if (componentVisitor != null) {
                LOG.info("      * Start processing ComponentTree...");
                GraphHelper.processGraph(graph, componentRoot, "ComponentTree",
                        componentVisitor);
                LOG.info("      * ComponentTree processing time: " + componentVisitor.getComponentTime() + MS);
                componentVisitor = null;
            }
            LOG.info("      * Processing LogicalTree...");
            GraphHelper.processGraph(graph, "__LogicalRoot__", "LogicalTree", logicalVisitor);
            LOG.info("      * Processing LogicalTree done: " + logicalVisitor.getLogicalTime() + MS);
            logicalVisitor = null;

            LOG.info("      * Processing PhysicalTree...");
            GraphHelper.processGraph(graph, "__PhysicalRoot__", "PhysicalTree", physicalVisitor);
            LOG.info("      * Processing PhysicalTree done: " + physicalVisitor.getFileTime() + MS);
            physicalVisitor = null;

            if (!this.isIncrementalMode) {
                LOG.info("      * Start processing CloneTree...");
                GraphHelper.processGraph(graph, "__CloneRoot__", "CloneTree", cloneVisitor);
                LOG.info("      * CloneTree processing time: " + cloneVisitor.getCloneTime() + MS);

                // Save duplications
                startTime = System.currentTimeMillis();
                LOG.info("    Save duplications...");

                Iterator<Entry<Resource, Set<String>>> dupIt = cloneVisitor.getDuplicationsMap().entrySet().iterator();
                while (dupIt.hasNext()) {
                    Map.Entry<Resource, Set<String>> pairs = dupIt.next();
                    Set<String> set = pairs.getValue();
                    StringBuffer tmp = new StringBuffer("<duplications>");
                    for (String cloneClass : set) {
                        tmp.append(cloneClass);
                    }
                    tmp.append("</duplications>");
                    sensorContext.saveMeasure(pairs.getKey(), new Measure(CoreMetrics.DUPLICATIONS_DATA, tmp.toString()));
                    dupIt.remove(); // avoids a ConcurrentModificationException
                }
                LOG.info("    Save duplications done: " + (System.currentTimeMillis() - startTime) + MS);
                cloneVisitor = null;
            }
        } catch (VisitorException e) {
            throw (SonarException) new SonarException(e.getMessage()).initCause(e);
        } finally {
            graph = null;
        }
    }
}
