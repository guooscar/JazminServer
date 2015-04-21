package  {
	
	import flash.display.MovieClip;
	import flash.events.AsyncErrorEvent;
	import flash.events.NetStatusEvent;
	import flash.media.Video;
	import flash.net.NetConnection;
	import flash.net.NetStream;
	//
	public class MediaPlayer extends MovieClip {
		public var netStreamObj:NetStream;
		public var nc:NetConnection;
		public var vid:Video;
		
		public var streamID:String;
		public var videoURL:String;
		public var metaListener:Object;
		
		public function MediaPlayer () {
			NetConnection.defaultObjectEncoding = flash.net.ObjectEncoding.AMF0;
			initRTMP(); 
		}
		//
		private function initRTMP():void{
			streamID  = "test1";
			videoURL = "rtmp://localhost/live/";
			
			vid = new Video(); //typo! was "vid = new video();"
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
			// Resize video instance.
			vid.width = item.width;
			vid.height = item.height;
			// Center video instance on Stage.
			vid.x = (stage.stageWidth - vid.width) / 2;
			vid.y = (stage.stageHeight - vid.height) / 2;
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
				//intervalID = setInterval(playback, 1000);
			}
		}
		
		public function asyncErrorHandler(event:AsyncErrorEvent):void {
			trace("asyncErrorHandler.." + "\r"); 
		}
	} //end class
} //end package