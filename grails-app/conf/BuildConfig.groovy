/*******************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 *******************************************************************************/

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
//grails.project.war.file = "target/${appName}-${appVersion}.war"
grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
	
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    distribution = {
         localRepository = ""
         remoteRepository( id:"releases", url:"http://m038083.sungardhe.com:8081/nexus/content/repositories/releases" ) {
             authentication  username:'admin', password:'admin123'
         }
    }

    repositories {
        mavenRepo "http://m038083.sungardhe.com:8081/nexus/content/repositories/releases/"
        mavenRepo "http://m038083.sungardhe.com:8081/nexus/content/repositories/snapshots/"
        mavenRepo "http://m038083.sungardhe.com:8081/nexus/content/repositories/thirdparty/"
        
        grailsPlugins()
        grailsHome()
        grailsCentral()
        
        mavenCentral()
        mavenRepo "http://repository.jboss.org/maven2/"
        mavenRepo "http://repository.codehaus.org"

        // uncomment the below to enable remote dependency resolution
        // from public Maven repositories
        //mavenLocal()
        //mavenCentral()
        //mavenRepo "http://snapshots.repository.codehaus.org"
        //mavenRepo "http://repository.codehaus.org"
        //mavenRepo "http://download.java.net/maven/2/"
        //mavenRepo "http://repository.jboss.com/maven2/"
    }
    dependencies {
	    // This plugin builds a non-grails application.  See the _Events.groovy file for the actual depdency list.

        // specify dependencies here under either 'build', 'compile', 'runtime', 'test' or 'provided' scopes eg.

        // runtime 'mysql:mysql-connector-java:5.1.5'
    }
}
