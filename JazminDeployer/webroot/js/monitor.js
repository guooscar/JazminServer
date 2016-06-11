/*!
 Licensed under the MIT license

 Copyright (c) 2016 ItIt.Io

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Any Problem , please contact <a href="mailto:yingosen@gmail.com">yingosen@gmail.com</a>

 */
/**
 * @auth: ginko.wang
 * @date: 2016-05-25 23:39
 */
!!(function(window, $) {
	var ITAjax = function() {
		this.baseRes = undefined;
		this.url = undefined;
		this.timeout = 0;
		this.type = undefined;
		this.resDataType = undefined;
		this.async = true;
		this.args = {};
		this.headers = {};
		this.codes = {};
		this.onproccess = undefined;
		this.code = undefined;
		this.onsuccess = undefined;
		this.onerror = undefined;
		this.oncomplete = undefined;
		this.__init__();
	}
	/**
	 * 全局配置项
	 * 
	 * @type {{defaults: {timeout: number, method: string, dataType: string,
	 *       async: boolean, params: {}, headers: {}, method: string, codes:
	 *       {1000: string, 100: string, 101: string, 102: string, 200: string,
	 *       201: string, 202: string, 203: string, 204: string, 205: string,
	 *       206: string, 302: string, 303: string, 304: string, 305: string,
	 *       306: string, 400: string, 401: string, 403: string, 404: string,
	 *       405: string, 406: string, 407: string, 408: string, 409: string,
	 *       410: string, 411: string, 412: string, 413: string, 414: string,
	 *       415: string, 421: string, 422: string, 423: string, 424: string,
	 *       426: string, 500: string, 501: string, 502: string, 503: string,
	 *       504: string, 505: string, 506: string, 507: string, 509: string,
	 *       510: string, 601: string, 602: string}, code:
	 *       _this.options.defaults.code, proccess:
	 *       _this.options.defaults.proccess, success:
	 *       _this.options.defaults.success, error:
	 *       _this.options.defaults.error, complete:
	 *       _this.options.defaults.complete}}}
	 */
	ITAjax.options = {
		defaults : {
			timeout : 300000,
			type : "POST",
			resDataType : "JSON",
			async : true,
			args : {},
			headers : {},
			types : {
				GET : "GET",
				POST : "POST"
			},
			codes : {

			}

		},
		callbacks : {
			code : function(codes, data) {
				return false;
			},
			proccess : function(proccess) {
			},
			success : function(data) {
			},
			error : function(xhr, textStatus, errorThrown, _url) {
				var _this = this;
				var data = {};
				var _status = xhr.status;
				_status = parseInt(_status);
				if (!isNaN(_status) && _status === 0) {
					_status = 1000;
				}
				data.code = _status;
				data.message = textStatus;
				var _codes = $.extend({}, ITAjax.options.defaults.codes);
				var checked = ITAjax.options.callbacks.code(_codes, data);
				if (checked) {
					return;
				}
				alert("error occur ：" + errorThrown + "\r\nurl : " + _url + "");
			},
			complete : function(xhr, textStatus, statusCode) {

			}
		}
	};

	/**
	 * 设定地址前缀
	 * 
	 * @param resBase
	 * @returns {ITAjax.resBase|*}
	 */
	ITAjax.prototype.resBase = function(baseRes) {
		var _this = this;
		if (!!baseRes) {
			_this.baseRes = baseRes;
			return;
		}
		if (!!_this.baseRes) {
			return _this.baseRes;
		}
		_this.baseRes = window.location.host;
		if (_this.baseRes.indexOf("http://") < 0) {
			_this.baseRes = "http://" + _this.baseRes;
		}
		return _this.baseRes;
	};

	/**
	 * 初始化
	 * 
	 * @private
	 */
	ITAjax.prototype.__init__ = function() {
		var _this = this;
		_this.baseRes = "";
		_this.url = undefined;
		_this.timeout = ITAjax.options.defaults.timeout;
		_this.type = ITAjax.options.defaults.type;
		_this.resDataType = ITAjax.options.defaults.resDataType;
		_this.async = ITAjax.options.defaults.async;
		_this.args = {};
		_this.headers = {};
		_this.codes = {};
		_this.onproccess = undefined;
		_this.oncode = undefined;
		_this.onsuccess = undefined;
		_this.onerror = undefined;
		_this.oncomplete = undefined;
	};

	/**
	 * 设定url
	 * 
	 * @param url
	 */
	ITAjax.prototype.action = function(url) {
		var _this = this;
		_this.url = url;
		return _this;
	};
	/**
	 * 设定查询参数
	 * 
	 * @param params
	 */
	ITAjax.prototype.params = function(args) {
		var _this = this;
		_this.args = $.extend(_this.args, args);
		return _this;
	};
	/**
	 * 设定请求方法
	 * 
	 * @param type
	 */
	ITAjax.prototype.method = function(type) {
		var _this = this;
		_this.type = type;
		return _this;
	};

	/**
	 * 设置返回数据类型
	 * 
	 * @param dataType
	 */
	ITAjax.prototype.dataType = function(resDataType) {
		var _this = this;
		_this.resDataType = resDataType;
		return _this;
	};
	/**
	 * 执行成功回调
	 * 
	 * @param callback
	 */
	ITAjax.prototype.success = function(callback) {
		var _this = this;
		_this.onsuccess = callback;
		return _this;
	};

	/**
	 * 调用报错回调
	 * 
	 * @param callback
	 */
	ITAjax.prototype.error = function(callback) {
		var _this = this;
		_this.onerror = callback;
		return _this;
	};

	/**
	 * 调用完成回调
	 * 
	 * @param callback
	 */
	ITAjax.prototype.complete = function(callback) {
		var _this = this;
		_this.oncomplete = callback;
		return _this;
	};

	/**
	 * 发起接口调用
	 */
	ITAjax.prototype.invoke = function() {
		var _this = this;
		var _url = _this.resBase() + _this.url;
		if (!_url) {
			return;
		}
		var _headers = $.extend({}, ITAjax.options.defaults.headers,
				_this.headers);
		var _timeout = _this.timeout;
		var _method = _this.type;
		var _dataType = _this.resDataType;
		var _params = $.extend({}, ITAjax.options.defaults.args, _this.args);
		var _async = _this.async;
		var _codes = $.extend({}, ITAjax.options.defaults.codes, _this.codes);
		var _proccess = _this.onproccess || ITAjax.options.callbacks.proccess
				|| function(_process) {
				};
		var _code = _this.oncode || ITAjax.options.callbacks.code;
		var _success = _this.onsuccess || ITAjax.options.callbacks.success;
		var _error = _this.onerror || ITAjax.options.callbacks.error;
		var _complete = _this.oncomplete || ITAjax.options.callbacks.complete;
		if (isNaN(parseInt(_timeout))) {
			_timeout = ITAjax.options.defaults.timeout;
		}
		$.ajax(_url, {
			type : _method,
			method : _method,
			headers : _headers,
			data : _params,
			beforeSend : function() {
				_proccess(10);
			},
			success : function(data) {
				_proccess(70);
				var checked = _code(_codes, data);
				if (checked) {
					return;
				}
				_proccess(80);
				if (typeof (_success) === "function") {
					_success.call(_this, data);
				}
				_proccess(90);
			},
			error : function(xhr, textStatus, errorThrown) {
				if (typeof (_error) === "function") {
					_error.call(_this, xhr, textStatus, errorThrown, _url);
				} else {
					itit.logger.error("error ", xhr);
				}
			},
			complete : function(xhr, textStatus, statusCode) {
				if (typeof (_complete) === "function") {
					_complete.call(_this, xhr, textStatus, statusCode);
				}
				_proccess(100);
				_this.__done__();
			},
			statusCode : {
				0 : function() {
				},
				200 : function() {
				},
				400 : function() {
				},
				403 : function() {
				},
				404 : function() {
				},
				408 : function() {
				},
				500 : function() {
				},
				502 : function() {
				},
				503 : function() {
				},
				504 : function() {
				}
			},
			timeout : _timeout,
			async : _async,
			dataType : _dataType
		});
		return _this;
	};

	/**
	 * 调用完成
	 * 
	 * @returns {ITAjax}
	 * @private
	 */
	ITAjax.prototype.__done__ = function() {
		var _this = this;
		_this.__init__();
		return _this;
	};

	/**
	 * 将局部变量绑定到全局
	 * 
	 * @type {ITAjax}
	 */
	window.ITAjax = ITAjax;

	/**
	 * 创建全局单例对象
	 * 
	 * @type {ITAjax}
	 */
	window.itAjax = new ITAjax();
})(window, jQuery);
/*
 * ! Licensed under the MIT license
 * 
 * Copyright (c) 2016 ItIt.Io
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * Any Problem , please contact <a
 * href="mailto:yingosen@gmail.com">yingosen@gmail.com</a>
 * 
 */
