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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.globalmentor.config.AbstractConfiguration;
import com.globalmentor.java.StackTrace;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Objects.*;

/**An abstract base implementation of logging configuration.
<p>This implementation uses a concurrent map which is thread-safe but still allows race conditions.
It is assumed that it is benign if multiple loggers get created temporarily for a class.</p>
<p>The logger created in {@link #createLogger(Class)} should take into consideration the key returned by {@link #getLoggerKey(Class)} if appropriate.</p>
@param <K> The type of key with which loggers are associated.
@author Garret Wilson
*/
public abstract class AbstractLogConfiguration<K> extends AbstractConfiguration implements LogConfiguration
{

	/**Whether a common logger is used if no specific logger registrations have yet been made.*/
	private boolean commonLoggerSupported;
	
		/**@return Whether a common logger is used if no specific logger registrations have yet been made.*/
		protected boolean isCommonLoggerSupported() {return commonLoggerSupported;}
	
	/**The common logger, or <code>null</code> if the common logger has not yet been designated.*/
	private Logger commonLogger=null;

		/**@return The common logger, or <code>null</code> if the common logger has not yet been designated.
		@see #isCommonLoggerSupported()
		*/
		public Logger getCommonLogger() {return commonLogger;}
	
		/**Sets the common logger.
		The common logger will be used if using a common logger is supported and no specific logger registrations have been made.
		@param commonLogger The new common logger.
		@throws NullPointerException if the given common logger is <code>null</code>.
		@see #isCommonLoggerSupported()
		*/
		public void setCommonLogger(final Logger commonLogger) {this.commonLogger=checkInstance(commonLogger, "Common logger cannot be null.");}

		/**Returns the current common logger.
		If no common logger has been designated, one is created and configured.
		<p>This implementation creates a common logger by delegating to {@link #createLogger(Class)}
		passing the class for {@link Object}.</p>
		@return The common logger, created and configured if necessary.
		*/
		protected Logger determineCommonLogger()
		{
			if(commonLogger==null)	//if no common logger has yet been created (this race condition is benign, because it is assumed that at this point in time multiple threads would create equivalent loggers)
			{
				commonLogger=createLogger(Object.class);	//create a new logger
			}
			return commonLogger;
		}

	/**Determines the object related to the given class with which a logger should be associated.
	This could be the package of the class if loggers are grouped according to package,
	or the class itself if loggers are configured on a fine-grained level.
 	@param objectClass The specific class for which a logger key should be returned.
	@return A new association for this class around which to group loggers.
	@throws NullPointerException if the given class is <code>null</code>.
	*/
	protected abstract K getLoggerKey(final Class<?> objectClass);

	/**The map of loggers.*/
	private final Map<K, Logger> classLoggerMap=new ConcurrentHashMap<K, Logger>();

	/**Returns the logger registered for a specific class/interface.
 	<p>The actual object with which the logger is registered depends on the key determined for the class.</p>
 	@param objectClass The specific class for which a logger should be returned.
	@return The logger registered for the specific given class.
	@throws NullPointerException if the given class is <code>null</code>.
	@see #getLoggerKey(Class)
	*/
	public Logger getRegisteredLogger(final Class<?> objectClass)
	{
		return classLoggerMap.get(getLoggerKey(objectClass));
	}

	/**Registers a logger for a given class/interface and all its sublasses/implementations that don't have specific logger registrations.
 	If a logger is already configured for the given class, it will be replaced.
 	<p>The actual object with which the logger is registered depends on the key determined for the class.</p>
 	@param objectClass The class for which a logger should be registered.
	@param logger The logger to use for the given class. 
	@return The logger previously registered for the given class.
	@throws NullPointerException if the given class and/or logger is <code>null</code>.
	@see #getLoggerKey(Class)
	*/
	public Logger registerLogger(final Class<?> objectClass, final Logger logger)
	{
		return classLoggerMap.put(getLoggerKey(objectClass), checkInstance(logger, "Logger cannot be null."));
	}

	/**Unregisters a logger for a given class/interface.
 	If no logger is registered for a given class, no action is taken.
 	<p>The actual object with which the logger is registered depends on the key determined for the class.</p>
 	@param objectClass The class for which a logger should be unregistered.
	@return The logger previously registered for the given class.
	@throws NullPointerException if the given class is <code>null</code>.
	*/
	public Logger unregisterLogger(final Class<?> objectClass)
	{
		return classLoggerMap.remove(getLoggerKey(objectClass));
	}

	/**Retrieves an appropriate logger.
	<p>The returned logger may be a common logger, or it may be a logger configured for the calling class.</p>
	<p>This implementation locates the logger quickly if no class-specific loggers have been configured and use of the common logger is supported.
	Otherwise, a logger is returned for the calling class by delegating to {@link #getLogger(Class)}.</p>
	@return An appropriate logger for the current circumstances.
	@see #isCommonLoggerSupported()
	@see #determineCommonLogger()
	*/
	public Logger getLogger()
	{
		if(isCommonLoggerSupported() && classLoggerMap.isEmpty())	//if using the common logger is supported and there are no mappings
		{
			return determineCommonLogger();	//return the common logger, creating it if necessary
		}
		return getLogger(StackTrace.getCallingClass(Log.class.getPackage()));	//get a logger for the class calling this class (ignoring the entire logging package, which might have been used for its convenience methods)
	}

	/**Retrieves the appropriate logger for the given class.
	<p>This implementation locates the logger quickly if no class-specific loggers have been configured and use of the common logger is supported.
	Otherwise, this implementation determines a logger in the following manner:
	If there is no logger configured for the specific class, a logger is searched for using each ancestor class and interface of the given class.
	If no logger is found for any superclass or interface, a logger is created using {@link #createLogger(Class)}.
	If there was no logger configured for the specific class, the determined logger will be associated with the specific class
	for faster lookups in the future.</p>
	@param objectClass The class for which a logger should be returned.
	@return The logger configured for the given class.
	@throws NullPointerException if the given class is <code>null</code>.
	*/
	public Logger getLogger(final Class<?> objectClass)
	{
		checkInstance(objectClass, "Class cannot be null.");
		if(isCommonLoggerSupported() && classLoggerMap.isEmpty())	//if using the common logger is supported and there are no mappings
		{
			return determineCommonLogger();	//return the common logger, creating it if necessary
		}
		Logger logger=getRegisteredLogger(objectClass);	//see if we already have a logger for this specific class
		if(logger==null)	//if we don't yet have a logger for this class
		{
			for(final Class<?> ancestorClass:getProperAncestorClasses(objectClass))	//look at all the classes and interfaces, not including this one
			{
				logger=getRegisteredLogger(ancestorClass);	//see if we already have a logger for this ancestor class
				if(logger!=null)	//if we found a logger for this class
				{
					registerLogger(objectClass, logger);	//register the logger with the specific class to speed searches the next time
					break;	//stop looking for a logger for an ancestor class
				}
			}
		}
		if(logger==null)	//if we couldn't find a registered logger for any of the ancestor classes
		{
			logger=createLogger(objectClass);	//create a new logger for the class
			registerLogger(objectClass, logger);	//register the logger with the specific class to speed searches the next time
		}
		return logger;
	}

	/**Common logger support constructor.
	@param supportCommonLogger Whether a common logger is used if no specific logger registrations have yet been made.
	*/
	public AbstractLogConfiguration(final boolean supportCommonLogger)
	{
		this.commonLoggerSupported=supportCommonLogger;
	}

}
