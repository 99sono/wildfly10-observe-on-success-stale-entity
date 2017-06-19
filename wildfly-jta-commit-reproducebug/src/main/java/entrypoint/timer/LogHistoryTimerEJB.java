package entrypoint.timer;

import javax.ejb.ScheduleExpression;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import constants.ApplicationConstants;
import history.MdbExecutionHistory;

/**
 * Create a timer that periodically dumps the messages accumuldated in the Mdb Execution History cache
 */
@Singleton
@Startup
public class LogHistoryTimerEJB extends AbstractWeblogicTimerEJB {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogHistoryTimerEJB.class);

    @Inject
    MdbExecutionHistory mdbExecutionHistory;

    /**
     * Create a new LogHistoryTimerEJB.
     *
     */
    public LogHistoryTimerEJB() {
        super(new ScheduleExpression().hour("*").minute("*").second("*/20"));
    }

    @Override
    public void poll() {
        if (ApplicationConstants.MDB_EXECUTION_LOG_HISTORY_ENABLED) {
            mdbExecutionHistory.logIntoServerLogHistory();
        }
    }

    @Override
    public String getTimerName() {
        return LogHistoryTimerEJB.class.getSimpleName();
    }

}
