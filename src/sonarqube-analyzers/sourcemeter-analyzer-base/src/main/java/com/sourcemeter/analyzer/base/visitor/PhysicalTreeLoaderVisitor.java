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

package com.sourcemeter.analyzer.base.visitor;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;

import graphlib.Node;

import com.sourcemeter.analyzer.base.helper.GraphHelper;
import com.sourcemeter.analyzer.base.helper.VisitorHelper;

/**
 * Visitor class for visiting and storing physical nodes from the result graph.
 */
public abstract class PhysicalTreeLoaderVisitor extends BaseVisitor {

    private long fileTime;

    private long numOfVisitedNodes;
    protected final long numOfNodes;
    private boolean emptyProject;
    private final FileSystem fileSystem;

    public PhysicalTreeLoaderVisitor(FileSystem fileSystem,
            SensorContext sensorContext, long numOfNodes,
            VisitorHelper visitorHelper) {
        super(visitorHelper);
        this.sensorContext = sensorContext;
        this.fileSystem = fileSystem;

        this.numOfNodes = numOfNodes;

        FilePredicate mainFilePredicate = fileSystem.predicates().hasType(InputFile.Type.MAIN);

        if (!fileSystem.hasFiles(mainFilePredicate)) {
            this.emptyProject = true;
        }
    }

    /**
     * Returns the time of processing the physical tree.
     *
     * @return execution time.
     */
    public long getFileTime() {
        return this.fileTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postNodeVisitorFunc(Node node) {
        if (this.isDebugMode) {
            this.numOfVisitedNodes++;
            printProgress(this.numOfVisitedNodes, this.numOfNodes, this.fileTime);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preNodeVisitorFunc(Node node) {
        if (this.emptyProject) {
            return;
        }

        long startTime = System.currentTimeMillis();
        String nodeLongName = GraphHelper.getNodeLongNameAttribute(node);

        InputFile file = null;
        String nodeType = node.getType().getType();

        if ("File".equals(nodeType)) {
            file = fileSystem.inputFile(fileSystem.predicates().hasPath(nodeLongName));
        } else {
            return;
        }

        uploadMetrics(node, file);
        this.fileTime += (System.currentTimeMillis() - startTime);
    }
}
