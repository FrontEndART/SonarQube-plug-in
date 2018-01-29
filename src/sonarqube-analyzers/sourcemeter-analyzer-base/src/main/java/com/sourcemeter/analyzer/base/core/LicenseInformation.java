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

import java.util.Set;
import java.util.TreeSet;

/**
 * Class for storing SourceMeter license information for every SM tool on
 * projects. License can be: full, limited, inactive.
 */
public class LicenseInformation {

    public static final String FULL = "full";

    public static final String LIMITED = "limited";

    public static final String INACTIVE = "inactive";

    private final Set<String> full;

    private final Set<String> limited;

    private final Set<String> inactive;

    /**
     * Creates an empty LicenseInformation.
     */
    public LicenseInformation() {
        full = new TreeSet<String>();
        limited = new TreeSet<String>();
        inactive = new TreeSet<String>();
    }

    /**
     * A tool is added to the current license informations with the given
     * license
     *
     * @param tool A tool to be add to the current license.
     * @param license A given license for the tool.
     */
    public void addTool(String tool, String license) {
        if (FULL.equals(license)) {
            full.add(tool);
        } else if (LIMITED.equals(license)) {
            limited.add(tool);
        } else if (INACTIVE.equals(license)){
            inactive.add(tool);
        }
    }

    /**
     * Returns the Set of tools with full license.
     *
     * @return Full license tools.
     */
    public Set<String> getFull() {
        return full;
    }

    /**
     * Returns a Set of tools with limited license.
     *
     * @return Limited license tools.
     */
    public Set<String> getLimited() {
        return limited;
    }

    /**
     * Returns the Set of tools which were inactive during the analyzis.
     *
     * @return Inactive tools.
     */
    public Set<String> getInactive() {
        return inactive;
    }
}
