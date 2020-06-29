#!/usr/bin/env python

# Copyright (c) 2014-2020, FrontEndART Software Ltd.
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
# 1. Redistributions of source code must retain the above copyright
#     notice, this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright
#    notice, this list of conditions and the following disclaimer in the
#    documentation and/or other materials provided with the distribution.
# 3. All advertising materials mentioning features or use of this software
#    must display the following acknowledgement:
#    This product includes software developed by FrontEndART Software Ltd.
# 4. Neither the name of FrontEndART Software Ltd. nor the
#    names of its contributors may be used to endorse or promote products
#    derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY FrontEndART Software Ltd. ''AS IS'' AND ANY
# EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
# WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL FrontEndART Software Ltd. BE LIABLE FOR ANY
# DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
# (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
# LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
# ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
# SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

import argparse
import re
import csv
import os
import sys
from os import path
from os import listdir
from os.path import isfile, join
from xml.sax.saxutils import unescape
from xml.sax.saxutils import escape
import xml.etree.ElementTree as ET
import xml.dom.minidom as minidom
from shutil import copyfile

activeRulesDefaultSet = { 'LOC', 'LLOC', 'NUMPAR', 'NOS', 'CLOC', 'DLOC', 'McCC', 'NLE', 'CCO', 'CI', 'CLLC', 'NII', 'NOI', 'NLA', 'NLM', 'NLPM', 'NOS', 'TLOC', 'TLLOC', 'TNOS', 'AD', 'WMC', 'CBO', 'RFC', 'DIR', 'NOC', 'LCOM5', 'CE', 'CLLOC'}
ignoredRulesSet= { 'NCR' }

MET_ID = 'MET'
RPG_MET_ID = 'RPG2Metrics'
DCF_ID = 'DCF'
RULECHECKER = 'RULECHECKER'
PMD_ID = 'PMD'
FH_ID = 'FaultHunter'
DEFAULT_RULE_CONFIG = 'Default'

rul2xml_path = join('tools', 'rul2xml')

rule_types = ['CODE_SMELL', 'VULNERABILITY', 'BUG']

toolsInRulCsv = {
                #CPP
                'Cppcheck':'CPPCHECK', 'FaultHunterCPP':'FHCPP',
                #CSHARP
                'FxCop':'FXCOP',
                #JAVA
                FH_ID:'FH', 'AndroidHunter':'AH', 'VulnerabilityHunter':'VH', 'FindBugs':'FB', PMD_ID:'PMD', 'RTEHunter':'RH',
                #JAVASCRIPT
                'ESLint':'ESLINT',
                #PYTHON
                'FaultHunterPython':'FHPY', 'Pylint':'PYLINT',
                #RPG
                'FaultHunterRPG':'FHRPG'
                }

def get_arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument('-sm', '--sourcemeter-path', default=None,
                        help='Full path of the SourceMeter analyzer base directory.', required=True)


    return parser.parse_args()

