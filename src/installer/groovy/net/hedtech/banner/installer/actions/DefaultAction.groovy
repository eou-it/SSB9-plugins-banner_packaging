/* ********************************************************************************
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

import org.springframework.beans.factory.annotation.Required
import net.hedtech.commoncomponents.installer.*
import org.apache.tools.ant.taskdefs.*
import org.apache.tools.ant.types.*
import net.hedtech.banner.installer.*

/**
 * Installer action for assembling a deployable ear from a template.
 **/
abstract
public class DefaultAction extends Action {

			
	protected Properties getProperties( String filePath ) {
		File f = resolveFile( filePath )
		Properties p = new Properties()
		p.load( new FileInputStream( f ) )
		p
	}


	protected Properties getInstanceProperties() {
		getProperties( FileStructure.LOCAL_INSTANCE_PROPERTIES )
	}
	
	
    protected File getSharedConfiguration() {
		String sharedConfigDirName = getInstanceProperties().getProperty( "shared.config.dir" )
		if (sharedConfigDirName?.trim()?.size() == 0) {
			throw new RuntimeException( "Shared config dir not set" )
		}
		File sharedConfDir = resolveFile( sharedConfigDirName )
		if (!sharedConfDir.exists()) {
			throw new RuntimeException( "Shared config dir: ${sharedConfigDirName} does not exist" )
		}
        sharedConfDir
    }

	
	protected String getBanner() {
	   '''
       |    ______                                    ____
       |   (____  \\                                  / __ \\
       |    ____)  ) ____ ____  ____   ____  ____   ( (__) )
       |   |  __  ( / _  |  _ \\|  _ \\ / _  )/ ___)   \\__  /
       |   | |__)  | ( | | | | | | | ( (/ /| |         / /
       |   |______/ \\_||_|_| |_|_| |_|\\____)_|        /_/
        '''.stripMargin()
	}
	
}
