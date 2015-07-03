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
package com.sourcemeter.analyzer.python.helper;

import graphlib.Attribute;
import graphlib.AttributeComposite;
import graphlib.AttributeInt;
import graphlib.AttributeString;
import graphlib.Node;
import graphsupportlib.Metric.Position;
import com.sourcemeter.analyzer.base.helper.VisitorHelper;
import com.sourcemeter.analyzer.python.SourceMeterPythonMetricFinder;
import com.sourcemeter.analyzer.python.profile.SourceMeterPythonRuleRepository;

import java.io.File;
import java.util.List;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.Resource;
import org.sonar.api.rule.RuleKey;

public class VisitorHelperPython extends VisitorHelper {

    public VisitorHelperPython(Project project, SensorContext sensorContext,
            ResourcePerspectives perspectives, Settings settings) {

        super(project, sensorContext, perspectives, settings,
                new SourceMeterPythonMetricFinder());
    }

    @Override
    public void uploadWarnings(Attribute attribute, Node node, Position nodePosition) {
        AttributeComposite warningAttribute = (AttributeComposite) attribute;
        int lineId = 0;
        String warningText = "";
        String warningPath = "";

        List<Attribute> compAttributes = warningAttribute.getAttributes();
        for (Attribute a : compAttributes) {
            if ("Path".equals(a.getName())) {
                warningPath = ((AttributeString) a).getValue();
            } else if ("Line".equals(a.getName())) {
                lineId = ((AttributeInt) a).getValue();
            } else if ("WarningText".equals(a.getName())) {
                warningText = ((AttributeString) a).getValue();
            }
        }

        Resource violationResource = org.sonar.api.resources.File.fromIOFile(
                new File(warningPath), this.project);

        if (violationResource == null) {
            return;
        }

        Issuable issuable = this.perspectives.as(Issuable.class,
                violationResource);
        if (issuable != null) {
            String tmpRuleKey = warningAttribute.getName();

            if (tmpRuleKey.startsWith("PYLINT_")) {
                warningText = "SourceMeter (from Pylint): " + warningText;
            } else {
                warningText = "SourceMeter: " + warningText;
            }

            tmpRuleKey = getCorrectedRuleKey(tmpRuleKey);
            RuleKey ruleKey = RuleKey.of(SourceMeterPythonRuleRepository.getRepositoryKey(),
                    tmpRuleKey);
            Issue issue = issuable.newIssueBuilder().ruleKey(ruleKey)
                    .message(warningText).line(lineId).build();

            issuable.addIssue(issue);
        }
    }
}
