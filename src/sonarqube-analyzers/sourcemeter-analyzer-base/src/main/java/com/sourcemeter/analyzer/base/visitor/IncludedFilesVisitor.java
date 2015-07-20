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

import graphlib.Edge;
import graphlib.Node;
import graphlib.VisitorException;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;

import com.sourcemeter.analyzer.base.helper.GraphHelper;

/**
 * Visitor class for visiting files in result graph and storing their paths.
 * With this information, not analyzed files can be filtered.
 */
public class IncludedFilesVisitor extends BaseVisitor {

    private long fileTime;
    private boolean emptyProject = false;

    private final Set<String> includedFiles;

    public IncludedFilesVisitor(FileSystem fileSystem) {
        super(null, null);

        this.fileTime = 0;
        this.includedFiles = new HashSet<>();

        FilePredicate mainFilePredicate = fileSystem.predicates().hasType(
                InputFile.Type.MAIN);

        if (!fileSystem.hasFiles(mainFilePredicate)) {
            this.emptyProject = true;
        }
    }

    /**
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
        String nodeType = node.getType().getType();

        if ("File".equals(nodeType) && nodeLongName != null) {
            includedFiles.add(nodeLongName.replace("\\", "/")
                    .toLowerCase(Locale.getDefault()));
        } else {
            return;
        }

        this.fileTime += (System.currentTimeMillis() - startTime);
    }

    public Set<String> getIncludedFiles() {
        return includedFiles;
    }

    @Override
    public void edgeVisitorFunc(Edge e) throws VisitorException {
    }
}
