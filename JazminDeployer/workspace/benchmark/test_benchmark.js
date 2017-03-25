benchmark.start(function(){	
	benchmark.log(" start");
});
//
benchmark.end(function(){
	benchmark.log(" end");
});
//
benchmark.loop(function(count){
	benchmark.log(" loop "+count);
	http.get("http://www.baidu.com");
	http.get("http://www.sina.com.cn");
	http.get("http://cloudchen.logdown.com/posts/247932/apache-jmeter-tool-for-load-test-and-measure-performance");
});