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

/**
 * A log configuration that creates Java loggers.
 * <p>
 * This class provides no global Java logging configuration; the system-level Java logging and must be configured externally.
 * </p>
 * @author Garret Wilson
 * @see java.util.logging.Logger
 */
public class JavaLoggingLogConfiguration extends AbstractAffiliationLogConfiguration {

	/** Default constructor. */
	public JavaLoggingLogConfiguration() {
		super(false); //do not allow the use of a common logger
	}

	/**
	 * Creates a new logger for the given class, configured using the current configuration settings.
	 * @param objectClass The specific class for which a logger should be returned.
	 * @return A new logger instance for the given class.
	 * @see #getLoggerKey(Class)
	 * @see java.util.logging.Logger#getLogger(String)
	 */
	public Logger createLogger(final Class<?> objectClass) {
		final Object loggerKey = getLoggerKey(objectClass); //get a key for associating the class to a logger
		return new JavaLoggingLogger(java.util.logging.Logger.getLogger(loggerKey.toString())); //get a Java logging logger and return a logger from that
	}

}
