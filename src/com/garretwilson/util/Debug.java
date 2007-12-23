package com.garretwilson.util;

import java.io.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.locks.*;

import static java.util.Collections.*;

import com.garretwilson.io.AsynchronousWriter;
import com.garretwilson.io.WriterPrintStream;
import com.garretwilson.lang.ObjectUtilities;

import static com.garretwilson.lang.SystemUtilities.*;
import static com.garretwilson.text.CharacterEncodingConstants.*;

import com.garretwilson.text.W3CDateFormat;

/**Static class that encapsulates debugging functionality.
@author Garret Wilson
*/
public class Debug
{

	/**Private constructor; this class cannot be publicly instantiated.*/
	private Debug() {}

	/**Whether or not debugging is turned on.*/
	private static boolean isDebug=false;

		/**@return Whether or not debugging is turned on.*/
		public static boolean isDebug() {return isDebug;}

		/**Sets whether debugging is turned on.
		@param newDebug Whether debugging should be turned on.
		@exception IOException if when turning debug on or off there is an I/O error updating the debug output.
		*/
		public static synchronized void setDebug(final boolean newDebug) throws IOException
		{
			if(isDebug!=newDebug)	//if the debug status is really changing
			{
				isDebug=newDebug;	//update the debug variable
				updateOutput();	//update our output to coincide with our new debug status
			}
		}

	/**The name of the JFrame class.*/
	private final static String JFRAME_CLASS_NAME="javax.swing.JFrame";

	/**The name of the DebugAWTDisplay class.*/
	private final static String DEBUGAWTDISPLAY_CLASS_NAME="com.garretwilson.awt.DebugAWTDisplay";

	/**The name of the DebugSwingDisplay class.*/
	private final static String DEBUGSWINGDISPLAY_CLASS_NAME="com.garretwilson.swing.DebugSwingDisplay";

	/**The lock for allowing lazy creation and access of the debug display.*/
	private final static Lock debugDisplayLock=new ReentrantLock();
	
	/**The lazily-initialized class used for displaying information in a graphical user interface.*/
	private static DebugDisplay debugDisplay=null;

		/**Returns the class used for displaying information in a graphical user interface.
		This method is thread safe; its synchronization overhead is probably negligible compared to the overhead used in actually writing to the debug display, which is synchronized.
		@return The lazily-initialized class used for displaying information in a graphical user interface.
		*/
		protected static DebugDisplay getDebugDisplay()
		{
			debugDisplayLock.lock();	//acquire the debug display lock
			try
			{
				if(debugDisplay==null)	//if there is no debug display
				{
					try
					{
						try	//see which debug class we can load
						{
							Class.forName(JFRAME_CLASS_NAME); //load the JFrame class, if we can; if so, we have Swing available
							final Class swingDisplayClass=Class.forName(DEBUGSWINGDISPLAY_CLASS_NAME); //load the Swing debug display class
							debugDisplay=(DebugDisplay)swingDisplayClass.newInstance(); //create the display class
						}
						catch(final ClassNotFoundException swingClassNotFoundException) //if a Swing class couldn't be loaded
						{
							final Class awtDisplayClass=Class.forName(DEBUGAWTDISPLAY_CLASS_NAME); //load the AWT debug display class
							debugDisplay=(DebugDisplay)awtDisplayClass.newInstance(); //create the display class
						}
					}
					catch(final ClassNotFoundException classNotFoundException)	//if we can't load a class
					{
						throw new AssertionError(classNotFoundException);
					}
					catch(final IllegalAccessException illegalAccessException)	//if we can't access the display class constructor
					{
						throw new AssertionError(illegalAccessException);
					}
					catch(final InstantiationException instantiationException)	//if we can't create the display object					
					{
						throw new AssertionError(instantiationException);
					}
				}
			}
			finally
			{
				debugDisplayLock.unlock();	//always release the debug lock
			}
			return debugDisplay;	//return our debug display
		}

	/**Whether debugging is visible on the screen; this variable is only updated under synchronization of the debug display, so it is guaranteed to be in synch with the debug display setting.*/
	private static boolean visible=false;

