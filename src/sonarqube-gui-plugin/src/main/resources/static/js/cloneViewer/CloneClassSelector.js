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

SM.CloneClassSelector = function(HTMLelem, options) {
  SM.Subscribable.call(this); // inherit from SM.Subscribable
  this.elem = null; // :DOMElement
  this.cloneClassList; // :CloneClass[]
  this.selected; // :int

  var self = this;

  this.init = function(HTMLelem,options) {
    this.cloneClassList = (typeof options.cloneClassList !== "undefined") ? options.cloneClassList : [];
    this.selected = (typeof options.selected !== "undefined") ? options.selected : undefined;

    this.elem = HTMLelem;
    this.bindElement(HTMLelem);
    this.renderAll();
    SM.MetricLoader.subscribe("finishedAllRequests", function() {
      self.renderMetrics();
    });
    this.renderAll();
  };

  /**
   * Generates HTML of dependant of the current state of the object.
   * Then overwrites the HTML in the visible webpage as well.
   * @return {void}
   */
  this.renderAll = function() {
    this.elem.html("");

    // put together the selectors html string
    var html = [];
    html.push('<div>');
    html.push('<select id="cloneClassSelector">')
    this.cloneClassList.forEach(function(cloneClass, i) {
        html.push("<option value="+i+"> "+cloneClass.name+"</option>");
    });
    html.push('</select>');
    html.push('</div>');
    html.push('<div id="cloneClassMetricsContainer"></div>');

    this.elem.append(html.join(""));

    this.renderMetrics();

    // make selectmenu
    $("#cloneClassSelector").selectmenu();
    $("#cloneClassSelector").val(this.selected).selectmenu("refresh");

  };

  this.renderMetrics = function() {
    var div = $('#cloneClassMetricsContainer');
    var html = [];
    var data = this.cloneClassList[this.selected].cloneClassMetrics;
    var metrics = SM.state[SM.options.component.key].clone.classMetrics;
    metrics.forEach(function(metric) {
      html.push(SM.cloneViewer.getFormatedMetric(data[metric.title], metric));
    });
    div.html(html.join(""));
    $("#cloneClassSelectorContainer .sm-cloneviewer-metric-title-container").tooltip({
      content: function() {
        return $(this).prop('title');
      }
    });
  };

  /**
   * selects a CloneClass, and calls everything that needs to update its state.
   * @param  {int} choice    the id of the cloneclass in the this.CloneClassList
   * @return {void}
   */
  this.select = function (choice) {
    this.selected = choice;
    this.renderMetrics();
    this.emit("onSelect", choice);
  };

  this.onCloneClassChange = function(event, ui) {
    this.select(ui.item.index);
  };

  this.bindElement = function(elem) {
    if (this.elem !== null && jQuery.contains(document, this.elem[0])) {
      this.elem.html("");
    }
    this.elem = elem;
    if (this.elem !== null && jQuery.contains(document, this.elem[0])) {
      this.renderAll();
      this.registerEvents();
    }
  };

  this.registerEvents = function() {
    this.elem.on("selectmenuselect", "#cloneClassSelector", this.onCloneClassChange);
  };

  this.getFormatedMetric = function(val, metric) {
    if (val === undefined) {
      return '-';
    }
    SM.state[SM.options.component.key].clone.classMetrics.forEach(function(tempMetric) {
      if (metric === tempMetric.title) {
        metric = tempMetric;
      }
    });
    metric = metric || { direction: 0 };
    var greenClass = "sm-widget-threshold-green";
    var redClass = "sm-widget-threshold-red";
    var faCheckCircle = "fa fa-check-circle";
    var faExclamationCircle = "fa fa-exclamation-circle";
    var valueClass = "";
    var iconClass = "";
    var iconStyle = "";
    if (metric.baseline === undefined) {
      valueClass = "";
      iconClass = "";
      iconStyle = "";
    } else if (metric.direction === -1) { // 1:lesser=worse && larger=better;
      valueClass = (val <= metric.baseline) ? greenClass : redClass;
      iconClass = (val <= metric.baseline) ? faCheckCircle : faExclamationCircle;
      iconStyle = (val <= metric.baseline) ? "color:green" : "color:red";
    } else if (metric.direction === 1) { // -1: lesser=better && larger=worse
      valueClass = (val >= metric.baseline) ? greenClass : redClass;
      iconClass = (val >= metric.baseline) ? faCheckCircle : faExclamationCircle;
      iconStyle = (val >= metric.baseline) ? "color:green" : "color:red";
    }

    return [
      '<div class="sm-cloneviewer-metric-container">',
      '  <div class="sm-cloneviewer-metric-icon-container"><i class="'+iconClass+'" style="'+iconStyle+'"></i></div>',
      '  <div class="">'+metric.longName+'</div>',
      '  <div class="sm-cloneviewer-metric-title-container">('+metric.title+'</div>,',
      '  <div class="sm-cloneviewer-metric-value-container ' + valueClass + '">' + (Math.round(val * 100) / 100) + ')</div>',
      '</div>'
    ].join("");
  };

  SM.bindFunctions(this);
  this.init(HTMLelem,options);
};
SM.CloneClassSelector.prototype = new SM.Subscribable();
SM.CloneClassSelector.prototype.constructor = SM.Subscribable;
