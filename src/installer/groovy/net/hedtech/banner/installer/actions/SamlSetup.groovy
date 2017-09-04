/* *******************************************************************************
Ellucian 2017 copyright
 **********************************************************************************/
package net.hedtech.banner.installer.actions

import com.sungardhe.commoncomponents.installer.ActionRunnerException
import net.hedtech.banner.installer.FileStructure
import java.sql.*

/**
 * Installer action for saml setup
 **/

public class SamlSetup extends BaseSystoolAction {
    private File sharedConfigDir
    private static final String ALIAS = 'alias'
    private static final String SPASSERTION_LOCATION = 'SPAssertionLocation'
    private static final String SPLOGOUT_LOCATION = 'SPLogoutLocation'
    private static final String IDP_LOCATION = 'IDPLocation'

    public String getNameResourceCode() {
        "installer.saml.setup.name"
    }


    public void execute() throws ActionRunnerException {
        def alias,SPAssertionLocation,SPLogoutLocation,IDPLocation
        sharedConfigDir = getSharedConfiguration()
        Properties config = new Properties()
        File propertiesFile = new File("${sharedConfigDir.getAbsolutePath()}/saml_configuration.properties")
        propertiesFile.withInputStream {
            config.load(it)
        }
        def appId= config?.getProperty("appId")
        def spCertificatePath=config?.getProperty("spCertificatePath")
        def idpCertificatePath=config?.getProperty("idpCertificatePath")
        def spXmlPath=config?.getProperty("spXmlPath")
        def idpXmlPath=config?.getProperty("idpXmlPath")
        def dbConnection = config?.getProperty("dbconnectionURL")
        def username = config?.getProperty("dbusername")
        def pass = config?.getProperty("dbpassword")
        def appName = config?.getProperty("appName")

        Class.forName("oracle.jdbc.OracleDriver");
        Connection con = DriverManager.getConnection("$dbConnection", "$username", "$pass");
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("select * from GUROCFG where GUROCFG_GUBAPPL_APP_ID='$appId'");
        while (rs.next()) {
            switch (rs.getString("GUROCFG_NAME")) {
                case ALIAS:
                    alias = rs.getString("GUROCFG_VALUE")
                    break
                case SPASSERTION_LOCATION:
                    SPAssertionLocation = rs.getString("GUROCFG_VALUE")
                    break
                case SPLOGOUT_LOCATION:
                    SPLogoutLocation = rs.getString("GUROCFG_VALUE")
                    break
                case IDP_LOCATION:
                    IDPLocation = rs.getString("GUROCFG_VALUE")
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
            generateSPXML(alias, SPLogoutLocation, SPAssertionLocation)
            certificateInsertion(spCertOutput)

            def idpCertCommand = "keytool -printcert -rfc -file $idpCertificatePath"
            Process idpCertquantCmd = idpCertCommand.execute()
            println "***************************************************"
            def idpCertOutput = idpCertquantCmd.in.text
            idpCertOutput = idpCertOutput.replace("-----BEGIN CERTIFICATE-----", "")
            idpCertOutput = idpCertOutput.replace("-----END CERTIFICATE-----", "")
            idpCertOutput = idpCertOutput.trim()
            println idpCertOutput
            generateIDPXML(IDPLocation)
            applicationConfigChanges(appName,alias)
           // readingSPXML(spXmlPath, alias, SPLogoutLocation, SPAssertionLocation)
            def ant = new AntBuilder()
            ant.copy( file:"$idpCertificatePath", todir: "${FileStructure?.INSTANCE_CONFIG_DIR}")
            ant.copy( file:"$spCertificatePath", todir: "${FileStructure?.INSTANCE_CONFIG_DIR}")
        }

        //----------- Private Method ------------------------

        private generateSPXML(alias, location, SPAssertionLocation) {

            def content = """|<?xml version="1.0" encoding="UTF-8"?>
                     |  <md:EntityDescriptor xmlns:md="urn:oasis:names:tc:SAML:2.0:metadata" ID="$alias" entityID="$alias">
                     |    <md:SPSSODescriptor AuthnRequestsSigned="false" WantAssertionsSigned="false" protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol">
                     |      <md:KeyDescriptor use="signing">
                     |        <ds:KeyInfo xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
                     |          <ds:X509Data>
                     |            <ds:X509Certificate>
                     |                  Enter the certificate here!!
                     |            </ds:X509Certificate>
                     |          </ds:X509Data>
                     |        </ds:KeyInfo>
                     |      </md:KeyDescriptor>
                     |      <md:KeyDescriptor use="encryption">
                     |        <ds:KeyInfo xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
                     |          <ds:X509Data>
                     |            <ds:X509Certificate>
                     |                  Enter the certificate here!!
                     |            </ds:X509Certificate>
                     |          </ds:X509Data>
                     |        </ds:KeyInfo>
                     |      </md:KeyDescriptor>
                     |      <md:SingleLogoutService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" Location="$location"/>
                     |      <md:SingleLogoutService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect" Location="$location"/>
                     |      <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress</md:NameIDFormat>
                     |      <md:NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:transient</md:NameIDFormat>
                     |      <md:NameIDFormat>urn:oasis:names:tc:SAML:2.0:nameid-format:persistent</md:NameIDFormat>
                     |      <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified</md:NameIDFormat>
                     |      <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName</md:NameIDFormat>
                     |      <md:AssertionConsumerService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" Location="$SPAssertionLocation" index="0" isDefault="true"/>
                     |      <md:AssertionConsumerService Binding="urn:oasis:names:tc:SAML:2.0:profiles:holder-of-key:SSO:browser" Location="$SPAssertionLocation" hoksso:ProtocolBinding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Artifact" index="1" xmlns:hoksso="urn:oasis:names:tc:SAML:2.0:profiles:holder-of-key:SSO:browser"/>
                     |      <md:AssertionConsumerService Binding="urn:oasis:names:tc:SAML:2.0:profiles:holder-of-key:SSO:browser" Location="$SPAssertionLocation" hoksso:ProtocolBinding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" index="2" xmlns:hoksso="urn:oasis:names:tc:SAML:2.0:profiles:holder-of-key:SSO:browser"/>
                     |    </md:SPSSODescriptor>
                     |  </md:EntityDescriptor>
                     |""".stripMargin()
            def serviceProviderFile = new File("${FileStructure?.INSTANCE_CONFIG_DIR}/ServiceProvider.xml")
            serviceProviderFile.write content
        }


        private generateIDPXML(location) {

            def content = """| <?xml version="1.0"?>
                         |  <md:EntityDescriptor xmlns:md="urn:oasis:names:tc:SAML:2.0:metadata" entityID="$location" cacheDuration="PT1440M">
                         |      <md:IDPSSODescriptor protocolSupportEnumeration="urn:oasis:names:tc:SAML:2.0:protocol">
                         |          <md:KeyDescriptor use="signing">
                         |              <ds:KeyInfo xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
                         |                  <ds:X509Data>
                         |                      <ds:X509Certificate>
                         |                          <!-- <Extracted-Data This is the certificate of WSO2 / EIS or Identity Provider server using wso2carbon.jks> -->
                         |                      </ds:X509Certificate>
                         |                  </ds:X509Data>
                         |              </ds:KeyInfo>
                         |          </md:KeyDescriptor>
                         |          <md:KeyDescriptor use="encryption">
                         |              <ds:KeyInfo xmlns:ds="http://www.w3.org/2000/09/xmldsig#">
                         |                  <ds:X509Data>
                         |                      <ds:X509Certificate>
                         |                          <!-- <Extracted-Data This is the certificate of WSO2 / EIS or Identity Provider server using wso2carbon.jks> -->
                         |                      </ds:X509Certificate>
                         |                  </ds:X509Data>
                         |              </ds:KeyInfo>
                         |          </md:KeyDescriptor>
                         |          <md:SingleLogoutService Location="$location" Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"/>
                         |          <md:SingleLogoutService Location="$location" Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"/>
                         |          <md:NameIDFormat>urn:oasis:names:tc:SAML:1.1:nameid-format:entity</md:NameIDFormat>
                         |          <md:SingleSignOnService Location="$location" Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"/>
                         |          <md:SingleSignOnService Location="$location" Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Redirect"/>
                         |      </md:IDPSSODescriptor>
                         |      <md:ContactPerson contactType="administrative"/>
                         |  </md:EntityDescriptor>
                     |""".stripMargin()
            def currentDir = System.getProperty("user.dir");
            def identityProviderFile = new File("${FileStructure?.INSTANCE_CONFIG_DIR}/IdentityProvider.xml")
            identityProviderFile.write content

        }

        private certificateInsertion(certificateFile) {
            def currentDir = System.getProperty("user.dir")
            def ant = new AntBuilder()
            ant.replace(file: "${FileStructure?.INSTANCE_CONFIG_DIR}/ServiceProvider.xml", token: "Enter the certificate here!!", value: "$certificateFile")
        }


        private applicationConfigChanges(keystoreName,alias){
            def content ="""|
                        |grails.plugin.springsecurity.saml.active = true
                        |grails.plugin.springsecurity.auth.loginFormUrl = '/saml/login'
                        |grails.plugin.springsecurity.saml.afterLogoutUrl ='/logout/customLogout'
                        |banner.sso.authentication.saml.localLogout='false'                 // To disable single logout set this to true,default 'false'.
                        |grails.plugin.springsecurity.saml.keyManager.storeFile = 'classpath:security/$keystoreName'  // for unix file based Example:- 'file:/home/u02/samlkeystore.jks'
                        |grails.plugin.springsecurity.saml.keyManager.storePass = '<PASSWORD>'
                        |grails.plugin.springsecurity.saml.keyManager.passwords = [ '$alias': '<PASSWORD>' ]
                        |grails.plugin.springsecurity.saml.keyManager.defaultKey = '$alias'
                        |grails.plugin.springsecurity.saml.metadata.sp.file = 'classpath:security/ServiceProvider.xml'    // for unix file based Example:-'/home/u02/sp-local.xml'
                        |grails.plugin.springsecurity.saml.metadata.providers = [adfs: 'classpath:security/IdentityProvider.xml'] // for unix file based Ex:- '/home/u02/idp-local.xml'
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