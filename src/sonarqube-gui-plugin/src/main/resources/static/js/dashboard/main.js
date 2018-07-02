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

SM.dashboard = {};

/**
 * this function is called to format data before printing it into a <td>.
 * Currently it rounds input up to two decimal places.
 *
 * @param {number} num
 * @returns {string}
 */
SM.formatMetric = function(val, metric) {
  if (val === undefined) {
    // if the object does not have this metric
    return '-';
  }

  metric = metric || { direction: 0 };
  var greenClass = "sm-widget-threshold-green";
  var redClass = "sm-widget-threshold-red";
  var theClass = "";
  if (metric.baseline === undefined) {
    // no extra class, don't color it...
  } else if (metric.direction === -1) { // 1:lesser=worse && larger=better;
    theClass = (val <= metric.baseline) ? greenClass : redClass;
  } else if (metric.direction === 1) { // -1: lesser=better && larger=worse
    theClass = (val >= metric.baseline) ? greenClass : redClass;
  }

  return '<span class="' + theClass + '">' + (Math.round(val * 100) / 100) + '</span>';
};
/**
 * Converts all applicable characters to HTML entities
 *
 * @param {string} str
 * @returns {string}
 */
SM.htmlEncode = function(str) {
  if (!str) {
    return "";
  }

  var html = str.replace(/[^a-z0-9A-Z ]/g, function(c) {
    return "&#" + c.charCodeAt() + ";";
  });

  return html;
};

/**
 * Decodes all HTML entities to characters
 *
 * @param {string} str
 * @returns {string}
 */
SM.htmlDecode = function(value) {
  return $('<div/>').html(value).text();
};

SM.dashboard.loadWidget = function(lang, lvl, key) {
  window.SonarRequest.getJSON(location.origin + '/api/measures/component', {
    componentKey: key,
    metricKeys: "SM_" + lang.id + "_LOGICAL_LEVEL" + lvl
  }).then(function(response) {
    if (response.component.measures.length > 0) {
      var json = JSON.parse(response.component.measures[0].value);

      if (!json.level || json.level.length === 0) {
        return;
      }

      var objects = json.level;
      var objectMetrics = [];

      var scope = json.levelTypes[0].toLowerCase();
      // fix some inconsistencies
      if ((lang.id === "PYTHON"
           || lang.id === "CSHARP"
           || lang.id === "CPP") && scope === "function") {
        scope = "method";
      }

      // fix html encoding
      objects.forEach(function(obj) {
        obj.name = SM.htmlEncode(obj.name);
        obj.langID = lang.id; // nescessary for displaying multilingual scans
        obj.level = scope; // nescessary for displaying multilingual scans
        if (lvl === 1) { // don't schow positions on level1
          obj.positions = [];
        }
      });

      for (var mTitle in objects[0].metrics) {
        if (objects[0].metrics.hasOwnProperty(mTitle)) {
          var metric = new SM.Metric(mTitle, lang.id, scope);
          objectMetrics.push(metric);
        }
      }

      if (typeof SM.state[SM.options.component.key]['level' + lvl] === 'undefined') {
        SM.state[SM.options.component.key]['level' + lvl] = new SM.LogicWidget($("#div" + lvl), {
          data: objects,
          metrics: objectMetrics,
          title: json.levelTypes[0],
          projectId: key,
          langID: lang.id
        });
      } else {
        var widget = new SM.LogicWidget(null, {
          data: objects,
          metrics: objectMetrics,
          title: json.levelTypes[0],
          projectId: key,
          langID: lang.id
        });
        SM.state[SM.options.component.key]['level' + lvl].merge(widget);
      }
    }
  });
};

SM.dashboard.loadCloneWidget = function(lang, key) {
  // clones SM_ {JAVA,CPP,CSHARP,PYTHON,RPG}_CLONE_TREE
  window.SonarRequest.getJSON(location.origin + '/api/measures/component', {
    componentKey: key,
    metricKeys: "SM_" + lang.id + "_CLONE_TREE"
  }).then(function(response) {
    if (response.component.measures.length > 0) {
      var clones = JSON.parse(response.component.measures[0].value);

      if (!clones.cloneClasses || clones.cloneClasses.length === 0) {
        return;
      }

      var cloneClasses = clones.cloneClasses;

      var cloneClassMetrics = [];
      var cloneInstanceMetrics = [];

      // extract clone data
      var metric = null;
      for (var mTitle in cloneClasses[0].cloneClassMetrics) {
        if (cloneClasses[0].cloneClassMetrics.hasOwnProperty(mTitle)) {
          metric = new SM.Metric(mTitle, lang.id, 'cloneclass');
          cloneClassMetrics.push(metric);
        }
      }

      for (mTitle in cloneClasses[0].cloneInstances[0].cloneInstanceMetrics) {
        if (cloneClasses[0].cloneInstances[0].cloneInstanceMetrics.hasOwnProperty(mTitle)) {
          metric = new SM.Metric(mTitle, lang.id, 'cloneinstance');
          cloneInstanceMetrics.push(metric);
        }
      }

      if (typeof SM.state[SM.options.component.key].clone === 'undefined') {
        SM.state[SM.options.component.key].clone = new SM.CloneWidget($("#div4"), {
          data: cloneClasses,
          classMetrics: cloneClassMetrics,
          instanceMetrics: cloneInstanceMetrics,
          projectId: key,
          langID: lang.id
        });
      } else {
        var widget = new SM.CloneWidget(null, {
          data: cloneClasses,
          classMetrics: cloneClassMetrics,
          instanceMetrics: cloneInstanceMetrics,
          projectId: key,
          langID: lang.id
        });
        SM.state[SM.options.component.key].clone.merge(widget);
      }
    }
  });
};

