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

import com.globalmentor.config.Configurator;

/**Central class for controlling logging.
<p>A default logger is automatically installed that send all non-error messages
to {@link System#out} and all error messages to {@link System#err}.</p>
@author Garret Wilson
*/
public final class Log
{

	/**The common name extension for log files.*/
	public final static String NAME_EXTENSION="log";

	static
	{
		Configurator.setDefaultConfiguration(LogConfiguration.class, new DefaultLogConfiguration());	//install a default log configuration using stdout/err
	}

	/**Returns the default log configuration.
	<p>This method is the preferred approach for determining the default log configuration,
	as it ensures a default configuration has been installed.</p>
	@return The default log configuration.
	@see Configurator#getDefaultConfiguration(Class)
	*/
	public static LogConfiguration getDefaultConfiguration()
	{
		return Configurator.getDefaultConfiguration(LogConfiguration.class);
	}

	/**Sets the default log configuration.
	@param configuration The configuration to set.
	@return The previous configuration, or <code>null</code> if there was no previous configuration.
	@throws NullPointerException if the given configuration is <code>null</code>.
	@see Configurator#setDefaultConfiguration(Class)
	*/
	public static LogConfiguration setDefaultConfiguration(final LogConfiguration logConfiguration)
	{
		return Configurator.setDefaultConfiguration(LogConfiguration.class, logConfiguration);
	}

	/**Returns the configured log configuration for the current context.
	<p>This method is the preferred approach for determining the log configuration,
	as it ensures a default configuration has been installed.</p>
	@return The configured log configuration for the current context.
	@see Configurator#getConfiguration(Class)
	*/
	public static LogConfiguration getConfiguration()
	{
		return Configurator.getConfiguration(LogConfiguration.class);
	}

	/**The available logging levels.*/
	public enum Level
	{
		/**Indicates the program's execution path.*/
		TRACE,
		/**Indicates useful information, usually verbose.*/
		INFO,
		/**Specific information on an event which should be logged but which is adversity-neutral.*/
		LOG,
		/**Indications that conditions are possibly adverse.*/
		WARN,
		/**Indicates an unexpected condition representing an error.*/
		ERROR
	}; 

	/**The available reporting options.*/
	public enum Report
	{
		/**Indicates that the log level should be reported.*/
		LEVEL,	
		/**Indicates that execution time should be reported.*/
		TIME,
		/**Indicates that the thread name should be reported.*/
		THREAD,
		/**Indicates that the location of program execution should be reported.*/
		LOCATION
	}

	/**Logs a series of trace objects.
	<p>Meant for messages that show the path of program execution.</p>
	@param objects The objects to log.
	@see Log.Level#TRACE
	*/
	public static void trace(final Object... objects)
	{
		Configurator.getConfiguration(LogConfiguration.class).getLogger().trace(objects);
	}

	/**Logs a series of trace objects.
	<p>Meant for messages that show the path of program execution.</p>
	@param objectClass The class for which a logger should be returned.
	@param objects The objects to log.
	@throws NullPointerException if the given class is <code>null</code>.
	@see Log.Level#TRACE
	*/
	public static void trace(final Class<?> objectClass, final Object... objects)
	{
		Configurator.getConfiguration(LogConfiguration.class).getLogger(objectClass).trace(objects);
	}

	/**Logs a series of trace objects and a stack trace.
	<p>Meant for messages that show the path of program execution.</p>
	<p>If no objects are provided, only the trace location will be output.</p>
	@param objects The objects to log; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	@see Log.Level#TRACE
	*/
	public static void traceStack(final Object... objects)
	{
		Configurator.getConfiguration(LogConfiguration.class).getLogger().traceStack(objects);
	}

	/**Logs a series of trace objects and a stack trace.
	<p>Meant for messages that show the path of program execution.</p>
	<p>If no objects are provided, only the trace location will be output.</p>
	@param objectClass The class for which a logger should be returned.
	@param objects The objects to log; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	@throws NullPointerException if the given class is <code>null</code>.
	@see Log.Level#TRACE
	*/
	public static void traceStack(final Class<?> objectClass, final Object... objects)
	{
		Configurator.getConfiguration(LogConfiguration.class).getLogger(objectClass).traceStack(objects);
	}

	/**Logs a series of information objects.
	<p>Meant for useful information, usually verbose.</p>
	@param objects The objects to log; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	@see Log.Level#INFO
	*/
	public static void info(final Object... objects)
	{
		Configurator.getConfiguration(LogConfiguration.class).getLogger().info(objects);
	}

	/**Logs a series of information objects.
	<p>Meant for useful information, usually verbose.</p>
	@param objectClass The class for which a logger should be returned.
	@param objects The objects to log; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	@throws NullPointerException if the given class is <code>null</code>.
	@see Log.Level#INFO
	*/
	public static void info(final Class<?> objectClass, final Object... objects)
	{
		Configurator.getConfiguration(LogConfiguration.class).getLogger(objectClass).info(objects);
	}

	/**Logs a series of objects.
	<p>Meant for logging specific events which should be logged but which are adversity-neutral.</p>
	@param objects The objects to log; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	@see Log.Level#LOG
	*/
	public static void log(final Object... objects)
	{
		Configurator.getConfiguration(LogConfiguration.class).getLogger().log(objects);
	}

	/**Logs a series of objects.
	<p>Meant for logging specific events which should be logged but which are adversity-neutral.</p>
	@param objectClass The class for which a logger should be returned.
	@param objects The objects to log; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	@throws NullPointerException if the given class is <code>null</code>.
	@see Log.Level#LOG
	*/
	public static void log(final Class<?> objectClass, final Object... objects)
	{
		Configurator.getConfiguration(LogConfiguration.class).getLogger(objectClass).log(objects);
	}

	/**Logs a series of warning objects
	<p>Meant for errors that should not prevent the robust functioning of the program
		and that are expected to occur infrequently and not because of program design.</p>
	@param objects The objects to log; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	@see Log.Level#WARN
	*/
	public static void warn(final Object... objects)
	{
		Configurator.getConfiguration(LogConfiguration.class).getLogger().warn(objects);
	}

	/**Logs a series of warning objects
	<p>Meant for errors that should not prevent the robust functioning of the program
		and that are expected to occur infrequently and not because of program design.</p>
	@param objectClass The class for which a logger should be returned.
	@param objects The objects to log; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	@throws NullPointerException if the given class is <code>null</code>.
	@see Log.Level#WARN
	*/
	public static void warn(final Class<?> objectClass, final Object... objects)
	{
		Configurator.getConfiguration(LogConfiguration.class).getLogger(objectClass).warn(objects);
	}

	/**Logs a series of error objects.
	<p>Meant for unexpected conditions representing errors.</p>
	@param objects The objects to log; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	@see Log.Level#ERROR
	*/
	public static void error(final Object... objects)
	{
		Configurator.getConfiguration(LogConfiguration.class).getLogger().error(objects);
	}

	/**Logs a series of error objects, using the configured logger for the given class.
	<p>Meant for unexpected conditions representing errors.</p>
	@param objectClass The class for which a logger should be returned.
	@param objects The objects to log; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	@throws NullPointerException if the given class is <code>null</code>.
	@see Log.Level#ERROR
	*/
	public static void error(final Class<?> objectClass, final Object... objects)
	{
		Configurator.getConfiguration(LogConfiguration.class).getLogger(objectClass).error(objects);
	}

}
