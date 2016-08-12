/**
 * 
 */
package jazmin.deploy.controller;

import java.io.File;
import java.io.FileOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import jazmin.server.web.mvc.Context;
import jazmin.server.web.mvc.Controller;
import jazmin.server.web.mvc.HttpMethod;
import jazmin.server.web.mvc.JsonView;
import jazmin.server.web.mvc.ResourceView;
import jazmin.server.web.mvc.Service;
import jazmin.util.IOUtil;

/**
 * 
 * @author icecooly
 *
 */
@Controller(id = "tencent_audio")
public class AudioController {
	//
	@Service(id = "record", method = HttpMethod.ALL)
	public void report(Context c) {
		c.view(new ResourceView("/jsp/audio_record.jsp"));
	}
	//
	@Service(id="upload_audio",method=HttpMethod.POST)
	public void uploadAudio(Context ctx)throws Exception{
		HttpServletRequest req=ctx.request().raw();
		Part part = req.getPart("data");
		File outFile=new File("/Users/yama/Desktop/test.wav");
		outFile.createNewFile();
		FileOutputStream fos=new FileOutputStream(outFile);
		IOUtil.copy(part.getInputStream(), fos);
		ctx.view(new JsonView());
	}
	//
	@Service(id = "callback", method = HttpMethod.ALL)
	public void callback(Context ctx) {
		ctx.put("code", 0);
		ctx.put("message", "成功");
		ctx.view(new JsonView());
	}
}