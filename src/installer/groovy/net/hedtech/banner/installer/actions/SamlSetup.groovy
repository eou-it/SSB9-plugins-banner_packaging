/* *******************************************************************************
Ellucian 2017 copyright
 **********************************************************************************/
package net.hedtech.banner.installer.actions

import com.sungardhe.commoncomponents.installer.ActionRunnerException
import com.sungardhe.commoncomponents.installer.StringResource
import groovy.io.FileType
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import net.hedtech.banner.installer.FileStructure
import org.springframework.beans.factory.annotation.Required

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Installer action for saml setup
 **/

public class SamlSetup extends BaseSystoolAction {
    private static File sharedConfigDir
    private StringResource dbUserName
    private StringResource dbPassword
    private static final String SERVICE_PROVIDER_ENTITY_ID= 'serviceProviderEntityID'
    private static final String SERVICE_PROVIDER_ASSERTION_CONSUMER_SERVICE = 'serviceProviderAssertionConsumerService'
    private static final String SERVICE_PROVIDER_SINGLE_LOGOUT_SERVICE = 'serviceProviderSingleLogoutService'
    private static final String IDENTITY_PROVIDER_ENTITY_ID = 'identityProviderEntityID'
    private static final String SERVICE_PROVIDER_CERTIFICATE_PATH = 'serviceProviderCertificatePath'
    private static final String IDENTITY_PROVIDER_CERTIFICATE_PATH = 'identityProviderCertificatePath'
    private static final String SERVICE_PROVIDER_XML_PATH= 'serviceProviderXmlPath'
    private static final String IDENTITY_PROVIDER_XML_PATH = 'identityProviderXmlPath'
    private static final String GUROCFG_VALUE = 'GUROCFG_VALUE'
    private static final String GUROCFG_NAME = 'GUROCFG_NAME'


    public String getNameResourceCode() {
        "installer.saml.setup.name"
    }


    @Required // 'username'
    public void setDbUserName( StringResource dbUserName ) {
        this.dbUserName = dbUserName
        addRequiredResource( this.dbUserName )
    }


    @Required //  'password'
    public void setDbPassword( StringResource dbPassword ) {
        this.dbPassword = dbPassword
        addRequiredResource( this.dbPassword )
    }


