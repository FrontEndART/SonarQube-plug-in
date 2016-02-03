/**
 * Copyright (c) 2014-2016, FrontEndART Software Ltd.
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
import graphlib.Attribute.aType;
import graphlib.AttributeComposite;
import graphlib.AttributeFloat;
import graphlib.AttributeInt;
import graphlib.AttributeString;
import graphlib.Node;
import graphsupportlib.Metric.Position;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.SonarException;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterMetricFinder;

import com.sourcemeter.analyzer.base.visitor.BaseVisitor;

/**
 * Helper class for Visitor classes to upload metrics and warnings.
 */
public abstract class VisitorHelper {

    public static final String METRIC_VIOLATION_CONTAINS = "_warning_";

    private static final Logger LOG = LoggerFactory.getLogger(VisitorHelper.class);
    protected static final String METRIC_PREFIX = "MET_";

    private final Project project;
    private final SensorContext sensorContext;
    private final ResourcePerspectives perspectives;
    private final SourceMeterMetricFinder metricFinder;
    protected final FileSystem fileSystem;

    public VisitorHelper(Project project, SensorContext sensorContext,
            ResourcePerspectives perspectives, FileSystem fileSystem,
            SourceMeterMetricFinder metricFinder) {
        this.project = project;
        this.sensorContext = sensorContext;
        this.perspectives = perspectives;
        this.metricFinder = metricFinder;
        this.fileSystem = fileSystem;
    }

    /**
     * Upload warnings for the given resource
     *
     * @param attribute
     * @param node
     * @param resource
     * @param nodePosition
     */
    public void uploadWarnings(Attribute attribute, Node node, Position nodePosition) {
        AttributeComposite warningAttribute = (AttributeComposite) attribute;
        int lineId = 0;
        String warningText = "";
        String warningPath = "";
        String stackTrace = "";

        List<Attribute> compAttributes = warningAttribute.getAttributes();
        for (Attribute a : compAttributes) {
            if ("Path".equals(a.getName())) {
                warningPath = ((AttributeString) a).getValue();
            } else if ("Line".equals(a.getName())) {
                lineId = ((AttributeInt) a).getValue();
            } else if ("WarningText".equals(a.getName())) {
                warningText = ((AttributeString) a).getValue();
            } else if ("ExtraInfo".equals(a.getName())) {
                stackTrace = getStackTraceFromWarningAttribute(
                        (AttributeComposite) a, warningText.length());
            }
        }

        Resource violationResource = FileHelper.getIndexedFileForFilePath(
                fileSystem, sensorContext, project, warningPath);
        if (violationResource == null || lineId == 0) {
            return;
        }

        Issuable issuable = this.perspectives.as(Issuable.class, violationResource);
        if (issuable != null) {
            String tmpRuleKey = warningAttribute.getName();
            warningText = getWarningTextWithPrefix(tmpRuleKey, warningText) + stackTrace;
            tmpRuleKey = getCorrectedRuleKey(tmpRuleKey);
            RuleKey ruleKey = RuleKey.of(getRuleKey(), tmpRuleKey);

            Issue issue = issuable.newIssueBuilder()
                    .ruleKey(ruleKey)
                    .message(warningText.toString())
                    .line(lineId)
                    .build();

            issuable.addIssue(issue);
        }
    }

    /**
     * Returns the language specific rule key.
     *
     * @return
     */
    public abstract String getRuleKey();

    /**
     * Returns the warning text with the corresponding prefix for the given rule
     * key.
     *
     * @param warningText
     * @return
     */
    public abstract String getWarningTextWithPrefix(String ruleKey, String warningText);

    /**
     * If a Ruleset metric for the current language is not uploaded, it will be
     * uploaded with 0 value.
     *
     * @param metrics
     */
    public void uploadEmptyRulesetMetricsByZero(Resource resource) {
        for (Metric metric : this.metricFinder.findLanguageSpecificRulesetMetrics()) {
            Measure measure = this.sensorContext.getMeasure(resource, metric);
            if (measure != null) {
                continue;
            }
            measure = new Measure(metric);
            measure.setValue(0.0);
            this.sensorContext.saveMeasure(resource, measure);
        }
    }

    /**
     * Upload metrics from the given attribute to the given resource.
     *
     * @param metricAttribute
     * @param resource
     */
    public void uploadMetrics(Attribute metricAttribute, Resource resource) {
        try {
            Measure measure = createMeasureFromAttribute(metricAttribute, resource);
            if (measure != null) {
                this.sensorContext.saveMeasure(resource, measure);
            }
        } catch (SonarException e) {
            LOG.warn(e.getMessage());
            if (resource != null) {
                LOG.warn("Resource: " + resource.getName());
            }
        }
    }

    /**
     * Creates and returns a measure from the given metric attribute for the
     * given resource.
     *
     * @param metricAttribute
     * @param resource
     * @return
     */
    public Measure createMeasureFromAttribute(Attribute metricAttribute, Resource resource) {
        aType metricType = metricAttribute.getType();
        Metric metric = this.metricFinder.findByKey(metricAttribute.getName());
        Measure measure = null;
        if (metric != null) {
            measure = this.sensorContext.getMeasure(resource, metric);
            if (measure == null) {
                measure = new Measure(metric);
            }
            if (metricType == aType.atInt) {
                int value = ((AttributeInt) metricAttribute).getValue();
                measure.setIntValue(value);
            } else if (metricType == aType.atFloat) {
                Double value = (double) ((AttributeFloat) metricAttribute)
                        .getValue();
                if (!value.isNaN() && !value.isInfinite()) {
                    if (metric.getType().equals(ValueType.PERCENT)) {
                        value = value * 100;
                    }
                    measure.setValue(value, BaseVisitor.DEFUALT_PRECISION);
                }
            }
        }

        return measure;
    }

