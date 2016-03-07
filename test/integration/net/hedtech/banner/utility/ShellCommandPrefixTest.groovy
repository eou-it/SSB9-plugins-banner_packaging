package net.hedtech.banner.utility
import net.hedtech.banner.utility.ShellCommandPrefix
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*


/**
 * Created by mohitj on 8/12/15.
 */

class ShellCommandPrefixTest {

    def service
    @Before
    public void init(){
        service =new  ShellCommandPrefix();
    }

    @Test
    void "getShellCommandPrefix should return windows prefix if operating system is windows"(){
        System.setProperty("os.name","Windows");
        assertEquals(ShellCommandPrefix.getShellCommandPrefix(),"cmd /c ")
    }

    @Test
    void "getShellCommandPrefix should return empty prefix if operating system is not windows"(){
        System.setProperty("os.name","linux");
        assertEquals(ShellCommandPrefix.getShellCommandPrefix(),"")
    }
}