/**
 * 
 */
package jazmin.deploy.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import jazmin.deploy.domain.AppPackage;
import jazmin.deploy.domain.Instance;
import jazmin.deploy.domain.PackageDownloadInfo;
import jazmin.deploy.manager.DeployManager;
import jazmin.server.web.mvc.Context;
import jazmin.server.web.mvc.View;
import jazmin.util.IOUtil;

/**
 * @author yama
 * 8 Jan, 2015
 */
public class PackageDownloadView implements View{
	private File file;
	private String mimetype;
	private Instance instance;
	private AppPackage packageInfo;
	
	public PackageDownloadView(Instance instance,AppPackage pkg) {
		this.file=new File(pkg.file);
		this.instance=instance;
		this.packageInfo=pkg;
	}
	//
	@Override
	public void render(Context ctx) throws Exception {
		HttpServletResponse response=ctx.response().raw();
		if(!file.exists()){
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		PackageDownloadInfo info=new PackageDownloadInfo();
		info.instanceId=instance.id;
		info.packageId=packageInfo.id;
		info.startTime=new Date();
		info.packageSize=file.length();
		info.clientRemoteHost=ctx.request().raw().getRemoteAddr();
		info.clientRemotePort=ctx.request().raw().getRemotePort();
		DeployManager.addPackageDownloadInfo(info);
		//
		ServletOutputStream outStream = response.getOutputStream();
        // sets response content type
		if(mimetype==null){
			mimetype = "application/octet-stream";
		}
        response.setContentType(mimetype);
        response.setContentLengthLong(file.length());
        // sets HTTP header
        String filename = new String(file.getName().getBytes("UTF-8"), "ISO8859-1");
        response.setHeader("Content-Disposition", 
        		"attachment; filename=\"" + filename + "\"");
        FileInputStream fis=new FileInputStream(file);
    	byte[] buffer = new byte[4096];
		long count = 0;
		int n = 0;
		while (-1 != (n = fis.read(buffer))) {
			outStream.write(buffer, 0, n);
			count += n;
			info.currentDownloadSize=count;
			//
			double percent=(double)info.currentDownloadSize/(double)info.packageSize;
			info.percent=(int) (percent*100);
		}
        IOUtil.closeQuietly(fis);
        IOUtil.closeQuietly(outStream);
        DeployManager.removePackageDownloadInfo(info);
	}
}
