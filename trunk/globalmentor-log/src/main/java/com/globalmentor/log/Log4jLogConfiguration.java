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
 * A log configuration that creates Log4j loggers.
 * <p>
 * This class provides no global Log4j configuration; the system-level Log4j logging and must be configured externally.
 * </p>
 * @author Garret Wilson
 * @see org.apache.log4j.BasicConfigurator
 * @see org.apache.log4j.PropertyConfigurator
 * @see org.apache.log4j.xml.DOMConfigurator
 * @see org.apache.log4j.Logger
 */
public class Log4jLogConfiguration extends AbstractAffiliationLogConfiguration {

	/** Default constructor. */
	public Log4jLogConfiguration() {
		super(false); //do not allow the use of a common logger
	}

	/**
	 * Creates a new logger for the given class, configured using the current configuration settings.
	 * @param objectClass The specific class for which a logger should be returned.
	 * @return A new logger instance for the given class.
	 * @see #getLoggerKey(Class)
	 * @see org.apache.log4j.Logger#getLogger(String)
	 */
	public Logger createLogger(final Class<?> objectClass) {
		final Object loggerKey = getLoggerKey(objectClass); //get a key for associating the class to a logger
		final org.apache.log4j.Logger logger;
		if(loggerKey instanceof Class<?>) { //if the logger key is a class
			logger = org.apache.log4j.Logger.getLogger((Class<?>)loggerKey); //get a logger based upon the class
		} else { //if the logger key is any other type of objects
			logger = org.apache.log4j.Logger.getLogger(loggerKey.toString()); //get a logger based upon the string form of the key
		}
		return new Log4jLogger(logger); //return a logger for the Log4j logger
	}

}
