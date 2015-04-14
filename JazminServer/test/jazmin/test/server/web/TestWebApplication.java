/**
 * 
 */
package jazmin.test.server.web;

import jazmin.server.web.WebApplication;
import jazmin.server.web.mvc.Context;
import jazmin.server.web.mvc.Controller;
import jazmin.server.web.mvc.PlainTextView;
import jazmin.server.web.mvc.ResourceView;
import jazmin.server.web.mvc.Service;

/**
 * @author g2131
 *
 */
@Controller(id="test")
public class TestWebApplication extends WebApplication{
	public TestWebApplication() {
		super("/srv/*");
	}
	//
	@Override
	public void init() throws Exception {
		super.init();
		registerController(this);
	}
	//
	//
	/**
	 * /srv/index/hello
	 */
	@Service(id="hello")
	public void hello(Context c){
		c.view(new PlainTextView("hello"));
	}
	//
	@Service(id="jsp")
	public void jsp(Context c){
		c.put("testValue","hello");
		c.view(new ResourceView("/jsp/test.jsp") );
	}
}
