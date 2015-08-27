<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<!doctype html>
<html>
  <head>
    <meta charset="utf-8"/>
    <title>WebSSH</title>
    <style>body {position: absolute; height: 100%; width: 100%; margin: 0px;}</style>
  </head>
  <body>
    <script src="/js/hterm.js"></script>
    <script>
(function() {
    var url ='ws://'+window.location.hostname+':9001/ws';
    var protocols = ["webssh"];
    var login=function(ws){
        var loginData=JSON.stringify(
                            {
                                user: '${sshUser}',
                                host: '${sshHost}',
                                port: ${sshPort},
                                password:'${sshPassword}' 
                            });
         ws.send("2" + loginData);
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
    </script>
    <div id="infolabel" style="font-size:30px;font-weight:700;margin: auto;width: 40%;border:1px solid #ccc; padding: 10px;text-align:center;margin-top:300px;">
    Connecting...
    </div>
  </body>
</html>