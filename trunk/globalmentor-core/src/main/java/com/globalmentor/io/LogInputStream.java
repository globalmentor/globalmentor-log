/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
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
import static com.globalmentor.java.Objects.*;
import com.globalmentor.log.Log;

/**An input stream that logs all transferred bytes of a decorated stream.
@author Garret Wilson
@see Log
*/
public class LogInputStream extends InputStreamDecorator<InputStream>
{

	/**The size of the local buffer used for skipping.*/
	private final long SKIP_BUFFER_SIZE=2048;

	/**The log level to use.*/
	private final Log.Level logLevel;

	/**Decorates the given input stream with a log level of {@link Log.LEVEL#INFO}.
	@param inputStream The input stream to decorate.
	@exception NullPointerException if the given stream is <code>null</code>.
	*/
	public LogInputStream(final InputStream inputStream)
	{
		this(inputStream, Log.Level.INFO);
	}

	/**Decorates the given input stream.
	@param inputStream The input stream to decorate.
	@exception NullPointerException if the given stream and/or log level is <code>null</code>.
	*/
	public LogInputStream(final InputStream inputStream, final Log.Level logLevel)
	{
		super(inputStream);	//construct the parent class
		this.logLevel=checkInstance(logLevel, "Log level cannot be null.");
	}

  /**
   * Reads the next byte of data from the input stream. The value byte is
   * returned as an <code>int</code> in the range <code>0</code> to
   * <code>255</code>. If no byte is available because the end of the stream
   * has been reached, the value <code>-1</code> is returned. This method
   * blocks until input data is available, the end of the stream is detected,
   * or an exception is thrown.
   *
   * <p> A subclass must provide an implementation of this method.
   *
   * @return     the next byte of data, or <code>-1</code> if the end of the
   *             stream is reached.
   * @exception  IOException  if an I/O error occurs.
   */
  public int read() throws IOException
	{
  	final int b=super.read();	//read data normally
  	Log.log(logLevel, Character.valueOf((char)b));
  	return b;	//return the data read
	}

  /**
   * Reads some number of bytes from the input stream and stores them into
   * the buffer array <code>b</code>. The number of bytes actually read is
   * returned as an integer.  This method blocks until input data is
   * available, end of file is detected, or an exception is thrown.
   *
   * <p> If <code>b</code> is <code>null</code>, a
   * <code>NullPointerException</code> is thrown.  If the length of
   * <code>b</code> is zero, then no bytes are read and <code>0</code> is
   * returned; otherwise, there is an attempt to read at least one byte. If
   * no byte is available because the stream is at end of file, the value
   * <code>-1</code> is returned; otherwise, at least one byte is read and
   * stored into <code>b</code>.
   *
   * <p> The first byte read is stored into element <code>b[0]</code>, the
   * next one into <code>b[1]</code>, and so on. The number of bytes read is,
   * at most, equal to the length of <code>b</code>. Let <i>k</i> be the
   * number of bytes actually read; these bytes will be stored in elements
   * <code>b[0]</code> through <code>b[</code><i>k</i><code>-1]</code>,
   * leaving elements <code>b[</code><i>k</i><code>]</code> through
   * <code>b[b.length-1]</code> unaffected.
   *
   * <p> If the first byte cannot be read for any reason other than end of
   * file, then an <code>IOException</code> is thrown. In particular, an
   * <code>IOException</code> is thrown if the input stream has been closed.
   *
   * <p> The <code>read(b)</code> method for class <code>InputStream</code>
   * has the same effect as: <pre><code> read(b, 0, b.length) </code></pre>
   *
   * @param      b   the buffer into which the data is read.
   * @return     the total number of bytes read into the buffer, or
   *             <code>-1</code> is there is no more data because the end of
   *             the stream has been reached.
   * @exception  IOException  if an I/O error occurs.
   * @exception  NullPointerException  if <code>b</code> is <code>null</code>.
   * @see        java.io.InputStream#read(byte[], int, int)
   */
  public int read(byte b[]) throws IOException
	{
  	final int count=super.read(b);	//read data normally
  	if(count>0)	//if data was read
  	{
    	Log.log(logLevel, new String(b, US_ASCII_CHARSET));
  	}
  	return count;	//return the amount of data read
	}

