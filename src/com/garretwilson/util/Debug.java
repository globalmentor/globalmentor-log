package com.garretwilson.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.*;
import java.text.FieldPosition;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.StringTokenizer;
import com.garretwilson.lang.StringUtilities;

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

		//the available reporting levels

	/**Used in instances in which no level should be used, such as as a parameter
		to <code>setNotify()</code>.*/
	public final static int NO_LEVEL=0;

	/**Used to indicate a program's execution path.*/
	public final static int TRACE_LEVEL=1;

	/**The information-only level, used to convey specific information more than
		simply program execution location.*/
	public final static int INFORMATION_LEVEL=2;

	/**Indicates that conditions are possibly adverse.*/
	public final static int WARN_LEVEL=4;

	/**Indicates an unexpected condition representing an error.*/
	public final static int ERROR_LEVEL=8;

	/**Indicates all levels when specifying, for example, which levels should
		notify the user.
	*/
	public final static int ALL_LEVELS=TRACE_LEVEL|INFORMATION_LEVEL|WARN_LEVEL|ERROR_LEVEL;

	/**The levels that should notify the user.*/
	private int notifyLevel=ERROR_LEVEL;

	/**The levels that should be reported.*/
	private int reportLevel=ALL_LEVELS;

		//various reporting options

	/**Indicates that no extra information (besides the trace information) should
		be reported.
	*/
	public final static long REPORT_NONE=0;

	/**Indicates that the error level should be reported.*/
	public final static long REPORT_LEVEL=1;

	/**Indicates that execution time should be reported.*/
	public final static long REPORT_TIME=2;

	/**Indicates that the thread name should be reported.*/
	public final static long REPORT_THREAD=4;

	/**Indicates that the location of program execution should be reported.*/
	public final static long REPORT_LOCATION=8;

	/**Indicates that all information should be reported.*/
	public final static long REPORT_ALL=REPORT_LEVEL|REPORT_TIME|REPORT_THREAD|REPORT_LOCATION;

	/**The information that should be reported with each log.*/
	private long report=REPORT_ALL;

	/**The single copy of the debug frame class that's allowed to be created if needed.*/
//G***del	private Frame debugFrame=null;
//G***del when works	private JFrame debugFrame=null;

	/**The text area used to log visible output.*/
