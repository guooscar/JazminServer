/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
