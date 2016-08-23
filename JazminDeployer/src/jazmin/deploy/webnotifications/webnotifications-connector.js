window.jazmin_deploy_webnotifications_WebNotification = function() {
	
	this.requestPermission = function() {
		Notify.requestPermission();
	}
	
	this.show = function(title, message,icon) {
		var n = new Notify(title, {
			body : message,
			icon:icon
		});
		n.show();
	}
}
