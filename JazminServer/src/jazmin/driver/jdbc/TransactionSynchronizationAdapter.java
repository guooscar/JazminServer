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
	public void beforeCommit(boolean readOnly) {
		
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
	public void beforeCompletion() {
	}

	/**
	 * 
	 * @param status
	 */
	@Override
	public void afterCompletion(int status) {
	}

}