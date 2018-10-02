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

import argparse
from urllib.request import urlopen
import os
import shutil
import subprocess
import zipfile
from time import sleep
import platform

import common

def get_arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument('--init', action='store_true', default=False,
                        help='Initialize')
    parser.add_argument('-sv', '--server-version', default='7.2',
                        help='Version of the SQ server (default=%(default)s)')
    parser.add_argument('-scv', '--scanner-version', default='3.2.0.1227',
                        help='Version of the SQ scanner (default=%(default)s)')
    parser.add_argument('-plgf', '--plugins-folder', default=None,
                        help='Folder containing the plugins to be tested (default=%(default)s)')
    parser.add_argument('-prjf', '--projects-folder', default='projectsfolder',
                        help='Folder containing the project to be analyzed (default=%(default)s)')
    parser.add_argument('-noa', '--number-of-attempts', default=20,
                        help='Number of checking the status of SonarQube server (default=%(default)s times)')
    parser.add_argument('-wait', '--wait', default=10,
                        help='Wait between checking the status of SonarQube server (default=%(default)s seconds)')
    parser.add_argument('-cf', '--client-folder', default='test',
                        help='Folder for downloading and unzipping SQ server and scanner (default=%(default)s)')
    parser.add_argument('--print-log', action='store_true',
                        help='Prints the SQ log files to the screen')

    return parser.parse_args()

def run_check(runnable):
    sys.stderr.write('Test command: %s\n' % ' '.join(runnable))

    try:
        ret = subprocess.check_call(runnable)
    except subprocess.CalledProcessError as err:
        return err.returncode

    return ret

def download_sq_server(version, dst):
    print('Downloading: SonarQube server (version %s)...' % version)
    sq = urlopen('https://sonarsource.bintray.com/Distribution/'
                         'sonarqube/sonarqube-%s.zip' % version)
    dl = sq.read()
    path = os.path.join(dst, 'sonarqube-%s.zip' % version)
    with open(path, 'wb') as f:
        f.write(dl)
    print('SonarQube server download is completed! (version %s)' % version)

def download_sq_scanner(version, system, dst):
    system = system.lower()
    print('Downloading: SonarQube scanner (version %s)...' % version)
    sq = urlopen('https://sonarsource.bintray.com/Distribution/'
                         'sonar-scanner-cli/sonar-scanner-cli-%s-%s.zip' % (version, system))
    dl = sq.read()
    path = os.path.join(dst, 'sonar-scanner-cli-%s-%s.zip' % (version, system))
    with open(path, 'wb') as f:
        f.write(dl)
    print('SonarQube scanner download is completed! (version %s)' % version)

def unzip(file, dst):
    print('Unzipping files...')
    with zipfile.ZipFile(file, "r") as zip_ref:
        zip_ref.extractall(dst)
    dst = os.path.join(os.getcwd(), dst)
    print('Unzip finished! (%s)' % dst)

def copy_all_files_from_folder(src, dst):
    if src:
        src_files = os.listdir(src)
        for file_name in src_files:
            full_file_name = os.path.join(src, file_name)
            if (os.path.isfile(full_file_name)):
                shutil.copy(full_file_name, dst)
    else:
        plugins = ['core', 'gui']
        for plugin in plugins:
            path = ['src', 'sonarqube-%s-plugin' % plugin, 'target', 'sourcemeter-%s-plugin-1.0.0.jar' % plugin]
            path  = os.path.join(*path)
            shutil.copy(path, dst)

        languages = ['cpp', 'csharp', 'java', 'python', 'rpg']
        for language in languages:
            path = ['src', 'sonarqube-analyzers', 'sourcemeter-analyzer-%s' % language, 'target', 'sourcemeter-analyzer-%s-plugin-1.0.0.jar' % language]
            path  = os.path.join(*path)
            shutil.copy(path, dst)
    print('Copy finished!')

