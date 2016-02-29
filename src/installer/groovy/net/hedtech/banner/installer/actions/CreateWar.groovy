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
package net.hedtech.banner.installer.actions

import net.hedtech.banner.installer.*
import com.sungardhe.commoncomponents.installer.*

import groovy.xml.StreamingMarkupBuilder

import org.apache.tools.ant.taskdefs.*
import org.apache.tools.ant.types.*


/**
 * Installer action for assembling a deployable war file from a template.
 **/
public class CreateWar extends BaseSystoolAction {

    private File stagingWarDir    // set by a call to 'setDirectories'
    private File sharedConfigDir  // set by a call to 'setDirectories'


    public String getNameResourceCode() {
        "installer.action.CreateWar.name"
    }


    public void execute() throws ActionRunnerException {

        setDirectories()
        updateWAR()
        deleteDir( stagingWarDir )
    }


//---------------------------- private methods ---------------------------------


    private void setDirectories() {
        stagingWarDir = resolveFile( "staging.war" )
        deleteDir( stagingWarDir )

        mkdir( FileStructure.DIST_DIR )
        mkdir( stagingWarDir )

		sharedConfigDir = getSharedConfiguration()
    }


	private Properties getReleaseProperties() {
		getProperties( "${FileStructure.I18N_DIR}/release.properties" )
	}


	private File getTemplate() {
		File dir = resolveFile( FileStructure.WEBAPP_DIR )
		String[] names = dir.list()
		if (names.length != 1) {
			throw new RuntimeException( "$dir must contain a single war" )
		}
		new File( dir, names[0] )		
	}


    private void updateWAR() throws ActionRunnerException {	
	
		File templateWar = getTemplate()
	
		def warName = templateWar.getName()
		def warFile = resolveFile( "${FileStructure.DIST_DIR}/${warName}" )
        deleteFile( warFile, true )

        Expand unwar = (Expand) newTask( Tasks.UNWAR )
        unwar.setSrc( templateWar )
        unwar.setDest( stagingWarDir )
        runTask( unwar )

		updateI18N( stagingWarDir )
		updateCSS( stagingWarDir )
		updateJS( stagingWarDir )
		updateStaging( stagingWarDir, "WEB-INF/classes", sharedConfigDir.getAbsolutePath() )
		updateStaging( stagingWarDir, "WEB-INF/classes", FileStructure.INSTANCE_CONFIG_DIR )
        updateSecurityXmlIfSamlEnabled()
		// copyReleaseProperties() // we'll copy release.properties to WEB-INF/classes 		
		updateWebXml()
				
        War war = (War) newTask( Tasks.WAR )
        war.setDestFile( warFile )
        war.setUpdate( true )
        war.setWebxml( resolveFile( stagingWarDir.getAbsolutePath() + "/WEB-INF/web.xml" ) )
        war.setManifest( resolveFile( stagingWarDir.getAbsolutePath() + "/META-INF/MANIFEST.MF" ) )

        FileSet fs = newFileSet()
        fs.setDir( stagingWarDir )
        fs.createExclude().setName( "**WEB-INF/web.xml" )
        war.addFileset( fs )
        runTask( war )
    }

    private void updateSecurityXmlIfSamlEnabled(){
        def appName = getReleaseProperties().getProperty( "application.name" )
        def instanceConfig = new ConfigSlurper().parse( resolveFile( "${FileStructure.INSTANCE_CONFIG_DIR}/${appName}_configuration.groovy" ).toURL() )
        if(samlIsEnabled(instanceConfig)){
            updateSamlConfigurationFiles( stagingWarDir, "WEB-INF/classes/security", FileStructure.INSTANCE_CONFIG_DIR )
        }
    }


