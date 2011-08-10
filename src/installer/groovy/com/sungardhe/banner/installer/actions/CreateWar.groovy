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
		String globalConfigDirName = getInstanceProperties().getProperty( "global.config.dir" )
		if (globalConfigDirName?.trim().size() == 0) {
			throw new RuntimeException( "Global config dir not set" )
		}
		File globalConfigDir = resolveFile( globalConfigDirName )
		if (!globalConfigDir.exists()) {
			throw new RuntimeException( "Global config dir: ${globalConfigDirName} does not exist" )
		}
	
		File templateWar = getTemplate()
	
		def warName = templateWar.getName()
		def warFile = resolveFile( "${FileStructure.DIST_DIR}/${warName}" )
		updateProgress( new CreatingEarMessage( warFile.getName() ) );
        deleteFile( warFile, true );

        Expand unwar = (Expand) newTask( Tasks.UNWAR )
        unwar.setSrc( templateWar )
        unwar.setDest( stagingWarDir )
        runTask( unwar )

		updateI18N( stagingWarDir )
		updateCSS( stagingWarDir )
		updateJS( stagingWarDir )
		updateStaging( stagingWarDir, "WEB-INF/classes", globalConfigDir.getAbsolutePath() )
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

        /*Copy copy = (Copy) newTask( Tasks.COPY );
        copy.setForce( true );
        copy.setFile( resolveFile( FileStructure.CONFIG_DATABASE_PROPERTIES ) );
        copy.setTodir( resolveFile( stagingWarDir.getAbsolutePath() + "/WEB-INF/classes" ) );
        copy.setOverwrite( true );
        runTask( copy );

        //generate the webapp location properties
        GenerateWebAppLocationProperties genAppLoc = new GenerateWebAppLocationProperties();
        readyTask( genAppLoc );
        genAppLoc.setConfigDir( resolveFile( FileStructure.CONFIG_DIR ) );
        genAppLoc.setOutputDir( resolveFile( stagingWarDir.getAbsolutePath() + "/WEB-INF/config" ) );
        runTask( genAppLoc );

        copy = (Copy) newTask( Tasks.COPY );
        copy.setForce( true );
        copy.setFile( resolveFile( FileStructure.SECURITY_JAR ) );
        copy.setTodir( resolveFile( stagingWarDir.getAbsolutePath() + "/WEB-INF/lib" ) );
        copy.setOverwrite( true );
        runTask( copy );

        FileSet locales = newFileSet();
        locales.setDir( resolveFile( FileStructure.LOCALE_DIR ) );
        copy = (Copy) newTask( Tasks.COPY );
        copy.setForce( true );
        copy.setTodir( resolveFile( stagingWarDir.getAbsolutePath() + "/WEB-INF/classes/resources/locale" ) );
        copy.addFileset( locales );
        copy.setOverwrite( true );
        runTask( copy );

        FileSet extLocales = newFileSet();
        extLocales.setDir( resolveFile( FileStructure.EXT_LOCALE_DIR ) );
        copy = (Copy) newTask( Tasks.COPY );
        copy.setForce( true );
        copy.setTodir( resolveFile( stagingWarDir.getAbsolutePath() + "/WEB-INF/classes/resources/locale" ) );
        copy.addFileset( extLocales );
        copy.setOverwrite( true );
        runTask( copy );

        //update WEB-INF
        copy = (Copy) newTask( Tasks.COPY );
        copy.setForce( true );
        copy.setTodir( resolveFile( stagingWarDir.getAbsolutePath() + "/WEB-INF" ) );
        FileSet webInf = newFileSet();
        webInf.setDir( resolveFile( FileStructure.RESOURCES_DIR + "/WEB-INF" ) );
        webInf.setIncludes( "*" );
        copy.addFileset( webInf );
        runTask( copy );

        //update spring configs
        deleteDir( resolveFile( stagingWarDir.getAbsolutePath() + "/WEB-INF/spring" ) );
        copy = (Copy) newTask( Tasks.COPY );
        copy.setForce( true );
        copy.setTodir( resolveFile( stagingWarDir.getAbsolutePath() + "/WEB-INF/spring" ) );
        FileSet springConfigs = newFileSet();
        springConfigs.setDir( resolveFile( FileStructure.RESOURCES_DIR + "/spring" ) );
        springConfigs.createInclude().setName( "*" );
        springConfigs.createInclude().setName( "services/**" );
        springConfigs.createInclude().setName( "remoting/**" );
        springConfigs.createInclude().setName( "uimediators/**" );
        springConfigs.createInclude().setName( "importers/**" );
        copy.addFileset( springConfigs );
        runTask( copy );

        //process templates
        XmlTemplateProcessorTask processorTask = new XmlTemplateProcessorTask();
        readyTask( processorTask );
        processorTask.setConfigDir( resolveFile( FileStructure.CONFIG_DIR ) );
        processorTask.setTargetDir( resolveFile( stagingWarDir.getAbsolutePath() + "/WEB-INF" ) );
        runTask( processorTask );

        GenerateCASProperties cas = new GenerateCASProperties();
        readyTask( cas );
        cas.setOutputDir( resolveFile( stagingWarDir.getAbsolutePath() + "/WEB-INF/classes" ) );
        cas.setConfigDir( resolveFile( FileStructure.CONFIG_DIR ) );
        runTask( cas );

        installHelp();

        updateSWF();

        //If installing to weblogic, we need to remove JAXB classes and use those built in to weblogic
        if (config.getAdvanced().getApplicationServer().getType().equals( com.sungardhe.relate.config.ApplicationServer.Type.Enum.forString( "weblogic" ))) {
            Delete deleteTask = (Delete) newTask( Tasks.DELETE );
            FileSet fs = newFileSet();
            fs.setDir( resolveFile( stagingWarDir.getAbsoluteFile() + "/WEB-INF/lib" ) );
            fs.createInclude().setName( "jaxb-api-2.0.jar" );
            fs.createInclude().setName( "jaxb-impl-2.0.1.jar" );
            fs.createInclude().setName( "jaxb-xjc-2.0.1.jar" );
            fs.createInclude().setName( "jaxrpc.jar" );
            fs.createInclude().setName( "jsr173_1.0_api.jar" );
            fs.createInclude().setName( "jsr173_api.jar" );
            fs.createInclude().setName( "stax-api-1.0.1.jar" );
            fs.createInclude().setName( "xmlbeans-qname.jar" );
            deleteTask.addFileset( fs );
            runTask( deleteTask );
        }

        File warPatchesDir = resolveFile( FileStructure.WAR_PATCH_CLASS_DIR );
        if (warPatchesDir.exists()) {
            FileSet warPatches = newFileSet();
            warPatches.setDir( warPatchesDir );
            copy = (Copy) newTask( Tasks.COPY );
            copy.setForce( true );
            copy.setTodir( stagingWarDir );
            copy.addFileset( warPatches );
            copy.setOverwrite( true );
            runTask( copy );
        }*/

        /*
        SimpleDateFormat format = new SimpleDateFormat( "MM-dd-yyyy hh:mm aa" );
        String timeStamp = format.format( new Date() );
        UpdatePropertyFileTask update = new UpdatePropertyFileTask();
        readyTask( update );
        update.setPropertyFile( resolveFile( _stagingWAR.getAbsolutePath() + "/WEB-INF/classes/deployment.properties" ) );
        update.setName( "deployment.message" );
        update.setValue( "Build Date: " + timeStamp );
        runTask( update );*/

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