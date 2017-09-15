/* *******************************************************************************
Ellucian 2017 copyright
 **********************************************************************************/
package net.hedtech.banner.installer.actions

import com.sungardhe.commoncomponents.installer.ActionRunnerException
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import net.hedtech.banner.installer.FileStructure

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Installer action for saml setup
 **/

public class SamlSetup extends BaseSystoolAction {
    private static File sharedConfigDir
    private static final String ALIAS = 'alias'
    private static final String SPASSERTION_LOCATION = 'SPAssertionLocation'
    private static final String SPLOGOUT_LOCATION = 'SPLogoutLocation'
    private static final String IDP_LOCATION = 'IDPLocation'
    private static final String SP_CERTIFICATE_PATH = 'spCertificatePath'
    private static final String IDP_CERTIFICATE_PATH = 'idpCertificatePath'
    private static final String SP_XML_PATH = 'spXmlPath'
    private static final String IDP_XML_PATH = 'idpXmlPath'
    private static final String GUROCFG_VALUE = 'GUROCFG_VALUE'
    private static final String GUROCFG_NAME = 'GUROCFG_NAME'


    public String getNameResourceCode() {
        "installer.saml.setup.name"
    }


    public void execute() throws ActionRunnerException {
        def alias, SPAssertionLocation, SPLogoutLocation, IDPLocation, spCertificatePath, idpCertificatePath, spXmlPath, idpXmlPath
        sharedConfigDir = getSharedConfiguration()
        Properties config = new Properties()
        File propertiesFile = new File("${sharedConfigDir.getAbsolutePath()}/saml_configuration.properties")
        propertiesFile.withInputStream {
            config.load(it)
        }
        def appId = config?.getProperty("appId")
        def appName = config?.getProperty("appName")
        def dbConnection = config?.getProperty("dbconnectionURL")
        def username = config?.getProperty("dbusername")
        def pass = config?.getProperty("dbpassword")
        Class.forName("oracle.jdbc.OracleDriver");
        Connection con = DriverManager.getConnection("$dbConnection", "$username", "$pass");
        PreparedStatement stmt =con.prepareStatement("SELECT * from GUROCFG WHERE  GUROCFG_GUBAPPL_APP_ID = ?");
        stmt.setString(1, "$appId");
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            switch (rs.getString(GUROCFG_NAME)) {
                case ALIAS:
                    alias = rs.getString(GUROCFG_VALUE)
                    break
                case SPASSERTION_LOCATION:
                    SPAssertionLocation = rs.getString(GUROCFG_VALUE)
                    break
                case SPLOGOUT_LOCATION:
                    SPLogoutLocation = rs.getString(GUROCFG_VALUE)
                    break
                case IDP_LOCATION:
                    IDPLocation = rs.getString(GUROCFG_VALUE)
                    break
                case SP_CERTIFICATE_PATH:
                    spCertificatePath = rs.getString(GUROCFG_VALUE)
                    break
                case IDP_CERTIFICATE_PATH:
                    idpCertificatePath = rs.getString(GUROCFG_VALUE)
                    break
                case SP_XML_PATH:
                    spXmlPath = rs.getString(GUROCFG_VALUE)
                    break
                case IDP_XML_PATH:
                    idpXmlPath = rs.getString(GUROCFG_VALUE)
                    break
            }
        }
        con.close();

        println "**************************************"
        println "ALIAS" + alias
        println "**************************************"
        println "SPAssertionLocation" + SPAssertionLocation
        println "**************************************"
        println "SPLogoutLocation" + SPLogoutLocation
        println "**************************************"
        println "IDPLocation" + IDPLocation
        println "**************************************"
        println "SP_CERTIFICATE_PATH" + spCertificatePath
        println "**************************************"
        println "IDP_CERTIFICATE_PATH" + idpCertificatePath
        println "**************************************"
        println "SP_XML_PATH" + spXmlPath
        println "**************************************"
        println "IDP_XML_PATH" + idpXmlPath

