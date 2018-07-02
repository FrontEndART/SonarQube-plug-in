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

SM.CloneWidget = function(elem, options) {
  /**
   * (Re)render the plugin onto the div
   */
  this.renderAll = function() {
    this.elem.html("");
    this.elem.addClass("sm-widget");
    // I dont give it an id, beacause it might already have one, and I dont want to owerride that, therefore the class
    this.elem.addClass("id-" + this.id);
    this.elem.addClass("sm-widget-clone");
    this.elem.append("<div class='sm-widget-title'>" + consts.TEXT_CLONECLASS_HEADER + "</div>");

    var inputNumRows = [
      '<select class="sm-widget-control-num-of-rows">',
      '  <option id="5" value="5">5</option>',
      '  <option id="10" value="10">10</option>',
      '  <option id="25" value="25">25</option>',
      '  <option id="50" value="50">50</option>',
      '  <option id="100" value="100">100</option>',
      '</select>'
    ].join("\n");

    var pageNum = Math.ceil(this.filter.filteredData.length / this.rows.rowsPerPage);
    var currentPage = Math.ceil(this.rows.startOffset / this.rows.rowsPerPage) + 1;
    pageNum = (pageNum < currentPage) ? currentPage : pageNum; // maximum
    var controlRow = [
      '<div class="sm-widget-control">',
      '  <span>' + consts.TEXT_NUMBER_OF_ROWS + ': ' + inputNumRows + '</span>',
      '  <div class=\"sm-widget-control-arrows\"></div>',
      '  <div class=\"sm-widget-control-paging-state\">',
      '    page ' + currentPage + '/' + pageNum,
      '  </div>',
      '  <div class="control-filter-div">',
           consts.TEXT_FILTER,
      '    <input class="control-filter-input" type="text" value="' + this.filter.text + '">',
      '  </div>',
      '</div>'
    ].join("\n");

    this.elem.append(controlRow);
    this.elem.append('<table id="sm-widget-table"></table>');

    this.renderTable();

    // make selectmenu
    $(".id-" + this.id + " .sm-widget-control-num-of-rows").selectmenu({ width : 'auto' });
    // set the selected in numRows combobox
    $(".id-" + this.id + " .sm-widget-control-num-of-rows").val(this.rows.rowsPerPage).selectmenu('refresh');

    // render paging arrows
    if (this.rows.startOffset > 0) {
      $(".id-" + this.id + " .sm-widget-control-arrows").append(
        "<button id=\"control-left\"> <i class=\"fa fa-arrow-left\" aria-hidden=\"true\"></i></button>"
      );
    }
    if (this.rows.startOffset + this.rows.rowsPerPage < this.filter.filteredData.length) {
      $(".id-" + this.id + " .sm-widget-control-arrows").append(
        "<button id=\"control-right\"> <i class=\"fa fa-arrow-right\" aria-hidden=\"true\"></i></button>"
      );
    }
  };
  this.renderAll = this.renderAll.bind(this);
  /**
   * (Re)renders only the data (the table) onto the div
   */
  this.renderTable = function() {
    var table = $(".id-" + this.id + " #sm-widget-table");
    table.html("");
    // render title row
    var titleRow = [
      "<tr>",
      '  <th id="noOrdering"></th>', // row numbers
      '  <th id="noOrdering"></th>', // icons
      "  <th id='" + consts.TEXT_CLONECLASS_HEADER_ID + "' >" + consts.TEXT_CLONECLASS_HEADER + "</th>"
    ].join("\n");
    this.classMetrics.forEach(function(metric) {
      titleRow += '<th class="sm-widget-metric-title"' + ' id="' + metric.title + '" title=\'' + metric.helpText + '\'>';
      titleRow += metric.title;
      titleRow += '</th>';
    });
    titleRow += [
      '  <th id="noOrdering"></th>', // icons
      '  <th id="noOrdering" >' + consts.TEXT_INSTANCE_HEADER + '</th>'
    ].join("\n");
    this.instanceMetrics.forEach(function(metric) {
      titleRow += '<th class="sm-widget-metric-title" id="noOrdering" title=\'' + metric.helpText + '\'>';
      titleRow += metric.title;
      titleRow += '</th>';
    });
    titleRow += '<th class="sm-widget-metric-title sm-widget-padding-row" id="noOrdering"></th>';
    titleRow += "</tr>";

    table.append(titleRow);
    // render rows for cloneclasses
    var i;
    for (
      i = this.rows.startOffset;
      (i < this.rows.startOffset + this.rows.rowsPerPage) && (i < this.filter.filteredData.length);
      i++
    ) {
      var cloneClass = this.filter.filteredData[i];
      var rowspan = cloneClass.cloneInstances.length;
      var classEven = (! ((i + 1) % 2) ) ? 'sm-widget-row-even' : 'sm-widget-row-odd';
      var row = [
        '<tr>',
        '  <td class="sm-widget-line-numbering ' + classEven + '" rowspan="' + rowspan + '">' + (i + 1) + '</td>',
        '  <td class="sm-widget-line-icons ' + classEven + '" rowspan="' + rowspan + '">',
        '    <i class="sm-icon-square sm-icon-clone-class"></i>',
        '  </td>',
        '  <td class="sm-widget-row-class ' + classEven + '" rowspan="' + rowspan + '">' + cloneClass.name + '</td>',
      ].join("\n");
      this.classMetrics.forEach(function(metric) {
        row += '<td class="' + classEven + '" rowspan="' + rowspan + '">';
        row += SM.formatMetric(cloneClass.cloneClassMetrics[metric.title], metric);
        row += "</td>";
      });

      // first instance
      // there sould always be at least 2 instances per class
      var cloneInstance = cloneClass.cloneInstances[0];
      row += '<td class="sm-widget-line-icons"><i class="sm-icon-square sm-icon-clone-instance"></i></td>';
      row += '<td class="sm-widget-row-instance">' + this.generateCloneViewerLink(cloneInstance, cloneClass.index, 0) + '</td>';
      this.instanceMetrics.forEach(function(metric) {
        row += '<td>';
        row += SM.formatMetric(cloneClass.cloneInstances[0].cloneInstanceMetrics[metric.title], metric);
        row += '</td>';
      });
      row += '<td class="sm-widget-padding-row"></td>';
      row += "</tr>"
      table.append(row);

      // append all other cloneinstances as well
      for (var j = 1; j < cloneClass.cloneInstances.length; j++) {
        cloneInstance = cloneClass.cloneInstances[j];
        row = '<tr>';
        row += '<td class="sm-widget-line-icons"><i class="sm-icon-square sm-icon-clone-instance"></i></td>';
        row += '<td class="sm-widget-row-instance">' + this.generateCloneViewerLink(cloneInstance, cloneClass.index, j) + '</td>';
        this.instanceMetrics.forEach(function(metric) {
          // there sould always be at least 2 instances per class
          row += '<td>' + SM.formatMetric(cloneInstance.cloneInstanceMetrics[metric.title], metric) + '</td>';
        });
        row += '<td class="sm-widget-padding-row"></td>';
        row += '</tr>';
        table.append(row);
      }
    }
    // render ordering arrow
    if (this.currentOrdering.order === "asc") {
      $(".id-" + this.id + " #" + this.currentOrdering.column).append(
        '<i class="fa fa-arrow-down" aria-hidden="true"></i>'
      );
    }
    if (this.currentOrdering.order === "desc") {
      $(".id-" + this.id + " #" + this.currentOrdering.column).append(
        '<i class="fa fa-arrow-up" aria-hidden="true"></i>'
      );
    }
    // init tooltips
    $(".id-" + this.id +' th').tooltip({
      content: function() {
        return $(this).prop('title');
      }
    });
  };
  this.renderTable = this.renderTable.bind(this);

  /**
     * orders the packages array by specified metric name and order.
   * (on re-rendering it will be displayed in the new order)
     */
  this.orderBy = function(metricTitle, order) {
    this.currentOrdering.column = metricTitle || this.currentOrdering.column;
    this.currentOrdering.order = order || this.currentOrdering.order;

    if (this.currentOrdering.column === consts.TEXT_CLONECLASS_HEADER_ID) {
      this.filter.filteredData = _.sortBy(this.filter.filteredData, function(cloneClass) {
        return parseInt(cloneClass.name);
      });
    } else {
      this.filter.filteredData = _.sortBy(this.filter.filteredData, function(cloneClass) {
        return cloneClass.cloneClassMetrics[self.currentOrdering.column];
      });
    }

    if (this.currentOrdering.order === "desc") {
      this.filter.filteredData = this.filter.filteredData.reverse();
    }
  }
  this.orderBy = this.orderBy.bind(this);

  /**
   * Filters the packagelist. the filtered data list is put into
   * this.filter.filteredData, which is always what rendering is based on.
   * Filtering ignores case, and is done on the "Name" column.
   */
  this.doFilter = function() {
    if (this.filter.text !== "") {
      this.filter.filteredData = this.data;
    }

    var i = 0;
    this.filter.filteredData = [];

    // apply filtering
    for (i = 0; i < this.data.length; i++) {
      var pack = this.data[i];
      var lowercaseName = pack.name.toLowerCase();

      if (lowercaseName.indexOf(this.filter.text) !== -1) {
        this.filter.filteredData.push(pack);
      }
    }

    this.rows.startOffset = 0; // filtering resets the page index to one
    this.orderBy();
  }
  this.doFilter = this.doFilter.bind(this);

  /**
   * guarantees that rows.startoffset is a valid number [].
   * if input < 0,=> input = 0
   * if input > count(packages)-maxrowsPerPage => input = count(packages)-maxrowsPerPage
   */
  this.setRowsStartOffset = function(input) {
    if (input < 0) {
      this.rows.startOffset = 0;
      return;
    }

    if (input > this.filter.filteredData.length - this.rows.rowsPerPage) {
      this.rows.startOffset = this.filter.filteredData.length - this.rows.rowsPerPage;
      return;
    }
    this.rows.startOffset = input;
  }
  this.setRowsStartOffset = this.setRowsStartOffset.bind(this);

  /**
   * Generates a link that points to the CloneViewer interface. When clicked the browser is
   * redirected to show the CloneViewer with the clicked Instance selected on the left pane
   * and another Instance from the same class on the right pane. The other instance is
   * instance-#2 if instance-#1 was clicked, in any other case instance-#1 is selected as
   * the right hand side instance.
   *
   * @param  {CloneInstance} pack  cloneInstance
   * @param  {index}         i     index  of the parent clonClass
   * @param  {index}         j     index  of the current cloneInstace inside the parent cloneClass
   * @return {String}              an HTML <li> element
   */
  this.generateCloneViewerLink = function(pack, i, j) {
    var anchor = pack.name;
    if (pack.positions[0]) {
      var url = 'http://' + window.location.host + '/project/extension/SourceMeterGUI/cloneViewer?id='
      + SM.options.component.key;
      var href = 'javascript:(function() {'
      + 'var stateData = SM.state[SM.options.component.key];'
      + 'stateData.cloneViewer.selectedCloneClass = ' + i + ';'
      + 'stateData.cloneViewer.selectedInstances = [' + j + ', ' + ((j===0) ? 1 : 0) + '];'
      + 'SM.options.router.push(\'' + url + '\');'
      + '})()';
      anchor = '<a href="' + href + '">' + pack.name + '</a>';
    }

    return anchor;
  };
  this.generateCloneViewerLink = this.generateCloneViewerLink.bind(this);

  this.generatePositionAnchorTab = function(pack) {
    var anchor = pack.name;
    if (pack.positions[0]) {
      var href = 'http://' + window.location.host + '/component?id=' + pack.displayedPath
                + '&line=' + pack.positions[0].line;
      anchor = '<a href="' + href + '" target="_blank">' + pack.name + '</a>';
    }
    return anchor;
  };
  this.generatePositionAnchorTab = this.generatePositionAnchorTab.bind(this);

  this.generatePositionAnchorPopup = function(pack) {
    var anchor = pack.name;
    if (pack.positions[0]) {
      var url = 'http://' + window.location.host + '/component?id=' + pack.displayedPath
                + '&line=' + pack.positions[0].line;
      var href = 'javascript:(function() {window.open(\'' + url + '\', \'' + pack.displayedPath
                 + '\', \'resizable,scrollbars,status\');})()';
      anchor = '<a href="' + href + '">' + pack.name + '</a>';
    }
    // format: <a href="javascript:window.open('some.html', 'yourWindowName', 'width=200,height=150');">Test</a>
    return anchor;
  };
  this.generatePositionAnchorPopup = this.generatePositionAnchorPopup.bind(this);

  var self = this;


  var consts = {
    TEXT_NAME:'Name',
    TEXT_NUMBER_OF_ROWS: 'Number of rows',
    TEXT_FILTER: 'Filter: ',
    TEXT_CLONECLASS_HEADER: 'Clone class',
    TEXT_CLONECLASS_HEADER_ID: 'Clone_class',
    TEXT_INSTANCE_HEADER: 'Clone instance',
    TEXT_TITLE: 'Clones'
  }

  /**
   * Options:
   * {
   *   maxRows: initial maximal rows / line
   *   instanceMetricTitles: an array of metric identifiers, have to be identical with metric propertynames in the
   *   datasource (instance metrics)
   *   classMetricTitles: an array of metric identifiers, have to be identical with metric propertynames in the datasource
   *   (clone class metrics)
   *   data: the data source. format described below
   *   title: widget display title
   *   projectId: SonarQube project key
   * }
   */

  this.rows = {
    startOffset: 0,
    rowsPerPage: options.maxRows ? options.maxRows : 5
  }
  this.instanceMetrics = options.instanceMetrics ? options.instanceMetrics : [];
  this.classMetrics = options.classMetrics ? options.classMetrics : [];
  this.data = options.data ? options.data : [];

  /**
   *  eg:
   *  options.data = [{
   *    "Name": "unnamed package",
   *    "Position": [{
   *      "path": "...",
   *      "line": 1
   *    }],
   *    "classMetrics": { ... }
   *    "cloneInstances": [{
   *      "name": "2~CloneInstance",
   *      "positions": [{
   *        "path": "...",
   *            "line": 1
   *      }],
   *      "instanceMetrics": {...}
   *    }, ...
   *    ]
   *  }, ...
   *  ]
   */
  this.options = options;
  this.elem = elem;
  this.id = new Date().getTime() + "";
  this.currentOrdering = {
    column: null,
    order: null,
  };
  this.filter = {
    text: "",
    filteredData: this.data
  }
  this.title = options.title ? options.title : consts.TEXT_TITLE;
  this.langID = options.langID ? options.langID : undefined;
  this.projectId = options.projectId ? options.projectId : "";

  /* Event handlers */
  this.onHeaderClick = function(e) {
    // fix clicking on the arrow error bug:
    var column = "";
    if (e.target.id === "noOrdering") {
      return;
    }

    if (e.target.id == "") {
      column = e.target.parentElement.id
    } else {
      column = e.target.id
    }

    var reverse = (column === this.currentOrdering.column)
    if (reverse) {
      var ordering = (this.currentOrdering.order === "asc") ? "desc" : "asc";
      this.orderBy(this.currentOrdering.column, ordering);
    } else {
      // determine metric direction
      var direction = 1; // default, (if column is not a metric, eg Name)
      this.classMetrics.forEach(function(metric) {
        if (metric.title === column) {
          direction = metric.direction;
        }
      });

      if (direction === 1) {
        this.orderBy(column, "asc");
      } else if (direction === -1) {
        this.orderBy(column, "desc");
      }
    }
    this.renderAll();
  };
  this.onHeaderClick = this.onHeaderClick.bind(this);

  this.onmaxRowChange = function(e) {
    this.rows.rowsPerPage = e.target.value * 1;
    this.renderAll();
  };
  this.onmaxRowChange = this.onmaxRowChange.bind(this);

  this.nextPage = function(e) {
    this.rows.startOffset += this.rows.rowsPerPage;
    this.renderAll();
  };
  this.nextPage = this.nextPage.bind(this);

  this.prevPage = function(e) {
    this.setRowsStartOffset(this.rows.startOffset - this.rows.rowsPerPage);
    this.renderAll();
  };
  this.prevPage = this.prevPage.bind(this);

  this.filterChange = function(e) {
    this.filter.text = e.target.value.toLowerCase();
    this.doFilter();
    this.renderTable();
    this.updatePagingState();
  };
  this.filterChange = this.filterChange.bind(this);

  this.registerEvents = function() {
    $(".id-" + this.id).on("click", "th", this.onHeaderClick);
    $(".id-" + this.id).on("selectmenuchange", ".sm-widget-control-num-of-rows", this.onmaxRowChange);
    $(".id-" + this.id).on("click", "#control-right", this.nextPage);
    $(".id-" + this.id).on("click", "#control-left", this.prevPage);
    $(".id-" + this.id).on("keyup", ".control-filter-input", this.filterChange);
  }
  this.registerEvents = this.registerEvents.bind(this);

  this.bindElement = function(elem) {
    if (this.elem !== null) {
      this.elem.html("");
      this.elem.removeClass("sm-widget");
      this.elem.removeClass("id-" + this.id);
      this.elem.removeClass("sm-widget-clone");
    }
    this.elem = elem;
    if (this.elem !== null && jQuery.contains(document, this.elem[0])) {
      this.renderAll();
      this.registerEvents();
    }
  }
  this.bindElement = this.bindElement.bind(this);

  this.init = function() {
    if(typeof SM.state[SM.options.component.key].cloneViewer === "undefined") {
      SM.state[SM.options.component.key].cloneViewer = {};
    }

    // set code path properly (important when there are submodules)
    self.data.forEach(function(obj, index) {
      obj.index = index;
      obj.cloneInstances.forEach(function(inst) {
        if (inst.positions.length > 0) {
          inst.displayedPath = self.projectId + ':' + inst.positions[0].path;
        }
      });
    });

    this.orderBy(consts.TEXT_CLONECLASS_HEADER_ID, "asc")
    if (this.elem !== null && jQuery.contains(document, this.elem[0])) {
      this.renderAll();
      this.registerEvents();
    }
    SM.MetricLoader.subscribe("finishedAllRequests", function() {
      if (self.elem !== null && jQuery.contains(document, self.elem[0])) {
        self.renderTable();
      }
    });
    // loading metric thresholds one by one
    this.instanceMetrics.forEach(SM.MetricLoader.requestMetric);
    this.classMetrics.forEach(SM.MetricLoader.requestMetric);
  };

  this.merge = function(other) {
    // merge the data
    other.data.forEach(function(obj) {
      obj.index = self.data.length;
      self.data.push(obj);
    });

    // remove the init method
    this.init = undefined;
    this.doFilter();
    this.orderBy(this.currentOrdering.column, this.currentOrdering.order);
    this.renderAll();
  };
  this.merge = this.merge.bind(this);

  this.updatePagingState = function() {
    // update pager info
    var pageNum = Math.ceil(this.filter.filteredData.length / this.rows.rowsPerPage);
    var currentPage = Math.ceil(this.rows.startOffset / this.rows.rowsPerPage) + 1;
    pageNum = (pageNum < currentPage) ? currentPage : pageNum; // maximum

    if (pageNum === 1) {
      $(".id-" + this.id + " .sm-widget-control-paging-state").html("");
    } else {
      $('.id-' + this.id + ' div.sm-widget-control-paging-state').html('page ' + currentPage + '/' + pageNum);
    }

    // update paging arrows
    $('.id-' + this.id + ' div.sm-widget-control-arrows').html('');
    if (this.rows.startOffset > 0) {
      $(".id-" + this.id + " .sm-widget-control-arrows").append(
        "<button id=\"control-left\"> <i class=\"fa fa-arrow-left\" aria-hidden=\"true\"></i></button>"
      );
    }
    if (this.rows.startOffset + this.rows.rowsPerPage < this.filter.filteredData.length) {
      $(".id-" + this.id + " .sm-widget-control-arrows").append(
        "<button id=\"control-right\"> <i class=\"fa fa-arrow-right\" aria-hidden=\"true\"></i></button>"
      );
    }
  };
  this.updatePagingState = this.updatePagingState.bind(this);

  this.init();
};
