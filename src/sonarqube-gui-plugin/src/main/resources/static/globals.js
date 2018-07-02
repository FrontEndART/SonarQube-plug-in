/**
 * Copyright (c) 2014-2018, FrontEndART Software Ltd.
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

// "GLOBAL"-like vars
if (typeof SM === 'undefined') { // ensure this only runs once
  var SM = {};

  SM.SmGuiExtensionAlreadyLoaded = (typeof SM.SmGuiExtensionAlreadyLoaded !== 'undefined')
    ? Sm.SmGuiExtensionAlreadyLoaded
    : false;
  SM.pageBuilder = {};
  SM.rootID   = 'sm-page-content';
  SM.basePath = "/static/SourceMeterGUI/";
  SM.options  = null;
  SM.isDisplayed = true;
  SM.state = [];
  SM.languages  = [
    { id: 'JAVA',   symbol: 'Java',   pluginId: 'SourceMeterAnalyzerjava'   },
    { id: 'CPP',    symbol: 'C++',    pluginId: 'SourceMeterAnalyzercpp'    },
    { id: 'CSHARP', symbol: 'C#',     pluginId: 'SourceMeterAnalyzercsharp' },
    { id: 'PYTHON', symbol: 'Python', pluginId: 'SourceMeterAnalyzerpython' },
    { id: 'RPG',    symbol: 'RPG',    pluginId: 'SourceMeterAnalyzerrpg'    }
  ];
  SM.languages.helpPageLoaded = {};

  SM.getRoot = function() {
    return $('#sm-page-content');
  };

  SM.exportOptions = function(opts) {
    SM.options = opts;
  };

  /**
  * waitForNDo - Uses setInterval to test for function {test}. When test returns
  * true the first time exe will be executed, and the intervall will be cleared.
  *
  * @param {function} test the tester function, returns logical value.
  * @param {function} exe  the taskj function, executed after test() gets true
  * @param {int} time milliseconds to pass between subsequent tests. default = 100
  *
  * @returns {undefined} undefined
  */
  SM.waitForNDo = function (test, exe, time) {
    time = time || 100;
    var tester = setInterval(function() {
      if (test()) {
        clearInterval(tester);
        exe();
      }
    }, time);
  };

  /**
   * binds an objects functions this scope to itself, so it doesnt matter from
   * where a function is called, this is always going to be the object the
   * function belongs to.
   * @param  {object} self the object we wish to do the binding on
   */
  SM.bindFunctions = function(self){
    Object.keys(self).forEach((prop)=>{
      if(self.hasOwnProperty(prop) && typeof self[prop] === "function"){
      self[prop] = self[prop].bind(self);
      }
    });
  };

  SM.globalsInitialized = true;
}
