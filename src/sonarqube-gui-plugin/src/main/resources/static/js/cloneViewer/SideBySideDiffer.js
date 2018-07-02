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
 * @param {[type]} HTMLelem [description]
 * @param {[type]} options  [description]
 */
SM.SideBySideDiffer = function(HTMLelem, options) {
  this.elem = null; // :DOMElement
  this.text = []; // :string[]

  this.init = function(HTMLelem,options) {
    options = (options) ? options:  {};
    this.text[1] = options.text1 ? options.text1 : {start: 0, txt: ""};
    this.text[2] = options.text2 ? options.text2 : {start: 0, txt: ""};

    this.bindElement(HTMLelem);
  };

  /**
   * Generates HTML of dependant of the current state of the object.
   * Then overwrites the HTML in the visible webpage as well.
   * @return {void}
   */
  this.renderAll = function() {
    var isMatch = (this.text[1].txt === this.text[2].txt);

    if (!isMatch) {
      var diff = JsDiff.createPatch('fileName', this.text[1].txt, this.text[2].txt, 'oldHeader', 'newHeader',{context:100000});
    } else {
      var diff = [
        "Index: fileName",
        "===================================================================",
        "--- fileName  oldHeader",
        "+++ fileName  newHeader",
        "@@ -1 +1 @@",
      ].join("\n");
      txt = this.text[1].txt.split("\n");
      txt = txt.map(function(s) {return " " + s});
      txt = txt.join("\n");
      diff = [diff, txt].join("\n");
    }

    var diff2htmlUi = new Diff2HtmlUI({diff: diff});
    diff2htmlUi.draw(this.elem, {
      inputFormat: 'diff',
      showFiles: false, matching:
      'lines',outputFormat: 'side-by-side',
      synchronisedScroll:true
    });
    diff2htmlUi.highlightCode(this.elem);

    if (isMatch) {
      $(".d2h-file-header").html("The instances are identical! (the left instance is shown)");
      $(".d2h-files-diff").children()[1].remove(); // remove the right hand size panel
      $($(".d2h-files-diff").children()[0]).css({width:"100%"}); // stretch the left hand panel
    } else {
      $(".d2h-file-header").html("Diff:");
    }
    $(".d2h-diff-tbody tr:first-child").remove() // remove the annotation line (eg. "@@ -1 +1 @@")

    // overwrite line numbers to match line numbers of the original file
    for (var i = 1; i <= 2; i++) {
      var index = this.text[i].startLine;
      $($(".d2h-diff-tbody")[i-1])
        .find(".d2h-code-side-linenumber")
        .each(function(node) {
          $(this).html(index++)
        });
    }
  };

  this.setText = function (id, obj) {
    if (id >= this.text.length) {
      throw "IndexOutOfBoundsException";
      return;
    }
    this.text[id] = obj;
    this.renderAll();
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
  };

  SM.bindFunctions(this);
  this.init(HTMLelem,options);
};
