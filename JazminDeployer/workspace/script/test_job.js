/**
 * this is a demo script to test webssh server channel robot.
 * robot context reference jazmin/server/webssh/ScriptChannelContext.
 */
//-----------------------------------------------------------
//
function printInfo(msg){
    robot.sendc(msg);
}
//-----------------------------------------------------------
//
robot.open(function(){	
	printWelcome();
});
//
robot.close(function(){
	robot.log("channel closed");
});
//
robot.ticket(function(ticket){
	if(ticket>10){
		robot.close();
	}
});


//-----------------------------------------------------------
//
function printWelcome(){
    printInfo(
    "============================================\r\n"+
	"Welcome to use demo robot\r\n"+
	"host:"+robot.host()+"\r\n"+
	"port:"+robot.port()+"\r\n"+
	"user:"+robot.user()+"\r\n"+
	"============================================\r\n"
    );
    //
    var instances=deployer.getInstances();
    for(var i=0;i<instances.size();i++){
    	var instance=instances.get(i);
    	if(instance.machineId==deployer.getMachine().id){
    		printInfo("instance:"+instance.id);
    	}
    }
    //
    //runPing();
    robot.close();
}

function runPing(){
	robot.expectClear();
    robot.sends("ping www.baidu.com\n");
}