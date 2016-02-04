/**
 * @copyright
 * ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one
 *    or more contributor license agreements.  See the NOTICE file
 *    distributed with this work for additional information
 *    regarding copyright ownership.  The ASF licenses this file
 *    to you under the Apache License, Version 2.0 (the
 *    "License"); you may not use this file except in compliance
 *    with the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 * ====================================================================
 * @endcopyright
 */

package org.apache.subversion.javahl;

import java.io.File;

import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.javahl.AbstractJhlClientAdapter;
import org.tigris.subversion.svnclientadapter.javahl.JhlClientAdapter;

/**
 * This checked exception is thrown whenever something goes wrong with
 * the Subversion JavaHL bindings.
 */
public class SubversionException extends Exception
{
    // Update the serialVersionUID when there is a incompatible change
    // made to this class.  See any of the following, depending upon
    // the Java release.
    // http://java.sun.com/j2se/1.3/docs/guide/serialization/spec/version.doc7.html
    // http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf
    // http://java.sun.com/j2se/1.5.0/docs/guide/serialization/spec/version.html#6678
    // http://java.sun.com/javase/6/docs/platform/serialization/spec/version.html#6678
    private static final long serialVersionUID = 1L;

    /**
     * This constructor is only used by sub-classes.
     *
     * @param message A description of the problem.
     */
   private  String message;
    protected SubversionException(String message)
    /*
     * The working copy needs to be upgraded
svn: The working copy at 'F:\lizhi\runtime-New_configuration\test003'
is too old (format 10) to work with client version '1.8.8 (r1568071)' (expects format 31). You need to upgrade the working copy first.
     * 
     * 
     */
    
    
    
    {
    	super(message);
    	if(message.contains("The working copy needs to be upgraded")){
    		
    		int startIndex=message.indexOf("'");
    		message=message.substring(startIndex+1, message.length());
    		message=message.substring(0,message.indexOf("'"));
    		AbstractJhlClientAdapter adapter=new JhlClientAdapter();
    		File file=new File(message);
    		adapter.upgradeProject(file);
    	/*	try {
				adapter.revert(file, true);
			} catch (SVNClientException e) {
				e.printStackTrace();
			}*/
    		
    	}
    	this.message=message;
    }
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
    
}
