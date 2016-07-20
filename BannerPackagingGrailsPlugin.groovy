/* *******************************************************************************
 Copyright 2009-2012 SunGard Higher Education. All Rights Reserved.
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
	
    def version = "9.18"

    def scopes = [ excludes:'war' ]

    def grailsVersion  = "2.2.1 > *"
    def dependsOn      = [:]
    def pluginExcludes = [ "grails-app/views/error.gsp" ]

    def author      = "ellucian"
    def authorEmail = "banner9-support@ellucian.com"
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
