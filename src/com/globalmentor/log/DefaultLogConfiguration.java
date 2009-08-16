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

import java.io.File;
import java.io.Writer;
import java.util.*;

import static java.util.Collections.*;

import static com.globalmentor.java.Classes.*;
import static com.globalmentor.java.Java.*;
import static com.globalmentor.java.Objects.*;
import com.globalmentor.util.*;

/**The configuration for default logging.
<p>The configuration will be used for creating new loggers; changing the configuration
will not affect previously created loggers.</p>
@author Garret Wilson
@see DefaultLogger
*/
public class DefaultLogConfiguration implements LogConfiguration
{

	/**Whether the default logger should be used.
	This flag is set to <code>false</code> when per-class loggers are configured.
	*/
	private boolean useDefaultLogger=true;

	/**The default logger, or <code>null</code> if the default logger has not yet been configured.*/
	private Logger defaultLogger=null;

	/**@return The default logger, or <code>null</code> if the default logger has not yet been configured.*/
	public Logger getDefaultLogger() {return defaultLogger;}

	/**Sets the default logger.
	@param defaultLogger The new default logger.
	@throws NullPointerException if the given logger is <code>null</code>.
	*/
	public void setDefaultLogger(final Logger defaultLogger) {this.defaultLogger=checkInstance(defaultLogger, "Default logger cannot be null.");}

	/**Returns the current default logger.
	If no default logger has been configured, one is created and configured.
	@return The default logger, created and configured if necessary.
	*/
	protected Logger determineDefaultLogger()
	{
		if(defaultLogger==null)	//if no default logger has yet been created (this race condition is benign, because it is assumed that at this point in time multiple threads would create equivalent loggers)
		{
			defaultLogger=createLogger();	//create a new logger with the current configuration
		}
		return defaultLogger;
	}

	/**Creates a new logger using the current configuration settings.
	@return A new logger instance, configured using the current configuration settings.
	@see #getFile()
	@see #getWriter()
	@see #isStandardOutput()
	@see #getLevels()
	@see #getReport()
	*/
	public Logger createLogger()
	{
		final Writer writer=getWriter();	//see if we have a writer specified
		final DefaultLogger logger=writer!=null ? new DefaultLogger(writer) : new DefaultLogger(getFile());	//configure the logger with a writer or file (the latter of which may be null)
		logger.setStandardOutput(isStandardOutput());	//set whether the standard output should be used
		logger.setLevels(getLevels());	//set the levels to report
		logger.setReport(getReport());	//set the information to report
		return logger;	//return the created and configured logger
	}

	/**The map of loggers keyed to classes.
	Code accessing this map should use its read/write lock.
	*/
	private final ReadWriteLockMap<Class<?>, Logger> classLoggerMap=new DecoratorReadWriteLockMap<Class<?>, Logger>(new HashMap<Class<?>, Logger>());

	/**Returns the logger registered for a specific class/interface.
 	@param objectClass The specific class for which a logger should be returned.
	@return The logger registered for the specific given class.
	@throws NullPointerException if the given class is <code>null</code>.
	*/
	public Logger getRegisteredLogger(final Class<?> objectClass)
	{
		return classLoggerMap.get(checkInstance(objectClass, "Class cannot be null."));
	}

	/**Registers a logger for a given class/interface and all its sublasses/implementations that don't have specific logger registrations.
 	If a logger is already configured for the given class, it will be replaced.
 	@param objectClass The class for which a logger should be registered.
	@param logger The logger to use for the given class. 
	@return The logger previously registered for the given class.
	@throws NullPointerException if the given class and/or logger is <code>null</code>.
	*/
	public Logger registerLogger(final Class<?> objectClass, final Logger logger)
	{
		classLoggerMap.writeLock().lock();
		try
		{
			final Logger oldLogger=classLoggerMap.put(checkInstance(objectClass, "Class cannot be null."), checkInstance(logger, "Logger cannot be null."));
			useDefaultLogger=false;	//indicate we now have mappings
			return oldLogger;
		}
		finally
		{
			classLoggerMap.writeLock().unlock();
		}
	}

	/**Unregisters a logger for a given class/interface.
 	If no logger is registered for a given class, no action is taken.
 	@param objectClass The class for which a logger should be unregistered.
	@return The logger previously registered for the given class.
	@throws NullPointerException if the given class is <code>null</code>.
	*/
	public Logger unregisterLogger(final Class<?> objectClass)
	{
		classLoggerMap.writeLock().lock();
		try
		{
			final Logger oldLogger=classLoggerMap.remove(checkInstance(objectClass, "Class cannot be null."));
			useDefaultLogger=classLoggerMap.isEmpty();	//if the class logger map is empty, use the default logger for everything
			return oldLogger;
		}
		finally
		{
			classLoggerMap.writeLock().unlock();
		}
	}

