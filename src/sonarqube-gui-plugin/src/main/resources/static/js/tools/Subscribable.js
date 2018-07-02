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
 * This class implements functionality for managing subscriptions to events.
 */
SM.Subscribable = function () {
  this.subscriptions = {
    index: 0
  }; // current subscriptions

  /**
   * Event handling helper function.
   * By calling this function an eventhandler can be registered for specific events inside this object.
   * The object will handle calling these at apropriate times. Events can be single or indefinite fire events.
   *
   * Supported events:
   * finishedAllRequests noParameters   | fires after next batch of metrics is retrieved
   *
   * @param  {String}   event    the event id
   * @param  {Function} callback
   * @param  {bool}     persist  if false, event handler will be called the first time
   *                             and the subscription will be removed thereafter
   * @return {index}    subscriptionID
   */
  this.subscribe = function(event, callback, persist) {
    persist = (typeof persist === "undefined")? true : false;
    if (typeof this.subscriptions[event] === "undefined") this.subscriptions[event] = [];
    this.subscriptions[event][this.subscriptions.index] = {
      callback: callback,
      persist: persist,
      index: this.subscriptions.index
    };
    return this.subscriptions.index++;
  };

  /**
   * Manually fire an event
   *
   * @param  {String}       event the event id
   * @param  {any} param    parameters passed to the callbacks
   * @return {void}
   */
  this.emit = function(event, param) {
    if (typeof this.subscriptions[event] === "undefined") {
      // throw "UndefinedEventException";
      return;
    }
    this.subscriptions[event].forEach(function(e) {
      e.callback(param);
      if (!e.persist) self.removeSubscription(e.index)
    });
  };

  /**
   * Remove a subscription
   * @param  {index} index  the subscriptionID returned at the time of subscription by `subscribe()`
   * @return {void}
   */
  this.removeSubscription = function(index) {
    var keys = Object.keys(this.subscriptions);
    for (var i = 0; i < keys.length; i++) {
      event = this.subscriptions[keys[i]];
      if (typeof event[index] !== "undefined") {
        event.splice(index, 1);
        break;
      }
    }
  };

};