/**
 * 网络请求出发管理
 * 
 * @auth: ginko.wang
 * @date: 2016-05-25 23:39
 */
!!(function(window, $, itit) {
	var $codes = {};

	/**
	 * 请求返回错误统一处理类
	 * 
	 * @param codes
	 * @param data
	 * @returns {boolean}
	 */
	ITAjax.options.callbacks.code = function(codes, data) {
		var _code = -1;
		do {
			if (typeof (data) !== "object") {
				_code = 1000;
				break;
			}
			if (typeof (data.errorCode) !== "number") {
				_code = 1000;
				break;
			}
			_code = data.errorCode;
		} while (false);
		var _message = codes[_code];
		if (_code === -1) {
			_message = data.errorMessage;
		}
		if (_message !== 0 && !!!_message) {
			return false;
		}
		console.log(_message);
		return true;
	};

	/**
	 * 请求发送进度
	 * 
	 * @param precent
	 */
	ITAjax.options.callbacks.proccess = function(precent) {
		var _precent = parseInt(precent);
		if (isNaN(_precent)) {
			return;
		}
		var $container = $("#process-bar-container");
		var $processer = $("#process-bar");
		if (_precent > 100) {
			return;
		}
		if ($container.is(":hidden")) {
			$container.fadeIn(300);
		}
		if (isNaN(_precent)) {
			_precent = 0;
		}
		if (_precent >= 100) {
			$processer.width("100%");
			$container.delay(500).fadeOut(300, function() {
				$processer.width("0%");
			});
			return;
		}
		$processer.width(_precent + "%");
	};
})(window, jQuery, window.itit);
/**
 * @auth: ginko.wang
 * @date: 2016-06-09 19:43
 */
