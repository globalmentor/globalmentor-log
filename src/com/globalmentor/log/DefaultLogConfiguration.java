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

import java.io.*;
import java.util.*;
import static java.util.Collections.*;

import static com.globalmentor.java.Objects.*;

/**The configuration for default logging.
<p>The configuration will be used for creating new loggers; changing the configuration
will not affect previously created loggers.</p>
<p>This implementation allows the user of a common logger.</p>
@author Garret Wilson
@see DefaultLogger
*/
public class DefaultLogConfiguration extends AbstractAffiliationLogConfiguration
{

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

		/**Sets the level of log information that will be logged.
		Any log information at or above the given level will be logged.
		@param minimumLevel The minimum level that will be logged.
		@throws NullPointerException if the given level is <code>null</code>.
		@see #setLevels(Set)
		*/
		public void setLevel(final Log.Level minimumLevel)
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
		@param report The type of information to report.
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
		super(true);	//allow the use of a common logger
		this.file=file;
		setStandardOutput(file!=null);	//by default turn off logging to the standard output if a writer was given
	}

	/**Writer constructor.
	If a writer is given, logging to the standard output will default to disabled.
	@param writer The writer to be used to log information, or <code>null</code> if there is no writer to be used to log information.
	*/
	public DefaultLogConfiguration(final Writer writer)
	{
		super(true);	//allow the use of a common logger
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
	@throws NullPointerException if the given level is <code>null</code>.
	*/
	public DefaultLogConfiguration(final File file, final Log.Level minimumLevel)
	{
		this(file);
		setLevel(minimumLevel);
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
	@throws NullPointerException if the given level is <code>null</code>.
	*/
	public DefaultLogConfiguration(final Writer writer, final Log.Level minimumLevel)
	{
		this(writer);
		setLevel(minimumLevel);
	}

	/**File, levels, and report constructor.
	If a file is given, logging to the standard output will default to disabled.
	@param file The file to be used for logging, or <code>null</code> if no file is to be used.
	@param levels The levels that will be logged.
	@param report The type of information to report.
	@throws NullPointerException if the given levels and/or report is <code>null</code>.
	*/
	public DefaultLogConfiguration(final File file, final Set<Log.Level> levels, final Set<Log.Report> report)
	{
		this(file);
		setLevels(levels);
		setReport(report);
	}
	
	/**File, minimum level, and report constructor.
	If a file is given, logging to the standard output will default to disabled.
	@param file The file to be used for logging, or <code>null</code> if no file is to be used.
	@param minimumLevel The minimum level that will be logged.
	@param report The type of information to report.
	@throws NullPointerException if the given level and/or report is <code>null</code>.
	*/
	public DefaultLogConfiguration(final File file, final Log.Level minimumLevel, final Set<Log.Report> report)
	{
		this(file);
		setLevel(minimumLevel);
		setReport(report);
	}

	/**Writer, levels, and report constructor.
	If a writer is given, logging to the standard output will default to disabled.
	@param writer The writer to be used to log information, or <code>null</code> if there is no writer to be used to log information.
	@param levels The levels that will be logged.
	@param report The type of information to report.
	@throws NullPointerException if the given levels and/or report is <code>null</code>.
	*/
	public DefaultLogConfiguration(final Writer writer, final Set<Log.Level> levels, final Set<Log.Report> report)
	{
		this(writer);
		setLevels(levels);
		setReport(report);
	}
	
	/**Writer, minimum level, and report constructor.
	If a writer is given, logging to the standard output will default to disabled.
	@param writer The writer to be used to log information, or <code>null</code> if there is no writer to be used to log information.
	@param minimumLevel The minimum level that will be logged.
	@param report The type of information to report.
	@throws NullPointerException if the given level and/or report is <code>null</code>.
	*/
	public DefaultLogConfiguration(final Writer writer, final Log.Level minimumLevel, final Set<Log.Report> report)
	{
		this(writer);
		setLevel(minimumLevel);
		setReport(report);
	}

	/**Creates a new logger for the given class, configured using the current configuration settings.
 	@param objectClass The specific class for which a logger should be returned.
	@return A new logger instance for the given class.
	@see #getFile()
	@see #getWriter()
	@see #isStandardOutput()
	@see #getLevels()
	@see #getReport()
	*/
	public Logger createLogger(final Class<?> objectClass)
	{
		final Writer writer=getWriter();	//see if we have a writer specified
		final DefaultLogger logger=writer!=null ? new DefaultLogger(writer) : new DefaultLogger(getFile());	//configure the logger with a writer or file (the latter of which may be null)
		logger.setStandardOutput(isStandardOutput());	//set whether the standard output should be used
		logger.setLevels(getLevels());	//set the levels to report
		logger.setReport(getReport());	//set the information to report
		return logger;	//return the created and configured logger
	}

}
