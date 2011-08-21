/** *****************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 ****************************************************************************** */

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


eventPackagePluginEnd = { pluginName ->
    
	if (pluginName == "${metadata.'app.name'}") {
		try {
			IvyDependencyManager dm = new IvyDependencyManager( "$pluginName", "${plugin.version}" )
			
			dm.parseDependencies( 
			{
				distribution = {
			         localRepository = ""
			         remoteRepository( id:"releases", url:"http://m038083.sungardhe.com:8081/nexus/content/repositories/releases" ) {
			             authentication  username:'admin', password:'admin123'
			         }
			    }

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
			    }
			} )
		
	        ResolveReport report = dm.resolveDependencies()
	        if (dm.resolveErrors) {
	            println "Error: There was an error resolving dependencies"
	            exit 1
	        }
					
			def templateZip  = "$projectWorkDir/template.zip"
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

			def installerSourceDir = "${basedir}/src/installer"
			ant.copy( todir:installerDir ) {
				fileset( dir:installerSourceDir, excludes:"apache-ant*" )
			}
		
			ant.unzip( src:"${basedir}/src/installer/apache-ant-1.8.2-bin.zip", dest:installerDir )
		
		    ant.zip( destfile:templateZip ) {
				fileset( dir:stagingDir )
			}
		
			ant.zip( destfile:"${basedir}/grails-${pluginName}-${plugin.version}.zip", 
			         update:true, basedir:"${projectWorkDir}", includes:"template.zip")
			         
			ant.delete( dir:stagingDir )
			ant.delete( file:templateZip )
		} 
		catch (Throwable t) {
		    ant.echo "Caught $t"
			t.printStackTrace()
			throw t;
		}
	}
		
}

