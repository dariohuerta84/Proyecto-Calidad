/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
var showControllersOnly = false;
var seriesFilter = "";
var filtersOnlySampleSeries = true;

/*
 * Add header in statistics table to group metrics by category
 * format
 *
 */
function summaryTableHeader(header) {
    var newRow = header.insertRow(-1);
    newRow.className = "tablesorter-no-sort";
    var cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Requests";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 3;
    cell.innerHTML = "Executions";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 7;
    cell.innerHTML = "Response Times (ms)";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 1;
    cell.innerHTML = "Throughput";
    newRow.appendChild(cell);

    cell = document.createElement('th');
    cell.setAttribute("data-sorter", false);
    cell.colSpan = 2;
    cell.innerHTML = "Network (KB/sec)";
    newRow.appendChild(cell);
}

/*
 * Populates the table identified by id parameter with the specified data and
 * format
 *
 */
function createTable(table, info, formatter, defaultSorts, seriesIndex, headerCreator) {
    var tableRef = table[0];

    // Create header and populate it with data.titles array
    var header = tableRef.createTHead();

    // Call callback is available
    if(headerCreator) {
        headerCreator(header);
    }

    var newRow = header.insertRow(-1);
    for (var index = 0; index < info.titles.length; index++) {
        var cell = document.createElement('th');
        cell.innerHTML = info.titles[index];
        newRow.appendChild(cell);
    }

    var tBody;

    // Create overall body if defined
    if(info.overall){
        tBody = document.createElement('tbody');
        tBody.className = "tablesorter-no-sort";
        tableRef.appendChild(tBody);
        var newRow = tBody.insertRow(-1);
        var data = info.overall.data;
        for(var index=0;index < data.length; index++){
            var cell = newRow.insertCell(-1);
            cell.innerHTML = formatter ? formatter(index, data[index]): data[index];
        }
    }

    // Create regular body
    tBody = document.createElement('tbody');
    tableRef.appendChild(tBody);

    var regexp;
    if(seriesFilter) {
        regexp = new RegExp(seriesFilter, 'i');
    }
    // Populate body with data.items array
    for(var index=0; index < info.items.length; index++){
        var item = info.items[index];
        if((!regexp || filtersOnlySampleSeries && !info.supportsControllersDiscrimination || regexp.test(item.data[seriesIndex]))
                &&
                (!showControllersOnly || !info.supportsControllersDiscrimination || item.isController)){
            if(item.data.length > 0) {
                var newRow = tBody.insertRow(-1);
                for(var col=0; col < item.data.length; col++){
                    var cell = newRow.insertCell(-1);
                    cell.innerHTML = formatter ? formatter(col, item.data[col]) : item.data[col];
                }
            }
        }
    }

    // Add support of columns sort
    table.tablesorter({sortList : defaultSorts});
}

