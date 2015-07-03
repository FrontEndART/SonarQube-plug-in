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
package com.sourcemeter.analyzer.cpp.visitor;

import graphlib.Edge;
import graphlib.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.utils.SonarException;

import com.sourcemeter.analyzer.base.helper.FileHelper;
import com.sourcemeter.analyzer.base.helper.GraphHelper;
import com.sourcemeter.analyzer.base.helper.GraphHelper.Position;
import com.sourcemeter.analyzer.base.helper.GraphHelper.RealizationLevel;
import com.sourcemeter.analyzer.base.visitor.LogicalTreeLoaderVisitor;
import com.sourcemeter.analyzer.cpp.core.resources.ClassData;
import com.sourcemeter.analyzer.cpp.core.resources.CppClass;
import com.sourcemeter.analyzer.cpp.core.resources.CppFunction;
import com.sourcemeter.analyzer.cpp.core.resources.CppMethod;
import com.sourcemeter.analyzer.cpp.helper.VisitorHelperCpp;

public class LogicalTreeLoaderVisitorCpp extends LogicalTreeLoaderVisitor {

    private final Map<Resource, List<ClassData>> classesInFiles;
    private final Map<Resource, List<ClassData>> methodsInFiles;
    private final Map<Resource, List<ClassData>> filePathsForClasses;
    private final Map<Resource, List<ClassData>> filePathsForMethods;
    private final Map<String, Map<String, Resource>> resourceDeclarationsWithoutDefinition;
    private final boolean skipTUID;

    public static final Map<Resource, Integer> FUNCTIONS_FOR_FILES = new HashMap<Resource, Integer>();

