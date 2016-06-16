'use strict';

var hideNormalCaption_hide = "Отображенно все",
        hideNormalCaption_show = "Спрятаны не требующие внимания";

function getWarn(dnsName, nbNamesString) {

    var nbnames = nbNamesString ? nbNamesString.split(',') : [];

    dnsName = dnsName ? dnsName.toLowerCase() : "";

    for (var i = 0; i < nbnames.length; i++) {

        if (dnsName.indexOf(".") > 0) {

            if (dnsName.startsWith(nbnames[i].toLowerCase() + ".")) {
                return "normal";
            }

        } else {

            if (dnsName === nbnames[i].toLowerCase()) {
                return "normal";
            }
        }
    }

    return nbnames.length > 0 ? "bg-danger" : (dnsName ? "bg-info" : "bg-danger");

}

function toggleNormal() {
    var hideNormal = $("#hideNormal").is(":checked");

    var normalS = $("#mainTable .normal");

    if (hideNormal) {
        normalS.hide();
        $("#hideNormalCaption").text(hideNormalCaption_show);
        $("#hideNormal").attr("title", hideNormalCaption_show);
    } else {
        normalS.show();
        $("#hideNormalCaption").text(hideNormalCaption_hide);
        $("#hideNormal").attr("title", hideNormalCaption_hide);
    }
}


function setupTableSorter() {
    $("#mainTable").tablesorter({cssHeader: "sortableHeader",
        cssAsc: "th-glyphicon glyphicon-menu-up",
        cssDesc: "th-glyphicon glyphicon-menu-down",
        headers: {0: {sorter: false}}}

    );
}


$(document).ready(function () {

    $.getJSON("rs/nbn", function (data) {

        var tbody = $("<tbody/>").appendTo("#mainTable");

        var rows = data.row;

        $.each(rows, function (key, val) {

            // console.log(key);
            // console.log(val);

            $("<tr/>", {"class": getWarn(val.data[2], val.data[3])})
                    .append($("<td/>").text(val.data[0]))
                    .append($("<td/>").text(val.data[1]))
                    .append($("<td/>").text(val.data[2]))
                    .append($("<td/>").text(val.data[3]))
                    .appendTo(tbody);
        });


    }).done(function () {
        setupTableSorter();
        $("#waitImg").hide();
        toggleNormal();

        $("#hideNormal").change(toggleNormal);

    });
});
