function updateCloneInstanceAndMetrics(url, group_ind, index, linesCount, from_lines, included_to_lines, instance_key, class_key) {
    var newTable = document.getElementsByName('metrics-' + instance_key);
    var prevClone = document.getElementById(class_key).getElementsByClassName(
            'selectedclone');
    var i = 0;
    for (i = prevClone.length - 1; i >= 0; --i) {
        prevClone[i].style.display = 'none';
        prevClone[i].className = '';
    }

    for (i = 0; i < newTable.length; i++) {
        newTable[i].style.display = '';
        newTable[i].className = 'selectedclone';
    }

    return updateDuplicationLines(url, group_ind, index, linesCount,
            from_lines, included_to_lines);
}

function updateDuplicationLines(url, groupId, itemId, linesCount, fromLine, toLine) {
    $j('#duplGroup_' + groupId + ' p.selected').removeClass('selected');
    $j('#duplCount-' + groupId + '-' + itemId).addClass('selected');
    $j('#duplFrom-' + groupId + '-' + itemId).addClass('selected');
    $j('#duplName-' + groupId + '-' + itemId).addClass('selected');
    $j('#duplLoading-' + groupId).addClass('loading');

    if ($j('#source-' + groupId).hasClass('expanded')) {
        toLine = fromLine + linesCount;
    }
    $j.ajax({
        url : url + "&to_line=" + toLine + "&from_line=" + fromLine
                + "&lines_count=" + linesCount + "&group_index=" + groupId,
        success : function(response) {
            $j('#source-' + groupId).html(response);
        }, type : 'get'
    });
    return false;
}