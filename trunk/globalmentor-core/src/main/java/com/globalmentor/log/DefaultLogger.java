/*
 * Copyright © 1996-2012 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import java.io.*;
import java.text.*;
import java.util.*;

import static java.util.Collections.*;

import com.globalmentor.config.ConfigurationException;

import static com.globalmentor.java.CharSequences.*;
import static com.globalmentor.java.Characters.*;
import static com.globalmentor.java.Objects.*;
import static com.globalmentor.java.OperatingSystem.*;
import static com.globalmentor.java.Threads.*;

import com.globalmentor.text.W3CDateFormat;

/**
 * Default implementation of a logger.
 * <p>
 * This implementation defaults to logging all levels of information.
 * </p>
 * <p>
 * Multiple loggers can use the same file for the same configuration. Information is sent to the files asynchronously.
 * </p>
 * <p>
 * This implementation allows for same-line updates; if a line ends with '\r', it is assumed that the caller wants to control its own line breaks, so no
 * additional line separator is added, allowing the implementation of progress bars, for example.
 * </p>
 * @author Garret Wilson
 */
public class DefaultLogger extends AbstractLogger
{

	/**
	 * An object for formatting the date and time. This object is not thread safe and must be synchronized externally; in this implementation it is synchronized
	 * on itself.
	 */
	protected static final DateFormat DATE_FORMAT = new W3CDateFormat(W3CDateFormat.Style.DATE_TIME);

	/** The system line separator characters in use when the class is created. */
	private final static String LINE_SEPARATOR = getLineSeparator();

	/** The levels that should be logged. */
	private Set<Log.Level> levels = unmodifiableSet(EnumSet.allOf(Log.Level.class));

	/**
	 * Returns the levels that should be logged. Defaults to all available levels.
	 * @return The levels that will be logged.
	 */
	public Set<Log.Level> getLevels()
	{
		return levels;
	}

	/**
	 * Sets the report levels that will be logged.
	 * @param levels The levels that will be logged.
	 * @throws NullPointerException if the given levels is <code>null</code>.
	 */
	public void setLevels(final Set<Log.Level> levels)
	{
		this.levels = unmodifiableSet(EnumSet.copyOf(checkInstance(levels, "Levels cannot be null.")));
	}

	/**
	 * Sets the level of information that will be logged. Any log information at or above the given level will be logged.
	 * @param minimumLevel The minimum level that will be logged.
	 * @throws NullPointerException if the given level is <code>null</code>.
	 * @see #setLevels(Set)
	 */
	public void setLevel(final Log.Level minimumLevel)
	{
		final int minimumOrdinal = minimumLevel.ordinal(); //get the ordinal of the minimum level
		final Set<Log.Level> levels = EnumSet.of(minimumLevel); //create a set with the minimum level
		for(final Log.Level level : Log.Level.values()) //for all available levels
		{
			if(level.ordinal() > minimumOrdinal) //if this level is higher than the minimum
			{
				levels.add(level); //add this level to the set as well
			}
		}
		setLevels(levels); //set the levels
	}

	/** The information that should be reported with each log. */
	private Set<Log.Report> report = unmodifiableSet(EnumSet.allOf(Log.Report.class));

	/**
	 * Returns the log information reported. Defaults to all report options.
	 * @return The information that will be reported with each log.
	 */
	public Set<Log.Report> getReport()
	{
		return report;
	}

	/**
	 * Sets the type of information that should be reported with each log.
	 * @param report The type of information to report.
	 * @throws NullPointerException if the given report is <code>null</code>.
	 */
	public void setReport(final Set<Log.Report> report)
	{
		this.report = unmodifiableSet(EnumSet.copyOf(checkInstance(report, "Report cannot be null.")));
	}

	/** The file being used for logging, or <code>null</code> if no file is being used. */
	private File file = null;

	/**
	 * The writer used to log information, or <code>null</code> if there is no writer being used to log information or the writer has not yet been created for a
	 * file.
	 */
	private Writer writer = null;

	/** Whether information should be sent to {@link System#out} or {@link System#err} as appropriate, in addition to other destinations, if any. */
	private boolean standardOutput;

	/** @return Whether information should be sent to {@link System#out} or {@link System#err} as appropriate, in addition to other destinations, if any. */
	public boolean isStandardOutput()
	{
		return standardOutput;
	}

	/**
	 * Sets whether information should be sent to {@link System#out} or {@link System#err} as appropriate, in addition to other destinations, if any.
	 * @param standardOutput Whether information should be sent to the standard output.
	 */
	public void setStandardOutput(final boolean standardOutput)
	{
		this.standardOutput = standardOutput;
	}

	/** The owner log configuration; serves as a factor for new writers. */
	private final DefaultLogConfiguration logConfiguration;

