benchmark.start(function(){	
	benchmark.log(" start");
	msg.connect("host",8602)
	msg.invoke("XXXService.xxxxx",[])
});
//
benchmark.end(function(){
	benchmark.log(" end");
});
//
benchmark.loop(function(count){
	benchmark.log(" loop "+count);
    obj=msg.invoke("XXXService.xxxxx",null)
    if(obj!=null){
    	obj=JSON.parse(obj)
        benchmark.log(" id "+obj.id);
    }
});