benchmark.start(function(){	
	benchmark.log(" start");
	rpc.connect("uat.itit.io",8601,'90')
});
//
benchmark.end(function(){
	benchmark.log(" end");
});
//
benchmark.loop(function(count){
	benchmark.log(" loop "+count);
	account=rpc.invoke("ZjhAction.loginByPassword",new Array(90,'57c23484a8b8991e8eb05371cb39792d','127.0.0.1','robot'))
	if(account!=null){
	    benchmark.log(" id "+account.id+",nickname:"+account.nickname);
	}
});