    private void updateWebXml() {

        def config  = new ConfigSlurper().parse( resolveFile( "${sharedConfigDir.getAbsolutePath()}/banner_configuration.groovy" ).toURL() )
        def appName = getReleaseProperties().getProperty( "application.name" )
        def instanceConfig = new ConfigSlurper().parse( resolveFile( "${FileStructure.INSTANCE_CONFIG_DIR}/${appName}_configuration.groovy" ).toURL() )        

        File webXml = resolveFile( stagingWarDir.getAbsolutePath() + "/WEB-INF/web.xml" )
        def root = new XmlParser().parseText( webXml.getText() )

    	updateWebXmlDataSourceRef( config, root )

    	if (!casIsEnabled( instanceConfig )) {
            removeCasContentIfPresent( root )
        } else {
        	updateWebXmlCasConfiguration( instanceConfig, root )
        }

        def stringWriter = new StringWriter() 
        new XmlNodePrinter( new PrintWriter( stringWriter ) ).print( root ) 
//println stringWriter.toString()
        webXml.text = stringWriter.toString()
    }


    private void updateWebXmlDataSourceRef( config, root ) {

        updateJndi( config.bannerDataSource.jndiName,
                    'config.bannerDataSource.jndiName',
                    root, 
                    'BannerDS Datasource' )

        updateJndi( config.bannerSsbDataSource.jndiName, 
                    'config.bannerSsbDataSource.jndiName', 
                    root, 
                    'Banner Self Service Datasource', 
                    false )
    }


    private void updateJndi( jndiName, configProp, root, refName, required = true ) {

        if (required && jndiName instanceof Map) throw new RuntimeException( "Please configure '$configProp, and re-run this action." )

        if ("jdbc/bannerDataSource" != jndiName) {
            def resourceRef = root.'resource-ref'.find { it.'description'.toString().contains( refName ) } 
            def resourceRefName = resourceRef ? resourceRef.children().find { it.toString().contains( 'res-ref-name' ) } : null

            if (required && !resourceRefName) throw new RuntimeException( "Expected to find '$refName JNDI reference within the web.xml!" )                      
            if (resourceRefName && "$jndiName" != "${resourceRefName.name()}") {
                def oldValue = resourceRefName.value
                resourceRefName.value = jndiName
                updateProgress( new UpdateDataSourceCompleteMessage( "${resourceRef.description}", jndiName, "$oldValue" ) )     
            }
        }
    }


    private boolean casIsEnabled( instanceConfig ) {
        'cas' == instanceConfig.banner.sso.authenticationProvider      
    }

    private boolean samlIsEnabled( instanceConfig ) {
        'saml' == instanceConfig.banner.sso.authenticationProvider
    }

    private boolean casIsConfiguredInWebXml( root ) {
        root.filter.'filter-name'.any { it.'filter-name'.toString().contains( 'CAS' ) }
    }

    private boolean casSaml11ProtocolEnabled( instanceConfig ) {
        'SAMLart' == instanceConfig.grails.plugin.springsecurity.cas.artifactParameter
    }
    private void updateWebXmlCasConfiguration( instanceConfig, root ) {

        if (casIsConfiguredInWebXml( root )) { 
            // We just need to update the validation filter content...
            def validationFilter = root.filter.find { it.'filter-name'.toString() == 'CAS Validation Filter' }
            validationFilter?.replaceNode( getCasValidationFilter() )
        }
        else {
            if(!checkExistingCasContentIfPresent( root )){
				def ant = new AntBuilder()
				ant.echo "Inserting CAS filter and filter-mapping elements into web.xml ..."
				insertCasFilters( instanceConfig, root )
				insertCasFilterMappings( root )
			} 
        }
    }


    private void removeCasContentIfPresent( root ) {

        if (casIsConfiguredInWebXml( root )) {
            ant.echo "Removing CAS filter and filter-mapping elements from the web.xml..."
            def allChildren = root.children()
            def casContent = allChildren.find { it.'filter-name'.toString().contains( 'CAS' ) }
            casContent?.each { it.replaceNode {} }
        }
    }
	
	private boolean checkExistingCasContentIfPresent( root ) {

			def ant = new AntBuilder()
            ant.echo "Check CAS filter and filter-mapping elements from the web.xml..."
			root.filter.any { it.'filter-name'.toString().contains('CAS Validation Filter') }
    }


