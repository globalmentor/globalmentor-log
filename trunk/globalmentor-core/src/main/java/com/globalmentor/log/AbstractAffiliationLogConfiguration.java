/*
 * Copyright © 2009 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

import static com.globalmentor.java.Objects.*;

/**An abstract logging configuration that allows several predefined ways to affiliate loggers with classes.
By default this class using {@link Affiliation#PACKAGE} to affiliate loggers with the package of the requesting class.
@author Garret Wilson
*/
public abstract class AbstractAffiliationLogConfiguration extends AbstractLogConfiguration<Object>
{

	/**Indicates the granularity with which loggers are associated with classes.*/
	public enum Affiliation
	{
		/**Loggers are associated with individual classes.*/
		CLASS,

		/**Loggers are associated with packages.*/
		PACKAGE;
	}

	/**The granularity with which loggers are associated with classes.*/
	private Affiliation affiliation;

		/**@return The granularity with which loggers are associated with classes.*/
		public Affiliation getAffiliation() {return affiliation;}

		/**Sets the granularity with which loggers are associated with classes.
		@param affiliation The granularity with which loggers are associated with classes.
		*/
		public void setAffiliation(final Affiliation affiliation) {this.affiliation=checkInstance(affiliation, "Affiliation cannot be null.");}

	/**Determines the object related to the given class with which a logger should be associated.
	This could be the package of the class if loggers are grouped according to package,
	or the class itself if loggers are configured on a fine-grained level.
	<p>This implementation returns a key based upon the logger affiliation.</p>
 	@param objectClass The specific class for which a logger key should be returned.
	@return A new association for this class around which to group loggers.
	@throws NullPointerException if the given class is <code>null</code>.
	@see #getAffiliation()
	*/
	protected Object getLoggerKey(final Class<?> objectClass)
	{
		final Affiliation affiliation=getAffiliation();
		switch(affiliation)
		{
			case CLASS:
				return checkInstance(objectClass);
			case PACKAGE:
				return objectClass.getPackage();
			default:
				throw new AssertionError("Unrecognized affiliation: "+affiliation);
		}
	}

	/**Common logger support constructor.
	Affiliation defaults to {@link Affiliation#PACKAGE}.
	@param supportCommonLogger Whether a common logger is used if no specific logger registrations have yet been made.
	*/
	public AbstractAffiliationLogConfiguration(final boolean supportCommonLogger)
	{
		super(supportCommonLogger);
		this.affiliation=Affiliation.PACKAGE;
	}

}
