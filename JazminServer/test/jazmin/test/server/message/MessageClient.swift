//
//  SocketManager.swift
//  YxwlIOSBuyerApp
//
//  Created by yama on 11/09/2017.
//  Copyright © 2017 yama. All rights reserved.
//

import Foundation


public class RequestMessage{
    public var timestamp:UInt64=0;
    public var requestId:UInt32=0;
    public var serviceId:String="";
    public var rps:[String]=[];
    public var callback:((ResponseMessage)->Void)?;
}
//
public class ResponseMessage{
    public static let STATUS_CODE_OK=0;
    public static let STATUS_CODE_TIMEOUT = -1;
    
    //
    public var requestId:UInt32=0;
    public var serviceId:String="";
    public var timestamp:UInt64=0;
    public var statusCode:Int16=0;
    public var statusMessage:String="";
    public var payload:Any?;
}
//
//
extension Data {
    func hexEncodedString() -> String {
        return map { String(format: "%02hhx", $0) }.joined()
    }
}
//
public class MessageClient: NSObject {
    var socket:Socket?;
    var requestId:Int=1;
    var bufferData:Data = Data();
    var isConnectedSocket:Bool=false;
    //
    var callbackMap = [Int:RequestMessage]();
    public var pushCallback:((ResponseMessage)->Void)? = nil;
    //
    override init(){
        super.init();
        socket=setup();
        startReceiveThread();
    }
    //
    func handleError(error:Error){
        guard let socketError = error as? Socket.Error else {
            print("Unexpected error...")
            return
        }
        //
        print("Error reported: \(socketError.description)")
        isConnectedSocket=false;
        socket?.close();
        print("close socket");
    }
    //
    public func setup()->Socket?{
        do{
            let s =  try Socket.create()
            try s.setReadTimeout(value: 1000*5);
            try s.setWriteTimeout(value: 1000*5);
            return s;
        }catch let error {
            handleError(error: error);
        }
        return nil;
    }
    //
    public func startReceiveThread(){
        let thread = Thread {
            self.readData();
        }
        thread.start();
    }
    //
    private func readData(){
        print("start Receive Thread");
        while(true){
            do{
                if(!isConnected()){
                    continue;
                }
                Thread.sleep(forTimeInterval:0.1);
                checkTimeout();
                var readData = Data()
                if let st  = socket {
                    if(st.isActive && st.isConnected ){
                        let bytesRead = try st.read(into: &readData)
                        if(bytesRead>0){
                            bufferData.append(readData);
                            newDataArrived();
                        }
                    }
                }
                
            }catch let error{
                print("readData error");
                handleError(error: error);
            }
        }
    }
    //
    
