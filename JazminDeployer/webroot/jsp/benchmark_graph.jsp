<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Benchmark Graph</title>
    <style>

        * {
            -moz-box-sizing: border-box;
            -webkit-box-sizing: border-box;
            box-sizing: border-box;
        }

        html, body {
            padding: 0;
            margin: 0;
            height: 100%;
            width: 100%;
            background-color: #ffffff;
        }

        body {
            font-family: "Helvetica Neue", "Helvetica", "Arial", sans-serif;
            font-size: 14px;
        }

        .table {
            position: relative;
            width: 100%;
            max-width: 100%;
            border-spacing: 0;
            border-collapse: collapse;
            overflow: auto;
        }

        .table > tbody > tr > td,
        .table > tbody > tr > th,
        .table > tfoot > tr > td,
        .table > tfoot > tr > th,
        .table > thead > tr > td,
        .table > thead > tr > th {
            padding: 8px;
            line-height: 1.42857143;
            vertical-align: middle;
        }

        .table tbody tr {
            border-top: 1px solid #ddd;
        }

        .text-center {
            text-align: center;
        }

        .text-ellipsis {
            text-overflow: ellipsis;
            -o-text-overflow: ellipsis;
            white-space: nowrap;
            overflow: hidden;
        }

        .chart-js {
            position: relative;
            height: 100%;
            width: 100%;
            padding-bottom: 200px;
        }

        .stat-view-bg {
            position: absolute;
            display: none;
            top: 0;
            left: 0;
            height: 100%;
            width: 100%;
            background-color: #0e0e0e;
            background-color: rgba(0, 0, 0, 0.5);
            z-index: 99;
        }

        .stat-view-bg.show {
            display: block;
        }

        .stat-view-bg .stat-view {
            position: relative;
            width: 80%;
            margin: 30px auto;
            max-width: 1024px;
            min-width: 640px;
            overflow: auto;
            background-color: #ffffff;
            border: solid 1px #ababab;
            -webkit-border-radius: 5px;
            -moz-border-radius: 5px;
            border-radius: 5px;
        }

        .stat-view-bg .stat-view .header {
            width: 100%;
            height: 50px;
            line-height: 50px;
            font-size: 20px;
            text-align: center;
            border-bottom: solid 2px #dddddd;
            background-color: #ffffff;
        }

        .stat-view-bg .stat-view .content {
            position: relative;
            max-height: 500px;;
            width: 100%;
            padding: 0 16px;
            overflow: auto;

        }

        .stat-view-bg .stat-view .content .viewer {
            position: relative;
            height: 100%;
            width: 100%;
            overflow: auto;
        }

        .stat-view-bg .stat-view .bottom {
            height: 50px;
            line-height: 50px;
            padding: 0 16px;
            border-top: solid 2px #dddddd;
            text-align: right;
            background-color: #ffffff;
        }

        .stat-view-bg .stat-view .bottom .btn-close {
            display: inline-block;
            line-height: normal;
            padding: 6px 16px;
            border: solid 1px #cccccc;
            -webkit-border-radius: 4px;
            -moz-border-radius: 4px;
            border-radius: 4px;
            cursor: pointer;
        }

        .stat-view-bg .stat-view .bottom .btn-close:hover {
            background-color: #f2f2f2;
        }

        .chart-js .benchmark-chart {
            position: relative;
            height: 100%;
            width: 100%;
            overflow: auto;
            padding: 16px;
        }

        .chart-js .benchmark-chart .highchartjs {
            position: relative;
            height: 100%;
            width: 100%;
            overflow: auto;
        }

        .chart-js .summary {
            position: absolute;
            bottom: 0;
            height: 200px;
            padding: 20px 30px;
            width: 100%;
            z-index: 99;
            border-top: dashed 2px #888888;
        }

        .chart-js .summary .btn-view {
            padding: 0 4px;
            color: #232323;
            cursor: pointer;
            border: solid 1px #cccccc;
            -webkit-border-radius: 2px;
            -moz-border-radius: 2px;
            border-radius: 2px;
        }

        .chart-js .summary .btn-view:hover {
            background-color: #f2f2f2;
        }

        .chart-js .summary .name {
            height: 30px;
            line-height: 30px;
            font-size: 18px;
            font-weight: 600;
        }

        .chart-js .summary .name .value {
            color: #345678;
        }

        .chart-js .summary .item {
            display: inline-block;
            width: 320px;
            font-size: 16px;
            color: #345678;
        }

        .chart-js .summary .item .value {
            font-weight: 600;
        }

        .chart-js .time {
            padding: 5px 0;
        }

        .chart-js .time .value {
            color: #232323;
        }

        .chart-js .item .label {
            display: inline-block;
            width: 120px;
            text-align: center;
        }

        .chart-js .item.throughtput .value {
            color: #6599FF;
        }

        .chart-js .item.noOfSamples .value {
            color: #79A701;
        }

        .chart-js .item.noOfUsers .value {
            color: #35CE8D;
        }

        .chart-js .item.max .value,
        .chart-js .item.min .value,
        .chart-js .item.average .value {
            color: #118DF0;
        }

        .chart-js .item.deviation .value {
            color: #9068BE;
        }

        .chart-js .item.errorCount .value {
            color: #DB3A34;
        }

    </style>
