package jazmin.driver.jdbc;

/**
 * 
 * @author skydu
 *
 */
public abstract class TransactionSynchronizationAdapter implements TransactionSynchronization{

	/**
	 * 
	 */
	@Override
	public void beforeCommit() {	
	}

	/**
	 * 
	 */
	@Override
	public void afterCommit() {
	}
	
	/**
	 * 
	 */
	@Override
	public void beforeRollback() {
	}

	/**
	 * 
	 * @param status
	 */
	@Override
	public void afterRollback() {
	}

}