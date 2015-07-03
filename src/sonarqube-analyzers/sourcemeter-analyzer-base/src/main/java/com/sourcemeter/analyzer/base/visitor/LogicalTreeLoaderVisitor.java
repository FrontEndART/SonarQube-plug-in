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
package com.sourcemeter.analyzer.base.visitor;

import graphlib.Node;
import graphsupportlib.Metric.Position;
import com.sourcemeter.analyzer.base.batch.SourceMeterInitializer;
import com.sourcemeter.analyzer.base.helper.VisitorHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterCoreMetrics;

/**
 * Class for visiting and storing logical nodes from the result graph.
 */
public abstract class LogicalTreeLoaderVisitor extends BaseVisitor {

    final long numOfNodes;
    private long numOfVisitedNodes;

    protected boolean emptyProject;
    protected long logicalTime;
    protected final boolean uploadMethods;
    protected final FileSystem fileSystem;
    protected final Settings settings;

    protected static final Logger LOG = LoggerFactory.getLogger(LogicalTreeLoaderVisitor.class);

    public LogicalTreeLoaderVisitor(FileSystem fileSystem, Settings settings,
            ResourcePerspectives perspectives, Project project,
            SensorContext sensorContext, long numOfNodes,
            VisitorHelper visitorHelper) {
        super(perspectives, visitorHelper);
        this.project = project;
        this.fileSystem = fileSystem;
        this.sensorContext = sensorContext;
        this.settings = settings;

        this.logicalTime = 0;
        this.numOfNodes = numOfNodes;
        this.numOfVisitedNodes = 0;

        FilePredicate mainFilePredicate = fileSystem.predicates().hasType(InputFile.Type.MAIN);

        if (!fileSystem.hasFiles(mainFilePredicate)) {
            this.emptyProject = true;
        }

        this.uploadMethods = settings.getBoolean("sm." + SourceMeterInitializer.pluginLanguage.getKey() + ".uploadMethods");
    }

    /**
     * @return execution time.
     */
    public long getLogicalTime() {
        return this.logicalTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postNodeVisitorFunc(Node node) {
        if (this.isDebugMode) {
            this.numOfVisitedNodes++;
            printProgress(this.numOfVisitedNodes, this.numOfNodes, this.logicalTime);
        }
    }

    /**
     * Indexes a resource if it's not already indexed + saves metics and
     * warnings
     *
     * @param resource
     * @param parentResource
     * @param nodePosition
     */
    protected boolean indexResource(Node node, Resource resource,Resource parentResource,
            Position nodePosition) {
        Resource indexedResource = this.sensorContext.getResource(resource);
        if (null == indexedResource) {
            if (!this.sensorContext.index(resource, parentResource)) {
                return false;
            }
            this.sensorContext.saveMeasure(resource, new Measure(SourceMeterCoreMetrics.BEGIN_LINE, (double) nodePosition.line));
            this.sensorContext.saveMeasure(resource, new Measure(SourceMeterCoreMetrics.END_LINE, (double) nodePosition.endline));
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void preNodeVisitorFunc(Node node);
}
