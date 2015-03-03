<img src='https://github.com/guooscar/JazminServer/blob/master/JazminServer/src/jazmin/core/jazmin-logo.png?raw=true' width=250 height=220/>
# JazminServer
JazminServer is a Java based application/message/rpc server.
# Main features
* Core
  * Log 
  * AopDispatcher
  * JobScheduler
  * ApplicationLoader
  * BootLoader
  * JobScheduler
  * TaskScheduler
* Drivers
  * HttpDriver
  * JDBCDriver
  * LuceneDriver
  * MemcachedDriver
  * RPCDriver
* RPCServer
  * Server push message to client
  * Client proxy 
* MessageServer
  * Session management
  * Asyc service
  * Continuation service
  * Oneway service
  * Invoke frequency restrict
  * AMF/json/zjson message format
* WebServer
  * Jetty based webserver
  * Simple MVC framework
* ConsoleServer
  * SSH based monitor server
  * Piped command
  * REPL env
  
# Demo
Start a rpc server and register remote server
<pre>
   Jazmin.addServer(new ConsoleServer());
   RPCServer rpcServer=new RPCServer();
   rpcServer.registerService(new TestRemoteServiceImpl());
   Jazmin.addServer(rpcServer);
   Jazmin.start();
</pre>
