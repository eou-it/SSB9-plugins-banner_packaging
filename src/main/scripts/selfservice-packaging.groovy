
String pluginDirs = System.getProperty("user.dir");
println " pluginDirectory is >>>>"+pluginDirs
GroovyClassLoader cLoader = new GroovyClassLoader(this.class.getClassLoader())

def shellCommandPref = this.class.classLoader.parseClass(new File("$pluginDirs/src/main/groovy/net/hedtech/banner/utility/ShellCommandPrefix.groovy"))


String shellCommandPrefix = cLoader.loadClass("net.hedtech.banner.utility.ShellCommandPrefix").getShellCommandPrefix()


println '>>>>>>>>>>>>>shell CommandPrefix ::  ' + shellCommandPrefix

String pluginDirectory = System.getProperty("user.dir");


println(">>>>>>>>>>>>>>>>>>>>>>>    pluginDirectory   " + pluginDirectory)
int endIndex = pluginDirectory.lastIndexOf("/");
println(">>>>>>>>>>>>>>>>>>>>>>>" + endIndex)
String applicationRoot;

//change directory to top level up
if (endIndex != -1) {
    applicationRoot = pluginDirectory.substring(0, endIndex); // not forgot to put check if(endIndex != -1)
    endIndex = applicationRoot.lastIndexOf("/");
    applicationRoot = pluginDirectory.substring(0, endIndex);
    println '  >>>>>>>>> rootDir  is ' + applicationRoot
}
def ant = new groovy.util.AntBuilder()

println "**************************************************************************************"

File pluginRoot = new File(applicationRoot + "/plugins");
File[] files = pluginRoot.listFiles();
for (File file : files) {
    if (file.isDirectory()) {
        System.out.println("directory:" + file.getCanonicalPath());
        println "    ......... Deleting contents of ::" + file.getCanonicalPath() + "/build"

        if( new File(file.getCanonicalPath() + "/build").exists()){
            ant.delete(dir: file.getCanonicalPath() + "/build")
        }
    }
}
if((new File(applicationRoot+"/build")).exists()){
    ant.delete(dir: applicationRoot+"/build", failonerror: false)
}

println "**************************************************************************************"

if (isDirty(applicationRoot, shellCommandPrefix)) {
    println "*******************************************"
    println "<<< WARNING: Git Working Copy NOT Clean >>>"
    println "            (proceeding anyway)"
    println "*******************************************"
}

println ">>>>>>>>> OS is " + System.getProperty('os.name');

if (System.properties.'os.name'.startsWith('Windows')) {
    println ' >>>os is windows'
    command = "grails.bat"

} else {
    command = "grails"
}
println '>>>>>>>>>>>>>>>building WAR'
ant.exec(executable: command, dir: "$applicationRoot", failonerror: true) {
    arg(value: "prod")
    arg(value: "war")
}


