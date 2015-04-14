<img src='https://github.com/guooscar/JazminServer/blob/master/JazminServer/src/jazmin/core/jazmin-logo.png?raw=true' width=250 height=220/>
# JazminServer
JazminServer is a Java based application/message/rpc server.
#Docs
<a href="https://www.icloud.com/pages/AwBWCAESEBqRx3Y0b6K60cCDTjv_gqwaKhxr7jmKAHmWIASYPLkXtFgE-ZbrosvMH1797SkOjiYZs-YbVKRW_kPOCgMCUCAQEEIJbquw2jZjPQyheLWKR79lbbmGan_ldQTtuEKJkTIaWG#JazminServer介绍">JazminServer Introduction</a>
<a href="https://www.icloud.com/pages/AwBWCAESEM7dnqjoCeKVKTsZC3SHYXYaKgoIDSk8UwNlhvlhvDrG7j88UPJEZSXx5xLa_vSPAQpoRkfUIY8sjQ_aDQMCUCAQEEIEd4rlnWXJI2bZ5XSFYT7XJy6u8kL1-Grq37Pw_xRX9U#JazminServer使用指南">JazminServer Setup</a>
<a href="https://www.icloud.com/pages/AwBWCAESEB7ohnqZnWgZORAUX6zosYIaKtaoprUFLmyFJpWCLSg_4tpgbJSJa954o0ojwNUpK51MNBHdayhIRdx6LwMCUCAQEEILtgmcdb5kZH8dnqvzPt4wjU2Un3iVd4xKNAzlxykm09#中间件服务器设计与实现">JazminServer Arch</a>

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
Start a rpc server and register remote service
<pre>
   Jazmin.addServer(new ConsoleServer());
   RPCServer rpcServer=new RPCServer();
   rpcServer.registerService(new TestRemoteServiceImpl());
   Jazmin.addServer(rpcServer);
   Jazmin.start();
</pre>
