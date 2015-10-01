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

import static com.globalmentor.java.Characters.*;
import static com.globalmentor.java.Strings.*;

import com.globalmentor.event.*;

/**
 * A progress listener that logs any progress reported. If a progress bar is included, the progress logged will include controlled carriage returns to allow
 * same-line updates to the console.
 * 
 * @author Garret Wilson
 * 
 * @see Log#info(Object...)
 */
public class LogProgressListener implements ProgressListener {

	/** Whether a progress bar should be included in the log if possible. */
	private final boolean progressBarLogged;

	/** @return Whether a progress bar should be included in the log if possible. */
	public boolean isProgressBarLogged() {
		return progressBarLogged;
	}

	/**
	 * Default constructor with progress bar included.
	 */
	public LogProgressListener() {
		this(true);
	}

	/**
	 * Progress bar constructor.
	 * @param progressBarLogged Whether a progress bar should be included in the log.
	 */
	public LogProgressListener(final boolean progressBarLogged) {
		this.progressBarLogged = progressBarLogged;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This version logs information regarding the progress event in the form <code>XX........ X% (123/1000)<code>.
	 * </p>
	 * @see #isProgressBarLogged()
	 * @see Log#info(Object...)
	 */
	public void progressed(final ProgressEvent progressEvent) {
		String logString = progressEvent.toString(); //start with the default progress event
		final long value = progressEvent.getValue();
		final long maximum = progressEvent.getMaximum();
		if(isProgressBarLogged() && value >= 0 && maximum >= 0) { //if we should and can show a progress bar 
			final int completeLogStringLength = new ProgressEvent(progressEvent.getSource(), maximum, maximum).toString().length() + 2; //see how long the log string would be when the activity is complete (counting parentheses)
			logString = makeStringLength("(" + logString + ")", completeLogStringLength, ' ', 0); //add parentheses pad the log string so it will align with the finished log string
			final StringBuilder stringBuilder = new StringBuilder(progressEvent.getProgressBarString()); //XX........
			final long percent = value * 100 / maximum; //0-100
			final String percentString = makeStringLength(Long.toString(percent), 3, ' ', 0); //pad the percent with spaces so they will align
			stringBuilder.append(' ').append(percentString).append('%').append(' ').append(logString); // 23% (123/1000)
			if(value == maximum) { //if we finished with progress
				stringBuilder.append(LINE_FEED_CHAR); //skip to the next line; we're finished with the progress update
			}
			stringBuilder.append(CARRIAGE_RETURN_CHAR); //add a carriage return so that the next update will overwrite this one, allowing us to update the same line over and over for the progress bar
			logString = stringBuilder.toString(); //replace the log string with the expanded, progress bar version
		}
		Log.info(Log.RAW_FLAG, logString); //log the information, indicating that want to control our own line breaks and have no prefix
	}
}
