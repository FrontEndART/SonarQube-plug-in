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

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.AbstractLanguage;

import graphlib.Attribute;
import graphlib.AttributeString;
import graphlib.Node;
import graphlib.VisitorException;

import com.google.gson.Gson;
import com.sourcemeter.analyzer.base.helper.FileHelper;
import com.sourcemeter.analyzer.base.helper.VisitorHelper;
import com.sourcemeter.analyzer.base.jsontree.Position;
import com.sourcemeter.analyzer.base.jsontree.interfaces.MetricsInt;
import com.sourcemeter.analyzer.base.jsontree.logical.Level;
import com.sourcemeter.analyzer.base.jsontree.logical.LevelMetrics;
import com.sourcemeter.analyzer.base.jsontree.logical.LevelOne;
import com.sourcemeter.analyzer.base.jsontree.logical.Levels;
import com.sourcemeter.analyzer.base.jsontree.logical.metrics.base.LevelOneMetrics;

public abstract class LogicalTreeSaverVisitor extends BaseVisitor {

    protected boolean extendedMetrics;

    private long logicalTime;
    private String name;
    private List positionsList;

    private List levelOneList;
    private List levelTwoList;
    private List levelThreeList;

    private List<Node.NodeType> levelOneTypes;
    private List<Node.NodeType> levelTwoTypes;
    private List<Node.NodeType> levelThreeTypes;

    public LogicalTreeSaverVisitor(List<Node.NodeType> levelOneTypes,
            List<Node.NodeType> levelTwoTypes,
            List<Node.NodeType> levelThreeTypes,
            SensorContext sensorContext, VisitorHelper visitorHelper, AbstractLanguage pluginLnaguage) {
        super(visitorHelper, sensorContext.config(), pluginLnaguage);

        this.sensorContext = sensorContext;
        positionsList = new ArrayList<Position>();

        levelOneList = new ArrayList<LevelOne>();
        levelTwoList = new ArrayList<Level>();
        levelThreeList = new ArrayList<Level>();

        this.levelOneTypes = levelOneTypes;
        this.levelTwoTypes = levelTwoTypes;
        this.levelThreeTypes = levelThreeTypes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postNodeVisitorFunc(Node node) throws VisitorException {

    }

    /**
     * Extended version of preNodeVisitorFunc which saves more
     * metrics' values for the three logical level.
     *
     * @param node Node of the result graph.
     * @param levelOneMetrics Metrics for the first logical level.
     * @param levelTwoMetrics Metrics for the second logical level.
     * @param levelThreeMetrics Metrics for the third logical level.
     * @throws VisitorException
     */
    public void preNodeVisitorFunc(Node node, MetricsInt levelOneMetrics,
            MetricsInt levelTwoMetrics, MetricsInt levelThreeMetrics) throws VisitorException {
        long startTime = System.currentTimeMillis();
        Levels levels = new Levels(new LevelOne(), new Level(), new Level());

        if (levelOneTypes.contains(node.getType())
            || levelTwoTypes.contains(node.getType())
            || levelThreeTypes.contains(node.getType())) {

            name = null;
            positionsList.clear();

            List<Attribute> attributeList = node.getAttributes();
            ListIterator<Attribute> it = attributeList.listIterator();

            while (it.hasNext()) {
                Attribute temp = it.next();
                if ("Name".equals(temp.getName())) {
                    name = ((AttributeString) temp).getValue();
                } else if ("Position".equals(temp.getName()) && !levelOneTypes.contains(node.getType())) {
                    readPosition(temp, positionsList);
                } else if ("metric".equals(temp.getContext()) && levelOneTypes.contains(node.getType())) {
                    readMetrics(temp, levelOneMetrics);
                } else if ("metric".equals(temp.getContext()) && (levelTwoTypes.contains(node.getType()))) {
                    readMetrics(temp, levelTwoMetrics);
                } else if ("metric".equals(temp.getContext()) && (levelThreeTypes.contains(node.getType()))) {
                    readMetrics(temp, levelThreeMetrics);
                }
            }
            LevelMetrics levelMetrics = new LevelMetrics(levelOneMetrics, levelTwoMetrics, levelThreeMetrics);
            uploadList(node.getType(), levels, levelMetrics);
        }
        this.logicalTime += (System.currentTimeMillis() - startTime);
    }

    /**
     * Sets the logical level name and uploads the metrics.
     *
     * @param nodeType Type of the node.
     * @param levels Container of the three level.
     * @param levelMetrics Metrics of the actual level.
     */
    private void uploadList(Node.NodeType nodeType, Levels levels, LevelMetrics levelMetrics) {
        if (levelOneTypes.contains(nodeType)) {
            levels.getLevelOne().setName(name);
            levels.getLevelOne().setMetrics(levelMetrics.getLevelOneMetrics());
            levelOneList.add(levels.getLevelOne());
        } else if (levelTwoTypes.contains(nodeType)) {
            levels.getLevelTwo().setName(name);
            levels.getLevelTwo().setPositions(positionsList);
            levels.getLevelTwo().setMetrics(levelMetrics.getLevelTwoMetrics());
            levelTwoList.add(levels.getLevelTwo());
        } else if (levelThreeTypes.contains(nodeType)) {
            levels.getLevelThree().setName(name);
            levels.getLevelThree().setPositions(positionsList);
            levels.getLevelThree().setMetrics(levelMetrics.getLevelThreeMetrics());
            levelThreeList.add(levels.getLevelThree());
        }
    }

    /**
     * Extended version of preNodeVisitorFunc which saves less
     * metrics' values for the three logical level.
     *
     * @param node Node of the result graph.
     * @param levelTwoMetrics Metrics for the second logical level.
     * @param levelThreeMetrics Metrics for the third logical level.
     * @throws VisitorException
     */
    public void preNodeVisitorFunc(Node node,
            MetricsInt levelTwoMetrics, MetricsInt levelThreeMetrics) throws VisitorException {
        preNodeVisitorFunc(node, new LevelOneMetrics(), levelTwoMetrics, levelThreeMetrics);
    }

    /**
     * Saving the logical trees in JSON format in the specified metrics.
     *
     * @param metricLvl1 Metrics for the first logical level.
     * @param metricLvl2 Metrics for the second logical level.
     * @param metricLvl3 Metrics for the third logical level.
     */
    public void saveLogicalTreeToDatabase(Metric metricLvl1, Metric metricLvl2, Metric metricLvl3) {
        Gson gson = new Gson();

        FileHelper.saveGraphToDataBase(this.sensorContext,
                    gson.toJson(new LevelOne.LevelOneContainer(levelOneList, levelOneTypes)), metricLvl1);
        FileHelper.saveGraphToDataBase(this.sensorContext,
                    gson.toJson(new Level.LevelContainer(levelTwoList, levelTwoTypes)), metricLvl2);
        if (super.uploadMethods) {
            FileHelper.saveGraphToDataBase(this.sensorContext,
                    gson.toJson(new Level.LevelContainer(levelThreeList, levelThreeTypes)), metricLvl3);
        }
    }

    /**
     * Time of saving the logical tree in JSON format.
     *
     * @return Execution time.
     */
    public long getLogicalTime() {
        return this.logicalTime;
    }
}
