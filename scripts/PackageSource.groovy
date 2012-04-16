/* *******************************************************************************
 Copyright 2009-2012 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/

import org.codehaus.groovy.grails.plugins.GrailsPluginUtils


/**
 * Gant script for creating a release package for a banner module.
 * The release package contains an 'installer/systool' that is used 
 * to manage product homes and to re-generate the war file with 
 * environment specific configuration and overrides (localization message bundles, CSS, JavaScript).
 **/
scriptEnv = "production"
includeTargets << grailsScript( "_GrailsPackage" )
includeTargets << grailsScript( "_GrailsEvents" )

target( default: "Packages the source code into a zip file under target directory" ) {

    File sourcePackageZip = new File( "${basedir}/target/src-${metadata.'app.name'}-${metadata.'app.version'}.zip" )
    ant.delete( file:sourcePackageZip )

    def sourceStagingDir = new File( "${projectWorkDir}/source-staging" )
    ant.delete( dir:sourceStagingDir )
    ant.mkdir( dir:sourceStagingDir )

    includeSource( sourceStagingDir )

    ant.zip( destfile: sourcePackageZip ) {
        fileset( dir:sourceStagingDir, excludes:"*.lock" )
    }
}


/**
 * Includes source code within the staging directory.
 * The 'package-release' target delegates to this method to include 
 * source code. The source code for the application and plugins are
 * packaged up as bare git repositories cloned from internal 
 * development repositories (albeit with individual developer commits
 * squashed).
 **/
private void includeSource( sourceStagingDir ) {

    cloneRepository( sourceStagingDir, "${metadata.'app.name'}", 
                     "git config --get remote.origin.url".execute().text )

    // Next, we'll clone repositories for all in-house plugins
    def inlinePluginDirs = pluginSettings.inlinePluginDirectories*.file
    inlinePluginDirs.each {
        cloneRepository( sourceStagingDir, "${it.name}", 
                         "git config --get remote.origin.url".execute( null, new File( it.path ) ).text )
    }

    println "Staging of source: "
    process = "ls -laF".execute(null, new File( "${sourceStagingDir}" ) )
    process.in.eachLine { line -> println line }

    // Lastly, we'll update the project so that in-place plugins (git submodules) 
    // reference the cloned repositories as their 'origin'.

}


private void cloneRepository( sourceStagingDir, repoName, originUrl ) {

    // We'll clone the repository as a 'bare' repository, since we do NOT
    // want any possibility that a client might attempt to use this repository
    // as a 'working copy' clone.
    //
    process = "git clone --bare ${originUrl}".execute( null, new File( "${sourceStagingDir}" ) )
    process.waitFor()
}