</head>
<body>
<div class="chart-js">
    <div id="benchmark-chart" class="benchmark-chart">
        <div id="benchmark-chart-view" class="highchartjs"></div>
    </div>
    <div id="summary" class="summary">
        <div class="name">
            <span class="label">Summary</span>#<span class="value">--</span>
            <span id="btn-view" class="btn-view">≡</span>
        </div>
        <div class="time">
            <div class="item startTime">
                <span class="label">Start Time</span>:
                <span class="value">--</span>
            </div>
            <div class="item endTime">
                <span class="label">Current Time</span>:
                <span class="value">--</span>
            </div>
            <div style="clear:both;"></div>
        </div>
        <div class="items">
            <div class="item throughtput">
                <span class="label">Throughtput</span>:
                <span class="value">--</span>/min
            </div>
            <div class="item noOfSamples">
                <span class="label">No Of Samples</span>:
                <span class="value">--</span>
            </div>
            <div class="item noOfUsers">
                <span class="label">No Of Users</span>:
                <span class="value">--</span>
            </div>
            <div class="item errorCount">
                <span class="label">Error Count</span>:
                <span class="value">--</span>
            </div>
            <div class="item deviation">
                <span class="label">Deviation</span>:
                <span class="value">--</span>
            </div>
            <div class="item max">
                <span class="label">Max</span>:
                <span class="value">--</span>ms
            </div>
            <div class="item average">
                <span class="label">Average Value</span>:
                <span class="value">--</span>ms
            </div>
            <div class="item min">
                <span class="label">Min</span>:
                <span class="value">--</span>ms
            </div>
            <div style="clear:both;"></div>
        </div>
    </div>
</div>
<div id="stat-view-bg" class="stat-view-bg">
    <div id="stat-view" class="stat-view">
        <div class="header">Stats Of <span class="name">--</span></div>
        <div class="content">
            <div class="viewer">
                <table class=" table">
                    <thead>
                    <td>Name</td>
                    <td>Throughtput</td>
                    <td>No Of Samples</td>
                    <td>No Of Users</td>
                    <td>Max Value</td>
                    <td>Average Value</td>
                    <td>Min Value</td>
                    <td>Deviation</td>
                    <td>Error Count</td>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="text-center" colspan="9">No Datas</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="bottom">
            <div id="btn-close" class="btn-close">Close</div>
        </div>
    </div>