$(document).ready(function() {

    // Customize table sorter default options
    $.extend( $.tablesorter.defaults, {
        theme: 'blue',
        cssInfoBlock: "tablesorter-no-sort",
        widthFixed: true,
        widgets: ['zebra']
    });

    var data = {"OkPercent": 100.0, "KoPercent": 0.0};
    var dataset = [
        {
            "label" : "FAIL",
            "data" : data.KoPercent,
            "color" : "#FF6347"
        },
        {
            "label" : "PASS",
            "data" : data.OkPercent,
            "color" : "#9ACD32"
        }];
    $.plot($("#flot-requests-summary"), dataset, {
        series : {
            pie : {
                show : true,
                radius : 1,
                label : {
                    show : true,
                    radius : 3 / 4,
                    formatter : function(label, series) {
                        return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'
                            + label
                            + '<br/>'
                            + Math.round10(series.percent, -2)
                            + '%</div>';
                    },
                    background : {
                        opacity : 0.5,
                        color : '#000'
                    }
                }
            }
        },
        legend : {
            show : true
        }
    });

    // Creates APDEX table
    createTable($("#apdexTable"), {"supportsControllersDiscrimination": true, "overall": {"data": [0.9932935916542474, 800, 1500, "Total"], "isController": false}, "titles": ["Apdex", "T (Toleration threshold)", "F (Frustration threshold)", "Label"], "items": [{"data": [0.97, 800, 1500, "TC-PERF-001 GET /auth/csrf"], "isController": false}, {"data": [1.0, 800, 1500, "setUp Publicar ids como propiedades"], "isController": false}, {"data": [1.0, 800, 1500, "TC-PERF-001 GET /auth/me"], "isController": false}, {"data": [1.0, 800, 1500, "TC-PERF-005 Verificación: cupo_reservado == confirmadas <= cupo"], "isController": false}, {"data": [1.0, 800, 1500, "TC-PERF-003 GET /auth/csrf"], "isController": false}, {"data": [1.0, 800, 1500, "tearDown POST /auth/login"], "isController": false}, {"data": [1.0, 800, 1500, "setUp GET /tours/{id}/salidas"], "isController": false}, {"data": [1.0, 800, 1500, "setUp GET /auth/csrf"], "isController": false}, {"data": [1.0, 800, 1500, "setUp POST /hoteles (1 unidad)"], "isController": false}, {"data": [1.0, 800, 1500, "tearDown GET /auth/csrf"], "isController": false}, {"data": [1.0, 800, 1500, "TC-PERF-001 POST /auth/login"], "isController": false}, {"data": [1.0, 800, 1500, "TC-PERF-001 POST /auth/logout"], "isController": false}, {"data": [1.0, 800, 1500, "TC-PERF-003 Verificación: 1x201 y (N-1)x409"], "isController": false}, {"data": [1.0, 800, 1500, "setUp POST /tours (cupo limitado)"], "isController": false}, {"data": [1.0, 800, 1500, "TC-PERF-003 POST /reservas-habitaciones (simultáneo)"], "isController": false}, {"data": [1.0, 800, 1500, "setUp GET /hoteles/{id}"], "isController": false}, {"data": [1.0, 800, 1500, "setUp POST /auth/login"], "isController": false}, {"data": [1.0, 800, 1500, "TC-PERF-003 POST /auth/login"], "isController": false}]}, function(index, item){
        switch(index){
            case 0:
                item = item.toFixed(3);
                break;
            case 1:
            case 2:
                item = formatDuration(item);
                break;
        }
        return item;
    }, [[0, 0]], 3);

    // Create statistics table
    createTable($("#statisticsTable"), {"supportsControllersDiscrimination": true, "overall": {"data": ["Total", 671, 0, 0.0, 96.4441132637854, 6, 1288, 13.0, 218.0, 306.39999999999986, 867.1199999999965, 25.5696974315982, 19.29200297947946, 14.487227655094886], "isController": false}, "titles": ["Label", "#Samples", "FAIL", "Error %", "Average", "Min", "Max", "Median", "90th pct", "95th pct", "99th pct", "Transactions/s", "Received", "Sent"], "items": [{"data": ["TC-PERF-001 GET /auth/csrf", 150, 0, 0.0, 117.20666666666662, 6, 1288, 9.0, 498.30000000000064, 823.8499999999999, 1246.6900000000007, 7.78089013383131, 4.746545608984335, 2.423929641300965], "isController": false}, {"data": ["setUp Publicar ids como propiedades", 1, 0, 0.0, 107.0, 107, 107, 107.0, 107.0, 107.0, 107.0, 9.345794392523365, 0.31030957943925236, 0.0], "isController": false}, {"data": ["TC-PERF-001 GET /auth/me", 150, 0, 0.0, 11.66666666666667, 9, 33, 11.0, 14.0, 15.0, 25.350000000000136, 7.585335018963337, 4.8252804993678895, 5.669741624525917], "isController": false}, {"data": ["TC-PERF-005 Verificación: cupo_reservado == confirmadas <= cupo", 1, 0, 0.0, 18.0, 18, 18, 18.0, 18.0, 18.0, 18.0, 55.55555555555555, 30.002170138888893, 41.72092013888889], "isController": false}, {"data": ["TC-PERF-003 GET /auth/csrf", 20, 0, 0.0, 32.55, 8, 81, 27.5, 75.00000000000004, 80.8, 81.0, 19.76284584980237, 11.81138833992095, 3.9564291007905137], "isController": false}, {"data": ["tearDown POST /auth/login", 1, 0, 0.0, 201.0, 201, 201, 201.0, 201.0, 201.0, 201.0, 4.975124378109452, 6.233481032338308, 2.113456156716418], "isController": false}, {"data": ["setUp GET /tours/{id}/salidas", 1, 0, 0.0, 17.0, 17, 17, 17.0, 17.0, 17.0, 17.0, 58.8235294117647, 31.767003676470587, 44.1750919117647], "isController": false}, {"data": ["setUp GET /auth/csrf", 1, 0, 0.0, 66.0, 66, 66, 66.0, 66.0, 66.0, 66.0, 15.151515151515152, 9.055397727272727, 3.0332623106060606], "isController": false}, {"data": ["setUp POST /hoteles (1 unidad)", 1, 0, 0.0, 33.0, 33, 33, 33.0, 33.0, 33.0, 33.0, 30.303030303030305, 30.98366477272727, 32.34493371212121], "isController": false}, {"data": ["tearDown GET /auth/csrf", 1, 0, 0.0, 11.0, 11, 11, 11.0, 11.0, 11.0, 11.0, 90.9090909090909, 54.33238636363637, 18.199573863636363], "isController": false}, {"data": ["TC-PERF-001 POST /auth/login", 150, 0, 0.0, 208.1333333333333, 197, 257, 207.0, 216.8, 224.79999999999995, 249.35000000000014, 7.603020933650971, 9.376069174818795, 3.3810178702671196], "isController": false}, {"data": ["TC-PERF-001 POST /auth/logout", 150, 0, 0.0, 12.933333333333335, 10, 27, 12.0, 15.0, 16.0, 26.49000000000001, 7.404116688879017, 3.796055919591293, 5.722283151932475], "isController": false}, {"data": ["TC-PERF-003 Verificación: 1x201 y (N-1)x409", 1, 0, 0.0, 40.0, 40, 40, 40.0, 40.0, 40.0, 40.0, 25.0, 0.5126953125, 0.0], "isController": false}, {"data": ["setUp POST /tours (cupo limitado)", 1, 0, 0.0, 140.0, 140, 140, 140.0, 140.0, 140.0, 140.0, 7.142857142857142, 9.270368303571427, 7.645089285714285], "isController": false}, {"data": ["TC-PERF-003 POST /reservas-habitaciones (simultáneo)", 20, 0, 0.0, 210.45, 95, 282, 225.0, 277.6, 281.8, 282.0, 68.02721088435375, 43.1281887755102, 69.56180909863946], "isController": false}, {"data": ["setUp GET /hoteles/{id}", 1, 0, 0.0, 18.0, 18, 18, 18.0, 18.0, 18.0, 18.0, 55.55555555555555, 56.20659722222223, 41.39539930555556], "isController": false}, {"data": ["setUp POST /auth/login", 1, 0, 0.0, 227.0, 227, 227, 227.0, 227.0, 227.0, 227.0, 4.405286343612335, 5.51951404185022, 1.8713862885462555], "isController": false}, {"data": ["TC-PERF-003 POST /auth/login", 20, 0, 0.0, 324.25, 214, 480, 313.5, 468.9, 479.45, 480.0, 15.337423312883436, 18.90517158742331, 6.598536953604294], "isController": false}]}, function(index, item){
        switch(index){
            // Errors pct
            case 3:
                item = item.toFixed(2) + '%';
                break;
            // Mean
            case 4:
            // Mean
            case 7:
            // Median
            case 8:
            // Percentile 1
            case 9:
            // Percentile 2
            case 10:
            // Percentile 3
            case 11:
            // Throughput
            case 12:
            // Kbytes/s
            case 13:
            // Sent Kbytes/s
                item = item.toFixed(2);
                break;
        }
        return item;
    }, [[0, 0]], 0, summaryTableHeader);

    // Create error table
    createTable($("#errorsTable"), {"supportsControllersDiscrimination": false, "titles": ["Type of error", "Number of errors", "% in errors", "% in all samples"], "items": []}, function(index, item){
        switch(index){
            case 2:
            case 3:
                item = item.toFixed(2) + '%';
                break;
        }
        return item;
    }, [[1, 1]]);

        // Create top5 errors by sampler
    createTable($("#top5ErrorsBySamplerTable"), {"supportsControllersDiscrimination": false, "overall": {"data": ["Total", 671, 0, "", "", "", "", "", "", "", "", "", ""], "isController": false}, "titles": ["Sample", "#Samples", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors", "Error", "#Errors"], "items": [{"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}, {"data": [], "isController": false}]}, function(index, item){
        return item;
    }, [[0, 0]], 0);

});