    private void insertCasFilters( instanceConfig, root ) {

        def insertionIndex
        def allChildren = root.children()
        allChildren.eachWithIndex { elem, index ->
            if (elem.'filter-name'.toString().contains( 'springSecurityFilterChain' )) {
                insertionIndex = insertionIndex ?: index + 1
            }
        }
        def casFilters
        if(casSaml11ProtocolEnabled( instanceConfig )) {
            casFilters = getCasSaml11ProtocolFilters( instanceConfig )
        } else {
            casFilters = getCasFilters( instanceConfig )
        }
        casFilters.each {
            allChildren.add( insertionIndex++, it )
        }
    }


    private void insertCasFilterMappings( root ) {

        def insertionIndex = 0
        def grailsWebRequestFilterIndex = 0
        def allChildren = root.children()
        allChildren.eachWithIndex { elem, index ->
            if (insertionIndex == 0) {
                // we want to add the CAS filter-mapping elements as the first filter-mapping elements. 
                // Unfortunately, the CAS plugin puts duplicate entries into the web.xml, placed with the 'filter' elements. 
                // We know our last 'filter' is named 'grailsWebRequest', so we'll find the index for this filter.
                if (grailsWebRequestFilterIndex == 0 && elem.'filter-name'.toString().contains( 'grailsWebRequest' )) {
                    grailsWebRequestFilterIndex = index
                }

                if (elem.name().toString().contains( 'filter-mapping' )) {
                    if (elem.'filter-name'.toString().contains( 'CAS' ) && index < grailsWebRequestFilterIndex) {
                        // We haven't reached the last filter, but encountered a CAS filter-mapping... 
                        // We'll remove this (as we'll be adding the CAS filter-mapping elements in the correct location later...)
//                        println "Going to remove CAS filter-mapping at index $index:  ${elem.'filter-name'.toString()}"
                        elem.replaceNode {} 
                    }

                    if (grailsWebRequestFilterIndex > 0 && grailsWebRequestFilterIndex < index) {
                        insertionIndex = index - 1
                    }
                }
            }
        }
        // we've got the index of the last filter-mapping, so we'll insert after that...
        insertionIndex++
        def casFilterMappings = getCasFilterMappings()
        casFilterMappings.each {
            allChildren.add( insertionIndex++, it )
        }
    }


    private def getCasFilters( instanceConfig ) {

        def casValidationFilters = getCasValidationFilterString( instanceConfig )
        def filters = """
            |<snippet-root>
            |<filter>
    	    |	<filter-name>CAS Single Sign Out Filter</filter-name>
           |	<filter-class>org.jasig.cas.client.session.SingleSignOutFilter</filter-class>
    	    |</filter>
    	    |$casValidationFilters
    	    |<filter>
    	    |	<filter-name>CAS HttpServletRequest Wrapper Filter</filter-name>
    	    |	<filter-class>org.jasig.cas.client.util.HttpServletRequestWrapperFilter</filter-class>
    	    |</filter>
            |</snippet-root>
    	    |""".stripMargin()
    	def root = new XmlParser().parseText( filters )
    	root.children()
    }

    private def getCasSaml11ProtocolFilters( instanceConfig ) {
        def casValidationFilters = getCasValidationFilterString( instanceConfig )
        def filters = """
            |<snippet-root>
            |<filter>
            |    <filter-name>CAS Single Sign Out Filter</filter-name>
            |    <filter-class>org.jasig.cas.client.session.SingleSignOutFilter</filter-class>
            |    <init-param>
            |        <param-name>artifactParameterName</param-name>
            |        <param-value>SAMLart</param-value>
            |    </init-param>
            |</filter>
            |$casValidationFilters
            |<filter>
            |    <filter-name>CAS HttpServletRequest Wrapper Filter</filter-name>
            |    <filter-class>org.jasig.cas.client.util.HttpServletRequestWrapperFilter</filter-class>
            |</filter>
            |</snippet-root>
            |""".stripMargin()
        def root = new XmlParser().parseText( filters )
        root.children()
    }

    private def getCasValidationFilter() {
        def root = new XmlParser().parseText( "<snippet-root>${getCasValidationFilterString()}</snippet-root>" )
        root.filter
    }