		/**@return Whether or not debugging is visible on the screen, independent of whether debug is turned on.*/
		public static boolean isVisible() {return visible;}

		/**Sets whether debugging output is shown in a separate log window. Has no effect if debugging is not turned on.
		This implementation changes the visible setting synchronizing on the debug display so that it's value will be in synch with the setting of the debug display.
		@param visible Whether debugging should be displayed visibly.
		@see #getDebugDisplay()
		*/
		public static void setVisible(final boolean visible)
		{
			final DebugDisplay debugDisplay=getDebugDisplay();	//get the debug display
			synchronized(debugDisplay)	//keep the debug display setting and the visibility variable in synch
			{
				debugDisplay.setEnabled(visible);	//enable or disable the debug display
				Debug.visible=visible;	//update the local variable to match
			}
		}

	/**An object for formatting the date and time. This object is not thread safe and must be synchronized externally; in this implementation it is synchronized on itself.*/
	protected static final DateFormat dateFormat=new W3CDateFormat(W3CDateFormat.Style.DATE_TIME);

	/**The system line separator characters in use when the class is created.*/
	private final static String LINE_SEPARATOR=getLineSeparator();

	/**The available debug reporting levels.*/
	public enum ReportLevel
	{
		/**Indicates the program's execution path.*/
		TRACE,
		/**Indicates useful information that should nonetheless not be logged.*/
		INFO,
		/**Specific information which should be logged but which are adversity-neutral.*/
		LOG,
		/**Indications that conditions are possibly adverse.*/
		WARN,
		/**Indicates an unexpected condition representing an error.*/
		ERROR
	}; 

	/**The levels that should be reported.*/
	private static Set<ReportLevel> reportLevels=unmodifiableSet(EnumSet.allOf(ReportLevel.class));

		/**Returns the report levels which should actually be logged.
		Defaults to all available levels.
		@return The log levels that will be logged.
		*/
		public static Set<ReportLevel> getReportLevels() {return reportLevels;}
	
		/**Sets the report levels that will actually be logged.
		@param levels The levels that will be logged.
		*/
		public static void setReportLevels(final Set<ReportLevel> levels) {reportLevels=levels;}

	/**The available reporting options.*/
	public enum ReportOption
	{
		/**Indicates that the error level should be reported.*/
		LEVEL,	
		/**Indicates that execution time should be reported.*/
		TIME,
		/**Indicates that the thread name should be reported.*/
		THREAD,
		/**Indicates that the location of program execution should be reported.*/
		LOCATION
	}

	/**The information that should be reported with each log.*/
	private static Set<ReportOption> reportOptions=unmodifiableSet(EnumSet.allOf(ReportOption.class));

		/**Returns the debug information reported.
		Defaults to all report options.
		@return The debug information that will be reported with each log.
		*/
		public static Set<ReportOption> getReportOptions() {return reportOptions;}
	
		/**Sets the type of information that should be reported with each log.
		@param options The information to be reported on.
		*/
		public static void setReportOptions(final Set<ReportOption> options) {reportOptions=options;}

	/**The writer used to output debug messages. This stream is also used as the output stream if a debug file is specified.
	This defaults to a writer to {@link System#out} using the system default character encoding, because the system out stream expects the current encoding.
	@see #setOutput(java.io.File)
	@see #setOutput(java.io.PrintStream)
	*/
	private static Writer debugWriter=new OutputStreamWriter(System.out);	//don't wrap this in an AsynchronousWriter; there's no need for it, if we're doing console output, and sending asynchronous output to System.out seems to randomly fail, halting the application and giving no error message

	/**The file being used for debug output, or <code>null</code> if no file is being used.
	@see #setOutput(java.io.PrintStream)
	*/
	private static File debugFile=null;

	/**Sets the error output of the application. If any error occur, they are reported to the standard error output.
	@param errorPrintStream The new stream to use for error output; can be <code>System.err</code> for the default system error output.
	*/
	public static void setErr(final PrintStream errorPrintStream)
	{
		try
		{
			System.setErr(errorPrintStream);	//change the error output print stream
		}
		catch(Exception exception)
		{
			System.err.println(exception.getMessage()); //write any errors to the standard error output
		}
	}

