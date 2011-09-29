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

includeTargets << grailsScript( "_GrailsPackage" )

thisPluginDir = pluginSettings.getPluginInfos().findResult { (it.name.equals( "banner-packaging" )) ? it.pluginDir : null }
includeTargets << new File( "${thisPluginDir.path}/scripts/PackageRelease.groovy" )


/**
 * Assigns a build number and records release information within release.properties file
 * within target/classes. 
 **/
target( default: "Assigns a build number and creates a release.properties file" ) {  
    
    // Note: This target is really implemented within PackageRelease.groovy, in the
    // genReleaseProperties target.  If the implementation is contained herein with 
    // the packageRelease target depending on this target, the remaining 
    // code within packageRelease is not executed. Consequently, this 'odd' 
    // way to re-use a target is used to circumvent this issue.  
    //   
    depends( checkVersion, compile, createConfig, genReleaseProperties )
}

