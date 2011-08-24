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

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.commons.GrailsApplication


class BannerDependencyService {
       
    private final Logger log = Logger.getLogger( getClass() )

    def sessionFactory   // injected by Spring
    
    
    def init() {
        
        // 1. Validate dependencies.groovy against the installed applications
        log.info "(Note: Runtime dependency validation is not yet supported)"
                
        // 2. Record this application version (if not yet recorded)
        Sql sql = new Sql( sessionFactory.getCurrentSession().connection() )
        try {
            log.error "BannerDependencyService not yet implemented!"
            
        } catch (e) {
            log.error "Could not record application version within the database due to: ${e.message}", e
        } finally {
            sql?.close()
        }
    }
    
}
