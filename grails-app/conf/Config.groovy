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

 grails.doc.authors = '''Prepared by: SunGard Higher Education
                         |4 Country View Road Malvern, Pennsylvania 19355 United States of America (800) 522 - 4827'''.stripMargin()

 grails.doc.footer = '''Contains confidential and proprietary information of SunGard and its subsidiaries.'''

 grails.doc.license = '''Use of these materials is limited to SunGard Higher Education licensees, and is subject to the terms and conditions of one or more written license agreements between SunGard Higher Education and the licensee in question.
                         |In preparing and providing this publication, SunGard Higher Education is not rendering legal, accounting, or other similar professional services. SunGard Higher Education makes no claims that an institution's use of this publication or the software for which it is provided will insure compliance with applicable federal or state laws, rules, or regulations. Each organization should seek legal, accounting and other similar professional services from competent providers of the organization’s own choosing.
                         |'''.stripMargin()

 grails.doc.copyright = '''© 2010-2011 SunGard. All rights reserved.
                           |Use of these materials is limited to SunGard Higher Education licensees, and is subject to the terms and conditions of one or more written license agreements between SunGard Higher Education and the licensee in question.
                           |In preparing and providing this publication, SunGard Higher Education is not rendering legal, accounting, or other similar professional services. SunGard Higher Education makes no claims that an institution's use of this publication or the software for which it is provided will insure compliance with applicable federal or state laws, rules, or regulations. Each organization should seek legal, accounting and other similar professional services from competent providers of the organization’s own choosing.
                           |'''.stripMargin()

 grails.doc.images = new File( "src/docs/images" )
 grails.doc.logo = '''<img src="../img/main-banner-image.png">'''
 grails.doc.sponsorLogo = '''<img src="../img/sghe_global.logo.jpg">'''

 grails.doc.alias.release_notes = "1. Release Notes"
 grails.doc.alias.user          = "2. User Guide"
 grails.doc.alias.release       = "2.1 Building the Release Package"
 grails.doc.alias.install       = "2.2 Installing into a 'Home'"
 grails.doc.alias.war           = "2.3 Re-generating the War File"
 grails.doc.alias.deploytomcat  = "2.4 Deploying the War to Tomcat"

 
// configuration for plugin testing - will not be included in the plugin zip 
log4j = {

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
}

// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

/* remove this line 
// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside null
                scriptlet = 'none' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}
remove this line */
