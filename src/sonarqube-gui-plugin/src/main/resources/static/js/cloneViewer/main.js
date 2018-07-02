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

SM.cloneViewer = {};

SM.cloneViewer.getFormatedMetric = function(val, metric) {
  if (val === undefined) {
    return '-';
  }

  var greenClass = "sm-widget-threshold-green";
  var redClass = "sm-widget-threshold-red";
  var faCheckCircle = "fa fa-check-circle";
  var faExclamationCircle = "fa fa-exclamation-circle";
  var valueClass = "", iconClass = "", iconStyle = "";
  if (metric.baseline === undefined) {
    iconClass = "fa fa-minus-circle";
    iconStyle = "color:#444444";
  } else if (metric.direction === -1) { // 1:lesser=worse && larger=better;
    valueClass = (val <= metric.baseline) ? greenClass : redClass;
    iconClass = (val <= metric.baseline) ? faCheckCircle : faExclamationCircle;
    // iconStyle = (val <= metric.baseline) ? "color:#00AA00" : "color:#D4333F";
    iconStyle = (val <= metric.baseline) ? "color:green" : "color:#D4333F";
  } else if (metric.direction === 1) { // -1: lesser=better && larger=worse
    valueClass = (val >= metric.baseline) ? greenClass : redClass;
    iconClass = (val >= metric.baseline) ? faCheckCircle : faExclamationCircle;
    iconStyle = (val >= metric.baseline) ? "color:#green" : "color:#D4333F";
  }

  return [
    '<div class="sm-cloneviewer-metric-container">',
      '<i class="sm-cloneviewer-metric-icon ' + iconClass + '" style="' + iconStyle + '"></i>',
      '<div class="sm-cloneviewer-metric-title-container" title=\'' + metric.helpText + '\'">',
        metric.longName + ' (' + metric.title + ', ' + ((metric.baseline !== undefined)? metric.baseline : "-") + '):',
      '</div>',
      '<div class="sm-cloneviewer-metric-value-container ' + valueClass + '">',
        '<b>' + (Math.round(val * 100) / 100) + '</b>',
      '</div>',
    '</div>'
  ].join("");
};

SM.cloneViewer.generatePositionAnchorPopup = function(instance) {
  var anchor = instance.name;
  if (instance.positions[0]) {
    var url = 'http://' + window.location.host + '/component?id=' + instance.displayedPath
              + '&line=' + instance.positions[0].line;
    var href = 'javascript:(function() {window.open(\'' + url + '\', \'' + instance.displayedPath
               + instance.name+'\', \'resizable,scrollbars,status\');})()';
    anchor = '<a href="' + href + '">' + instance.displayedPath.split(":")[1] + '</a>';
  }
  // format: <a href="javascript:window.open('some.html', 'yourWindowName', 'width=200,height=150');">Test</a>
  return anchor;
};
/**
 * Entry point for the cloneViewer. this function is guaranteed to be executed
 * after loading of all dependency scripts and styles. It will be executed every
 * time when the user opens the codeViewer page.
 * @return {void}
 */
SM.cloneViewer.init = function() {
  SM.pageBuilder.cloneViewer.build();
  SM.state[SM.options.component.key].cloneViewer.data = new SM.CloneViewer();
};

SM.cloneViewer.main = function() {

  if (!SM.state[SM.options.component.key].hasOwnProperty("cloneViewer")) {
    SM.state[SM.options.component.key].cloneViewer = {}; // create it the first time
  }

  var stateData = SM.state[SM.options.component.key].cloneViewer;

  // dashboard data was never fetched before, so neither was the cloneViewer or the dashboard visited before
  if (!SM.state[SM.options.component.key].initialized) {
    SM.dashboard.fetch();
    SM.waitForNDo(
      function() {
        if (SM.state[SM.options.component.key].clone) {
          return true;
        } else {
          return false ;
        }
      },
      function() {
        stateData.numOfCloneClasses = SM.state[SM.options.component.key].clone.data.length;
        stateData.selectedCloneClass = (stateData.numOfCloneClasses >= 1)? 0 : undefined;
        stateData.selectedInstances = (stateData.numOfCloneClasses >= 1)? [0,1] : undefined;
        SM.cloneViewer.init();
      },
      100
    );
    return;
  } else {// dashboard or cloneViewer WAS visited and loaded before
    SM.cloneViewer.init();
  }

}; // END OF function main
