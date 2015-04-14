<img src='https://github.com/guooscar/JazminServer/blob/master/JazminServer/src/jazmin/core/jazmin-logo.png?raw=true' width=250 height=220/>
# JazminServer
 JazminServer是基于Java语言开发的统一中间件系统，她为开发者提供了统一的开发平台，帮助开发者快速开发基于Java的大型分布式应用。JazminServer可以被配置成Web服务器，RPC服务器，消息服务器，监控服务器等多种服务器，向客户端提供高效，稳定的服务。并且还集成了声明式事务，依赖注入，AOP日志，JOB/TASK调度等开发中常用的功能。JazminServer还提供了基于Web的自动化配置管理工具和自动化部署工具。
JazminServer适合被用作大型多人在游戏的服务器、物联网系统的后台服务器、大型网站系统的后台服务器等。
# 特性列表
* Core
	* Log 统一的全局日志系统
	* Dispatcher  统一的多线程调度器
	* JobScheduler 基于CRON表达式的Job调度器
	* ApplicationLoader 从自定义格式的二进制分发包加载插件
	* BootLoader 基于JavaScript的启动配置文件
	* TaskScheduler 定时任务调度器
* Drivers
	* HttpDriver 基于NIO的Http客户端，可用于大并发量的爬虫系统
	* JDBCDriver 声明式事务的JDBC连接池
	* LuceneDriver Lucene搜索引擎包装
	* MemcachedDriver Memcached包装
	* RPCDriver RPC客户端
* RPCServer
	* Request/Response RPC call  基于请求响应方式的RPC调用
	* Server push message to client 服务器主动推送消息到客户端
	* Client proxy 基于动态代理的客户端编程模型
	* Async Call 异步RPC调用
* MessageServer
	* Session management 会话管理
	* Asyc service 异步消息调用
	* Continuation service continuation消息
	* Oneway service 无响应消息
	* Invoke frequency restrict 调用频率限制
	* AMF/json/zjson message format 支持AMF、JSON、ZJSON等多种消息格式
* WebServer
	* Jetty based webserver  基于Jetty内核
	* Simple MVC framework 提供简单的MVC框架支持
* ConsoleServer
	* SSH based monitor server  基于SSH协议的监控控制台
	* Piped command 支持管道命令
	* REPL env 有完整的交互式的命令行环境
<p>
<img src='https://github.com/guooscar/JazminServer/blob/master/JazminServer/doc/images/image002.jpg?raw=true'/>
<span alian="center">架构图</span>
</p>
<img src='https://github.com/guooscar/JazminServer/blob/master/JazminServer/doc/images/image003.jpg?raw=true'/>
<img src='https://github.com/guooscar/JazminServer/blob/master/JazminServer/doc/images/image004.jpg?raw=true'/>
<img src='https://github.com/guooscar/JazminServer/blob/master/JazminServer/doc/images/image005.jpg?raw=true'/>
<img src='https://github.com/guooscar/JazminServer/blob/master/JazminServer/doc/images/image006.jpg?raw=true'/>
<img src='https://github.com/guooscar/JazminServer/blob/master/JazminServer/doc/images/image007.gif?raw=true'/>

