/* *******************************************************************************
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
package net.hedtech.banner.installer.actions

import org.springframework.beans.factory.annotation.Required
import com.sungardhe.commoncomponents.installer.*
import org.apache.tools.ant.taskdefs.*
import org.apache.tools.ant.types.*
import net.hedtech.banner.installer.*
import java.text.*

/**
 * Installer action for updating a product home.
 **/
public class InstallHome extends DefaultAction {

    private StringResource home
    private StringResource sharedConfigHome
    private File           currentDir


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

        println "\n"
        println "${getBanner()}"
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

        FileSet sources = newFileSet()
        sources.with {
            setDir( source )
            setExcludes( "installer/installer-store.properties" )
            setExcludes( "installer/systool.jar" )
            setExcludes( "installer/installerenabled" )
            setExcludes( "installer/bin/**/*" )
        }

        Copy copy = (Copy) newTask( Tasks.COPY )
        copy.with {
            setForce( true )
            setTodir( currentDir )
            setOverwrite( true )
            addFileset( sources )
        }

        runTask( copy )

        def instanceProps = new Properties() 
        String instancePropertiesFileName = "$currentDir/instance/config/instance.properties".toString()
        def instancePropertiesFile = new File( instancePropertiesFileName )

        println "\n"
        File sharedConfigDir = resolveFile( sharedConfigHome.getValue() )
        if (!sharedConfigDir.exists()) {
            mkdir( sharedConfigDir )
        }

        instancePropertiesFile.withInputStream { stream -> instanceProps.load( stream ) }
        instanceProps['shared.config.dir'] = "${sharedConfigHome.getValue()}".toString()
        instanceProps.store( new FileOutputStream( instancePropertiesFileName ), '' )

        File sharedConfig = resolveFile( "${sharedConfigHome.getValue()}/banner_configuration.groovy" )
        if (!sharedConfig.exists()) {
            def example = resolveFile( "${home.getValue()}/current/config/banner_configuration.example" )
            new File( "${sharedConfigHome.getValue()}/banner_configuration.groovy" ).write( example?.text )
        }

        def appName = getReleaseProperties()['application.name']
        File instanceConfig = resolveFile( "${home.getValue()}/current/instance/config/${appName}_configuration.groovy" )
        if (!instanceConfig.exists()) {
            def example = resolveFile( "${home.getValue()}/current/config/${appName}_configuration.example" )
            new File( "${home.getValue()}/current/instance/config/${appName}_configuration.groovy" ).write( example?.text )
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
        getProperties( "${currentDir}/i18n/release.properties" )
    }

}

