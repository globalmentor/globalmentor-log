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
import static java.lang.Math.*;

import static com.globalmentor.io.Charsets.*;
import static com.globalmentor.java.Characters.*;
import static com.globalmentor.java.Objects.*;

import com.globalmentor.log.Log;

/**
 * An input stream that logs all transferred bytes of a decorated stream.
 * @author Garret Wilson
 * @see Log
 */
public class LogInputStream extends InputStreamDecorator<InputStream>
{

	/** The size of the local buffer used for skipping. */
	private final long SKIP_BUFFER_SIZE = 2048;

	/** The log level to use. */
	private final Log.Level logLevel;

	/**
	 * Decorates the given input stream with a log level of {@link Log.LEVEL#INFO}.
	 * @param inputStream The input stream to decorate.
	 * @throws NullPointerException if the given stream is <code>null</code>.
	 */
	public LogInputStream(final InputStream inputStream)
	{
		this(inputStream, Log.Level.INFO);
	}

	/**
	 * Decorates the given input stream.
	 * @param inputStream The input stream to decorate.
	 * @throws NullPointerException if the given stream and/or log level is <code>null</code>.
	 */
	public LogInputStream(final InputStream inputStream, final Log.Level logLevel)
	{
		super(inputStream); //construct the parent class
		this.logLevel = checkInstance(logLevel, "Log level cannot be null.");
	}

	/** Indicates whether the end-of-transmission has been detected and logged. */
	private boolean eot = false;

	/** Called when the end-of transmission is detected. The EOT will be logged the first time it is detected. */
	protected void logEOT()
	{
		if(!eot)
		{
			eot = true;
			Log.log(logLevel, Log.RAW_FLAG, END_OF_TRANSMISSION_SYMBOL); //EOT
		}
	}

	/** {@inheritDoc} */
	@Override
	public int read() throws IOException
	{
		final int b = super.read(); //read data normally
		if(b >= 0)
		{
			Log.log(logLevel, Log.RAW_FLAG, Character.valueOf((char)b));
		}
		else
		{
			logEOT();
		}
		return b; //return the data read
	}

	/** {@inheritDoc} */
	@Override
	public int read(byte b[]) throws IOException
	{
		final int count = super.read(b); //read data normally
		if(count > 0) //if data was read
		{
			Log.log(logLevel, Log.RAW_FLAG, new String(b, 0, count, US_ASCII_CHARSET));
		}
		else if(count < 0)
		{
			logEOT();
		}
		return count; //return the amount of data read
	}

	/** {@inheritDoc} */
	@Override
	public int read(byte b[], int off, int len) throws IOException
	{
		final int count = super.read(b, off, len); //read data normally
		if(count > 0) //if data was read
		{
			Log.log(logLevel, Log.RAW_FLAG, new String(b, off, count, US_ASCII_CHARSET));
		}
		else if(count < 0)
		{
			logEOT();
		}
		return count; //return the amount of data read
	}

	/** {@inheritDoc} This version reads and captures the skipped data. */
	@Override
	public long skip(final long n) throws IOException
	{
		final byte[] buffer = new byte[(int)min(n, SKIP_BUFFER_SIZE)]; //make a buffer only as large as needed (we can cast to an int, because we know that at least one of the values is an int, and we're taking the minimum of the two)
		final int bufferSize = buffer.length; //get the length of the buffer
		long bytesLeft = n; //we'll start out needing to read all the bytes
		int bufferBytesRead = 0; //we'll keep track of how many bytes we read each time
		while(bytesLeft > 0 && bufferBytesRead >= 0) //while there are bytes left and we haven't reached the end of the stream
		{
			bufferBytesRead = read(buffer, 0, (int)min(bytesLeft, bufferSize)); //read as many bytes as we have left, or as many as our buffer can hold, whichever is less; this will also automatically capture our data
			if(bufferBytesRead > 0) //if we read any bytes at all (this could be negative, so don't blindly subtract; but since we're checking anyway, we might as well throw out the zero case)
			{
				bytesLeft -= bufferBytesRead; //decrease the bytes left by the number read
			}
			else if(bufferBytesRead < 0)
			{
				logEOT();
			}
		}
		return n - bytesLeft; //return the number of bytes we skipped (captured), which will be the total number minus however many we have left to read, if any (if we reached the end of the stream, that is)
	}

}
