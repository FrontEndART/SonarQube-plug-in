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

SM.CloneInstanceSelector = function(HTMLelem, options) {
  SM.Subscribable.call(this); // inherit from SM.Subscribable

  this.elem = null; // :DOMElement
  this.id; // : int (sorszám)
  this.cloneInstanceList; // :CloneClass[]
  this.selected; // :int
  this.codeBrowser; // :CodeBrowser (manages the diffs)
  this.parent; // :CloneClassSelector

  var self = this;

  this.init = function(HTMLelem,options) {

    this.id = (typeof options.id !== "undefined") ? options.id : 0;
    this.codeBrowser = (typeof options.codeBrowser !== "undefined") ? options.codeBrowser : null;
    this.parent = (typeof options.parent !== "undefined") ? options.parent : null;
    this.cloneInstanceList = (typeof options.cloneInstanceList !== "undefined") ? options.cloneInstanceList : [];
    this.selected = (typeof options.selected !== "undefined") ? options.selected : 0;

    this.elem = HTMLelem;
    this.bindElement(HTMLelem);
    this.select(this.selected);

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
    this.elem.addClass("sm-cloneInstanceSelector");
    // put together the selectors html string
    var html = [];

    html.push('<div id="cloneInstanceLinkContainer">');
      html.push(SM.cloneViewer.generatePositionAnchorPopup(this.cloneInstanceList[this.selected]));
    html.push('</div>');
    html.push('<div>');
      html.push('<select id="selectmenu">');
      this.cloneInstanceList.forEach(function(cloneInstance, i) {
          html.push("<option value=" + i + "> " + cloneInstance.name + "</option>");
      });
      html.push('</select>');
    html.push('</div>');
    html.push('<div id="cloneInstanceMetricsContainer'+this.id+'"></div>');

    this.elem.append(html.join(""));

    this.renderMetrics();

    // make selectmenu
    $("#cloneInstanceSelector" + this.id + " #selectmenu").selectmenu();
    $("#cloneInstanceSelector" + this.id + " #selectmenu").val(this.selected).selectmenu("refresh");
  };

  this.renderMetrics = function() {
    var div = $('#cloneInstanceMetricsContainer'+this.id);
    var html = [];
    var data = this.cloneInstanceList[this.selected].cloneInstanceMetrics;
    var metrics = SM.state[SM.options.component.key].clone.instanceMetrics;
    metrics.forEach(function(metric) {
      html.push(SM.cloneViewer.getFormatedMetric(data[metric.title], metric));
    });
    div.html(html.join(""));
    $("#cloneInstanceSelector" + this.id + " .sm-cloneviewer-metric-title-container").tooltip({
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
    this.emit("onSelect", self.id);
  };

  this.onCloneInstanceChange = function(event, ui) {
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
    this.elem.on("selectmenuselect", this.onCloneInstanceChange);
  };

  SM.bindFunctions(this);
  this.init(HTMLelem,options);
};
SM.CloneInstanceSelector.prototype = new SM.Subscribable();
SM.CloneInstanceSelector.prototype.constructor = SM.Subscribable;
