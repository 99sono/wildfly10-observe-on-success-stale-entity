/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facade;

import org.junit.Before;
import org.junit.Test;

import facade.systemtest.SystemTestFacade;
import facade.systemtest.TestASendMessageToQueueSystemTestImpl;
import util.jndi.JndiUtil;

/**
 * A test class.
 */
public class ExecuteSystemTestA {

    private static final String WAR_NAME = "weblogic-jta-commit-reproducebug";

    /**
     * Remote EJB containing the business logic for the test.
     */
    SystemTestFacade testee;

    /**
     * Startup collaborators.
     */
    @Before
    public void before() {
        testee = JndiUtil.SINGLETON.resolveBean(WAR_NAME, TestASendMessageToQueueSystemTestImpl.class.getSimpleName(),
                SystemTestFacade.class);

    }

    /**
     * Invoke the container run test.
     */
    @Test
    public void executeTest() {
        testee.executeTest();
    }
}