	/** @return The owner log configuration; serves as a factor for new writers. */
	public DefaultLogConfiguration getLogConfiguration()
	{
		return logConfiguration;
	}

	/**
	 * Log configuration constructor. Information will be logged to the standard output {@link System#out}.
	 */
	public DefaultLogger(final DefaultLogConfiguration logConfiguration)
	{
		this(logConfiguration, (File)null, true); //don't log to a file, but log to stdout
	}

	/**
	 * File constructor with no output to {@link System#out} or {@link System#err}.
	 * @param file The file to use for logging, or <code>null</code> if no file logging should be used.
	 */
	public DefaultLogger(final DefaultLogConfiguration logConfiguration, final File file)
	{
		this(logConfiguration, file, false);
	}

	/**
	 * File and log system out constructor.
	 * @param file The file to use for logging, or <code>null</code> if no file logging should be used.
	 * @param standardOutput Whether information should also be sent to {@link System#out} or {@link System#err} as appropriate.
	 */
	public DefaultLogger(final DefaultLogConfiguration logConfiguration, final File file, final boolean standardOutput)
	{
		this.logConfiguration = checkInstance(logConfiguration, "Log configuration must be provided.");
		this.file = file; //save the file, if any
		this.standardOutput = standardOutput;
	}

	/**
	 * Writer constructor with no output to {@link System#out} or {@link System#err}.
	 * @param writer The writer to use for logging.
	 * @throws NullPointerException if the given writer is <code>null</code>.
	 */
	public DefaultLogger(final DefaultLogConfiguration logConfiguration, final Writer writer)
	{
		this(logConfiguration, writer, false);
	}

	/**
	 * Writer and log system out constructor.
	 * @param writer The writer to use for logging.
	 * @param standardOutput Whether information should also be sent to {@link System#out} or {@link System#err} as appropriate.
	 * @throws NullPointerException if the given writer is <code>null</code>.
	 */
	public DefaultLogger(final DefaultLogConfiguration logConfiguration, final Writer writer, final boolean standardOutput)
	{
		this(logConfiguration, (File)null, standardOutput); //construct the class with no file 
		this.writer = checkInstance(writer, "Writer cannot be null.");
	}

	/**
	 * Returns the writer, if any, used to log information. This implementation lazily determines and caches the writer if appropriate.
	 * @return The writer used to log information, or <code>null</code> if information should not be logged to a writer.
	 * @throws ConfigurationException if there is an error retrieving the writer.
	 */
	protected Writer getWriter()
	{
		if(writer == null) //if there is no writer configured (we don't need to synchronize here, because the race condition will be resolved when we actually try to get a writer
		{
			if(file != null) //if we have a file, we need to lazily retrieve a writer
			{
				try
				{
					writer = getLogConfiguration().getWriter(file); //get a writer for this file
				}
				catch(final IOException ioException)
				{
					throw new ConfigurationException("Unable to create log writer to file " + file, ioException);
				}
			}
		}
		return writer; //return the writer
	}

	/**
	 * Appends a stack trace to a string builder, including any recursive causes.
	 * @param stringBuilder The string builder to retrieve the stack trace.
	 * @param throwable The source of the stack trace, not including any source inside this class.
	 * @return The string builder into which the stack trace was written.
	 */
	public static StringBuilder appendStackTrace(final StringBuilder stringBuilder, final Throwable throwable)
	{
		stringBuilder.append(throwable).append('\n'); //append the throwable itself
		final StackTraceElement[] stack = throwable.getStackTrace(); //get the trace of the stack
		for(final StackTraceElement stackTraceElement : stack) //for each element on the stack
		{
			if(!DefaultLogger.class.getName().equals(stackTraceElement.getClassName())) //if this location was not inside this class
			{
				stringBuilder.append('\t').append(stackTraceElement).append('\n'); //append the stack trace element
			}
		}
		final Throwable cause = throwable.getCause(); //get the cause of this throwable
		if(cause != null) //if there is a cause
		{
			stringBuilder.append("Cause:"); //Cause:
			appendStackTrace(stringBuilder, cause); //recursively append the cause
		}
		return stringBuilder; //return the string buffer, now with the new information
	}