	/**@return The writer used to output debug messages.*/
	public static Writer getOutput()
	{
		return debugWriter;	//return the debug writer
	}

	/**Sets the stream used to output debug messages when debug is turned on.
	The default before this function is called is a writer to {@link System#out}.
	@param writer The writer to use to print debug messages.
	@exception IOException if there is an I/O error updating the debug output.
	@see #isDebug()
	@see #setOutput(java.io.File)
	*/
	public static synchronized void setOutput(final Writer writer) throws IOException
	{
		debugWriter=writer;	//save the writer they pass
		updateOutput();	//update our output
	}

	/**@return The output file if debugging is turned on and debug output has been redirected to a file, or <code>null</code> if there is no file assigned or debugging is turned off.
	@see #isDebug()
	@see #setOutput(java.io.File)
	*/
	public static synchronized File getOutputFile()
	{
		return isDebug() ? debugFile : null;	//return the file if we're debugging, null if not
	}

	/**Redirects debug output to a specific file when debug is turned on.
	If the file isn't changing no action occurs.
	@param file The file in which debug information should be stored, or <code>null</code> to revert back to the standard output.
	@exception IOException if there is an I/O exception closing the current writer.
	@exception FileNotFoundException if the specified file is not found.
	@see #isDebug()
	@see #setOutput(java.io.PrintStream)
	*/
	public static synchronized void setOutput(final File file) throws IOException, FileNotFoundException
	{
		if(!ObjectUtilities.equals(debugFile, file))	//if the file is really changing
		{
			if(debugFile!=null)	//if they had a file open already
			{
				debugWriter.close();	//close the stream to the existing file
			}
			debugFile=file;	//show which file we'll use for debugging
			if(isDebug())	//if debugging is turned on
			{
				updateOutput();	//update our output
			}
		}
	}

	
	/**Updates the debug output, based upon whether debug is turned on or off.
	@exception IOException if there is an I/O error updating the debug output.
	*/
	protected static synchronized void updateOutput() throws IOException
	{
		if(isDebug())	//if debugging is turned on
		{
			if(debugFile!=null)	//if we have a debug file
			{
				try
				{
					final OutputStream outputStream=new FileOutputStream(debugFile, true);	//create an output stream
					outputStream.write(BOM_UTF_8);	//write the UTF-8 byte order mark
					debugWriter=new AsynchronousWriter(new OutputStreamWriter(new BufferedOutputStream(outputStream), UTF_8));	//open an asynchronous, buffered writer for appending to the file in UTF-8 (open this first so that any error will not affect our current status)
				}
				catch(final FileNotFoundException fileNotFoundException)	//if we can't open an output stream to the file
				{
					System.err.println(fileNotFoundException);	//indicate an error TODO send this to the existing error stream, if we can
				}
			}
			if(debugWriter!=null)	//if a debug print stream is given
			{
				setErr(new WriterPrintStream(debugWriter));	//redirect the error output to let all errors go to the debug output as well; wrap the writer we already have (which is thread safe itself) with a print stream adapter (which is also thread safe)
			}
		}
		else	//if debug is turned off
		{
			if(debugFile!=null)	//if we have a debug file
			{
				debugWriter.close();	//close the debug output stream
			}
			setErr(System.err);	//set error output back to its default
		}
	}
	
	/**Returns the report levels for which the user should by notified, which will
		be one or more of the <code>_LEVEL</code> constants ORed together. The
		default is <code>ERROR_LEVEL</code>.
	@return The log levels that will cause a notification.
	@see #notifyLevel
	*/
/*TODO fix
	public static int getNotifyLevel()
	{
		return getDebug().notifyLevel; //return the notify level variable
	}
*/

