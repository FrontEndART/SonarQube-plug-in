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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.measures.Metric;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ThresholdPropertiesHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ThresholdPropertiesHelper.class);

    private static final String TYPE = "type";

    /**
     * Reads the threshold properties for metrics from XML file.
     *
     * @param xmlFile InputStream of the XML file contains the properties for the thresholds.
     * @return List of PropertyDefinitions for the thresholds.
     */
    public static List<PropertyDefinition> readPropertiesFromXML(InputStream xmlFile) {

        List properties = new ArrayList<PropertyDefinition>();

        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("threshold");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    PropertyType type;

                    if ("INTEGER".equals(eElement.getAttribute(TYPE))) {
                        type = PropertyType.INTEGER;
                    } else if ("FLOAT".equals(eElement.getAttribute(TYPE))) {
                        type = PropertyType.FLOAT;
                    } else {
                        type = PropertyType.STRING;
                    }

                    PropertyDefinition newProperty = PropertyDefinition.builder(eElement.getAttribute("key"))
                            .category(eElement.getAttribute("category"))
                            .subCategory(eElement.getAttribute("domain"))
                            .name(eElement.getAttribute("name"))
                            .type(type)
                            .defaultValue(eElement.getAttribute("defaultValue"))
                            .build();

                    properties.add(newProperty);
                }
            }

        } catch (ParserConfigurationException e) {
            LOG.error("Error during reading properties!");
        } catch (SAXException e) {
            LOG.error("Error during reading properties!");
        } catch (IOException e) {
            LOG.error("Error during reading properties!");
        }

        return properties;
    }

    /**
     * Reads the metrics from the XML file.
     *
     * @param thresholdProperties InputStream of the XML file contains the metrics.
     * @param entity Entity of the metrics.
     * @return List of Metrics.
     */
    public static List<Metric> readThresholdsFromXml(InputStream thresholdProperties, String entity) {
        List metrics = new ArrayList<Metric>();

        List processedMetricKeys = new ArrayList<String>();

        Metric metric;
        String key;
        String name;
        Metric.ValueType type = null;
        String domain;

        try {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(thresholdProperties);

            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("threshold");

            for (int temp = 0; temp < nList.getLength(); temp++) {

                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;

                    String[] tokenize = eElement.getAttribute("key").split("\\.");

                    key = tokenize[4];

                    if (eElement.hasAttribute("entity") && eElement.getAttribute("entity").contains(entity) && !(processedMetricKeys.contains(key))) {
                        name = eElement.getAttribute("name");
                        if ("INTEGER".equals(eElement.getAttribute(TYPE))) {
                            type = Metric.ValueType.INT;
                        } else if ("FLOAT".equals(eElement.getAttribute(TYPE))) {
                            type = Metric.ValueType.FLOAT;
                        }
                        domain = eElement.getAttribute("domain");
                        if ("gt".equals(eElement.getAttribute("relation"))) {
                            metric = new Metric.Builder(key, name, type)
                                               .setDomain(domain)
                                               .create();
                        } else {
                            metric = new Metric.Builder(key, name, type)
                                               .setDirection(Metric.DIRECTION_BETTER)
                                               .setDomain(domain)
                                               .create();
                        }
                        processedMetricKeys.add(key);
                        metrics.add(metric);
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            LOG.error("Error during reading thresholds!");
        } catch (SAXException e) {
            LOG.error("Error during reading thresholds!");
        } catch (IOException e) {
            LOG.error("Error during reading thresholds!");
        }
        return metrics;
    }

    /**
     * Get metrics for class threshold violations.
     *
     * @return Class threshold metrics.
     */
    public static List<Metric> getClassThresholdMetrics(InputStream xmlFile) {
        return readThresholdsFromXml(xmlFile, "Class, Interface, Enum");
    }

    /**
     * Get metrics for method threshold violations.
     *
     * @return Method threshold metrics.
     */
    public static List<Metric> getMethodThresholdMetrics(InputStream thresholdProperties) {
        return readThresholdsFromXml(thresholdProperties, "Method");
    }

    /**
     * Get metrics for function threshold violations. By default these metrics
     * are the same as the method threshold violation metrics.
     *
     * @return Function threshold metrics.
     */
    public static List<Metric> getFunctionThresholdMetrics(InputStream thresholdProperties) {
        return getMethodThresholdMetrics(thresholdProperties);
    }

    /**
     * Get metrics for CloneClass threshold violations.
     *
     * @return CloneClass threshold metrics.
     */
    public static List<Metric> getCloneClassThresholdMetrics(InputStream thresholdProperties) {
        return readThresholdsFromXml(thresholdProperties, "CloneClass");
    }

    /**
     * Get metrics for CloneInstance threshold violations.
     *
     * @return CloneInstance threshold metrics.
     */
    public static List<Metric> getCloneInstanceThresholdMetrics(InputStream thresholdProperties) {
        return readThresholdsFromXml(thresholdProperties, "CloneInstance");
    }

}
