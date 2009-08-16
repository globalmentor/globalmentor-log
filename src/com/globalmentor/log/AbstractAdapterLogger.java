/*
 * Copyright © 1996-2009 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.log;

import static com.globalmentor.java.Objects.*;

/**A logger that delegates to another type of logger.
@param <L> The type of delegate logger.
@author Garret Wilson
*/
public abstract class AbstractAdapterLogger<L> implements Logger
{

	/**The logger delegate.*/
	private L logger;

		/**@return The logger delegate.*/
		protected L getLogger() {return logger;}

	/**Decorated constructor.
	@param logger The logger delegate.
	@throws NullPointerException if the given logger is <code>null</code>.
	*/
	public AbstractAdapterLogger(final L logger)
	{
		this.logger=checkInstance(logger, "Logger cannot be null.");
	}

}