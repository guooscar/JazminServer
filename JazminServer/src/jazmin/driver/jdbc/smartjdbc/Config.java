package jazmin.driver.jdbc.smartjdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import jazmin.driver.jdbc.smartjdbc.provider.SelectProvider;

/**
 * 
 * @author skydu
 *
 */
public class Config {
	/**
	 * 
	 */
	private static List<DAOInterceptor> daoInterceptors=new ArrayList<>();
	//
	/**
	 * default domainClass's name
	 */
	private static Function<Class<?>,String> tableNameFunc=(domainClass)->{
		return "t"+convertField(domainClass.getSimpleName());
	};
	
	/**
	 * javaFieldName->dbName
	 */
	private static Function<String,String> convertFieldNameFunc=(name)->{
		return convertField(name);	
	};
	
	/**
	 * defaultOrderBy
	 */
	private static BiConsumer<SelectProvider,Query> defaultOrderBy;
	//
	public static String getTableName(Class<?> domainClass) {
		return tableNameFunc.apply(domainClass);
	}
	//
	/**
	 * @return the tableNameFunc
	 */
	public static Function<Class<?>, String> getTableNameFunc() {
		return tableNameFunc;
	}
	/**
	 * @param tableNameFunc the tableNameFunc to set
	 */
	public static void setTableNameFunc(Function<Class<?>, String> tableNameFunc) {
		Config.tableNameFunc = tableNameFunc;
	}
	/**
	 * @return the convertFieldNameFunc
	 */
	public static Function<String, String> getConvertFieldNameFunc() {
		return convertFieldNameFunc;
	}
	/**
	 * @param convertFieldNameFunc the convertFieldNameFunc to set
	 */
	public static void setConvertFieldNameFunc(Function<String, String> convertFieldNameFunc) {
		Config.convertFieldNameFunc = convertFieldNameFunc;
	}
	/**
	 * 
	 * @param name
	 * @return
	 */
	public static String convertFieldName(String name) {
		return convertFieldNameFunc.apply(name);
	}
	
	/**
	 * @return the daoInterceptors
	 */
	public static List<DAOInterceptor> getDaoInterceptors() {
		return daoInterceptors;
	}
	
	/**
	 * @param daoInterceptors the daoInterceptors to set
	 */
	public static void setDaoInterceptors(List<DAOInterceptor> daoInterceptors) {
		Config.daoInterceptors = daoInterceptors;
	}
	
	/**
	 * 
	 * @param interceptor
	 */
	public static void addDAOInterceptor(DAOInterceptor interceptor) {
		daoInterceptors.add(interceptor);
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	private static  String convertField(String name) {
		StringBuffer result = new StringBuffer();
		for (char c : name.toCharArray()) {
			if (Character.isUpperCase(c)) {
				result.append("_");
			}
			result.append(Character.toLowerCase(c));
		}
		return result.toString();
	}
	/**
	 * 
	 * @return
	 */
	public static BiConsumer<SelectProvider,Query> getDefaultOrderBy() {
		return defaultOrderBy;
	}
	/**
	 * 
	 * @param defaultOrderBy
	 */
	public static void setDefaultOrderBy(BiConsumer<SelectProvider,Query> defaultOrderBy) {
		Config.defaultOrderBy = defaultOrderBy;
	}
	
}
