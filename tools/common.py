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

import platform
import subprocess
import os
import shutil

def run_cmd(cmd, args=[], quiet=False, work_dir=None):
    if not quiet:
        print(cmd, args)
    try:
        ret_code = 0
        if platform.system() == 'Windows':
            ret_code = subprocess.call([cmd] + args, cwd=work_dir, shell=True)
        else:
            ret_code = subprocess.call([cmd] + args, cwd=work_dir)
        if ret_code != 0:
            print("[Failed - command exited with %s]" % ret_code)
            exit(ret_code)
    except OSError as e:
        print("[Failed - %s] %s" % (cmd, e.strerror))
        exit(1)

def mkdir(directory):
    if not os.path.exists(directory):
        os.makedirs(directory)

def rmdir(directory):
    if os.path.exists(directory):
        shutil.rmtree(directory)
