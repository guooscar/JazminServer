<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>Monitor</title>
<link rel="stylesheet" type="text/css" href="/css/sweetalert.css">
<style>
html, body {
	padding: 0;
	margin: 0;
}

body {
	font-family: 'Helvetica Neue', 'Helvetica', 'Arial', sans-serif;
	font-size: 14px;
	line-height: 150%;
}

.remove {
	display: inline-block;
	width: 24px;
	height: 24px;
	background:url("/image/close-alt.png") center no-repeat transparent;
	margin:5px 10px;
	text-align: center;
	font-size: 18px;
	-webkit-border-radius:50%;
    -moz-border-radius:50%;
    border-radius:50%;
    cursor: pointer;
}

.remove:hover {
	background-color: #C2B9BC;
}

.container-charts {
	
}

.instance-charts {
	float: left;
}

.date-container {
	position: fixed;
	height: 30px;
	line-height: 30px;
	width: 100%;
	background-color: #FFF;
	border-bottom: solid #EEE 1px;
	-webkit-box-shadow: 0px 5px 5px #888;
	-khtml-box-shadow: 0px 5px 5px #888;
	-moz-box-shadow: 0px 5px 5px #888;
	box-shadow: 0px 5px 5px #888;
	border-bottom: solid #EEE 1px;
	border-bottom: solid #EEE 1px;
}

.info-container {
	float: left;
	overflow: auto;
	border-radius: 5px;
	margin: 5px 0px;
	border: solid 1px #EEE;
	width: 33%;
	height: 450px;
}

.info-container .remove, .info-container .remove {
	float: right;
}

.basic-info {
	
}

.basic-info .title {
	text-align: center;
	font-weight: 900;
	color: #666;
}

.basic-info .infos {
	text-align: center;
}

.basic-info .infos table {
	width: 100%;
	height: 100%;
	border-collapse: collapse;
}

.basic-info .infos table tr {
	height: 30px;
}

.basic-info .infos table tr:nth-child(even) {
	background-color: #eee;
}

.basic-info .infos table td {
	border: 1px solid #DDD;
}

.basic-info .infos table .label {
	padding: 0px 10px;
}

.basic-info .infos table .content {
	color: #555;
	padding: 0px 10px;
}

.date-time-container {
	width: 700px;
	text-align: center;
	margin: 0 auto;
}

.date-time-container .item {
	float: left;
}

.date-time-container .item input[type="date"] {
	width: 140px;
}

.date-time-container .item input[type="time"] {
	width: 140px;
}

.date-time-container .item input[type="button"] {
	width: 80px;
	height: 25px;
	line-height: 25px;
	margin: 0px 5px;
}

