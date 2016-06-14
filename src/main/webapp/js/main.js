'use strict';


$(document).ready(function () {
    $('#mainTable').DataTable({
        "dom": '<t>',
        "processing": true,
        "serverSide": true,
        ajax: {
            url: 'rs/nbn',
            dataSrc: 'row'
        },
        "columns": [//https://datatables.net/reference/option/columns.data
            {"data": function (row) {
                    return row.data[0];
                }},
            {"data": function (row) {
                    return row.data[1];
                }},
            {"data": function (row) {
                    return row.data[2];
                }},
            {"data": function (row) {
                    return row.data[3];
                }}
        ]
    });
});
