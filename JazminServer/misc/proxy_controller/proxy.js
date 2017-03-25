var ProxyClient = {
		url:"http://localhost:7001/srv/proxy/invoke",
		token:"",
		//
		invoke:function(name,args,success,error){
			//
			var data={};
			for(var i=0;i<args.length;i++){
				data["arg"+i]=JSON.stringify(args[i]);
			}
			data["token"]=this.token;
			//
			$.ajax({
				url : this.url+"/"+name,
				data : data,
				type : 'post',
				cache : false,
				success : function(data) {
					var dataArray=data.split('\n');
					var ret;
					var exception;
					if(dataArray.length>=1&&dataArray[0].length>0){
						ret=JSON.parse(dataArray[0]);
					}
					if(dataArray.length>=2&&dataArray[1].length>0){
						var fullMsg=dataArray[1];
						var offset=fullMsg.indexOf(',');
						var errorType=fullMsg.substring(0,offset);
						var t=offset;
						offset=fullMsg.indexOf(',',offset);
						var errorCode=fullMsg.substring(t,offset);
						var errorMessage=fullMsg.substring(offset+1);
						exception={
								code:errorCode,
								message:errorMessage,
								type:errorType
						}
					}
					success(ret,exception);
					
				},
				error : function(e) {
					if(error){
						error();
					}
				}
			});	
		}
};