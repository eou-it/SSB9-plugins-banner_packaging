/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
package com.sungardhe.banner.configuration

import groovy.sql.Sql

import org.apache.log4j.Logger

import org.springframework.context.ApplicationContext
import org.codehaus.groovy.grails.commons.ApplicationHolder

                                                                 
class BannerDependencyService { 
       
    private final Logger log = Logger.getLogger( getClass() )
    
    Properties releaseProperties 
    def sessionFactory   // injected by Spring
    
    private String  applicationVersion
    private String  buildNumber
    private String  appName
    
    def init() {
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
        def currentRelease = sql.firstRow('select gurwapp_release, gurwapp_build_no from gurwapp where gurwapp_application_name = ?',[appName])
        log.debug("RecordVersion ${currentRelease}")
        if (!currentRelease)  {
            sql.execute('insert into gurwapp (gurwapp_application_name, gurwapp_release, gurwapp_build_no, gurwapp_stage_date, gurwapp_user_id, gurwapp_activity_date) values (?,?,?,sysdate,user,sysdate)', [appName, applicationVersion,buildNumber])
            sql.commit()
        }
        else
            if ((currentRelease.gurwapp_release != applicationVersion) || (currentRelease.gurwapp_build_no != buildNumber)) {
                sql.executeUpdate('update gurwapp set gurwapp_release = ?,gurwapp_build_no = ? where gurwapp_application_name=? ', [applicationVersion,buildNumber,appName])
                sql.commit()
            }

    }
    
    
    private cacheReleaseProperties( filePath ) {
        ApplicationContext ctx = (ApplicationContext) ApplicationHolder.getApplication().getMainContext()
        def releasePropertiesFile = ctx.getResource( filePath )?.file
        releaseProperties = new Properties()
    	releaseProperties.load( new FileInputStream( releasePropertiesFile ) )
    	
    	applicationVersion = getAppVersion()
        buildNumber = getBuildNumber()
        appName = getAppName()
    }
    
    /**
     * Returns the application version.
     * The application version is specified within the application's 'application.properties' file.
     **/
    public String getAppName() {
        def appName = releaseProperties['application.name']
        appName instanceof String ? appName : ''
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
