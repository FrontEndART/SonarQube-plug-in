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

import graphlib.Graph;
import graphlib.GraphlibException;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.config.Settings;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.resources.Project;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.ActiveRule;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import com.google.gson.Gson;
import com.sourcemeter.analyzer.base.core.LicenseInformation;
import com.sourcemeter.analyzer.base.profile.SourceMeterRuleRepository;

public abstract class SourceMeterSensor implements Sensor {

    protected static final Logger LOG = LoggerFactory.getLogger(SourceMeterSensor.class);
    protected static final String MS = " ms";

    protected final Settings settings;
    protected String resultGraph = "";
    protected String projectName = "";
    protected boolean isIncrementalMode = false;
    protected final ResourcePerspectives perspectives;
    protected final ModuleFileSystem moduleFileSystem;
    protected final FileSystem fileSystem;
    private static final Map<String, String> activeInfoRules = new TreeMap<String, String>();

    public static Map<String, String> getActiveInfoRules() {
        return activeInfoRules;
    }

    /**
     * Constructor: Use of IoC to get Settings
     */
    public SourceMeterSensor(ModuleFileSystem moduleFileSystem, FileSystem fileSystem, Settings settings,
            ResourcePerspectives perspectives, RulesProfile rulesProfile) {
        this.moduleFileSystem = moduleFileSystem;
        this.fileSystem = fileSystem;
        this.perspectives = perspectives;
        this.settings = settings;

        List<ActiveRule> activeRulesList = rulesProfile
                .getActiveRulesByRepository(SourceMeterRuleRepository.getRepositoryKey());
        if (activeRulesList != null) {
            for (ActiveRule activeRule : activeRulesList) {
                if (Severity.INFO.equals(activeRule.getSeverity().name())) {
                    activeInfoRules.put(activeRule.getRuleKey(), "");
                }
            }
        }
    }

    /**
     * Load result graph binary
     *
     * @param filename
     * @param project
     * @param sensorContext
     */
    protected abstract void loadDataFromGraphBin(String filename,
            Project project, SensorContext sensorContext) throws GraphlibException;

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void analyse(Project project, SensorContext sensorContext);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldExecuteOnProject(Project project) {
        return this.fileSystem.hasFiles(fileSystem.predicates().hasLanguage(
                SourceMeterInitializer.getPluginLanguage().getKey()));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    protected void saveLicense(Graph graph, SensorContext sensorContext,
            Map<String, String> headerLicenseInformations, Metric targetMetric) {
        Gson gson = new Gson();

        LicenseInformation licenseInformation = new LicenseInformation();
        for (Map.Entry<String, String> entry : headerLicenseInformations.entrySet()) {
            String modeInfo = entry.getKey() + "-mode";
            String value = graph.getHeaderInfo(modeInfo);

            if (value == null) {
                continue;
            }

            licenseInformation.addTool(entry.getValue(), value);
        }

        sensorContext.saveMeasure(new Measure(targetMetric, gson.toJson(
                licenseInformation).toString()));
    }
}
