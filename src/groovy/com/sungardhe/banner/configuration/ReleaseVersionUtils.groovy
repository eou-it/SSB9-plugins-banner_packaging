/** *****************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.configuration

import grails.util.GrailsUtil

import org.apache.log4j.Logger

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.codehaus.groovy.grails.commons.GrailsApplication


/**
 * Utilities for application configuration.
 */
abstract
class ReleaseVersionUtils {


    private static final Logger log = Logger.getLogger( "com.sungardhe.banner.configuration.ReleaseVersionUtils" )


    /**
     * Returns the application version with a build number appended.
     * The returned string is suitable for displaying to the end user.
     **/
    public static String getReleaseLabel() {
        def buildNum = getBuildNumber() 
        if (buildNum?.empty()) "${getAppVersion()}"
        else                   "${getAppVersion()}-${buildNum}"
    }


    /**
     * Returns the application version.
     * The application version is specified within the application's 'application.properties' file. 
     * Note: This application version is recorded in the database during initialization.
     * @see com.sungardhe.banner.configuration.BannerDependencyService.init()
     **/
    public static String getAppVersion() {
        AH.application.metadata[ 'app.version' ]
    }
    
    
    /**
     * Returns the internal build number. 
     * The buildNumber is a one-up number provided by a 'build number' web service. 
     * Please see the 'scripts/BuildRelease.groovy' for details concerning assigning 
     * a build number for the application.
     * Note: This application version is recorded in the database during initialization.
     * @see com.sungardhe.banner.configuration.BannerDependencyService.init()
     **/
    public static String getBuildNumber() {
        def buildNum = getProperties( "release.properties" )?.getProperty( "application.build.number" )
println "XXXXXXXXXX buildNum = $buildNum"
buildNum = getProperties( "grails-app/i18n/release.properties" )?.getProperty( "application.build.number" )
println "XXXXXXXXXX buildNum = $buildNum"
        (buildNum instanceof String && buildNum != 'Unassigned') ? buildNum : ''
    }


    private static Properties getProperties( String filePath ) {

		Properties p = new Properties()
		try {
		    def fs = new FileInputStream( filePath )
            p.load( fs )
            println "XXXX Using filepath $filePath, found properties '$p with ${p.getProperty('application.version')}" 
            return p
    	} catch (e) {
    	    // We'll log this, but it may be ok that we couldn't find a file... a rare case where we'll bury the exception...
    	    log.error "Caught (and will bury) ${e.message}", e
    	}
    	
        if (p.getProperties().size() == 0) {
            def foundProps = Thread.currentThread().getContextClassLoader().getResource( "$filePath" )//?.toURI()
println "XXXX Found $filePath on classpath: $foundProps "
            if (foundProps) p.load foundProps
println "XXXX Using classpath, found properties '$p' with application.version ${p.getProperty('application.version')}" 
        } else {
println "XXXXX properties already have values... ${p.getProperties().size()}"
        }
println "XXXXXXXX getProperties( '$filePath' ) is RETURNING $p"        
        p   
	}


}
