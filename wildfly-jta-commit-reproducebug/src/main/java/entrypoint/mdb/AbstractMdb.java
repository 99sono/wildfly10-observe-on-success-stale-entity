/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entrypoint.mdb;

import java.util.Date;

import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constants.ApplicationConstants;
import history.MdbExecutionHistory;
import messages.DoWorkObjectMessage;

/**
 * An mdb extending this class will always blow up.
 *
 */
public abstract class AbstractMdb implements MessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMdb.class);

    @Inject
    MdbExecutionHistory mdbHistory;

    /**
     * Handling of JMS message.
     */
    @Override
    public void onMessage(Message message) {
        // (a) some basic initializations
        final Date startDate = new Date();
        final DoWorkObjectMessage simpleObjectMessage = getMessage(message);
        String jmsMessageId = getJmsMessageId(message);
        StringBuilder messageToLog = new StringBuilder();
        messageToLog.append(" The MDB: ").append(this.getType().getSimpleName()).append(" Received message ")
                .append(String.format("JMS MESSAGE ID: %1$s ", jmsMessageId)).append(" with text: ")
                .append(simpleObjectMessage.getMessageText());
        // (b) we want to track how long it took for the message send to "start" being processed
        long msElapsedSinceSendAndProcessOfMessage = startDate.getTime()
                - simpleObjectMessage.getMessageSendDate().getTime();
        messageToLog.append(String.format(" Weblogic took a total of ( %1$s ms) to deliver us the message.",
                msElapsedSinceSendAndProcessOfMessage));
        boolean weblogicTooSlow = ApplicationConstants.MAX_ALLOWED_NUMBER_OF_MS_BETWEEN_SEND_AND_RECEIVE < msElapsedSinceSendAndProcessOfMessage;
        if (weblogicTooSlow) {
            LOGGER.warn("Weblogic was too slow to prcess message {} taking a total of {} ms to give us the message",
                    jmsMessageId, msElapsedSinceSendAndProcessOfMessage);
        }

        try {
            // (c) let the MDB log that it got a message
            logSimpleMessageAtMdbExecutionStart(simpleObjectMessage);

            // (d) if the implementation bean wants to do something with the message so be it
            businessLogic(simpleObjectMessage);

        } finally {
            if (ApplicationConstants.MDB_EXECUTION_LOG_HISTORY_ENABLED) {
                final Date endDate = new Date();
                mdbHistory.addHistory(this.getType(), messageToLog.toString(), startDate, endDate);
            }
        }
    }

    /**
     * Concrete classes decide what to do with the message
     */
    protected abstract void businessLogic(DoWorkObjectMessage simpleObjectMessage);

    // helper logic ...

    /**
     * wraps call to getJmsMessageId to not be bothered with check exception.
     */
    protected String getJmsMessageId(Message message) {
        try {
            return message.getJMSMessageID();
        } catch (JMSException e) {
            throw new RuntimeException(String.format("Failed to get JMS message id of object %1$s", message), e);
        }
    }

    /**
     * Get the content of a message assumed to be a JMS text message.
     *
     * @param message
     *            jms message
     * @return the text content
     */
    private DoWorkObjectMessage getMessage(Message message) {
        try {
            ObjectMessage jmsObjectMessage = (ObjectMessage) message;
            return (DoWorkObjectMessage) jmsObjectMessage.getObject();
        } catch (JMSException ex) {
            throw new RuntimeException("Failed to read the data in the received message object: " + message, ex);
        }
    }

    private void logSimpleMessageAtMdbExecutionStart(DoWorkObjectMessage simpleObjectMessage) {
        String msgToLog = String.format("%n%1$s %nMDB (%3$s) received message: %2$s %n%1$s%n",
                ApplicationConstants.LONG_SLASH_LINE, simpleObjectMessage.getMessageText(),
                this.getClass().getSimpleName());
        LOGGER.info(msgToLog);
    }

    /**
     * @return the real implementation class for the mdb, not a proxy.
     */
    public Class<? extends AbstractMdb> getType() {
        return this.getClass();
    }

}
