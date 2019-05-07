package banner.packaging


import grails.dev.commands.*
import grails.plugins.GrailsPlugin

class GenerateReleaseinfoCommand implements GrailsApplicationCommand {

    boolean handle() {

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
