<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>Monitor</title>
<style>
.container-charts {
	
}

.instance-charts {
	float: left;
}
</style>
</head>
<body>
	<div class="container-charts">
		<c:forEach var="item" items="${list }">
			<canvas id="${item.instance }-${item.name }" class="instance-charts"
				data-instance="${item.instance }" data-name="${item.name }"
				data-type="${item.type }" data-id="${item.instance }-${item.name }"
				width="500" height="400"></canvas>
		</c:forEach>
		<i style="clear: both;"></i>
	</div>
	<canvas id="itchart" width="600" height="400"></canvas>
	<script type="text/javascript" src="/js/jquery.js"></script>
	<script type="text/javascript" src="/js/Chart.js"></script>
	<script type="text/javascript" src="/js/itit.common.js"></script>
	<script type="text/javascript" src="/js/itit.ajax.js"></script>
	<script type="text/javascript" src="/js/itit.net.js"></script>
	<script type="text/javascript" src="/js/itit.charts.js"></script>
	<script>
		$(function() {
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
			window.__init__ = function(el) {
				var $el = $(el);
				var _id = $el.data("id");
				var _instanceChart = instanceCharts[_id];
				if (!_instanceChart) {
					instanceCharts[_id] = new ITChart($el);
				}
				refresh(el);
			}
			window.refresh = function(el) {
				var $el = $(el);
				var _id = $el.data("id");
				var _instance = $el.data("instance");
				var _type = $el.data("type");
				var _name = $el.data("name");
				itAjax.action("/srv/monitor/interval").params({
					instance : _instance,
					name : _name,
					type : _type,
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
			$(".instance-charts").each(function(index, el) {
				__init__(el);
			});
			window.setInterval(function() {
				$(".instance-charts").each(function(index, el) {
					refresh(el);
				});
			}, 10000);
		});
		/* var itchart = new ITChart("#itchart");
		itchart.labels([ "A", "B", "C", "D", "E", "F", "G", "H", "I" ]);
		window.setInterval(function() {
			itchart.push({
				label : '# of Votes',
				backgroundColor : "rgba(255, 99, 132, 0.2)",
				borderColor : "rgba(255,99,132,1)",
				data : [ Math.random() * 20, Math.random() * 20,
						Math.random() * 20, Math.random() * 20,
						Math.random() * 20, Math.random() * 20,
						Math.random() * 20, Math.random() * 20,
						Math.random() * 20 ],
				borderWidth : 1
			});
			itchart.push({
				label : '# of Top',
				backgroundColor : "rgba(54, 162, 235, 0.2)",
				borderColor : "rgba(54, 162, 235, 1)",
				data : [ Math.random() * 20, Math.random() * 20,
						Math.random() * 20, Math.random() * 20,
						Math.random() * 20, Math.random() * 20,
						Math.random() * 20, Math.random() * 20,
						Math.random() * 20 ],
				borderWidth : 1
			});
			itchart.update();
		}, 5000); */
	</script>
</body>
</html>
