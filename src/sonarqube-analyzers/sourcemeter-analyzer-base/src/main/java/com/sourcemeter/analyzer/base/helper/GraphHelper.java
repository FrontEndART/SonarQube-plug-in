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
package com.sourcemeter.analyzer.base.helper;

import graphlib.Attribute;
import graphlib.AttributeComposite;
import graphlib.AttributeInt;
import graphlib.AttributeString;
import graphlib.Edge;
import graphlib.Edge.EdgeType;
import graphlib.Edge.EdgeTypeSet;
import graphlib.Edge.eDirectionType;
import graphlib.Graph;
import graphlib.Node;
import graphlib.Visitor;
import graphlib.VisitorException;
import graphsupportlib.Metric;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphHelper {

    public static final String ATTR_REALIZATION_LEVEL = "RealizationLevel";

    private static final Logger LOG = LoggerFactory.getLogger(GraphHelper.class);

    /**
     * Position class extended with realization level information
     */
    public static class Position extends graphsupportlib.Metric.Position {
        public RealizationLevel realizationLevel;

        public Position(String path, int line, int col, int endline,
                int endcol, RealizationLevel realizationLevel) {
            super(path, line, col, endline, endcol);
            this.realizationLevel = realizationLevel;
        }
    }

    /**
     * Enum for storing realization levels for nodes.
     *
     */
    public enum RealizationLevel {
        DECLARATION("declaration"), DEFINITION("definition");

        private final String name;

        private RealizationLevel(String s) {
            name = s;
        }

        public boolean equalsName(String otherName) {
            return (otherName == null) ? false : name.equals(otherName);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Returns the first position of the given node, extended with
     * realizationLevel information.
     *
     * @param node
     * @return position
     */
    public static Position getFirstPositionAttributeWithRealizationLevel(Node node) {
        AttributeComposite posAttr = (AttributeComposite) Metric
                .getNodeAttribute(node, Metric.ATTR_POSITION);
        if (posAttr == null) {
            return null;
        } else {
            String path = null;
            int line = 0, col = 0, endline = 0, endcol = 0;
            RealizationLevel realizationLevel = RealizationLevel.DECLARATION;

            for (Attribute attr : posAttr.getAttributes()) {
                if (Metric.ATTR_PATH.equals(attr.getName())) {
                    path = ((AttributeString) attr).getValue();
                } else if (Metric.ATTR_LINE.equals(attr.getName())) {
                    line = ((AttributeInt) attr).getValue();
                } else if (Metric.ATTR_COLUMN.equals(attr.getName())) {
                    col = ((AttributeInt) attr).getValue();
                } else if (Metric.ATTR_ENDLINE.equals(attr.getName())) {
                    endline = ((AttributeInt) attr).getValue();
                } else if (Metric.ATTR_ENDCOLUMN.equals(attr.getName())) {
                    endcol = ((AttributeInt) attr).getValue();
                } else if (ATTR_REALIZATION_LEVEL.equals(attr.getName())) {
                    if (RealizationLevel.DEFINITION.toString().equals(
                            ((AttributeString) attr).getValue())) {
                        realizationLevel = RealizationLevel.DEFINITION;
                    }

                }
            }
            return new Position(path, line, col, endline, endcol,
                    realizationLevel);
        }
    }


    /**
     * Check if the node is Java class or not
     *
     * @param node
     * @return true if node type is Class or Interface or Enum
     */
    public static boolean isClass(Node node) {
        if (node == null) {
            return false;
        }

        if ("Class".equals(node.getType().getType())
                || "Interface".equals(node.getType().getType())
                || "Enum".equals(node.getType().getType())
                || "Structure".equals(node.getType().getType())
                || "Union".equals(node.getType().getType())) {
            return true;
        }
        return false;
    }

    /**
     * Gives back the long name attribute of a node.
     *
     * @param node
     * @return long name
     */
    public static String getNodeLongNameAttribute(Node node) {
        AttributeString longName = (AttributeString) Metric.getNodeAttribute(
                node, "LongName");
        if (longName == null) {
            return null;
        } else {
            return longName.getValue();
        }
    }

    /**
     * Gives back the long name attribute of a node.
     *
     * @param node
     * @return name
     */
    public static String getNodeNameAttribute(Node node) {
        AttributeString name = (AttributeString) Metric.getNodeAttribute(node,
                "Name");
        if (name == null) {
            return null;
        } else {
            return name.getValue();
        }
    }

    /**
     * Gives back the unique ID of a node
     *
     * @param node
     * @return UUID
     */
    public static String getNodeTUID(Node node) {
        Attribute attribute = Metric.getNodeAttribute(node, "TUID");
        if (attribute == null) {
            return null;
        }
        return ((AttributeString) attribute).getValue();
    }

    /**
     * Gives back the parent of a node
     *
     * @param node
     * @return parent node
     */
    public static Node getParentNode(Node node) {
        List<Edge> edges = node.getOutEdges();
        for (Edge cloneEdge : edges) {
            if ("LogicalTree".equals(cloneEdge.getType().getType()) && eDirectionType.edtReverse.equals(cloneEdge.getType().getDirectionType())) {
                return cloneEdge.getToNode();
            }
        }
        return null;
    }

    /**
     * Return the node that is declared by the given node.
     *
     * @param node
     * @return
     */
    public static Node getDeclaresNode(Node node) {
        List<Edge> edges = node.getOutEdges();
        for (Edge edge : edges) {
            if ("Declares".equals(edge.getType().getType())) {
                return edge.getToNode();
            }
        }
        return null;
    }

    /**
     * Process graph
     *
     * @param graph
     * @param root
     * @param edgeType
     * @param visitor
     * @throws VisitorException
     * @throws ColumbusRuntimeException
     */
    public static void processGraph(Graph graph, String root, String edgeType,
            Visitor visitor) throws VisitorException {
        Node rootNode = graph.findNode(root);

        if (null != rootNode) {
            processGraph(graph, rootNode, edgeType, visitor);
        } else {
            LOG.warn("TreeRoot [" + root + "] not found!");
        }
    }

    /**
     * Process graph
     *
     * @param graph
     * @param rootNode
     * @param edgeType
     * @param visitor
     * @throws VisitorException
     * @throws ColumbusRuntimeException
     */
    public static void processGraph(Graph graph, Node rootNode, String edgeType,
            Visitor visitor) throws VisitorException {

        if (rootNode != null) {
            EdgeTypeSet treeEdges = new EdgeTypeSet();
            treeEdges.add(new EdgeType(edgeType, Edge.eDirectionType.edtDirectional));
            graph.traverseDepthFirst(rootNode, treeEdges, visitor);
        } else {
            LOG.warn("TreeRoot is invalid!");
        }
    }
}
