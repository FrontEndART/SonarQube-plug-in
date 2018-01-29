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

SM.MetricLoader = new (function() { // Singleton object
  var self = this;
  var config = {
    reversedMetrics: [ "CLOC", "DLOC", "TCLOC", "TPDA", "TPUA", "CD", "TCD", "AD", "TAD"]
  };
  this.metrics = {}; //of Metric
  this.metricsToGet = {}; // of Metric
  this.callbacks = {}; //of Function
  this.countdown = undefined; // Timeout object holder
  this.ready = false; // becomes true when UG-s are loaded
  /**
   *  This function handles smart data filling of a Metric object.
   *  If this function is called frequently , subsequent calls will
   *  be batched together into one web api call to the back-end. If the Metric
   *  was requested previously, it is served up from an inner cache, it will not
   *  be updated from the databasse in this case.
   *
   *  @param metric The metric object. it has to at least have title,
   *                lang, and level data filled in.
   *  @param callback this function is called, when data for the given metric is
   *                  successfully retrieved, and reciewes one argument which is
   *                  the completed Metric object. It is called twice: once when
   *                  baselines are loaded, and a second time when the metricinfo
   *                  has loaded.
   */
  this.requestMetric = function(metric, callback) {
    // collapse frequent requests into a single one.
    // This also substitutes a common lock for this function and getEm.
    // thus getEm wil veryvery likely not fire during the execution of
    // requestMetric. (JS doesnt support multithreading => no native locks)
    var resetCountdown = false;
    if (this.countdown !== undefined) {
      clearTimeout(this.countdown);
      this.countdown = undefined;
      resetCountdown = true;
    }

    if (this.metrics[metric.getUniqueKey()]) {
      // requested earlyer, fetched already -> returning that one
      metric.baseline = this.metrics[metric.getUniqueKey()].baseline;
      metric.helpText = this.metrics[metric.getUniqueKey()].helpText;
      if (callback) {
        callback(this.metrics[metric.getUniqueKey()]);
      }
    } else if (this.metricsToGet[metric.getUniqueKey()]) { // requested earlyer, NOT fetched yet
      // add callback as normally.
      if (callback) {
        this.callbacks[metric.getUniqueKey()].push(callback);
      }
      // add callback that fills data into supplied metric object.
      this.callbacks[metric.getUniqueKey()].push(function(met) {
        metric.baseline = met.baseline;
        metric.helpText = met.helpText;
      });
      resetCountdown = true;

    } else { // first time this metric was requested, adding it to the to-get list
      this.metricsToGet[metric.getUniqueKey()] = metric;
      this.callbacks[metric.getUniqueKey()] = this.callbacks[metric.getUniqueKey()] || [];
      if (callback) {
        this.callbacks[metric.getUniqueKey()].push(callback);
      }
      resetCountdown = true;
    }

    if (resetCountdown) {
      this.countdown = setTimeout(this.getEm, 1000);
    }
  };

  this.getEm = function() {
    /* private -> eg it is not supposed to be callable from outside,
     * but JS has no privacy... */
    if (Object.keys(this.metricsToGet).length === 0) return; // nothing to do

    if (!this.ready) {
      setTimeout(this.getEm, 1000);
      return false; // try again later
    }

    var helper = {};
    var parsedHTML = {};
    SM.languages.forEach(function(lang, i) {
      parsedHTML[lang.id] = $(lang.helpPage);
    });

    // get the metric baselines
    var uniqueKeys = Object.keys(this.metricsToGet);
    var metric = this.metricsToGet[uniqueKeys[0]];
    var smid = metric.langID.toLowerCase();
    // fix id inconsistencies
    if (smid === "csharp") {
      smid = "cs";
    } else if (smid === "python") {
      smid = "py";
    }
    var key = "sm." + smid + "." + metric.scope.toLowerCase()
              + ".baseline." + metric.title;
    var queryString = key;
    helper[key] = metric.getUniqueKey();

    for (var i = 1; i < uniqueKeys.length; i++) {
      metric = this.metricsToGet[uniqueKeys[i]];
      smid = metric.langID.toLowerCase();
      // fix id inconsistencies
      if (smid === "csharp") {
        smid = "cs";
      } else if (smid === "python") {
        smid = "py";
      }
      key = "sm." + smid + "." + metric.scope.toLowerCase()
            + ".baseline." + metric.title;
      queryString += ", " + key;
      helper[key] = metric.getUniqueKey();
    }
    a = window.SonarRequest.getJSON(location.origin + '/api/settings/values', {
      component: SM.options.component.key,
      keys: queryString
    }).then(function(response) {

      uniqueKeys.forEach(function(key) {
        var instance = self.metricsToGet[key]

        // correct the direction for some metrics
        if (config.reversedMetrics.indexOf(instance.title) != -1) {
          instance.direction = 1;
        }
      })

      response.settings.forEach(function(res) {
        var met = self.metricsToGet[helper[res.key]];
        met.baseline = (res.value)  // if its undefined -> 0
          ? res.value * 1
          : undefined;

        self.metrics[met.getUniqueKey()] = met;
        self.callbacks[met.getUniqueKey()].forEach(function(callback) {
          callback(met);
        });

      });

      // now, after the baseline loaded, parse the metric description
      Object.keys(self.metricsToGet).forEach(function(key) {
        met = self.metricsToGet[key];
        if (typeof self.metrics[met.getUniqueKey()] === "undefined") {
          self.metrics[met.getUniqueKey()] = met;
        }
        var query = parsedHTML[met.langID].filter('h4#' + met.title);
        if (query.length > 0) {
          met.helpText = query[0].outerHTML;

          query.nextUntil('h4, h3, h2', '*').each(
            function(a,t) {
              met.helpText += t.outerHTML;
            }
          );
        } else {
          met.helpText = 'Description not found.';
        }

        self.callbacks[met.getUniqueKey()].forEach(function(callback) {
          callback(met);
        });
      });

      self.metricsToGet = {};

    });
  };
  this.getEm = this.getEm.bind(this);

  x = SM.waitForNDo(
      function() {
        var bool = true;
        SM.languages.forEach(function(lang) {
          bool = bool && (SM.languages.helpPageLoaded[lang.id] == true);
        });
        return bool;
      },
      function() {
        self.ready = true;
      },
      100
    );
})();