//-----------------------------------------------------------
function executeHandler(ctx,type){
    robot.sendc(type+" :"+ctx.getNode().id+" "+ctx.getNode().name+" "+ctx.getNode().type+"\r\n");
    //invoke function
    if(ctx.getNode().type=='task'&&type=='enter'){
        var functionName=ctx.getNode().name;
        ctx.signal();
    }
}
//
function exceptionHandler(ctx,error){
    robot.sendc(error.getMessage()+"\r\n");
}
//
var workflowInstance;
//
robot.open(function(){	
    workflow.startWorkflow("testworkflow",executeHandler,exceptionHandler);
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
robotScript.getUser=function (success){
	workflowInstance.setVariable('user',robot.user());
	success();
}
/*
 * list temp files
 */
robotScript.listFile=function(success){
	robot.expect(".*",function(msg){
		success();
	});
	robot.sends("ls /tmp \n");
}