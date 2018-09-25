package banner.packaging


import grails.dev.commands.*
import grails.plugins.GrailsPlugin

class GenerateReleaseinfoCommand implements GrailsApplicationCommand {

    boolean handle() {

        println"    Hello Lokesh this is get-plugininfo command >>>>>>>>>>>>>>>>>"
        println"    Hello Lokesh this is get-plugininfo command >>>>>>>>>>>>>>>>>"
        //println ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>+{rootProject.name}"+rootProject.name
        def applicationContext = grails.util.Holders.grailsApplication.mainContext
        ArrayList<GrailsPlugin> pluginList = applicationContext.getBean('pluginManager').allPlugins
        println '#######################################################################################'
        for (GrailsPlugin plugin : pluginList) {

            println "Plugin Name ====> " + plugin.name
            println "Plugin version ====> " + plugin.version

        }

        println '#######################################################################################'

        return true
    }
}