SM.dashboard.reInit = function() { // for fast debug/test reasons
  // clear view
  SM.pageBuilder.dashboard.build();
  SM.dashboard.fetch();
};

SM.dashboard.buildPage = function() {
  SM.pageBuilder.dashboard.build();
}
/**
 * Fetch/refresh data from the back end.
 * @return {void}
 */
SM.dashboard.fetch = function() {
  // clear cached state. nescessary, otherwise duplications appear due to merging
  SM.state[SM.options.component.key].initialized = false;
  SM.state[SM.options.component.key].level1     = undefined;
  SM.state[SM.options.component.key].level2     = undefined;
  SM.state[SM.options.component.key].level3     = undefined;
  SM.state[SM.options.component.key].clone      = undefined;
  SM.state[SM.options.component.key].license    = undefined;
  SM.state[SM.options.component.key].components = undefined;

  // get list of subcomponents:
  window.SonarRequest.getJSON(location.origin + '/api/components/tree', {
      component: SM.options.component.key,
      qualifiers: "BRC"
    }).then(function(response) {
      SM.state[SM.options.component.key].components = [SM.options.component];

      response.components.forEach(function(comp) {
        SM.state[SM.options.component.key].components.push(comp);
      });

      SM.state[SM.options.component.key].components.forEach(function(comp) {
        SM.languages.forEach(function(lang) {
          [1,2,3].forEach(function(lvl) {
            SM.dashboard.loadWidget(lang, lvl, comp.key);
          });

          SM.dashboard.loadCloneWidget(lang, comp.key);
        });

        // load license header
        var versions = ["full", "limited", "inactive"];
        var state = SM.state[SM.options.component.key];
        state.license = {};
        SM.languages.forEach(function(lang) {
          window.SonarRequest.getJSON(location.origin + '/api/measures/component', {
            componentKey: comp.key,
            metricKeys: "SM:" + lang.id.toLowerCase() + "_license"
          }).then(function(response) {
            if (response.component.measures.length <= 0) return;
            if (typeof state.license[lang.symbol] !== "undefined") return; // skip if language already handled

            state.license[lang.symbol] = {};
            versions.forEach(function(version) {
              state.license[lang.symbol][version] = [];
            });
            var json = JSON.parse(response.component.measures[0].value);
            versions.forEach(function(version) {
              json[version].forEach(function(val) {
                if (! _.includes(state.license[lang.symbol][version], val)) {
                  state.license[lang.symbol][version].push(val);
                }
              });
            });

            SM.pageBuilder.dashboard.appendToLicenseTable({
              lang: lang.symbol,
              json: state.license[lang.symbol]
            });
          });

          SM.state[SM.options.component.key].initialized = true;
        });
      });
    });
} // END OF function fetch

/**
 * Main entrypoint for the SM-gui-plugin.
 * Gets data from the web api, and displays it on the page.
 */
SM.dashboard.main = function() {
  SM.dashboard.buildPage();

  if (SM.state[SM.options.component.key].initialized) {
    // reinitialize page
    if (SM.state[SM.options.component.key].level1) {
      SM.state[SM.options.component.key].level1.bindElement($("#div1"));
    }
    if (SM.state[SM.options.component.key].level2) {
      SM.state[SM.options.component.key].level2.bindElement($("#div2"));
    }
    if (SM.state[SM.options.component.key].level3) {
      SM.state[SM.options.component.key].level3.bindElement($("#div3"));
    }
    if (SM.state[SM.options.component.key].clone) {
      SM.state[SM.options.component.key].clone.bindElement($("#div4"));
    }
    if (SM.state[SM.options.component.key].license) {
      Object.keys(SM.state[SM.options.component.key].license).forEach(function(lang) {
        lic = SM.state[SM.options.component.key].license[lang];
        SM.pageBuilder.dashboard.appendToLicenseTable({
          lang: lang,
          json: lic
        });
      });
    }
    return;
  }

  SM.dashboard.fetch();
};
