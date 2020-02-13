/* *****************************************************************************
 Copyright 2011-2017 Ellucian Company L.P. and its affiliates.
*******************************************************************************/

import groovy.xml.StreamingMarkupBuilder

import grails.util.BuildSettings
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
import org.codehaus.groovy.grails.io.support.UrlResource
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.codehaus.groovy.grails.resolve.IvyDependencyManager


includeTargets << grailsScript("_GrailsEvents")

eventCleanEnd = {
    ant.delete(file:"./target/template.zip")
}


extraSrcDirs = ["${basedir}/src/installer/groovy","${basedir}/src/installer/i18n","${basedir}/src/installer/spring"]

eventCompileStart = {
    for (String path in extraSrcDirs) {
        projectCompiler.srcDirectories << path
        println "extraSrcDirs = "+path
    }
    copyResources buildSettings.resourcesDir

}

eventCreateWarStart = { warName, stagingDir ->
    copyResources "$stagingDir/WEB-INF/classes"
}

private copyResources(destination) {
    ant.copy(todir: destination,
            failonerror: false,
            preservelastmodified: true) {
        for (String path in extraSrcDirs) {
            fileset(dir: path) {
                exclude(name: '*.groovy')
                exclude(name: '*.java')
            }
        }
    }
}

eventPackagePluginEnd = { pluginName ->

    if (pluginName == "${metadata.'app.name'}") {
        try {
            event "TemplateZip", ["$pluginName", "${plugin.version}"]

            ant.zip( destfile:"${basedir}/grails-${pluginName}-${plugin.version}.zip",
            update:true, basedir:"${projectWorkDir}", includes:"template.zip")

            def stagingDir   = "$projectWorkDir/staging"
            def templateZip  = "$projectWorkDir/target/template.zip"
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

        def pluginInfo  = pluginSettings.getPluginInfos().find({it.name.equals("banner-packaging")})
        def pluginDir   = pluginInfo.pluginDir.getFile()
        def templateZip = new File( "$pluginDir/target", "template.zip" )
        def stagingDir   = "$projectWorkDir/staging"
        def installerDir = "$stagingDir/installer"

    if (templateZip.exists()) {
        ant.echo "...found existing template.zip "
        return
    }

    try {
        IvyDependencyManager dm = new IvyDependencyManager( "$pluginName", "$pluginVersion", new BuildSettings() )
        dm.parseDependencies({
            repositories {
                grailsPlugins()
                grailsHome()
                grailsCentral()
                //mavenCentral()
                /*Comment starts
                With initiative to formally decommission the use of HTTP on January 15th, 2020 by maven
                where url http://repo1.maven.org/maven2/ need to be changed to new https URL for packaging
                the jar dependencies
c               Commnent ends*/
                mavenRepo "https://repo1.maven.org/maven2/"
                mavenRepo "https://repository.jboss.org/maven2/"
                mavenRepo "https://repository.codehaus.org"
            }

            log "error"
            dependencies {
                runtime( 'org.springframework:spring-core:3.1.4.RELEASE') {
                    export = false
                }
                runtime( 'org.springframework:spring-expression:3.1.4.RELEASE') {
                    export = false
                }
                runtime( 'org.springframework:spring-context:3.1.4.RELEASE') {
                    export = false
                }
                runtime( 'org.springframework:spring-beans:3.1.4.RELEASE') {
                    export = false
                }
                runtime( 'org.springframework:spring-aop:3.1.4.RELEASE') {
                    export = false
                }
                runtime( 'org.springframework:spring-asm:3.1.4.RELEASE') {
                    export = false
                }
                runtime( 'org.codehaus.groovy:groovy-all:2.0.7') {
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
            ant.echo "Error: There was an error resolving dependencies"
            exit 1
        }

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
        ant.echo "Going to copy installer from ${pluginDir}/lib"
        // Now we'll add internal dependencies from our project into the lib dir
        ant.copy( todir: "$libDir", overwrite:true, preservelastmodified:true ) {
            fileset( dir: "${pluginDir}/lib", includes:"*.jar" )
        }

        def instanceDir = "$stagingDir/instance"
        ant.mkdir( dir:"$instanceDir/i18n" )
        ant.mkdir( dir:"$instanceDir/css" )
        ant.mkdir( dir:"$instanceDir/js" )
        ant.mkdir( dir:"$instanceDir/images" )
        ant.mkdir( dir:"$instanceDir/config" )

        File instanceProperties = new File( "$instanceDir/config/instance.properties" )
        instanceProperties << "shared.config.dir="

        def installerSourceDir = "${pluginDir}/src/installer"
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
