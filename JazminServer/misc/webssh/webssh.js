(function() {
    var url ='ws://127.0.0.1:9001/ws';
    var protocols = ["webssh"];
    var login=function(ws){
        var loginData=JSON.stringify(
                            {
                                user: 'yama',
                                host: 'localhost',
                                port:22,
                                password:'77585211' ,
                               cmd:"tmux new -A -s webssh bash"
                            });
         ws.send("2" + loginData);
         //run tmux new -A -s webssh in your shell
    }
   
    var openWs = function() {
        var ws = new WebSocket(url, protocols);

        var term;

        ws.onopen = function(event) {
        	//
        	var elem=document.getElementById('infolabel');
        	elem.parentNode.removeChild(elem);
            //
            login(ws);
        	//
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
        	 term.io.writeUTF16(event.data);
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