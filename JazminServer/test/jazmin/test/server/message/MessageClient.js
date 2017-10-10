	var requestId = 1;
    var connection = new WebSocket("ws://host:port/ws");
    connection.binaryType = 'arraybuffer';
    connection.onopen = function () {
    	//
    };
    connection.onerror = function (error) {
        console.log(error);
    };
    connection.onmessage = function (e) {
        var ret = decodeResponse(e.data);
    };

    function createRequest(ri, si, rps) {
        var body = JSON.stringify(rps);
        var capacity = 12 + si.length + body.length;
        var buffer = new ArrayBuffer(capacity);
        var dataView = new DataView(buffer);
        dataView.setInt32(0, capacity);
        dataView.setInt16(4, 1);
        dataView.setInt32(6, ri);
        dataView.setInt16(10, si.length);
        var i, n;
        for (i = 0, n = si.length; i < n; i++) {
            dataView.setUint8(i + 12, si.charCodeAt(i));
        }
        for (i = 0, n = body.length; i < n; i++) {
            dataView.setUint8(i + 12 + si.length, body.charCodeAt(i));
        }
        return buffer;

    }
    //
    function decodeResponse(buffer) {
        var dataView = new DataView(buffer);
        var ret = {};
        ret.bodyLength = dataView.getInt32(0);
        ret.payloadType = (dataView.getInt16(4));//payloadType
        ret.requestId = (dataView.getInt32(6));//requestId
        ret.time = (dataView.getFloat64(10));
        ret.statusCode = (dataView.getInt16(18));//statusCode
        ret.statusMsgLength = (dataView.getInt16(20));//statusMsgLength
        ret.serviceIdLength = (dataView.getInt16(22));//serviceIdLength
        ret.statusMessage = ab2str(dataView, 24, ret.statusMsgLength);
        ret.serviceId = ab2str(dataView, 24 + ret.statusMsgLength, ret.serviceIdLength);
        var payloadLength = ret.bodyLength - 20 - ret.statusMsgLength - ret.serviceIdLength;
        var body = ab2str(dataView, 24 + ret.statusMsgLength + ret.serviceIdLength, payloadLength);
        ret.body = JSON.parse(body);
        return ret;
    }
    //
    function ab2str(dataView, start, len) {
        var t = [];
        for (var i = 0; i < len; i++) {
            t.push(dataView.getUint8(start + i));
        }
        var arr = new Uint8Array(t);
        var enc = new TextDecoder();
        return enc.decode(arr);
    }