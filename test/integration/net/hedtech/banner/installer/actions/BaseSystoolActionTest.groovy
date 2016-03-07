package net.hedtech.banner.installer.actions


import com.sungardhe.commoncomponents.installer.Action
import com.sungardhe.commoncomponents.installer.ActionRunner
import com.sungardhe.commoncomponents.installer.ActionRunnerListener
import com.sungardhe.commoncomponents.installer.Resource
import net.hedtech.banner.installer.actions.BaseSystoolAction
import net.hedtech.banner.installer.actions.CreateWar
import org.junit.*
import org.junit.rules.ExpectedException
import net.hedtech.banner.installer.exception.GenericCustomException

import java.awt.event.ActionListener

class BaseSystoolActionTest {

    static BaseSystoolAction baseSystoolAction;
    static ActionRunner runner=new ActionRunner();
    static Action action;


    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        if (baseSystoolAction==null) {
            baseSystoolAction=new CreateWar();
            baseSystoolAction.actionRunner=runner;
        } else {
            System.out.println("baseSystoolAction object should be singleton in nature..");
        }
    }


    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        baseSystoolAction=null;
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
        Assert.assertSomething(baseSystoolAction.getSharedConfiguration());
    }

}
