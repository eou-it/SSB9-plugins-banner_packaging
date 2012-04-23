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

import org.apache.ivy.core.report.ArtifactDownloadReport
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.codehaus.groovy.grails.resolve.IvyDependencyManager


/**
 * Gant script for creating a release package for a banner module.
 * The release package contains an 'installer/systool' that is used 
 * to manage product homes and to re-generate the war file with 
 * environment specific configuration and overrides (localization message bundles, CSS, JavaScript).
 **/
scriptEnv = "production"
includeTargets << grailsScript( "_GrailsPackage" )
includeTargets << grailsScript( "_GrailsWar" )
includeTargets << grailsScript( "_GrailsEvents" )


target( default:"Package Release" ) {

    depends( checkVersion, compile, createConfig, genReleaseProperties, war )
    pluginName = "banner-packaging"
    event "PackageReleaseStart", []

    try {
        verifyCleanWorkingCopy( basedir )
    }
    catch (e) {
        println "*******************************************"
        println "<<< WARNING: Git Working Copy NOT Clean >>>"
        println "            (proceeding anyway)"
        println "*******************************************"
    }

    // We'll fire a 'TemplateZip' event, and respond to it via the _Events.groovy script
    // before continuing. The 'eventTemplateZip' handler will use Ivy to retrieve all of 
    // the dependencies that need to be included in the release package
    //
    event "TemplateZip", [pluginName, '1.0.2'] // TODO: Read plugin version from it's *Plugin file...

    File releasePackageZip = new File( "${basedir}/target/release-${metadata.'app.name'}-${metadata.'app.version'}.zip" )
    ant.delete( file:releasePackageZip )

    def stagingDir = new File( "${projectWorkDir}/installer-staging" )
    ant.delete( dir:stagingDir )
    ant.mkdir( dir:stagingDir )

    File templateZip = getTemplateHomeZip()
    ant.unzip( src:templateZip, dest:stagingDir ) {
        patternset {
            exclude( name:"installer/apache-ant-1.8.2/docs/**/*" )
        }
    }

    File customDir = new File( "${basedir}/src/installer" )	
    if (customDir.exists()) {
        ant.copy( todir:"${stagingDir}/installer", overwrite:true ) {
            fileset( dir:"${customDir}", includes:"**/*" )
        }
    }

    ant.mkdir( dir:"${stagingDir}/i18n" )
    ant.copy( todir:"${stagingDir}/i18n" ) {
        fileset( dir:"${basedir}/grails-app/i18n", includes:"**/*" )
        // we'll also copy the release.properties to the i18n directory so that 
        // it is easily accessible by the installer...
        fileset( dir: "$basedir/target/classes", includes: "release.properties" )
    }

    ant.mkdir( dir:"${stagingDir}/webapp" )
    ant.copy( todir:"${stagingDir}/webapp" ) {
        fileset( dir:"${basedir}/target", includes:"*.war" )
    }

    ant.mkdir( dir:"${stagingDir}/config" )
    ant.copy( todir:"${stagingDir}/config" ) {
        fileset( dir:"${basedir}/", includes:"*_configuration.example" )
    }

    ant.mkdir( dir:"${stagingDir}/lib" )
    ant.copy( todir:"${stagingDir}/lib" ) {
        fileset( dir:"${basedir}/target", includes:"ojdbc6.jar" )
        fileset( dir:"${basedir}/src", includes:"logging.properties" )
    }

    ant.zip( destfile:releasePackageZip ) {
        fileset( dir:stagingDir, excludes:"*.lock" )
    }

    ant.delete( dir:stagingDir )
    event( "PackageReleaseEnd", [] )
}


