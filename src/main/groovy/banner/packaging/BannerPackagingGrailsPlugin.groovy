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
package banner.packaging

import grails.plugins.*

class BannerPackagingGrailsPlugin extends Plugin {
    def version = "9.18.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.3.2 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

   
    def author      = "ellucian"
    def authorEmail = "banner9-support@ellucian.com"
    def title       = "Banner Packaging Framework Plugin"
    def description = '''This plugin adds a package-release Grails target that creates a release package containing an installer.'''.stripMargin() 
    def profiles = ['web']

    Closure doWithSpring() { {->
            // no-op
        }
    }

    void doWithDynamicMethods() {
       // no-op
    }

    void doWithApplicationContext() {
        // no-op
    }

    void onChange(Map<String, Object> event) {
       // no-op
    }

    void onConfigChange(Map<String, Object> event) {
        // no-op
    }

    void onShutdown(Map<String, Object> event) {
        // no-op
    }
}
