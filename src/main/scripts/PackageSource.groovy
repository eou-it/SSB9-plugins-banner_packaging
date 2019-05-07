/* *****************************************************************************
 Copyright 2009-2018 Ellucian Company L.P. and its affiliates.
******************************************************************************/

/**
 *  This script generates zip file src-release-appName-version.zip which has all plugins .git folder.
 */

String applicationRoot = System.getProperty("rootProject.path")
String pluginDirs = applicationRoot + "/plugins/banner_packaging.git"
GroovyClassLoader cLoader = new GroovyClassLoader(this.class.getClassLoader())
this.class.classLoader.parseClass(new File("$pluginDirs/src/main/groovy/net/hedtech/banner/utility/ShellCommandPrefix.groovy"))
String shellCommandPrefix = cLoader.loadClass("net.hedtech.banner.utility.ShellCommandPrefix").getShellCommandPrefix()

def ant = new groovy.util.AntBuilder()
def ln = File.separator
def appDirectoryName = new File(applicationRoot)
def allPluginDetailsList = findPlugins(appDirectoryName, ln)
String applicationName = System.getProperty("rootProject.name")

String appVersion = System.getProperty("rootProject.version")

File sourcePackageZip = new File("${applicationRoot}/build/src-${applicationName}-${appVersion}.zip")
if (sourcePackageZip.exists()) {
    ant.delete(file: sourcePackageZip)
}
def sourceStagingDir = new File("${applicationRoot}/build/source-staging")
ant.delete(dir: sourceStagingDir)
ant.mkdir(dir: sourceStagingDir)

includeSource(sourceStagingDir, shellCommandPrefix, allPluginDetailsList)

ant.zip(destfile: sourcePackageZip) {
    fileset(dir: sourceStagingDir, excludes: "*.lock")
}

ant.delete(dir:"${applicationRoot}/build/source-staging")


private void cloneRepository(sourceStagingDir, originUrl, shellCommandPrefix) {

    // We'll clone the repository as a 'bare' repository, since we do NOT
    // want any possibility that a client might attempt to use this repository
    // as a 'working copy' clone.
    //
    process = "$shellCommandPrefix git clone --bare ${originUrl}".execute(null, new File("${sourceStagingDir}"))
    process.waitFor()
}

private void includeSource(sourceStagingDir, shellCommandPrefix, pluginsDetailsList) {

    cloneRepository(sourceStagingDir, "$shellCommandPrefix git config --get remote.origin.url".execute().text, shellCommandPrefix)

    // Next, we'll clone repositories for all in-house plugins

    pluginsDetailsList.each {
        cloneRepository(sourceStagingDir, "$shellCommandPrefix git config --get remote.origin.url".execute(null, new File(it.path)).text, shellCommandPrefix)
    }

    println "Staging of source: "
    process = "$shellCommandPrefix ls -laF".execute(null, new File("${sourceStagingDir}"))
    process.in.eachLine { line -> println line }

    // Lastly, we'll update the project so that in-place plugins (git submodules)
    // reference the cloned repositories as their 'origin'.

}


private findPlugins(def applicationDir, def pathSep) {
    def plugins = new File(applicationDir.toString() + pathSep + "plugins")
    def pluginPaths = []
    plugins.eachDir { plugin ->
        if (plugin.isDirectory()) {
            pluginPaths.add(plugin)
        }
    }
    return pluginPaths
}
