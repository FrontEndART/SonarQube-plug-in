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

package com.sourcemeter.analyzer.base.jsontree.logical;

import java.util.List;

import graphlib.Node;

import com.sourcemeter.analyzer.base.jsontree.interfaces.LevelInt;
import com.sourcemeter.analyzer.base.jsontree.interfaces.MetricsInt;

/**
 * First logical level of the project.
 */
public class LevelOne implements LevelInt {
    public String name;
    public MetricsInt metrics;

    /**
     * Inner class which contains the types of the level and the array of levels.
     */
    public static class LevelOneContainer {
        public String[] levelTypes;
        public LevelOne[] level;

        public LevelOneContainer(List list, List<Node.NodeType> typesList) {
            LevelOne[] array = new LevelOne[list.size()];
            list.toArray(array);

            level = array;

            Node.NodeType[] arrayTypes = new Node.NodeType[typesList.size()];
            typesList.toArray(arrayTypes);

            String[] arrayTypesString = new String[arrayTypes.length];
            for (int i = 0; i < arrayTypesString.length; i++) {
                arrayTypesString[i] = arrayTypes[i].getType();
            }

            levelTypes = arrayTypesString;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPositions(List positionList) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setMetrics(MetricsInt metrics) {
        this.metrics = metrics;
    }
}
