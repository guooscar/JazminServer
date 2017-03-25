benchmark.start(function(){	
	benchmark.log(" start");
	msg.connect("uat.itit.io",8602)
	msg.invoke("ZjhService.loginByPassword",new Array(30,'57c23484a8b8991e8eb05371cb39792d','127.0.0.1'))
});
//
benchmark.end(function(){
	benchmark.log(" end");
});
//
benchmark.loop(function(count){
	benchmark.log(" loop "+count);
    gameTableInfo=msg.invoke("ZjhService.getTableInfo",null)
    if(gameTableInfo!=null){
        gameTableInfo=JSON.parse(gameTableInfo)
        benchmark.log(" id "+gameTableInfo.id+",roomId:"+gameTableInfo.roomId);
    }
});