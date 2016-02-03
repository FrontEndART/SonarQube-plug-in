var shouldDrilldownRefreshSelectedButtons = false;
var lastSelectedSourceMeterElement = '';
var refreshedSourceManually = false;
var selectedSourceMeterElements = [];
var refreshSourceCodeFunction = null;
var stopButtonLoading = false;
var refreshSourceInProgress = false;
var lastSelectedFromLine = false;
var lastSelectedToLine = false;
var lastDataKey = '';
var lastSelectedSourceMeterDrilldownElement = '';

function createSMEvents(smResource) {
    $j(document).bind('DOMNodeInserted', function(e) {
        var element = e.target;
        if ($j('.component-viewer-header-measure').length
            && !$j('#sm_source_button').length
            && lastDataKey
            && !refreshSourceInProgress) {

            insertSourceMeterButtons(lastDataKey);
        }

        if ($j(element).hasClass('code-issues')) {
            replaceBlockerViolationTrace();
        }
    });
    $j(document).ajaxComplete(
        function(event, xhr, settings) {
            if (document.URL.indexOf("issues/search") > -1) {
                return;
            }
            var url = settings.url;
            var sourceURLSubstring = "/api/sources/show?key=";
            if (url.indexOf("/api/issues/") > -1) {
                replaceBlockerViolationTrace();
            }
            if (url.indexOf(sourceURLSubstring) > -1) {
                placeSourceMeterHelperLink();
                if (refreshSourceCodeFunction == null) {
                    var obj = $j('#sourcemeter-dummy-href');
                    if (obj.length) {
                        var events = $j._data(obj[0], "events");
                        if (events !== undefined) {
                            createRefreshSourceFunction(events.click[0].handler);
                        } else {
                            if (window.location.href.indexOf("/issues/") < 0) {
                                window.drilldown = {}; // prevents source code reloading from showing issues in popup
                                window.drilldown.metric = "";
                            }

                            $j.getScript("/js/require.js").done(function() {
                                $j.getScript("/js/drilldown/app.js").done(function() {
                                    setTimeout(function() {
                                        var obj = $j('#sourcemeter-dummy-href');
                                        var events = $j ._data(obj[0], "events");
                                        createRefreshSourceFunction(events.click[0].handler);
                                    }, 5000);
                                });
                            });
                        }
                    }
                }

                var data_key = url.substring(sourceURLSubstring.length, url.length);
                if (!refreshSourceInProgress) {
                    $j.ajax({
                        url : '/sourcemeter_drilldown/is_sm_resource_in_file?data_key=' + data_key,
                        success : function(response) {
                            if (response === "true") {
                                data_key = decodeURIComponent(data_key);
                                if (smResource) {
                                    insertSourceMeterButtons(data_key, true, smResource, true);
                                } else {
                                    insertSourceMeterButtons(data_key, false, false, true);
                                }
                            }
                        }, type : 'get'
                    });
                }
            }
        }
    );

    $j("body").delegate(".component-viewer-header-measures-expand", "click",
            function() {
                var smButton = $j("#sm_source_button");
                if (smButton.hasClass('active')) {
                    smButton.removeClass('active');
                    $j('#sm_source_toggle').removeClass('active');
                    $j('.sourcemeter-metrics-container').remove();
                }
            });

    $j("body").delegate(".sourcemeter-refresh-source", "click", function(e) {
        refreshSourceCodeFunction(e);
        var key = $j(this).attr('data-key');
        insertSourceMeterButtons(key, true, lastSelectedSourceMeterDrilldownElement);
        stopButtonLoading = true;
    });
}

