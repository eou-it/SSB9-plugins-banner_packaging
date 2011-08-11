/*******************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 *******************************************************************************/
package com.sungardhe.banner.installer.actions

import org.springframework.beans.factory.annotation.Required;
import com.sungardhe.commoncomponents.installer.*
import org.apache.tools.ant.taskdefs.*
import org.apache.tools.ant.types.*
import com.sungardhe.banner.installer.*
import java.text.*

/**
 * Installer action for assembling a deployable ear from a template.
 **/
public class InstallHome extends DefaultAction {
	
	private StringResource home
	private StringResource sharedConfigHome
	private File currentDir


    public String getNameResourceCode() {
        "installer.action.InstallHome.name"
    }
    

    @Required
    public void setHome( StringResource home ) {
        this.home = home
        addRequiredResource( this.home )
    }
    

    @Required
    public void setSharedConfigHome( StringResource sharedConfigHome ) {
        this.sharedConfigHome = sharedConfigHome
        addRequiredResource( this.sharedConfigHome )
    }
    

    public void execute() throws ActionRunnerException {
        
		File homeDir = resolveFile( home.getValue() )
		if (!homeDir.exists()) {
			mkdir( homeDir )
		}
                
		File sharedConfigDir = resolveFile( sharedConfigHome.getValue() )
		if (!sharedConfigDir.exists()) {
			mkdir( sharedConfigDir )
		}
		
		this.currentDir = resolveFile( "${home.getValue()}/current" )
		if (currentDir.exists()) {
			backupCurrent( homeDir )
			clearCurrent( homeDir )
		} 
		else {
			mkdir( currentDir )			
		}

		File source = new File( ".." )
		if (!source.exists()) return
		
		FileSet sources = newFileSet()
        sources.setDir( source )
		sources.setExcludes( "installer/installer-store.properties" )
		sources.setExcludes( "installer/systool.jar" )
		sources.setExcludes( "installer/installerenabled" )
		sources.setExcludes( "installer/bin/**/*" )
		
		Copy copy = (Copy) newTask( Tasks.COPY )
        copy.setForce( true )
        copy.setTodir( currentDir )
        copy.setOverwrite( true )
		copy.addFileset( sources )
		
        runTask( copy ) 
                
        def instanceProps = new Properties() 
        String instancePropertiesFileName = "$currentDir/instance/config/instance.properties".toString()
        def instancePropertiesFile = new File( instancePropertiesFileName )
        
        instancePropertiesFile.withInputStream { stream -> instanceProps.load( stream ) }
        instanceProps['shared.config.dir'] = "${sharedConfigHome.getValue()}".toString()
        instanceProps.store( new FileOutputStream( instancePropertiesFileName ), '' )          
		
		File sharedConfig = resolveFile( "${sharedConfigHome.getValue()}/banner_configuration.groovy" )
		if (!sharedConfig.exists()) {
		    def example = resolveFile( "${home.getValue()}/current/config/banner_configuration.example" )  
            new File( "${sharedConfigHome.getValue()}/banner_configuration.groovy" ).write( example?.text )
		}
		
		def releaseProps = getReleaseProperties()
		File instanceConfig = resolveFile( "${home.getValue()}/current/instance/${releaseProps['application.name']}_configuration.groovy" )
		if (!instanceConfig.exists()) {
		    def example = resolveFile( "${home.getValue()}/current/config/${releaseProps['application.name']}_configuration.example" )  
            new File( "${home.getValue()}/current/instance/config/${releaseProps['application.name']}_configuration.groovy" ).write( example?.text )
		}	    			    
    }


//---------------------------- private methods ---------------------------------


	private void backupCurrent( File homeDir ) {
		File currentDir = resolveFile( "${homeDir}/current" )
		File archiveDir = resolveFile( "${homeDir}/archive" )
		mkdir( archiveDir )
		
		Properties p = getReleaseProperties()
		String name = p.getProperty( "application.name" )
		String version = p.getProperty( "application.version" )
		SimpleDateFormat format = new SimpleDateFormat( "yyyy-MMM-dd-HH-mm-ss")
		String date = format.format( new Date() )
		String destFileName = "${name}-${version}-${date}.zip"
		
		Zip zip = (Zip) newTask( Tasks.ZIP )
		zip.setDestFile( resolveFile( "${archiveDir}/${destFileName}") )
		zip.setBasedir( this.currentDir )
		runTask( zip )
	}
	
	
	private void clearCurrent( File homeDir ) {
		File currentDir = resolveFile( "${homeDir}/current" )
		
		Delete deleteTask = (Delete) newTask( Tasks.DELETE )
		deleteTask.setIncludeEmptyDirs( true )
        FileSet fs = newFileSet();
		fs.setDefaultexcludes( false )
        fs.setDir( currentDir )
		fs.setExcludes( "instance/**/*" )
        deleteTask.addFileset( fs )
        runTask( deleteTask )
	}
	
	
	private Properties getReleaseProperties() {
		Properties p = new Properties()
		File f = resolveFile( "${currentDir}/i18n/release.properties" )
		p.load( new FileInputStream( f ) )
		p
	}

}