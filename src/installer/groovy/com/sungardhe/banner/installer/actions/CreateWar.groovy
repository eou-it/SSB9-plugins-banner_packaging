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

/**
 * Installer action for assembling a deployable ear from a template.
 **/
public class CreateWar extends DefaultAction {
    
    private File stagingWarDir
    

    public String getNameResourceCode() {
        "installer.action.CreateWar.name"
    }
    

    public void execute() throws ActionRunnerException {

        stagingWarDir = resolveFile( "staging.war" )
        deleteDir( stagingWarDir )

        mkdir( FileStructure.DIST_DIR )
        mkdir( stagingWarDir )

        updateWAR()

        deleteDir( stagingWarDir )
    }


//---------------------------- private methods ---------------------------------


	private File getTemplate() {
		File dir = resolveFile( FileStructure.WEBAPP_DIR )
		String[] names = dir.list()
		if (names.length != 1) {
			throw new RuntimeException( "$dir must contain a single war" )
		}
		return new File( dir, names[0] )
		
	}


    private void updateWAR() throws ActionRunnerException {	
		String sharedConfigDirName = getInstanceProperties().getProperty( "shared.config.dir" )
		if (sharedConfigDirName?.trim()?.size() == 0) {
			throw new RuntimeException( "Shared config dir not set" )
		}
		File sharedConfigDir = resolveFile( sharedConfigDirName )
		if (!sharedConfigDir.exists()) {
			throw new RuntimeException( "Shared config dir: ${sharedConfigDirName} does not exist" )
		}
	
		File templateWar = getTemplate()
	
		def warName = templateWar.getName()
		def warFile = resolveFile( "${FileStructure.DIST_DIR}/${warName}" )
		updateProgress( new CreatingEarMessage( warFile.getName() ) )
        deleteFile( warFile, true );

        Expand unwar = (Expand) newTask( Tasks.UNWAR )
        unwar.setSrc( templateWar )
        unwar.setDest( stagingWarDir )
        runTask( unwar )

		updateI18N( stagingWarDir )
		updateCSS( stagingWarDir )
		updateJS( stagingWarDir )
		updateStaging( stagingWarDir, "WEB-INF/classes", sharedConfigDir.getAbsolutePath() )
		updateStaging( stagingWarDir, "WEB-INF/classes", FileStructure.INSTANCE_CONFIG_DIR )
				
        War war = (War) newTask( Tasks.WAR );
        war.setDestFile( warFile );
        war.setUpdate( true );
        war.setWebxml( resolveFile( stagingWarDir.getAbsolutePath() + "/WEB-INF/web.xml" ) );
        war.setManifest( resolveFile( stagingWarDir.getAbsolutePath() + "/META-INF/MANIFEST.MF" ) );
        FileSet fs = newFileSet();
        fs.setDir( stagingWarDir );
        fs.createExclude().setName( "**WEB-INF/web.xml" );
        war.addFileset( fs );
        runTask( war );
    }


	private void updateI18N( File stagingDir ) {
		updateStaging( stagingDir, "WEB-INF/grails-app/i18n", FileStructure.I18N_DIR )
		updateStaging( stagingDir, "WEB-INF/grails-app/i18n", FileStructure.INSTANCE_I18N_DIR )
	}

	
	private void updateCSS( File stagingDir ) {
		updateStaging( stagingDir, "css", FileStructure.INSTANCE_CSS_DIR )
	}

	
	private void updateJS( File stagingDir )  {
		updateStaging( stagingDir, "js", FileStructure.INSTANCE_JS_DIR )
	}

	
	/**
	 * Copies (recursively) a source directory (relative to the installer home) to
	 * the target dir (relative to the stagingDir)
	 **/
	private void updateStaging( File stagingDir, String target, String sourceDir ) {
		File toDir = resolveFile( stagingDir.getAbsolutePath() + "/" + target )
		mkdir( toDir )

		File source = new File( sourceDir )
		if (!source.exists()) return
		
		FileSet sources = newFileSet();
        sources.setDir( source );
		
		Copy copy = (Copy) newTask( Tasks.COPY );
        copy.setForce( true );
        copy.setTodir( toDir );
        copy.setOverwrite( true );
		copy.addFileset( sources )
        runTask( copy );
		
	}


    private class CreatingEarMessage extends ProgressMessage {
        private static final String RESOURCE_CODE = "installer.message.creatingWar";

        CreatingEarMessage( String name ) {
            super( RESOURCE_CODE, [ name ] );
        }
    }
	
}