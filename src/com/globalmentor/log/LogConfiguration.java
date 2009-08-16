/*
 * Copyright © 2009 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import com.globalmentor.config.Configuration;

/**Configuration for logging.
@author Garret Wilson
*/
public interface LogConfiguration extends Configuration
{

	/**Retrieves an appropriate logger.
	<p>The returned logger may be a default logger, or it may be a logger configured for the calling class.</p>
	@return An appropriate logger for the current circumstances.
	*/
	public Logger getLogger();

	/**Retrieves the appropriate logger for the given class.
	@param objectClass The class for which a logger should be returned.
	@return The logger configured for the given class.
	@throws NullPointerException if the given class is <code>null</code>.
	*/
	public Logger getLogger(final Class<?> objectClass);

}
