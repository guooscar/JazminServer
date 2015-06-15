/*
 * jazmin boot script template.variable '$' or 'jazmin' support function to control
 * jazmin server. 
 */
$.setLogLevel('ALL');
$.setLogFile('./log/'+$.serverName()+".log",true);
//$.disableConsoleLog();
//
$.addServer(new MessageServer());
$.addServer(new RPCServer());
$.addServer(new ConsoleServer());
$.addServer(new WebServer());