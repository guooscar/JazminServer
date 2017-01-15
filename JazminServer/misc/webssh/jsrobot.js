/**
 * this is a demo script to test webssh server channel robot.
 * robot context reference jazmin/server/webssh/ScriptChannelContext.
 */

/**
 * when connection open send hello jazmin
 */
robot.open(function(){	
	robot.sendc("hello jazmin\r\n");
	robot.sendc("host"+robot.host()+"\r\n");
	robot.sendc("port"+robot.port()+"\r\n");
	robot.sendc("user"+robot.user()+"\r\n");
	robot.sendc("password"+robot.password()+"\r\n");
	robot.sendc("ticket"+robot.ticket()+"\r\n");
	robot.sends("1234567\r\n");
});
//
robot.close(function(){
	robot.log("channel closed");
});
//
robot.ticket(function(ticket){
	//robot.log("ticket:"+ticket);
});
//
robot.expect(".*",function(fullmsg){
	robot.log("match all:"+fullmsg);
});