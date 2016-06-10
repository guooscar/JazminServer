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
!!(function (window, $) {
    var ITAjax = function () {
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
     * @type {{defaults: {timeout: number, method: string, dataType: string, async: boolean, params: {}, headers: {}, method: string, codes: {1000: string, 100: string, 101: string, 102: string, 200: string, 201: string, 202: string, 203: string, 204: string, 205: string, 206: string, 302: string, 303: string, 304: string, 305: string, 306: string, 400: string, 401: string, 403: string, 404: string, 405: string, 406: string, 407: string, 408: string, 409: string, 410: string, 411: string, 412: string, 413: string, 414: string, 415: string, 421: string, 422: string, 423: string, 424: string, 426: string, 500: string, 501: string, 502: string, 503: string, 504: string, 505: string, 506: string, 507: string, 509: string, 510: string, 601: string, 602: string}, code: _this.options.defaults.code, proccess: _this.options.defaults.proccess, success: _this.options.defaults.success, error: _this.options.defaults.error, complete: _this.options.defaults.complete}}}
     */
    ITAjax.options = {
        defaults: {
            timeout: 300000,
            type: "POST",
            resDataType: "JSON",
            async: true,
            args: {},
            headers: {},
            types: {
                GET: "GET",
                POST: "POST"
            },
            codes: {
                1000: "请求出现错误，请稍后重试",
                100: "客户端应当继续发送请求",
                101: "服务器已经接收到请求，正在处理",
                102: "处理将被继续执行",
                200: "请求被服务器处理成功.",
                201: "请求已经被服务器实现，正在创建处理.",
                202: "服务器已接受请求，但尚未处理",
                203: "服务器已成功处理了请求，但返回数据头异常",
                204: "服务器成功处理了请求，但不需要返回任何实体内容",
                205: "服务器成功处理了请求，且没有返回任何内容。",
                206: "服务器已经成功处理了部分GET请求",
                302: "请求的资源现在临时从不同的URI响应请求",
                303: "当前请求的响应可以在另一个URI上被找到",
                304: "请求的资源未发生更新",
                305: "被请求的资源必须通过指定的代理才能被访问",
                306: "请求的资源现在临时从不同的URI响应请求",
                400: "请求无法被服务器理解...",
                401: "请求需要用户验证...",
                403: "请求被限制...",
                404: "请求资源不存在...",
                405: "请求方法不能被用于请求相应的资源",
                406: "请求的资源的内容特性无法满足请求头中的条件，因而无法生成响应实体",
                407: "当前请求需要用户在代理服务器进行身份验证",
                408: "请求超时...",
                409: "被请求的资源的当前状态之间存在冲突，请求无法完成",
                410: "被请求的资源在服务器上已经不再可用，而且没有任何已知的转发地址",
                411: "服务器拒绝在没有定义Content-Length头的情况下接受请求",
                412: "服务器在验证在请求的头字段中给出先决条件时，没能满足其中的一个或多个",
                413: "服务器拒绝处理当前请求，因为该请求提交的实体数据大小超过了服务器愿意或者能够处理的范围",
                414: "请求的URI长度超过了服务器能够解释的长度，因此服务器拒绝对该请求提供服务",
                415: "对于当前请求的方法和所请求的资源，请求中提交的实体并不是服务器中所支持的格式，因此请求被拒绝",
                421: "从当前客户端所在的IP地址到服务器的连接数超过了服务器许可的最大范围",
                422: "请求格式正确，但是由于含有语义错误，无法响应",
                423: "当前资源被锁定",
                424: "客户端应当切换到TLS/1.0",
                426: "客户端应当切换",
                500: "服务器遇到未知错误...",
                501: "当服务器无法识别请求的方法，并且无法支持其对任何资源的请求",
                502: "服务器接收到无效的响应...",
                503: "服务器当前无法处理请求",
                504: "作为网关或者代理工作的服务器尝试执行请求时",
                505: "服务器不支持，或者拒绝支持在请求中使用的HTTP版本",
                506: "服务器存在内部配置错误",
                507: "服务器无法存储完成请求所必须的内容",
                509: "服务器达到带宽限制",
                510: "获取资源所需要的策略并没有被满足",
                601: "请求服务器处理失败，请稍后重试...",
                602: "请求返回数据格式异常..."
            }

        },
        callbacks: {
            code: function (codes, data) {
                return false;
            },
            proccess: function (proccess) {
            },
            success: function (data) {
            },
            error: function (xhr, textStatus, errorThrown, _url) {
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
                alert("发生错误：" + errorThrown + "\r\nurl : " + _url + "");
            },
            complete: function (xhr, textStatus, statusCode) {

            }
        }
    };

    /**
     * 设定地址前缀
     * @param resBase
     * @returns {ITAjax.resBase|*}
     */
    ITAjax.prototype.resBase = function (baseRes) {
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
     * @private
     */
    ITAjax.prototype.__init__ = function () {
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
     * @param url
     */
    ITAjax.prototype.action = function (url) {
        var _this = this;
        _this.url = url;
        return _this;
    };
    /**
     * 设定查询参数
     * @param params
     */
    ITAjax.prototype.params = function (args) {
        var _this = this;
        _this.args = $.extend(_this.args, args);
        return _this;
    };
    /**
     * 设定请求方法
     * @param type
     */
    ITAjax.prototype.method = function (type) {
        var _this = this;
        _this.type = type;
        return _this;
    };

    /**
     * 设置返回数据类型
     * @param dataType
     */
    ITAjax.prototype.dataType = function (resDataType) {
        var _this = this;
        _this.resDataType = resDataType;
        return _this;
    };
    /**
     * 执行成功回调
     * @param callback
     */
    ITAjax.prototype.success = function (callback) {
        var _this = this;
        _this.onsuccess = callback;
        return _this;
    };

    /**
     * 调用报错回调
     * @param callback
     */
    ITAjax.prototype.error = function (callback) {
        var _this = this;
        _this.onerror = callback;
        return _this;
    };

    /**
     * 调用完成回调
     * @param callback
     */
    ITAjax.prototype.complete = function (callback) {
        var _this = this;
        _this.oncomplete = callback;
        return _this;
    };

    /**
     * 发起接口调用
     */
    ITAjax.prototype.invoke = function () {
        var _this = this;
        var _url = _this.resBase() + _this.url;
        if (!_url) {
            return;
        }
        var _headers = $.extend({}, ITAjax.options.defaults.headers, _this.headers);
        var _timeout = _this.timeout;
        var _method = _this.type;
        var _dataType = _this.resDataType;
        var _params = $.extend({}, ITAjax.options.defaults.args, _this.args);
        var _async = _this.async;
        var _codes = $.extend({}, ITAjax.options.defaults.codes, _this.codes);
        var _proccess = _this.onproccess || ITAjax.options.callbacks.proccess || function (_process) {
            };
        var _code = _this.oncode || ITAjax.options.callbacks.code;
        var _success = _this.onsuccess || ITAjax.options.callbacks.success;
        var _error = _this.onerror || ITAjax.options.callbacks.error;
        var _complete = _this.oncomplete || ITAjax.options.callbacks.complete;
        if (isNaN(parseInt(_timeout))) {
            _timeout = ITAjax.options.defaults.timeout;
        }
        $.ajax(_url, {
            type: _method,
            method: _method,
            headers: _headers,
            data: _params,
            beforeSend: function () {
                _proccess(10);
            },
            success: function (data) {
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
            error: function (xhr, textStatus, errorThrown) {
                if (typeof (_error) === "function") {
                    _error.call(_this, xhr, textStatus, errorThrown, _url);
                } else {
                    itit.logger.error("error ", xhr);
                }
            },
            complete: function (xhr, textStatus, statusCode) {
                if (typeof (_complete) === "function") {
                    _complete.call(_this, xhr, textStatus, statusCode);
                }
                _proccess(100);
                _this.__done__();
            },
            statusCode: {
                0: function () {
                },
                200: function () {
                },
                400: function () {
                },
                403: function () {
                },
                404: function () {
                },
                408: function () {
                },
                500: function () {
                },
                502: function () {
                },
                503: function () {
                },
                504: function () {
                }
            },
            timeout: _timeout,
            async: _async,
            dataType: _dataType
        });
        return _this;
    };

    /**
     * 调用完成
     * @returns {ITAjax}
     * @private
     */
    ITAjax.prototype.__done__ = function () {
        var _this = this;
        _this.__init__();
        return _this;
    };

    /**
     * 将局部变量绑定到全局
     * @type {ITAjax}
     */
    window.ITAjax = ITAjax;

    /**
     * 创建全局单例对象
     * @type {ITAjax}
     */
    window.itAjax = new ITAjax();
})(window, jQuery);