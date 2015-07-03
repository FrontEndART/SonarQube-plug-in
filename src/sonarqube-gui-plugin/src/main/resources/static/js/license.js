function addLicense(language, licenses, isSMHeaderShown) {
    var content = "";
    if (!isSMHeaderShown) {
        content += '<div style="padding-top: 6px;">';
        content += '<table id="sourcemeter_license_table" class="license_table" align = "center">';
        content += '<tr class="sm_license_header"><td>Language</td><td>Full functionality</td><td>Limited functionality</td><td>Not executed</td></tr>';
    }

    content += '<tr><td>' + language + '</td>';
    var tools = $j.parseJSON(licenses);
    var i = 0;
    content += '<td class="license_status_full">';
    for (i = 0; i < tools.full.length; i++) {
        content += tools.full[i];
        if (i < tools.full.length - 1) {
            content += ', ';
        }
    }
    content += '</td>';
    content += '<td class="license_status_limited">';
    for (i = 0; i < tools.limited.length; i++) {
        content += tools.limited[i];
        if (i < tools.limited.length - 1) {
            content += ', ';
        }
    }
    content += '</td>';
    content += '<td class="license_status_inactive">';
    for (i = 0; i < tools.inactive.length; i++) {
        content += tools.inactive[i];
        if (i < tools.inactive.length - 1) {
            content += ', ';
        }
    }
    content += '</td></tr>';
    
    return content;
}