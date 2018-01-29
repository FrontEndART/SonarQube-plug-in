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

import graphlib.Attribute;
import graphlib.AttributeString;
import graphlib.Node;
import graphlib.VisitorException;

import com.google.gson.Gson;
import com.sourcemeter.analyzer.base.helper.FileHelper;
import com.sourcemeter.analyzer.base.helper.VisitorHelper;
import com.sourcemeter.analyzer.base.jsontree.Position;
import com.sourcemeter.analyzer.base.jsontree.clone.CloneClass;
import com.sourcemeter.analyzer.base.jsontree.clone.CloneInstance;
import com.sourcemeter.analyzer.base.jsontree.clone.metrics.CloneClassMetrics;
import com.sourcemeter.analyzer.base.jsontree.clone.metrics.CloneInstanceMetrics;

public abstract class CloneTreeSaverVisitor extends BaseVisitor {

    private long fileTime;

    private List cloneClassesList;
    private List cloneInstanceList;

    public CloneTreeSaverVisitor(SensorContext sensorContext, VisitorHelper visitorHelper) {
        super(visitorHelper);

        this.sensorContext = sensorContext;

        cloneClassesList = new ArrayList<CloneClass>();
        cloneInstanceList = new ArrayList<CloneInstance>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postNodeVisitorFunc(Node node) throws VisitorException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preNodeVisitorFunc(Node node) throws VisitorException {
        long startTime = System.currentTimeMillis();
        String name = null;

        if (node.getType().equals(new Node.NodeType("CloneClass"))) {

            CloneClassMetrics cloneClassMetrics = new CloneClassMetrics();
            cloneInstanceList.clear();

            List<Attribute> attributeList = node.getAttributes();
            ListIterator<Attribute> it = attributeList.listIterator();

            while (it.hasNext()) {
                Attribute tempCloneClassAtt = it.next();
                if ("Name".equals(tempCloneClassAtt.getName())) {
                    name = ((AttributeString)tempCloneClassAtt).getValue();
                } else if ("metric".equals(tempCloneClassAtt.getContext())) {
                    readMetrics(tempCloneClassAtt, cloneClassMetrics);
                }
            }
                cloneClassesList.add(new CloneClass(name, cloneClassMetrics));
        } else if (node.getType().equals(new Node.NodeType("CloneInstance"))) {

            CloneInstanceMetrics cloneInstanceMetrics = new CloneInstanceMetrics();

            List positionsList = new ArrayList<Position>();
            List<Attribute> attributeList = node.getAttributes();
            ListIterator<Attribute> it = attributeList.listIterator();

            while (it.hasNext()){
                Attribute tempCloneInstanceAtt = it.next();
                if ("Name".equals(tempCloneInstanceAtt.getName())) {
                    name = ((AttributeString)tempCloneInstanceAtt).getValue();
                } else if ("Position".equals(tempCloneInstanceAtt.getName())) {
                    readPosition(tempCloneInstanceAtt, positionsList);
                } else if ("metric".equals(tempCloneInstanceAtt.getContext())) {
                    readMetrics(tempCloneInstanceAtt, cloneInstanceMetrics);
                }
            }

            cloneInstanceList.add(new CloneInstance(name, positionsList, cloneInstanceMetrics));

            /*
            * The cloneClassesList contains the CloneClasses
            * preNodeVisitorFunc use depth traversal, that means that each '[num]~CloneClass' typed node will followed by
            * his children '[num]~CloneInstance' nodes. This line gets the last element of the list and sets his children '[num]~CloneInstance'
            * each time if the current node type is '[num]~CloneInstance'.
            * cloneInstancesList is cleared (Line:92) each time when the current node type is '[num]~CloneClass'. Reason:
            * When we get '[num]~CloneClass' typed node, we can be assured, that we processed the previous '[num]~CloneClass' node's child '[num]~CloneInstance' node's,
            * because of the depth traversal.
            */
            ((CloneClass)cloneClassesList.get(cloneClassesList.size() - 1)).setInstances(cloneInstanceList);

        }
        this.fileTime += (System.currentTimeMillis() - startTime);
    }

    /**
     * Saves the cloneTree for the target metric in JSON format.
     *
     * @param metric Target metric.
     */
    public void saveCloneTreeToDatabase(Metric metric) {
        Gson gson = new Gson();

        FileHelper.saveGraphToDataBase(this.sensorContext,
                    gson.toJson(new CloneClass.CloneClasses(cloneClassesList)), metric);
    }

    /**
     * Time of saving the clone tree in JSON format
     *
     * @return execution time.
     */
    public long getFileTime() {
        return this.fileTime;
    }
}
