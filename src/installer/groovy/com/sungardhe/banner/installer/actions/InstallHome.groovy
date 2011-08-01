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
	private File currentDir

    public String getNameResourceCode() {
        "installer.action.InstallHome.name"
    }

    @Required
    public void setHome(StringResource home) {
        this.home = home
        addRequiredResource(this.home)
    }

    public void execute() throws ActionRunnerException {
		File homeDir = resolveFile( home.getValue() )
		if (!homeDir.exists()) {
			mkdir( homeDir )
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
		
		FileSet sources = newFileSet();
        sources.setDir( source );
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