String pluginDirs = System.getProperty("user.dir")
pluginDirs = pluginDirs+"\\plugins\\banner_packaging.git"
GroovyClassLoader cLoader = new GroovyClassLoader(this.class.getClassLoader())

def shellCommandPref = this.class.classLoader.parseClass(new File("$pluginDirs/src/main/groovy/net/hedtech/banner/utility/ShellCommandPrefix.groovy"))
String shellCommandPrefix = cLoader.loadClass("net.hedtech.banner.utility.ShellCommandPrefix").getShellCommandPrefix()

String pluginDirectory = System.getProperty("user.dir")
System.setProperty('Dfile.encoding','UTF-8')
pluginDirectory = pluginDirectory+"\\plugins\\banner_packaging.git"
int endIndex = pluginDirectory.lastIndexOf("\\");
String applicationRoot

//change directory to to level up
if (endIndex != -1) {
    applicationRoot = pluginDirectory.substring(0, endIndex)     // not forgot to put check if(endIndex != -1)
    endIndex = applicationRoot.lastIndexOf("\\");
    applicationRoot = pluginDirectory.substring(0, endIndex)
}
def ant = new groovy.util.AntBuilder()

File pluginRoot = new File(applicationRoot + "/plugins")
File[] files = pluginRoot.listFiles();
for (File file : files) {
    if (file.isDirectory()) {
        if( new File(file.getCanonicalPath() + "\\build").exists()){
            ant.delete(dir: file.getCanonicalPath() + "\\build")
        }
    }
}
if((new File(applicationRoot+"\\build")).exists()){
    ant.delete(dir: applicationRoot+"\\build", failonerror: false)
}

if (isDirty(applicationRoot, shellCommandPrefix)) {
    println "*******************************************"
    println "<<< WARNING: Git Working Copy NOT Clean >>>"
    println "            (proceeding anyway)"
    println "*******************************************"
}

println ">>>>>>>>> OS is " + System.getProperty('os.name');

if (System.properties.'os.name'.startsWith('Windows')) {
    command = "grails.bat"

} else {
    command = "grails"
}

ant.exec(executable: command, dir: "$applicationRoot", failonerror: true) {
    arg(value: "prod")
    arg(value: "war")
}


String warLocation = applicationRoot + "/build/libs/Release"
File releaseDir = new File(warLocation);
File[] artifacts = releaseDir.listFiles();
String appName
String appVersion;
for (File file : artifacts) {
    if (file.getName().endsWith(".war")) {
        StringTokenizer tokens = new StringTokenizer(file.getName(), "-");
        if (tokens.hasMoreTokens()) {
            appName = tokens.nextToken()
        }
        if (tokens.hasMoreTokens()) {
            appVersion = tokens.nextToken()
        }
    }
}

appVersion = appVersion.subSequence(0, appVersion.lastIndexOf('.'))

System.setProperty('appVersion',appVersion);

System.setProperty('appName',appName);
System.setProperty('applicationRoot', applicationRoot)

def message = "Application Name: ${appName} ::  Application Version: ${appVersion} "

ant.copy (file: "${applicationRoot}/grails-app/conf/application.groovy", tofile: "${applicationRoot}/build/libs/release/application.properties" )

Properties prop = new Properties();
String propertiesPath= "${applicationRoot}/build/libs/release/application.properties"
InputStream input
try{
    input = new FileInputStream(propertiesPath)
    prop.load(input)
} catch (IOException ex) {
    ex.printStackTrace()
} finally {
    if (input != null) {
        try {
            input.close()
        } catch (IOException e) {
            e.printStackTrace()
        }
    }
}
def buildNumber= prop.getProperty("build.number.uuid")
def buildURL=prop.getProperty("build.number.base.url")
def appId = prop.getProperty("app.appId")
appId = appId.replace("\"", "")

def scmRevision         = "$shellCommandPrefix git rev-parse HEAD".execute(null, new File(applicationRoot)).text
def scmRepository       = "$shellCommandPrefix git config --get remote.origin.url".execute(null, new File(applicationRoot)).text

