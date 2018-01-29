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
SM.pageBuilder.help = {};

SM.pageBuilder.help.build = function() {
  var tabs = [];
  tabs.push('<li><a href="#general">General</a></li>');
  SM.languages.forEach(function(lang) {
    tabs.push('<li><a href="#' + lang.id + '">' + lang.symbol + '</a></li>');
  });

  var divs = [];
  divs.push('<div id="general"></div>');
  SM.languages.forEach(function(lang) {
    divs.push('<div id="' + lang.id + '"></div>');
  });

  var html = [
    '<h1 class="sm-page-title">SourceMeter Help</h1>',
    '<div id="tabs">',
    '  <ul>',
         tabs.join('\n'),
    '  </ul>',
       divs.join('\n'),
    '</div>'
  ].join("\n");

  SM.getRoot().html(html);
  $('#sm-page-content #tabs').tabs();

  SM.pageBuilder.help.fixStyle();
};

/**
 * fixStyle - fixes style issues with the stylesheet the UsersGuides come with
 * inside of the <iframe>-s:
 *   * width is adjusted to fit inside the viewport of the iframe
 *   * height is adjusted to precisely fit into the browser viewport
 *
 * @returns {type} Description
 */
SM.pageBuilder.help.fixStyle = function() {
  var newHeight = $(window).height()
    - parseInt($('div#footer').css("height"))
    - $('nav#global-navigation').height()
    - 80;

  $('div.sm-help #tabs > div').css('height', newHeight + 'px');
  $('#sm-page-content #tabs iframe').contents().find("body").css(
    'width',
    ($('#sm-page-content #tabs').width() - 150) + 'px'
  );
};

SM.pageBuilder.help.iframePostLoad = function(langId) {
  // fix css width & height. (original html had a hard coded with of whatewer...)
  $('#sm-page-content #tabs #' + langId + ' iframe').contents().find("body").css(
    'width',
    ($('#sm-page-content #tabs').width() - 150) + 'px'
  );

  $(window).on('resize', SM.pageBuilder.help.fixStyle);
  $('#sm-page-content #tabs').on("tabsactivate", SM.pageBuilder.help.fixStyle);
  $("body").css("overflow-y", "hidden");
  SM.state.help.cleanup.push(function() {
    $(window).off('resize');
    $("body").css("overflow-y", '');
  });
};
