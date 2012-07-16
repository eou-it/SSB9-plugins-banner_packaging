/*********************************************************************************
 Copyright 2009-2011 SunGard Higher Education. All Rights Reserved.
 This copyrighted software contains confidential and proprietary information of 
 SunGard Higher Education and its subsidiaries. Any use of this software is limited 
 solely to SunGard Higher Education licensees, and is further subject to the terms 
 and conditions of one or more written license agreements between SunGard Higher 
 Education and the licensee in question. SunGard is either a registered trademark or
 trademark of SunGard Data Systems in the U.S.A. and/or other regions and/or countries.
 Banner and Luminis are either registered trademarks or trademarks of SunGard Higher 
 Education in the U.S.A. and/or other regions and/or countries.
 **********************************************************************************/
package net.hedtech.banner.installer

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
