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
package com.sourcemeter.analyzer.java.helper;

import graphlib.Attribute;
import graphlib.AttributeComposite;
import graphlib.AttributeInt;
import graphlib.AttributeString;
import graphlib.Node;
import graphsupportlib.Metric.Position;
import com.sourcemeter.analyzer.base.helper.VisitorHelper;
import com.sourcemeter.analyzer.java.SourceMeterJavaMetricFinder;
import com.sourcemeter.analyzer.java.profile.SourceMeterJavaRuleRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rule.RuleKey;

public class VisitorHelperJava extends VisitorHelper {

    public VisitorHelperJava(Project project, SensorContext sensorContext,
            ResourcePerspectives perspectives, Settings settings) {
        super(project, sensorContext, perspectives, settings,
                new SourceMeterJavaMetricFinder());
    }

    @Override
    public void uploadWarnings(Attribute attribute, Node node, Position nodePosition) {
        AttributeComposite warningAttribute = (AttributeComposite) attribute;
        int lineId = 0;
        int callStackDepth = 0;
        StringBuffer warningText = new StringBuffer();
        String warningPath = "";
        StringBuffer stackTrace = null;
        List<String> stackTraceList = null;

        List<Attribute> compAttributes = warningAttribute.getAttributes();
        for (Attribute a : compAttributes) {
            if ("Path".equals(a.getName())) {
                warningPath = ((AttributeString) a).getValue();
            } else if ("Line".equals(a.getName())) {
                lineId = ((AttributeInt) a).getValue();
            } else if ("WarningText".equals(a.getName())) {
                warningText.append(((AttributeString) a).getValue());
            } else if ("ExtraInfo".equals(a.getName())) {
//                Example:
//                <attribute type = "composite" name = "ExtraInfo" context = "">
//                  <attribute type = "composite" name = "SourceLink" context = "">
//                    <attribute type = "string" name = "Path" context = "" value = "src/org/snipsnap/net/AddLabelServlet.java"/>
//                    <attribute type = "int" name = "Line" context = "" value = "71"/>
//                    <attribute type = "int" name = "Column" context = "" value = "0"/>
//                    <attribute type = "int" name = "EndLine" context = "" value = "71"/>
//                    <attribute type = "int" name = "EndColumn" context = "" value = "10000"/>
//                    <attribute type = "int" name = "CallStackDepth" context = "" value = "0"/>
//                  </attribute>
//                  ...
//                </attribute>

                stackTrace = new StringBuffer("<br/>Trace:<br/>");
                stackTraceList = new ArrayList<String>();
                List<Attribute> extraInfoAttributes = ((AttributeComposite) a).getAttributes();
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
                        } else if ("CallStackDepth".equals(sourceLinkAttribute.getName())) {
                            callStackDepth = ((AttributeInt) sourceLinkAttribute).getValue();
                        }
                    }
                    if (!previousPath.equals(path) || previousLine != line) {
                        previousLine = line;
                        previousPath = path;
                        Resource tracePathResource = org.sonar.api.resources.File.fromIOFile(new File(path), this.project);
                        StringBuffer pathLink = new StringBuffer();
                        pathLink.append("__");
                        pathLink.append(sensorContext.getResource(tracePathResource).getId());
                        pathLink.append(":");
                        pathLink.append(sensorContext.getResource(tracePathResource).getName());
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
                }
                int sum = warningText.length();
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
            }
        }

        Resource violationResource = org.sonar.api.resources.File.fromIOFile(new File(warningPath), this.project);
        if (violationResource == null) {
            return;
        }

        Issuable issuable = this.perspectives.as(Issuable.class, violationResource);
        if (issuable != null) {
            String tmpRuleKey = warningAttribute.getName();
            if (tmpRuleKey.startsWith("PMD_")) {
                warningText.insert(0, "SourceMeter (from PMD): ");
            } else if (tmpRuleKey.startsWith("FB_")) {
                warningText.insert(0, "SourceMeter (from FindBugs): ");
            } else {
                warningText.insert(0, "SourceMeter: " + warningText);
            }
            tmpRuleKey = getCorrectedRuleKey(tmpRuleKey);
            RuleKey ruleKey = RuleKey.of(SourceMeterJavaRuleRepository.getRepositoryKey(), tmpRuleKey);
            if (stackTrace != null) {
                warningText.append(stackTrace);
            }
            Issue issue = issuable.newIssueBuilder()
                    .ruleKey(ruleKey)
                    .message(warningText.toString())
                    .line(lineId)
                    .build();

            issuable.addIssue(issue);
        }
    }
}
