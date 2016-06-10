/*!
 Licensed under the MIT license

 Copyright (c) 2016 ItIt.Io

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Any Problem , please contact <a href="mailto:yingosen@gmail.com">yingosen@gmail.com</a>

 */
/**
 * 网络请求出发管理
 * @auth: ginko.wang
 * @date: 2016-05-25 23:39
 */
!!(function (window, $,itit) {
    var $codes = {};

    /**
     * 请求返回错误统一处理类
     * @param codes
     * @param data
     * @returns {boolean}
     */
    ITAjax.options.callbacks.code = function (codes, data) {
        var _code = -1;
        do {
            if (typeof(data) !== "object") {
                _code = 1000;
                break;
            }
            if (typeof(data.errorCode) !== "number") {
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
     * @param precent
     */
    ITAjax.options.callbacks.proccess = function (precent) {
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
            $container.delay(500).fadeOut(300, function () {
                $processer.width("0%");
            });
            return;
        }
        $processer.width(_precent + "%");
    };
})(window, jQuery, window.itit);