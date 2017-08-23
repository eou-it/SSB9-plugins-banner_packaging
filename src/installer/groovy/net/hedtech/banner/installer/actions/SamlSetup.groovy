/* *******************************************************************************
Ellucian 2017 copyright
 **********************************************************************************/
package net.hedtech.banner.installer.actions
import net.hedtech.banner.installer.FileStructure

import com.sungardhe.commoncomponents.installer.ActionRunnerException

/**
 * Installer action for saml setup
 **/

public class SamlSetup extends BaseSystoolAction {
    private File sharedConfigDir

    public String getNameResourceCode() {
        "installer.saml.setup.name"
    }


    public void execute() throws ActionRunnerException {
        def currentDir = System.getProperty("user.dir");
        sharedConfigDir = getSharedConfiguration()
        def config = new ConfigSlurper().parse(resolveFile("${sharedConfigDir.getAbsolutePath()}/saml_configuration.properties").toURL())
        def alias = config?.getProperty("alias")
        def CN = config?.getProperty("CN")
        def OU = config?.getProperty("OU")
        def O = config?.getProperty("O")
        def L = config?.getProperty("L")
        def S = config?.getProperty("S")
        def C = config?.getProperty("C")
        def password1 = config?.getProperty("password")
        def keystoreName = config?.getProperty("keystoreName")
        def serviceProviderLocation = config?.getProperty("serviceProviderLocation")
        def identityProviderLocation = config?.getProperty("identityProviderLocation")
        """keytool -genkey -noprompt -alias $alias -dname "CN=$CN, OU=$OU, O=$O, L=$L, S=$S, C=$C" -keystore $keystoreName -storepass $password1 -keypass $password1""".execute()
        "keytool -export -alias $alias -storepass $password1 -file ${FileStructure?.INSTANCE_CONFIG_DIR}/SERVICE-PROVIDER.cer -keystore $keystoreName".execute()
        def ant = new AntBuilder()
        ant.move( file:"${currentDir}/$keystoreName", todir: "${FileStructure?.INSTANCE_CONFIG_DIR}")
        def certCommand = "keytool -printcert -rfc -file SERVICE-PROVIDER.cer"
        Process certquantCmd = certCommand.execute()
        def certOutput = certquantCmd.in.text
        certOutput = certOutput.replace("-----BEGIN CERTIFICATE-----", "")
        certOutput = certOutput.replace("-----END CERTIFICATE-----", "")
        certOutput = certOutput.trim()
        generateSPXML(alias, serviceProviderLocation)
        generateIDPXML(alias, identityProviderLocation)
        certificateInsertion(certOutput)
        //def config1 = new ConfigSlurper().parse(resolveFile("${FileStructure?.INSTANCE_CONFIG_DIR}/PlatformSandboxApp_configuration.groovy").toURL())
        //println config1?.banner?.sso?.authenticationProvider
        //config1?.banner?.sso?.authenticationProvider = "default"
        applicationConfigChanges(keystoreName,password1,alias)
    }

    //----------- Private Method ------------------------

    private generateSPXML(alias, location) {

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
                     |      <md:AssertionConsumerService Binding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" Location="$location" index="0" isDefault="true"/>
                     |      <md:AssertionConsumerService Binding="urn:oasis:names:tc:SAML:2.0:profiles:holder-of-key:SSO:browser" Location="$location" hoksso:ProtocolBinding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-Artifact" index="1" xmlns:hoksso="urn:oasis:names:tc:SAML:2.0:profiles:holder-of-key:SSO:browser"/>
                     |      <md:AssertionConsumerService Binding="urn:oasis:names:tc:SAML:2.0:profiles:holder-of-key:SSO:browser" Location="$location" hoksso:ProtocolBinding="urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST" index="2" xmlns:hoksso="urn:oasis:names:tc:SAML:2.0:profiles:holder-of-key:SSO:browser"/>
                     |    </md:SPSSODescriptor>
                     |  </md:EntityDescriptor>
                     |""".stripMargin()
        def currentDir = System.getProperty("user.dir");
        def serviceProviderFile = new File("${FileStructure?.INSTANCE_CONFIG_DIR}/ServiceProvider.xml")
        serviceProviderFile.write content
    }


    private generateIDPXML(alias, location) {

        def content = """| <?xml version="1.0"?>
                         |  <md:EntityDescriptor xmlns:md="urn:oasis:names:tc:SAML:2.0:metadata" entityID="$alias" cacheDuration="PT1440M">
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
        def InputXML = new XmlParser().parse("${FileStructure?.INSTANCE_CONFIG_DIR}/ServiceProvider.xml")
        def ant = new AntBuilder()
        ant.replace(file: "${FileStructure?.INSTANCE_CONFIG_DIR}/ServiceProvider.xml", token: "Enter the certificate here!!", value: "$certificateFile")
    }

    private applicationConfigChanges(keystoreName,password1,alias){
        def currentDir = System.getProperty("user.dir")
        def content ="""|
                        |grails.plugin.springsecurity.saml.active = true
                        |grails.plugin.springsecurity.auth.loginFormUrl = '/saml/login'
                        |grails.plugin.springsecurity.saml.afterLogoutUrl ='/logout/customLogout'
                        |banner.sso.authentication.saml.localLogout='false'                 // To disable single logout set this to true,default 'false'.
                        |grails.plugin.springsecurity.saml.keyManager.storeFile = 'classpath:security/$keystoreName'  // for unix file based Example:- 'file:/home/u02/samlkeystore.jks'
                        |grails.plugin.springsecurity.saml.keyManager.storePass = '$password1'
                        |grails.plugin.springsecurity.saml.keyManager.passwords = [ '$alias': '$password1' ]
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