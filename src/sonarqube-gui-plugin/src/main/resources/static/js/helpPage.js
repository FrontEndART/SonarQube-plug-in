var selectedHelpPageLanguage = "";
var selectedHelpPageMetric = "";

function updateHelpPage(language, metric) {
    selectedHelpPageLanguage = language;
    selectedHelpPageMetric = metric;

    $j('.sm-help-button').each(function() {
      $j(this).removeClass("selected");
    });

    var languageButton = $j('#sm-help-button-' + language);
    languageButton.addClass("selected");
    var container = $j('#sm-metrics-help-container');
    container.addClass("loading");
    var ajaxUrl = '/sourcemeter_' + language + '_help/sourcemeter_help_page';

    if (language === 'general') {
      ajaxUrl = generalUGURL;
    }

    $j('#sm-ug-container').attr('src', ajaxUrl);
    container.removeClass("loading");
  }

function resizeUG() {
    var maxWidth = $j('.tabs').width();
    var contentBody = $j('#sm-ug-container').contents().find("body");
    contentBody.width(maxWidth - 100);
    $j('#sm-ug-container').height(contentBody.height() + 100);
    $j('#sm-ug-container').width(maxWidth);
    var iframeOffset = $j("#sm-ug-container", window.parent.document).offset();
    if (selectedHelpPageMetric) {
      setTimeout(function() {
          var metricElement = $j('#sm-ug-container').contents().find("#" + selectedHelpPageMetric);
          if (metricElement) {
              var offset = metricElement.offset();
              window.parent.scrollTo(offset.left, offset.top + iframeOffset.top);
            }
      }, 1000);
    }
  }

  $j(function(){
    $j('#sm-ug-container').load(function() {
      var iframeOffset = $j("#sm-ug-container", window.parent.document).offset();
      $j("<style type='text/css'> #nonav { margin: 0 !important; } html{ background: #fff !important; } body{background: #fff !important; box-shadow: 0 0 0 0 !important}</style>").appendTo($j('#sm-ug-container').contents().find("head"));
      $j('#sm-ug-container').contents().find("LINK[href='/css/sonar.css']").remove();
      resizeUG();
      $j('#sm-ug-container').contents().find("a").each(function () {
            var link = $j(this);
            var href = link.attr("href");
            if (href && href[0] === "#") {
                var name = href.substring(1);
                $j(this).click(function () {
                    var nameElement = $j('#sm-ug-container').contents().find("[name='" + name + "']");
                    var idElement = $j('#sm-ug-container').contents().find("#" + name);
                    var element = null;
                    if (nameElement.length > 0) {
                        element = nameElement;
                    } else if (idElement.length > 0) {
                        element = idElement;
                    }
                    if (element) {
                        var offset = element.offset();
                        window.parent.scrollTo(offset.left, offset.top + iframeOffset.top);
                    }
                    return false;
                });
           } else {
               $j(this).attr("target", "_blank");
           }
        });
    });
  });