    /**
     * If a rule key is a threshold violation in a graph, the key rule differs
     * from the stored rule in SonarQube. This method corrects it.
     *
     * @param ruleKey
     * @return
     */
    public String getCorrectedRuleKey(String ruleKey) {
        if (ruleKey.contains(METRIC_VIOLATION_CONTAINS)) {
            // class treshold violation
            ruleKey = METRIC_PREFIX + ruleKey.substring(0, ruleKey.indexOf(METRIC_VIOLATION_CONTAINS));
        } else {
            String[] splittedKey = ruleKey.split("_");
            ruleKey = splittedKey[splittedKey.length - 1];
        }

        return ruleKey;
    }

    /**
     * Method reads path attribute from the given node. In some analyzer
     * plugins, this method should be overridden for correcting the path
     * attribute.
     *
     * @return
     */
    public String getPathFromNode(Node node) {
        String path = null;
        Position position = graphsupportlib.Metric.getFirstPositionAttribute(node);

        if (null != position) {
            path = position.path;
        }

        return path;
    }

    /**
     * Creates the stack trace String from the given attribute.
     *
     * @param extraInfoAttribute
     *            the attribute that contains the stack trace information
     * @param warningTextLength
     *            length of the current warning text
     * @return a String, containing the stack trace information
     */
    public String getStackTraceFromWarningAttribute(
            AttributeComposite extraInfoAttribute, int warningTextLength) {
        if (!"ExtraInfo".equals(extraInfoAttribute.getName())) {
            return "";
        }

        StringBuffer stackTrace = new StringBuffer("<br/>Trace:<br/>");
        List<String> stackTraceList = null;
        int callStackDepth = 0;

//      Example:
//      <attribute type = "composite" name = "ExtraInfo" context = "">
//        <attribute type = "composite" name = "SourceLink" context = "">
//          <attribute type = "string" name = "Path" context = "" value =
//            "src/org/snipsnap/net/AddLabelServlet.java"/>
//          <attribute type = "int" name = "Line" context = "" value = "71"/>
//          <attribute type = "int" name = "Column" context = "" value = "0"/>
//          <attribute type = "int" name = "EndLine" context = "" value = "71"/>
//          <attribute type = "int" name = "EndColumn" context = "" value = "10000"/>
//          <attribute type = "int" name = "CallStackDepth" context = "" value = "0"/>
//        </attribute>
//      ...
//      </attribute>
        stackTraceList = new ArrayList<String>();
        List<Attribute> extraInfoAttributes = extraInfoAttribute.getAttributes();
        String previousPath = "";
        int previousLine = 0;
        for (Attribute sourceLink : extraInfoAttributes) {
            List<Attribute> sourceLinkAttributes = sourceLink.getAttributes();
            String path = "";
            int line = 0;
            for (Attribute sourceLinkAttribute : sourceLinkAttributes) {
                if ("Path".equals(sourceLinkAttribute.getName())) {
                    path = ((AttributeString) sourceLinkAttribute).getValue();
                } else if ("Line".equals(sourceLinkAttribute.getName())) {
                    line = ((AttributeInt) sourceLinkAttribute).getValue();
                } else if ("CallStackDepth".equals(sourceLinkAttribute
                        .getName())) {
                    callStackDepth = ((AttributeInt) sourceLinkAttribute)
                            .getValue();
                }
            }
            if (!previousPath.equals(path) || previousLine != line) {
                previousLine = line;
                previousPath = path;
                Resource tracePathResource = org.sonar.api.resources.File
                        .fromIOFile(new File(path), this.project);
                tracePathResource = sensorContext.getResource(tracePathResource);

                if (tracePathResource != null) {
                    StringBuffer pathLink = new StringBuffer();
                    pathLink.append("__");
                    pathLink.append(tracePathResource.getId());
                    pathLink.append(":");
                    pathLink.append(tracePathResource.getName());
                    pathLink.append(":");
                    pathLink.append(line);
                    pathLink.append(":");
                    if (callStackDepth > 0) {
                        pathLink.append("+");
                    }
                    pathLink.append(callStackDepth);
                    pathLink.append("__<br/>");
                    stackTraceList.add(pathLink.toString());
                }
                else
                {
                    LOG.warn ("File is not part of the project, skip from stack trace: " + path);
                }
            }
        }
        int sum = warningTextLength;
        int index = stackTraceList.size();
        ListIterator<String> stlIt = stackTraceList.listIterator(stackTraceList.size());
        while (stlIt.hasPrevious()) {
            String string = stlIt.previous();
            int length = string.length();
            if (sum + length > 3950) {
                break;
            } else {
                sum += length;
                index--;
            }
        }
        if (index > 0) {
            stackTrace.append("...<br/>");
        }
        stlIt = stackTraceList.listIterator(index);
        while (stlIt.hasNext()) {
            String string = stlIt.next();
            stackTrace.append(string);
        }

        return stackTrace.toString();
    }
}