String warLocation = applicationRoot + "/build/libs/Release"
File releaseDir = new File(warLocation);
File[] artifacts = releaseDir.listFiles();
String appName;
String appVersion;
for (File file : artifacts) {
    System.out.println("directory:" + file.getCanonicalPath());
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
println "   >>>>>>>>>>> appName is ::" + appName
println "   >>>>>>>>>>> appVersion is ::" + appVersion

System.setProperty('appVersion',appVersion);

System.setProperty('appName',appName);
System.setProperty('applicationRoot', applicationRoot)
println '>>>>>>>>>>>>>>>Generating release.properties'



def message = "Application Name: ${appName} ::  Application Version: ${appVersion} "
println "***************************************************** message ::"+message
println " >>>>>>>>> renaming application.groovy to application.properties"


ant.copy (file: "${applicationRoot}/grails-app/conf/application.groovy", tofile: "${applicationRoot}/build/libs/release/application.properties" )

Properties prop = new Properties();
String propertiesPath= "${applicationRoot}/build/libs/release/application.properties"
InputStream input;
try{
    input = new FileInputStream(propertiesPath);
    prop.load(input)
} catch (IOException ex) {
    ex.printStackTrace();
} finally {
    if (input != null) {
        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
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

println "*****************************************************************************************************"
println "<<< Start Generating release.properties >>>"
println "*****************************************************************************************************"


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
        System.out.println("directory:" + file.getCanonicalPath());
        String pluginName
		if(System.properties.'os.name'.startsWith('Windows')){
			pluginName= getPluginName(applicationRoot + "\\plugins\\", fullPath)
		}else{
			pluginName= getPluginName(applicationRoot + "/plugins/", fullPath)
		}
        def treeish = resolvePluginSha1(applicationRoot,pluginName)
        def branch = getWorkingBranch(fullPath,shellCommandPrefix)
        def status = getStatus( fullPath,shellCommandPrefix )
        println">>>>>>>>>>> plugin name is "+pluginName
        content += "\n#  Plugin         :  ${pluginName}"
        content += "\n#  location       :  ${fullPath}"
        content += "\n#  Git treeish    :  ${treeish}"
        content += "\n#  Git branch     :  ${branch}"
        content += "\n#  Git status     :  ${status}"
        content += "\n"
        content += "\n"
    }
}
def releasePropertiesFile = new File( "$applicationRoot/build/libs/Release/release.properties" );
releasePropertiesFile.write content;

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

println "*****************************************************************************************************"
println "<<< End  Generating release.properties >>>"
println "*****************************************************************************************************"

/*****************************************************************************************************/


println "*****************************************************************************************************"
println "<<< Start updating war with  release.properties >>>"
println "*****************************************************************************************************"
String repackagePath = applicationRoot + "/build/libs/Re-package/"+appName
String warName =appName+"-"+appVersion+".war"
ant.mkdir( dir: repackagePath)
ant.unzip(src: warLocation+"/"+warName, dest: repackagePath, overwrite: "true")

ant.copy (todir: repackagePath+"/WEB-INF/classes"){
    fileset( dir:"$applicationRoot/build/libs/Release", includes:"release.properties" )
}

ant.delete( file: warLocation+"/"+warName )

ant.zip(destfile: warLocation+"/"+warName, basedir: repackagePath, encoding: "UTF-8")

ant.delete( dir: applicationRoot + "/build/libs/Re-package/" )
println "*****************************************************************************************************"
println "<<< Completed updating  release.properties >>>"
println "*****************************************************************************************************"






/*****************************************************************************************************/

println "*****************************************************************************************************"
println "<<< Preparing to   Generating "+ warLocation + "/" + appName + "-" + appVersion + ".zip" +" file >>>"
println "*****************************************************************************************************"
def projectWorkDir = warLocation + "/" + appName
File releasePackageZip = new File(warLocation + "/" + appName + "-" + appVersion + ".zip")

println '>>>>>>>>>>>>>>>Generating deleting releasePackageZip'
ant.delete(file: releasePackageZip)
def configDir = projectWorkDir+"/config"
def i18nDir = projectWorkDir+"/i18n"
def installerDir = projectWorkDir+"/installer"
def instanceDir = projectWorkDir+"/instance"
def libDir= projectWorkDir+"/lib"
def webapp= projectWorkDir+"/webapp"



println '>>>>>>>>>>>>>>>>>>>>>>>>>projectWorkDir is ::' + projectWorkDir
//ant.delete(dir: stagingDir)
println '>>>>>>>>>>>>>>>>>>>>>>>>>creating  ::' + configDir
ant.mkdir(dir: configDir)
println '>>>>>>>>>>>>>>>>>>>>>>>>>creating  ::' + i18nDir
ant.mkdir(dir: i18nDir)
println '>>>>>>>>>>>>>>>>>>>>>>>>>creating  ::' + installerDir
ant.mkdir(dir: installerDir)
println '>>>>>>>>>>>>>>>>>>>>>>>>>creating  ::' + instanceDir
ant.mkdir(dir: instanceDir)
println '>>>>>>>>>>>>>>>>>>>>>>>>>creating  ::' + libDir
ant.mkdir(dir: libDir)
println '>>>>>>>>>>>>>>>>>>>>>>>>>creating  ::' + webapp
ant.mkdir(dir: webapp)

println '>>>>>>>>>>>>>>>>>>>>>>>>>Copying Cofiguration files  ::'
ant.copy (todir: configDir){
    fileset( dir:"${applicationRoot}", includes:"*.example" )
}

ant.copy(todir: configDir) {
    fileset( file:"$applicationRoot/build/libs/saml_configuration.properties" )
}

println '>>>>>>>>>>>>>>>>>>>>>>>>>Copying i18n files  ::' + webapp
ant.copy (todir: i18nDir){
    fileset( dir:"${applicationRoot}/grails-app/i18n", includes:"*.properties" )
}
ant.copy (todir: i18nDir){
    fileset( file:"$applicationRoot/build/libs/Release/release.properties" )
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
}
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
println "*****************************************************************************************************"
println "<<< Completed  Generating "+ warLocation + "/" + appName + "-" + appVersion + ".zip" +" file >>>"
println "*****************************************************************************************************"

private boolean isDirty(dir, shellCommandPrefix) {
    if (getStatus(dir, shellCommandPrefix) != 'clean')
        return true
    else
        return false
}

private String getStatus(dir, shellCommandPrefix) {

    def status = 'not clean (changes found)'
    println '>>>>>>>>>>>>>shellCommandPrefix' + shellCommandPrefix
    println">>>>>>>>>>>>>> dir is "+dir
    process = "$shellCommandPrefix git status".execute(null,new File(dir))
    process.in.eachLine { line ->
        if (line.contains('nothing to commit')) {
            status = 'clean'
            return
        }
    }
    status
}

private String getPluginName(applicationRoot, fullPath){
    println ">>>>>>>>>>>fullPath is ::"+fullPath

    String pluginName = fullPath.replace(applicationRoot, "")
    println ">>>>>>>>>>>> Plugin Name is ::"+pluginName
    return pluginName

}

private String resolvePluginSha1( dir, pluginName ) {
    def gitDir = new File( "${dir}/.git" )
    try{
        if ( gitDir.isDirectory() ) {
            new File("${dir}/.git/modules/plugins/${pluginName}/refs/heads/master").text
        } else {
            ""
        }
    }catch(FileNotFoundException fne){
        println " Directory ::"+"${dir}/.git/modules/plugins/${pluginName}/refs/heads/master" +"::  Missing"
        return ""
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