    public LogicalTreeLoaderVisitorCpp(FileSystem fileSystem, Settings settings,
            ResourcePerspectives perspectives, Project project,
            SensorContext sensorContext, long numOfNodes) {

        super(fileSystem, settings, perspectives, project, sensorContext,
                numOfNodes, new VisitorHelperCpp(project, sensorContext,
                perspectives, settings, fileSystem));

        this.classesInFiles = new HashMap<Resource, List<ClassData>>();
        this.methodsInFiles = new HashMap<Resource, List<ClassData>>();
        this.filePathsForClasses = new HashMap<Resource, List<ClassData>>();
        this.filePathsForMethods = new HashMap<Resource, List<ClassData>>();
        this.resourceDeclarationsWithoutDefinition = new HashMap<>();

        this.skipTUID = settings.getBoolean("sm.cpp.skipTUID");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void edgeVisitorFunc(Edge e) {
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void preNodeVisitorFunc(Node node) {
        if (this.emptyProject) {
            return;
        }

        long startLogicalTime = System.currentTimeMillis();
        Position nodePosition = GraphHelper.getFirstPositionAttributeWithRealizationLevel(node);
        Position definitionPosition = nodePosition;

        if (nodePosition == null) {
            return;
        }

        Resource resource = null;
        Resource file = null;
        Resource definitonFile = null;
        boolean hasDefinition = (RealizationLevel.DEFINITION == nodePosition.realizationLevel);

        file = FileHelper.getIndexedFileForFilePath(this.fileSystem,
                this.sensorContext, this.project, nodePosition.path);
        if (file == null) {
            return;
        }

        definitonFile = file;

        if (RealizationLevel.DECLARATION == nodePosition.realizationLevel) {
            Node definitionNode = GraphHelper.getDeclaresNode(node);
            if (definitionNode != null) {
                node = definitionNode;
                hasDefinition = true;
                definitionPosition = GraphHelper.getFirstPositionAttributeWithRealizationLevel(definitionNode);
                if (definitionPosition != null) {
                    definitonFile = FileHelper.getIndexedFileForFilePath(
                            this.fileSystem, this.sensorContext, this.project,
                            definitionPosition.path);
                }
            }
        }

        if (definitonFile == null) {
            return;
        }

        String nodeType = node.getType().getType();
        String nodeLongName = GraphHelper.getNodeLongNameAttribute(node);
        String nodeName = GraphHelper.getNodeNameAttribute(node);

        String nodeTUID = GraphHelper.getNodeTUID(node);

        if (nodeTUID == null) {
            String warningMessage = "A node has no TUID attribute: "
                    + nodeLongName + ", UID: " + node.getUID();

            if (skipTUID) {
                LOG.warn(warningMessage);
                return;
            } else {
                throw new SonarException(warningMessage);
            }
        }

        if (GraphHelper.isClass(node)) {
            resource = new CppClass(nodeTUID, nodeName,
                    nodeLongName).setParent(definitonFile);
            if (!hasDefinition) {
                resource = addResourceWithoutDefinition(nodeType, nodeLongName, resource);
            }
            Resource indexedResource = this.sensorContext.getResource(resource);

            if (indexedResource == null) {
                if (!indexResource(node, resource, definitonFile, definitionPosition)) {
                    return;
                }
            } else {
                resource = indexedResource;
            }
            ClassData classData = new ClassData(resource.getId(), nodeName,
                    resource.getQualifier(), nodePosition.line, nodePosition.endline);
            storeFileForClass(file, classData, (CppClass) resource);
        } else if (this.uploadMethods && "Method".equals(nodeType)) {
            Node parentNode = GraphHelper.getParentNode(node);
            if (GraphHelper.isClass(parentNode)) {
                String parentNodeName = GraphHelper
                        .getNodeNameAttribute(parentNode);

                CppClass parentClass = new CppClass(
                        GraphHelper.getNodeTUID(parentNode), parentNodeName,
                        GraphHelper.getNodeLongNameAttribute(parentNode));
                resource = new CppMethod(nodeTUID,
                        nodeName, nodeLongName).setParent(parentClass);
                if (!hasDefinition) {
                    resource = addResourceWithoutDefinition(nodeType, nodeLongName, resource);
                }
                Resource indexedResource = this.sensorContext
                        .getResource(resource);

                if (indexedResource == null) {
                    if (!indexResource(node, resource, parentClass, definitionPosition)) {
                        return;
                    }
                } else {
                    resource = indexedResource;
                }

                ClassData classData = new ClassData(resource.getId(), nodeName,
                        resource.getQualifier(), nodePosition.line, nodePosition.endline);
                storeFileForMethod(file, classData, resource);
            }

        } else if (this.uploadMethods && "Function".equals(nodeType)) {
            resource = new CppFunction(nodeTUID, nodeName,
                    nodeLongName).setParent(definitonFile);
            if (!hasDefinition) {
                resource = addResourceWithoutDefinition(nodeType, nodeLongName, resource);
            }
            Resource indexedResource = this.sensorContext.getResource(resource);
            if (indexedResource == null) {
                if (!indexResource(node, resource, definitonFile, definitionPosition)) {
                    return;
                }
            } else {
                resource = indexedResource;
            }
            ClassData classData = new ClassData(resource.getId(), nodeName,
                    resource.getQualifier(), nodePosition.line, nodePosition.endline);
            storeFileForMethod(file, classData, resource);
        }

        if (nodePosition.realizationLevel == RealizationLevel.DEFINITION || file == definitonFile) {
            uploadMetricsAndWarnings(node, resource, nodePosition, true);
            if ("Function".equals(nodeType) || "Method".equals(nodeType)) {
                incFunctionToFile(file);
            }
        }

        this.logicalTime += (System.currentTimeMillis() - startLogicalTime);
    }

    /**
     * Stores a resource that has no definitions if one of the declarations
     * hasn't been stored yet.
     *
     * @param nodeType
     * @param longName
     * @param resource
     *
     * @return the actual resource, if one of it's similar declarations hasn't
     *         been indexed yet. Returns the indexed resource otherwise.
     */
    private Resource addResourceWithoutDefinition(String nodeType,
            String longName, Resource resource) {
        Map<String, Resource> resourcesForNodeType = null;
        if (resourceDeclarationsWithoutDefinition.containsKey(nodeType)) {
            resourcesForNodeType = resourceDeclarationsWithoutDefinition.get(nodeType);
        } else {
            resourcesForNodeType = new HashMap<>();
            resourceDeclarationsWithoutDefinition.put(nodeType, resourcesForNodeType);
        }

        if (resourcesForNodeType.containsKey(longName)) {
            return resourcesForNodeType.get(longName);
        }

        resourcesForNodeType.put(longName, resource);

        return resource;
    }

    /**
     * Stores file data for classes
     *
     * @param file
     * @param classData
     * @param cppClass
     */
    private void storeFileForClass(Resource file, ClassData classData, CppClass cppClass) {
        file = this.sensorContext.getResource(file);
        if (file == null) {
            return;
        }
        putClassDataInResourceMap(this.classesInFiles, file, classData);

        ClassData filePath = new ClassData(file.getId(), file.getName(), file.getQualifier(),
                classData.getLine(), classData.getEndLine());
        putClassDataInResourceMap(this.filePathsForClasses, cppClass, filePath);
    }

    /**
     * Stores file data for methods
     *
     * @param classData
     * @param cppMethod
     */
    private void storeFileForMethod(Resource file, ClassData classData, Resource cppMethod) {
        file = this.sensorContext.getResource(file);
        if (file == null) {
            return;
        }
        putClassDataInResourceMap(this.methodsInFiles, file, classData);

        ClassData filePath = new ClassData(file.getId(), file.getName(), file.getQualifier(),
                classData.getLine(), classData.getEndLine());
        putClassDataInResourceMap(this.filePathsForMethods, cppMethod, filePath);
    }

    private void putClassDataInResourceMap(Map<Resource, List<ClassData>> resourceMap,
            Resource key, ClassData value) {
        List<ClassData> classDatas;
        if (resourceMap.containsKey(key)) {
            classDatas = resourceMap.get(key);
            classDatas.add(value);
        } else {
            classDatas = new ArrayList<ClassData>();
            classDatas.add(value);
            resourceMap.put(key, classDatas);
        }
    }

    private void incFunctionToFile(Resource file) {
        if (FUNCTIONS_FOR_FILES.containsKey(file)) {
            FUNCTIONS_FOR_FILES.put(file, FUNCTIONS_FOR_FILES.get(file) + 1);
        } else {
            FUNCTIONS_FOR_FILES.put(file, 1);
        }
    }

    public Map<Resource, List<ClassData>> getClassesInFiles() {
        return classesInFiles;
    }

    public Map<Resource, List<ClassData>> getFilePathsForClasses() {
        return filePathsForClasses;
    }

    public Map<Resource, List<ClassData>> getFilePathsForMethods() {
        return filePathsForMethods;
    }

    public Map<Resource, List<ClassData>> getMethodsInFiles() {
        return methodsInFiles;
    }
}
