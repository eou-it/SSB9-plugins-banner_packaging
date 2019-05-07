/* *****************************************************************************
 Copyright 2014 Ellucian Company L.P. and its affiliates.
 ****************************************************************************** */

package net.hedtech.banner.utility

public class ShellCommandPrefix {

    private static final OS_NAME_PROPERTY = "os.name"
    private static final OS_WINDOWS = "Windows"
    private static final WINDOWS_SHELL_PREFIX = "cmd /c "
    private static final EMPTY_PREFIX = ""

    public static String getShellCommandPrefix(){
        String operatingSystem = System.getProperty(OS_NAME_PROPERTY);
        if(operatingSystem.startsWith(OS_WINDOWS)){
            return WINDOWS_SHELL_PREFIX
        }
        return EMPTY_PREFIX
    }

}
