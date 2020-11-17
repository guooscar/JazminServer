package jazmin.driver.jdbc;

/**
 * 
 * @author skydu
 *
 */
public interface TransactionSynchronization {

	/**
	 * 
	 */
	default void beforeCommit() {
	}

	/**
	 * 
	 */
	default void afterCommit() {
	}
	
	/**
	 * 
	 */
	default void beforeRollback() {
	}

	/**
	 * 
	 * @param status
	 */
	default void afterRollback() {
	}

}
