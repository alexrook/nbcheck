'use strict';

$(document).ready(function () {
    $('#mainTable').DataTable({
        "processing": true,
        "serverSide": true,
        ajax: {
            url: 'rs/nbn',
            dataSrc: 'row'
        },
        "columns": [
            {"data": "data[0]"},
            {"data": "data[1]"},
            {"data": "data[2]"},
            {"data": "data[3]"}
        ]
    });
});
