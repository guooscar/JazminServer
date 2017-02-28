/*
 * jazmin boot script template.variable '$' or 'jazmin' support function to control
 * jazmin server. 
 */
$.setLogLevel('ALL');
$.setLogFile('./log/'+$.getServerName()+".log",true);
//$.disableConsoleLog();
//
$.addServer(new MessageServer());
$.addServer(new RpcServer());
$.addServer(new ConsoleServer());
$.addServer(new WebServer());