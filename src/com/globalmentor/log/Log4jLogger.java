/*
 * Copyright © 1996-2009 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import static com.globalmentor.collections.Arrays.*;
import static com.globalmentor.java.Strings.*;

/**A logger that delegates to a Java logger.
<p>The log levels in this implementation correspond as follows:</p>
<dl>
	<dt>{@link Log.Level#TRACE}</dt> <dd>{@link org.apache.log4j.Level#TRACE}</dd>
	<dt>{@link Log.Level#DEBUG}</dt> <dd>{@link org.apache.log4j.Level#DEBUG}</dd>
	<dt>{@link Log.Level#INFO}</dt> <dd>{@link org.apache.log4j.Level#INFO}</dd>
	<dt>{@link Log.Level#WARNING}</dt> <dd>{@link org.apache.log4j.Level#WARN}</dd>
	<dt>{@link Log.Level#ERROR}</dt> <dd>{@link org.apache.log4j.Level#ERROR}</dd>
</dl>
@author Garret Wilson
@see org.apache.log4j.Logger
*/
public class Log4jLogger extends AbstractAdapterLogger<org.apache.log4j.Logger>
{

	/**Java logger constructor.
	@param logger The Java logger delegate.
	@throws NullPointerException if the given logger is <code>null</code>.
	*/
	public Log4jLogger(final org.apache.log4j.Logger logger)
	{
		super(logger);
	}

	/**{@inheritDoc}
	This method delegates to {@link org.apache.log4j.Logger#trace(Object)}
	or {@link org.apache.log4j.Logger#trace(Object, Throwable)} as appropriate.
	*/
	@Override
	public void trace(final Object... objects)
	{
		final Throwable throwable=getInstance(objects, Throwable.class);	//see if a throwable was given, so that we can give it explicitly to Log4j
		if(throwable!=null)	//if a throwable is one of the objects
		{
			getLogger().trace(concat(objects, ' ', throwable), throwable);	//ignore the throwable in the string, but give it explicitely to log4j
			
		}
		else
		{
			getLogger().trace(concat(objects, ' '));
		}
	}

	/**{@inheritDoc}
	*/
	@Override
	public void traceStack(final Object... objects)
	{
		Throwable throwable=getInstance(objects, Throwable.class);	//see if a throwable was given, so that we can give it explicitly to log4j
		if(throwable==null)	//if no throwable was given
		{
			throwable=new Throwable();	//create our own throwable
		}
		getLogger().trace(concat(objects, ' ', throwable), throwable);	//ignore the throwable when constructing the string, but give it explicitely to log4j
	}

	/**{@inheritDoc}
	This method delegates to {@link org.apache.log4j.Logger#debug(Object)}
	or {@link org.apache.log4j.Logger#debug(Object, Throwable)} as appropriate.
	*/
	@Override
	public void debug(final Object... objects)
	{
		final Throwable throwable=getInstance(objects, Throwable.class);	//see if a throwable was given, so that we can give it explicitly to log4j
		if(throwable!=null)	//if a throwable is one of the objects
		{
			getLogger().debug(concat(objects, ' ', throwable), throwable);	//ignore the throwable when constructing the string, but give it explicitely to log4j
			
		}
		else
		{
			getLogger().debug(concat(objects, ' '));
		}
	}

	/**{@inheritDoc}
	This method delegates to {@link org.apache.log4j.Logger#info(Object)}
	or {@link org.apache.log4j.Logger#info(Object, Throwable)} as appropriate.
	*/
	@Override
	public void info(final Object... objects)
	{
		final Throwable throwable=getInstance(objects, Throwable.class);	//see if a throwable was given, so that we can give it explicitly to log4j
		if(throwable!=null)	//if a throwable is one of the objects
		{
			getLogger().info(concat(objects, ' ', throwable), throwable);	//ignore the throwable when constructing the string, but give it explicitely to log4j
			
		}
		else
		{
			getLogger().info(concat(objects, ' '));
		}
	}

	/**{@inheritDoc}
	This method delegates to {@link org.apache.log4j.Logger#warn(Object)}
	or {@link org.apache.log4j.Logger#warn(Object, Throwable)} as appropriate.
	*/
	@Override
	public void warn(final Object... objects)
	{
		final Throwable throwable=getInstance(objects, Throwable.class);	//see if a throwable was given, so that we can give it explicitly to log4j
		if(throwable!=null)	//if a throwable is one of the objects
		{
			getLogger().warn(concat(objects, ' ', throwable), throwable);	//ignore the throwable when constructing the string, but give it explicitely to log4j
			
		}
		else
		{
			getLogger().warn(concat(objects, ' '));
		}
	}

	/**{@inheritDoc}
	This method delegates to {@link org.apache.log4j.Logger#error(Object)}
	or {@link org.apache.log4j.Logger#error(Object, Throwable)} as appropriate.
	*/
	@Override
	public void error(final Object... objects)
	{
		final Throwable throwable=getInstance(objects, Throwable.class);	//see if a throwable was given, so that we can give it explicitly to log4j
		if(throwable!=null)	//if a throwable is one of the objects
		{
			getLogger().error(concat(objects, ' ', throwable), throwable);	//ignore the throwable when constructing the string, but give it explicitely to log4j
			
		}
		else
		{
			getLogger().error(concat(objects, ' '));
		}
	}

}