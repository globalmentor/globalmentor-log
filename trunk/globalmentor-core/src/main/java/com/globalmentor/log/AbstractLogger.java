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

/**Abstract implementation of a logger.
@author Garret Wilson
*/
public abstract class AbstractLogger implements Logger
{

	/**Logs a series of objects at a given log level.
	This implementation delegates to {@link #trace(Object...)}, {@link #debug(Object...)}, {@link #info(Object...)},
	{@link #warn(Object...)}, or {@link #error(Object...)}. 
	@param level The level at which to log the objects.
	@param objects The objects to log; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	@throws NullPointerException if the given log level is <code>null</code>.
	*/
	public void log(final Log.Level level, final Object... objects) {
		switch(level) {
			case TRACE:
				trace(objects);
				break;
			case DEBUG:
				debug(objects);
				break;
			case INFO:
				info(objects);
				break;
			case WARN:
				warn(objects);
				break;
			case ERROR:
				error(objects);
				break;
			default:
				throw new AssertionError("Unrecognized log level: "+level);
		}
	}

}