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

window.registerExtension('SourceMeterGUI/cloneViewer', function(options) {
  /**
   *  Manages the 'page' initialization of the plugin. The content of this file
   *  must be copied and modified if it is needed to add a new page to load the
   *  SM GUI plugin. The page initialization code must be placed into the body
   *  of this function. This function will be called after 'global.js' and 'loader.js'
   *  files has been loaded and executed.
   *
   * @returns {undefined} undefined
   */
  var init = function() {
    SM.cloneViewer.main();
  }
  /** the statekey identifies somethings state in SM.state. use SM.state[stateKey]
   *  to store information, and reload it when the your page is loaded again*.
   *  This can make page loads happen faster for example data retrieval from
   *  databases dont have to happen the second time, simply reload the data
   *  stored in SM.state[stateKey]. Change this to your hearts content.
   *  *: information will stay there until an explicit page load occures, for more
   *  info see how react works, sonarQube uses react.
   */
  var stateKey = options.component.key;

  /*  DO NOT EDIT BELOW THIS LINE (you can, but should not have to) */

  /**
  * waitForNDo - Uses setInterval to test for function {test}. When test returns
  * true the first time exe will be executed, and the interval will be cleared.
  *
  * @param {function} test the tester function, returns logical value.
  * @param {function} exe  the task function, executed after test() gets true
  * @param {int} time milliseconds to pass between subsequent tests. default = 100
  *
  * @returns {undefined} undefined
  */
  var waitForNDo = function(test, exe, time) {
    time = time || 100;
    var tester = setInterval(function() {
      if (test()) {
        clearInterval(tester);
        exe();
      }
    }, time);
  };

  /**
   * load - Manages the loading of the SM-gui-plugin itself. Ensures loading
   * happens only once. Assumes SM exists.
   *
   * @returns {undefined} undefined
   */
  var load = function() {
    SM.exportOptions(options); // this is loaded already in globals.js

    if (!SM.state.hasOwnProperty(stateKey)) {
      SM.state[stateKey] = {}; // create it the first time
    }
    SM.state[stateKey].isDisplayed = true;

    SM.state[stateKey].cleanup = [];
    SM.state[stateKey].cleanup.push(function() {
      SM.state[stateKey].isDisplayed = false;
    });

    // Don't load everything twice, since SQ uses react, and never loads a page from scratch.
    // Might need to change js loading to a more modular solution, but it seems to work so far.
    if (SM.SmGuiExtensionAlreadyLoaded) {
      init();
      return;
    } // else {...

    // Load the 'loader.js' script after the loader loaded everything.
    var scriptLoad = document.createElement('script');
    scriptLoad.type = 'text/javascript';
    scriptLoad.async = true;
    scriptLoad.src = window.baseUrl + "/static/SourceMeterGUI/loader.js";

    document.head.appendChild(scriptLoad);

    waitForNDo(
      function() {
        return SM.SmGuiExtensionAlreadyLoaded;
      },
      function(){
        SM.globalinit();
        init();
      },
      10
    );
  };

  options.el.textContent = '';
  options.el.id = 'sm-page-content';
  options.el.className = 'page';

  // Load global vars into global scope.
  var scriptGlob = document.createElement('script');
  scriptGlob.type = 'text/javascript';
  scriptGlob.src = window.baseUrl + "/static/SourceMeterGUI/globals.js";

  document.head.appendChild(scriptGlob);
  waitForNDo(
    function() {
      return (typeof SM !== 'undefined') && SM.globalsInitialized;
    },
    load,
    10
  );

  return function() {
    SM.state[stateKey].cleanup.forEach(function(task) {
      task();
    });
  };
});
