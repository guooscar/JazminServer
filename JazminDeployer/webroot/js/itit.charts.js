/**
 * @auth: ginko.wang
 * @date: 2016-06-09 19:43
 */
!!(function (document, window, Chart) {

    /**
     * @param selector
     * @constructor
     */
    var ITChart = function (el) {
        this.context = el;
        this.__chart__ = undefined;
        this.__datasets__ = undefined;
        this.__init__();
    };
    /**
     * 初始化表格
     * @private
     */
    ITChart.prototype.__init__ = function () {
        Chart.defaults.global.hover.mode = 'single';
        var _this = this;
        _this.__chart__ = new Chart(_this.context, {
            type: 'line',
            data: {
                labels: [""],
                datasets: []
            },
            options: {
                responsive: false,
                hover: {
                    mode: 'label'
                },
                scales: {
                    yAxes: [{
                        ticks: {
                            beginAtZero: true
                        }
                    }]
                }
            }
        });
        this.__datasets__ = [];
    };

    /**
     * 设置图片的labels
     * @param labels
     */
    ITChart.prototype.labels = function (labels) {
        if (!Array.isArray(labels)) {
            return;
        }
        var _this = this;
        _this.__chart__.data.labels = labels;
    };

    /**
     * 追加数据展示源
     * @param dataset
     */
    ITChart.prototype.push = function (dataset) {
        if (typeof(dataset) !== "object") {
            return;
        }
        var _label = dataset.label;
        if (!_label) {
            return;
        }
        var _this = this;
        var _index = _this.__datasets__[_label];
        if (isNaN(_index)) {
            _this.__datasets__[_label] = _this.__chart__.data.datasets.length;
            _this.__chart__.data.datasets.push(dataset);
        } else {
            _this.__chart__.data.datasets[_index] = dataset;
        }

    };

    /**
     * 数据图标更新
     */
    ITChart.prototype.update = function () {
        var _this = this;
        _this.__chart__.update(1000, true);
    };


    /**
     * 导出ITChart 到window 变量域
     * @type {ITChart}
     */
    window.ITChart = ITChart;
})(document, window, Chart);