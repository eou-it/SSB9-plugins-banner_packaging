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
package com.sungardhe.banner.installer.actions

import org.springframework.beans.factory.annotation.Required;
import com.sungardhe.commoncomponents.installer.*
import org.apache.tools.ant.taskdefs.*
import org.apache.tools.ant.types.*
import com.sungardhe.banner.installer.*
import java.text.*

/**
 * Installer action for deploying the war to tomcat.
 **/
public class DeployTomcat extends DefaultAction {
	
	private StringResource tomcatMgrUrl
	private StringResource tomcatUser
	private StringResource tomcatPassword


    public String getNameResourceCode() {
        "installer.action.DeployTomcat.name"
    }
    

    @Required // 'http://localhost:8080/manager'
    public void setTomcatMgrUrl( StringResource tomcatMgrUrl ) {
        this.tomcatMgrUrl = tomcatMgrUrl
        addRequiredResource( this.tomcatMgrUrl )
    }
    

    @Required // 'manager'
    public void setTomcatUser( StringResource tomcatUser ) {
        this.tomcatUser = tomcatUser
        addRequiredResource( this.tomcatUser )
    }
    

    @Required //  'secret'
    public void setTomcatPassword( StringResource tomcatPassword ) {
        this.tomcatPassword = tomcatPassword
        addRequiredResource( this.tomcatPassword )
    }
        

    public void execute() throws ActionRunnerException {

        def ant = new AntBuilder()
        ant.taskdef( name:'deploy',   classname:'org.apache.catalina.ant.DeployTask' )
        ant.taskdef( name:'list',     classname:'org.apache.catalina.ant.ListTask' )
        ant.taskdef( name:'undeploy', classname:'org.apache.catalina.ant.UndeployTask' )

		File templateWar = getTemplate()
		def warName = templateWar.getName()
		def warFile = resolveFile( "${FileStructure.DIST_DIR}/${warName}" )
		String contextName = getReleaseProperties().getProperty( "application.name" )
		
		updateProgress( new DeployTomcatStartMessage( warFile.getName() ) )
		
		ant.echo "${warFile?.getPath()}"
		
	    ant.deploy( war:      warFile?.getPath(),
			        url:      tomcatMgrUrl.getValue(),
			        path:     "/$contextName",
			        username: tomcatUser.getValue(),
			        password: tomcatPassword.getValue() )
			    
	    updateProgress( new DeployTomcatCompleteMessage( warFile.getName() ) )
	}


	private File getTemplate() {
		File dir = resolveFile( FileStructure.WEBAPP_DIR )
		String[] names = dir.list()
		if (names.length != 1) {
			throw new RuntimeException( "$dir must contain a single war" )
		}
		new File( dir, names[0] )		
	}
	
		
	private Properties getReleaseProperties() {
		getProperties( "${FileStructure.I18N_DIR}/release.properties" )
	}


	private class DeployTomcatStartMessage extends ProgressMessage {
	    private static final String RESOURCE_CODE = "installer.message.deploy_tomcat_start"
	
	    DeployTomcatStartMessage( String name ) {
	        super( RESOURCE_CODE, [ name ] )
	    }
	}
	
	
	private class DeployTomcatCompleteMessage extends ProgressMessage {
	    private static final String RESOURCE_CODE = "installer.message.deploy_tomcat_complete"
	
	    DeployTomcatCompleteMessage( String name ) {
	        super( RESOURCE_CODE, [ name ] )
	    }
	}
	
}


// ============================
/*		
		case 'list':
		    list(  url:url,
				   username:user,
				   password:pass)


		case 'undeploy':
			configureServerContextPath()
			println "Undeploying application $serverContextPath from Tomcat"
			println '''\
  
  NOTE: If you experience a classloading error during undeployment you need to take the following steps:					

* Upgrade to Tomcat 6.0.20 or above
* Pass this system argument to Tomcat: -Dorg.apache.catalina.loader.WebappClassLoader.ENABLE_CLEAR_REFERENCES=false

See http://tomcat.apache.org/tomcat-6.0-doc/config/systemprops.html for more information
'''
		    undeploy(
				   url:url,
				   path:serverContextPath,
				   username:user,
				   password:pass)		
		
	}

} */