!!(function(document, window, Chart) {

	/**
	 * @param selector
	 * @constructor
	 */
	var ITChart = function(name, el) {
		this.name = name;
		this.context = el;
		this.__chart__ = undefined;
		this.__datasets__ = undefined;
		this.__init__();
	};
	/**
	 * 初始化表格
	 * 
	 * @private
	 */
	ITChart.prototype.__init__ = function() {
		Chart.defaults.global.hover.mode = 'single';
		Chart.defaults.global.animation = {
			duration : 0
		};
		Chart.defaults.global.elements.point = {
			pointStyle : "point"
		};
		var _this = this;
		_this.__chart__ = new Chart(_this.context, {
			type : 'line',
			data : {
				labels : [ "" ],
				datasets : []
			},
			animation : {
				duration : 0
			},
			options : {
				title : {
					display : true,
					text : _this.name,
				},
				responsive : false,
				scales : {
					xAxes : [ {
						type : 'time',
						time : {
							displayFormats : {
								minute : "HH:mm",
								hour : "HH:mm"
							}
						}
					} ],
					yAxes : [ {
						ticks : {
							beginAtZero : true
						}
					} ]
				}
			},
			scaleOverride : true,
			scaleSteps : 10,
			scaleStepWidth : 50,
			scaleStartValue : 0
		});
		this.__datasets__ = [];
	};

	/**
	 * 设置图片的labels
	 * 
	 * @param labels
	 */
	ITChart.prototype.labels = function(labels) {
		if (!Array.isArray(labels)) {
			return;
		}
		var _this = this;
		_this.__chart__.data.labels = labels;
	};

	/**
	 * 追加数据展示源
	 * 
	 * @param dataset
	 */
	ITChart.prototype.push = function(dataset) {
		if (typeof (dataset) !== "object") {
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
	ITChart.prototype.update = function() {
		var _this = this;
		_this.__chart__.update();
	};

	/**
	 * 导出ITChart 到window 变量域
	 * 
	 * @type {ITChart}
	 */
	window.ITChart = ITChart;
})(document, window, Chart);