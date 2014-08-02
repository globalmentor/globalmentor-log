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

package com.globalmentor.io;

import java.io.*;

import static com.globalmentor.io.Charsets.*;
import static com.globalmentor.java.Characters.*;
import static com.globalmentor.java.Objects.*;
import com.globalmentor.log.Log;

/**
 * An output stream that logs all transferred bytes of a decorated stream.
 * @author Garret Wilson
 * @see Log
 */
public class LogOutputStream extends OutputStreamDecorator<OutputStream>
{

	/** The log level to use. */
	private final Log.Level logLevel;

	/**
	 * Decorates the given output stream with a report level of {@link Log.Level#INFO}.
	 * @param outputStream The output stream to decorate.
	 * @throws NullPointerException if the given stream is <code>null</code>.
	 */
	public LogOutputStream(final OutputStream outputStream)
	{
		this(outputStream, Log.Level.INFO);
	}

	/**
	 * Decorates the given output stream.
	 * @param outputStream The output stream to decorate.
	 * @throws NullPointerException if the given stream and/or log level is <code>null</code>.
	 */
	public LogOutputStream(final OutputStream outputStream, final Log.Level logLevel)
	{
		super(outputStream); //construct the parent class
		this.logLevel = checkInstance(logLevel, "Log level cannot be null.");
	}

	/** {@inheritDoc} */
	@Override
	public void write(int b) throws IOException
	{
		Log.log(logLevel, Log.RAW_FLAG, Character.valueOf((char)b));
		super.write(b); //do the default writing
	}

	/** {@inheritDoc} */
	@Override
	public void write(byte b[]) throws IOException
	{
		Log.log(logLevel, Log.RAW_FLAG, new String(b, US_ASCII_CHARSET));
		super.write(b); //do the default writing
	}

	/** {@inheritDoc} */
	@Override
	public void write(byte b[], int off, int len) throws IOException
	{
		Log.log(logLevel, Log.RAW_FLAG, new String(b, off, len, US_ASCII_CHARSET));
		super.write(b, off, len); //do the default writing
	}

	@Override
	protected void beforeClose() throws IOException
	{
		Log.log(logLevel, Log.RAW_FLAG, END_OF_TRANSMISSION_SYMBOL); //EOT
		super.beforeClose();
	}
}
