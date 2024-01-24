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

package com.sourcemeter.analyzer.base.core;

import org.sonar.api.platform.Server;
import org.sonar.api.platform.ServerStartHandler;

public class VersionChecker implements ServerStartHandler {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onServerStart(Server server) {
        String[] version = server.getVersion().split("\\.");

        int majorVersion = Integer.parseInt(version[0]);
        int minorVersion = Integer.parseInt(version[1]);

        if (
            majorVersion < 8 || // we do not support it any more as componentKey is changed to component in WEB API
            majorVersion > 9 || // we do not support versions later than 9.9 LTS
            ( majorVersion == 8 && minorVersion < 8 ) // 8.8 introduced an API change we used.
        ) {
            throw new UnsupportedOperationException("FrontEndART SourceMeter plugin does not support your SonarQube server version (" + server.getVersion()
                    + "). For more information about the supported versions, check http://www.frontendart.com/ or read the documention of the plugin.");
        }
    }
}