function insertSourceMeterButtons(data_key, clickOnButton, lastSelectedElement, forceRefresh) {
    if (stopButtonLoading && !forceRefresh) {
        return;
    }
    stopButtonLoading = true;

    lastDataKey = data_key;
    setTimeout(
            function() {
                if ($j('#sm_source_button').length) {
                    $j('#sm_source_button').remove();
                }
                if ($j('#sm_clones_button').length) {
                    $j('#sm_clones_button').remove();
                }
                var headerMeasures = $j(".component-viewer-header-measures");
                headerMeasures
                        .append("<div class='component-viewer-header-measures-scope'><a id='sm_source_button' href='#' onclick='showHideSourceMeterDrilldown(\""
                                + data_key
                                + "\")' data-scope='sourcemeter' class='sm-button'><div class='component-viewer-header-measure'><span class='component-viewer-header-measure-value'>SM</span><span class='component-viewer-header-measure-label'>Source</span></div><i class='icon-dropdown'></i></a></div>");
                if ($j(".js-header-tab-duplications ").length) {
                    headerMeasures
                            .append("<div class='component-viewer-header-measures-scope'><a id='sm_clones_button' href='#' onclick='showClones(\""
                                    + data_key
                                    + "\")' data-scope='sourcemeter' class='sm-button'><div class='component-viewer-header-measure'><span class='component-viewer-header-measure-value'>SM</span><span class='component-viewer-header-measure-label'>Clones</span></div></a></div>");
                }
                if (clickOnButton) {
                    checkComponentViewerSize();
                    shouldDrilldownRefreshSelectedButtons = true;
                    if (refreshedSourceManually) {
                        refreshedSourceManually = false;
                    } else {
                        lastSelectedSourceMeterElement = lastSelectedElement;
                    }
                    $j('#sm_source_button').click();
                }

                stopButtonLoading = false;
                refreshSourceInProgress = false;
            }, 1000);
}

function highlightLines(bline, eline) {
    $j('.row-highlighted').each(function() {
        $j(this).removeClass('row-highlighted');
    });

    var firstLine = false;
    $j('.row').each(function() {
        var line = parseInt($j(this).attr('data-line-number'));

        if (line === bline) {
            firstLine = $j(this);
        }

        if (line >= bline && line <= eline) {
            $j(this).addClass('row-highlighted');
        }
    });

    if (firstLine) {
        var offset = firstLine.position().top;
        $j(document).scrollTop(offset);
        offset = firstLine.position().top;
        var parent = $j('.component-viewer-source');
        offset = parent.scrollTop() + offset;
        parent.animate({
            scrollTop : offset
        }, 300);
    }
}

function showAllLines(bline, eline) {
    var allLinesShown = false;
    var lastLine = bline;
    $j('.row').each(function() {
        var line = parseInt($j(this).attr('data-line-number'));
        if (line < bline) {
            return true; // continue
        }

        if (line > lastLine) {
            return false; // break
        }

        lastLine++;
        if (line === eline) {
            allLinesShown = true;
            return false; // break iteration
        }
    });

    if (!allLinesShown) {
        $j('.js-actions').click();
        $j('.js-full-source').click();
    }
}

function showHideSourceMeterDrilldown(data_key, refresh) {
    var expandedBar = $j(".component-viewer-header-expanded-bar");
    highlightLines(0, 0);
    if (refresh || expandedBar.is(':empty')
            || $j("#source-meter-drilldown").length === 0) {
        $j(".sourcemeter-metrics-container").remove();
        $j("#sm_source_button").addClass('active');
        $j('.component-viewer-header-measures-expand').each(function() {
            if ($j(this).hasClass('active')) {
                $j(this).removeClass('active');
            }
        });
        expandedBar.show();
        expandedBar.empty();
        expandedBar.addClass("loading");
        expandedBar.addClass("sm-loading");
        $j.ajax({
            url : '/sourcemeter_drilldown/load_drilldown?data_key=' + data_key,
            success : function(response) {
                $j('#sm_source_toggle').addClass('active');
                expandedBar.removeClass("loading");
                expandedBar.removeClass("sm-loading");
                expandedBar.html(response);
                if (shouldDrilldownRefreshSelectedButtons) {
                    refreshDrilldownButtons();
                }
            }, type : 'get'
        });
    } else {
        expandedBar.html("");
        $j("#sm_source_button").removeClass('active');
        $j('#sm_source_toggle').removeClass('active');
        $j('.sourcemeter-metrics-container').remove();
    }
}

function updateMethodsForClass(id, isProjectId) {
    var methodContainer = $j("#method_selector").parent();
    methodContainer.hide();
    methodContainer.addClass("loading");
    methodContainer.addClass("sm-loading");
    
    var ajaxURL =  '/sourcemeter_drilldown/load_method_drilldown?';
    if (isProjectId) {
        ajaxURL += 'project_id=' + id
    } else {
        ajaxURL += 'snapshot_id=' + id;
    }

    $j.ajax({
        url : ajaxURL,
        success : function(response) {
            methodContainer.removeClass("loading");
            methodContainer.removeClass("sm-loading");
            var methodIds = $j.parseJSON(response);
            methodContainer.find("tr").each(function() {
                var methodId = $j(this).attr("id");
                methodId = parseInt(methodId.substring(11, methodId.length));
                if ($j.inArray(methodId, methodIds) < 0) {
                    $j(this).hide();
                } else {
                    $j(this).show();
                }
            });
            methodContainer.show();
            hideEmptySourceMeterTables();
        }, type : 'get'
    });
}

