/*****************************************************************************************
 * Copyright 2009 - 2014 Ellucian Company L.P. and its affiliates.                       *
 *****************************************************************************************/

grails.project.class.dir        = "target/classes"
grails.project.test.class.dir   = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolver="maven"

grails.project.dependency.resolution = {
	
    inherits("global") { }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    distribution = {
    }

    repositories {
        if (System.properties['PROXY_SERVER_NAME']) {
            mavenRepo "${System.properties['PROXY_SERVER_NAME']}"
        } else
        {
            grailsCentral()
            mavenCentral()
            mavenRepo "http://repository.jboss.org/maven2/"
            mavenRepo "http://repository.codehaus.org"
        }
    }

    plugins {
        test ':code-coverage:2.0.3-3',
        {
            excludes 'xercesImpl'
        }
    }

    dependencies {
	    // This plugin builds a non-grails application.  See the _Events.groovy file for the actual depdency list.
    }
}
