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
 * this script loads all the resources that pages from the plugin use.
 */

SM.loader = {};

SM.loader.src = {
  cdn: {
    scripts: [ // these scripts will be loaded on this page, replacing f.e. the older jquery or underscore that sonarqube uses.
      // this kind of loading is not allowed in SonarQube CPS. Keep it, we might use once.
    ],
    styles: [ // these stylesheets will be loaded
      // this kind of loading is not allowed in SonarQube CPS. Keep it, we might use once.
    ]
  },
  local: { // these get baseUrl prepended automatically
    scripts: [
      'ext/jquery.min.js',
      'ext/jquery-ui.min.js',
      'ext/lodash.min.js', // these elements were loaded externally before init.js, keep the order
      'init.js',
      'lib/highlightjs/highlight.pack.js',
      'lib/diff2html/diff2html.min.js',
      'lib/diff2html/diff2html-ui.min.js',
      'lib/jsdiff/diff.min.js',
      'js/tools/Subscribable.js',
      'js/dashboard/Metric.js',
      'js/dashboard/MetricLoader.js',
      'js/dashboard/pageBuilder.js',
      'js/dashboard/SM-clone-widget.js',
      'js/dashboard/SM-logic-widget.js',
      'js/dashboard/main.js',
      'js/help/pageBuilder.js',
      'js/help/main.js',
      'js/cloneViewer/pageBuilder.js',
      'js/cloneViewer/main.js',
      'js/cloneViewer/RawFileLoader.js',
      'js/cloneViewer/CloneClassSelector.js',
      'js/cloneViewer/CloneInstanceSelector.js',
      'js/cloneViewer/CloneViewer.js',
      'js/cloneViewer/SideBySideDiffer.js',
      'last.js'
    ],
    styles: [
      'css/ext/font-awesome.min.css',
      'css/ext/github.min.css',
      'css/ext/octicons.css',
      'css/sm-widget.css',
      'css/sm-icons.css',
      'css/license.css',
      'css/dashboard.css',
      'css/jquery-ui/jquery-ui.min.css',
      'css/jquery-ui/jquery-ui.structure.min.css',
      'css/jquery-ui/jquery-ui.theme.min.css',
      'lib/diff2html/diff2html.min.css',
      'css/cloneViewer.css'
    ]
  }
};

/**
 *  Loads a script into the DOM with src="url".
 *  if 'basePath' is defined, it gets prepended to 'url'
 *  Only loads the script if it hasn't been appended to the document before
 *
 *  Copied to help.js
 */
SM.loader.loadScript = function(url, basePath) {
  basePath = basePath ? basePath : "";
  if (document.getElementById(basePath + url) === null) { //checks if the script is already loaded
    var script = document.createElement('script');

    script.type = 'text/javascript';
    script.async = false;
    script.src = basePath + url;
    script.setAttribute("id", basePath + url);

    document.head.append(script);
  }
};

/**
 *  Loads a stylesheet into the DOM with href="url".
 *  if 'base' is defined, it gets prepended to 'url'
 *  Only loads the stylesheet if it hasn't been appended to the document before
 */
SM.loader.loadStyle = function(url, base) {
  base = base ? base : "";
  if (document.getElementById(base + url) === null) { // checks if the stylesheet is already loaded
    var style = document.createElement('link');

    style.rel = "stylesheet";
    style.href = base + url;
    style.setAttribute("id", base + url);

    document.head.append(style);
  }
};

/**
 *  Loads scripts and stylesheets from CDN or local. local scripts get 'basePath' prepended.
 *  input = {
 *    cdn: {
 *      scripts: [...],
 *      styles: [...]
 *    },
 *    local: {
 *      scripts: [...],
 *      styles: [...]
 *    }
 *  }
 */
SM.loader.loadSources = function(sources, base) {
  sources.cdn.styles.forEach(function(styleSrc) {
    SM.loader.loadStyle(styleSrc);
  });
  sources.cdn.scripts.forEach(function(scriptSrc) {
    SM.loader.loadScript(scriptSrc);
  });

  if (base) {
    sources.local.styles.forEach(function(styleSrc) {
      SM.loader.loadStyle(styleSrc, base);
    });
    sources.local.scripts.forEach(function(scriptSrc) {
      SM.loader.loadScript(scriptSrc, base);
    });
  }
};

SM.loader.loadSources(SM.loader.src, SM.basePath);

