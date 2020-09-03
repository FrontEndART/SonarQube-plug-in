SM.globalinit = function(){
  /*
   *  Load the Usersguides into variables for later use. Ideally theese would
   *  be displayed as the help pages on the SourceMeter Help page, but I imple-
   *  mented this function after I wrote the SourceMeter Help page, and couldnt
   *  figure out a fast and easy way to refactor the code, so refacoring remains
   *  a todo for later. The usersguides loaded here are used by the Metric.js
   *  class, to fetch tooltip data.
   */
  SM.languages.forEach(function(lang, i) {
    $.get(window.baseUrl + '/static/' + lang.pluginId + '/help/usersguide.html')
      .done(function(data, status, xhr) {
        SM.languages[i].helpPage = data;
        SM.languages.helpPageLoaded[lang.id] = true;
      })
      .fail(function(data, status, xhr) {
        SM.languages[i].helpPage = [
          '<div class="ui-state-error ui-state-highlight ui-corner-all" style="padding:10px"> ',
          '  In order to see the users guide for the SourceMeter ' + lang.symbol + ' ',
          '  scanner plugin, please put the ',
          '  sourcemeter-analyzer-' + lang.id.toLowerCase() + '-plugin-2.0.0.jar',
          '  into the extensions/plugins directory of your SonarQube instance. ',
          '</div>'
        ].join('/n');
      });
  });   
}