    //
    func dataToUInt32(data:Data,from:Int)->UInt32{
        let count = 4;
        let to = from+count;
        var t = [UInt8](repeating:0,count:count);
        bufferData.copyBytes(to: &t, from: Range(uncheckedBounds:(from,to)))
        //
        var value : UInt32 = 0
        let data = NSData(bytes: t, length: count);
        data.getBytes(&value, length: count);
        value = UInt32(bigEndian: value)
        return value;
    }
    //
    func dataToUInt16(data:Data,from:Int)->UInt16{
        let count = 2;
        let to = from+count;
        var t = [UInt8](repeating:0,count:count);
        bufferData.copyBytes(to: &t, from: Range(uncheckedBounds:(from,to)))
        //
        var value : UInt16 = 0
        let data = NSData(bytes: t, length: count);
        data.getBytes(&value, length: count);
        value = UInt16(bigEndian: value)
        return value;
    }
    //
    //
    func dataToUInt64(data:Data,from:Int)->UInt64{
        let count = 8;
        let to = from+count;
        var t = [UInt8](repeating:0,count:count);
        bufferData.copyBytes(to: &t, from: Range(uncheckedBounds:(from,to)))
        //
        var value : UInt64 = 0
        let data = NSData(bytes: t, length: count);
        data.getBytes(&value, length: count);
        value = UInt64(bigEndian: value)
        return value;
    }
    //
    //
    private func newDataArrived(){
        if(bufferData.count < 4){
            //包头没有读完
            print("not full package count");
            return;
        }
        let capacityInt=dataToUInt32(data: bufferData,from:0);
        //
        let allCount = Int(capacityInt)+4;
        //
        if(bufferData.count < allCount){
            //not full package
            print("not full package all count");
            
            return;
        }
        //
        let payloadType=dataToUInt16(data:bufferData,from:4);    //2
        let requestId=dataToUInt32(data:bufferData,from:6);      //4
        let timestamp=dataToUInt64(data:bufferData,from:10);     //8
        let statusCode=dataToUInt16(data:bufferData,from:18);       //2
        let statusMsgLength=dataToUInt16(data:bufferData,from:20);   //2
        let serviceIdLength=dataToUInt16(data:bufferData,from:22);    //2
        //
        var statusMsg = [UInt8](repeating:0,count:Int(statusMsgLength));
        var serviceId = [UInt8](repeating:0,count:Int(serviceIdLength));
        //
        let payloadLength = Int(capacityInt) - 20 - Int(statusMsgLength) - Int(serviceIdLength);
        //
        var payload = [UInt8](repeating:0,count:Int(payloadLength));
        //
        var to = 24+Int(statusMsgLength);
        bufferData.copyBytes(to: &statusMsg, from: Range(uncheckedBounds:(24,to)));
        bufferData.copyBytes(to: &serviceId, from: Range(uncheckedBounds:(to,to+Int(serviceIdLength))));
        to = to + Int(serviceIdLength);
        bufferData.copyBytes(to: &payload, from: Range(uncheckedBounds:(to,to+Int(payloadLength))));
        //
        let resp = ResponseMessage();
        resp.requestId = requestId;
        resp.serviceId = String(bytes:serviceId,encoding:.utf8)!;
        resp.statusCode = Int16(statusCode);
        resp.statusMessage = String(bytes:statusMsg,encoding:.utf8)!;
        resp.timestamp = timestamp;
        //
        if(payloadType==1){
            let string = String(bytes:payload,encoding:.utf8)!
            let data = string.data(using: String.Encoding.utf8)
            do{
                let jsonArr = try JSONSerialization.jsonObject(with: data!,
                                                               options: []);
                resp.payload=jsonArr;
            }catch let error{
                print("payload error \(error)  \(string)");
            }
        }
        //
        if(requestId==0){
            if let callback = pushCallback{
                DispatchQueue.main.async {
                    callback(resp);
                }
            }
        }else{
            if let callback = callbackMap[Int(requestId)]{
                DispatchQueue.main.async {
                    callback.callback!(resp);
                }
                callbackMap.removeValue(forKey: Int(requestId));
            }
        }
        //
        bufferData.removeFirst(to+Int(payloadLength));
        if(bufferData.count>0){
            newDataArrived();
        }
    }
    //
    private func checkTimeout(){
        let now = UInt64(Date.milliseconds);
        //
        var timeouts = [Int]();
        //
        for (id, requestMessage) in callbackMap {
            let diff = now - requestMessage.timestamp;
            if( diff > (1000*10)){
                //timeout
                print("request \(id) \(requestMessage.serviceId) timeout :\(diff)");
                timeouts.append(id);
            }
        }
        //
        for id in timeouts{
            if let msg=callbackMap.removeValue(forKey: id){
                if let callback = msg.callback{
                    let responseMessage=ResponseMessage();
                    responseMessage.statusCode=Int16(ResponseMessage.STATUS_CODE_TIMEOUT);
                    callback(responseMessage);
                }
            }
        }
    }
    //
    public func connect(ip:String,port:Int32){
        do{
            print("connect socket \(ip):\(port)");
            socket=setup();
            try socket?.connect(to: ip, port: port);
            print("socket status:\(socket?.isActive ?? false) \(socket?.isConnected ?? false)");
            isConnectedSocket=(socket?.isConnected ?? false);
        }catch let error {
            handleError(error: error);
        }
    }
    //
    public func isConnected()->Bool{
        return isConnectedSocket;
    }
    //
    public func disConnect(){
        socket?.close();
        isConnectedSocket=false;
    }
    //
    //
    public func invoke(serviceId:String,args:[String],callback:@escaping (ResponseMessage)->Void){
        requestId=requestId+1;
        let requestMessage = RequestMessage();
        requestMessage.timestamp = UInt64(Date.milliseconds);
        requestMessage.requestId = UInt32(requestId);
        requestMessage.serviceId=serviceId;
        requestMessage.callback = callback;
        requestMessage.rps = args;
        //
        callbackMap[requestId]=requestMessage;
        //
        let data = encode(reqMsg: requestMessage);
        //
        if(!isConnected()){
            print("socket disconnect");
            return;
        }
        do{
            try socket?.write(from: data);
        }catch let error {
            handleError(error: error);
        }
    }
    //
    public func encode(reqMsg:RequestMessage)->Data{
        var requestId = reqMsg.requestId.bigEndian;
        let payladJson = try? JSONSerialization.data(withJSONObject: reqMsg.rps, options: [])
        var payloadString = String(data:payladJson!, encoding: String.Encoding.utf8);
        let payload:[UInt8] = Array(payloadString!.utf8);
        let serviceId:[UInt8] = Array(reqMsg.serviceId.utf8);
        //
        var capacity:UInt32 =  UInt32(12+serviceId.count+payload.count).bigEndian;
        var payloadType:UInt16 = 1;
        payloadType = payloadType.bigEndian;
        var serviceIdLength:UInt16 = UInt16(serviceId.count).bigEndian;
        //
        var data = Data();
        data.append(UnsafeBufferPointer(start: &capacity, count: 1));
        data.append(UnsafeBufferPointer(start: &payloadType, count: 1));
        data.append(UnsafeBufferPointer(start: &requestId, count: 1));
        data.append(UnsafeBufferPointer(start: &serviceIdLength, count: 1));
        data.append(serviceId,count: serviceId.count);
        data.append(payload,count: payload.count);
        //
        return data;
    }
}

