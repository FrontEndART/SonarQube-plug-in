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

package com.sourcemeter.analyzer.base.helper;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class GraphHelper {

    private static final Logger LOG = LoggerFactory.getLogger(GraphHelper.class);

    /**
     * Gives back the long name attribute of a node.
     *
     * @param node Node of the result graph.
     * @return Long name.
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
     * @param node Node of the result graph.
     * @return Name of the node.
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
     * Check whether the given has a declares edge or not.
     *
     * @param node Node of the result graph.
     * @return true if has declares edge, false otherwise.
     */
    public static boolean hasDeclaresEdge(Node node) {
        List<Edge> edges = node.getOutEdges();
        for (Edge edge : edges) {
            if ("Declares".equals(edge.getType().getType())
                    && eDirectionType.edtDirectional.equals(edge.getType().getDirectionType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Process graph.
     *
     * @param graph Result graph to be processed.
     * @param root Root of the result graph.
     * @param edgeType Type of the edges.
     * @param visitor Type of the visitor (ex.: logical, physical).
     * @throws VisitorException
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
     * Process graph.
     *
     * @param graph Result graph to be processed.
     * @param rootNode Root of the node in result graph.
     * @param edgeType Type of the edges.
     * @param visitor Type of the visitor (ex.: logical, physical).
     * @throws VisitorException
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
