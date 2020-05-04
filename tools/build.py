#!/usr/bin/env python

# Copyright (c) 2014-2018, FrontEndART Software Ltd.
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

# Requirements:
#    * Maven
#    * Python3
#    * Pandoc 2.2.3.0+

import argparse
import glob
import os
import platform
import shutil
import subprocess
import tarfile

import common

PACKAGENAME = 'sourcemeter-8.2-plugins-for-sonarqube-6.7-v1.1.0'

TOOLS_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_DIR = os.path.normpath(os.path.join(TOOLS_DIR, '..'))

def get_arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument('--all', action='store_true',
                        help='Build everything (plugins, UG) and pack it.')
    parser.add_argument('--builddir', metavar='DIR',
                        default=os.path.join(PROJECT_DIR, 'build'),
                        help='Specify build directory (default: %(default)s)')
    parser.add_argument('--clean', action='store_true',
                        help='Clean build')
    parser.add_argument('--dist', action='store_true',
                        help='Build everything and create a distribution package.')

    group = parser.add_argument_group('Build Options')
    group.add_argument('--cpp', action='store_true',
                        help='Build C/C++ Analyzer')
    group.add_argument('--csharp', action='store_true',
                       help='Build C# Analyzer')
    group.add_argument('--gui', action='store_true',
                        help='Build GUI plugin (Dashboard, '
                             'Clone View, UsersGuide, Help)')
    group.add_argument('--java', action='store_true',
                        help='Build Java Analyzer')
    group.add_argument('--javascript', action='store_true',
                        help='Build JavaScript Analyzer')
    group.add_argument('--python', action='store_true',
                        help='Build Python Analyzer')
    group.add_argument('--rpg', action='store_true',
                        help='Build RPG Analyzer')

    return parser.parse_args()

def clean():
    common.rmdir('src/sonarqube-core-plugin/target')
    common.rmdir('src/sonarqube-gui-plugin/target')
    common.rmdir('src/sonarqube-analyzers/sourcemeter-analyzer-base/target')
    common.rmdir('src/sonarqube-analyzers/sourcemeter-analyzer-cpp/target')
    common.rmdir('src/sonarqube-analyzers/sourcemeter-analyzer-csharp/target')
    common.rmdir('src/sonarqube-analyzers/sourcemeter-analyzer-java/target')
    common.rmdir('src/sonarqube-analyzers/sourcemeter-analyzer-javascript/target')
    common.rmdir('src/sonarqube-analyzers/sourcemeter-analyzer-python/target')
    common.rmdir('src/sonarqube-analyzers/sourcemeter-analyzer-rpg/target')

def mvn_install(target):
    common.run_cmd('mvn', ['-f', ('src/%s/pom.xml' % target), 'clean', 'install'])

def usersguide():
    if platform.system() == 'Windows':
        common.run_cmd('py', ['-3', 'generatedoc.py', '-css', 'style\\SourceMeter.css', '-html'],
                False, 'doc/usersguide')
    else:
        common.run_cmd('python3', ['generatedoc.py', '-css', 'style/SourceMeter.css', '-html'],
                False, 'doc/usersguide')
    try:
        shutil.copy('doc/usersguide/results/UG.html', 'doc/UG.html')
        shutil.copy('doc/UG.html', 'src/sonarqube-gui-plugin/src/main/resources/static/help/usersguide.html')
    except OSError:
        print('Cannot copy usersguide. Please check if it was generated successfully.')

def copy_jars(src, dest):
    jars = glob.glob('%s/*.jar' % src)
    for jar in jars:
        try:
            shutil.copy(jar, dest)
        except OSError:
            print('Cannot copy JAR from %s. Please check if it was built '
                  'successfully.' % src)

def main(options):
    if not os.path.isabs(options.builddir):
        options.builddir = os.path.join(PROJECT_DIR, options.builddir)

    if options.clean:
        clean()
        common.rmdir(options.builddir)

    common.mkdir(options.builddir)

    if options.all or options.dist:
        options.cpp = True
        options.csharp = True
        options.gui = True
        options.java = True
        options.javascript = True
        options.python = True
        options.rpg = True

    # install dependencies
    common.run_cmd('mvn', ['install:install-file', '-DgroupId=com.frontendart.columbus',
                    '-DartifactId=graphsupportlib', '-Dversion=1.0',
                    '-Dpackaging=jar', '-Dfile=lib/graphsupportlib-1.0.jar'])
    common.run_cmd('mvn', ['install:install-file', '-DgroupId=com.frontendart.columbus',
                    '-DartifactId=graphlib', '-Dversion=1.0', '-Dpackaging=jar',
                    '-Dfile=lib/graphlib-1.0.jar'])

    # sonarqube-core-plugin
    mvn_install('sonarqube-core-plugin')

    # sourcemeter-analyzer-base
    mvn_install('sonarqube-analyzers/sourcemeter-analyzer-base')

    # sonarqube-gui-plugin
    if options.gui:
        usersguide()
        mvn_install('sonarqube-gui-plugin')

    # analyzers
    if options.cpp:
        mvn_install('sonarqube-analyzers/sourcemeter-analyzer-cpp')
    if options.csharp:
        mvn_install('sonarqube-analyzers/sourcemeter-analyzer-csharp')
    if options.java:
        mvn_install('sonarqube-analyzers/sourcemeter-analyzer-java')
    if options.javascript:
        mvn_install('sonarqube-analyzers/sourcemeter-analyzer-javascript')
    if options.python:
        mvn_install('sonarqube-analyzers/sourcemeter-analyzer-python')
    if options.rpg:
        mvn_install('sonarqube-analyzers/sourcemeter-analyzer-rpg')

    target_dir = os.path.join(options.builddir, PACKAGENAME)
    common.mkdir(target_dir)
    common.mkdir('%s/doc' % target_dir)
    common.mkdir('%s/plugins' % target_dir)
    try:
        shutil.copy('doc/UG.html', '%s/doc' % target_dir)
        shutil.copy('README.md', target_dir)
    except OSError:
        print('Cannot copy doc files.')
    copy_jars('src/sonarqube-core-plugin/target/', '%s/plugins' % target_dir)
    copy_jars('src/sonarqube-gui-plugin/target/', '%s/plugins' % target_dir)
    copy_jars('src/sonarqube-analyzers/sourcemeter-analyzer-cpp/target/',
              '%s/plugins' % target_dir)
    copy_jars('src/sonarqube-analyzers/sourcemeter-analyzer-csharp/target/',
              '%s/plugins' % target_dir)
    copy_jars('src/sonarqube-analyzers/sourcemeter-analyzer-java/target/',
              '%s/plugins' % target_dir)
    copy_jars('src/sonarqube-analyzers/sourcemeter-analyzer-javascript/target/',
              '%s/plugins' % target_dir)
    copy_jars('src/sonarqube-analyzers/sourcemeter-analyzer-python/target/',
              '%s/plugins' % target_dir)
    copy_jars('src/sonarqube-analyzers/sourcemeter-analyzer-rpg/target/',
              '%s/plugins' % target_dir)

    if options.dist:
        tarfile_name = os.path.join(options.builddir,
                                    '%s.tar.gz' % PACKAGENAME)
        tar = tarfile.open(tarfile_name, 'w:gz')
        tar.add(target_dir, arcname=PACKAGENAME)
        tar.close()

    print('\nBUILD SUCCESS\n')
if __name__ == "__main__":
    main(get_arguments())
