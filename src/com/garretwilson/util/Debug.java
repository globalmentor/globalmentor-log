package com.garretwilson.util;

import java.io.*;
import java.text.*;
import java.util.*;

import static java.util.Collections.*;

import com.garretwilson.lang.ObjectUtilities;

/**Singleton class which encapsulates debugging functionality.
@author Garret Wilson
*/
public class Debug
{

//G***del	private final static boolean USE_SWING=false; //G***fix

	/**The single copy of the debug class that's allowed to be created.*/
	private static Debug debug=null;

	/**Whether or not debugging is turned on.*/
	private boolean isDebug=false;

	/**Whether or not Swing is available.*/
	private boolean isSwingAvailable=false;

	/**The name of the JFrame class.*/
	private final static String JFRAME_CLASS_NAME="javax.swing.JFrame";

	/**The name of the DebugAWTDisplay class.*/
	private final static String DEBUGAWTDISPLAY_CLASS_NAME="com.garretwilson.awt.DebugAWTDisplay";

	/**The name of the DebugSwingDisplay class.*/
	private final static String DEBUGSWINGDISPLAY_CLASS_NAME="com.garretwilson.swing.DebugSwingDisplay";

	/**The class used for displaying information in a graphical user interface.*/
	private DebugDisplay debugDisplay=null;

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
	
	/**Indicates all reporting levels.*/
	public final static Set<ReportLevel> ALL_REPORT_LEVELS=unmodifiableSet(EnumSet.of(ReportLevel.TRACE, ReportLevel.INFO, ReportLevel.LOG, ReportLevel.WARN, ReportLevel.ERROR));

	/**The levels that should notify the user.*/
//G***del	private int notifyLevel=ERROR_LEVEL;

	/**The levels that should be reported.*/
	private Set<ReportLevel> reportLevels=unmodifiableSet(EnumSet.of(ReportLevel.TRACE, ReportLevel.INFO, ReportLevel.LOG, ReportLevel.WARN, ReportLevel.ERROR));

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

	/**Indicates that all information should be reported.*/
//G***del	public final static long REPORT_ALL=REPORT_LEVEL|REPORT_TIME|REPORT_THREAD|REPORT_LOCATION;

	/**The information that should be reported with each log.*/
	private Set<ReportOption> reportOptions=unmodifiableSet(EnumSet.of(ReportOption.LEVEL, ReportOption.TIME, ReportOption.THREAD, ReportOption.LOCATION));

	/**The stream used to output debug messages. This stream is also used as the
		output stream if a debuf file is specified.
	@see #setOutput(java.io.File)
	@see #setOutput(java.io.PrintStream)
	@see System#out
	*/
	private PrintStream debugPrintStream=System.out;

	/**The file being used for debug output, or <code>null</code> if no file is
		being used.
	@see #setOutput(java.io.PrintStream)
	*/
	private File debugFile=null;

	/**Sets the error output of the application. If any error occur, they are
		reported to the standard error output.
	@param errorPrintStream The new stream to use for error output; can be
		<code>System.err</code> for the default system error output.
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

	/**@return The print stream used to output debug messages.*/
	public static PrintStream getOutput()
	{
		return getDebug().debugPrintStream;	//return the debug print stream
	}

	/**Sets the stream used to output debug messages when debug is turned on.
		The default before this function is called is <code>System.out</code>.
	@param printStream The print stream to use to print debug messages.
	@see #isDebug
	@see #setOutput(java.io.File)
	*/
	public static synchronized void setOutput(final PrintStream printStream)
	{
		final Debug debug=getDebug();	//get debug support
		debug.debugPrintStream=printStream;	//save the print stream they pass
		if(isDebug())	//if debugging is turned on
		{
			debug.updateOutput();	//update our output
		}
	}

	/**@return The output file if debugging is turned on and debug output has been
		redirected to a file, or <code>null</code> if there is no file assigned or
		debugging is turned off.
	@see #isDebug
	@see #setOutput(java.io.File)
	*/
	public static synchronized File getOutputFile()
	{
		return isDebug() ? getDebug().debugFile : null;	//return the file if we're debugging, null if not
	}

