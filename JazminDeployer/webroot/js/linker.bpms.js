/**
 * @name Logger
 * @author GinKo.Wang
 * @mail <a href='mailTo:yingosen@gmaiil.com'>GinKo.Wang</a>
 * @date 2016-01-04 20:57
 * 
 */
!!(function (window) {

    /**
	 * 日志打印
	 * 
	 * @name Logger
	 * @author GinKo.Wang
	 * @mail <a href='mailTo:yingosen@gmaiil.com'>GinKo.Wang</a>
	 * @date 2015-11-10 17:22
	 * 
	 */
    var Logger = function (name, level, enable) {
        this.__name__ = name;
        this.__logLevel__ = isNaN(parseInt(level)) ? Logger.Level.ALL : level;
        this.__systemSupport__ = true;
        this.__available__ = !!enable ? enable === true : true;
        this.__init__();
    };

    /**
	 * 定义类版本
	 * 
	 * @type {string}
	 */
    Logger.prototype.version = "1.0.2.1";

    /**
	 * 初始化
	 * 
	 * @private
	 */
    Logger.prototype.__init__ = function () {
        this.__systemSupport__ = !!(window.console);
        if (!this.__systemSupport__) {
            window.alert("Browser unsupport logger , logger will not run.");
        }
    }

    /**
	 * 日志是否可以使用
	 * 
	 * @private
	 */
    Logger.prototype.__isAvailable__ = function () {
        return this.__systemSupport__ && this.__available__;
    };


    /**
	 * 日志输出级别
	 */
    Logger.Level = {
        ALL: 0,
        DEBUG: 1,
        INFO: 2,
        WARN: 3,
        ERROR: 4
    };

    /**
	 * 更新日志级别
	 * 
	 * @param level
	 */
    Logger.prototype.level = function (level) {
        this.__logLevel__ = isNaN(parseInt(level)) ? Logger.Level.ALL : level;
    };

    /**
	 * 输出当前时间
	 * 
	 * @returns {string}
	 * @private
	 */
    Logger.prototype.__time__ = function () {
        var now = new Date();
        var year = now.getFullYear();
        var month = now.getMonth() + 1;
        var day = now.getDate();
        var hour = now.getHours();
        var minutes = now.getMinutes();
        var seconds = now.getSeconds();
        var milliseconds = now.getMilliseconds();
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
        if (month < 10) {
            format.push("0");
            format.push(day);
        } else {
            format.push(day);
        }
        format.push(" ");
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
        if (seconds < 10) {
            format.push("0");
            format.push(seconds);
        } else {
            format.push(seconds);
        }
        format.push(",");
        if (milliseconds < 10) {
            format.push("00");
            format.push(milliseconds);
        } else if (milliseconds < 100) {
            format.push("0");
            format.push(milliseconds);
        } else {
            format.push(milliseconds);
        }
        return format.join("");
    };

    /**
	 * 格式化参数
	 * 
	 * @returns {Array}
	 * @private
	 */
    Logger.prototype.__formatMessage__ = function () {
        var array = Array.prototype.slice.call(arguments);
        var buffer = [];
        if (!Array.isArray(array) || !array[0]) {
            array.push(array);
            return buffer;
        }
        array = array[0];
        var len = array.length;
        for (var i = 0; i < len; i++) {
            buffer.push(array[i]);
        }
        return buffer;
    }
    /**
	 * 是否启用日志
	 * 
	 * @param enable
	 */
    Logger.prototype.enable = function (enable) {
        if (typeof(enable) !== "boolean") {
            return;
        }
        this.__available__ = enable;
    };
    /**
	 * debug输出日志
	 */
    Logger.prototype.debug = function () {
        if (!this.__isAvailable__()) {
            return;
        }
        if (this.__logLevel__ > Logger.Level.DEBUG) {
            return;
        }
        var format = this.__formatMessage__(arguments);
        var message = undefined;
        var object = undefined;
        if (Array.isArray(format)) {
            message = format[0];
            object = format.slice(1, format.length);
        } else {
            message = format;
        }
        if (!message) {
            message = "";
        }
        window.console.debug("%c[" + this.__time__() + "] [ DEBUG ] [" + this.__name__ + "]", "font-weight: 500;color:#157E07;", message, object);
    };

    /**
	 * info输出日志
	 */
    Logger.prototype.info = function (message, args) {
        if (!this.__isAvailable__()) {
            return;
        }
        if (this.__logLevel__ > Logger.Level.INFO) {
            return;
        }
        var format = this.__formatMessage__(arguments);
        var message = undefined;
        var object = undefined;
        if (Array.isArray(format)) {
            message = format[0];
            object = format.slice(1, format.length);
        } else {
            message = format;
        }
        if (!message) {
            message = "";
        }
        window.console.info("%c[" + this.__time__() + "] [ INFO ] [" + this.__name__ + "]", "font-weight: 500;", message, object);
    };

    /**
	 * warn输出日志
	 */
    Logger.prototype.warn = function () {
        if (!this.__isAvailable__()) {
            return;
        }
        if (this.__logLevel__ > Logger.Level.WARN) {
            return;
        }
        var format = this.__formatMessage__(arguments);
        var message = undefined;
        var object = undefined;
        if (Array.isArray(format)) {
            message = format[0];
            object = format.slice(1, format.length);
        } else {
            message = format;
        }
        if (!message) {
            message = "";
        }
        window.console.warn("%c[" + this.__time__() + "] [ WARN ] [" + this.__name__ + "]", "font-weight: 500;color:#A7610C;", message, object);
    };

    /**
	 * error输出日志
	 */
    Logger.prototype.error = function (message, args) {
        if (!this.__isAvailable__()) {
            return;
        }
        if (this.__logLevel__ > Logger.Level.ERROR) {
            return;
        }
        var format = this.__formatMessage__(arguments);
        var message = undefined;
        var object = undefined;
        if (Array.isArray(format)) {
            message = format[0]
            object = format.slice(1, format.length);
        } else {
            message = format;
        }
        if (!message) {
            message = "";
        }
        window.console.error("%c[" + this.__time__() + "] [ ERROR ] [" + this.__name__ + "]", "font-weight: 500;", message, object);
    };

    /**
	 * 清除console输出的日志
	 */
    Logger.prototype.clear = function () {
        window.console.clear();
    };

    window.Logger = Logger;
})(window);
/*
 * ! Licensed under the MIT license
 * 
 * Copyright (c) 2016 bpms
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
 * @auth: ginko.wang
 * @date: 2016-05-25 23:39
 */
