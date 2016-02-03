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
package com.sourcemeter.analyzer.base.batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.sonar.api.config.Settings;
import org.sonar.api.measures.Metric;
import org.sonar.api.measures.Metric.ValueType;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;
import org.sonar.api.rules.RuleRepository;
import org.sonar.api.utils.SonarException;

/**
 * Class for initializing MetricHunter\"s input file
 */
public class ProfileInitializer {

    private final Settings settings;
    private final List<MetricHunterCategory> categories;
    private final Set<String> activeRuleKeys;
    private final List<Rule> allRules;

    /**
     * Sets the needed properties for generating a profile file for SourceMeter
     * toolchain.
     *
     * @param settings for getting the metric threshold properties from SonarQube
     * @param categories for setting which categories are passed to MetricHunter
     * @param activeRules needed for the list of active rules passed to profile file
     */
    public ProfileInitializer(Settings settings,
            List<MetricHunterCategory> categories, RulesProfile profile,
            RuleRepository ruleRepository) {
        this.settings = settings;
        this.categories = categories;
        this.allRules = ruleRepository.createRules();
        String repositoryKey = ruleRepository.getKey();
        activeRuleKeys = new HashSet<String>();
        for (ActiveRule rule : profile.getActiveRulesByRepository(repositoryKey)) {
            activeRuleKeys.add(rule.getRuleKey());
        }
    }

    /**
     * Generates a profile file for sourceMeter toolchain.
     *
     * @param path the generated profile file\"s path
     * @throws IOException
     */
    public void generatePofileFile(String path) throws IOException {
        File file = new File(path);

        if (!file.exists() && !file.createNewFile()) {
            throw new SonarException("Profile file could not be created: "
                    + path);
        }

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path), Charset.defaultCharset()));

        bw.write("<sourcemeter-profile>\n");
        bw.write("  <tool-options>\n");
        bw.write(getMetricHunterThresholds());
        bw.write("  </tool-options>\n");
        bw.write(getRuleOptions());
        bw.write("</sourcemeter-profile>\n");
        bw.close();
    }

    private String getRuleOptions() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("  <rule-options>\n");

        for (Rule rule : allRules) {
            if (rule.getSeverity() == RulePriority.INFO) {
                continue;
            }

            String[] splittedKey = rule.getKey().split("_");
            String key = splittedKey[splittedKey.length - 1];

            String priority = "Blocker";

            // have to check the value for setting priority, RulePriority enum's
            // ordinal is not in order
            if (rule.getSeverity() == RulePriority.CRITICAL) {
                priority = "Critical";
            } else if (rule.getSeverity() == RulePriority.MAJOR) {
                priority = "Major";
            } else if (rule.getSeverity() == RulePriority.MINOR) {
                priority = "Minor";
            }

            buffer.append("    <rule id=\"").append(key).append("\"  enabled=\"");
            if (activeRuleKeys.contains(rule.getKey())) {
                buffer.append("true");
            } else {
                buffer.append("false");
            }
            buffer.append("\" priority=\"").append(priority).append("\"");
            buffer.append("/>\n");
        }

        buffer.append("  </rule-options>\n");

        return buffer.toString();
    }

    private String getMetricHunterThresholds() throws IOException {
        StringBuffer buffer = new StringBuffer();

        buffer.append("    <tool name = \"MetricHunter\" enabled = \"true\">\n");
        buffer.append("      <metric-thresholds>\n");

        for (MetricHunterCategory category : categories) {
            List<Metric> metrics = category.getMetrics();

            for (Metric metric : metrics) {
                String metricKey = metric.getKey();

                if (activeRuleKeys.contains("MET_" + metricKey)) {
                    String line = getTresholdLine(metric,
                                                  category.getCategoryName(),
                                                  category.getpropertyName());
                    if (!line.isEmpty()) {
                        buffer.append(line);
                    }
                } else {
                    // Metric threshold rule is not active. Turn it off.
                    buffer.append("        <threshold metric-id=\"")
                          .append(metricKey)
                          .append("\" relation=\"gt\" value=\"none\" entity=\"")
                          .append(category.getCategoryName())
                          .append("\" />\n");
                }
            }

            buffer.append("\n");
        }

        buffer.append("      </metric-thresholds>\n");
        buffer.append("    </tool>\n");
        return buffer.toString();
    }

    private String getTresholdLine(Metric metric, String entity, String property) {
        StringBuffer baseline = new StringBuffer("sm.");
        baseline.append(SourceMeterInitializer.getPluginLanguage().getKey().toLowerCase(Locale.ENGLISH));
        baseline.append(".").append(property.toLowerCase(Locale.ENGLISH))
                .append(".baseline.").append(metric.getKey());
        Double threshold = this.settings.getDouble(baseline.toString());

        if (threshold == null) {
            return "";
        }

        if (metric.getType() == ValueType.PERCENT) {
            threshold /= 100;
        }

        String thresholdString = threshold.toString();

        if (threshold % 1 == 0) {
            thresholdString = Long.toString(Math.round(threshold));
        }

        StringBuffer line = new StringBuffer();
        String relation = "gt";

        if (metric.getDirection() > 0) {
            relation = "lt";
        }

        line.append("        <threshold metric-id=\"").append(metric.getKey()).append("\" relation=\"").append(relation);
        line.append("\" value=\"").append(thresholdString).append("\" entity=\"").append(entity).append("\" />\n");

        return line.toString();
    }
}
