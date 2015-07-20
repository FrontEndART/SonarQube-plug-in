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
package com.sourcemeter.analyzer.rpg.helper;

import graphlib.Node;
import graphsupportlib.Metric.Position;

import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.resources.Project;

import com.sourcemeter.analyzer.base.batch.ProfileInitializer;
import com.sourcemeter.analyzer.base.helper.VisitorHelper;
import com.sourcemeter.analyzer.rpg.SourceMeterRPGMetricFinder;
import com.sourcemeter.analyzer.rpg.profile.SourceMeterRPGRuleRepository;

public class VisitorHelperRPG extends VisitorHelper {

    public static final String PROGRAM_TRESHOLD_VIOLATION_SUFFIX = "_warning_Program";
    public static final String PROCEDURE_TRESHOLD_VIOLATION_SUFFIX = "_warning_Procedure";
    public static final String SUBROUTINE_TRESHOLD_VIOLATION_SUFFIX = "_warning_Subroutine";

    public VisitorHelperRPG(Project project, SensorContext sensorContext,
            ResourcePerspectives perspectives, FileSystem fileSystem) {

        super(project, sensorContext, perspectives, fileSystem,
                new SourceMeterRPGMetricFinder());
    }

    @Override
    public String getRuleKey() {
        return SourceMeterRPGRuleRepository.getRepositoryKey();
    }

    @Override
    public String getWarningTextWithPrefix(String ruleKey, String warningText) {
        return "SourceMeter: " + warningText;
    }

    @Override
    public String getCorrectedRuleKey(String ruleKey) {
        if (ruleKey.contains(PROGRAM_TRESHOLD_VIOLATION_SUFFIX)) {
            // program treshold violation
            ruleKey = METRIC_PREFIX + ruleKey.replaceAll(PROGRAM_TRESHOLD_VIOLATION_SUFFIX, "");
        } else if (ruleKey.contains(PROCEDURE_TRESHOLD_VIOLATION_SUFFIX)) {
            // procedure treshold violation
            ruleKey = METRIC_PREFIX + ruleKey.replaceAll(PROCEDURE_TRESHOLD_VIOLATION_SUFFIX, "");
        } else if (ruleKey.contains(SUBROUTINE_TRESHOLD_VIOLATION_SUFFIX)) {
            // subroutine treshold violation
            ruleKey = METRIC_PREFIX + ruleKey.replaceAll(SUBROUTINE_TRESHOLD_VIOLATION_SUFFIX, "");
        } else if (ruleKey.contains(ProfileInitializer.ClONE_CLASS_TRESHOLD_VIOLATION_SUFFIX)) {
            // CloneClass treshold violation
            ruleKey = METRIC_PREFIX + ruleKey.replaceAll(ProfileInitializer.ClONE_CLASS_TRESHOLD_VIOLATION_SUFFIX, "");
        } else if (ruleKey.contains(ProfileInitializer.CLONE_INSTANCE_TRESHOLD_VIOLATION_SUFFIX)) {
            // CloneInstance treshold violation
            ruleKey = METRIC_PREFIX + ruleKey.replaceAll(ProfileInitializer.CLONE_INSTANCE_TRESHOLD_VIOLATION_SUFFIX, "");
        } else {
            String[] splittedKey = ruleKey.split("_");
            ruleKey = splittedKey[splittedKey.length - 1];
        }

        return ruleKey;
    }

    @Override
    public String getPathFromNode(Node node) {
        String path = null;
        Position position = graphsupportlib.Metric
                .getFirstPositionAttribute(node);

        if (null != position) {
            path = FileHelperRPG.getCorrectedFilePath(position.path, fileSystem);
        }

        return path;
    }
}
