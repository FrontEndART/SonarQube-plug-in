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

SM.LogicWidget = function(HTMLelem, options) {

  /**
   * (Re)render the plugin onto the div
   */
  this.renderAll = function() {
    if (this.elem === null && !jQuery.contains(document, this.elem[0])) {
      return;
    }

    this.elem.html("");
    this.elem.addClass("sm-widget");
    // I dont give it an id, beacause it might already have one, and I dont want to owerride that, therefore the class
    this.elem.addClass("id-" + this.id);
    this.elem.append("<div class='sm-widget-title'>" + this.title.join(' / ') + "</div>");
    var table = $('<div class="sm-widget-table-div"><table id="sm-widget-table"></table></div>');

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
      '  <div class="sm-widget-control-num-of-rows-wrapper">' + consts.TEXT_NUMBER_OF_ROWS + ': ' + inputNumRows + '</div>',
      '  <div class=\"sm-widget-control-arrows\"></div>',
      '  <div class=\"sm-widget-control-paging-state\">',
      '    page ' + currentPage + '/' + pageNum,
      '  </div>',
      '  <div class="control-filter-div">',
           consts.TEXT_FILTER,
      '    <input class="control-filter-input" type="text" value="' + this.filter.text + '">',
      '  </div>',
      '  <div class="control-filter-div">',
           consts.TEXT_COLUMN_FILTER,
      '    <input class="control-filter-column-input" type="text" value="' + this.filter.columnText + '">',
      '  </div>',
      '</div>'
    ].join("\n");

    this.elem.append(controlRow);
    this.elem.append(table);

    this.renderTable();

    // make selectmenu
    $(".id-" + this.id + " .sm-widget-control-num-of-rows").selectmenu({ width : 'auto'});
    // set the selected in numRows combobox
    $(".id-" + this.id + " .sm-widget-control-num-of-rows").val(this.rows.rowsPerPage).selectmenu('refresh');

    // render paging arrows
    if (this.rows.startOffset > 0) {
      $(".id-" + this.id + " .sm-widget-control-arrows").append(
        "<button id=\"control-left\"><i class=\"fa fa-arrow-left\" aria-hidden=\"true\"></i></button>"
      );
    }
    if (this.rows.startOffset + this.rows.rowsPerPage < this.filter.filteredData.length) {
      $(".id-" + this.id + " .sm-widget-control-arrows").append(
        "<button id=\"control-right\"><i class=\"fa fa-arrow-right\" aria-hidden=\"true\"></i></button>"
      );
    }
    if (pageNum === 1) {
      $(".id-" + this.id + " .sm-widget-control-paging-state").html("");
    }
  };
  this.renderAll = this.renderAll.bind(this);

  /**
   * (Re)renders only the data (the table) onto the div
   */
  this.renderTable = function() {
    if (this.elem === null && !jQuery.contains(document, this.elem[0])) {
      return;
    }

    var table = $(".id-" + this.id + " #sm-widget-table");
    table.html("");
    // render title row
    var titleRow = [
      "<tr>",
      '  <th id="noOrdering"></th>', // line numbers
      '  <th id="noOrdering"></th>', // icons
      '  <th id="' + consts.TEXT_NAME + '" >' + consts.TEXT_NAME + '</th>'
    ].join("\n");
    this.filter.filteredColumns.forEach(function(metric) {
      titleRow += '<th ' +
                   ' class="sm-widget-metric-title"' +
                   ' id="' + metric.title + '"' +
                   ' title=\'' + metric.helpText + '\'>' + metric.title +
                  '</th>';
    });
    titleRow += '<th class="sm-widget-metric-title sm-widget-padding-row" id="noOrdering"></th>';
    titleRow += "</tr>";

    table.append(titleRow);

    // render rows for packages
    var i;
    for (
      i = this.rows.startOffset;
      (i < this.rows.startOffset + this.rows.rowsPerPage) && (i < this.filter.filteredData.length);
      i++
    ) {
      var pack = this.filter.filteredData[i];
      var row = [
        '<tr>',
        '  <td class="sm-widget-line-numbering">' + (i + 1) + '</td>',
        '  <td class="sm-widget-line-icons">',
        '    <i class="sm-icon-rect sm-icon-' + pack.langID.toLowerCase() + '-' + pack.level + '"></i>',
        '  </td>',
        '  <td class="sm-widget-row-name">',
        this.generatePositionAnchorPopup(pack),
        '  </td>'
      ].join("\n");
      this.filter.filteredColumns.forEach(function(metric) {
        row += "<td>" + SM.formatMetric(pack.metrics[metric.title], self.metrics[metric.langID][metric.title]) + "</td>";
      });

      row += '<td class="sm-widget-padding-row"></td>';
      row += '</tr>';
      table.append(row);
    }

    // render ordering arrow
    if (this.currentOrdering.order === "asc") {
      $(".id-" + this.id + " #" + this.currentOrdering.column).append(
        ' <i class="fa fa-arrow-down" aria-hidden="true"></i>'
      );
    }

    if (this.currentOrdering.order === "desc") {
      $(".id-" + this.id + " #" + this.currentOrdering.column).append(
        ' <i class="fa fa-arrow-up" aria-hidden="true"></i>'
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

    if (this.currentOrdering.column === consts.TEXT_NAME) {
      this.filter.filteredData = _.sortBy(this.filter.filteredData, function(pack) {
        return pack.name;
      });
    } else {
      this.filter.filteredData = _.sortBy(this.filter.filteredData, function(pack) {
        return pack.metrics[self.currentOrdering.column];
      });
    }

    if (this.currentOrdering.order === "desc") {
      this.filter.filteredData = this.filter.filteredData.reverse();
    }
  };
  this.orderBy = this.orderBy.bind(this);

  /**
   * Filters the packagelist. the filtered data list is put into
   * this.filter.filteredData, which is always what rendering is based on.
   * Filtering ignores case, and is done on the "Name" column.
   */
  this.doFilter = function() {
    this.filter.text = SM.htmlEncode(this.filter.text.toLowerCase());

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
  };
  this.doFilter = this.doFilter.bind(this);

  /**
   * Filters the column(=metrics list). the filtered data list is put into
   * this.filter.filteredColumns, which is always what rendering is based on.
   * Filtering ignores case.
   */
  this.doMetricFilter = function() {
    this.filter.columnText = this.filter.columnText.toLowerCase();

    var i = 0;
    this.filter.filteredColumns = [];

    // apply filtering
    for (i = 0; i < this.commonMetrics.length; i++) {
      var metric = this.commonMetrics[i];
      var lowercaseName = metric.title.toLowerCase();

      if (lowercaseName.indexOf(this.filter.columnText) !== -1) {
        this.filter.filteredColumns.push(metric);
      }
    }
  };
  this.doFilter = this.doFilter.bind(this);

  /**
   * guarantees that rowsstartoffset is a valid number.
   * if input < 0,=> input = 0
   * if input > count(packages)-maxrowsPerPage => input = count(packages)-maxrowsPerPage
   */
  this.setRowsStartOffset = function(input) {
    if (input < 0) {
      this.rows.startOffset = 0;
      return;
    }
    if (input > this.filter.filteredData.length-this.rows.rowsPerPage) {
      this.rows.startOffset = this.filter.filteredData.length-this.rows.rowsPerPage;
      return;
    }
    this.rows.startOffset = input;
  };
  this.setRowsStartOffset = this.setRowsStartOffset.bind(this);

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
    TEXT_NAME:"Name",
    TEXT_NUMBER_OF_ROWS: 'Number of rows',
    TEXT_FILTER: 'Filter: ',
    TEXT_COLUMN_FILTER: 'Column filter: '
  };

  /**
   * Options:
   * {
   *   maxRows: initial maximal rows / line
   *   metricTitles: an array of metric identifiers, have to be identical with metric propertynames in the datasource
   *   data: the data source. format described below
   *   title: widget display title
   *   projectId: SonarQube project key
   * }
   */
  this.title = options.title ? [options.title] : [];
  this.langID = options.langID ? [options.langID] : [];
  this.projectId = options.projectId ? options.projectId : "";
  this.rows = {
    startOffset: 0,
    rowsPerPage: options.maxRows ? options.maxRows : 5
  };
  this.metrics = {}; // set of metrics per language. format: {langID: Metric[],...}
  this.commonMetrics = []; // the merged set of metrics if multiple languages are merged
  if (options.metrics) {
    this.metrics[this.langID[0]] = {};
    this.commonMetrics = options.metrics;

    options.metrics.forEach(function(metric) {
      self.metrics[self.langID[0]][metric.title] = metric;
    });
  }
  /* this.metrics : [ {title, direction, baseline}]. direction =
   * 1: larger value is better, -1 smaller is better, 0: unknown/undefined */

  this.data = options.data ? options.data : [];
  /**
   * eg:
   * options.data = [{
   *  "Name": "unnamed package",
   *  "Position": [{
   *    "path": "C:\\Projects\\sonarQube\\sonar-scanning-examples-master\\log4j\\log4j-1.2.8\\src\\java\\org\\apache\\log4j\\jmx\\T.java",
   *    "line": 1
   *  }],
   *  "Metrics": {
   *    "LOC": 1094,
   *    "LLOC": 15
   *  },
   *  langID:"eg: java"
   * }, ...
   * ]
   */
  this.options = options;
  this.elem = HTMLelem;
  this.id = new Date().getTime() + "";
  this.currentOrdering = {
    column: null,
    order: null,
  };
  this.filter = {
    text: "",
    filteredData: this.data,
    columnText: "",
    filteredColumns: this.commonMetrics
  };

  /* Event handlers */
  this.onHeaderClick = function(e) {
    var column = "";
    if (e.target.id === "noOrdering") {
      return;
    }

    if (e.target.id == "") { // the arrow itself was clicked
      column = e.target.parentElement.id;
    } else { // the <th> element was clicked
      column = e.target.id;
    }

    var reverse = (column === this.currentOrdering.column);
    if (reverse) {
      // flip the current ordering, same column
      var ordering = (this.currentOrdering.order === "asc") ? "desc" : "asc";
      this.orderBy(this.currentOrdering.column, ordering);
    } else {
      // determine metric direction
      var direction = 1; // default, (if column is not a metric, eg Name)
      this.commonMetrics.forEach(function(metric) {
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
    this.renderTable();
    this.updatePagingState();
  };
  this.nextPage = this.nextPage.bind(this);

  this.prevPage = function(e) {
    this.setRowsStartOffset(this.rows.startOffset - this.rows.rowsPerPage);
    this.renderTable();
    this.updatePagingState();
  };
  this.prevPage = this.prevPage.bind(this);

  this.filterChange = function(e) {
    this.filter.text = e.target.value;
    this.doFilter();
    this.renderTable();
    this.updatePagingState();
  };
  this.filterChange = this.filterChange.bind(this);

  this.columnFilterChange = function(e) {
    this.filter.columnText = e.target.value;
    this.doMetricFilter();
    this.renderTable();
  };
  this.columnFilterChange = this.columnFilterChange.bind(this);

  this.registerEvents = function() {
    $(".id-" + this.id).on("click", "th", this.onHeaderClick);
    $(".id-" + this.id).on("selectmenuchange", ".sm-widget-control-num-of-rows", this.onmaxRowChange);
    $(".id-" + this.id).on("click", "#control-right", this.nextPage);
    $(".id-" + this.id).on("click", "#control-left", this.prevPage);
    $(".id-" + this.id).on("keyup", ".control-filter-input", this.filterChange);
    $(".id-" + this.id).on("keyup", ".control-filter-column-input", this.columnFilterChange);
  };
  this.registerEvents = this.registerEvents.bind(this);

  this.bindElement = function(elem) {
    if (!jQuery.contains(document, this.elem[0])) {
      this.elem.html("");
      this.elem.removeClass("sm-widget");
      this.elem.removeClass("id-" + this.id);
    }
    this.elem = elem;
    if (jQuery.contains(document, this.elem[0])) {
      this.renderAll();
      this.registerEvents();
    }
  };
  this.bindElement = this.bindElement.bind(this);

  this.init = function() {
    // set code path properly
    this.data.forEach(function(obj) {
      if (obj.positions.length > 0) {
        obj.displayedPath = self.projectId + ':' + obj.positions[0].path;
      }
    });

    // call renderTable when all metric data has been loaded to refresh the view
    SM.MetricLoader.subscribe("finishedAllRequests", function() {
      if (self.elem !== null && jQuery.contains(document, self.elem[0])) {
        self.renderTable();
      }
    });
    // start loading metric thresholds and helptext
    this.commonMetrics.forEach(SM.MetricLoader.requestMetric);

    this.orderBy(consts.TEXT_NAME, "asc");
    if (this.elem !== null && jQuery.contains(document, this.elem[0])) {
      this.renderAll();
      this.registerEvents();
    }
  };
  this.init = this.init.bind(this);

  /**
   *  merges two instances of this class into one. Used for multilanguage widgets.
   *  Entries, title and language are merged. you need to first instantiate both widgets
   *  as a single language widget, and bevare: initialization requires for there to be only
   *  one language.
   */
  this.merge = function(other) {
    // union this.commonMetrics
    other.commonMetrics.forEach(function(metric) {
      var foundit = false;
      for (var i = 0; i < self.commonMetrics.length; i++) {
        var m = self.commonMetrics[i];
        if (m.title === metric.title) {
          foundit = true;
          break;
        }
      }
      if (!foundit) {
        self.commonMetrics.push(metric);
      }
    });
    // append this.metrics with {other.langID: Metrics[]} foreach langID of other
    for (var lang in other.metrics) {
      if (!other.metrics.hasOwnProperty(lang)) {
        continue;
      }

      if (self.metrics.hasOwnProperty(lang)) { // same language already exists
        for (var metric in other.metrics[lang]) {
          if (other.metrics.hasOwnProperty(lang)
              && !self.metrics[lang][metric.title]) {
            self.metrics[lang][metric.title] = metric;
          }
        }
      } else { // new language
        self.metrics[lang] = other.metrics[lang];
      }
    }
    // merge the data
    other.data.forEach(function(obj) {
      self.data.push(obj);
    });

    // merge title
    other.title.forEach(function(otherTitle) {
      var foundit = false;
      for (var i = 0; i < self.title.length; i++) {
        var t = self.title[i];
        if (t === otherTitle) {
          foundit = true;
          break;
        }
      }
      if (!foundit) {
        self.title.push(otherTitle);
      }
    });

    // remove the init method
    this.init = undefined;

    this.doFilter(); this.doMetricFilter();
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
