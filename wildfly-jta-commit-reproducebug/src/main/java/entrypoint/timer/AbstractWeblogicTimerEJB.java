package entrypoint.timer;

import java.util.Random;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.ScheduleExpression;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import facade.SendJmsMessageFacadeLocal;

/**
 *
 * @author b7godin
 */
public abstract class AbstractWeblogicTimerEJB {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWeblogicTimerEJB.class);

    @Resource
    TimerService timerService;
    @Inject
    SendJmsMessageFacadeLocal sendJmsMessageFacadeLocal;

    volatile Timer currentTimer = null;
    private int executionNumber = 0;
    private ScheduleExpression scheduleExpression;
    private Random random = new Random();

    // //////////////////////////////////
    // CONSTRUCTIOn
    // ////////////////////////////////////
    /**
     * run every second the timer + the transaction time which will be greater than one second due to thead sleep
     */
    public AbstractWeblogicTimerEJB() {
        this(new ScheduleExpression().hour("*").minute("*").second("*/10"));
    }

    /**
     * Create a new <code>AbstractWMTimer</code> using given timer schedule settings.
     */
    public AbstractWeblogicTimerEJB(ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
        LOGGER.debug("-------------- Timer created ---------------");
    }

    /**
     * EJB Timer time out function. This will trigger the WM 6 Timer poll method.
     */
    @Timeout
    public void timeout(Timer timer) {
        LOGGER.trace("Executing poll method of the timer with configured timeout expression {} ",
                getScheduleExpression());
        try {
            poll();
        } catch (Exception ex) {
            LOGGER.error("Exception took place executing timer.", ex);
            throw new RuntimeException("Wrapped timer exception ", ex);
        } finally {
            incrementExecutionNumber();
        }
    }

    // //////////////////////////////////
    // EXECUTION LOGIC
    // ////////////////////////////////////
    /**
     * should send message to queue
     */
    public abstract void poll();

    protected void sendToQueueAysnchronous(ConnectionFactory connectionFactory, String jndiQueueName, String message) {
        sendJmsMessageFacadeLocal.sendDoWorkJmsMessageAsynchronousNewJtaTransaction(message, jndiQueueName);
    }

    protected void sendToQueue(String jndiQueueName, String message) {
        sendJmsMessageFacadeLocal.sendDoWorkJmsMessageNewJtaTransaction(message, jndiQueueName);
    }

    protected int getRandomNumber() {
        return random.nextInt();
    }

    public ScheduleExpression getScheduleExpression() {
        return scheduleExpression;
    }

    public void setScheduleExpression(ScheduleExpression scheduleExpression) {
        this.scheduleExpression = scheduleExpression;
    }

    // //////////////////////////////////
    // START THE TIMER
    // ////////////////////////////////////
    @PostConstruct
    public void postConstruct() {
        LOGGER.info("---------------- POST CONSTRUCT [{}] -------------------", this.getClass().getSimpleName());
        startTimer();
    }

    public void startTimer() {
        LOGGER.info("Starting timer. [{}]", this.getClass().getSimpleName());
        currentTimer = timerService.createCalendarTimer(scheduleExpression, new TimerConfig());
    }

    // not needed
    private void stopTimer() {
        if (currentTimer != null) {
            currentTimer.cancel();
            currentTimer = null;
        }

    }

    // //////////////////////////////////
    // BOILER PLATE
    // ////////////////////////////////////
    public abstract String getTimerName();

    /**
     * Current execution number of the timer.
     *
     * @return the current execution number of the timer
     */
    public int getExecutionNumber() {
        return executionNumber;
    }

    public void setExecutionNumber(int executionNumber) {
        this.executionNumber = executionNumber;
    }

    synchronized private int incrementExecutionNumber() {
        return ++executionNumber;
    }

}