	/**Sets the log levels that will cause a notification to be given to the user.
	@param newNotifyLevel The levels that will notify the user, one or more of the
		<code>_LEVEL</code> constants ORed together.
	@see #notifyLevel
	*/
/*TODO fix
	public static void setNotifyLevel(final int newNotifyLevel)
	{
		getDebug().notifyLevel=newNotifyLevel;  //update the notify level variable
	}
*/

	/**Sets the minimum report levels that will actually be logged.
	@param minimumLevel The minimum level that will be logged.
	@see #setReportLevels(Set)
	*/
	public static void setMinimumReportLevel(final ReportLevel minimumLevel)
	{
		final int minimumOrdinal=minimumLevel.ordinal();	//get the ordinal of the minimum level
		final Set<ReportLevel> levels=EnumSet.of(minimumLevel);	//create a set with the minimum level
		for(final ReportLevel level:ReportLevel.values())	//for all available report levels
		{
			if(level.ordinal()>minimumOrdinal)	//if this level is higher than the minimum
			{
				levels.add(level);	//add this level to the set as well
			}
		}
		setReportLevels(levels);	//set the report levels
	}

	/**Appends a stack trace to a string builder, including any recursive causes.
	@param stringBuilder The string builder to retrieve the stack trace.
	@param throwable The source of the stack trace, not including any source inside this class.
	@return The string builder into which the stack trace was written.
	*/
	public static StringBuilder appendStackTrace(final StringBuilder stringBuilder, final Throwable throwable)
	{
		stringBuilder.append(throwable).append('\n');	//append the throwable itself
		final StackTraceElement[] stack=throwable.getStackTrace();	//get the trace of the stack
		for(final StackTraceElement stackTraceElement:stack)	//for each element on the stack
		{
			if(!Debug.class.getName().equals(stackTraceElement.getClassName()))	//if this location was not inside this class
			{
				stringBuilder.append('\t').append(stackTraceElement).append('\n');	//append the stack trace element
			}
		}
		final Throwable cause=throwable.getCause();	//get the cause of this throwable
		if(cause!=null)	//if there is a cause
		{
			stringBuilder.append("Cause:");	//Cause:
			appendStackTrace(stringBuilder, cause);	//recursively append the cause
		}
		return stringBuilder;	//return the string buffer, now with the new information
	}

	/**Writes a debug message to all previously specified locations, such as
		to the standard output, to the visible log, to disk, etc.
	@param level The reporting level of the log entry.
	@param objects The objects to output. If an object is an instance of <code>Throwable</code>,
		a stack trace will be generated.
	@see #isVisible()
	@see #setVisible(boolean)
	*/
	private static void write(final ReportLevel level, final Object... objects)
	{
		final Set<ReportOption> options=reportOptions;  //get the items we should report
			//construct a string with time level : [thread] location : objects
		final StringBuilder stringBuilder=new StringBuilder();  //create a string builder for constructing our log output
		if(options.contains(ReportOption.LEVEL))	//if we should report the report level
		{
			stringBuilder.append(level).append(' ').append(':');	//level:
		}
		if(options.contains(ReportOption.TIME))  //if we should report the time
		{
		  stringBuilder.append(' ');  //append a space
		  final Date now=new Date();	//get the current date and time
		  final String dateTimeString;	//we'll format a date+time string separately, so as to narrow the number of operations we have to synchronize
		  synchronized(dateFormat)	//ensure only one thread accesses the date format at a time, because the object is not thread-safe
		  {
		  	dateTimeString=dateFormat.format(now);	//format the current date and time
		  }
		  stringBuilder.append(dateTimeString);	//append the date+time string to the string builder
		}
		if(options.contains(ReportOption.THREAD))  //if we should report the thread
		{
		  stringBuilder.append(' ');  //append a space
		  stringBuilder.append('[');  //append a left bracket
			stringBuilder.append(Thread.currentThread().getName()); //append the thread name
		  stringBuilder.append(']');  //append a right bracket
		}
		if(options.contains(ReportOption.LOCATION))  //if we should report the program location
		{
		  stringBuilder.append(' ');  //append a space
			stringBuilder.append(getProgramLocation()); //append the program location
		}
		if(objects.length>0)	//if there are objects to output
		{			
			stringBuilder.append(' ');  //append a space
			stringBuilder.append(':');  //append a colon
			for(final Object object:objects)	//for each object
			{
				stringBuilder.append(' ');  //append a space
				if(object instanceof Throwable)	//if this is a throwable object
				{
					appendStackTrace(stringBuilder, (Throwable)object);	//append a stack trace
				}
				else	//if this is not a throwable object
				{
					stringBuilder.append(object);	//append this object's string value with a separator
				}
			}
		}
		stringBuilder.append(LINE_SEPARATOR);	//append the end-of-line character(s)
		final String logString=stringBuilder.toString();  //get the text we constructed
		try
		{
			debugWriter.write(logString);	//send the text using the debug output writer
			debugWriter.flush(); //make sure the text is flushed to the output			
		}
		catch(final IOException ioException)	//if there is a problem writing the debug information
		{
			System.err.println(ioException);	//write the error to the system error output
		}
		if(isVisible())	//if we have visual debugging as well (this method isn't thread-aware, so there is a benign race condition that in the worse case will allow continuing logging for a while after visibility is turned off)
		{
		  getDebugDisplay().trace(logString);	//show the text visually; this method will block
		}
	}