function showClones(data_key) {
    window.open('/sourcemeter_duplications/load_duplications_page?data_key='
                + data_key, 'SourceMeter Clones',
                'height=900,width=1250,scrollbars=1,resizable=1');
    $j('#sm_clones_button').removeClass('active');
}

function placeSourceMeterHelperLink() {
    if ($j(".component-viewer-header-measures").length) {
        $j(body).append('<a href="#" style="display: none;" id="sourcemeter-dummy-href" title="" data-key="" class="js-drilldown-link underlined-link">BImage.h</a>');
    }
}

function placeAccordionPanel() {
    if (!$j('#accordion-panel').length) {
        var html = $j(body).html();
        $j(body).html("<div id='content'><div id='accordion-panel'>"
                      + html
                      + "</div></div>");
    }
}

function createRefreshSourceFunction(refreshFunction) {
    refreshSourceCodeFunction = function(e) {
        refreshSourceInProgress = true;
        placeAccordionPanel();
        refreshFunction(e);
    }
}

function checkComponentViewerSize() {
    if ($j('.component-viewer-source').width() === 0) {
        resizeComponentViewer();
        $j(window).resize(function() {
            resizeComponentViewer();
        });

    }
}

function resizeComponentViewer() {
    setTimeout(function() {
        var newWidth = $j('.component-viewer-header').width();
        var newHeight = $j('#body').height() - $j('.component-viewer-header').height() - 20;
        $j('.component-viewer-source').width(newWidth);
        $j('.component-viewer-source').height(newHeight);
    }, 500);
}

function refreshDrilldownButtons() {
    var intervalUntilElementIsLoaded = setInterval(function() {
        var lastElement = $j('#' + lastSelectedSourceMeterElement);
        if (!lastElement.length) {
            lastElement = $j('#class_row_' + lastSelectedSourceMeterElement);
        }
        if (!lastElement.length) {
            lastElement = $j('#method_row_' + lastSelectedSourceMeterElement);
        }
        if (!lastElement.hasClass('selected')) {
            if (lastElement.length) {
                lastElement.find('a')[1].click();
            }
            window.clearInterval(intervalUntilElementIsLoaded);
            selectedSourceMeterElements = [];
            lastSelectedSourceMeterElement = '';
            shouldDrilldownRefreshSelectedButtons = false;
        }
    }, 500);
}

function replaceBlockerViolationTrace(forceReplace) {
    setTimeout(function() {
        $j('.rulename').each(function(i, v) {
            if (!forceReplace && $j(this).hasClass('replaced')) {
                return true; // continue iteration
            } else {
                $j(this).addClass('replaced');
            }
            var warningtext = v.innerHTML;
            var url = '/dashboard/index/';
            warningtext = warningtext.replace('Source:', '&ltbr&gtSource:');
            warningtext = warningtext.replace('Sink:', '&ltbr&gtSink:');
            warningtext = warningtext.replace('Trace:', '&ltbr&gtTrace:');
            warningtext = warningtext.replace(/__([0-9]+):([^:]*):([0-9]+):([\+\-]?[0-9]+)__/g, '&lta target="_blank" onclick="window.open(this.href, \'stacktrace window\', \'height=800,width=1250,scrollbars=1,resizable=1\').focus();event.stopPropagation();return false;" href="'
                + url + '$1?display_title=true&highlighted_line=$3"&gt$2($3)[$4]&lt/a&gt');
            v.innerHTML = $j('<div/>').html(warningtext).text();
        });
    }, 500);
};

function refreshSource(rowIdd, fromLine, toLine, smResourceId) {
    if (smResourceId && smResourceId > 0) {
        lastSelectedSourceMeterElement = smResourceId;
        refreshedSourceManually = true;
    }
    lastSelectedFromLine = fromLine;
    lastSelectedToLine = toLine;
    $j('#' + rowIdd).find('.sourcemeter-refresh-source').click();
}