    public void execute() throws ActionRunnerException {
        def serviceProviderEntityID, serviceProviderAssertionConsumerService, serviceProviderSingleLogoutService, identityProviderEntityID, serviceProviderCertificatePath, identityProviderCertificatePath, serviceProviderXmlPath, identityProviderXmlPath
        sharedConfigDir = getSharedConfiguration()
        Properties config = new Properties()
        File propertiesFile = new File("${sharedConfigDir.getAbsolutePath()}/saml_configuration.properties")
        propertiesFile.withInputStream {
            config.load(it)
        }
        def appId = config?.getProperty("appId")
        def appName = config?.getProperty("appName")
        def dbConnection = config?.getProperty("dbconnectionURL")
        sharedConfigDir.eachFileRecurse(FileType.FILES) {
            if (it.name =~ /\.xml/) {
                if (it.name.toLowerCase().endsWith("saml_idp.xml")) {
                    identityProviderXmlPath = it
                }

            }
        }
        (new File(FileStructure?.INSTANCE_CONFIG_DIR)).eachFileRecurse(FileType.FILES) {
            if (it.name =~ /\.xml/) {
                if (it.name.toLowerCase().endsWith("saml_sp.xml")) {
                    serviceProviderXmlPath = it
                }
            }
        }
        Class.forName("oracle.jdbc.OracleDriver");
        Connection con = DriverManager.getConnection("$dbConnection", dbUserName.getValue(), dbPassword.getValue());
        try {
            PreparedStatement stmt = con.prepareStatement("SELECT * from GUROCFG WHERE  GUROCFG_GUBAPPL_APP_ID = ?");
            stmt.setString(1, "$appId");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                switch (rs.getString(GUROCFG_NAME)) {
                    case SERVICE_PROVIDER_ENTITY_ID:
                        serviceProviderEntityID = rs.getString(GUROCFG_VALUE)
                        break
                    case SERVICE_PROVIDER_ASSERTION_CONSUMER_SERVICE:
                        serviceProviderAssertionConsumerService = rs.getString(GUROCFG_VALUE)
                        break
                    case SERVICE_PROVIDER_SINGLE_LOGOUT_SERVICE:
                        serviceProviderSingleLogoutService = rs.getString(GUROCFG_VALUE)
                        break
                    case IDENTITY_PROVIDER_ENTITY_ID:
                        identityProviderEntityID = rs.getString(GUROCFG_VALUE)
                        break
                    case SERVICE_PROVIDER_CERTIFICATE_PATH:
                        serviceProviderCertificatePath = rs.getString(GUROCFG_VALUE)
                        break
                    case IDENTITY_PROVIDER_CERTIFICATE_PATH:
                        identityProviderCertificatePath = rs.getString(GUROCFG_VALUE)
                        break
                }
            }
        }catch (SQLException ex) {
            exit(-1)
        }
        catch (Exception ex) {
            exit(-1)
        } finally {
            con.close();
        }
        println "\n----------- Successfully extracted values from DB ----------------"
        /*"""keytool -genkey -noprompt -alias $alias -dname "CN=$CN, OU=$OU, O=$O, L=$L, S=$S, C=$C" -keystore $keystoreName -storepass $password1 -keypass $password1""".execute()*/
        /*"keytool -export -alias $alias -storepass $password1 -file SERVICE-PROVIDER.cer -keystore $keystoreName"*/
        /*"keytool -export -alias $alias -storepass $password1 -file $currentDir//SERVICE-PROVIDER.cer -keystore $keystoreName".execute()*/
        def spCertCommand = "keytool -printcert -rfc -file $serviceProviderCertificatePath"
        Process spCertquantCmd = spCertCommand.execute()
        def spCertOutput = spCertquantCmd.in.text
        spCertOutput = spCertOutput.replace("-----BEGIN CERTIFICATE-----", "")
        spCertOutput = spCertOutput.replace("-----END CERTIFICATE-----", "")
        spCertOutput = spCertOutput.trim()
        println "----------- Service Provider certificate extracted Successfully------------------"
        updateSPXML(serviceProviderXmlPath, serviceProviderEntityID, serviceProviderSingleLogoutService, serviceProviderAssertionConsumerService, spCertOutput, appName)
        def idpCertCommand = "keytool -printcert -rfc -file $identityProviderCertificatePath"
        Process idpCertquantCmd = idpCertCommand.execute()
        def idpCertOutput = idpCertquantCmd.in.text
        idpCertOutput = idpCertOutput.replace("-----BEGIN CERTIFICATE-----", "")
        idpCertOutput = idpCertOutput.replace("-----END CERTIFICATE-----", "")
        idpCertOutput = idpCertOutput.trim()
        println "----------- Identity Provider certificate extracted Successfully----------------"
        updateIDPXML(identityProviderXmlPath, identityProviderEntityID, idpCertOutput, appName)
        applicationConfigChanges(appName, serviceProviderEntityID)
        def ant = new AntBuilder()
        ant.copy(file: "$identityProviderCertificatePath", todir: "${FileStructure?.INSTANCE_CONFIG_DIR}")
        ant.copy(file: "$serviceProviderCertificatePath", todir: "${FileStructure?.INSTANCE_CONFIG_DIR}")
        println "----------- Successfully created configurations ----------------"
    }

    /**
     * updateSPXML method is to read the service-provider xml from the path mentioned in SS config table and
     * update the corresponding values based on values provided in SS config table
     * and writing the XML to new file (appName-SamlMeta_SP) in sharedConfiguration location
     * @param path
     * @param serviceProviderEntityID
     * @param serviceProviderSingleLogoutService
     * @param serviceProviderAssertionConsumerService
     * @param spCertOutput
     * @param appName
     * @return
     */

    private updateSPXML(path, serviceProviderEntityID, serviceProviderSingleLogoutService, serviceProviderAssertionConsumerService, spCertOutput, appName) {
        File inputFile = new File("$path")
        def spXML = new XmlSlurper().parse(inputFile)
        spXML?.SPSSODescriptor?.KeyDescriptor?.KeyInfo?.X509Data?.X509Certificate?.replaceBody "$spCertOutput"
        spXML['@ID'] = "$serviceProviderEntityID"
        spXML['@entityID'] = "$serviceProviderEntityID"
        spXML?.SPSSODescriptor?.SingleLogoutService['@Location'] = "$serviceProviderSingleLogoutService"
        spXML?.SPSSODescriptor?.AssertionConsumerService['@Location'] = "$serviceProviderAssertionConsumerService"

        def newwriter = new FileWriter("${FileStructure?.INSTANCE_CONFIG_DIR}/$appName-saml_sp.xml")
        def result = new StreamingMarkupBuilder().bind { mkp.yield spXML }.toString()
        new XmlSlurper().parseText(result)
        XmlUtil.serialize(result, newwriter)
    }

    /**
     * updateIDPXML method is to read the identity-provider xml from the path mentioned in SS config table and
     * update the corresponding values based on values provided in SS config table
     * and writing the XML to new file ($appName-SamlMeta_IDP.xml) in sharedConfiguration location
     * @param path
     * @param identityProviderEntityID
     * @param idpCertOutput
     * @param appName
     * @return
     */
    private updateIDPXML(path, identityProviderEntityID, idpCertOutput, appName) {
        File inputFile = new File("$path")
        def idpXML = new XmlSlurper().parse(inputFile)
        idpXML?.IDPSSODescriptor?.KeyDescriptor?.KeyInfo?.X509Data?.X509Certificate?.replaceBody "$idpCertOutput"
        idpXML['@entityID'] = "$identityProviderEntityID"
        idpXML?.IDPSSODescriptor?.SingleLogoutService['@Location'] = "$identityProviderEntityID"
        idpXML?.IDPSSODescriptor?.SingleSignOnService['@Location'] = "$identityProviderEntityID"

        def newwriter = new FileWriter("${FileStructure?.INSTANCE_CONFIG_DIR}/$appName-saml_idp.xml")
        def result = new StreamingMarkupBuilder().bind { mkp.yield idpXML }.toString()
        new XmlSlurper().parseText(result)
        XmlUtil.serialize(result, newwriter)
    }

    private applicationConfigChanges(appName, serviceProviderEntityID) {
        def content = """|
                        |grails.plugin.springsecurity.saml.active = true
                        |grails.plugin.springsecurity.auth.loginFormUrl = '/saml/login'
                        |grails.plugin.springsecurity.saml.afterLogoutUrl ='/logout/customLogout'
                        |banner.sso.authentication.saml.localLogout='false'                 // To disable single logout set this to true,default 'false'.
                        |grails.plugin.springsecurity.saml.keyManager.storeFile = 'classpath:security/$appName'  // for unix file based Example:- 'file:/home/u02/samlkeystore.jks'
                        |grails.plugin.springsecurity.saml.keyManager.storePass = '<PASSWORD>'
                        |grails.plugin.springsecurity.saml.keyManager.passwords = [ '$serviceProviderEntityID': '<PASSWORD>' ]
                        |grails.plugin.springsecurity.saml.keyManager.defaultKey = '$serviceProviderEntityID'
                        |grails.plugin.springsecurity.saml.metadata.sp.file = 'classpath:security/$appName-saml_sp.xml'    // for unix file based Example:-'/home/u02/sp-local.xml'
                        |grails.plugin.springsecurity.saml.metadata.providers = [adfs: 'classpath:security/$appName-saml_idp.xml'] // for unix file based Ex:- '/home/u02/idp-local.xml'
                        |grails.plugin.springsecurity.saml.metadata.defaultIdp = 'adfs'
                        |grails.plugin.springsecurity.saml.metadata.sp.defaults = [
                        |       local: true,
                        |       alias: '$serviceProviderEntityID',
                        |       securityProfile: 'metaiop',
                        |       signingKey: '$serviceProviderEntityID',
                        |       encryptionKey: '$serviceProviderEntityID',
                        |       tlsKey: '$serviceProviderEntityID',
                        |       requireArtifactResolveSigned: false,
                        |       requireLogoutRequestSigned: false,
                        |       requireLogoutResponseSigned: false
                        |]
                        |""".stripMargin()
        def appConfig = new File("${FileStructure?.INSTANCE_CONFIG_DIR}/app_saml_config.txt")
        appConfig.write content
    }
}