def workingBranchStatus =  getStatus( applicationRoot, shellCommandPrefix )

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
                         |application.build.number=$buildNumber
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
                         |working copy status: $workingBranchStatus
                         |
                         |#******************************************************
                         |#*                 Plugin Dependencies                *
                         |#**************************************************** */
                         |""".stripMargin();

for (File file : files) {
    if (file.isDirectory()) {
        def fullPath =file.getCanonicalPath()
        String pluginName= getPluginName(applicationRoot + "\\plugins\\", fullPath)
        def treeish = resolvePluginSha1(applicationRoot,pluginName)
        def branch = getWorkingBranch(fullPath,shellCommandPrefix)
        def status = getStatus( fullPath,shellCommandPrefix )
        content += "\n#  Plugin         :  ${pluginName}"
        content += "\n#  location       :  ${fullPath}"
        content += "\n#  Git treeish    :  ${treeish}"
        content += "\n#  Git branch     :  ${branch}"
        content += "\n#  Git status     :  ${status}"
        content += "\n"
        content += "\n"
    }
}
def releasePropertiesFile = new File( "$applicationRoot/build/libs/release.properties" );
releasePropertiesFile.write content

/**
 * SAML CONFIGURATION FILE GENERATION
 */
def samlConfigContent = """|#This file is automatically generated and contains SAML Configuration specific properties.
                     |#This file MUST be changed when SAML configuration is required
                     |#
                     |#  ****** DO NOT EDIT OR TRANSLATE THIS FILE. *******
                     |#
                     |# this is important property do not change it. unless required
                     |appId=$appId
                     |appName=$appName
                     |#DB Details to be entered here in order to connect to db and fetch
                     |dbconnectionURL=jdbc:oracle:thin:@hostname:portnumber:serviceName/SID
                     |#*******************************************************
                     |""".stripMargin()
def samlConfigurationFile = new File("$applicationRoot/build/libs/saml_configuration.properties")
samlConfigurationFile.write samlConfigContent

ant.delete(file: propertiesPath)

def projectWorkDir = warLocation + "/" + appName
File releasePackageZip = new File(warLocation + "/" + appName + "-" + appVersion + ".zip")

ant.delete(file: releasePackageZip)
def configDir = projectWorkDir+"/config"
def i18nDir = projectWorkDir+"/i18n"
def installerDir = projectWorkDir+"/installer"
def instanceDir = projectWorkDir+"/instance"
def libDir= projectWorkDir+"/lib"
def webapp= projectWorkDir+"/webapp"

ant.mkdir(dir: configDir)
ant.mkdir(dir: i18nDir)
ant.mkdir(dir: installerDir)
ant.mkdir(dir: instanceDir)
ant.mkdir(dir: libDir)
ant.mkdir(dir: webapp)
ant.mkdir( dir:"$installerDir/lib" )

ant.copy (todir: configDir){
    fileset( dir:"${applicationRoot}", includes:"*.example" )
    fileset( dir:"$applicationRoot/build/libs", includes:"saml_configuration.properties" )
}

ant.copy (todir: i18nDir){
    fileset( dir:"${applicationRoot}/grails-app/i18n", includes:"*.properties" )
}
ant.copy (todir: i18nDir){
    fileset( dir:"$applicationRoot/build/libs", includes:"release.properties" )
    fileset( file:"$applicationRoot/build/libs/release.properties" )
}

File customDir = new File( "${pluginDirectory}/src/main/installer" )
if (customDir.exists()) {
    ant.copy( todir:"${installerDir}", overwrite:true ) {
        fileset( dir:"${customDir}", includes:"**/*" )
    }
}
ant.mkdir( dir:"$installerDir/lib" )
ant.mkdir( dir:"$instanceDir/i18n" )
ant.mkdir( dir:"$instanceDir/css" )
ant.mkdir( dir:"$instanceDir/js" )
ant.mkdir( dir:"$instanceDir/images" )
ant.mkdir( dir:"$instanceDir/config" )

File instanceProperties = new File( "$instanceDir/config/instance.properties" )
instanceProperties << "shared.config.dir="
ant.copy (todir: libDir){
    fileset( dir:"${warLocation}", includes:"ojdbc*.jar" )
    fileset( dir:"${warLocation}", includes:"xdb*.jar" )
}
ant.copy (todir: webapp){
    fileset( dir:"${warLocation}", includes:"*.war" )
}
ant.copy( todir: "$installerDir/lib", overwrite:true, preservelastmodified:true ) {
    fileset( dir: "${pluginDirectory}/lib", includes:"*.jar" )
    fileset( dir:"${warLocation}", includes:"ojdbc*.jar" )
    fileset( dir:"${warLocation}", includes:"xdb*.jar" )
}

ant.delete(file: "${applicationRoot}/build/libs/release.properties")
ant.delete(file: "${applicationRoot}/build/libs/saml_configuration.properties")

ant.unzip(src: "${pluginDirectory}/src/main/installer/apache-ant-1.8.2-bin.zip", dest: installerDir, overwrite: "true")
ant.zip(destfile: warLocation + "/release-" + appName + "-" + appVersion + ".zip", basedir: projectWorkDir)


ant.zip( destFile: pluginDirectory+"/build/libs/template.zip") {
    zipfileset( dir: "${projectWorkDir}" ) {
        include( name:"installer/**" )

    }
    zipfileset( dir: "${projectWorkDir}" ) {
        include( name:"instance/**" )
    }
}

ant.delete( dir: projectWorkDir )

private boolean isDirty(dir, shellCommandPrefix) {
    if (getStatus(dir, shellCommandPrefix) != 'clean')
        return true
    else
        return false
}

private String getStatus(dir, shellCommandPrefix) {

    def status = 'not clean (changes found)'
    process = "$shellCommandPrefix git status".execute(null, new File(dir))
    process.in.eachLine { line ->
        if (line.contains('nothing to commit')) {
            status = 'clean'
            return
        }
    }
    status
}

private String getPluginName(applicationRoot, fullPath){
    String pluginName = fullPath.replace(applicationRoot, "")
    return pluginName
}

private String resolvePluginSha1( dir, pluginName ) {
    def gitDir = new File( "${dir}/.git" )
    if ( gitDir.isDirectory() ) {
        new File("${dir}/.git/modules/plugins/${pluginName}/refs/heads/master").text
    } else {
        ""
    }
}

private String getWorkingBranch( dir,shellCommandPrefix ) {

    def currentBranch = ''
    matcher = ~/\* (.*)\s/

    process = "$shellCommandPrefix git branch".execute( null, new File( dir ) )
    process.in.eachLine { line ->
        m = line =~ /\*\s+(.*)\s?/
        if (m) {
            currentBranch = m[0][1]
            return
        }
    }
    currentBranch
}