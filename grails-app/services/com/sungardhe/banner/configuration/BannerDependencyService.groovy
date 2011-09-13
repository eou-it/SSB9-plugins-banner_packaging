/** *****************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.
 
 CONFIDENTIAL BUSINESS INFORMATION
 
 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */
package com.sungardhe.banner.configuration

import groovy.sql.Sql

import org.apache.log4j.Logger

import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.commons.ApplicationHolder

                                // Using this marker interface causes a side-effect, 
                                // which prevents authentication...
class BannerDependencyService { // implements ResourceLoaderAware {
       
    private final Logger log = Logger.getLogger( getClass() )
    
    Properties     releaseProperties     

    def sessionFactory   // injected by Spring
    
    private String applicationVersion
    private String buildNumber
    
    
    public void init() {
        Sql sql
        try {        
            cacheReleaseProperties( "/WEB-INF/grails-app/i18n/release.properties" )
            sql = new Sql( sessionFactory.getCurrentSession().connection() ) 
            
            log.info "Application version $applicationVersion (build $buildNumber) is initializing..."
            
            // Step 1: Validate dependencies.groovy against the installed applications
            // TODO: implement 'validateDependencies( sql ) method to validate dependencies
            
            // Step 2: Record this application version (if not yet recorded and dependencies are satisfied)
            recordVersion( sql )
        } catch (e) {
            log.error "Could not record application version within the database due to: ${e.message}"
        } finally {
            sql?.close()
        }
    }
    
    
    private recordVersion( sql ) {
        
        // TODO: Insert/update into table to record app version and build number
    }
    
    
    private cacheReleaseProperties( filePath ) {
        ApplicationContext ctx = (ApplicationContext) ApplicationHolder.getApplication().getMainContext()
        def releasePropertiesFile = ctx.getResource( filePath )?.file
        releaseProperties = new Properties()
    	releaseProperties.load( new FileInputStream( releasePropertiesFile ) )
    	
    	applicationVersion = getAppVersion()
        buildNumber = getBuildNumber()
    }
    
    
    /**
     * Returns the application version with a build number appended.
     * The returned string is suitable for displaying to the end user.
     **/
    public String getReleaseLabel() {
        def buildNum = getBuildNumber() 
        if (buildNum?.empty()) "${getAppVersion()}"
        else                   "${getAppVersion()}-${buildNum}"
    }


    /**
     * Returns the application version.
     * The application version is specified within the application's 'application.properties' file. 
     **/
    public String getAppVersion() {
        def appVersion = releaseProperties['application.version']
        appVersion instanceof String ? appVersion : ''
    }


    /**
     * Returns the internal build number. 
     * The buildNumber is a one-up number provided by a 'build number' web service. 
     * Please see the 'scripts/PackageRelease.groovy' for details concerning assigning 
     * a build number for the application.
     **/
    public String getBuildNumber() {
       def buildNum = releaseProperties['application.build.number']
       buildNum instanceof String ? buildNum : ''
    }
    
        
}
