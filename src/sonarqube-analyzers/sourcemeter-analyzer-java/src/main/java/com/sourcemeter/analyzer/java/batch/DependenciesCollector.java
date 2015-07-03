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

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.apache.maven.shared.dependency.tree.filter.AncestorOrSelfDependencyNodeFilter;
import org.apache.maven.shared.dependency.tree.filter.DependencyNodeFilter;
import org.apache.maven.shared.dependency.tree.filter.StateDependencyNodeFilter;
import org.apache.maven.shared.dependency.tree.traversal.BuildingDependencyNodeVisitor;
import org.apache.maven.shared.dependency.tree.traversal.CollectingDependencyNodeVisitor;
import org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor;
import org.apache.maven.shared.dependency.tree.traversal.FilteringDependencyNodeVisitor;
import org.sonar.api.batch.Initializer;
import org.sonar.api.batch.SupportedEnvironment;
import org.sonar.api.resources.Project;
import org.sonar.api.utils.SonarException;

@SupportedEnvironment("maven")
public class DependenciesCollector extends Initializer {

    private final ArtifactRepository localRepository;
    private final ArtifactFactory artifactFactory;
    private final ArtifactMetadataSource artifactMetadataSource;
    private final ArtifactCollector artifactCollector;
    private final DependencyTreeBuilder treeBuilder;
    private final MavenProject mavenProject;
    private static StringBuffer mavenClassPath = new StringBuffer();

    public DependenciesCollector(ArtifactRepository localRepository, ArtifactFactory artifactFactory, ArtifactMetadataSource artifactMetadataSource,
            ArtifactCollector artifactCollector, DependencyTreeBuilder treeBuilder, MavenProject mavenProject) {
        this.localRepository = localRepository;
        this.artifactFactory = artifactFactory;
        this.artifactMetadataSource = artifactMetadataSource;
        this.artifactCollector = artifactCollector;
        this.treeBuilder = treeBuilder;
        this.mavenProject = mavenProject;

    }

    public static String getClassPath() {
        if (mavenClassPath.toString().isEmpty()) {
            return null;
        }
        return "-cp \"." + mavenClassPath + "\"";
    }

    private void saveDependency(DependencyNode node) {
        Artifact artifact = node.getArtifact();
        File file = artifact.getFile();
        if (file != null) {
            mavenClassPath.append(File.pathSeparator + file.getAbsolutePath());
        }
    }

    @Override
    public void execute(Project project) {
        try {
            DependencyNode root = treeBuilder.buildDependencyTree(mavenProject, localRepository, artifactFactory, artifactMetadataSource, null,
                    artifactCollector);

            DependencyNodeVisitor visitor = new BuildingDependencyNodeVisitor(new DependencyNodeVisitor() {

                @Override
                public boolean visit(DependencyNode node) {
                    return true;
                }

                @Override
                public boolean endVisit(DependencyNode node) {
                    if (node.getParent() != null && !node.getParent().equals(node)) {
                        saveDependency(node);
                    }
                    return true;
                }
            });

            // mode verbose OFF : do not show the same lib many times
            DependencyNodeFilter filter = StateDependencyNodeFilter.INCLUDED;

            CollectingDependencyNodeVisitor collectingVisitor = new CollectingDependencyNodeVisitor();
            DependencyNodeVisitor firstPassVisitor = new FilteringDependencyNodeVisitor(collectingVisitor, filter);
            root.accept(firstPassVisitor);

            DependencyNodeFilter secondPassFilter = new AncestorOrSelfDependencyNodeFilter(collectingVisitor.getNodes());
            visitor = new FilteringDependencyNodeVisitor(visitor, secondPassFilter);

            root.accept(visitor);

        } catch (DependencyTreeBuilderException e) {
            throw new SonarException("Can not load the graph of dependencies of the project " + project.getKey(), e);
        }
    }
}