def start_sq_server(version, system, dst):
    cmd = ''
    cwd = os.getcwd()
    if system == 'Windows':
        sonar_location = cwd + '\\' + dst + '\\' + 'sonarqube-%s\\bin\\windows-x86-64\\StartSonar.bat' % version
        common.run_cmd('start', [sonar_location])
    elif system == 'Linux':
        sonar_location = cwd + '/' + dst + '/sonarqube-%s' % version

        temp = sonar_location
        for root, dirs, files in os.walk(temp):
            for file in files:
                file_path = os.path.join(root, file)
                os.chmod(file_path, 0o744)

        cmd = os.path.join(sonar_location, 'bin/linux-x86-64/sonar.sh')
        common.run_cmd(cmd, ['start', '&'])
    print('Starting SQ server...')

def validate_running_of_sq_server(version, number_of_attempts, wait):
    number_of_attempts = int(number_of_attempts)
    while not number_of_attempts == 0:
        try:
            contents = urlopen('http://localhost:9000/api/system/ping').read()
            return True
        except:
            print('SonarQube is not started yet, rechecking...' + ' (%d attempt(s) left)' % number_of_attempts)
            number_of_attempts -= 1
            sleep(float(wait))
    return False

def analyze(scanner_version, project_folder, system, dst):
    cmd = ''
    cwd = os.getcwd()
    scanner_location =  ''
    if system == 'Windows':
        scanner_location = cwd  + '\\' + dst + '\\sonar-scanner-%s-windows\\bin\\sonar-scanner.bat' % scanner_version
        os.chdir(project_folder)
        common.run_cmd('start', [scanner_location])
    elif system == 'Linux':
        cmd = cwd + '/' + dst + '/sonar-scanner-%s-linux/bin/sonar-scanner&' % scanner_version
        os.chdir(project_folder)
        common.run_cmd(cmd, [])
    os.chdir('..')

def print_log(version, path):
    path = os.path.join(path, 'sonarqube-%s' % version, 'logs')
    for file in [f for f in os.listdir(path) if os.path.isfile(os.path.join(path, f))]:
        print('File name: ' + file)
        with open(os.path.join(path, file), 'r') as f_in:
            print(f_in.read())

def main(options):
    server_version = options.server_version
    scanner_version = options.scanner_version
    src_of_the_plugins = options.plugins_folder
    src_of_the_project = options.projects_folder
    noa = options.number_of_attempts
    wait = options.wait
    system = platform.system()
    dst = options.client_folder
    print_log_files = options.print_log
    common.mkdir(dst)

    # 0, a) Try to build the plugins with 'build.py'

    if system == 'Windows':
        common.run_cmd('py', ['-3', 'build.py', '--all'])
    elif system == 'Linux':
        common.run_cmd('python3', ['tools/build.py', '--all'])

    if options.init == True:
        # 0, b) download sonar-server'

        download_sq_server(server_version, dst)

        # 1) download sonar-scanner

        download_sq_scanner(scanner_version, system, dst)

        # 2) unzip both server and scanner

        src = os.path.join(dst, 'sonarqube-%s.zip' % server_version)
        unzip(src, dst)
        if 'Windows' == system:
            src = os.path.join(dst, 'sonar-scanner-cli-%s-windows.zip' % scanner_version)
        elif 'Linux' == system:
            src = os.path.join(dst, 'sonar-scanner-cli-%s-linux.zip' % scanner_version)
        unzip(src, dst)

    # 3) copy the plugins into the server dir

    path = [dst, 'sonarqube-%s' % server_version, 'extensions', 'plugins']
    path = os.path.join(*path)
    copy_all_files_from_folder(src_of_the_plugins, path)

    # 4) start the server with the defult config

    start_sq_server(server_version, system, dst)

    # 5) Validate the server is started succesfully
    # 6) Analyze the given project

    sleep(60)
    if validate_running_of_sq_server(server_version, noa, wait):
        print('SonarQube started properly!')
    else:
        print(('SonarQube did not start in time (-noa=%s (number of attempts))' % (noa)))
        if print_log_files:
            print_log(server_version, dst)
        exit(1)

if __name__ == "__main__":
    main(get_arguments())