	/**Redirects debug output to a specific file when debug is turned on.
	If the file isn't changing no action occurs.
	@param file The file in which debug information should be stored, or <code>null</code> to revert back to the standard output.
	@exception FileNotFoundException Thrown if the specified file is not found
	@see #isDebug
	@see #setOutput(java.io.PrintStream)
	*/
	public static synchronized void setOutput(final File file) throws FileNotFoundException
	{
		final Debug debug=getDebug();	//get debug support
		if(!ObjectUtilities.equals(debug.debugFile, file))	//if the file is really changing
		{
			if(debug.debugFile!=null)	//if they had a file open already
			{
				debug.debugPrintStream.close();	//close the stream to the existing file
			}
			debug.debugFile=file;	//show which file we'll use for debugging
			if(isDebug())	//if debugging is turned on
			{
				debug.updateOutput();	//update our output
			}
		}
	}

	
	/**Updates the debug output, based upon whether debug is turned on or off.*/
	protected void updateOutput()
	{
		if(isDebug)	//if debugging is turned on
		{
			if(debugFile!=null)	//if we have a debug file
			{
				try
				{
					debugPrintStream=new PrintStream(new FileOutputStream(debugFile, true));	//open an output stream for appending to the file (open this first so that any error will not affect our current status)
				}
				catch(final FileNotFoundException fileNotFoundException)	//if we can't open an output stream to the file
				{
					System.err.println(fileNotFoundException);	//indicate an error TODO send this to the existing error stream, if we can
				}
			}
			if(debugPrintStream!=null)	//if a debug print stream is given
			{
				setErr(debugPrintStream);	//redirect the error output to let all errors go to the debug output as well
			}
		}
		else	//if debug is turned off
		{
			if(debugFile!=null)	//if we have a debug file
			{
				debugPrintStream.close();	//close the debug output stream
			}
//G***del; this would result in infinite recursion, and isn't necessary, anyway			setOutput(System.out);	//redirect all debugging output back to the standard output
			setErr(System.err);	//set error output back to its default
		}
	}
	
	/**An object for formatting the date and time.*/
//TODO del	protected final DateFormat dateFormat=DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
	protected final DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS Z");

	/**The default constructor, which isn't allowed to be called by external functions.
	@see #getDebug
	*/
  private Debug()
	{
		try
		{
			Class.forName(JFRAME_CLASS_NAME); //load the JFrame class, if we can
			isSwingAvailable=true;  //if this didn't cause an error, we know that Swing is available
		}
		catch(ClassNotFoundException e) //if a Swing class couldn't be loaded
		{
			isSwingAvailable=false;  //if caused an error, Swing isn't available
		}
		try //try to create a debug display object
		{
			if(isSwingAvailable)  //if Swing is available
			{
				final Class swingDisplayClass=Class.forName(DEBUGSWINGDISPLAY_CLASS_NAME); //load the Swing debug display class
				debugDisplay=(DebugDisplay)swingDisplayClass.newInstance(); //create the display class
			}
			else  //if Swing isn't available, we'll use the AWT
			{
				final Class awtDisplayClass=Class.forName(DEBUGAWTDISPLAY_CLASS_NAME); //load the AWT debug display class
				debugDisplay=(DebugDisplay)awtDisplayClass.newInstance(); //create the display class
			}
		}
		catch(ClassNotFoundException e) {} //if we can't load a class, ignore the error; debug display will not have been turned on G***fix comments here
		catch(IllegalAccessException e) {} //if we can't access the display class constructor, ignore the error; debug display will not have been turned on
		catch(InstantiationException e) {} //if we can't create the display object, ignore the error; debug display will not have been turned on
	}

	/**@param object The object for which a status should be returned.
	@return "null" if the object is <code>null</code>, otherwise "not null".*/
/*G***del
	public static String getNullStatus(final Object object)
	{
		return object==null ? "null" : "not null";	//return whether this object is null
	}
*/

	/**Returns the single debug object after creating it if necessary.
	@return The singleton debug object.
	*/
	public static synchronized Debug getDebug()
	{
		if(debug==null)	//if there is no debug object, yet
		{
			debug=new Debug();	//create a new one
		}
		return debug;	//return the single debug object
	}

	/**@return Whether or not debugging is turned on.*/
	public static boolean isDebug()
	{
		return debug!=null ? debug.isDebug : false;	//don't even bother creating a debug object if there isn't one---just report back that debugging isn't turned on
	}

