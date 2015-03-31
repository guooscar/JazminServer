/**
 * 
 */
package jazmin.test.core.app;

import jazmin.core.app.AutoWired;
import jazmin.driver.jdbc.C3P0ConnectionDriver;

/**
 * @author yama
 * 31 Mar, 2015
 */
public class TestDAO {
	
	@AutoWired
	C3P0ConnectionDriver connectionDriver;
}