  /**
   * Reads up to <code>len</code> bytes of data from the input stream into
   * an array of bytes.  An attempt is made to read as many as
   * <code>len</code> bytes, but a smaller number may be read.
   * The number of bytes actually read is returned as an integer.
   *
   * <p> This method blocks until input data is available, end of file is
   * detected, or an exception is thrown.
   *
   * <p> If <code>b</code> is <code>null</code>, a
   * <code>NullPointerException</code> is thrown.
   *
   * <p> If <code>off</code> is negative, or <code>len</code> is negative, or
   * <code>off+len</code> is greater than the length of the array
   * <code>b</code>, then an <code>IndexOutOfBoundsException</code> is
   * thrown.
   *
   * <p> If <code>len</code> is zero, then no bytes are read and
   * <code>0</code> is returned; otherwise, there is an attempt to read at
   * least one byte. If no byte is available because the stream is at end of
   * file, the value <code>-1</code> is returned; otherwise, at least one
   * byte is read and stored into <code>b</code>.
   *
   * <p> The first byte read is stored into element <code>b[off]</code>, the
   * next one into <code>b[off+1]</code>, and so on. The number of bytes read
   * is, at most, equal to <code>len</code>. Let <i>k</i> be the number of
   * bytes actually read; these bytes will be stored in elements
   * <code>b[off]</code> through <code>b[off+</code><i>k</i><code>-1]</code>,
   * leaving elements <code>b[off+</code><i>k</i><code>]</code> through
   * <code>b[off+len-1]</code> unaffected.
   *
   * <p> In every case, elements <code>b[0]</code> through
   * <code>b[off]</code> and elements <code>b[off+len]</code> through
   * <code>b[b.length-1]</code> are unaffected.
   *
   * <p> If the first byte cannot be read for any reason other than end of
   * file, then an <code>IOException</code> is thrown. In particular, an
   * <code>IOException</code> is thrown if the input stream has been closed.
   *
   * <p> The <code>read(b,</code> <code>off,</code> <code>len)</code> method
   * for class <code>InputStream</code> simply calls the method
   * <code>read()</code> repeatedly. If the first such call results in an
   * <code>IOException</code>, that exception is returned from the call to
   * the <code>read(b,</code> <code>off,</code> <code>len)</code> method.  If
   * any subsequent call to <code>read()</code> results in a
   * <code>IOException</code>, the exception is caught and treated as if it
   * were end of file; the bytes read up to that point are stored into
   * <code>b</code> and the number of bytes read before the exception
   * occurred is returned.  Subclasses are encouraged to provide a more
   * efficient implementation of this method.
   *
   * @param      b     the buffer into which the data is read.
   * @param      off   the start offset in array <code>b</code>
   *                   at which the data is written.
   * @param      len   the maximum number of bytes to read.
   * @return     the total number of bytes read into the buffer, or
   *             <code>-1</code> if there is no more data because the end of
   *             the stream has been reached.
   * @exception  IOException  if an I/O error occurs.
   * @exception  NullPointerException  if <code>b</code> is <code>null</code>.
   * @see        java.io.InputStream#read()
   */
  public int read(byte b[], int off, int len) throws IOException
	{
  	final int count=super.read(b, off, len);	//read data normally
  	if(count>0)	//if data was read
  	{
    	Log.log(logLevel, new String(b, off, len, US_ASCII_CHARSET));
  	}
  	return count;	//return the amount of data read
	}

  /**
   * Skips over and discards <code>n</code> bytes of data from this input
   * stream. The <code>skip</code> method may, for a variety of reasons, end
   * up skipping over some smaller number of bytes, possibly <code>0</code>.
   * This may result from any of a number of conditions; reaching end of file
   * before <code>n</code> bytes have been skipped is only one possibility.
   * The actual number of bytes skipped is returned.  If <code>n</code> is
   * negative, no bytes are skipped.
   *
   * This version reads and captures the skipped data. 
   *
   * @param      n   the number of bytes to be skipped.
   * @return     the actual number of bytes skipped.
   * @exception  IOException  if an I/O error occurs.
   */
  public long skip(final long n) throws IOException
	{
  	final byte[] buffer=new byte[(int)min(n, SKIP_BUFFER_SIZE)];	//make a buffer only as large as needed (we can cast to an int, because we know that at least one of the values is an int, and we're taking the minimum of the two)
  	final int bufferSize=buffer.length;	//get the length of the buffer
  	long bytesLeft=n;	//we'll start out needing to read all the bytes
  	int bufferBytesRead=0;	//we'll keep track of how many bytes we read each time
  	while(bytesLeft>0 && bufferBytesRead>=0)	//while there are bytes left and we haven't reached the end of the stream
  	{
  		bufferBytesRead=read(buffer, 0, (int)min(bytesLeft, bufferSize));	//read as many bytes as we have left, or as many as our buffer can hold, whichever is less; this will also automatically capture our data
	  	if(bufferBytesRead>0)	//if we read any bytes at all (this could be negative, so don't blindly subtract; but since we're checking anyway, we might as well throw out the zero case)
	  	{
	  		bytesLeft-=bufferBytesRead;	//decrease the bytes left by the number read
	  	}
  	}
  	return n-bytesLeft;	//return the number of bytes we skipped (captured), which will be the total number minus however many we have left to read, if any (if we reached the end of the stream, that is)
	}
	
}
