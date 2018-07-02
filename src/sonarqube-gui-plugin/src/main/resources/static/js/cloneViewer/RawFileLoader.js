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
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRAC, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

SM.RawFileLoader = new (function() { // Singleton object
  var self = this;
  var storeValue = true;
  this.cache = {};

  /**
   * Gets raw file data
   * @param  {string}   fileUrl  url of the file
   * @param  {Function} callback is called with the raw data passed as argument (string)
   * @return {void}
   */
   this.requestRawFile = function(filePath, callback) {
    if (storeValue && filePath in this.cache) {
      callback(this.cache[filePath]);
      return;
    }
    $.get(location.origin + '/api/sources/raw',
      {key: filePath},
      (function(data) {
        if (storeValue) {
          this.cache[filePath]=data;
        }
        callback(data);
      }).bind(this));
    };

  /**
   * Gets raw file data, only returns lines fromLine toLine
   * @param  {string}   filePath  url of the file
   * @param  {Function} callback is called with the raw data passed as argument (string)
   * @param  {Number}   fromLine starting default is 0
   * @param  {Number}   toLine   ending line default is file length, truncated if longer
   * @return {void}
   */
    this.requestSliceOfRawFile = function(callback, filePath, fromLine, toLine) {
        this.requestRawFile(filePath, function(rawFile) {
            var temp = [];
            var x = rawFile.split("\n");

            if (fromLine === undefined) {
                fromLine = 0;
            }
            if (toLine === undefined || toLine > x.length) {
                toLine = x.length;
            }

            for (var i = fromLine-1; i < toLine-1; i++) {
                temp.push(x[i]);
            }
            callback(temp);
        });
    };
})();
