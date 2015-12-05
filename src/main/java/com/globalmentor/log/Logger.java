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

/**
 * The interface to logging functionality.
 * <p>
 * The {@link Log#RAW_FLAG}, if present at the beginning of a sequence of log objects, indicates that the log text should be interpreted as literal, raw data
 * with no preface or control characters. This allows the caller wants to control its own line breaks so that no additional line separator is added, allowing
 * the implementation of progress bars, for example.
 * </p>
 * @author Garret Wilson
 */
public interface Logger {

	/**
	 * Logs a series of trace objects.
	 * <p>
	 * Meant for messages that show the path of program execution.
	 * </p>
	 * @param objects The objects to log.
	 * @see Log.Level#TRACE
	 */
	public void trace(final Object... objects);

	/**
	 * Logs a series of trace objects and a stack trace.
	 * <p>
	 * Meant for messages that show the path of program execution.
	 * </p>
	 * <p>
	 * If no objects are provided, only the trace location will be output.
	 * </p>
	 * @param objects The objects to log; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	 * @see Log.Level#TRACE
	 */
	public void traceStack(final Object... objects);

	/**
	 * Logs a series of debug objects.
	 * <p>
	 * Meant for useful information, usually verbose.
	 * </p>
	 * @param objects The objects to log; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	 * @see Log.Level#DEBUG
	 */
	public void debug(final Object... objects);

	/**
	 * Logs a series of information objects.
	 * <p>
	 * Meant for logging specific events which should be logged but which are adversity-neutral.
	 * </p>
	 * @param objects The objects to log; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	 * @see Log.Level#INFO
	 */
	public void info(final Object... objects);

	/**
	 * Logs a series of warning objects
	 * <p>
	 * Meant for errors that should not prevent the robust functioning of the program and that are expected to occur infrequently and not because of program
	 * design.
	 * </p>
	 * @param objects The objects to log; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	 * @see Log.Level#WARN
	 */
	public void warn(final Object... objects);

	/**
	 * Logs a series of error objects.
	 * <p>
	 * Meant for unexpected conditions representing errors.
	 * </p>
	 * @param objects The objects to log; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	 * @see Log.Level#ERROR
	 */
	public void error(final Object... objects);

	/**
	 * Logs a series of objects at a given log level.
	 * @param level The level at which to log the objects.
	 * @param objects The objects to log; if an object is an instance of {@link Throwable}, a stack trace will be generated.
	 * @throws NullPointerException if the given log level is <code>null</code>.
	 */
	public void log(final Log.Level level, final Object... objects);

}