</div>
<script type="text/javascript" src="/js/jquery.js"></script>
<script type="text/javascript" src="/js/moment.js"></script>
<script type="text/javascript" src="/js/highcharts.js"></script>
<script type="text/javascript" src="/js/monitor.js"></script>
<script>
    Highcharts.setOptions({
        global: {
            useUTC: false
        },
        lang: {
            noData: "暂无数据"
        }
    });
    window.__refreshStats__ = function () {
        if (!$("#stat-view-bg").hasClass("show")) {
            return;
        }
        itAjax.action("/srv/benchmark/stats").params({
            id: "${benchmarkId}"
        }).success(function (result) {
            if (!result) {
                return;
            }
            var _stats = result.list;
            if (!_stats || _stats.length === 0) {
                return;
            }
            var _len = _stats.length;
            var html = [];
            for (var i = 0; i < _len; i++) {
                var _temp = _stats[i];
                html.push("<tr>");
                html.push("<td title='" + _temp.name + "' style='cursor: pointer;'>");
                html.push("<div class='text-ellipsis' style='width: 200px;'>");
                html.push(_temp.name);
                html.push("</div>");
                html.push("</td>");
                html.push("<td>");
                html.push(_temp.throughtput);
                html.push("/min");
                html.push("</td>");
                html.push("<td>");
                html.push(_temp.noOfSamples);
                html.push("</td>");
                html.push("<td>");
                html.push(_temp.noOfUsers);
                html.push("</td>");
                html.push("<td>");
                html.push(_temp.max);
                html.push("ms");
                html.push("</td>");
                html.push("<td>");
                html.push(_temp.average);
                html.push("ms");
                html.push("</td>");
                html.push("<td>");
                html.push(_temp.min);
                html.push("ms");
                html.push("</td>");
                html.push("<td>");
                html.push(_temp.deviation);
                html.push("</td>");
                html.push("<td>");
                html.push(_temp.errorCount);
                html.push("</td>");
                html.push("</tr>");
            }
            $("#stat-view .viewer").find("table > tbody").html(html.join(""));
            if (result.finished === true) {
                return;
            }
            window.setTimeout(function () {
                window.__refreshStats__();
            }, 1000);
        }).invoke();
    };
    window.__refreshChart__ = function (_chart) {
        itAjax.action("/srv/benchmark/data").params({
            id: "${benchmarkId}"
        }).success(function (result) {
            if (!result) {
                _chart.hideLoading();
                return;
            }
            window.__renderChart__(_chart, result.all);
            window.__renderTotal__(result.total);
            if (result.finished === true) {
                return;
            }
            window.setTimeout(function () {
                window.__refreshChart__(_chart);
            }, 1000);
        }).invoke();
    };
    window.__renderChart__ = function (_chart, data) {
        if (!data || data.length === 0) {
            _chart.hideLoading();
            return;
        }
        var _len = data.length;
        var _throughtput = [];
        var _noOfUsers = [];
        var _average = [];
        var _deviation = [];
        for (var i = 0; i < _len; i++) {
            var _temp = data[i];
            if (!_temp) {
                continue;
            }
            _throughtput.push([_temp.startTime, _temp.throughtput]);
            _noOfUsers.push([_temp.startTime, _temp.noOfUsers]);
            _average.push([_temp.startTime, _temp.average]);
            _deviation.push([_temp.startTime, _temp.deviation]);
        }
        _chart.hideLoading();
        var _min = _throughtput[0][0];
        _chart.update({
            xAxis: [
                {
                    min: _min
                }
            ],
            series: [
                {
                    id: "throughtput",
                    data: _throughtput
                },
                {
                    id: "noOfUsers",
                    data: _noOfUsers
                },
                {
                    id: "average",
                    data: _average
                },
                {
                    id: "deviation",
                    data: _deviation
                }
            ]
        });
    };
    window.__renderTotal__ = function (total) {
        if (!total) {
            return;
        }
        var $summary = $("#summary");
        $summary.find(".name .value").text(total.name);
        $("#stat-view").find(".name").text(total.name);
        try {
            $summary.find(".item.startTime .value").text(moment(total.startTime).format("YYYY-MM-DD HH:mm:ss.SSS"));
            $summary.find(".item.endTime .value").text(moment(total.endTime).format("YYYY-MM-DD HH:mm:ss.SSS"));
        } catch (e) {
            console.log(e);
        }
        $summary.find(".item.throughtput .value").text(total.throughtput);
        $summary.find(".item.noOfSamples .value").text(total.noOfSamples);
        $summary.find(".item.noOfUsers .value").text(total.noOfUsers);
        $summary.find(".item.min .value").text(total.min);
        $summary.find(".item.max .value").text(total.max);
        $summary.find(".item.average .value").text(total.average);
        $summary.find(".item.deviation .value").text(total.deviation);
        $summary.find(".item.errorCount .value").text(total.errorCount);
    };
    $(function () {
        $("body").on("click", "#btn-view", function () {
            $("#stat-view-bg").addClass("show");
            window.__refreshStats__();
        }).on("click", "#btn-close", function () {
            $("#stat-view-bg").removeClass("show");
        });
        var _hchart = new Highcharts.Chart("benchmark-chart-view", {
            chart: {
                showAxes: true
            },
            credits: {
                enabled: false
            },
            title: {
                text: "Benchmark"
            },
            noData: {
                style: {
                    fontSize: "14px",
                    color: "#888"
                }
            },
            xAxis: [
                {
                    id: "datetime",
                    type: "datetime",
                    dateTimeLabelFormats: {
                        millisecond: "%Y-%m-%d %H:%M:%S.%L",
                        second: "%H:%M:%S",
                        minute: "%H:%M:%S",
                        hour: "%H:%M:%S",
                        day: "%e. %b",
                        week: "%e. %b",
                        month: "%b \"%y",
                        year: "%Y"
                    },
                    minRange: 30000,
                    labels: {
                        overflow: "justify"
                    },
                    crosshair: true
                }
            ],
            yAxis: [
                {
                    labels: {
                        format: "{value}",
                        style: {
                            color: "#6599FF"
                        }
                    },
                    title: {
                        text: "Throughtput",
                        style: {
                            color: "#6599FF"
                        }
                    },
                    visible: false
                },

                {
                    title: {
                        text: "No Of Users",
                        style: {
                            color: "#35CE8D"
                        }
                    },
                    labels: {
                        format: "{value}",
                        style: {
                            color: "#35CE8D"
                        }
                    },
                    visible: false
                },
                {
                    title: {
                        text: "Average",
                        style: {
                            color: "#051181"
                        }
                    },
                    labels: {
                        format: "{value}",
                        style: {
                            color: "#051181"
                        }
                    },
                    visible: false
                },
                {
                    title: {
                        text: "Deviation",
                        style: {
                            color: "#9068BE"
                        }
                    },
                    labels: {
                        format: "{value}",
                        style: {
                            color: "#9068BE"
                        }
                    },
                    visible: false
                }
            ],
            tooltip: {
                shared: true,
                dateTimeLabelFormats: {
                    millisecond: "%Y-%m-%d %H:%M:%S.%L",
                    second: "%H:%M:%S",
                    minute: "%H:%M:%S",
                    hour: "%H:%M:%S",
                    day: "%e. %b",
                    week: "%e. %b",
                    month: "%b \"%y",
                    year: "%Y"
                }
            },
            legend: {
                align: "left",
                verticalAlign: "top",
                floating: true,
                backgroundColor: "#FFFFFF"
            },
            series: [
                {
                    id: "throughtput",
                    name: "Throughtput",
                    type: "spline",
                    color: "#6599FF",
                    yAxis: 0,
                    data: [],
                    marker: {
                        enabled: false
                    },
                    tooltip: {
                        valueSuffix: ""
                    }
                },
                {
                    id: "noOfUsers",
                    name: "No Of Users",
                    type: "spline",
                    color: "#35CE8D",
                    yAxis: 1,
                    data: [],
                    marker: {
                        enabled: false
                    },
                    dashStyle: "shortdot",
                    tooltip: {
                        valueSuffix: ""
                    }
                },
                {
                    id: "average",
                    name: "Average",
                    type: "spline",
                    color: "#051181",
                    yAxis: 2,
                    data: [],
                    marker: {
                        enabled: false
                    },
                    tooltip: {
                        valueSuffix: ""
                    }
                },
                {
                    id: "deviation",
                    name: "Deviation",
                    type: "spline",
                    color: "#9068BE",
                    yAxis: 3,
                    data: [],
                    marker: {
                        enabled: false
                    },
                    tooltip: {
                        valueSuffix: ""
                    }
                }
            ]
        });
        _hchart.showLoading();
        window.__refreshChart__(_hchart);
    });
</script>
</body>
</html>
