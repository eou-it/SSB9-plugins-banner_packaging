/*******************************************************************************
 © 2011 SunGard Higher Education.  All Rights Reserved.

 CONFIDENTIAL BUSINESS INFORMATION

 THIS PROGRAM IS PROPRIETARY INFORMATION OF SUNGARD HIGHER EDUCATION
 AND IS NOT TO BE COPIED, REPRODUCED, LENT, OR DISPOSED OF,
 NOR USED FOR ANY PURPOSE OTHER THAN THAT WHICH IT IS SPECIFICALLY PROVIDED
 WITHOUT THE WRITTEN PERMISSION OF THE SAID COMPANY
 *******************************************************************************/
package com.sungardhe.banner.installer

public interface FileStructure {
	public static final String DIST_DIR = "../dist"
	public static final String WEBAPP_DIR = "../webapp"
	public static final String INSTANCE_DIR = "../instance"
	public static final String INSTANCE_CONFIG_DIR = INSTANCE_DIR + "/config"
	public static final String LOCAL_INSTANCE_PROPERTIES = INSTANCE_CONFIG_DIR + "/instance.properties"
	public static final String INSTANCE_I18N_DIR = INSTANCE_DIR + "/i18n"
	public static final String I18N_DIR = "../i18n"
	public static final String INSTANCE_JS_DIR = INSTANCE_DIR + "/js"
	public static final String INSTANCE_CSS_DIR = INSTANCE_DIR + "/css"
	
	
}