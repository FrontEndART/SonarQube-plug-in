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

package com.sourcemeter.analyzer.base.profile;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class SourceMeterProfile implements BuiltInQualityProfilesDefinition {

    protected static final Logger LOG = LoggerFactory.getLogger(SourceMeterProfile.class);

    /**
     * Read the repository keys and the rule keys for the new QualityProfile from XML file.
     *
     * @param qualityProfile New QualityProfile.
     * @param qpXml Resource of the repository keys and the rule keys in XML format.
     */
    protected void parseXml(NewBuiltInQualityProfile qualityProfile, InputStream qpXml) {

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(qpXml);

            doc.getDocumentElement().normalize();

            NodeList ruleNodes = doc.getElementsByTagName("rule");

            for (int i = 0; i < ruleNodes.getLength(); i++) {
                Node ruleNode = ruleNodes.item(i);
                if (ruleNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element ruleElement = (Element) ruleNode;

                    String repoKey = ruleElement.getElementsByTagName("repositoryKey").item(0).getTextContent();
                    String ruleKey = ruleElement.getElementsByTagName("key").item(0).getTextContent();

                    qualityProfile.activateRule(repoKey, ruleKey);
                }
            }

        } catch (ParserConfigurationException e) {
            LOG.error("ERROR: Can not parse QualityProfile's XML file!");
        } catch (SAXException e) {
            LOG.error("ERROR: Can not parse QualityProfile's XML file!");
        } catch (IOException e) {
            LOG.error("ERROR: Can not parse QualityProfile's XML file!");
        }
    }
}