.date-time-container .item input[type="checkbox"] {
	width: 20px;
}
</style>
</head>
<body>
	<div class="date-container">
		<div class="date-time-container">
			<div class="item">
				<input id="day" type="date" />
			</div>
			<div class="item">
				<input id="startTime" type="time" />
			</div>
			<div class="item">
				<input id="endTime" type="time" />
			</div>
			<div class="item">
				<input id="refreshBtn" type="button" value="Query" />
			</div>
			<div class="item">
				<input id="recentlyTime" type="checkbox" />Realtime
			</div>
			<div style="clear: both;"></div>
		</div>
	</div>
	<div style="height: 60px;"></div>
	<div>
		<c:forEach var="item" items="${list }" varStatus="step">
			<c:if test="${item.type == 'KeyValue' }">
				<div id="${item.instance }-${step.index }-table-container"
					data-instance="${item.instance }" 
					data-name="${item.name }"
					class="info-container">
					<div class="opt">
						<span class="remove"
							data-selector="#${item.instance }-${step.index }-table-container"></span>
					</div>
					<div id="${item.instance }-${item.name }" class="basic-info"
						data-instance="${item.instance }" 
						data-name="${item.name }"
						data-type="${item.type }"
						data-id="${item.instance }-${item.name }"
						>
						<div class="title">${item.name }</div>
						<div class="infos">loading...</div>
					</div>
				</div>
			</c:if>
		</c:forEach>
		<div style="clear: both;"></div>
	</div>
	<div style="height: 10px;"></div>
	<div class="container-charts">
		<c:forEach var="item" items="${list }" varStatus="step">
			<c:if test="${item.type != 'KeyValue' }">
				<div id="${item.instance }-${step.index }-chart-container"
					data-instance="${item.instance }"
					data-name="${item.name }"
					class="info-container">
					<div class="opt">
						<span class="remove"
							data-selector="#${item.instance }-${step.index }-chart-container"></span>
					</div>
					<canvas id="${item.instance }-${item.name }"
						class="instance-charts" 
						data-instance="${item.instance }"
						data-name="${item.name }" 
						data-type="${item.type }"
						data-id="${item.instance }-${item.name }"
						width="600" height="400"></canvas>
				</div>
			</c:if>
		</c:forEach>
		<div style="clear: both;"></div>
	</div>
	<script type="text/javascript" src="/js/jquery.js"></script>
	<script type="text/javascript" src="/js/sweetalert.min.js"></script>
	<script type="text/javascript" src="/js/moment.js"></script>
	<script type="text/javascript" src="/js/Chart.js"></script>
	<script type="text/javascript" src="/js/monitor.js"></script>
	<script>
		$(function() {
			function time(date) {
				var hour = date.getHours();
				var minutes = date.getMinutes();
				var second = date.getSeconds();
				var format = [];
				if (hour < 10) {
					format.push("0");
					format.push(hour);
				} else {
					format.push(hour);
				}
				format.push(":");
				if (minutes < 10) {
					format.push("0");
					format.push(minutes);
				} else {
					format.push(minutes);
				}
				format.push(":");
				if (second < 10) {
					format.push("0");
					format.push(second);
				} else {
					format.push(second);
				}
				return format.join("");
			}
			function day(date) {
				var year = date.getFullYear();
				var month = date.getMonth() + 1;
				var day = date.getDate();
				var format = [];
				format.push(year);
				format.push("-");
				if (month < 10) {
					format.push("0");
					format.push(month);
				} else {
					format.push(month);
				}
				format.push("-");
				if (day < 10) {
					format.push("0");
					format.push(day);
				} else {
					format.push(day);
				}
				return format.join("");
			}
			;
			var date = new Date();
			$("#day").val(day(date));
			$("#startTime")
					.val(time(new Date(date.getTime() - 1000 * 60 * 60)));
			$("#endTime").val(time(date));
			var instanceCharts = [];
			var colors = [];
			colors.push({
				bgc : "rgba(255, 99, 132, 0.2)",
				bc : "rgba(255,99,132,1)"
			});
			colors.push({
				bgc : "rgba(54, 162, 235, 0.2)",
				bc : "rgba(54, 162, 235,1)"
			});
			colors.push({
				bgc : "rgba(255, 206, 86, 0.2)",
				bc : "rgba(255, 206, 86,1)"
			});
			colors.push({
				bgc : "rgba(75, 192, 192, 0.2)",
				bc : "rgba(75, 192, 192,1)"
			});
			colors.push({
				bgc : "rgba(153, 102, 255, 0.2)",
				bc : "rgba(153, 102, 255,1)"
			});
			colors.push({
				bgc : "rgba(255, 159, 64, 0.2)",
				bc : "rgba(255, 159, 64,1)"
			});
			colors.push({
				bgc : "rgba(255, 204, 255, 0.2)",
				bc : "rgba(255, 204, 255,1)"
			});
			colors.push({
				bgc : "rgba(204, 204, 51, 0.2)",
				bc : "rgba(204, 204, 51,1)"
			});
			colors.push({
				bgc : "rgba(153, 204, 0, 0.2)",
				bc : "rgba(153, 204, 0,1)"
			});
			window.__initBasicInfo__ = function(el) {
				var $el = $(el);
				var _id = $el.data("id");
				var _name = $el.data("name");
				var _type = $el.data("type");
				__refreshBasicInfo__(el);
			};
			window.__initChart__ = function(el) {
				var $el = $(el);
				var _id = $el.data("id");
				var _name = $el.data("name");
				var _type = $el.data("type");
				var _instanceChart = instanceCharts[_id];
				if (!_instanceChart) {
					instanceCharts[_id] = new ITChart(_name, $el);
				}
				__refreshChart__(el);
			};
			window.__refreshBasicInfo__ = function(el) {
				var $el = $(el);
				var _id = $el.data("id");
				var _instance = $el.data("instance");
				var _type = $el.data("type");
				var _name = $el.data("name");
				itAjax.action("/srv/monitor/refresh-basicinfo").params({
					instance : _instance,
					name : _name,
					type : _type
				}).success(function(data) {
					var html = [];
					html.push('<div class="title">');
					html.push(_name);
					html.push('</div>');
					html.push('<div class="infos">');
					html.push('<table><tbody>');
					var info = data.info;
					if (typeof (info) === "object") {
						for ( var _property in info) {
							html.push('<tr>');
							html.push('<td class="label"');
							html.push(' title = "');
							html.push(_property);
							html.push('"');
							html.push('>');
							html.push(_property);
							html.push('</td>');
							html.push('<td class="content" ');
							html.push(' title = "');
							html.push(info[_property]);
							html.push('">');
							html.push(info[_property]);
							html.push('</td>');
							html.push('</tr>');
						}
					}
					html.push('</tbody></table>');
					$el.html(html.join(''));
				}).invoke();
			};
			window.__refreshChart__ = function(el) {
				var $el = $(el);
				var _id = $el.data("id");
				var _instance = $el.data("instance");
				var _type = $el.data("type");
				var _name = $el.data("name");
				var _day = $("#day").val();
				var _stime = $("#startTime").val();
				var _etime = $("#endTime").val();
				var _startTime = Date.parse(_day + " " + _stime);
				var _endTime = Date.parse(_day + " " + _etime);
				itAjax.action("/srv/monitor/refresh-chart").params({
					instance : _instance,
					name : _name,
					type : _type,
					startTime : _startTime,
					endTime : _endTime
				}).success(function(data) {
					var _instanceChart = instanceCharts[_id];
					_instanceChart.labels(data.labels);
					var _index = 0;
					for ( var _property in data.datasets) {
						var _color = colors[_index];
						_instanceChart.push({
							label : '# ' + _property,
							backgroundColor : _color.bgc,
							borderColor : _color.bc,
							data : data.datasets[_property],
							borderWidth : 1
						});
						_index++;
					}
					_instanceChart.update();
				}).invoke();
			};
			window.__instanceRefresh__ = function() {
				$(".basic-info").each(function(index, el) {
					__refreshBasicInfo__(el);
				});
				$(".instance-charts").each(function(index, el) {
					__refreshChart__(el);
				});
			};
			$(".basic-info").each(function(index, el) {
				__initBasicInfo__(el);
			});
			$(".instance-charts").each(function(index, el) {
				__initChart__(el);
			});
			var interval = window.setInterval(function() {
				var _day = $("#day").val();
				var _recently = !!$("#recentlyTime:checked")[0];
				var _now = new Date();
				if (_recently) {
					$("#endTime").val(time(_now));
					window.__instanceRefresh__();
				}
				var _etime = $("#endTime").val();
				var _endTime = Date.parse(_day + " " + _etime);
				var _nowTime = (new Date()).getTime();
				if (_endTime <= _nowTime) {
					return;
				}
				window.__instanceRefresh__();
			}, 10000);
			$("body").on("click", "#refreshBtn", function() {
				window.__instanceRefresh__();
			});
			$("body").on("click",".info-container .remove",function() {
				var _selector = $(this).data("selector");
				var $selector = $(_selector);
				if (!$selector[0]) {
					return;
				}
				var _name = $selector.data("name");
				swal({
					title: "Are you sure?",
					text: "You will remove the <span style='font-weidht:900;color:#DD6B55;'>" + _name + "</span> monitor !<br/> You can refresh the page to restore it .",   
					showCancelButton: true,   
					confirmButtonColor: "#DD6B55",   
					confirmButtonText: "Yes, remove it!",
					html: true,
					closeOnConfirm: true 
				}, function(){
					$selector.remove();
				});
			});
		});
	</script>
</body>
</html>
