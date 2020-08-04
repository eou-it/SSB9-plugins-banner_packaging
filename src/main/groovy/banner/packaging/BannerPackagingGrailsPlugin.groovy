/*******************************************************************************
 Copyright 2020 Ellucian Company L.P. and its affiliates.
 *******************************************************************************/


/**
package banner.packaging

import grails.plugins.*

class BannerPackagingGrailsPlugin extends Plugin {
    def version = "9.18.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.3.11 > *"
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
}**/
