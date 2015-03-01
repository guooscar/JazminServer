package jazmin.driver.jdbc;

import java.sql.ResultSet;

/**
 * 
 * @author yama
 * 27 Dec, 2014
 * @param <T>
 */
@FunctionalInterface
public interface ResultSetHandler<T> {
	T handleRow(ResultSet row)throws Exception;
}
