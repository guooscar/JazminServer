<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<!doctype html>
<html>
<head>
    <title>noVNC</title>
    <meta charset="utf-8">
    <!-- Always force latest IE rendering engine (even in intranet) & Chrome Frame
                Remove this if you use the .htaccess -->
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">

    <!-- Apple iOS Safari settings -->
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <meta name="apple-mobile-web-app-status-bar-style" content="black-translucent" />
    <link rel="apple-touch-icon-precomposed" href="app/images/screen_57x57.png" />
    <!-- Stylesheets -->
    <link rel="stylesheet" href="/noVNC/app/styles/auto.css">
     <!--
    <script type='text/javascript'
        src='http://getfirebug.com/releases/lite/1.2/firebug-lite-compressed.js'></script>
    -->
        <script src="/noVNC/core/util.js"></script>
        <script src="/noVNC/app/webutil.js"></script>
</head>

<body style="margin: 0px;">
    <div id="noVNC_container">
            <div id="noVNC_status_bar" class="noVNC_status_bar" style="margin-top: 0px;">
                <table border=0 width="100%"><tr>
                    <td><div id="noVNC_status" style="position: relative; height: auto;">
                        Loading
                    </div></td>
                    <td width="1%"><div id="noVNC_buttons">
                        <input type=button value="Send CtrlAltDel"
                            id="sendCtrlAltDelButton">
                        <span id="noVNC_xvp_buttons">
                        <input type=button value="Shutdown"
                            id="xvpShutdownButton">
                        <input type=button value="Reboot"
                            id="xvpRebootButton">
                        <input type=button value="Reset"
                            id="xvpResetButton">
                        </span>
                            </div></td>
                </tr></table>
            </div>
            <canvas id="noVNC_canvas" width="640px" height="20px">
                Canvas not supported.
            </canvas>
        </div>

        <script>
        /*jslint white: false */
        /*global window, $, Util, RFB, */
        "use strict";

        // Load supporting scripts
        WebUtil.load_scripts({
            '/noVNC/core': ["base64.js", "websock.js", "des.js", "input/keysymdef.js",
                     "input/xtscancodes.js", "input/util.js", "input/devices.js",
                     "display.js", "inflator.js", "rfb.js", "input/keysym.js"]});

        var rfb;
        var resizeTimeout;
        var desktopName;


        function UIresize() {
            if (WebUtil.getConfigVar('resize', false)) {
                var innerW = window.innerWidth;
                var innerH = window.innerHeight;
                var controlbarH = document.getElementById('noVNC_status_bar').offsetHeight;
                if (innerW !== undefined && innerH !== undefined)
                    rfb.requestDesktopSize(innerW, innerH - controlbarH);
            }
        }
        function FBUComplete(rfb, fbu) {
            UIresize();
            rfb.set_onFBUComplete(function() { });
        }
        function updateDesktopName(rfb, name) {
            desktopName = name;
        }
        function passwordRequired(rfb, msg) {
            if (typeof msg === 'undefined') {
                msg = 'Password Required: ';
            }
            var html;
            html = '<form onsubmit="return setPassword();"';
            html += '  style="margin-bottom: 0px">';
            html += '<label></label>'
            html += '<input type=password size=10 id="password_input" class="noVNC_status">';
            html += '<\/form>';

            // bypass status() because it sets text content
            document.getElementById('noVNC_status_bar').setAttribute("class", "noVNC_status_warn");
            document.getElementById('noVNC_status').innerHTML = html;
            document.getElementById('noVNC_status').querySelector('label').textContent = msg;
        }
        function setPassword() {
            rfb.sendPassword(document.getElementById('password_input').value);
            return false;
        }
        function sendCtrlAltDel() {
            rfb.sendCtrlAltDel();
            return false;
        }
        function xvpShutdown() {
            rfb.xvpShutdown();
            return false;
        }
        function xvpReboot() {
            rfb.xvpReboot();
            return false;
        }
        function xvpReset() {
            rfb.xvpReset();
            return false;
        }
        function status(text, level) {
            switch (level) {
                case 'normal':
                case 'warn':
                case 'error':
                    break;
                default:
                    level = "warn";
            }
            document.getElementById('noVNC_status_bar').setAttribute("class", "noVNC_status_" + level);
            document.getElementById('noVNC_status').textContent = text;
        }
        function updateState(rfb, state, oldstate) {
            var cad = document.getElementById('sendCtrlAltDelButton');
            switch (state) {
                case 'connecting':
                    status("Connecting", "normal");
                    break;
                case 'connected':
                    if (rfb && rfb.get_encrypt()) {
                        status("Connected (encrypted) to " +
                               desktopName, "normal");
                    } else {
                        status("Connected (unencrypted) to " +
                               desktopName, "normal");
                    }
                    break;
                case 'disconnecting':
                    status("Disconnecting", "normal");
                    break;
                case 'disconnected':
                    status("Disconnected", "normal");
                    break;
                default:
                    status(state, "warn");
                    break;
            }

            if (state === 'connected') {
                cad.disabled = false;
            } else {
                cad.disabled = true;
                xvpInit(0);
            }

        }
        function disconnected(rfb, reason) {
            if (typeof(reason) !== 'undefined') {
                status(reason, "error");
            }
        }
        function notification(rfb, msg, level, options) {
            status(msg, level);
        }

        window.onresize = function () {
            // When the window has been resized, wait until the size remains
            // the same for 0.5 seconds before sending the request for changing
            // the resolution of the session
            clearTimeout(resizeTimeout);
            resizeTimeout = setTimeout(function(){
                UIresize();
            }, 500);
        };

        function xvpInit(ver) {
            var xvpbuttons;
            xvpbuttons = document.getElementById('noVNC_xvp_buttons');
            if (ver >= 1) {
                xvpbuttons.style.display = 'inline';
            } else {
                xvpbuttons.style.display = 'none';
            }
        }

        window.onscriptsload = function () {
            var host, port, password, path, token;

            document.getElementById('sendCtrlAltDelButton').style.display = "inline";
            document.getElementById('sendCtrlAltDelButton').onclick = sendCtrlAltDel;
            document.getElementById('xvpShutdownButton').onclick = xvpShutdown;
            document.getElementById('xvpRebootButton').onclick = xvpReboot;
            document.getElementById('xvpResetButton').onclick = xvpReset;

            WebUtil.init_logging(WebUtil.getConfigVar('logging', 'warn'));
            document.title = unescape(WebUtil.getConfigVar('title', 'noVNC'));
            // By default, use the host and port of server that served this file
            host = window.location.hostname;
            if (!port) {
                if (window.location.protocol.substring(0,5) == 'https') {
                    port = 9802;
                }else if (window.location.protocol.substring(0,4) == 'http') {
                    port = 9801;
                }
            }

            password =WebUtil.getConfigVar('password', null);
            path = WebUtil.getConfigVar('token', null);
          
            try {
                rfb = new RFB({'target':       document.getElementById('noVNC_canvas'),
                               'encrypt':      WebUtil.getConfigVar('encrypt',
                                        (window.location.protocol === "https:")),
                               'repeaterID':   WebUtil.getConfigVar('repeaterID', ''),
                               'true_color':   WebUtil.getConfigVar('true_color', true),
                               'local_cursor': WebUtil.getConfigVar('cursor', true),
                               'shared':       WebUtil.getConfigVar('shared', true),
                               'view_only':    WebUtil.getConfigVar('view_only', false),
                               'onNotification':  notification,
                               'onUpdateState':  updateState,
                               'onDisconnected': disconnected,
                               'onXvpInit':    xvpInit,
                               'onPasswordRequired':  passwordRequired,
                               'onFBUComplete': FBUComplete,
                               'onDesktopName': updateDesktopName});
            } catch (exc) {
                status('Unable to create RFB client -- ' + exc, 'error');
                return; // don't continue trying to connect
            }

            rfb.connect(host, port, password, path);
        };
        </script>

    </body>
</html>