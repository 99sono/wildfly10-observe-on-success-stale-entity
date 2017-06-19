
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package entrypoint.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import blogic.ReproduceBugService;
import constants.ApplicationConstants;
import facade.SendJmsMessageFacadeLocal;
import inteceptor.MdbInterceptor;
import messages.DoWorkObjectMessage;
import util.JndiNamesUtil;

/**
 * All the logic of loggging message received and tracking execution time is in the parent this is just a shell class to
 * bind itself to a queue.
 */
@MessageDriven(name = "ReproduceBugMdb", mappedName = "queue/ReproduceBugQueue", activationConfig = {
        @ActivationConfigProperty(propertyName = "endpointExceptionRedeliveryAttempts", propertyValue = "2"),
        @ActivationConfigProperty(propertyName = "endpointExceptionRedeliveryInterval", propertyValue = "200") })
@Interceptors(MdbInterceptor.class)
public class ReproduceBugMdb extends AbstractMdb {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReproduceBugMdb.class);

    @Inject
    ReproduceBugService reproduceBugService;

    @Inject
    SendJmsMessageFacadeLocal sendJmsMessageFacadeLocal;

    @Override
    protected void businessLogic(DoWorkObjectMessage simpleObjectMessage) {
        // (a) we start off by spamming the db with some entities
        // note: out bug in this case is that future transactions in some cases will not immediately see the changes
        reproduceBugService.createSomeEntityEntities(ApplicationConstants.NUMBER_OF_SOME_ENTITY_ENTITIES_TO_CREATE);
        LOGGER.info(
                "business logic invoked. A total of {} should have been persisted. We now report to a queue that the work is done.",
                ApplicationConstants.NUMBER_OF_SOME_ENTITY_ENTITIES_TO_CREATE);

        // (b) we now inform the system test that the work is done
        sendJmsMessageFacadeLocal
                .sendWorkDoneJmsMessageRequiresTransaction(JndiNamesUtil.getJndiQueueNameForWorkDoneMessage());

    }

}