	/**Outputs a series of objects to the standard output if debugging is enabled and the given report level is satisfied.
	Errors are considered so crucial that they are sent to the standard error output even if debugging is turned off.
	@param reportLevel The report level requested.
	@param objects The objects to output. If an object is an instance of <code>Throwable</code>, a stack trace will be generated.
	*/
	public static void output(final ReportLevel reportLevel, final Object... objects)
	{
		if(getReportLevels().contains(reportLevel))	//if this report level is requested
		{
			if(isDebug())	//if debug is turned on
			{
				write(reportLevel, objects);	//write the trace information
			}
			else if(reportLevel==ReportLevel.ERROR)	//if this is an error, send information to standard output
			{
				boolean foundThrowable=false;	//we'll see if a throwable object was passed
				for(final Object object:objects)	//look at each object
				{
					if(object instanceof Throwable)	//if this is a throwable object
					{
						foundThrowable=true;	//show that we found a throwable object
						((Throwable)object).printStackTrace(System.err);  //send a stack trace to the standard error output
					}
					else	//if this is not a throwable object
					{
						System.err.println(object);	//send the object to the standard error output
					}
				}
				if(!foundThrowable)	//if no throwable object was found TODO do this for debugging, too
				{
					new Throwable().printStackTrace(System.err);  //send a stack trace to the standard error output					
				}				
			}
		}
	}

	/**Outputs a series of objects to the standard output if debugging is enabled.
		Meant for messages that show the path of program execution.
	<p>If no objects are provided, only the trace location will be output.</p>
	@param objects The objects to output. If an object is an instance of <code>Throwable</code>,
		a stack trace will be generated.
	@see ReportLevel#TRACE
	*/
	public static void trace(final Object... objects)
	{
		output(ReportLevel.TRACE, objects);	//output the trace information
	}

	/**Outputs a stack trace and a series of objects to the standard output if debugging is enabled.
		Meant for messages that show the path of program execution.
	<p>If no objects are provided, only the trace location will be output.</p>
	@param objects The objects to output. If an object is an instance of <code>Throwable</code>,
		a stack trace will be generated.
	@see ReportLevel#TRACE
	*/
	public static void traceStack(final Object... objects)
	{
		trace(objects);	//trace the information
		trace(new Throwable());	//write a stack trace
	}

	/**Outputs a formatted message to the debug output if debugging is enabled.
		Meant for messages that show the path of program execution.
		Uses the same formatting as does <code>MessageFormat</code>.
		This method is preferred to the one-argument version of <code>trace()</code>
		if the value of multiple objects are being displayed, because
		<code>MessageFormat.format()</code> will only be called and the values
		converted to strings if debugging is enabled.
	@param pattern The message format pattern.
	@param arguments The object arguments which will be used with the pattern to
		generate a formatted trace string.
	@see MessageFormat
	@see #getDebug
	@see #isDebug
	*/
/*TODO del or fix
	public static void trace(final String pattern, final Object[] arguments)
	{
		if(isDebug() && (getReportLevel() & TRACE_LEVEL)>0)	//if debugging is turned on
			write(MessageFormat.format(pattern, arguments));	//format the string and trace the result
	}
*/

