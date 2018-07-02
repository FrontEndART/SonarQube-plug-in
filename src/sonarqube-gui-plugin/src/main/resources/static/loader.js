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
      "https://ajax.googleapis.com/ajax/libs/jquery/3.2.1/jquery.min.js",
      "https://code.jquery.com/ui/1.12.1/jquery-ui.min.js",
      "https://cdnjs.cloudflare.com/ajax/libs/lodash.js/4.17.4/lodash.min.js"
    ],
    styles: [ // these stylesheets will be loaded
      "https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css",
      "https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.12.0/styles/github.min.css"
    ]
  },
  local: { // these get baseUrl prepended automatically
    scripts: [
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

/*
 *  Load the Usersguides into variables for later use. Ideally theese would
 *  be displayed as the help pages on the SourceMeter Help page, but I imple-
 *  mented this function after I wrote the SourceMeter Help page, and couldnt
 *  figure out a fast and easy way to refactor the code, so refacoring remains
 *  a todo for later. The usersguides loaded here are used by the Metric.js
 *  class, to fetch tooltip data.
 */
SM.languages.forEach(function(lang, i) {
  $.get( '/static/' + lang.pluginId + '/help/usersguide.html')
    .done(function(data, status, xhr) {
      SM.languages[i].helpPage = data;
      SM.languages.helpPageLoaded[lang.id] = true;
    })
    .fail(function(data, status, xhr) {
      SM.languages[i].helpPage = [
        '<div class="ui-state-error ui-state-highlight ui-corner-all" style="padding:10px"> ',
        '  In order to see the users guide for the SourceMeter ' + lang.symbol + ' ',
        '  scanner plugin, please put the ',
        '  sourcemeter-analyzer-' + lang.id.toLowerCase() + '-plugin-1.0.0.jar',
        '  into the extensions/plugins directory of your SonarQube instance. ',
        '</div>'
      ].join('/n');
    });
});