        /*"""keytool -genkey -noprompt -alias $alias -dname "CN=$CN, OU=$OU, O=$O, L=$L, S=$S, C=$C" -keystore $keystoreName -storepass $password1 -keypass $password1""".execute()*/
        /*"keytool -export -alias $alias -storepass $password1 -file SERVICE-PROVIDER.cer -keystore $keystoreName"*/
        /*"keytool -export -alias $alias -storepass $password1 -file $currentDir//SERVICE-PROVIDER.cer -keystore $keystoreName".execute()*/
        def spCertCommand = "keytool -printcert -rfc -file $spCertificatePath"
        Process spCertquantCmd = spCertCommand.execute()
        println "***************************************************"
        def spCertOutput = spCertquantCmd.in.text
        spCertOutput = spCertOutput.replace("-----BEGIN CERTIFICATE-----", "")
        spCertOutput = spCertOutput.replace("-----END CERTIFICATE-----", "")
        spCertOutput = spCertOutput.trim()
        println spCertOutput
        updateSPXML(spXmlPath, alias, SPLogoutLocation, SPAssertionLocation, spCertOutput, appName)
        def idpCertCommand = "keytool -printcert -rfc -file $idpCertificatePath"
        Process idpCertquantCmd = idpCertCommand.execute()
        println "***************************************************>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
        def idpCertOutput = idpCertquantCmd.in.text
        idpCertOutput = idpCertOutput.replace("-----BEGIN CERTIFICATE-----", "")
        idpCertOutput = idpCertOutput.replace("-----END CERTIFICATE-----", "")
        idpCertOutput = idpCertOutput.trim()
        println idpCertOutput
        updateIDPXML(idpXmlPath, IDPLocation, idpCertOutput, appName)
        applicationConfigChanges(appName, alias)
        def ant = new AntBuilder()
        ant.copy(file: "$idpCertificatePath", todir: "${FileStructure?.INSTANCE_CONFIG_DIR}")
        ant.copy(file: "$spCertificatePath", todir: "${FileStructure?.INSTANCE_CONFIG_DIR}")
        ant.copy(file: "${sharedConfigDir.getAbsolutePath()}/banner-$appName-sp.xml", todir: "${FileStructure?.INSTANCE_CONFIG_DIR}")
        ant.copy(file: "${sharedConfigDir.getAbsolutePath()}/banner-$appName-idp.xml", todir: "${FileStructure?.INSTANCE_CONFIG_DIR}")
    }

    /**
     * updateSPXML method is to read the service-provider xml from the path mentioned in SS config table and
     * update the corresponding values based on values provided in SS config table
     * and writing the XML to new file (banner-appname-sp.xml) in sharedConfiguration location
     * @param path
     * @param alias
     * @param SPLogoutLocation
     * @param SPAssertionLocation
     * @param spCertOutput
     * @param appName
     * @return
     */

    private updateSPXML(path, alias, SPLogoutLocation, SPAssertionLocation, spCertOutput, appName) {
        File inputFile = new File("$path")
        def spXML = new XmlSlurper().parse(inputFile)
        spXML?.SPSSODescriptor?.KeyDescriptor?.KeyInfo?.X509Data?.X509Certificate?.replaceBody "$spCertOutput"
        spXML['@ID'] = "$alias"
        spXML['@entityID'] = "$alias"
        spXML?.SPSSODescriptor?.SingleLogoutService['@Location'] = "$SPLogoutLocation"
        spXML?.SPSSODescriptor?.AssertionConsumerService['@Location'] = "$SPAssertionLocation"

        def newwriter = new FileWriter("${sharedConfigDir.getAbsolutePath()}/banner-$appName-sp.xml")
        def result = new StreamingMarkupBuilder().bind { mkp.yield spXML }.toString()
        new XmlSlurper().parseText(result)
        XmlUtil.serialize(result, newwriter)
    }

    /**
     * updateIDPXML method is to read the identity-provider xml from the path mentioned in SS config table and
     * update the corresponding values based on values provided in SS config table
     * and writing the XML to new file (banner-appname-idp.xml) in sharedConfiguration location
     * @param path
     * @param IDPLocation
     * @param idpCertOutput
     * @param appName
     * @return
     */
    private updateIDPXML(path, IDPLocation, idpCertOutput, appName) {
        File inputFile = new File("$path")
        def idpXML = new XmlSlurper().parse(inputFile)
        idpXML?.IDPSSODescriptor?.KeyDescriptor?.KeyInfo?.X509Data?.X509Certificate?.replaceBody "$idpCertOutput"
        idpXML['@entityID'] = "$IDPLocation"
        idpXML?.IDPSSODescriptor?.SingleLogoutService['@Location'] = "$IDPLocation"
        idpXML?.IDPSSODescriptor?.SingleSignOnService['@Location'] = "$IDPLocation"

        def newwriter = new FileWriter("${sharedConfigDir.getAbsolutePath()}/banner-$appName-idp.xml")
        def result = new StreamingMarkupBuilder().bind { mkp.yield idpXML }.toString()
        new XmlSlurper().parseText(result)
        XmlUtil.serialize(result, newwriter)
    }

    private applicationConfigChanges(appName, alias) {
        def content = """|
                        |grails.plugin.springsecurity.saml.active = true
                        |grails.plugin.springsecurity.auth.loginFormUrl = '/saml/login'
                        |grails.plugin.springsecurity.saml.afterLogoutUrl ='/logout/customLogout'
                        |banner.sso.authentication.saml.localLogout='false'                 // To disable single logout set this to true,default 'false'.
                        |grails.plugin.springsecurity.saml.keyManager.storeFile = 'classpath:security/$appName'  // for unix file based Example:- 'file:/home/u02/samlkeystore.jks'
                        |grails.plugin.springsecurity.saml.keyManager.storePass = '<PASSWORD>'
                        |grails.plugin.springsecurity.saml.keyManager.passwords = [ '$alias': '<PASSWORD>' ]
                        |grails.plugin.springsecurity.saml.keyManager.defaultKey = '$alias'
                        |grails.plugin.springsecurity.saml.metadata.sp.file = 'classpath:security/banner-$appName-sp.xml'    // for unix file based Example:-'/home/u02/sp-local.xml'
                        |grails.plugin.springsecurity.saml.metadata.providers = [adfs: 'classpath:security/banner-$appName-idp.xml'] // for unix file based Ex:- '/home/u02/idp-local.xml'
                        |grails.plugin.springsecurity.saml.metadata.defaultIdp = 'adfs'
                        |grails.plugin.springsecurity.saml.metadata.sp.defaults = [
                        |       local: true,
                        |       alias: '$alias',
                        |       securityProfile: 'metaiop',
                        |       signingKey: '$alias',
                        |       encryptionKey: '$alias',
                        |       tlsKey: '$alias',
                        |       requireArtifactResolveSigned: false,
                        |       requireLogoutRequestSigned: false,
                        |       requireLogoutResponseSigned: false
                        |]
                        |""".stripMargin()
        def appConfig = new File("${FileStructure?.INSTANCE_CONFIG_DIR}/app_saml_config.txt")
        appConfig.write content
    }
}