	/**Outputs the stack trace of a particular error or exception object if
		debugging is enabled.
		This represents informational data, not an error condition.
	@param throwable The object which contains the stack information.
	@see #getDebug
	@see #isDebug
	*/
/*G***del
	public static void traceStack(final Throwable throwable)
	{
		if(isDebug() && (getReportLevel() & TRACE_LEVEL)>0)	//if debugging is turned on
		{
			trace(getStackTrace(throwable));	//send the text from a stack strace of the object as debug trace output
		}
	}
*/

	/**Outputs a series of objects to the standard output if debugging is enabled.
	Meant for useful information that should not be logged.
	<p>If no objects are provided, only the trace location will be output.</p>
	@param objects The objects to output. If an object is an instance of <code>Throwable</code>,
		a stack trace will be generated.
	@see ReportLevel#INFO
	*/
	public static void info(final Object... objects)
	{
		output(ReportLevel.INFO, objects);	//output the info information
	}

	/**Outputs a series of objects to the standard output if debugging is enabled.
	Meant for specific information which should be logged but which are adversity-neutral.
	<p>If no objects are provided, only the trace location will be output.</p>
	@param objects The objects to output. If an object is an instance of <code>Throwable</code>,
		a stack trace will be generated.
	@see ReportLevel#LOG
	*/
	public static void log(final Object... objects)
	{
		output(ReportLevel.LOG, objects);	//output the log information
	}

	/**Outputs a series of objects to the standard output if debugging is enabled.
	Meant for errors that should not prevent the robust functioning of the program
		and that are expected to occur infrequently and not because of program design.
	<p>If no objects are provided, only the trace location will be output.</p>
	@param objects The objects to output. If an object is an instance of <code>Throwable</code>,
		a stack trace will be generated.
	@see ReportLevel#WARN
	*/
	public static void warn(final Object... objects)
	{
		output(ReportLevel.WARN, objects);	//output the warn information
	}

	/**Outputs a series of objects to the standard output if debugging is enabled.
	Meant for errors that are not expected to occur during normal program operations
		 -- program logic errors, and exceptions that are not expected to be thrown.
	Errors are considered so crucial that they are sent to the standard error
		output even if debugging is turned off.
	<p>If no objects are provided, only the trace location will be output.</p>
	@param objects The objects to output. If an object is an instance of <code>Throwable</code>,
		a stack trace will be generated.
	@see ReportLevel#ERROR
	*/
	public static void error(final Object... objects)
	{
		output(ReportLevel.ERROR, objects);	//output the error information
	}

	/**Unconditionally displays a message dialog with the given message, whether
		or not debugging is enabled. If debugging is enabled, the message is traced
		as well.
		This method is meant to be a convenience routine for showing a notification
		dialog during testing, not for normal debug reporting.
	@param message The message to display
	@see #trace
	*/
	public static void notify(final String message)
	{
		trace(message);	//trace the message
		getDebugDisplay().notify(message);  //let the debug display object show the message
	}

	/**Returns a stack trace element of the last line of program execution before
		a {@link Debug} method was entered.
	@return A stack trace element representing the last line of program execution before a {@link Debug} method was entered.
	*/
	public static StackTraceElement getProgramLocation()
	{
		final StackTraceElement[] stack=new Throwable().getStackTrace();	//get the current stack
		for(final StackTraceElement stackTraceElement:stack)	//look at each item on the stack
		{
			if(!Debug.class.getName().equals(stackTraceElement.getClassName()))	//if this location was not inside this class
			{
				return stackTraceElement;	//return this location
			}
		}
		throw new AssertionError("Could not find non-debug program location.");	//this should never happen, because program execution never starts inside this class
	}

}