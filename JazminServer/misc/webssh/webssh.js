(function() {
    var url ='ws://127.0.0.1:9001/ws';
    var protocols = ["webssh"];
    var openWs = function() {
        var ws = new WebSocket(url, protocols);

        var term;

        ws.onopen = function(event) {
        	hterm.defaultStorage = new lib.Storage.Local();
            hterm.defaultStorage.clear();

            term = new hterm.Terminal();
            term.io.showOverlay("Connection Opened", null);
            term.getPrefs().set("send-encoding", "raw");

            term.onTerminalReady = function() {
                var io = term.io.push();

                io.onVTKeystroke = function(str) {
                    ws.send("0" + str);
                };

                io.sendString = io.onVTKeystroke;

                io.onTerminalResize = function(columns, rows) {
                    ws.send("1"+columns+","+rows);
                };

                term.installKeyboard();
            };

            term.decorate(document.body);
        };

        ws.onmessage = function(event) {
        	data = event.data.slice(1);
            switch(event.data[0]) {
            case '0':
                term.io.writeUTF16(data);
                break;
            case '1':
                term.setWindowTitle(data);
                break;
            case '2':
                preferences = JSON.parse(data);
                Object.keys(preferences).forEach(function(key) {
                    term.getPrefs().set(key, preferences[key]);
                });
                break;
            }
        }

        ws.onclose = function(event) {
            if (term) {
                term.uninstallKeyboard();
                term.io.showOverlay("Connection Closed", null);
            }
        }

        ws.onerror = function(error) {
            term.io.showOverlay("Connection Error "+error, null);
            
        }
    }

    openWs();
})()