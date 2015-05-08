/**
 * 
 */
package jazmin.test.core.app;

import jazmin.core.app.AutoWired;
import jazmin.driver.jdbc.C3p0ConnectionDriver;

/**
 * @author yama
 * 31 Mar, 2015
 */
public class TestDAO {
	
	@AutoWired
	C3p0ConnectionDriver connectionDriver;
}
