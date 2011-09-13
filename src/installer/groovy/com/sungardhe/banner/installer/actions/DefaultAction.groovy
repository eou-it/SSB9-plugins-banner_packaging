/*******************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 *******************************************************************************/
package com.sungardhe.banner.installer.actions

import org.springframework.beans.factory.annotation.Required
import com.sungardhe.commoncomponents.installer.*
import org.apache.tools.ant.taskdefs.*
import org.apache.tools.ant.types.*
import com.sungardhe.banner.installer.*

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