/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package entrypoint.timer;

import static util.JndiNamesUtil.getJndiQueueNameForReproduceBugQueue;

import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constants.ApplicationConstants;

/**
 * The purpose of this class is to pump messages into queues.
 *
 */
@Singleton
@Startup
@DependsOn(value = "LogHistoryTimerEJB")
public class PumpMessageTimerAEJB extends AbstractWeblogicTimerEJB {

    private static final Logger LOGGER = LoggerFactory.getLogger(PumpMessageTimerAEJB.class.getCanonicalName());

    /**
     * sends message to a queue. The MDB that receives it should blow up. The idea is to test the dead message queue
     * feature of weblogic.
     */
    @Override
    public void poll() {
        if (ApplicationConstants.PUMP_MESSAGE_TIMER_ENABLED) {
            //
            final int currentExecutionNumber = getExecutionNumber();
            final String currentMessageText = String.format("MESSAGE: Batch execution number: %1$05d  - Timer: %2$s ",
                    currentExecutionNumber, getTimerName());
            LOGGER.info("MDB about to send the following message: {} to Queues {}", currentMessageText,
                    ApplicationConstants.NUMBER_OF_MDBS_TO_PUBLISH_MESSAGE_TO);

            // publish 52 asynchronous messages to 52 MDB queues
            for (int currentMdbIndex = 0; currentMdbIndex < ApplicationConstants.NUMBER_OF_MDBS_TO_PUBLISH_MESSAGE_TO; currentMdbIndex++) {
                String jndiQueueName = getJndiQueueNameForReproduceBugQueue(currentMdbIndex + 1);
                // send one message per AM Process queue
                sendToQueue(jndiQueueName, currentMessageText);
            }
        }

    }

    @Override
    public String getTimerName() {
        return PumpMessageTimerAEJB.class.getSimpleName();
    }
}
