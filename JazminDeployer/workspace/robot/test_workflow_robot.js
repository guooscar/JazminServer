//-----------------------------------------------------------
function executeHandler(ctx,type){
    robot.sendc(type+" :"+ctx.getNode().id+" "+ctx.getNode().name+" "+ctx.getNode().type+"\r\n");
    //invoke function
    if(ctx.getNode().type=='task'&&type=='enter'){
        var functionName=ctx.getNode().name;
        var fn=robotScript[functionName];
        var nodeId=ctx.getNode().id;
        if(fn){
        	fn(ctx,function(){
        		ctx.signal(nodeId);
        	});
        }else{
        	robot.sendc('can not find func:'+functionName+"\r\n");
        }
    }
    if(ctx.getNode().type=='end'){
        robot.close();
    }
}
//
function exceptionHandler(ctx,error){
    robot.sendc(error.getMessage()+"\r\n");
}
//
robot.open(function(){	
    workflow.startWorkflow("loginlsworkflow",executeHandler,exceptionHandler);
});

//
robot.close(function(){
	robot.log("channel closed");
});
//------------------------------------------------------------------------------
var robotScript={};
/*
 * get running user
 */
robotScript.getUser=function (ctx,success){
	ctx.setVariable('user',robot.user());
	success();
}
/*
 * list temp files
 */
robotScript.listFile=function(ctx,success){
	robot.expect(".*",function(msg){
	    robot.expectClear();
		success();
	});
	robot.sends("ls /tmp \n");
}