	/**Sets whether debugging is turned on.
	@param newDebug Whether debugging should be turned on.
	*/
	public static void setDebug(final boolean newDebug)
	{
		final Debug debug=getDebug();	//get the debug object
		if(newDebug!=debug.isDebug)	//if they are really changing the debug status
		{
			debug.isDebug=newDebug;	//update the debug variable
			debug.updateOutput();	//update our output to coincide with our new debug status
		}
	}

	/**@return Whether or not debugging is visible on the screen.*/
	public static boolean isVisible()
	{
		return isDebug() && getDebug().debugDisplay!=null && getDebug().debugDisplay.isEnabled();	//if we have a displayer, and it's enabled, we're showing output on the screen
	}

	/**@return Whether or not the Swing classes are available.*/
	protected static boolean isSwingAvailable()
	{
		return getDebug().isSwingAvailable;	//see if swing is available
	}

	/**Sets whether debugging output is shown in a separate log window. Has no
		effect if debugging is not turned on.
	@param visible Whether debugging should be turned on.
	@see #isDebug
	*/
	public static synchronized void setVisible(final boolean visible)
	{
		if(isDebug() && isSwingAvailable())	//if debugging is turned on, and Swing is present
		{
			final Debug debug=getDebug(); //get a reference to the debug object
			if(debug.debugDisplay!=null)    //we can only become visible if we have a debug display object
			{
				if(visible)	//if they want to turn visible debugging on
				{
					if(!isVisible())	//if we're not already visible
					{
						debug.debugDisplay.setEnabled(true);  //enable the debug display
					}
				}
				else	//if they want to turn visible debugging off
				{
					if(isVisible())	//if we're visible now
					{
						debug.debugDisplay.setEnabled(false);  //disable the debug display
					}
				}
			}
		}
	}

	/**Returns the debug information reported.
	Defaults to all report options.
	@return The debug information that will be reported with each log.
	*/
	public static Set<ReportOption> getReportOptions()
	{
		return getDebug().reportOptions; //return the report options
	}

