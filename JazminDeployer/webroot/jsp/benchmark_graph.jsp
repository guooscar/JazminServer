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

        .chart-js {
            position: relative;
            height: 100%;
            width: 100%;
            padding-bottom: 200px;
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

        .chart-js .item.average .value {
            color: #118DF0;
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
            <div class="item average">
                <span class="label">Average Value</span>:
                <span class="value">--</span>ms
            </div>
            <div class="item errorCount">
                <span class="label">Error Count</span>:
                <span class="value">--</span>
            </div>
            <div style="clear:both;"></div>
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
    window.__refresh__ = function (_chart) {
        itAjax.action("/srv/benchmark/data").params({
            id: "${benchmarkId}"
        }).success(function (result) {
            if (!result) {
                _chart.hideLoading();
                return;
            }
            if (result.errorCode != 0) {
                _chart.hideLoading();
                return;
            }
            window.setTimeout(function () {
                window.__refresh__(_chart);
            }, 1000);
            window.__renderChart__(_chart, result.all);
            window.__renderTotal__(result.total);
        }).invoke();
    };
    window.__renderChart__ = function (_chart, data) {
        if (!data || data.length === 0) {
            _chart.hideLoading();
            return;
        }
        var _len = data.length;
        var _throughtput = [];
        var _noOfSamples = [];
        var _noOfUsers = [];
        var _average = [];
        for (var i = 0; i < _len; i++) {
            var _temp = data[i];
            if (!_temp) {
                continue;
            }
            _throughtput.push([_temp.startTime, _temp.throughtput]);
            _noOfSamples.push([_temp.startTime, _temp.noOfSamples]);
            _noOfUsers.push([_temp.startTime, _temp.noOfUsers]);
            _average.push([_temp.startTime, _temp.average]);
        }
        _chart.hideLoading();
        _chart.update({
            series: [
                {
                    id: "throughtput",
                    data: _throughtput
                },
                {
                    id: "noOfSamples",
                    data: _noOfSamples
                },
                {
                    id: "noOfUsers",
                    data: _noOfUsers
                },
                {
                    id: "average",
                    data: _average
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
        try {
            $summary.find(".item.startTime .value").text(moment(total.startTime).format("YYYY-MM-DD HH:mm:ss.SSS"));
            $summary.find(".item.endTime .value").text(moment(total.endTime).format("YYYY-MM-DD HH:mm:ss.SSS"));
        } catch (e) {
            console.log(e);
        }
        $summary.find(".item.throughtput .value").text(total.throughtput);
        $summary.find(".item.noOfSamples .value").text(total.noOfSamples);
        $summary.find(".item.noOfUsers .value").text(total.noOfUsers);
        $summary.find(".item.average .value").text(total.average);
        $summary.find(".item.errorCount .value").text(total.errorCount);
    };
    $(function () {
//        (function () {
//            var $chart = $("#benchmark-chart");
//            var _chartWidth = $chart.width();
//            var _chartheight = $chart.height();
//            $("#benchmark-chart-view").css({
//                width: ( _chartWidth - 20) + "px",
//                height: (_chartheight - 20) + "px"
//            });
//        })();
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
                    min: new Date().getTime(),
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
                        text: "No Of Samples",
                        style: {
                            color: "#79A701"
                        }
                    },
                    labels: {
                        format: "{value}",
                        style: {
                            color: "#79A701"
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
                backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColor) || "#FFFFFF"
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
                    id: "noOfSamples",
                    name: "No Of Samples",
                    type: "spline",
                    color: "#79A701",
                    yAxis: 1,
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
                    yAxis: 2,
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
        window.__refresh__(_hchart);
    });
</script>
</body>
</html>