function showMetrics(id, rowId, bline, eline, isProjectId) {
    showAllLines(bline, eline);
    lastSelectedSourceMeterDrilldownElement = rowId;
    var metricsDiv = $j("#sourcemeter_metrics");
    metricsDiv.html("");
    metricsDiv.addClass("loading");
    metricsDiv.addClass("sm-loading");

    var row = $j('#' + rowId);
    row.parent().children().each(function() {
        if ($j(this).hasClass('selected')) {
            $j(this).removeClass('selected');
        }
    });

    row.addClass('selected');

    var drilldownUrl = '/sourcemeter_drilldown/load_metrics?';
    if (isProjectId) {
        drilldownUrl += 'project_id=' + id;
    } else {
        drilldownUrl += 'snapshot_id=' + id;
    }

    $j.ajax({
        url : drilldownUrl,
        success : function(response) {
            metricsDiv.removeClass("loading");
            metricsDiv.removeClass("sm-loading");
            $j('.sourcemeter-metrics-container').remove();
            $j('.row').each(function() {
                var line = parseInt($j(this).attr('data-line-number'));
                if (line === bline) {
                    var lineElement = $j(this).find('.line');
                    lineElement.prepend("<div class='sourcemeter-metrics-container'><div class='code-issue-name-rule sourcemeter-metrics-header'><span class='rulename'>SourceMeter Metrics</span><span id='sourcemeter-metrics-collapse'> - Expand</span></div>"
                                        + response + "</div>");
                    highlightLines(bline, eline);
                    return false; // break iteration
                }
            });
        }, type : 'get'
    });
}

function hideEmptySourceMeterTables() {
    var isSomethingShown = false;
    if ($j('#class_selector').length && $j('#class_selector').html()
            && $j('#class_selector').html().replace(/\s/g, '') === "") {
        $j('#class_selector').parent().parent().remove();
    } else {
        isSomethingShown = true;
    }
    
    if ($j('#method_selector').length && $j('#method_selector').html()
            && $j('#method_selector').html().replace(/\s/g, '') === "") {
        $j('#method_selector').parent().parent().hide();
    } else {
        $j('#method_selector').parent().parent().show();
        var isMethodVisible = false;
        $j('#method_selector').find("tr").each(function() {
            if ($j(this).is(":visible")) {
                isMethodVisible = true;
                isSomethingShown = true;
                return false;   //break iteration
            }
        });
        if (!isMethodVisible) {
            $j('#method_selector').parent().parent().hide();
        }
    }

    if (!isSomethingShown) {
        $j('#source-meter-drilldown').html('<p class="error">There are no SourceMeter resources in the selected file!</p>');
    }
}

function showMetricsAndFilePaths(id, rowId, bline, eline, isProjectId, data_key, isClass) {
    var currentItem = $j('#' + rowId);
    if (currentItem.hasClass('selected')) {
        if (isClass) {
            showHideSourceMeterDrilldown(lastDataKey, true);
            return;
        } else {
            currentItem.removeClass('selected');
            var selectedClass = $j('#class_selector').find('.selected');
            if (selectedClass.length) {
                selectedClass.removeClass('selected');
                selectedClass.find('a')[1].click();
                return;
            }
        }
    }
    var drilldownUrl = '/sourcemeter_drilldown/filepaths?';
    if (isProjectId) {
        drilldownUrl += 'project_id=' + id;
    } else {
        drilldownUrl += 'snapshot_id=' + id;
    }
    
    if (isClass) {
        updateMethodsForClass(id, isProjectId);
    }

    if (lastSelectedFromLine) {
        bline = lastSelectedFromLine;
        eline = lastSelectedToLine;
        drilldownUrl += '&bline=' + bline + '&eline=' + eline;
    }

    showMetrics(id, rowId, bline, eline, isProjectId);

    if (data_key) {
        drilldownUrl += '&data_key=' + data_key;
    }

    lastSelectedFromLine = false;
    lastSelectedToLine = false;

    $j.ajax({
        url : drilldownUrl, success : function(response) {
            if (response.split("icon-qualifier-fil").length > 2) { 
                //file select table should only be shown when there are more than one file occurrencies
                $j('#sourcemeter-filepaths-row').show();
                $j('#sourcemeter-filepaths').html(response);
            } else {
                $j('#sourcemeter-filepaths-row').hide();
            }
        }, type : 'get'
    });
}

function showLinkForResource(resourceId) {
    var url = "/dashboard/index/" + lastDataKey + "?sm_resource=" + resourceId;
    window.open(url, resourceId, 'height=900,width=1250,scrollbars=1,resizable=1');
}