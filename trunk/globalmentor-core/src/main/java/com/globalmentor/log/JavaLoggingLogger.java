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

import static com.globalmentor.java.Throwables.*;
import static com.globalmentor.text.TextFormatter.*;

/**
 * A logger that delegates to a Java logger.
 * <p>
 * The log levels in this implementation correspond as follows:
 * </p>
 * <dl>
 * <dt>{@link Log.Level#TRACE}</dt>
 * <dd>{@link java.util.logging.Level#FINE}</dd>
 * <dt>{@link Log.Level#DEBUG}</dt>
 * <dd>{@link java.util.logging.Level#CONFIG}</dd>
 * <dt>{@link Log.Level#INFO}</dt>
 * <dd>{@link java.util.logging.Level#INFO}</dd>
 * <dt>{@link Log.Level#WARN}</dt>
 * <dd>{@link java.util.logging.Level#WARNING}</dd>
 * <dt>{@link Log.Level#ERROR}</dt>
 * <dd>{@link java.util.logging.Level#SEVERE}</dd>
 * </dl>
 * @author Garret Wilson
 * @see java.util.logging.Logger
 */
public class JavaLoggingLogger extends AbstractAdapterLogger<java.util.logging.Logger>
{

	/**
	 * Java logger constructor.
	 * @param logger The Java logger delegate.
	 * @throws NullPointerException if the given logger is <code>null</code>.
	 */
	public JavaLoggingLogger(final java.util.logging.Logger logger)
	{
		super(logger);
	}

	/**
	 * {@inheritDoc} This method delegates to {@link java.util.logging.Logger#fine(String)}.
	 */
	public void trace(final Object... objects)
	{
		getLogger().fine(formatList(' ', objects));
	}

	/**
	 * {@inheritDoc} This method delegates to {@link #trace(Object...)}.
	 */
	public void traceStack(final Object... objects)
	{
		if(objects.length > 0)
		{
			trace(objects); //trace the information
		}
		trace(getStackTraceString(new Throwable())); //write a stack trace
	}

	/**
	 * {@inheritDoc} This method delegates to {@link java.util.logging.Logger#config(String)}.
	 */
	public void debug(final Object... objects)
	{
		getLogger().config(formatList(' ', objects));
	}

	/**
	 * {@inheritDoc} This method delegates to {@link java.util.logging.Logger#info(String)}.
	 */
	public void info(final Object... objects)
	{
		getLogger().info(formatList(' ', objects));
	}

	/**
	 * {@inheritDoc} This method delegates to {@link java.util.logging.Logger#warning(String)}.
	 */
	public void warn(final Object... objects)
	{
		getLogger().warning(formatList(' ', objects));
	}

	/**
	 * {@inheritDoc} This method delegates to {@link java.util.logging.Logger#severe(String)}.
	 */
	public void error(final Object... objects)
	{
		getLogger().severe(formatList(' ', objects));
	}

}