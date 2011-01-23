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

import static com.globalmentor.io.Charsets.*;
import static com.globalmentor.java.Objects.*;
import com.globalmentor.log.Log;

/**An output stream that logs all transferred bytes of a decorated stream.
@author Garret Wilson
@see Log
*/
public class LogOutputStream extends OutputStreamDecorator<OutputStream>
{

	/**The log level to use.*/
	private final Log.Level logLevel;

	/**Decorates the given output stream with a report level of {@link Log.Level#INFO}.
	@param outputStream The output stream to decorate.
	@exception NullPointerException if the given stream is <code>null</code>.
	*/
	public LogOutputStream(final OutputStream outputStream)
	{
		this(outputStream, Log.Level.INFO);
	}

	/**Decorates the given output stream.
	@param outputStream The output stream to decorate.
	@exception NullPointerException if the given stream and/or log level is <code>null</code>.
	*/
	public LogOutputStream(final OutputStream outputStream, final Log.Level logLevel)
	{
		super(outputStream);	//construct the parent class
		this.logLevel=checkInstance(logLevel, "Log level cannot be null.");
	}

  /**
   * Writes the specified byte to this output stream. The general 
   * contract for <code>write</code> is that one byte is written 
   * to the output stream. The byte to be written is the eight 
   * low-order bits of the argument <code>b</code>. The 24 
   * high-order bits of <code>b</code> are ignored.
   * <p>
   * Subclasses of <code>OutputStream</code> must provide an 
   * implementation for this method. 
   *
   * @param      b   the <code>byte</code>.
   * @exception  IOException  if an I/O error occurs. In particular, 
   *             an <code>IOException</code> may be thrown if the 
   *             output stream has been closed.
   */
	public void write(int b) throws IOException
	{
  	Log.log(logLevel, Character.valueOf((char)b));
		super.write(b);	//do the default writing
	}

  /**
   * Writes <code>b.length</code> bytes from the specified byte array 
   * to this output stream. The general contract for <code>write(b)</code> 
   * is that it should have exactly the same effect as the call 
   * <code>write(b, 0, b.length)</code>.
   *
   * @param      b   the data.
   * @exception  IOException  if an I/O error occurs.
   * @see        java.io.OutputStream#write(byte[], int, int)
   */
  public void write(byte b[]) throws IOException
	{
  	Log.log(logLevel, new String(b, US_ASCII_CHARSET));
		super.write(b);	//do the default writing
  }

  /**
   * Writes <code>len</code> bytes from the specified byte array 
   * starting at offset <code>off</code> to this output stream. 
   * The general contract for <code>write(b, off, len)</code> is that 
   * some of the bytes in the array <code>b</code> are written to the 
   * output stream in order; element <code>b[off]</code> is the first 
   * byte written and <code>b[off+len-1]</code> is the last byte written 
   * by this operation.
   * <p>
   * The <code>write</code> method of <code>OutputStream</code> calls 
   * the write method of one argument on each of the bytes to be 
   * written out. Subclasses are encouraged to override this method and 
   * provide a more efficient implementation. 
   * <p>
   * If <code>b</code> is <code>null</code>, a 
   * <code>NullPointerException</code> is thrown.
   * <p>
   * If <code>off</code> is negative, or <code>len</code> is negative, or 
   * <code>off+len</code> is greater than the length of the array 
   * <code>b</code>, then an <tt>IndexOutOfBoundsException</tt> is thrown.
   *
   * @param      b     the data.
   * @param      off   the start offset in the data.
   * @param      len   the number of bytes to write.
   * @exception  IOException  if an I/O error occurs. In particular, 
   *             an <code>IOException</code> is thrown if the output 
   *             stream is closed.
   */
  public void write(byte b[], int off, int len) throws IOException
	{
  	Log.log(logLevel, new String(b, off, len, US_ASCII_CHARSET));
		super.write(b, off, len);	//do the default writing
  }

}
