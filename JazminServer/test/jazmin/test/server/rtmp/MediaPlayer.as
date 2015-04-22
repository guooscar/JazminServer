package  {
	
	import flash.display.MovieClip;
	import flash.events.AsyncErrorEvent;
	import flash.events.Event;
	import flash.events.KeyboardEvent;
	import flash.events.NetStatusEvent;
	import flash.media.Video;
	import flash.net.NetConnection;
	import flash.net.NetStream;
	import flash.text.TextField;
	import flash.text.TextFieldType;
	import flash.ui.Keyboard;

	[SWF(width = "640", height = "480", frameRate = "60"]
	public class MediaPlayer extends MovieClip {
		public var netStreamObj:NetStream;
		public var nc:NetConnection;
		public var vid:Video;
		
		public var streamID:String;
		public var videoURL:String;
		public var metaListener:Object;
		//
		private var addressTextField:TextField;
		private var videoTextField:TextField
		//
		public function MediaPlayer () {
			NetConnection.defaultObjectEncoding = flash.net.ObjectEncoding.AMF0;
			addEventListener(Event.ADDED_TO_STAGE,function(e:Event):void{
				setup();
			});
		}
		//
		private function setup():void{
			addressTextField=new TextField();
			addressTextField.width=200;
			addressTextField.htmlText="rtmp://localhost/live";
			addressTextField.type=TextFieldType.INPUT;
			addChild(addressTextField);
			addressTextField.addEventListener(KeyboardEvent.KEY_DOWN,function(e:KeyboardEvent):void{
				if(e.keyCode==Keyboard.ENTER){
					initRTMP();
				}
			});
			videoTextField=new TextField();
			videoTextField.htmlText="test";
			videoTextField.type=TextFieldType.INPUT;
			addChild(videoTextField);
			videoTextField.y=30;
		}
		//
		private function initRTMP():void{
			streamID  = videoTextField.text;
			videoURL = addressTextField.text;
			
			vid = new Video(); //typo! was "vid = new video();"
			vid.y=50;
			nc = new NetConnection();
			nc.addEventListener(NetStatusEvent.NET_STATUS, onConnectionStatus);
			nc.addEventListener(AsyncErrorEvent.ASYNC_ERROR, asyncErrorHandler);
			nc.connect(videoURL);     
			nc.client = {};
			nc.client.onMetaData = ns_onMetaData;
		}
		private function ns_onMetaData(item:Object):void {
			trace("metaData");
			for (var key:String in item) {
				trace(key + ": " + item[key]);
			}
		}
		
		private function ns_onCuePoint(item:Object):void {
			trace("cuePoint");
			trace(item.name + "\t" + item.time);
		}
		//
		private function onConnectionStatus(e:NetStatusEvent):void{
			trace(e.info.code);
			if (e.info.code == "NetConnection.Connect.Success"){
				trace("Creating NetStream");
				netStreamObj = new NetStream(nc);
				
				netStreamObj.client = {};
				netStreamObj.client.onMetaData = ns_onMetaData;
				netStreamObj.client.onCuePoint = ns_onCuePoint;
				//
				
				netStreamObj.play(streamID);
				vid.attachNetStream(netStreamObj);
				addChild(vid);
			}
		}
		
		public function asyncErrorHandler(event:AsyncErrorEvent):void {
			trace(event);
		}
		
		
	} //end class
} //end package