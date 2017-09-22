public class MessageEncoder{
	public byte[] Encode(RequestMessage req){
			string json = ToJson (req.rps);
			byte[] body = GetBytes (json);
			if (SocketManager.payloadType == PAYLOAD_TYPE_GZJSON) {
				body = Compress (body);
			}
			byte[] serviceId = GetBytes(req.si);
			int capacity=12 + serviceId.Length+body.Length;
			ByteBuf buf=new ByteBuf(capacity);
			buf.AddInt32 (capacity);
			buf.AddInt16 (SocketManager.payloadType);
			buf.AddInt32 (req.ri);
			buf.AddInt16 ((short)req.si.Length);
			buf.AddBytes (serviceId);
			buf.AddBytes (body);
			
			return buf.Bytes();
		}
		//
		private byte[] Compress(byte[] data){
			MemoryStream ms = new MemoryStream();  
			GZipStream compressedzipStream = new GZipStream(ms, CompressionMode.Compress, true);  
			compressedzipStream.Write(data, 0, data.Length);  
			compressedzipStream.Close();  
			return ms.ToArray();
        }
}


public class MessageDecoder {
		//
		protected ByteBuf buf;
		//
		public MessageDecoder(){
			buf = new ByteBuf (1024*1024);//最大1M
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
		public IList<ResponseMessage> Decode (byte[] data,int dataLength){
			if (dataLength == 0) {
				return null;
			}
			//add to bytebuf
			buf.AddBytes (data, 0, dataLength);
			//
			IList<ResponseMessage> list = null;
			while (true) {
				ResponseMessage msg = Decode0 ();
				if (msg == null) {
					break;
				}
				if (list == null) {
					list = new List<ResponseMessage> ();
				}
				list.Add (msg);
			}
			return list;
		}
		//
		private ResponseMessage Decode0(){
			long startTime=CurrentTimeMillis();
			//
			buf.MarkReadIndex ();
			if (buf.GetAvailable() < 4) {
				return null;
			}
			int bodyLength=buf.ReadInt32();//4+bodyLength等于整个包的长度
			if (buf.GetAvailable() < bodyLength) {
				buf.ResetReadIndex ();
				return null;	
			}
			ResponseMessage rsp = new ResponseMessage ();
			rsp.payloadType=buf.ReadInt16();
			rsp.requestId=buf.ReadInt32();
			rsp.timestamp=buf.ReadInt64();
			rsp.statusCode=buf.ReadInt16();
			int statusMsgLength=buf.ReadInt16();
			int serviceIdLength=buf.ReadInt16();
			//
			//读取statusMsg
			byte[] statusMsg=buf.ReadBytes(statusMsgLength);
			//读取serviceId
			byte[] serviceId=buf.ReadBytes(serviceIdLength);
			//读取payload
			int payloadLength=bodyLength-20-statusMsgLength-serviceIdLength;
			byte[] payload=buf.ReadBytes(payloadLength);
			//
			if (rsp.payloadType == SocketManager.PAYLOAD_TYPE_GZJSON) {
				payload = Decompress (payload);
			}
			//
			rsp.serviceId = GetString(serviceId);
			rsp.statusMsg = GetString(statusMsg);
			rsp.payload = GetString(payload);
			//如果数据全部读完，则重置
			if (buf.GetAvailable () == 0) {
				buf.Reset ();
			}
			return rsp;
		}
		//
		public byte[] Decompress(byte[] zippedData)  {  
			MemoryStream ms = new MemoryStream(zippedData);  
			GZipStream compressedzipStream = new GZipStream(ms, CompressionMode.Decompress);  
			MemoryStream outBuffer = new MemoryStream();  
			byte[] block = new byte[1024];  
			while (true)  {  
				int bytesRead = compressedzipStream.Read(block, 0, block.Length);  
				if (bytesRead <= 0)  
					break;  
				else  
					outBuffer.Write(block, 0, bytesRead);  
			}  
			compressedzipStream.Close();  
			return outBuffer.ToArray();  
		}  
}