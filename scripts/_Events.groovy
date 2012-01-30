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

import groovy.xml.StreamingMarkupBuilder

import grails.util.Environment

import org.apache.ivy.core.report.ArtifactDownloadReport
import org.apache.ivy.core.event.EventManager
import org.apache.ivy.core.module.descriptor.Configuration
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor
import org.apache.ivy.core.module.descriptor.DependencyDescriptor
import org.apache.ivy.core.module.id.ModuleRevisionId
import org.apache.ivy.core.report.ResolveReport
import org.apache.ivy.core.resolve.IvyNode
import org.apache.ivy.core.resolve.ResolveEngine
import org.apache.ivy.core.resolve.ResolveOptions
import org.apache.ivy.core.settings.IvySettings
import org.apache.ivy.core.sort.SortEngine
import org.apache.ivy.plugins.resolver.ChainResolver
import org.apache.ivy.plugins.resolver.FileSystemResolver
import org.apache.ivy.plugins.resolver.IBiblioResolver
import org.apache.ivy.util.DefaultMessageLogger
import org.apache.ivy.util.Message

import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.codehaus.groovy.grails.resolve.IvyDependencyManager


includeTargets << grailsScript("_GrailsEvents")

eventPackagePluginEnd = { pluginName ->

    if (pluginName == "${metadata.'app.name'}") {
        try {
            event "TemplateZip", ["$pluginName", "${plugin.version}"]

            ant.zip( destfile:"${basedir}/grails-${pluginName}-${plugin.version}.zip", 
            update:true, basedir:"${projectWorkDir}", includes:"template.zip")

            def stagingDir   = "$projectWorkDir/staging"
            def templateZip  = "$projectWorkDir/template.zip"
            ant.delete( dir:stagingDir )
            ant.delete( file:templateZip )
        }
        catch (Throwable t) {
            ant.echo "Caught $t"
            t.printStackTrace()
            throw t
        }
    }
}


eventTemplateZip = { pluginName, pluginVersion ->

    try {
        IvyDependencyManager dm = new IvyDependencyManager( "$pluginName", "$pluginVersion" )

        dm.parseDependencies({
            repositories {
                mavenRepo "http://m038083.sungardhe.com:8081/nexus/content/repositories/releases/"
                mavenRepo "http://m038083.sungardhe.com:8081/nexus/content/repositories/snapshots/"
                mavenRepo "http://m038083.sungardhe.com:8081/nexus/content/repositories/thirdparty/"
                grailsPlugins()
                grailsHome()
                grailsCentral()
                mavenCentral()
                mavenRepo "http://repository.jboss.org/maven2/"
                mavenRepo "http://repository.codehaus.org"
            }

            dependencies {
                runtime( 'sungardhe:installerframework:1.0.1' ) {
                    export = false
                }
                runtime( 'sungardhe:SGHECommonUtil:1.0.1') {
                    export = false
                }
                runtime( 'org.springframework:spring-core:3.0.5.RELEASE') {
                    export = false
                }
                runtime( 'org.springframework:spring-expression:3.0.5.RELEASE') {
                    export = false
                }
                runtime( 'org.springframework:spring-context:3.0.5.RELEASE') {
                    export = false
                }
                runtime( 'org.springframework:spring-beans:3.0.5.RELEASE') {
                    export = false
                }
                runtime( 'org.springframework:spring-aop:3.0.5.RELEASE') {
                    export = false
                }
                runtime( 'org.springframework:spring-asm:3.0.5.RELEASE') {
                    export = false
                }
                runtime( 'org.codehaus.groovy:groovy:1.8.0') {
                    export = false
                }
                runtime( 'log4j:log4j:1.2.16') {
                    export = false
                }
                runtime( 'commons-logging:commons-logging:1.1.1') {
                    export = false
                }
                runtime( 'tomcat:catalina-ant:5.5.23') {
                    export = false
                }
            }
        })

        ResolveReport report = dm.resolveDependencies()
        if (dm.resolveErrors) {
            println "Error: There was an error resolving dependencies"
            exit 1
        }

        def templateZip 
        pluginSettings.getPluginInfos().each() {
            if (it.name.equals( "banner-packaging" )) {
                templateZip = new File( it.pluginDir.getFile(), "template.zip" )
            }
        }

        def stagingDir   = "$projectWorkDir/staging"
        def installerDir = "$stagingDir/installer"

        ant.delete( dir:"$stagingDir" )
        ant.mkdir(  dir:"$stagingDir" )

        def libDir = "$installerDir/lib"
        ant.mkdir( dir:"$libDir" )
        ant.copy( todir:"$libDir", overwrite:true, preservelastmodified:true ) {
            if (report) {
               report.allArtifactsReports.each() {
                   def file = it.localFile
                   fileset( dir:file.parentFile, includes:file.name )
               }
            }
        }

        def instanceDir = "$stagingDir/instance"
        ant.mkdir( dir:"$instanceDir/i18n" )
        ant.mkdir( dir:"$instanceDir/css" )
        ant.mkdir( dir:"$instanceDir/js" )
        ant.mkdir( dir:"$instanceDir/config" )

        File instanceProperties = new File( "$instanceDir/config/instance.properties" )
        instanceProperties << "shared.config.dir="

        // TODO: determine the path to the src/installer, versus assuming it's in-place
//       def installerSourceDir = "${basedir}/plugins/banner_packaging.git/src/installer"
        def installerSourceDir = "${basedir}/src/installer"
        ant.copy( todir:installerDir ) {
            fileset( dir:installerSourceDir, includes:"**/*" /* excludes:"apache-ant*" */ )
        }

        ant.unzip( src:"${installerSourceDir}/apache-ant-1.8.2-bin.zip", dest:installerDir ) {
            patternset {
                exclude( name:"apache-ant-1.8.2/docs/**/*" )
            }
        }

        ant.zip( destfile:templateZip ) {
            fileset( dir:stagingDir )
        }
    }
    catch (Throwable t) {
        ant.echo "Caught $t"
        t.printStackTrace()
        throw t
    }
}
