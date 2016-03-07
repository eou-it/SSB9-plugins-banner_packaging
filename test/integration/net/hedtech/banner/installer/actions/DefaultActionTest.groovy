package net.hedtech.banner.installer.actions

import com.sungardhe.commoncomponents.installer.Action
import com.sungardhe.commoncomponents.installer.ActionRunner
import net.hedtech.banner.installer.actions.CreateWar
import net.hedtech.banner.installer.actions.DefaultAction
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

/**
 * Created by mohitj on 8/26/2015.
 */
class DefaultActionTest {
    static def DefaultAction defaultAction;
    static ActionRunner runner=new ActionRunner();
    static Action action;
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        if (defaultAction==null) {
            defaultAction=new CreateWar();
            defaultAction.actionRunner=runner;
        } else {
            System.out.println("baseSystoolAction object should be singleton in nature..");
        }
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        defaultAction=null;
        System.gc();
    }

    @Before
    public void setUp() throws Exception {
        System.out.println("setUp()");
    }

    @After
    public void tearDown() throws Throwable {
        System.out.println("tearDown()\n");
    }

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();


    @Test
    public void "getSharedConfiguration should throw File not found exception if file is not present"() {
        expectedEx.expect(FileNotFoundException.class);
        //expectedEx.expectMessage("File not found");
        Assert.assertSomething(defaultAction.getSharedConfiguration());
    }


}