//G***del	private TextArea debugTextArea;
//G***del when works	private JTextArea debugTextArea;

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

	/**Sets the stream used to output debug messages if debug is turned on.
		The default before this function is called is <code>System.out</code>.
	@param printStream The print stream to use to print debug messages.
	@see #isDebug
	@see #setOutput(java.io.File)
	*/
	public static synchronized void setOutput(final PrintStream printStream)
	{
		if(isDebug())	//if debugging is turned on
		{
			final Debug debug=getDebug();	//get the debug object
			if(printStream!=null)	//if they are passing a valid print stream
			{
				debug.debugPrintStream=printStream;	//save the print stream they pass
				setErr(debug.debugPrintStream);	//redirect the error output to let all errors go to the debug output as well
			}
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

	/**Redirects debug output to a specific file if debug is turned on.
	@param file The file in which debug information should be stored, or
		<code>null</code> to revert back to the standard output.
	@exception FileNotFoundException Thrown if the specified file is not found
	@see #isDebug
	@see #setOutput(java.io.PrintStream)
	*/
	public static synchronized void setOutput(final File file) throws FileNotFoundException
	{
		if(isDebug())	//if debugging is turned on
		{
			final Debug debug=getDebug();	//get the debug object
			if(file!=null)	//if they passed a valid file
			{
				final OutputStream outputStream=new FileOutputStream(file);	//open an output stream to the file (open this first so that any error will not affect our current status)
				if(debug.debugFile!=null)	//if they had a file open already
					debug.debugPrintStream.close();	//close the stream to the existing file
				debug.debugFile=file;	//show which file we're using for debugging
				setOutput(new PrintStream(outputStream));	//create a new print stream to the file and set it as our debug output stream
			}
			else	//if they passed null
			{
				if(debug.debugFile!=null)	//*only* if they had specified a file before
				{
					debug.debugPrintStream.close();	//close the debug print stream, which will be for that file
					debug.debugFile=null;	//show that a file is no longer specified
					setOutput(System.out);	//redirect all debugging output back to the standard output
				}
			}
		}
	}

	/**An object for formatting the date and time.*/
	protected DateFormat dateFormat=DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);

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
	public static String getNullStatus(final Object object)
	{
		return object==null ? "null" : "not null";	//return whether this object is null
	}

	/**Returns the single debug object after creating it if necessary.
	@return The singleton debug object.
	*/
	public static synchronized Debug getDebug()
	{
		if(debug==null)	//if there is no debug object, yet
			debug=new Debug();	//create a new one
		return debug;	//return the single debug object
	}

	/**@return Whether or not debugging is turned on.*/
	public static boolean isDebug()
	{
		return getDebug().isDebug;
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
			if(newDebug)	//if they are turning debug on
			{
				setErr(debug.debugPrintStream);	//redirect the error output to let all errors go to the debug output as well
//G***del; not needed			  trace(new Date().toString()); //output the date, if we're debugging G***use inform() here
			}
			else	//if they are turning debug off
			{
				try
				{
					setOutput((File)null);	//close any output files we may have had open
				}
				catch(FileNotFoundException e) {}	//there is no file to find, so this error will never occur here
				setErr(System.err);	//set error output back to its default
			}
		}
	}

	/**@return Whether or not debugging is visible on the screen.*/
	public static boolean isVisible()
	{
		return getDebug().debugDisplay!=null && getDebug().debugDisplay.isEnabled();	//if we have a displayer, and it's enabled, we're showing output on the screen
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

	/**Returns the debug information reported, which will be one or more of the
		<code>REPORT_</code> constants ORed together. The default is
		<code>REPORT_ALL</code>.
	@return The debug information that will be reported with each log.
	*/
	public static long getReport()
	{
		return getDebug().report; //return the report variable
	}

	/**Sets the type of information that should be reported with each log.
	@param newReport The information to be reported on, one or more of the
		<code>REPORT_</code> constants ORed together.
	*/
	public static void setReport(final long newReport)
	{
		getDebug().report=newReport;  //update the report variable
	}

	/**Returns the report levels for which the user should by notified, which will
		be one or more of the <code>_LEVEL</code> constants ORed together. The
		default is <code>ERROR_LEVEL</code>.
	@return The log levels that will cause a notification.
	@see #notifyLevel
	*/
	public static int getNotifyLevel()
	{
		return getDebug().notifyLevel; //return the notify level variable
	}

	/**Sets the log levels that will cause a notification to be given to the user.
	@param newNotifyLevel The levels that will notify the user, one or more of the
		<code>_LEVEL</code> constants ORed together.
	@see #notifyLevel
	*/
	public static void setNotifyLevel(final int newNotifyLevel)
	{
		getDebug().notifyLevel=newNotifyLevel;  //update the notify level variable
	}

	/**Returns the report levels which should actually be logged, which will
		be one or more of the <code>_LEVEL</code> constants ORed together. The
		default is <code>ALL_LEVELS</code>.
	@return The log levels that will be logged.
	@see #reportLevel
	*/
	public static int getReportLevel()
	{
		return getDebug().reportLevel; //return the report level variable
	}

	/**Sets the log levels that will actually be logged.
	@param newReportLevel The levels that will be logged, one or more of the
		<code>_LEVEL</code> constants ORed together.
	@see #reportLevel
	*/
	public static void setReportLevel(final int newReportLevel)
	{
		getDebug().reportLevel=newReportLevel;  //update the report level variable
	}

	/**@return The string representation of the stack trace at the current
		program location.
	*/
	public static String getStackTrace()
	{
		return getStackTrace(new Throwable());  //return the stack trace of a new throwable object
	}

	/**Gets a stack trace of a given error or exception.
	@param throwable The object which holds stack information.
	@return The stack trace of the given object in string form.
	*/
	public static String getStackTrace(final Throwable throwable)
	{
		final StringWriter stringWriter=new StringWriter();	//create a string writer that we can write to
		final PrintWriter stringPrintWriter=new PrintWriter(stringWriter);	//create a new print writer so that the stack trace can write to the string writer
		throwable.printStackTrace(stringPrintWriter);	//print the stack trace to the string writer
		//G***eventually remove the Debug lines
		return stringWriter.toString();	//return the constructed string containing the stack trace
	}

	/**Writes a debug message to all previously specified locations, such as
		to the standard output, to the visible log, to disk, etc.
	@param text The text to write, or <code>null</code> if no text should be output.
	@see #isVisible
	@see #setVisible
	*/
	private static void write(final String text)
	{
		final Debug debug=getDebug();	//get the debug object
		final long report=debug.report;  //get the items we should report
		//construct a string with time [thread] (location) : text
		final StringBuffer logStringBuffer=new StringBuffer();  //create a string buffer for constructing our log output
//G***fix		if((report&REPORT_LEVEL)!=0)  //if we should report the log level
		if((report&REPORT_TIME)!=0)  //if we should report the time
		{
			debug.dateFormat.format(new Date(), logStringBuffer, new FieldPosition(0)); //format the date into our string buffer
		  logStringBuffer.append(' ');  //append a space
		}
		if((report&REPORT_THREAD)!=0)  //if we should report the thread
		{
		  logStringBuffer.append('[');  //append a left bracket
			logStringBuffer.append(Thread.currentThread().getName()); //append the thread name
		  logStringBuffer.append(']');  //append a right bracket
		  logStringBuffer.append(' ');  //append a space
		}
		if((report&REPORT_LOCATION)!=0)  //if we should report the program location
		{
//G***del		  logStringBuffer.append('(');  //append a left parenthesis
			logStringBuffer.append(debug.getProgramLocation()); //append the program location
//G***del		  logStringBuffer.append(')');  //append a right parenthesis
		  logStringBuffer.append(' ');  //append a space
		}
		if(text!=null)  //if we were given text
		{
			logStringBuffer.append(':');  //append a colon
			logStringBuffer.append(' ');  //append a space
			logStringBuffer.append(text);  //append the text
		}
//G***del 		final Date now=new Date();	//get the current date and time
/*G***del when works
		final String outputText=debug.getProgramLocation()+" "+ //G***testing
			debug.dateFormat.format(now)+" ["+Thread.currentThread().getName()+"]: "+text;
		debug.debugPrintStream.println(outputText);	//send the text using the debug output print stream
*/
		final String logString=logStringBuffer.toString();  //get the text we constructed
		debug.debugPrintStream.println(logString);	//send the text using the debug output print stream
		debug.debugPrintStream.flush(); //G***testing
		if(isVisible()/*G***del && text!=null*/)	//if we have visual debugging as well
		  debug.logVisibleText(logString);	//show the text visually
//G***del when works		  debug.logVisibleText(text);	//show the text visually
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

	/**Outputs a stack trace of the given error or exception object.
	Meant for messages that show the path of program execution and do not
		represent error conditions.
	@param throwable The exception object that should be traced.
	@see #getDebug
	@see #getNotify
	@see #isDebug
	@see #notify
	@see #error
	*/
	public static void trace(final Throwable throwable)
	{
		traceStack(throwable);  //trace the throwable object
	}

	/**Outputs a series of objects to the standard output if debugging is enabled.
		Meant for messages that show the path of program execution.
		The objects are converted to a string and sent to the debug trace output on
		the same line.
	<p>If no objects are provided, only the trace location will be output.</p>
	<p>If an object is <code>null</code>, "null" will be output.</p>
	@param objects The objects to output.
	@see #getDebug
	@see #isDebug
	*/
	public static void trace(final Object... objects)
	{
		if(isDebug() && (getReportLevel() & TRACE_LEVEL)>0)	//if debugging is turned on
		{
			if(objects.length>0)	//if there are objects to output
			{
				final StringBuilder stringBuilder=new StringBuilder();	//we'll create the string manually
				for(final Object object:objects)	//for each object
				{
						//TODO fix null better
					stringBuilder.append(object!=null ? object : "NULL").append(' ');	//append this object's string value with a separator
				}
				write(stringBuilder.toString());	//output string string we constructed
			}
			else	//if there are no objects
			{
				write(null);	//write trace information without a message			
			}
		}
	}

	/**Outputs an integer to the standard output if debugging is enabled.
	Meant for messages that show the path of program execution.
	@param traceInt The integer to output.
	@see #getDebug
	@see #isDebug
	*/
	public static void trace(final int traceInt)
	{
		if(isDebug() && (getReportLevel() & TRACE_LEVEL)>0)	//if debugging is turned on
			write(String.valueOf(traceInt));	//trace with the string version of the integer
	}

	/**Outputs a message and an integer to the standard output if debugging is enabled.
		Meant for messages that show the path of program execution.
		The integer is concatenated with the trace string only if debugging is
		enabled, making it slightly more efficient than unconditionally
		concatenating in the calling method.
	@param traceString The string to output.
	@param traceInt The integer to output after the trace string.
	@see #getDebug
	@see #isDebug
	*/
	public static void trace(final String traceString, final int traceInt)
	{
		if(isDebug() && (getReportLevel() & TRACE_LEVEL)>0)	//if debugging is turned on
			write(traceString+traceInt);	//concatenate the with the integer and trace the result
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
	public static void trace(final String pattern, final Object[] arguments)
	{
		if(isDebug() && (getReportLevel() & TRACE_LEVEL)>0)	//if debugging is turned on
			write(MessageFormat.format(pattern, arguments));	//format the string and trace the result
	}

	/**Outputs the stack trace to the standard output if debugging is enabled.
		This represents informational data, not an error condition.
	@see #getDebug
	@see #isDebug
	*/
	public static void traceStack()
	{
		if(isDebug() && (getReportLevel() & TRACE_LEVEL)>0)	//if debugging is turned on
		{
			trace(getStackTrace());	//send the text from a stack strace as debug trace output
		}
	}

	/**Outputs the stack trace to the standard output if debugging is enabled,
		after outputting the specified trace text.
		This represents informational data, not an error condition.
		The objects are converted to a string and sent to the debug trace output on
		the same line.
	<p>If no objects are provided, only the trace stack will be output.</p>
	<p>If an object is <code>null</code>, "null" will be output.</p>
	@param objects The objects to output.
	@see #getDebug
	@see #isDebug
	*/
	public static void traceStack(final Object... objects)
	{
		if(isDebug() && (getReportLevel() & TRACE_LEVEL)>0)	//if debugging is turned on
		{
			if(objects.length>0)	//if objects were given
			{
				trace(objects);	//output the provided objects
			}
			traceStack(); //print a stack trace
		}
	}

	/**Outputs the stack trace of a particular error or exception object if
		debugging is enabled.
		This represents informational data, not an error condition.
	@param throwable The object which contains the stack information.
	@see #getDebug
	@see #isDebug
	*/
	public static void traceStack(final Throwable throwable)
	{
		if(isDebug() && (getReportLevel() & TRACE_LEVEL)>0)	//if debugging is turned on
		{
			trace(getStackTrace(throwable));	//send the text from a stack strace of the object as debug trace output
		}
	}

	/**Prints a warning message if debugging is turned on.
	Meant for errors that should not prevent the robust functioning of the program
		and that are expected to occur infrequently and not because of program design.
	@param warnString The warning message.
	@see #getDebug
	@see #getNotify
	@see #isDebug
	@see #notify
	*/
	public static void warn(final String warnString)
	{
		if(isDebug() && (getReportLevel() & WARN_LEVEL)>0)	//if debugging is turned on
		{
			write(warnString);	//write the string
		}
	}

	/**Prints a warning stack trace from an exception if debugging is turned on.
	Meant for errors that should not prevent the robust functioning of the program
		and that are expected to occur infrequently and not because of program design.
	@param throwable The exception object that represents the warning.
	@see #getDebug
	@see #getNotify
	@see #isDebug
	@see #notify
	*/
	public static void warn(final Throwable throwable)
	{
		if(isDebug() && (getReportLevel() & WARN_LEVEL)>0)	//if debugging is turned on
		{
			write(getStackTrace(throwable));	//write the stack trace
		}
	}

	/**Prints a stack trace and an error message from an exception to the standard
		error output if debugging is turned on.
	If debugging the notification includes the error level, the error message is also displayed in dialog box.
	Meant for errors that are not expected to occur during normal program operations
		 -- program logic errors, and exceptions that are not expected to be thrown.
	Errors are considered so crucial that they are sent to the standard error
		output even if debugging is turned off.
	@param throwable The exception object that represents the error
	@see System.err
	@see #getDebug
	@see #getNotify
	@see #isDebug
	@see #notify
	*/
	public static void error(final Throwable throwable) //G***probably allow for the error to be written to the standard error output if debugging is turned off
	{
		if(isDebug() && (getReportLevel() & ERROR_LEVEL)>0)	//if debugging is turned on
		{
			//G***why not just call stackTrace()?
			traceStack(throwable);  //trace the stack
			error(throwable.toString());	//do the default handling of the error
		}
		else  //if we aren't debugging
		{
			throwable.printStackTrace(System.err);  //send a stack trace to the standard error output
		}
	}

	/**Sends an error message to the standard error output if debugging is turned on.
	If the notification includes the error level, the error message is also
	displayed in dialog box.
	Meant for errors that are not expected to occur during normal program operations
		 -- program logic errors, and exceptions that are not expected to be thrown.
	Errors are considered so crucial that they are sent to the standard error
		output even if debugging is turned off.
	@param errorString The error message.
	@see System.err
	@see #getDebug
	@see #isDebug
	*/
	public static void error(final String errorString)
	{
		if(isDebug() && (getReportLevel() & ERROR_LEVEL)>0)	//if debugging is turned on
		{
			traceStack(errorString);	//write the string to the debug error output, along with the stack trace
			if((getNotifyLevel() & ERROR_LEVEL)!=0)	//if we should notify for error conditions
			{
				if(getDebug().debugDisplay!=null) //if we have something with which to display notifications
					getDebug().debugDisplay.error(errorString);  //let the debug display object show the error
			}
		}
		else  //if we aren't debugging
		{
			System.err.println(errorString);  //send the error to the standard error output
			new Throwable().printStackTrace(System.err);  //send a stack trace to the standard error output
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

	/**Unconditionally displays an error message for an exception.
//G***fix	@param title The error message box title.
	@param exception The exception that caused the error.
	*/
	public static void notifyError(/*G***fix final String title, */final Exception exception)
	{
		error(exception);	//log the error
//G***del; this is a user-interface function, so probably do it even if debug has already done it, using the user-friendly error message		if(!isDebug() || (getNotify()&ERROR_LEVEL)==0)	//if we don't have debug turned on, or notifications are turned off for errors
		{
			if(getDebug().debugDisplay!=null) //if we have something with which to display notifications
				getDebug().debugDisplay.error(getErrorMessage(exception));  //let the debug display object show a user-friendly error
		}
	}

	/**Returns a string description of the last line of program execution before
		a <code>Debug</code> method was entered. This is JVM implementation
		dependent, but a typical result would be,
		"com.mycompany.MyClass.text(MyClass.java:234)".
		<p>The success of this method depends on the JVM implementation of the
		<code>Thread.printStackTrace()</code> method, specifically that each program
		line begins with "at " and includes the class name.</p>
	@return A string representation of the last line of program execution, or the
		empty string if the program location could not be determined.
	*/  //G***maybe make this a public static routine
	public String getProgramLocation()
	{
		final String stackTrace=getStackTrace(); //get a stack trace at the current location
		//create a string tokenizer to read the contents of the stack trace line by line
		final StringTokenizer stringTokenizer=new StringTokenizer(stackTrace, "\r\n"); //G***maybe use a constant here
		while(stringTokenizer.hasMoreTokens())  //while there are more tokens
		{
			final String location=stringTokenizer.nextToken();  //get the next token, which should be a line of program execution
			final int atIndex=location.indexOf("at ");  //find the index of "at " G***use a constant here
		  //if we've found a stack trace line (indicated by the "at") that isn't
			//  in the Debug class (indicated by the lack of the Debug class name)
		  if(atIndex>=0 && location.indexOf("com.garretwilson.util.Debug")<0) //G***use constants here
			{
				return location.substring(atIndex); //remove the "at " and return the location G***use a constant here
			}
		}
		return "";  //show that we can't determine the program location
	}

	/**Converts a throwable into an assertion error.
	The message of the assertion error will be identical to the throwable, and
	the cause of the assertion error will be initialized to the given throwable.
	@param throwable The cause of the problem.
	@return A new assertion error initialized from the throwable.
	*/
	public static AssertionError toAssertionError(final Throwable throwable)
	{
		throw (AssertionError)new AssertionError(throwable.getMessage()).initCause(throwable); 
	}
}