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

/**
 * The object handling visual representation of the code
 *
 * @param {[type]} HTMLelem [description]
 * @param {[type]} options  [description]
 */
SM.SideBySideDiffer = function(HTMLelem, options) {
  var self = this;
  this.elem = null; // :DOMElement
  /**
   * Array of objects describing text on the left and right instance.
   * Object structure:
   *   startLine {Number}    the starting line number of the instance
   *                         (not the context!), numbering starts with 1
   *   ctxBefore {String}    how many lines of context should be prepended
   *   ctxAfter  {String}    how many lines of context should be appended
   *   txt       {String[]}  array of lines. text of the instance with context before and after
   *
   * @type {Array}
   */
  this.text = []; // :obj[]
  this.instance = []; // :Instance[]

  this.init = function(HTMLelem, options) {
    options = (options) ? options:  {};
    defaultval = {
      startLine: 1,
      ctxBefore: 0,
      ctxAfter: 0,
      txt: []
    };
    this.text[0] = options.text0 ? options.text0 : defaultval;
    this.text[1] = options.text1 ? options.text1 : defaultval;
    if (options.instance0) {
      this.setInstance(options.instance0);
    }
    if (options.instance1) {
      this.setInstance(options.instance1);
    }

    this.bindElement(HTMLelem);
  };

  /**
   * Generates HTML of dependant of the current state of the object.
   * Then overwrites the HTML in the visible webpage as well.
   *
   * @return {undefined}
   */
  this.renderAll = function() {
    var txtA = this.getTextNoCtx(0).join("\n");
    var txtB = this.getTextNoCtx(1).join("\n");
    var isMatch = (txtA === txtB);
    var diff;
    if (!isMatch) {
      diff = JsDiff.createPatch('fileName', txtA, txtB, 'oldHeader', 'newHeader', {context:100000});
    } else {
      // manually put together a diff with no changes, only context
      diff = [
        "Index: fileName",
        "===================================================================",
        "--- fileName  oldHeader",
        "+++ fileName  newHeader",
        "@@ -1 +1 @@",
      ].join("\n");
      diff = [diff, txtA.split("\n").map(function(s) {return " " + s;}).join("\n")].join("\n");
    }

    // Prepend, append lines here. (gets added to both sides, postprocessing fixes this later)
    // Add empty space before every line, to conform with diff format!
    // Prepend both contexts, left goes first.
    var preLeft = this.getTextCopy(0);
    preLeft.splice(this.text[0].ctxBefore, preLeft.length);
    var preRight = this.getTextCopy(1);
    preRight.splice(this.text[1].ctxBefore, preRight.length);
    var pre = preLeft.concat(preRight)
                .map(function(s) { return " " + s; })
                .join("\n");
    // append both contexts, left first
    var postLeft = this.getTextCopy(0);
    postLeft.splice(0, postLeft.length - this.text[0].ctxAfter);
    var postRight = this.getTextCopy(1);
    postRight.splice(0, postRight.length - this.text[1].ctxAfter);
    var post = postLeft.concat(postRight)
                .map(function(s) { return " " + s; })
                .join("\n");
    diffSplit = diff.split("\n");
    diff = [diffSplit.slice(0, 5).join("\n"), pre, diffSplit.slice(5, diffSplit.length).join("\n"), post].join("\n");

    var diff2htmlUi = new Diff2HtmlUI({diff: diff});
    diff2htmlUi.draw(this.elem, {
      inputFormat: 'diff',
      showFiles: false,
      matching: 'lines',
      outputFormat: 'side-by-side',
      synchronisedScroll: true
    });

    // Fix bad language recognition in highlightjs:
    var lang;
    switch (SM.state[SM.options.component.key].clone.instanceMetrics[0].langID) {
      case 'JAVA': lang = "java"; break;
      case 'CPP': lang = "cpp"; break;
      case 'CSHARP': lang = "cs"; break;
      case 'PYTHON': lang = "python"; break;
      case 'RPG': lang = ""; break;
    }
    $('.d2h-file-wrapper').data('lang', lang);
    diff2htmlUi.highlightCode(this.elem);

    // --- post processing ---
    // Handle matching instances
    if (isMatch) {
      $(".d2h-file-header").html("The instances are identical!");
    } else {
      $(".d2h-file-header").html("Diff:");
    }

    $(".d2h-diff-tbody tr:first-child").remove(); // remove the annotation line (eg. "@@ -1 +1 @@")

    var rowsLeft = $(".d2h-file-side-diff:nth-child(1) tr");
    var rowsRight = $(".d2h-file-side-diff:nth-child(2) tr");

    // remove context lines, that dont belong to the current instance (left/right)
    this.deleteLines(rowsLeft,  this.text[0].ctxBefore, this.text[1].ctxBefore)
    this.deleteLines(rowsRight, 0, this.text[0].ctxBefore)
    this.deleteLines(rowsLeft,  rowsLeft.length - this.text[1].ctxAfter, this.text[1].ctxAfter)
    this.deleteLines(rowsRight, rowsRight.length - (this.text[0].ctxAfter + this.text[1].ctxAfter), this.text[0].ctxAfter)

    // highlight lines of the belonging to the cloneinstances
    rowsLeft = $(".d2h-file-side-diff:nth-child(1) tr");
    rowsRight = $(".d2h-file-side-diff:nth-child(2) tr");
    this.highlightLines(rowsLeft,  this.text[0].ctxBefore, this.getTextNoCtx(0).length);
    this.highlightLines(rowsRight, this.text[1].ctxBefore, this.getTextNoCtx(1).length);

    // overwrite line numbers to match line numbers of the original file
    for (var i = 0; i <= 1; i++) {
      var index = this.text[i].startLine - this.text[i].ctxBefore;
      var number = new RegExp("[0-9]+");
      $($(".d2h-diff-tbody")[i])
        .find(".d2h-code-side-linenumber")
        .each(function(node) {
          if ($(this).text().match(number)) {
            $(this).html(index++);
          }
        });
    }
    this.renderCtxBeforeBtns();
    this.renderCtxAfterBtns();
  };

  /**
   * Removes html nodes from the dom, specified by an array of nodes, a starting index and number items to be removed.
   * Used to delete lines from the left/right panels
   *
   * @param  {JQuery}  rows   jquery result object array
   * @param  {index}   start  starting index, deletion starts here
   * @param  {integer} stop   number of items to be deleted
   */
  this.deleteLines = function(rows, delStart, delStop) {
    for (var i = delStart; i < delStart + delStop; i++) {
      $(rows[i]).remove();
    }
  };

  /**
   * Adds the highlighting css class to the objects in the provided array
   *
   * @param  {JQuery}  rows            jquery result object array
   * @param  {index}   highlightStart  starting index, highlighting starts at thin index
   * @param  {integer} highlightStop   number of items to be highlighted
   */
  this.highlightLines = function(rows, highlightStart, highlightStop) {
    var num = 0;
    var i = highlightStart;
    while (num < highlightStop && typeof rows[i] !== "undefined"){
      var number = new RegExp("[0-9]+");
      if ($(rows[i]).find(".d2h-code-side-linenumber").text().match(number)) {
        $(rows[i]).addClass('sm-cloneviewer-cloneinstance-highlight');
        num++;
      }
      i++;
    }
  };

  /**
   * Renders the buttons that adds context BEFORE the instances
   *
   * @return {undefined}
   */
  this.renderCtxBeforeBtns = function() {
    // Creating a button that will always be prepended to the table
    var button1 = [
      '<button id = "leftBeforeButton">',
        '<i class = "octicon octicon-unfold"></i>',
      '</button>'
    ].join("");

    $(".d2h-file-header").after($(button1));
  };

  /**
   * Renders the buttons that adds context AFTER the instances
   *
   * @return {undefined}
   */
  this.renderCtxAfterBtns = function() {
    // Creating the first button that will always be appended to the table
    var button1 = [
      '<button id = "leftAfterButton">',
        '<i class = "octicon octicon-unfold"></i>',
      '</button>'
    ].join("");

    $(".d2h-files-diff").after($(button1));
  };

  /**
   * Adds SM.cloneViewer.ctxUnit number of lines to the context BEFORE
   * updates the text[id] object accordingly.
   *
   * @param {index} id 0 for left, 1 for right side
   */
  this.addContextBefore = function(id) {
    if (id >= this.text.length) {
      throw "IndexOutOfBoundsException";
      return;
    }
    // Increasing the number of lines to be shown by SM.cloneViewer.ctxUnit and adjusts the variables accordingly
    var contextAfter = self.text[id].ctxAfter;
    var contextBefore = self.text[id].ctxBefore + SM.cloneViewer.ctxUnit;
    var start = this.text[id].startLine - contextBefore;
    // When reaching the start of the file, contextBefore doesnt point to a negative starting line
    if (start < 1) {
      contextBefore += start - 1;
      start = 1; // Default starting value
    }
    var stop = start + contextBefore + this.instance[id].cloneInstanceMetrics.CLLOC + contextAfter;
    var func = function(text) {
      self.text[id].txt = text;
      self.text[id].ctxBefore = contextBefore;
      self.renderAll();
    };
    SM.RawFileLoader.requestSliceOfRawFile(
      func,
      this.instance[id].displayedPath,
      start,
      stop
    );

  };

  /**
   * Adds SM.cloneViewer.ctxUnit number of lines to the context AFTER
   * updates the text[id] object accordingly.
   *
   * @param {index} id 0 for left, 1 for right side
   */
  this.addContextAfter = function(id) {
    if (id >= this.text.length) {
      throw "IndexOutOfBoundsException";
      return;
    }
    // Increasing the number of lines to be shown by SM.cloneViewer.ctxUnit and adjusts the variables accordingly
    var contextAfter = self.text[id].ctxAfter + SM.cloneViewer.ctxUnit;
    var contextBefore = self.text[id].ctxBefore;
    var start = this.text[id].startLine - contextBefore;
    var stop = start + contextBefore + this.instance[id].cloneInstanceMetrics.CLLOC + contextAfter;
    var func = function(text) {
      // When reaching the end of the file, contextAfter shouldn't point beyond the length of the file
      contextAfter -= (stop - (start + text.length));
      self.text[id].txt = text;
      self.text[id].ctxAfter = contextAfter;
      self.renderAll();
    };
    SM.RawFileLoader.requestSliceOfRawFile(
      func,
      this.instance[id].displayedPath,
      start,
      stop
    );
  };

  /**
   * Sets the instance displayed in the left or right pane.
   *
   * @param {index} id  id==0 for the left, id==1 for the right pane
   * @param {CloneInstance} selectedInstance the cloneinstance object that is displayed in the pane
   */
  this.setInstance = function (id, selectedInstance, clearOther = true) {
    if (id >= this.text.length) {
      throw "IndexOutOfBoundsException";
      return;
    }
    this.instance[id] = selectedInstance;

    var start = selectedInstance.positions[0].line;
    var stop = start + selectedInstance.cloneInstanceMetrics.CLLOC;
    var func = function(text) {
      self.text[id] = {
        startLine: start,
        txt: text,
        ctxBefore: 0,
        ctxAfter: 0
      };
      self.renderAll();
    };
    SM.RawFileLoader.requestSliceOfRawFile(
      func,
      selectedInstance.displayedPath,
      start,
      stop
    );

    // clear context of the other instance
    if (clearOther && typeof this.instance[1-id] != "undefined") {
      this.setInstance(1-id, this.instance[1-id], false);
    }
  };

  /**
   * Gets a copy of the text array (whole text, with context)
   *
   * @param {index} id   id==0 for the left, id==1 for the right pane
   */
  this.getTextCopy = function(id) {
    var txtarr = this.text[id].txt;
    var txt = txtarr.slice(0, txtarr.length);
    return txt;
  };

  /**
   * Calculates the array of lines that belong to the cloneclass in the specified
   * panel, without the context lines.
   *
   * @param  {index}         id               id==0 for the left, id==1 for the right pane
   */
  this.getTextNoCtx = function(id) {
    if (this.text[id].txt.length === 0) return [];
    var txt = this.getTextCopy(id);
    txt.splice(0, this.text[id].ctxBefore); // remove context before
    txt.splice(txt.length - this.text[id].ctxAfter, txt.length); // remove context after
    return txt;
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
    this.elem.on("click", "#leftBeforeButton", function() {
      self.addContextBefore(0);
      self.addContextBefore(1);
    });
    this.elem.on("click", "#leftAfterButton", function() {
      self.addContextAfter(0);
      self.addContextAfter(1);
    });
  };

  SM.bindFunctions(this);
  this.init(HTMLelem,options);
};