!!(function () {
    $.fn.extend({
        animateCss: function (animationName, callback) {
            this.addClass(animationName).on("webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend", function () {
                $(this).removeClass(animationName).off("webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend");
                if (typeof(callback) === "function") {
                    callback.apply(this, [animationName]);
                }
            });
            return this;
        }
    });
})();
!!(function (window, $) {
    window.parseIntWithDefault = function (value, vdefault) {
        vdefault = parseInt(vdefault);
        if (isNaN(vdefault)) {
            vdefault = 0;
        }
        value = parseInt(value);
        if (isNaN(value)) {
            value = vdefault;
        }
        return value;
    };
    window.parseFloatWithDefault = function (value, vdefault, toFixed) {
        vdefault = parseFloat(vdefault);
        if (isNaN(vdefault)) {
            vdefault = 0;
        }
        toFixed = parseInt(toFixed);
        if (isNaN(toFixed)) {
            toFixed = -1;
        }
        value = parseFloat(value);
        if (isNaN(value)) {
            value = vdefault;
        }
        if (toFixed > -1) {
            return parseFloat(value).toFixed(toFixed);
        }
        return value;
    };
    window.bpms = {
        version: "1.0.0",
        cached: {}
    };
    window.bpms.logger = new Logger("bpms.log");
    bpms.dialog = {
        alert: function (message) {
            if (!message) {
                return;
            }
            window.alert(message);
        },
        success: function (message) {
            if (!message) {
                return;
            }
            window.alert(message);
        },
        info: function (message) {
            if (!message) {
                return;
            }
            window.alert(message);
        },
        warn: function (message) {
            if (!message) {
                return;
            }
            window.alert(message);
        },
        error: function (message) {
            if (!message) {
                return;
            }
            window.alert(message);
        },
        confirm: function (message, callback) {
            message = message || "确定执行该操作吗?";
            var $dialog = $("#confirm-dialog");
            $dialog.find(".modal-body").html(message);
            $dialog.modal({
                backdrop: "static",
                keyboard: false
            });
            if (typeof(callback) !== "function") {
                return;
            }
            $dialog.off("confirmed.dialog.bpms").one("confirmed.dialog.bpms", function () {
                callback.apply(null);
            });
        },
        /**
		 * 
		 * @param message
		 * @param valueTip
		 * @param callback
		 * @param type
		 *            int float string
		 * @param required
		 *            必填
		 * @param min
		 * @param max
		 */
        prompt: function (message, valueTip, callback, type, required, min, max) {
            var $dialog = $("#prompt-dialog");
            $dialog.data("type", type || "string");
            $dialog.data("required", !!required);
            $dialog.data("min", min);
            $dialog.data("max", max);
            $dialog.find(".modal-body .bs-component.prompt-message").html(message);
            $dialog.find(".modal-body .bs-component.prompt-tip").html(valueTip);
            $dialog.modal({
                backdrop: "static",
                keyboard: false
            });
            if (typeof(callback) !== "function") {
                return;
            }
            $dialog.off("prompted.dialog.bpms").one("prompted.dialog.bpms", function (event, val) {
                callback.apply(null, [val]);
            });
        }
    }
})(window, jQuery);
// Add Extends for JS Object
(function () {
    if (!Array.isArray) {
        Array.isArray = function (arg) {
            return Object.prototype.toString.call(arg) === '[object Array]';
        };
    }
    Array.prototype.remove = function (index) {
        this.splice(index, 1);
    };
    if (!Array.prototype.forEach) {
        Array.prototype.forEach = function (callback) {
            var t, k;
            if (!this) {
                throw new TypeError('this is null or not defined');
            }
            var O = Object(this);
            var len = O.length >>> 0;
            if (typeof callback !== 'function') {
                throw new TypeError(callback + ' is not a function');
            }
            if (arguments.length > 1) {
                t = arguments[1];
            }
            k = 0;
            while (k < len) {
                var kValue;
                if (k in O) {
                    kValue = O[k];
                    var ret = callback.apply(t, [kValue, k, O]);
                    if (ret === true) {
                        break;
                    }
                }
                k++;
            }
        };
    }
})();
(function () {
    Date.prototype.format = function (fmt) { // author: meizz
        var o = {
            "M+": this.getMonth() + 1, // 月份
            "d+": this.getDate(), // 日
            "h+": this.getHours(), // 小时
            "m+": this.getMinutes(), // 分
            "s+": this.getSeconds(), // 秒
            "q+": Math.floor((this.getMonth() + 3) / 3), // 季度
            "S": this.getMilliseconds() // 毫秒
        };
        if (/(y+)/.test(fmt))
            fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "")
                .substr(4 - RegExp.$1.length));
        for (var k in o)
            if (new RegExp("(" + k + ")").test(fmt))
                fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k])
                    : (("00" + o[k]).substr(("" + o[k]).length)));
        return fmt;
    };
    Date.format = function (jsondate, fmt) {
        return new Date(jsondate).format(fmt);
    };
    /**
	 * 秒时间格式化
	 * 
	 * @param seconds
	 * @returns {string}
	 */
    Date.formatSecond = function (value) {
        var _diff = "-:-";
        if (typeof(value) !== "number") {
            return _diff;
        }
        var _val = [];
        var _second = value % 60;
        var _minute = parseInt(value / 60) % 60;
        var _hour = parseInt(value / 60 / 60) % 60;
        if (_hour > 0) {
            if (_hour < 10) {
                _val.push("0");
            }
            _val.push(_hour);
            _val.push(":");
        }
        if (_minute < 10) {
            _val.push("0");
        }
        _val.push(_minute);
        _val.push(":");
        if (_second < 10) {
            _val.push("0");
        }
        _val.push(_second);
        _diff = _val.join("");
        return _diff;
    };
    Date.dateDiff = function (jsonDate, now) {
        now = now || new Date();
        var date = new Date(parseInt(jsonDate, 10));
        var diffDay = Math.floor((now.getTime() - date.getTime()) / 1000);
        var _diff = Date.format(jsonDate, "yyyy-MM-dd hh:mm");
        if (diffDay < 60) {
            _diff = "一分钟前"
        } else if (diffDay < 5 * 60) {
            _diff = "五分钟前"
        } else if (diffDay < 10 * 60) {
            _diff = "十分钟前"
        } else if (diffDay < 20 * 60) {
            _diff = "二十分钟前"
        } else if (diffDay < 30 * 60) {
            _diff = "三十分钟前"
        } else if (diffDay < 60 * 60) {
            _diff = "一小时前"
        } else if (diffDay < 2 * 60 * 60) {
            _diff = "两小时前"
        } else if (diffDay < 3 * 60 * 60) {
            _diff = "三小时前"
        } else if (diffDay < 12 * 60 * 60) {
            _diff = "半天前"
        }
        return _diff;
    };
    String.isEmpty = function (value) {
        return !value || value.toString().trim().length === 0;
    };
    String.prototype.replaceAll = function (target, replacement) {
        return this.split(target).join(replacement);
    };
    String.prototype.trim = function () {
        return this.replace(/(^\s*)|(\s*$)/g, "");
    };
    String.prototype.ltrim = function () {
        return this.replace(/(^\s*)/g, "");
    };
    String.prototype.rtrim = function () {
        return this.replace(/(\s*$)/g, "");
    };
    String.prototype.startsWith = function (searchString, position) {
        position = position || 0;
        return this.indexOf(searchString, position) === position;
    };
    String.prototype.endWith = function (s) {
        if (!s || s.trim().length === 0 || s.length > this.length) {
            return false
        }
        return (this.substring(this.length - s.length) === s);
    };
})();

