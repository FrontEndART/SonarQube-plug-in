#!/usr/bin/env python

# Copyright (c) 2014-2016, FrontEndART Software Ltd.
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
import urllib2
import os
import shutil
import subprocess

def get_arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument('--init', action='store_true',
                        help='Initialize')
    return parser.parse_args()

def run_check(runnable):
    sys.stderr.write('Test command: %s\n' % ' '.join(runnable))

    try:
        ret = subprocess.check_call(runnable)
    except subprocess.CalledProcessError as err:
        return err.returncode

    return ret

def download_sq_server(version):
    sq = urllib2.urlopen('https://sonarsource.bintray.com/Distribution/'
                         'sonarqube/sonarqube-%s.zip' % version)
    dl = sq.read()
    with open('sonarqube-%s.zip' % version, 'w') as f:
        f.write(dl)

def main(options):
    # 0) Try to build the plugins with 'build.py'

    download_sq_server('7.2.1')
    # 1) download sonar-scanner
    # 2) unzip both server and scanner
    # 3) copy the plugins into the server dir
    # 4) start the server with the defult config
    # 5) Validate the server is started succesfully

if __name__ == "__main__":
    main(get_arguments())
