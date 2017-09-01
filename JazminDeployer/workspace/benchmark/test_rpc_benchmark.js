benchmark.start(function(){	
	benchmark.log(" start");
	rpc.connect("host",8601,'90')
});
//
benchmark.end(function(){
	benchmark.log(" end");
});
//
benchmark.loop(function(count){
	benchmark.log(" loop "+count);
	account=rpc.invoke("XXXXAction.xxxx",[])
	if(account!=null){
	    benchmark.log(" id "+account.id);
	}
});