/*
 * ! Licensed under the MIT license
 * 
 * Copyright (c) 2016 bpms
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
 * @auth: ginko.wang
 * @date: 2016-05-25 23:39
 */
!!(function (window, $, bpms) {
    $("body").on("hidden.bs.modal", "#confirm-dialog", function () {
        var event = arguments[0] || window.event;
        event.preventDefault();
        $(this).find(".modal-body").html("");
    }).on("hidden.bs.modal", "#prompt-dialog", function () {
        var event = arguments[0] || window.event;
        event.preventDefault();
        var $dialog = $("#prompt-dialog");
        $dialog.data("type", "string");
        $dialog.data("required", false);
        $dialog.data("min", -1);
        $dialog.data("max", -1);
        $dialog.find(".v-error").remove();
        $dialog.find(".modal-body .bs-component.prompt-message").html("");
        $dialog.find(".modal-body .bs-component.prompt-tip").html("");
        $dialog.find(".modal-body input.form-control").val("");
    }).on("click", "#prompt-dialog .modal-footer .btn-confirm", function () {
        var event = arguments[0] || window.event;
        event.preventDefault();
        var $dialog = $("#prompt-dialog");
        $dialog.find(".v-error").remove();
        var _type = $dialog.data("type");
        var _required = $dialog.data("required");
        var _min = $dialog.data("min");
        var _max = $dialog.data("max");
        _type = _type || "string";
        _required = _required === "true";
        _min = window.parseFloatWithDefault(_min, -1);
        _max = window.parseFloatWithDefault(_max, -1);
        var $input = $dialog.find("input.form-control");
        var _oval = $dialog.find("input.form-control").val();
        var _val = undefined;
        if (_type === "int") {
            _val = window.parseInt(_oval);
            if (isNaN(_val) || _val != _oval) {
                $input.parent().append('<div class="v-error pt-5">请输入整数</div>');
                return;
            }
            if (_min > -1 && _val < _min) {
                $input.parent().append('<div class="v-error pt-5">最小值不能小于' + _min + '</div>');
                return;
            }
            if (_max > -1 && _val > _max) {
                $input.parent().append('<div class="v-error pt-5">最大值不能大于' + _max + '</div>');
                return;
            }
        } else if (_type === "float") {
            _val = window.parseFloat(_oval);
            if (isNaN(_val) || _val != _oval) {
                $input.parent().append('<div class="v-error pt-5">请输入数字</div>');
                return;
            }
            if (_min > -1 && _val < _min) {
                $input.parent().append('<div class="v-error pt-5">最小值不能小于' + _min + '</div>');
                return;
            }
            if (_max > -1 && _val > _max) {
                $input.parent().append('<div class="v-error pt-5">最大值不能大于' + _max + '</div>');
                return;
            }
        } else if (_type === "string") {
            _val = _oval;
            if (_required && String.isEmpty(_val)) {
                $input.parent().append('<div class="v-error pt-5">请填写内容</div>');
                return;
            }
            if (_min > -1 && _val.length < _min) {
                $input.parent().append('<div class="v-error pt-5">输入字符长度不能小于' + _min + '</div>');
                return;
            }
            if (_max > -1 && _val.length > _max) {
                $input.parent().append('<div class="v-error pt-5">输入字符长度不能大于' + _max + '</div>');
                return;
            }
        }
        $dialog.modal("hide").trigger("prompted.dialog.bpms", [_val]);
    }).on("click", "#confirm-dialog .modal-footer .btn-confirm", function () {
        var event = arguments[0] || window.event;
        event.preventDefault();
        var $dialog = $("#confirm-dialog");
        $dialog.modal("hide").trigger("confirmed.dialog.bpms");
    });

})(window, jQuery, window.bpms);
/*
 * ! Licensed under the MIT license
 * 
 * Copyright (c) 2016 bpms
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
        this.silentd = false;
        this.async = true;
        this.args = {};
        this.headers = {};
        this.codes = {};
        this.onprogress = undefined;
        this.onbeforeInvoke = undefined;
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
        defaults: {
            silentd: false,
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
            codes: {}

        },
        callbacks: {
            code: function (codes, data) {
                return false;
            },
            beforeInvoke: function () {
                return true;
            },
            progress: function (proccess) {
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
	 * 
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
	 * 
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
        _this.silentd = ITAjax.options.defaults.silentd;
        _this.args = {};
        _this.headers = {};
        _this.codes = {};
        _this.onprogress = undefined;
        _this.onbeforeInvoke = undefined;
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
    ITAjax.prototype.action = function (url) {
        var _this = this;
        _this.url = url;
        return _this;
    };
    /**
	 * 设定url
	 * 
	 * @param url
	 */
    ITAjax.prototype.reqHeaders = function (headers) {
        var _this = this;
        if (typeof(headers) !== "object") {
            return;
        }
        _this.headers = $.extend({}, _this.headers, headers);
        return _this;
    };

    /**
	 * 设定Ajax请求沉默状态
	 * 
	 * @param silentd
	 */
    ITAjax.prototype.silent = function (silentd) {
        var _this = this;
        if (silentd !== true && silentd !== false) {
            return;
        }
        _this.silentd = silentd;
        return _this;
    };

    /**
	 * 设定查询参数
	 * 
	 * @param args
	 */
    ITAjax.prototype.params = function (args) {
        var _this = this;
        _this.args = $.extend(_this.args, args);
        return _this;
    };

    /**
	 * 设定请求方法
	 * 
	 * @param type
	 */
    ITAjax.prototype.method = function (type) {
        var _this = this;
        _this.type = type;
        return _this;
    };

    /**
	 * 设置返回数据类型
	 * 
	 * @param resDataType
	 */
    ITAjax.prototype.dataType = function (resDataType) {
        var _this = this;
        _this.resDataType = resDataType;
        return _this;
    };
    /**
	 * 执行成功回调
	 * 
	 * @param callback
	 */
    ITAjax.prototype.success = function (callback) {
        var _this = this;
        _this.onsuccess = callback;
        return _this;
    };

    /**
	 * 调用报错回调
	 * 
	 * @param callback
	 */
    ITAjax.prototype.error = function (callback) {
        var _this = this;
        _this.onerror = callback;
        return _this;
    };

    /**
	 * 调用完成回调
	 * 
	 * @param callback
	 */
    ITAjax.prototype.complete = function (callback) {
        var _this = this;
        _this.oncomplete = callback;
        return _this;
    };

    /**
	 * 调用完成回调
	 * 
	 * @param callback
	 */
    ITAjax.prototype.progress = function (callback) {
        var _this = this;
        _this.onprogress = callback;
        return _this;
    };

    /**
	 * 调用完成回调
	 * 
	 * @param callback
	 */
    ITAjax.prototype.beforeInvoke = function (callback) {
        var _this = this;
        _this.onbeforeInvoke = callback;
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
        var _progress = _this.onprogress || ITAjax.options.callbacks.progress || function (_process) {
        };
        var _beforeInvoke = _this.onbeforeInvoke || ITAjax.options.callbacks.beforeInvoke || function () {
            return true;
        };
        var _code = _this.oncode || ITAjax.options.callbacks.code;
        var _success = _this.onsuccess || ITAjax.options.callbacks.success;
        var _error = _this.onerror || ITAjax.options.callbacks.error;
        var _complete = _this.oncomplete || ITAjax.options.callbacks.complete;
        if (isNaN(parseInt(_timeout))) {
            _timeout = ITAjax.options.defaults.timeout;
        }
        var _silent = _this.silentd;
        if (typeof (_beforeInvoke) === "function") {
            try {
                var ret = _beforeInvoke.call(_this, _params);
                if (ret !== true) {
                    return;
                }
            } catch (e) {
                bpms.logger.error("error ocur ", e);
            }
        }
        $.ajax(_url, {
            type: _method,
            method: _method,
            headers: _headers,
            data: _params,
            beforeSend: function () {
                if (_silent) {
                    return;
                }
                _progress(10);
            },
            success: function (data) {
                if (_silent === true) {
                    _progress(100);
                    return;
                }
                _progress(60);
                var checked = _code(_codes, data);
                if (checked) {
                    _progress(100);
                    return;
                }
                _progress(80);
                if (typeof (_success) === "function") {
                    try {
                        _success.call(_this, data);
                    } catch (e) {
                        bpms.logger.error("error ocur ", e);
                    }
                }
                _progress(100);
            },
            error: function (xhr, textStatus, errorThrown) {
                if (_silent === true) {
                    bpms.logger.error("error ", xhr);
                    _progress(100);
                    return;
                }
                if (typeof (_error) === "function") {
                    try {
                        _error.call(_this, xhr, textStatus, errorThrown, _url);
                    } catch (e) {
                        bpms.logger.error("error ocur ", e);
                    }
                } else {
                    bpms.logger.error("error ", xhr);
                }
                _progress(100);
            },
            complete: function (xhr, textStatus, statusCode) {
                if (_silent === true) {
                    _this.__done__();
                    return;
                }
                if (typeof (_complete) === "function") {
                    _complete.call(_this, xhr, textStatus, statusCode);
                }
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
	 * 
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
	 * 
	 * @type {ITAjax}
	 */
    window.ITAjax = ITAjax;

    window.itAjax = function () {
        return new ITAjax();
    };
})(window, jQuery);
/*
 * ! Licensed under the MIT license
 * 
 * Copyright (c) 2016 bpms
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
 * 
 * @author ginko.wang
 * @date 2017-08-31 20:54
 */
;(function (window, $) {
    var Transtion = function (name, to) {
        this.name = name;
        this.to = to;
    };
    var BNode = function (lknode, type, id, name, x, y) {
        var _this = this;
        _this.__init__();
        _this.id = id;
        _this.inId = id + "-in";
        _this.outId = id + "-out";
        _this.name = name;
        _this.type = type;
        _this.x = x;
        _this.y = y;
        _this.linkerNode = lknode;
        var _nodeConfig = $.extend({}, BNode.config.default, BNode.config[type]);
        if (_nodeConfig.noInput !== true) {
            _this.linkIn = _this.linkerNode.input(_this.inId, "In");
        }
        if (_nodeConfig.noOutput !== true) {
            _this.linkerOut = _this.linkerNode.output(_this.outId, "Out");
            _this.linkerOut.onConnect = function (input) {
                _this.connect(input);
            };
            _this.linkerOut.onRemove = function (input) {
                _this.disconnect(input);
            };
        }
        // when the node position change
        _this.linkerNode.onDrag = function (x, y) {
            _this.drag(x, y);
        };
        // when the node position change
        _this.linkerNode.onActive = function () {
        		if(!!bpm){
        			bpm.activeNode = _this;
        		}
            $("#node-id").val(_this.id);
            $("#node-name").val(_this.name);
            $("#node-type").val(_this.type);
            $("#node-execute").val(_this.execute);
            $("#node-taskid").val(_this.taskId);
        };
        // trigger when delete the node
        _this.linkerNode.onRemove = function () {
            console.log(this); // print the node object
            delete bpm.__nodes__[_this.linkerNode.id];
            //
            for(var key in bpm.__nodes__){
            		var _node = bpm.__nodes__[key];
            		delete _node.__transtions__[_this.linkerNode.id];
            }
            //
        };
    };
    BNode.prototype.__init__ = function () {
        var _this = this;
        _this.linkerNode = undefined;
        _this.id = undefined;
        _this.name = undefined;
        _this.__transtions__ = {};
        _this.execute = undefined;
        _this.taskId = undefined;
        _this.type = undefined;
        _this.x = 0;
        _this.y = 0;
    };

    BNode.prototype.connectTo = function (node) {
        if (typeof(node) !== "object") {
            return;
        }
        var _this = this;
        var _isExisted = _this.__transtions__[node.id];
        if (!!_isExisted) {
            return;
        }
        _this.linkerOut.connect(node.linkIn);
    };

    BNode.prototype.connect = function (input) {
        if (typeof(input) !== "object" || typeof(input.node) !== "object") {
            return;
        }
        var _this = this;
        _this.__transtions__[input.node.id] = ({
            name: input.node.name(),
            to: input.node.id
        });
    };
    BNode.prototype.disconnect = function (input) {
        if (typeof(input) !== "object" || typeof(input.node) !== "object") {
            return;
        }
        var _this = this;
        delete _this.__transtions__[input.node.id];
    };

    BNode.prototype.drag = function (x, y) {
        var _this = this;
        _this.x = x;
        _this.y = y;
    };

    BNode.prototype.refreshName = function (name) {
        if (typeof(name) !== "string") {
            return;
        }
        var _this = this;
        _this.linkerNode.name(name);
        _this.name = name;
    };

    BNode.prototype.data = function () {
        var _this = this;
        var _transtions = [];
        for (var key in _this.__transtions__) {
            _transtions.push(_this.__transtions__[key]);
        }
        return {
            id: _this.id,
            name: _this.name,
            enterThreshold: _this.enterThreshold,
            execute: _this.execute,
            transtions: _transtions,
            taskId: _this.taskId,
            type: _this.type,
            x: _this.x,
            y: _this.y
        };
    };

    BNode.Types = {
        /**
		 * 普通节点
		 */
        Normal: "normal",
        /**
		 * 开始
		 */
        Start: "start",
        /**
		 * 分支
		 */
        Fork: "fork",
        /**
		 * 合并
		 */
        Join: "join",
        /**
		 * 任务节点
		 */
        Task: "task",
        /**
		 * 选择节点
		 */
        Decision: "decision",
        /**
		 * 结束节点
		 */
        End: "end"
    };
    BNode.config = {
        lang: {
            type: {
                name: "类型",
                type: "static"
            },
            name: {
                name: "节点名称",
                type: "input"
            },
            execute: {
                name: "执行任务",
                type: "input"
            }
        },
        default: {
            type: "",
            execute: "",
            name: "",
            noInput: false,
            noOutput: false
        },
        start: {
            type: "start",
            name: "开始",
            noInput: true
        },
        fork: {
            type: "fork",
            name: "分支"
        },
        join: {
            type: "join",
            name: "合并"
        },
        task: {
            type: "task",
            execute: "",
            name: "执行任务"
        },
        decision: {
            type: "decision",
            execute: "",
            name: "选择"
        },
        end: {
            type: "end",
            name: "结束",
            noOutput: true
        }
    };
    window.BNode = BNode;
})
(window, jQuery);
/*
 * ! Licensed under the MIT license
 * 
 * Copyright (c) 2016 bpms
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
 * 
 * @author ginko.wang
 * @date 2017-08-31 22:47
 */
;(function (window, $) {
    var Bpm = function (linker, id, name) {
        this.__init__();
        this.__linker__ = linker;
        this.id = id;
        this.name = name;
    };
    Bpm.prototype.__init__ = function () {
        this.id = undefined;
        this.name = undefined;
        this.activeNode = undefined;
        this.__idCounter__ = 0;
        this.__nodes__ = {};
    };
    /**
	 * 生成id
	 * 
	 * @returns {string}
	 */
    Bpm.prototype.generateId = function () {
        return "bpm-node-" + (this.__idCounter__++);
    };
    /**
	 * 当前id
	 * 
	 * @returns {string}
	 */
    Bpm.prototype.currentId = function () {
        return "bpm-node-" + (this.__idCounter__);
    };
    
    Bpm.prototype.setIdCounter = function(counter){
    		this.__idCounter__ = counter;
    };
    
    /**
	 * 新增节点
	 * 
	 * @param name
	 * @param type
	 */
    Bpm.prototype.add = function (name, type) {
        var _this = this;
        var _id = _this.generateId();
        var _position = _this.__position__();
        return _this.addCustomer(_id, name, type, _position.x, _position.y);
    };
    /**
	 * 新增节点
	 * 
	 * @param id
	 * @param name
	 * @param type
	 * @param x
	 * @param y
	 */
    Bpm.prototype.addCustomer = function (id, name, type, x, y) {
        var _this = this;
        var _lnode = _this.__linker__.node({id: id, name: name, type: type, x: x, y: y});
        var bnode = new BNode(_lnode, type, id, name, x, y);
        _this.__nodes__[id] = bnode;
        return bnode;
    };
    /**
	 * 
	 * @param id
	 * @param name
	 * @param type
	 * @param x
	 * @param y
	 */
    Bpm.prototype.connect = function (fromId, toId) {
        var _this = this;
        var _from = _this.__nodes__[fromId];
        var _to = _this.__nodes__[toId];
        _from.connectTo(_to);
    };
    /**
	 * 移除节点
	 * 
	 * @param id
	 */
    Bpm.prototype.remove = function (id) {
        var _this = this;
        delete _this.__nodes__[id];
    };
    /**
	 * 计算位置
	 * 
	 * @returns {{x: number, y: number}}
	 * @private
	 */
    Bpm.prototype.__position__ = function () {
        var _this = this;
        var _nodes = [];
        for (var key in _this.__nodes__) {
            _nodes.push(_this.__nodes__[key].data());
        }
        var _len = _nodes.length;
        return {
            x: (_len % 8) * 200 + (50 * (_len % 8 + 1)),
            y: parseInt(_len / 8) * 100 + 60
        };
    };
    /**
	 * 获取bpm配置数据
	 * 
	 * @returns {{id: *, name: *, nodes: Array}}
	 */
    Bpm.prototype.data = function () {
        var _this = this;
        var _nodes = [];
        for (var key in _this.__nodes__) {
            _nodes.push(_this.__nodes__[key].data());
        }
        return {
            id: _this.id,
            name: _this.name,
            counter:_this.__idCounter__,
            nodes: _nodes
        };
    };
    window.Bpm = Bpm;
})(window, jQuery);