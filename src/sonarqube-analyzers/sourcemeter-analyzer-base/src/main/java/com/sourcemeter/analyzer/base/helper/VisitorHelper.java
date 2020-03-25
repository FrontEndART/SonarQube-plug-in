/**
 * Copyright (c) 2014-2020, FrontEndART Software Ltd.
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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputComponent;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.measure.Metric;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.SourceMeterCore.api.SourceMeterMetricFinder;

import graphlib.Attribute;
import graphlib.Attribute.aType;
import graphlib.AttributeComposite;
import graphlib.AttributeFloat;
import graphlib.AttributeInt;
import graphlib.AttributeString;

/**
 * Helper class for Visitor classes to upload metrics and warnings.
 */
public abstract class VisitorHelper {

    public static final String METRIC_VIOLATION_CONTAINS = "_warning_";

    private static final Logger LOG = LoggerFactory.getLogger(VisitorHelper.class);
    protected static final String METRIC_PREFIX = "MET_";

    private final SensorContext sensorContext;
    private final SourceMeterMetricFinder metricFinder;
    protected final FileSystem fileSystem;

    public VisitorHelper(SensorContext sensorContext, FileSystem fileSystem,
            SourceMeterMetricFinder metricFinder) {
        this.sensorContext = sensorContext;
        this.metricFinder = metricFinder;
        this.fileSystem = fileSystem;
    }


    /**
     * Upload warnings for the given resource.
     *
     * @param attribute Node's attribute.
     */
    public void uploadWarnings(Attribute attribute) {
        AttributeComposite warningAttribute = (AttributeComposite) attribute;
        int startLine = 0;
        String warningText = "";
        String warningTextPref = "";
        String warningPath = "";
        AttributeComposite extraInfoAttribute = null;
        List<NewIssueLocation> stackTrace = null;
        List<Attribute> compAttributes = warningAttribute.getAttributes();
        for (Attribute a : compAttributes) {
            if ("Path".equals(a.getName())) {
                warningPath = ((AttributeString) a).getValue();
            } else if ("Line".equals(a.getName())) {
                startLine = ((AttributeInt) a).getValue();
            } else if ("WarningText".equals(a.getName())) {
                warningText = ((AttributeString) a).getValue();
            } else if ("ExtraInfo".equals(a.getName())) {
                extraInfoAttribute = (AttributeComposite) a;
            }
        }

        InputFile file = fileSystem.inputFile(fileSystem.predicates().hasPath(warningPath));
        if (file != null) {
            String tmpRuleKey = warningAttribute.getName();
            warningTextPref = getWarningTextWithPrefix(tmpRuleKey, warningText);
            tmpRuleKey = getCorrectedRuleKey(tmpRuleKey);
            RuleKey ruleKey = RuleKey.of(getRuleKey(), tmpRuleKey);

            NewIssue newIssue = sensorContext.newIssue().forRule(ruleKey);

            NewIssueLocation location = getIssueLocation(warningPath, warningTextPref, startLine);

            if (extraInfoAttribute != null) {
                stackTrace = getStackTraceFromWarningAttribute(extraInfoAttribute, warningText);
                if (stackTrace != null && !stackTrace.contains(null)) {
                    newIssue.at(location)
                            .addFlow(stackTrace)
                            .save();
                } else {
                    newIssue.at(location)
                            .save();
                }
            } else {
                newIssue.at(location)
                        .save();
            }
        }
    }

    /**
     * Returns the language specific rule key.
     *
     * @return The language specific rule key.
     */
    public abstract String getRuleKey();

    /**
     * Returns the warning text with the corresponding prefix for the given rule
     * key.
     *
     * @param warningText Text of the warning.
     * @return The warning text with the corresponding prefix for the given rule key.
     */
    public abstract String getWarningTextWithPrefix(String ruleKey, String warningText);

    /**
     * Upload metrics from the given attribute to the given resource.
     *
     * @param metricAttribute Attribute of the metrics.
     * @param inputComponent Component of the input.
     */
    public void uploadMetrics(Attribute metricAttribute, InputComponent inputComponent) {
        try {
            aType metricType = metricAttribute.getType();
            Metric metric = this.metricFinder.findByKey(metricAttribute.getName());

            if (metric != null) {
                if (metricType == aType.atInt) {
                    int value = ((AttributeInt) metricAttribute).getValue();
                    this.sensorContext.newMeasure().forMetric(metric).withValue(value).on(inputComponent).save();
                } else if (metricType == aType.atFloat) {
                    Double value = (double) ((AttributeFloat) metricAttribute)
                            .getValue();
                    if (!value.isNaN() && !value.isInfinite()) {
                        if (metric.valueType().equals(ValueType.PERCENT)) {
                            value = value * 100;
                        }
                        this.sensorContext.newMeasure().forMetric(metric).withValue(value).on(inputComponent).save();
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            LOG.warn(e.getMessage());
            if (inputComponent != null) {
                LOG.warn("Resource: " + inputComponent.key());
            }
        }
    }

    /**
     * If a rule key is a threshold violation in a graph, the key rule differs
     * from the stored rule in SonarQube. This method corrects it.
     *
     * @param ruleKey Key of the rule.
     * @return Corrected rule key.
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
     * Return the File System.
     *
     * @return Final, FileSystem typed member of VisitorHelper class.
     */
    public FileSystem getFileSystem() {
        return fileSystem;
    }

    /**
     * Creates the stack trace List from the given attribute.
     *
     * @param extraInfoAttribute
     *            The attribute that contains the stack trace information.
     * @param warningText
     *            Warning's text message.
     * @return A List, containing the stack trace locations.
     */
    private List<NewIssueLocation> getStackTraceFromWarningAttribute(
            AttributeComposite extraInfoAttribute, String warningText) {

        if (!"ExtraInfo".equals(extraInfoAttribute.getName())) {
            return null;
        }
        List<NewIssueLocation> stackTraceList = null;

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
        stackTraceList = new ArrayList<NewIssueLocation>();
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
                }
            }

            if (!previousPath.equals(path) || previousLine != line) {
                NewIssueLocation location = getIssueLocation(path, warningText, line);
                stackTraceList.add(location);
            }
        }
        return stackTraceList;
    }

    /**
     * Creates NewIssueLocation.
     *
     * @param path File's path.
     * @param warningText Text of the warning.
     * @param line Line in file.
     * @return NewIssueLocation
     */
    private NewIssueLocation getIssueLocation(String path, String warningText, int line) {
        InputFile file = fileSystem.inputFile(fileSystem.predicates().hasPath(path));
        NewIssueLocation location = null;
        if (file != null) {
            NewIssue newIssue = sensorContext.newIssue();
            location = newIssue.newLocation()
                               .message(warningText)
                               .on(file)
                               .at(file.selectLine(line));
        }
        return location;
    }
}
