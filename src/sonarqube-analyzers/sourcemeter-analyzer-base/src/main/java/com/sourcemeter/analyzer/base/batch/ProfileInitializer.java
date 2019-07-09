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

package com.sourcemeter.analyzer.base.batch;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.sonar.api.batch.rule.Rule;
import org.sonar.api.batch.rule.Rules;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.config.Configuration;
import org.sonar.api.measures.Metric;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.resources.AbstractLanguage;
import org.sonar.api.batch.rule.ActiveRule;

import com.sourcemeter.analyzer.base.helper.FileHelper;
import com.sourcemeter.analyzer.base.profile.SourceMeterRuleRepository;

/**
 * Class for initializing MetricHunter\"s input file
 */
public class ProfileInitializer {

    private final Configuration configuration;
    private final AbstractLanguage language;
    private final List<MetricHunterCategory> categories;
    private final Set<String> activeRuleKeys;
    private final Collection<Rule> allRules;

    // private final Collection<org.sonar.api.batch.rule.Rule> allRules;

    /**
     * Sets the needed properties for generating a profile file for SourceMeter
     * toolchain.
     *
     * @param configuration For getting the metric threshold properties from SonarQube.
     * @param categories For setting which categories are passed to MetricHunter.
     * @param activeRules Needed for the list of active rules passed to profile file.
     * @param ruleRepository Stores the rules.
     * @param rules Needed for find rules from RuleRepository by key.
     * @param pluginLanguage Current analyzed language.
     */
    public ProfileInitializer(Configuration configuration,
            List<MetricHunterCategory> categories, ActiveRules activeRules,
            SourceMeterRuleRepository ruleRepository, Rules rules, AbstractLanguage pluginLanguage) {
        this.configuration = configuration;
        this.categories = categories;
        String repositoryKey = ruleRepository.getRepositoryKey();
        this.allRules = rules.findByRepository(repositoryKey);
        activeRuleKeys = new HashSet<String>();
        for (ActiveRule rule : activeRules.findByRepository(repositoryKey)) {
            activeRuleKeys.add(rule.ruleKey().rule());
        }
        this.language = pluginLanguage;
    }

    /**
     * Generates a profile file for sourceMeter toolchain.
     *
     * @param path The generated profile file\"s path.
     * @throws IOException Thrown when the SM-Profile.xml file could not be created.
     */
    public void generatePofileFile(String path) throws IOException {
        File file = new File(path);

        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("Profile file could not be created: "
                    + path);
        }

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(path), Charset.defaultCharset()))) {
            bw.write("<sourcemeter-profile>\n");
            bw.write("  <tool-options>\n");
            bw.write(getMetricHunterThresholds());
            bw.write("  </tool-options>\n");
            bw.write(getRuleOptions());
            bw.write("</sourcemeter-profile>\n");
        }
    }

    /**
     * Returns the options for rules as XML tag in a String.
     *
     * @return The options for rules as XML tags in a String.
     */
    private String getRuleOptions() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("  <rule-options>\n");

        for (Rule rule : allRules) {
            if (rule.severity().equals(Severity.INFO)) {
                continue;
            }

            String[] splittedKey = rule.key().rule().split("_");
            String key = splittedKey[splittedKey.length - 1];

            String priority = "Blocker";

            // have to check the value for setting priority, Severity enum's
            // ordinal is not in order
            if (rule.severity().equals(Severity.CRITICAL)) {
                priority = "Critical";
            } else if (rule.severity().equals(Severity.MAJOR)) {
                priority = "Major";
            } else if (rule.severity().equals(Severity.MINOR)) {
                priority = "Minor";
            }

            buffer.append("    <rule id=\"").append(key).append("\"  enabled=\"");
            if (activeRuleKeys.contains(rule.key().rule())) {
                buffer.append("true");
            } else {
                buffer.append("false");
            }
            buffer.append("\" priority=\"")
                  .append(priority)
                  .append("\"/>\n");
        }

        buffer.append("  </rule-options>\n");

        return buffer.toString();

    }

    /**
     * Returns the MetricHunter thresholds for sourceMeter profile.
     *
     * @return The MetricHunter thresholds.
     * @throws IOException
     */
    private String getMetricHunterThresholds() throws IOException {
        StringBuffer buffer = new StringBuffer();

        buffer.append("    <tool name = \"MetricHunter\" enabled = \"true\">\n      <metric-thresholds>\n");

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
                    String relation = "gt";

                    if (metric.getDirection() > 0) {
                        relation = "lt";
                    }

                    // Metric threshold rule is not active. Turn it off.
                    buffer.append("        <threshold metric-id=\"")
                          .append(metricKey)
                          .append("\" relation=\"")
                          .append(relation)
                          .append("\" value=\"none\" entity=\"")
                          .append(category.getCategoryName())
                          .append("\" />\n");
                }
            }

            buffer.append("\n");
        }

        buffer.append("      </metric-thresholds>\n    </tool>\n");
        return buffer.toString();
    }

    /**
     * Generate the threshold for a metric.
     *
     * @param metric Threshold for this metric.
     * @param entity Entity of the threshold.
     * @param property Property for the threshold.
     * @return The threshold's relation, value, entity for specified metric in a String as an XML tag.
     */
    private String getTresholdLine(Metric metric, String entity, String property) {
        StringBuffer baseline = new StringBuffer("sm.");
        baseline.append(language.getKey().toLowerCase(Locale.ENGLISH))
                .append(".")
                .append(property.toLowerCase(Locale.ENGLISH))
                .append(".baseline.")
                .append(metric.getKey());
        Double threshold = FileHelper.getDoubleFromConfiguration(this.configuration, baseline.toString());

        if (threshold == null) {
            return "";
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

        line.append("        <threshold metric-id=\"")
            .append(metric.getKey())
            .append("\" relation=\"")
            .append(relation)
            .append("\" value=\"")
            .append(thresholdString)
            .append("\" entity=\"")
            .append(entity)
            .append("\" />\n");

        return line.toString();
    }
}
