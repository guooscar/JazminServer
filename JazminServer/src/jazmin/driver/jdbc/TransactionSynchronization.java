package jazmin.driver.jdbc;

/**
 * 
 * @author skydu
 *
 */
public interface TransactionSynchronization {

	/** Completion status in case of proper commit. */
	int STATUS_COMMITTED = 0;

	/** Completion status in case of proper rollback. */
	int STATUS_ROLLED_BACK = 1;

	/** Completion status in case of heuristic mixed completion or system errors. */
	int STATUS_UNKNOWN = 2;

	/**
	 * 
	 * @param readOnly
	 */
	default void beforeCommit(boolean readOnly) {
	}

	/**
	 * 
	 */
	default void beforeCompletion() {
	}

	/**
	 * 
	 */
	default void afterCommit() {
	}

	/**
	 * 
	 * @param status
	 */
	default void afterCompletion(int status) {
	}

}