	/**Retrieves an appropriate logger.
	<p>The returned logger may be a default logger, or it may be a logger configured for the calling class.</p>
	<p>This implementation locates the logger quickly if no class-specific loggers have been configured.
	Otherwise, a logger is returned for the calling class by delegating to {@link #getLogger(Class)}.</p>
	@return An appropriate logger for the current circumstances.
	*/
	public Logger getLogger()
	{
		if(useDefaultLogger)	//if we should use the default logger
		{
			return determineDefaultLogger();	//return the default logger, creating it if necessary
		}
		return getLogger(getCallingClass(Log.class));	//get a logger for the clasl calling this class (ignoring the Log class, which might have been used for its convenience methods)
	}

	/**Retrieves the appropriate logger for the given class.
	<p>This implementation determines a logger in the following manner:
	If there is no logger configured for the specific class, a logger is searched for using each ancestor class and interface of the given class.
	If no logger is found for any superclass or interface, the default logger is used.
	If the default logger does not exist, it will be created using the current settings.
	If there was no logger configured for the specific class, the determined logger will be associated with the specific class
	for faster lookups in the future, unless there are no mappings at all, in which case the default logger will immediately be returned.</p>
	@param objectClass The class for which a logger should be returned.
	@return The logger configured for the given class.
	@throws NullPointerException if the given class is <code>null</code>.
	*/
	public Logger getLogger(final Class<?> objectClass)
	{
		if(useDefaultLogger)	//if we should use the default logger
		{
			return determineDefaultLogger();	//return the default logger, creating it if necessary
		}
		Logger logger=getRegisteredLogger(objectClass);	//see if we already have a logger for this specific class
		if(logger==null)	//if we don't yet have a logger for this class
		{
			classLoggerMap.writeLock().lock();
			try
			{
				for(final Class<?> ancestorClass:getAncestorClasses(objectClass))	//look at all the classes and interfaces (including this one, now that we're under a write lock)
				{
					logger=getRegisteredLogger(ancestorClass);	//see if we already have a logger for this ancestor class
					if(logger!=null)	//if we found a logger for this class
					{
						registerLogger(objectClass, logger);	//register the logger with the specific class to speed searches the next time
						break;	//stop looking for a logger for an ancestor class
					}
				}
			}
			finally
			{
				classLoggerMap.writeLock().unlock();
			}
		}
		if(logger==null)	//if we couldn't find a registered logger for any of the ancestor classes
		{
			logger=determineDefaultLogger();	//determine the default logger to use
			registerLogger(objectClass, logger);	//register the logger with the specific class to speed searches the next time
		}
		return logger;
	}

	/**The levels that should be logged.*/
	private Set<Log.Level> levels=unmodifiableSet(EnumSet.allOf(Log.Level.class));

		/**Returns the levels that should be logged.
		Defaults to all available levels.
		@return The levels that will be logged.
		*/
		public Set<Log.Level> getLevels() {return levels;}
	
		/**Sets the report levels that will be logged.
		@param levels The levels that will be logged.
		@throws NullPointerException if the given levels is <code>null</code>.
		*/
		public void setLevels(final Set<Log.Level> levels) {this.levels=unmodifiableSet(EnumSet.copyOf(checkInstance(levels, "Levels cannot be null.")));}

		/**Sets the minimum level that will actually be logged.
		@param minimumLevel The minimum level that will be logged.
		@throws NullPointerException if the given minimum level is <code>null</code>.
		@see #setLevels(Set)
		*/
		public void setMinimumLevel(final Log.Level minimumLevel)
		{
			final int minimumOrdinal=minimumLevel.ordinal();	//get the ordinal of the minimum level
			final Set<Log.Level> levels=EnumSet.of(minimumLevel);	//create a set with the minimum level
			for(final Log.Level level:Log.Level.values())	//for all available levels
			{
				if(level.ordinal()>minimumOrdinal)	//if this level is higher than the minimum
				{
					levels.add(level);	//add this level to the set as well
				}
			}
			setLevels(levels);	//set the levels
		}

	/**The information that should be reported with each log.*/
	private Set<Log.Report> report=unmodifiableSet(EnumSet.allOf(Log.Report.class));

		/**Returns the log information reported.
		Defaults to all report options.
		@return The information that will be reported with each log.
		*/
		public Set<Log.Report> getReport() {return report;}
	
		/**Sets the type of information that should be reported with each log.
		@param report The information to be reported on.
		@throws NullPointerException if the given report is <code>null</code>.
		*/
		public void setReport(final Set<Log.Report> report) {this.report=unmodifiableSet(EnumSet.copyOf(checkInstance(report, "Report cannot be null.")));}

