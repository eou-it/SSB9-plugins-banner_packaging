/*******************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

import com.sungardhe.banner.configuration.BannerDependencyService 

/** 
 * Plugin class for the banner-packaging plugin.
 **/ 
class BannerPackagingGrailsPlugin {
	
	// Note: the groupId 'should' be used when deploying this plugin via the 'grails maven-deploy --repository=releases' command,
    // however it is not being picked up.  Consequently, a pom.xml file is added to the root directory with the correct groupId
    // and will be removed when the maven-publisher plugin correctly sets the groupId based on the following field.
	def groupID = "sungardhe"
    def version = "0.0.1.5"
    
    def scopes = [ excludes:'war' ]

    def grailsVersion  = "1.3.5 > *"
    def dependsOn      = [:]
    def pluginExcludes = [ "grails-app/views/error.gsp" ]

    def author      = "SunGard Higher Education"
    def authorEmail = "horizon-support@sungardhe.com"
    def title       = "Banner Packaging Framework Plugin"
    def description = '''This plugin adds a package-release Grails target that creates a release package containing an installer.'''.stripMargin() 

    def documentation = ""

    def doWithWebDescriptor = { xml -> }

    def doWithSpring = { 
        bannerDependencyService( BannerDependencyService ) { bean ->
            bean.initMethod = 'init'
        }    
    }

    def doWithDynamicMethods = { ctx -> }

    def doWithApplicationContext = { applicationContext -> }

    def onChange = { event -> }

    def onConfigChange = { event -> }

}