	/**
	 * Writes a log message to all previously specified locations, such as to the standard output, to the visible log, to disk, etc.
	 * @param level The reporting level of the log entry.
	 * @param objects The objects to write; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	 */
	private void write(final Log.Level level, final Object... objects)
	{
		final Set<Log.Report> report = getReport(); //get the items we should report
		//construct a string with time level : [thread] location : objects
		final StringBuilder stringBuilder = new StringBuilder(); //create a string builder for constructing our log output
		if(report.contains(Log.Report.LEVEL)) //if we should report the level
		{
			stringBuilder.append(level).append(' ').append(':'); //level:
		}
		if(report.contains(Log.Report.TIME)) //if we should report the time
		{
			stringBuilder.append(' '); //append a space
			final Date now = new Date(); //get the current date and time
			final String dateTimeString; //we'll format a date+time string separately, so as to narrow the number of operations we have to synchronize
			synchronized(DATE_FORMAT) //ensure only one thread accesses the date format at a time, because the object is not thread-safe
			{
				dateTimeString = DATE_FORMAT.format(now); //format the current date and time
			}
			stringBuilder.append(dateTimeString); //append the date+time string to the string builder
		}
		if(report.contains(Log.Report.THREAD)) //if we should report the thread
		{
			stringBuilder.append(' '); //append a space
			stringBuilder.append('['); //append a left bracket
			stringBuilder.append(Thread.currentThread().getName()); //append the thread name
			stringBuilder.append(']'); //append a right bracket
		}
		if(report.contains(Log.Report.LOCATION)) //if we should report the program location
		{
			stringBuilder.append(' '); //append a space
			stringBuilder.append(getCallingClassStackTraceElement(Log.class)); //append the program location (ignoring the Log class, which might have been used for its convenience methods)
		}
		int prefaceLength = stringBuilder.length(); //find out how much the preface information is, in case we want to remove it later
		if(objects.length > 0) //if there are objects to output
		{
			stringBuilder.append(' '); //append a space
			stringBuilder.append(':'); //append a colon
			prefaceLength = stringBuilder.length() + 1; //for objects, we add additional preface characters (including the first space we add below)
			for(final Object object : objects) //for each object
			{
				stringBuilder.append(' '); //append a space
				if(object instanceof Throwable) //if this is a throwable object
				{
					appendStackTrace(stringBuilder, (Throwable)object); //append a stack trace
				}
				else
				//if this is not a throwable object
				{
					stringBuilder.append(object); //append this object's string value with a separator
				}
			}
		}
		if(endsWith(stringBuilder, CARRIAGE_RETURN_CHAR)) //if the line ends with '\r', it means the caller wants to control its own line breaks---and do without the preface
		{
			stringBuilder.delete(0, prefaceLength); //remove the preface altogether
		}
		else
		//if the line doesn't end with '\r', log it normally
		{
			stringBuilder.append(LINE_SEPARATOR); //append the end-of-line character(s)
		}
		final String logString = stringBuilder.toString(); //get the text we constructed
		try
		{
			final Writer writer = getWriter(); //get our writer, if any
			if(writer != null) //if we have a writer
			{
				writer.write(logString); //send the text using the writer
				writer.flush(); //make sure the text is flushed to the writer
			}
		}
		catch(final IOException ioException) //if there is a problem writing the log information
		{
			System.err.println(ioException); //write the error to the system error output
		}
		if(isStandardOutput()) //if we should also send information to the standard output
		{
			if(level == Log.Level.ERROR) //if this is an error message
			{
				System.err.print(logString);
			}
			else
			//if this is any other type of message
			{
				System.out.print(logString);
			}
		}
	}

	/**
	 * Outputs a series of objects to the standard output if the given log level is satisfied.
	 * @param level The log level requested.
	 * @param objects The objects to output; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	 */
	protected void output(final Log.Level level, final Object... objects)
	{
		if(getLevels().contains(level)) //if this level is requested
		{
			write(level, objects); //write the trace information
		}
	}

	/**
	 * {@inheritDoc} This implementation generates a stack trace if an object is an instance of {@link Throwable}.
	 */
	public void trace(final Object... objects)
	{
		output(Log.Level.TRACE, objects);
	}

	/**
	 * {@inheritDoc} This implementation generates a stack trace if an object is an instance of {@link Throwable}.
	 */
	public void traceStack(final Object... objects)
	{
		if(objects.length > 0)
		{
			trace(objects); //trace the information
		}
		trace(new Throwable()); //write a stack trace
	}

	/**
	 * {@inheritDoc} This implementation generates a stack trace if an object is an instance of {@link Throwable}.
	 */
	public void debug(final Object... objects)
	{
		output(Log.Level.DEBUG, objects);
	}

	/**
	 * {@inheritDoc} This implementation generates a stack trace if an object is an instance of {@link Throwable}.
	 */
	public void info(final Object... objects)
	{
		output(Log.Level.INFO, objects);
	}

	/**
	 * {@inheritDoc} This implementation generates a stack trace if an object is an instance of {@link Throwable}.
	 */
	public void warn(final Object... objects)
	{
		output(Log.Level.WARN, objects);
	}

	/**
	 * {@inheritDoc} This implementation generates a stack trace if an object is an instance of {@link Throwable}.
	 */
	public void error(final Object... objects)
	{
		output(Log.Level.ERROR, objects);
	}

}