	/**The file to use for logging, or <code>null</code> if no file is to be used.*/
	private File file=null;

	/**The file to be used for logging, or <code>null</code> if no file is to be used.*/
	public File getFile() {return file;}

	/**Sets the file to use for logging.
	If a file is given, any configured writer is removed.
	@param file The file to be used for logging, or <code>null</code> if no file is to be used.
	@see #setWriter(Writer)
	*/
	public void setFile(final File file)
	{
		if(file!=null)	//if they want to use a file
		{
			writer=null;	//we won't use a writer
		}
		this.file=file;
	}

	/**The writer to be used to log information, or <code>null</code> if there is no writer to be used to log information.*/
	private Writer writer=null;

	/**@return The writer to be used to log information, or <code>null</code> if there is no writer to be used to log information.*/
	public Writer getWriter() {return writer;}

	/**Sets the writer to use for logging.
	If a writer is given, any configured file is removed.
	@param writer The writer to be used to log information, or <code>null</code> if there is no writer to be used to log information.
	@see #setFile(File)
	*/
	public void setWriter(final Writer writer)
	{
		if(writer!=null)	//if they want to use a writer
		{
			file=null;	//we won't use a file
		}
		this.writer=writer;
	}

	/**Whether information should be sent to {@link System#out} or {@link System#err} as appropriate, in addition to other destinations, if any.*/
	private boolean standardOutput;

		/**@return Whether information should be sent to {@link System#out} or {@link System#err} as appropriate, in addition to other destinations, if any.*/
		public boolean isStandardOutput() {return standardOutput;}

		/**Sets whether information should be sent to {@link System#out} or {@link System#err} as appropriate, in addition to other destinations, if any.
		@param standardOutput Whether information should be sent to the standard output.
		*/
		public void setStandardOutput(final boolean standardOutput) {this.standardOutput=standardOutput;}


	/**Default constructor.
	Logging to the standard output will be enabled by default.
	*/
	public DefaultLogConfiguration()
	{
		this((File)null);
	}

	/**File constructor.
	If a file is given, logging to the standard output will default to disabled.
	@param file The file to be used for logging, or <code>null</code> if no file is to be used.
	*/
	public DefaultLogConfiguration(final File file)
	{
		this.file=file;
		setStandardOutput(file!=null);	//by default turn off logging to the standard output if a writer was given
	}

	/**Writer constructor.
	If a writer is given, logging to the standard output will default to disabled.
	@param writer The writer to be used to log information, or <code>null</code> if there is no writer to be used to log information.
	*/
	public DefaultLogConfiguration(final Writer writer)
	{
		this.writer=writer;
		setStandardOutput(writer!=null);	//by default turn off logging to the standard output if a writer was given
	}

	/**File and levels constructor.
	If a file is given, logging to the standard output will default to disabled.
	@param file The file to be used for logging, or <code>null</code> if no file is to be used.
	@param levels The levels that will be logged.
	@throws NullPointerException if the given levels is <code>null</code>.
	*/
	public DefaultLogConfiguration(final File file, final Set<Log.Level> levels)
	{
		this(file);
		setLevels(levels);
	}
	
	/**File and minimum level constructor.
	If a file is given, logging to the standard output will default to disabled.
	@param file The file to be used for logging, or <code>null</code> if no file is to be used.
	@param minimumLevel The minimum level that will be logged.
	@throws NullPointerException if the given minimum level is <code>null</code>.
	*/
	public DefaultLogConfiguration(final File file, final Log.Level minimumLevel)
	{
		this(file);
		setMinimumLevel(minimumLevel);
	}

	/**Writer and levels constructor.
	If a writer is given, logging to the standard output will default to disabled.
	@param writer The writer to be used to log information, or <code>null</code> if there is no writer to be used to log information.
	@param levels The levels that will be logged.
	@throws NullPointerException if the given levels is <code>null</code>.
	*/
	public DefaultLogConfiguration(final Writer writer, final Set<Log.Level> levels)
	{
		this(writer);
		setLevels(levels);
	}
	
	/**Writer and minimum level constructor.
	If a writer is given, logging to the standard output will default to disabled.
	@param writer The writer to be used to log information, or <code>null</code> if there is no writer to be used to log information.
	@param minimumLevel The minimum level that will be logged.
	@throws NullPointerException if the given minimum level is <code>null</code>.
	*/
	public DefaultLogConfiguration(final Writer writer, final Log.Level minimumLevel)
	{
		this(writer);
		setMinimumLevel(minimumLevel);
	}
}
