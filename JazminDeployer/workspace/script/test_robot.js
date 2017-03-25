/**
 * this is a demo script to test webssh server channel robot.
 * robot context reference jazmin/server/webssh/ScriptChannelContext.
 */
//-----------------------------------------------------------
//print utils
var CLS = "\033[H\033[J\033[3J";	
var GOTO1_1 = "\033[1;1H";
var BOLD = "\033[1m";
var BLINK = "\033[5m";
var RESET = "\033[0m";
// #******************** ForGround Colors ***********************
var FBLACK = "\033[30m";
var FRED = "\033[31m";
var FGREEN = "\033[32m";
var FYELLOW = "\033[33m";
var FBLUE = "\033[34m";
var FMAGENTA = "\033[35m";
var FCYAN = "\033[36m";
var FWHITE = "\033[37m";
	// #******************** BackGround Colors ***********************
var BBLACK = "\033[40m";
var BRED = "\033[41m";
var BGREEN = "\033[42m";
var BYELLOW = "\033[43m";
var BBLUE = "\033[44m";
var BMAGENTA = "\033[45m";
var BCYAN = "\033[46m";
var BWHITE = "\033[47m";
//
function printInfo(msg){
    robot.sendc(BOLD);
    robot.sendc(FGREEN);
    robot.sendc(msg);
    robot.sendc(RESET);
}
function printError(msg){
    robot.sendc(BOLD);
    robot.sendc(FRED);
    robot.sendc(msg);
    robot.sendc(RESET);
}
function printDanger(msg){
    robot.sendc(BLINK);
    robot.sendc(BOLD);
    robot.sendc(BRED);
    robot.sendc(FWHITE);
    robot.sendc(msg);
    robot.sendc(RESET);
}
//-----------------------------------------------------------
//
robot.open(function(){	
	printWelcome();
	//checkCurrentUser();
	userConfirmRun();
});
//
robot.close(function(){
	robot.log("channel closed");
});
//
robot.ticket(function(ticket){
	
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
}
//
function checkCurrentUser(){
    if(robot.user()!='root'){
        printError('this robot must run as root user\r\n');
        robot.close();
    }
}
//
function userConfirmRun(){
    printDanger(
    "============================================\r\n"+
	"THIS ROBOT IS VERY DANGER.I WILL FORMAT ALL DATA FROM UR DISK\r\n"+
	"DO U REALLY WANT TO RUN ???????????\r\n"+
	"PLEASE TYPE echo yesido \r\n"+
	"============================================\r\n"
    );
    robot.enableInput(true);
    robot.expectClear();
    robot.expect(".*yesido$",function(msg){
        robot.enableInput(false);
        runStepPingBaidu();
    });
}
//
function runStepPingBaidu(){
    robot.sends("ping www.baidu.com\n");
    robot.setTimeout(5,function(){
       robot.sends("\003\n"); 
       runSshAppadmin();
    });
}

function runSshAppadmin(){
	robot.expectClear();
    robot.expect(".*password:$",function(msg){
        robot.sends("password\n");
    });
    robot.sends("ssh user@host\n");
}






