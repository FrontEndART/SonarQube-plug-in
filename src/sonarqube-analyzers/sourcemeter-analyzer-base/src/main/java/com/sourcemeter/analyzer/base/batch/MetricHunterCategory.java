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
package com.sourcemeter.analyzer.base.batch;

import java.util.List;

import org.sonar.api.measures.Metric;

/**
 * Class for storing categories for MetricHunter's generated threshold file.
 */
public class MetricHunterCategory {

    /**
     * The name of this category.
     */
    private String categoryName;

    /**
     * List of metrics in this category.
     */
    private List<Metric> metrics;

    /**
     * property name for this category.
     */
    private String propertyName;

    /**
     * Creates a category with it's name and it's list of metrics. property's
     * name will be the same as the category's name.
     *
     * @param metrics
     * @param categoryName
     */
    public MetricHunterCategory(String categoryName, List<Metric> metrics) {
        this.metrics = metrics;
        this.categoryName = categoryName;
        this.propertyName = categoryName;
    }

    /**
     * Creates a category with it's name, it's list of metrics and the
     * property's name.
     *
     * @param categoryName
     * @param propertyName
     * @param metrics
     */
    public MetricHunterCategory(String categoryName, String propertyName,
            List<Metric> metrics) {
        this.metrics = metrics;
        this.categoryName = categoryName;
        this.propertyName = propertyName;
    }

    public List<Metric> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getpropertyName() {
        return propertyName;
    }

    public void setpropertyName(String propertyName) {
        this.propertyName = propertyName;
    }
}