target( genReleaseProperties: "Creates a release.properties file holding a newly assigned build number and the application version." ) {    

    // This target uses a 'build number' web service to retrieve the next build number 
    // for the project.  Each project is assigned a UUID (manually), and this UUID is 
    // supplied to the web service to identify which 'build number sequence' to increment 
    // and return. The project's UUID, and the URL to the service, are 
    // found in the project's configuration. 
    //
    def appName    = "${metadata.'app.name'}"
    def appVersion = "${metadata.'app.version'}"

    // def config = new ConfigSlurper().parse( new File( "${basedir}/grails-app/conf/Config.groovy" ).toURL() )
    def uuid = config.build.number.uuid
    def url  = config.build.number.base.url + uuid
    def extractedBuildNumber = "Unassigned" // used when the configuration does not specify a uuid 

    if (uuid instanceof String && url instanceof String) {
        try {
            def buildNumberProperty  = url.toURL().getText()
            extractedBuildNumber = (buildNumberProperty =~ /[\d]+/).collect { it }[0]
        } catch (e) {
            def msg = "   **** WARNING: Build number could not be attained ****    "
            extractedBuildNumber = msg
            println "$msg"
            // we'll bury the excepton and let the packaging proceed...
        }
    }

    def scmRevision   = "git rev-parse HEAD".execute().text
    def scmRepository = "git config --get remote.origin.url".execute().text

    def content = """|#This file is automatically generated and contains release specific properties.
                     |#This file MUST not be changed.
                     |#
                     |#  ****** DO NOT EDIT OR TRANSLATE THIS FILE. *******
                     |#
                     |#******************************************************
                     |#*             Version and Build Number               *
                     |#**************************************************** */
                     |
                     |application.name=$appName
                     |application.version=$appVersion
                     |application.build.number=$extractedBuildNumber
                     |application.build.time='${new Date()}'
                     |
                     |
                     |#******************************************************
                     |#*               Source Code Revision                 *
                     |#**************************************************** */
                     |
                     |# The following source code repository and revision
                     |# values refer to the original development repository.
                     |#
                     |# Clients who decide to use git to manage their source
                     |# code modifications may use the bare repository clone
                     |# included within the release package.
                     |git.repository=$scmRepository
                     |git.commit=$scmRevision
                     |
                     |
                     |#******************************************************
                     |#*                 Plugin Dependencies                *
                     |#**************************************************** */
                     |
                     |""".stripMargin()

    def pluginVersionInfo = retrievePluginInfo()
    pluginVersionInfo.each {
        content += "\n# Plugin: ${it.key}"
        content += "\n#     version:   ${it.value.version}"
        content += "\n#     location:  ${it.value.location}"

        if (it.value.treeish) content += "\n#     Git treeish: ${it.value.treeish}"
        if (it.value.branch)  content += "\n#     Git branch:  ${it.value.branch}"
        if (it.value.status)  content += "\n#     Git status:  ${it.value.status}"
        else                  content += "\n"
    }
    def releasePropertiesFile = new File( "$basedir/target/classes/release.properties" )
    releasePropertiesFile.write content
}


// ------------------------------- Private Methods -----------------------------


private File getTemplateHomeZip() {

    //The template home dir is a zipped distro within the plugin directory.  We have to find the plugin dir.
    File zip = null
    pluginSettings.getPluginInfos().each() {
        if (it.name.equals( "banner-packaging" )) {
            zip = new File( it.pluginDir.getFile(), "template.zip" )
        }
    }

    if (zip == null) {
        throw new Exception( "Template home zip could not be found" )
    }
    zip
}


/**
 * Records plugin versions, and for in-place plugins the Git SHA1.
 **/
private Map retrievePluginInfo() {

    def inlinePluginVersionInfo = [:]
    def gitSubmodulesFile = new File( "${projectWorkDir}/.gitmodules" )

    GrailsPluginUtils.getPluginInfos().each {
        def inlinePluginDirPaths = pluginSettings.inlinePluginDirectories*.file.path
        inlinePluginDirPaths.each { println "INLINE: ${it}" }

        def pluginDirPath = it.pluginDir.path
        def sha1   = ''
        def branch = ''
        def status = ''
        if (pluginDirPath in inlinePluginDirPaths) {
            sha1   = new File("${it.pluginDir.path}/.git/refs/heads/master").text
            branch = getWorkingBranch( pluginDirPath )
            status = getStatus( pluginDirPath )
        }
        def versionInfo = [ version:  "${it.version}",
                            location: "${it.pluginDir.path}",
                            treeish:  "${sha1}",
                            branch:   "${branch}",
                            status:   "${status}" ]
        inlinePluginVersionInfo[it.name] = versionInfo
    }
//    inlinePluginVersionInfo.each { println "${it.key} : ${it.value}"}
    inlinePluginVersionInfo
}


private String getWorkingBranch( dir ) {

    def currentBranch = ''
    matcher = ~/\* (.*)\s/

    process = "git branch".execute( null, new File( dir ) )
    process.in.eachLine { line ->
        m = line =~ /\*\s+(.*)\s?/
        if (m) {
            currentBranch = m[0][1]
            return
        }
    }
    currentBranch
}


private verifyCleanWorkingCopy(dir) {

    if (getStatus(dir).value.status != 'clean')
        throw new RuntimeException( 'Git working copy is not clean' )
}


private String getStatus( dir ) {

    def status = ''
    process = "git status".execute( null, new File( dir ) )
    process.in.eachLine { line ->
        if (line.contains( 'nothing to commit' )) {
            status = 'clean'
            return
        }
        else {
            status = 'pending changes'
            return
        }
    }
    status
}


