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

/*
*  This file schould contain all simple DOM creation code.
*  So to say this file replaces the index.html file of the plugin,
*  it will (, well it should!) be loaded first before all the other
*  scripts execute.
*/
SM.pageBuilder.dashboard = {};
SM.pageBuilder.dashboard.build = function() {
  SM.getRoot().html([
    '  <div class="sm-page-header">',
    '    <img id="sm-logo" height="36px" src="' + window.baseUrl + '/static/SourceMeterGUI/graphics/MainLogo.png">',
    '    <h1 class="sm-page-title">Dashboard</h1>',
    '  </div>',
    '  <div id="div1">',
    '  </div><div id="div2">',
    '  </div><div id="div3">',
    '  </div><div id="div4">',
    '  </div><div id="div5">',
    '  </div>',
    // license table
    '<div>',
    '  <p style="text-align:center;font-size: 11px;">',
    '    <a href="https://sourcemeter.com" target="SourceMeter">SourceMeter 9.1</a>',
    '    <a href="https://github.com/FrontEndART/SonarQube-plug-in" target="SourceMeter_github">plug-in</a>',
    '    for SONARQUBE™ platform 7.9 v2.0.0',
    '  </p>',
    '  <div style="padding-top: 6px;">',
    '    <table id="sourcemeter_license_table" class="license_table" align="center">',
    '      <tbody>',
    '        <tr class="sm_license_header">',
    '          <td>Language</td>',
    '          <td>Full functionality</td>',
    '          <td>Limited functionality</td>',
    '          <td>Not executed</td>',
    '        </tr>',
    '      </tbody>',
    '    </table>',
    '  </div>',
    '</div>'
  ].join(""));
};

SM.pageBuilder.dashboard.appendToLicenseTable = function(options) {
  /* format options:
   *   options = {lang:string, json: >>SM:[lang]_license'<< }
   */
  var full = "";
  var limited = "";
  var inactive = "";
  if (options.json.full.length > 0) {
    full = options.json.full.join(', ');
  }

  if (options.json.limited.length > 0) {
    limited = options.json.limited.join(', ');
  }

  if (options.json.inactive.length > 0) {
    inactive = options.json.inactive.join(', ');
  }

  $("#sourcemeter_license_table").append([
    '<tr>',
    '   <td>' + options.lang + '</td>',
    '   <td class="license_status_full">' + full + '</td>',
    '   <td class="license_status_limited">' + limited + '</td>',
    '   <td class="license_status_inactive">' + inactive + '</td>',
    '</tr>'
  ].join("\n"));
};
