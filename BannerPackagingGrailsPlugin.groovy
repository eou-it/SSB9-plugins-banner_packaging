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


/** 
 * Plugin class for the banner-packaging plugin.
 **/ 
class BannerPackagingGrailsPlugin {
	
	// Note: the groupId 'should' be used when deploying this plugin via the 'grails maven-deploy --repository=releases' command,
    // however it is not being picked up.  Consequently, a pom.xml file is added to the root directory with the correct groupId
    // and will be removed when the maven-publisher plugin correctly sets the groupId based on the following field.
	def groupID = "sungardhe"
    def version = "1.0.2"

    def scopes = [ excludes:'war' ]

    def grailsVersion  = "1.3.7 > *"
    def dependsOn      = [:]
    def pluginExcludes = [ "grails-app/views/error.gsp" ]

    def author      = "SunGard Higher Education"
    def authorEmail = "horizon-support@sungardhe.com"
    def title       = "Banner Packaging Framework Plugin"
    def description = '''This plugin adds a package-release Grails target that creates a release package containing an installer.'''.stripMargin() 

    def documentation = ""

    def doWithWebDescriptor = { xml -> }

    def doWithSpring = { }

    def doWithDynamicMethods = { ctx -> }

    def doWithApplicationContext = { applicationContext -> }

    def onChange = { event -> }

    def onConfigChange = { event -> }

}
