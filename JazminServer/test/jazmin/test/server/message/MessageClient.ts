class MessageDecoder {
		//
		public MessageDecoder(){
		}
		/**
		 * length    			4
		 * payload flag  		2  
		 * requestId 			4
		 * timestamp			8
		 * statusCode			2
		 * statusMsgLength		2 
		 * serviceIdLength      2
		 * statusMessage		?
		 * serviceId			?
		 * payload				? 
		 *
		*/
		//
		//
        public decode(data:egret.ByteArray,dataLength:number):ResponseMessage[]{
			//
			if (dataLength == 0) {
				return null;
			}
			//
            var  list:Array<ResponseMessage> = [];
			while (true) {
				var msg:ResponseMessage = this.Decode0(data);
				if (msg == null) {
					break;
				}
				list.push (msg);
			}
			return list;
		}
		//
		private  Decode0(buf:egret.ByteArray):ResponseMessage{
            var startTime:number=new Date().getMilliseconds();
			//
			var markReadPosition:number=buf.position;
			if (buf.bytesAvailable < 4) {
				return null;
			}
            var bodyLength:number=buf.readInt();//4+bodyLength等于整个包的长度
			if (buf.bytesAvailable < bodyLength) {
				buf.position=markReadPosition;
				return null;	
			}
			var rsp:ResponseMessage = new ResponseMessage ();
            rsp.payloadType=buf.readShort();
			rsp.requestId=buf.readInt();
            var timestamp:string=buf.readUTFBytes(8);
			rsp.statusCode=buf.readShort();
			var statusMsgLength:number=buf.readShort();
			var serviceIdLength:number=buf.readShort();
			//
			//读取statusMsg
			rsp.statusMsg=buf.readUTFBytes(statusMsgLength);
			//读取serviceId
			rsp.serviceId=buf.readUTFBytes(serviceIdLength);
			//读取payload
			var payloadLength:number=bodyLength-20-statusMsgLength-serviceIdLength;
			rsp.payload=buf.readUTFBytes(payloadLength);
			//
			//如果数据全部读完，则重置
			if (buf.bytesAvailable == 0) {
				buf.clear();
			}
			//
			var usingTime = new Date().getMilliseconds() - startTime;
		
			return rsp;
		}
}
//
class MessageEncoder{
    //
    public encode(req:RequestMessage):egret.ByteArray{
        var body:string = JSON.stringify (req.rps);
        var buf:egret.ByteArray = new egret.ByteArray();
        //
        var capacity=12 + req.si.length+body.length;
        buf.writeInt (capacity);
        buf.writeShort (SocketManager.payloadType);
        buf.writeInt (req.ri);
        buf.writeShort (req.si.length);
        buf.writeUTFBytes(req.si);
        buf.writeUTFBytes(body);
       
        return buf;
    }
}