	/**Sets the type of information that should be reported with each log.
	@param options The information to be reported on.
	*/
	public static void setReportOptions(final Set<ReportOption> options)
	{
		getDebug().reportOptions=options;  //update the report options
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

	/**Returns the report levels which should actually be logged.
	Defaults to all available levels.
	@return The log levels that will be logged.
	*/
	public static Set<ReportLevel> getReportLevels()
	{
		return getDebug().reportLevels; //return the report levels
	}

	/**Sets the report levels that will actually be logged.
	@param levels The levels that will be logged.
	*/
	public static void setReportLevels(final Set<ReportLevel> levels)
	{
		getDebug().reportLevels=levels;  //update the report levels
	}

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

	/**@return The string representation of the stack trace at the current program location.*/
/*G***del
	public static String getStackTrace()
	{
		return getStackTrace(new Throwable());  //return the stack trace of a new throwable object
	}
*/

	/**Gets a string representation of a stack trace of a given error or exception.
	@param throwable The object which holds stack information.
	@return The stack trace of the given object in string form.
	*/
/*G***del
	public static String getStackTraceString(final Throwable throwable)
	{
		final StringWriter stringWriter=new StringWriter();	//create a string writer that we can write to
		final PrintWriter stringPrintWriter=new PrintWriter(stringWriter);	//create a new print writer so that the stack trace can write to the string writer
		throwable.printStackTrace(stringPrintWriter);	//print the stack trace to the string writer
		return stringWriter.toString();	//return the constructed string containing the stack trace
	}
*/

	/**Appends a stack trace to a string buffer, including any recursive causes.
	@param stringBuffer The string buffer to retrieve the stack trace.
	@param throwable The source of the stack trace, not including any
		source inside this class.
	@return The string buffer into which the stack trace was written.
	*/
	public static StringBuffer appendStackTrace(final StringBuffer stringBuffer, final Throwable throwable)
	{
		stringBuffer.append(throwable).append('\n');	//append the throwable itself
		final StackTraceElement[] stack=throwable.getStackTrace();	//get the trace of the stack
		for(final StackTraceElement stackTraceElement:stack)	//for each element on the stack
		{
			if(!Debug.class.getName().equals(stackTraceElement.getClassName()))	//if this location was not inside this class
			{
				stringBuffer.append('\t').append(stackTraceElement).append('\n');	//append the stack trace element
			}
		}
		final Throwable cause=throwable.getCause();	//get the cause of this throwable
		if(cause!=null)	//if there is a cause
		{
			stringBuffer.append("Cause:");	//Cause:
			appendStackTrace(stringBuffer, cause);	//recursively append the cause
		}
		return stringBuffer;	//return the string buffer, now with the new information
	}

	/**Writes a debug message to all previously specified locations, such as
		to the standard output, to the visible log, to disk, etc.
	@param level The reporting level of the log entry.
	@param objects The objects to output. If an object is an instance of <code>Throwable</code>,
		a stack trace will be generated.
	@see #isVisible
	@see #setVisible
	*/
	private static void write(final ReportLevel level, final Object... objects)
	{
		final Debug debug=getDebug();	//get the debug object
		final Set<ReportOption> options=debug.reportOptions;  //get the items we should report
			//construct a string with time level : [thread] location : objects
		final StringBuffer stringBuffer=new StringBuffer();  //create a string builder for constructing our log output
//G***fix		if((report&REPORT_LEVEL)!=0)  //if we should report the log level
		if(options.contains(ReportOption.LEVEL))	//if we should report the report level
		{
			stringBuffer.append(level).append(' ').append(':');	//level:
		}
		if(options.contains(ReportOption.TIME))  //if we should report the time
		{
		  stringBuffer.append(' ');  //append a space
			debug.dateFormat.format(new Date(), stringBuffer, new FieldPosition(0)); //format the date into our string buffer
		}
		if(options.contains(ReportOption.THREAD))  //if we should report the thread
		{
		  stringBuffer.append(' ');  //append a space
		  stringBuffer.append('[');  //append a left bracket
			stringBuffer.append(Thread.currentThread().getName()); //append the thread name
		  stringBuffer.append(']');  //append a right bracket
		}
		if(options.contains(ReportOption.LOCATION))  //if we should report the program location
		{
		  stringBuffer.append(' ');  //append a space
			stringBuffer.append(getProgramLocation()); //append the program location
		}
		if(objects.length>0)	//if there are objects to output
		{			
			stringBuffer.append(' ');  //append a space
			stringBuffer.append(':');  //append a colon
			for(final Object object:objects)	//for each object
			{
				stringBuffer.append(' ');  //append a space
				if(object instanceof Throwable)	//if this is a throwable object
				{
					appendStackTrace(stringBuffer, (Throwable)object);	//append a stack trace
				}
				else	//if this is not a throwable object
				{
					stringBuffer.append(object);	//append this object's string value with a separator
				}
			}
		}
		final String logString=stringBuffer.toString();  //get the text we constructed
		synchronized(debug.debugPrintStream)	//synchronize on the print stream
		{
			debug.debugPrintStream.println(logString);	//send the text using the debug output print stream
			debug.debugPrintStream.flush(); //make sure the text is flushed to the output
		}
		if(isVisible())	//if we have visual debugging as well
		{
		  debug.logVisibleText(logString);	//show the text visually
		}
	}

	/**Logs text visibly to the debug frame, if visible debugging is turned on.
	@param text The text to display.
	@see #isVisible
	@see setVisible
	*/
	private synchronized void logVisibleText(final String text)
	{
		if(isVisible() && isSwingAvailable())	//if visible debugging is turned on
		{
			getDebug().debugDisplay.trace(text);  //trace the text using the display
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
		if(isDebug() && getReportLevels().contains(ReportLevel.TRACE))	//if tracing is enabled
		{
			write(ReportLevel.TRACE, objects);	//write the trace information
		}
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
		if(isDebug() && getReportLevels().contains(ReportLevel.TRACE))	//if tracing is enabled
		{
			write(ReportLevel.TRACE, objects);	//write the trace information
			write(ReportLevel.TRACE, new Throwable());	//write a stack trace information
		}
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
		if(isDebug() && getReportLevels().contains(ReportLevel.INFO))	//if information reporting is enabled
		{
			write(ReportLevel.INFO, objects);	//write the information
		}
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
		if(isDebug() && getReportLevels().contains(ReportLevel.LOG))	//if logging is enabled
		{
			write(ReportLevel.LOG, objects);	//write the log information
		}
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
		if(isDebug() && getReportLevels().contains(ReportLevel.WARN))	//if warning is enabled
		{
			write(ReportLevel.WARN, objects);	//write the warn information
		}
	}

	/**Outputs a series of objects to the standard output if debugging is enabled.
	Meant for errors that are not expected to occur during normal program operations
		 -- program logic errors, and exceptions that are not expected to be thrown.
	Errors are considered so crucial that they are sent to the standard error
		output even if debugging is turned off.
	TODO If debugging the notification includes the error level, the error message is also displayed in dialog box.
	<p>If no objects are provided, only the trace location will be output.</p>
	@param objects The objects to output. If an object is an instance of <code>Throwable</code>,
		a stack trace will be generated.
	@see ReportLevel#ERROR
	*/
	public static void error(final Object... objects)
	{
		if(getReportLevels().contains(ReportLevel.ERROR))	//if error reporting is enabled
		{
			if(isDebug())	//if debug is turned on
			{
				write(ReportLevel.ERROR, objects);	//write the error information
			}
			else	//if debug is turned off
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
//G***del		if(isDebug())	//if debugging is turned on
		{
			trace(message);	//trace the message
			if(getDebug().debugDisplay!=null) //if we have a debug display object
				getDebug().debugDisplay.notify(message);  //let the debug display object show the message
/*G***del
			final String wrappedMessage=StringUtilities.wrap(message, 100);	//wrap the error message at 100 characters G***probably use a constant here
//G***del when works //G***bring back swing			JOptionPane.showMessageDialog(null, wrappedMessage, "Debug Message", JOptionPane.INFORMATION_MESSAGE);	//G***i18n; comment
		  if(USE_SWING) //if we're using Swing
			{
				try
				{
					getDebug().jOptionPaneShowMessageDialogMethod.invoke(null, //G***i18n; comment
							new Object[]{null, wrappedMessage, "Debug Message", new Integer(JOptionPane.INFORMATION_MESSAGE)}); //call debugTextArea.append()
				}
				catch(IllegalAccessException e) {}  //ignore any errors
				catch(InvocationTargetException e) {}
			}
			//G***add code for non-Swing debugging
*/
		}
	}

	/**Returns a user-friendly error message for the given exception.
		Provides more description messages than the default for some exceptions
		such as <code>FileNotFoundException</code> and
		<code>sun.io.MalformedInputException</code>.
	@param exception The exception for which an error message should be returned.
	@return The error message for the exception.
	*/
/*G***del
	public static String getErrorMessage(final Exception exception)
	{
		if(exception instanceof FileNotFoundException)	//if a file was not found
		{
			return "File not found: "+exception.getMessage();	//G***comment; i18n
		}
		else if(exception instanceof sun.io.MalformedInputException)	//if there was an error converting characters; G***put this elsewhere, fix for non-Sun JVMs
		{
			return "Invalid character encountered for file encoding.";	//G***comment; i18n
		}
		else  //for any another error
		{
		  final String message=exception.getMessage();  //get the exception message
			return message!=null ? message : exception.getClass().getName();  //if the message is null, use the class name of the exception as the message
		}
	}
*/

	/**Unconditionally displays an error message for an exception.
//G***fix	@param title The error message box title.
	@param exception The exception that caused the error.
	*/
//G***del	public static void notifyError(/*G***fix final String title, */final Exception exception)
	/*G***del
	{
		error(exception);	//log the error
//G***del; this is a user-interface function, so probably do it even if debug has already done it, using the user-friendly error message		if(!isDebug() || (getNotify()&ERROR_LEVEL)==0)	//if we don't have debug turned on, or notifications are turned off for errors
		{
			if(getDebug().debugDisplay!=null) //if we have something with which to display notifications
				getDebug().debugDisplay.error(getErrorMessage(exception));  //let the debug display object show a user-friendly error
		}
	}
*/

	/**Returns a stack trace element of the last line of program execution before
		a <code>Debug</code> method was entered.
	@return A stack trace element representing the last line of program execution before
		a <code>Debug</code> method was entered.
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