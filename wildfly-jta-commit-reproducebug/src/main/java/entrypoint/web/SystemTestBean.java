/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entrypoint.web;

import static java.lang.String.format;

import java.util.Date;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import facade.ExecutorFacadeLocal;
import facade.systemtest.TestASendMessageToQueueSystemTestImpl;
import facade.systemtest.TestBInserEntitiesFollowByCountEntitiesImpl;
import wildfly.bug.onsuccess.event.SomeEntityChangeAEvent;
import wildfly.bug.onsuccess.event.SomeEntityChangeBEvent;
import wildfly.bug.onsuccess.event.SomeEntityChangeCEvent;
import wildfly.bug.onsuccess.event.SomeEntityChangeDEvent;
import wildfly.bug.onsuccess.facade.ModifyEntityAndFireEventFacade;

/**
 * A CDI bean that we will invoke via JSF to trigger system tests.
 */
@Named("systemTestBean")
@ApplicationScoped
public class SystemTestBean {
    private static final String GLOBAL_CLIENT_ID = null;

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemTestBean.class);

    @Inject
    FacesContext facesContext;

    @Inject
    TestASendMessageToQueueSystemTestImpl testASendMessageToQueueSystemTestImpl;

    @Inject
    TestBInserEntitiesFollowByCountEntitiesImpl testBInserEntitiesFollowByCountEntitiesImpl;

    @Inject
    ExecutorFacadeLocal executorFacadeLocal;

    @Inject
    ModifyEntityAndFireEventFacade modifyEntityAndFireEventFacade;

    //

    /**
     * Fire an event that should be onbserver on success and where in wildfly we will have an issue of stale entity.
     */
    public void fireSomeEntityChangeAEvent() {
        modifyEntityAndFireEventFacade.modifyEntityAndFireEventA();

        addFacesMessageFireEventTestEventTriggeredTriggered(SomeEntityChangeAEvent.class);
    }

    /**
     * Fire an event that should be observed on success and where in wildfly we will have an issue of stale entity.
     */
    public void fireSomeEntityChangeBEvent() {
        modifyEntityAndFireEventFacade.modifyEntityAndFireEventB();

        addFacesMessageFireEventTestEventTriggeredTriggered(SomeEntityChangeBEvent.class);
    }

    public void fireSomeEntityChangeCEvent() {
        modifyEntityAndFireEventFacade.modifyEntityAndFireEventC();

        addFacesMessageFireEventTestEventTriggeredTriggered(SomeEntityChangeCEvent.class);
    }

    public void fireSomeEntityChangeDEvent() {
        // (a) run ther modify entity transaction and harves the event
        SomeEntityChangeDEvent eventD = modifyEntityAndFireEventFacade.modifyEntityAndFireEventDPart1ModifyEntity();

        // (b) Now fire the event sice the previous step must have committed already
        // we hope at least this should be successful
        modifyEntityAndFireEventFacade.modifyEntityAndFireEventDPart2FireEvent(eventD);

        addFacesMessageFireEventTestEventTriggeredTriggered(SomeEntityChangeDEvent.class);
    }

    // JSF EntryPoint actions
    /**
     * Invoke system test A.
     */
    public void triggerTestA() {
        // (a) put the system test to run on an asychronous thread
        executorFacadeLocal.executoreAsynchronousNoJta(new Runnable() {
            @Override
            public void run() {
                try {
                    LOGGER.info(
                            "System test A is now being triggered on asynchronous thread. Please wait for this test to finish. ");
                    testASendMessageToQueueSystemTestImpl.executeTest();
                } catch (Throwable e) {
                    // we catch throwable - as we might fail due to a junit assertion
                    LOGGER.error("Our system test blew up. With error: {} ", e.getMessage(), e);
                }
            }
        });

        // (b) Tell the user to go look at the Admin server log and leave the UI at rest until system test finishes
        addFacesMessageTestATriggered();
    }

    /**
     * Invoke system test B.
     */
    public void triggerTestB() {
        // (a) put the system test to run on an asychronous thread
        executorFacadeLocal.executoreAsynchronousNoJta(new Runnable() {
            @Override
            public void run() {
                try {
                    LOGGER.info(
                            "System test A is now being triggered on asynchronous thread. Please wait for this test to finish. ");
                    testBInserEntitiesFollowByCountEntitiesImpl.executeTest();
                } catch (Throwable e) {
                    // we catch throwable - as we might fail due to a junit assertion
                    LOGGER.error("Our system test blew up. With error: {} ", e.getMessage(), e);
                }
            }
        });

        // (b) Tell the user to go look at the Admin server log and leave the UI at rest until system test finishes
        addFacesMessageTestBTriggered();
    }

    // Helper logic

    private void addFacesMessageFireEventTestEventTriggeredTriggered(Class<?> eventClass) {
        String testName = getMessageDetail(eventClass.getCanonicalName());
        String msgToDisplay = getMessageDetail(testName);
        String msgSummary = format("Test : %1$s has been trigered. CurrentDateValue is: %2$s",
                eventClass.getCanonicalName(), new Date().toString());
        addInfoFacesMessage(msgSummary, msgToDisplay);
    }

    /**
     * Pump a faces message telling system test A is executing.
     */
    private void addFacesMessageTestATriggered() {
        String testName = getMessageDetail(TestASendMessageToQueueSystemTestImpl.class.getCanonicalName());
        String msgToDisplay = getMessageDetail(testName);
        String msgSummary = format("Test : %1$s has been trigered.",
                TestASendMessageToQueueSystemTestImpl.class.getCanonicalName());
        addInfoFacesMessage(msgSummary, msgToDisplay);
    }

    /**
     * Pump a faces message telling system test A is executing.
     */
    private void addFacesMessageTestBTriggered() {
        String testName = getMessageDetail(TestASendMessageToQueueSystemTestImpl.class.getCanonicalName());
        String msgToDisplay = getMessageDetail(testName);
        String msgSummary = format("Test : %1$s has been trigered.",
                TestBInserEntitiesFollowByCountEntitiesImpl.class.getCanonicalName());
        addInfoFacesMessage(msgSummary, msgToDisplay);
    }

    /**
     * Tell the user that he has triggered as system test.
     */
    private String getMessageDetail(String testName) {
        return format(
                "Test : %1$s has been trigered. %n" + "Please monitor the AdminServerLog while the test is ongoing. %n"
                        + "Do not trigger another system test in parallerl. %n"
                        + "We do not exercise any flow control to prevent you from triggering more than one test in parallel.",
                testName);
    }

    /**
     * Produce a faces message to be rendered on the p messages component during render response.
     */
    private void addInfoFacesMessage(String summary, String detail) {
        facesContext.addMessage(GLOBAL_CLIENT_ID, new FacesMessage(FacesMessage.SEVERITY_INFO, summary, detail));
    }

}