    private String getCasValidationFilterString( instanceConfig ) {

        def validationFilter = """
	        |<filter>
	        |	<filter-name>CAS Validation Filter</filter-name>
	        |	<filter-class>net.hedtech.jasig.cas.client.BannerSaml11ValidationFilter</filter-class>
	        |	<init-param>
	        |		<param-name>casServerUrlPrefix</param-name>
	        |		<param-value>${instanceConfig.grails.plugin.springsecurity.cas.serverUrlPrefix}</param-value>
	        |	</init-param>
	        |	<init-param>
	        |		<param-name>serverName</param-name>
	        |		<param-value>${instanceConfig.grails.plugin.springsecurity.cas.serverName}</param-value>
	        |	</init-param>
	        |	<init-param>
	        |		<param-name>redirectAfterValidation</param-name>
	        |		<param-value>true</param-value>
	        |	</init-param>
	        |   <init-param>
            |       <param-name>artifactParameterName</param-name>
            |       <param-value>${instanceConfig.grails.plugin.springsecurity.cas.artifactParameter}</param-value>
            |   </init-param>
            |   <init-param>
            |       <param-name>tolerance</param-name>
            |       <param-value />
            |   </init-param>
	        |</filter>
            |""".stripMargin()
    }


    private def getCasFilterMappings() {

        def filterMappings = """
            |<snippet-root>
            |<filter-mapping>
    	    |	<filter-name>CAS Single Sign Out Filter</filter-name>
    	    |	<url-pattern>/*</url-pattern>
    	    |</filter-mapping>
    	    |<filter-mapping>
    	    |	<filter-name>CAS Validation Filter</filter-name>
    	    |	<url-pattern>/*</url-pattern>
    	    |</filter-mapping>
    	    |<filter-mapping>
    	    |	<filter-name>CAS HttpServletRequest Wrapper Filter</filter-name>
    	    |	<url-pattern>/*</url-pattern>
    	    |</filter-mapping>
            |</snippet-root>
            |""".stripMargin()
        def root = new XmlParser().parseText( filterMappings )
        root.children()
    }


    // used only for debugging...
    private String renderFormattedXml( String xml ) {

      def stringWriter = new StringWriter()
      def node = new XmlParser().parseText( xml )
      new XmlNodePrinter( new PrintWriter( stringWriter ) ).print( node )
      stringWriter.toString()
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
	
	
	private void copyReleaseProperties() {
		
	    def ant = new AntBuilder()
    	ant.copy( todir: "${stagingWarDir.getAbsolutePath()}/WEB-INF/classes" ) {
    		fileset( dir: "${FileStructure.I18N_DIR}", includes: "release.properties" )
    	}
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

        String[] fileExtensionArray= ["*.xml","*.jks"]
        FileSet sources = newFileSet()
        sources.setDir( source )
        sources.appendExcludes(fileExtensionArray)

		Copy copy = (Copy) newTask( Tasks.COPY )
        copy.setForce( true )
        copy.setTodir( toDir )
        copy.setOverwrite( true )
		copy.addFileset( sources )
        runTask( copy )
	}

    public void updateSamlConfigurationFiles(File stagingDir, String target, String sourceDir ){
        File toDir = resolveFile( stagingDir.getAbsolutePath() + "/" + target )
        mkdir( toDir )

        File source = new File( sourceDir )
        if (!source.exists()) return

        String[] fileExtensionArray= ["*.xml","*.jks"]
        FileSet sources = newFileSet()
        sources.setDir( source )
        sources.appendIncludes(fileExtensionArray)

        Copy copy = (Copy) newTask( Tasks.COPY )
        copy.setForce( true )
        copy.setTodir( toDir )
        copy.setOverwrite( true )
        copy.addFileset( sources )
        runTask( copy )
    }


	private class UpdateDataSourceCompleteMessage extends ProgressMessage {

	    private static final String RESOURCE_CODE = "installer.message.update_datasource_complete"
	
	    UpdateDataSourceCompleteMessage( description, jndiName, resourceRefName ) {
	        super( RESOURCE_CODE, [ "$description", "$jndiName", "$resourceRefName" ] )
	    }
	}
}