class RulHandler:

    def __init__(self, rulFileName, csvTypeFileName, language, ruleCsvFileName):
        self.rulIds = []
        self.rulNames = []
        self.rulDescs = []
        self.rulPriorities = [] #severity
        self.rulTypes = []
        self.language = ''
        self.rulFileName = ''
        self.csvTypeFileName = ''
        self.toolDescription = ''
        self.numOfRuls = 0

        self.language = language
        self.rulFileName = rulFileName

        if path.isfile(csvTypeFileName):
            self.csvTypeFileName = csvTypeFileName
        else:
            print('No Type file for ' + self.language + ' (' + csvTypeFileName + ')')


        tree = ET.parse(self.rulFileName)
        root = tree.getroot()

        prefix = self.namespace(root)

        print('LANG: ' + self.language)
        print('FILE: ' + self.rulFileName)
        print('PREFIX: ' + prefix)

        for toolDescription in root.findall(prefix + 'ToolDescription'):
            for confi in toolDescription.findall(prefix + 'Configuration'):
                self.toolDescription = confi.find(prefix + 'ToolDescriptionItem').text

        for metric in root.findall(prefix + 'Metric'):
            _metricId = metric.get('id')
            if '_' in _metricId:
                _metricId = _metricId.split('_')[1]
            if (self.toolDescription != DCF_ID and self.toolDescription != MET_ID and self.toolDescription != RPG_MET_ID) and rulePriorInRulset(_metricId, self.toolDescription, ruleCsvFileName) != 1:
                continue;
            confLanguage = None
            confDefault = None
            for configuration in metric.findall(prefix + 'Configuration'):
                if configuration.get('name') == language:
                    confLanguage = configuration
                if configuration.get('name') == 'Default':
                    confDefault = configuration
            if confLanguage is not None:
                configuration = confLanguage
            elif confDefault is not None:
                configuration = confDefault
            isEnabled = configuration.find(prefix + 'Enabled').text == 'true'
            if configuration.find(prefix + 'Visible') is not None:
                isVisible = configuration.find(prefix + 'Visible').text == 'true'
            else:
                isVisible = True
            if confDefault.find(prefix + 'Group') is not None:
                isNotGroup = confDefault.find(prefix + 'Group').text == 'false'
            else:
                isNotGroup = True
            if isEnabled and isVisible and isNotGroup:
                for lang1 in configuration.findall(prefix + 'Language'):
                    if lang1.get('lang') == 'eng':
                        _displayName = lang1.find(prefix + 'DisplayName')
                        _description = lang1.find(prefix + 'HelpText')

                        if _displayName is not None:
                            _displayName = _displayName.text
                        else:
                            for lang2 in confDefault.findall(prefix + 'Language'):
                                if lang2.get('lang') == 'eng':
                                    _displayName = lang2.find(prefix + 'DisplayName').text

                        if _description is not None:
                            _description = _description.text
                        else:
                            _description = lang1.find(prefix + 'Description')
                            if _description is not None:
                                _description = _description.text
                            else:
                                for lang3 in confDefault.findall(prefix + 'Language'):
                                    if lang3.get('lang') == 'eng':
                                        _description = lang3.find(prefix + 'HelpText')
                                        if _description is not None:
                                            _description = _description.text
                                        else:
                                            _description = '&lt;h3&gt;' + self.toolDescription + '&lt;/h3&gt;'

                _severity = 'INFO'
                setting = None
                for settings in configuration.findall(prefix + 'Settings'):
                    for setting in settings.findall(prefix + 'Setting'):
                        if setting.get('name') == 'Priority':
                            _severity = setting.text.upper()

                if setting is None:
                    for settings in confDefault.findall(prefix + 'Settings'):
                        for setting in settings.findall(prefix + 'Setting'):
                            if setting.get('name') == 'Priority':
                                _severity = setting.text.upper()
                    if setting is None:
                        _severity = 'INFO'
                if _metricId not in ignoredRulesSet:
                    if _description is None:
                        _description = '&lt;h3&gt;' + self.toolDescription + '&lt;/h3&gt;'
                    elif self.toolDescription in toolsInRulCsv.keys():
                        _description = '&lt;h3&gt;' + self.toolDescription + '&lt;/h3&gt;' + _description
                    self.rulIds.append(_metricId)
                    self.rulNames.append(_displayName)
                    self.rulDescs.append(_description)
                    self.rulPriorities.append(_severity)
                    if self.csvTypeFileName != '':
                        self.rulTypes.append(self.findTypeByKey(_metricId))
                    else:
                        self.rulTypes.append('')

        self.numOfRuls = len(self.rulIds)

        self.sort()

    def sort(self):
        self.rulNames = [x for _,x in sorted(zip(self.rulIds, self.rulNames))]
        self.rulDescs = [x for _,x in sorted(zip(self.rulIds, self.rulDescs))]
        self.rulPriorities =[x for _,x in sorted(zip(self.rulIds, self.rulPriorities))]
        self.rulTypes =[x for _,x in sorted(zip(self.rulIds, self.rulTypes))]
        self.rulIds.sort()

    def namespace(sefl, element):
        tag = element.tag
        if '{' in tag and '}' in tag:
            return '{' + element.tag.split('}')[0].strip('{') + '}'
        else:
            return ''

    def findTypeByKey(self, id):
        retVal = ''
        toolDescCondition = (self.toolDescription != DCF_ID and
                             self.toolDescription != MET_ID and
                             self.toolDescription != RPG_MET_ID)
        with open(self.csvTypeFileName, encoding='utf8') as csv_file:
            csv_reader = csv.reader(csv_file, delimiter=',')
            line_count = 0
            for row in csv_reader:
                if line_count == 0:
                    line_count += 1
                elif toolDescCondition and re.search('_' + id + '$', row[2]):
                    retVal = row[3]
                line_count += 1

        return retVal

    def __str__(self):
        retVal = ''

        for i in range(self.numOfRuls):
            retVal += 'ID  : ' + self.rulIds[i] + '\n'
            retVal += 'Name: ' + self.rulNames[i] + '\n'
            retVal += 'Desc: ' + self.rulDescs[i] + '\n'
            retVal += 'Sev : ' + self.rulPriorities[i] + '\n'
            retVal += '\n'

        retVal += 'Language: ' + self.language + '\n'
        retVal += 'Rul file name: ' + self.rulFileName + '\n'
        retVal += 'Type CSV file name: ' + self.csvTypeFileName + '\n'
        retVal += 'Tool description: ' + self.toolDescription + '\n'
        retVal += 'Number of ruls: ' + str(self.numOfRuls) + '\n'

        return retVal

