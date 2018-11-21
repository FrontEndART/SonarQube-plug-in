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

/**
 * Main entrypoint for the SM-gui-plugin.
 * Gets data from the web api, and displays it on the page.
 */
SM.help = {};
SM.help.main = function() {

  SM.pageBuilder.help.build();

  var loadUG = function(lang) {
    var url = '/static/' + lang.pluginId + '/help/usersguide.html';
    $.get(url)
      .done(function(response, status, xhr) {
        if (!SM.state.help.isDisplayed) {
          return;
        }

        var iframe = [
          '<iframe ',
          '  title="UG-"' + lang.id,
          '  width="100%"',
          '  height="100%"',
          '  frameborder="0"', // HTML4 only
          '  marginheight="0"', // HTML4 only
          '  marginwidth="0"', // HTML4 only
          '  onload="SM.pageBuilder.help.iframePostLoad(\'' + lang.id + '\');"',
          '  src="' + url + '">',
          '</iframe>'
        ].join('\n');

        $('#tabs #' + lang.id).html(iframe);
      })
      .fail(function(response, status, xhr) {
        if (!SM.state.help.isDisplayed) {
          return;
        }

        var msg = [
          '<div class="ui-state-error ui-state-highlight ui-corner-all" style="padding:10px"> ',
          '  In order to see the users guide for the SourceMeter ' + lang.symbol + ' ',
          '  scanner plugin, please put the ',
          '  sourcemeter-analyzer-' + lang.id.toLowerCase() + '-plugin-1.1.0.jar',
          '  into the extensions/plugins directory of your SonarQube instance. ',
          '</div>'
        ].join('/n');
        $('#tabs #' + lang.id).html(msg);
      });
  };

  var general = {};
  general.id = 'general';
  general.pluginId = 'SourceMeterGUI';

  // load the UG-s into the iframes, if they exist. else print error msg.
  loadUG(general);
  SM.languages.forEach(loadUG);

}; // END OF function main