def rulePriorInRulset(rulName, rulSetName, ruleCsvFileName):
    retVal = 0
    if rulSetName in toolsInRulCsv.keys():
        rulSetName = toolsInRulCsv[rulSetName]
    else:
        return retVal
    with open(ruleCsvFileName, encoding='utf8') as csv_file:
        csv_reader = csv.DictReader(csv_file, delimiter=';')
        for row in csv_reader:
            if rulSetName in row.keys() and row[rulSetName].isnumeric():
                toolNumber = int(row[rulSetName])
                if row['toolId'] == rulName:
                    retVal = toolNumber
                    return retVal
    return retVal

def prettify(elem):
    # Return a pretty-printed XML string for the Element.
    rough_string = ET.tostring(elem, 'utf-8')
    reparsed = minidom.parseString(rough_string)
    return reparsed.toprettyxml(indent='  ')

def main(options):
    smPath = options.sourcemeter_path
    platform = ''

    if 'nt' == os.name:
        platform = 'Windows'
    elif 'posix' == os.name:
        platform = 'Linux'

    cpp_files = [
        'MET.rul',
        'DCF.rul',
        'FaultHunterCPP.rul',
        'Cppcheck.rul'
    ]

    csharp_files = [
        'MET.rul',
        'DCF.rul',
        'FxCop.rul'
    ]

    java_files = [
        'MET.rul',
        'DCF.rul',
        'PMD.rul',
        'VLH.rul',
        'FaultHunter.rul',
        'Android.rul',
        'FindBugs.rul',
        'RTEHunter.rul'
    ]

    javascirpt_files = [
        'MET.rul',
        'DCF.rul',
        'ESLint.rul'
    ]

    python_files = [
        'MET.rul',
        'DCF.rul',
        'FaultHunterPython.rul',
        'Pylint_2.rul'
    ]

    rpg_files = [
        'RPG-MET.rul',
        'DCF.rul',
        'FaultHunterRPG.rul'
    ]

    languages = ['cpp', 'csharp','java','javascript','python','rpg']

    for languageDir in languages:
        if languageDir == 'cpp':
            rulFiles = cpp_files;
            languageKey = languageDir
            languageDirInSm = 'CPP'
        elif languageDir == 'csharp':
            rulFiles = csharp_files;
            languageKey = 'cs'
            languageDirInSm = 'CSharp'
        elif languageDir == 'java':
            rulFiles = java_files;
            languageKey = languageDir
            languageDirInSm = 'Java'
        elif languageDir == 'javascript':
            rulFiles = javascirpt_files;
            languageKey = 'js'
            languageDirInSm = 'JavaScript'
        elif languageDir == 'python':
            rulFiles = python_files;
            languageKey = 'py'
            languageDirInSm = 'Python'
        elif languageDir == 'rpg':
            rulFiles = rpg_files;
            languageKey = languageDir
            languageDirInSm = 'RPG'

        ugDst = join('src', 'sonarqube-analyzers', 'sourcemeter-analyzer-' + languageDir, 'src', 'main', 'resources', 'static', 'help', 'usersguide.html')

        if path.isfile(ugDst):
            copyfile(join(smPath, languageDirInSm, 'UsersGuide.html'), ugDst)

        rulHs = []
        if languageDirInSm is not 'JavaScript' and languageDirInSm is not 'Python' and languageDirInSm is not 'RPG':
            toolsPath = join(smPath, languageDirInSm, platform + 'Tools')
        else:
            toolsPath = join(smPath, languageDirInSm, 'Tools')

        for rulFile in rulFiles:
            rulFile = join(toolsPath, rulFile)
            ruleCsvFileName = join(toolsPath, 'rules_' + languageDir + '.csv')

            rulHs.append(RulHandler(rulFile, join(rul2xml_path, languageDir, 'SonarQube-' + languageDir +'-cat.csv'), languageDir, ruleCsvFileName))

        root = ET.Element('rules')
        for j in range(len(rulHs)):
            for i in range(rulHs[j].numOfRuls):

                if rulHs[j].toolDescription == DCF_ID or rulHs[j].toolDescription == MET_ID or rulHs[j].toolDescription == RPG_MET_ID:
                    name = rulHs[j].rulNames[i] + ' (' + rulHs[j].rulIds[i] + ') Metric Threshold Violation'
                    key = 'MET_' + rulHs[j].rulIds[i]
                else:
                    name = rulHs[j].rulNames[i]
                    key = rulHs[j].rulIds[i]

                doc = ET.SubElement(root, 'rule')
                ET.SubElement(doc, 'key').text = key
                ET.SubElement(doc, 'name').text = name
                ET.SubElement(doc, 'description').text = unescape(rulHs[j].rulDescs[i])#.replace('\n', '&#x0A;')
                if rulHs[j].rulTypes[i] in rule_types:
                    ET.SubElement(doc, 'type').text = rulHs[j].rulTypes[i]
                ET.SubElement(doc, 'severity').text = rulHs[j].rulPriorities[i]
                ET.SubElement(doc, 'tag').text = 'sourcemeter'

        resource_dir = join('src', 'sonarqube-analyzers', 'sourcemeter-analyzer-' + languageDir, 'src','main','resources')

        #rules.xml
        if path.isdir(resource_dir):
            text_file = open(join(resource_dir, 'rules.xml'), 'w', encoding='utf-8')
            text_file.write(prettify(root))
            text_file.close()

        root2 = ET.Element('profile')
        ET.SubElement(root2, 'name').text = 'SourceMeter way'

        ET.SubElement(root2, 'language').text = languageKey

        doc2 = ET.SubElement(root2, 'rules')

        for j in range(len(rulHs)):
            for i in range(rulHs[j].numOfRuls):
                if rulHs[j].toolDescription == DCF_ID or rulHs[j].toolDescription == MET_ID or rulHs[j].toolDescription == RPG_MET_ID:
                    if rulHs[j].rulIds[i] not in activeRulesDefaultSet:
                        continue
                    key = 'MET_' + rulHs[j].rulIds[i]
                else:
                    key = rulHs[j].rulIds[i]

                doc = ET.SubElement(doc2, 'rule')
                ET.SubElement(doc, 'repositoryKey').text = 'SourceMeter_' + languageKey
                ET.SubElement(doc, 'key').text = key
                ET.SubElement(doc, 'priority').text = rulHs[j].rulPriorities[i]

        #SourceMeter_way_default_profile.xml
        if path.isdir(resource_dir):
            text_file = open(join(resource_dir, 'SourceMeter_way_default_profile.xml'), 'w', encoding='utf-8')
            text_file.write(prettify(root2))
            text_file.close()

        rulFiles.clear()

main(get_arguments())
