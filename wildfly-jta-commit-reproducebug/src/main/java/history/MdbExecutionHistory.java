/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package history;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import entrypoint.mdb.AbstractMdb;

/**
 *
 * @author b7godin
 */
@ApplicationScoped
@Named("mdbExecutionHistory")
public class MdbExecutionHistory {


    private static final java.util.logging.Logger LOGGER = Logger.getLogger(MdbExecutionHistory.class.getSimpleName());
    // //////////////////////////////////////////
    // BEGIN: INNER CLASSES
    // //////////////////////////////////////////
    private static class MapValue {

        final String msg;
        final Date startProcessingDate;
        final Date endProcessingDate;

        public MapValue(String msg, Date startProcessingDate, Date endProcessingDate) {
            this.msg = msg;
            this.startProcessingDate = startProcessingDate;
            this.endProcessingDate = endProcessingDate;
        }

        @Override
        public String toString() {
            return "MapValue{" + "msg=" + msg + ", startProcessingDate=" + startProcessingDate + ", endProcessingDate=" + endProcessingDate + '}';
        }


    }

    /**
     * initialize the map of MdbName -> History events after construction so that when an event take place we do not
     * need to double check weather or not a key and value are already in the map.
     */
    @PostConstruct
    public void postconsturct() {
        LOGGER.info(String.format("------------------- %1$s POSTCONSTRUCTED -------------------", this.getClass().
                getSimpleName()));
    }

    final TreeMap<String, List<MapValue>> historyEventy = new TreeMap<String, List<MapValue>>();

    /**
     * puts events into the history
     * <P>
     * NOTE: because of the post construc it is safe to assume that i can just pump into the map the event, since the
     * map shoud already have a (key, List) pair prepared.
     */
    synchronized public <T extends AbstractMdb> void addHistory(Class<T> mdbType, String msg, Date startDate,
            Date endDate) {
        // 1. make sure the map has a (key, value) pair for this mdb type
        preapreHistoryMap(mdbType);
        // 2. log the execution history of the mdb
        historyEventy.get(mdbType.getSimpleName()).add(new MapValue(msg, startDate, endDate));
    }

    /**
     * make sure for each mdbType there is a lsit prepared
     */
    private <T> void preapreHistoryMap(Class<T> mdbType) {
        if (!historyEventy.containsKey(mdbType.getSimpleName())) {
            historyEventy.put(mdbType.getSimpleName(), new ArrayList<MapValue>());
        }
    }

    /**
     * Know wha the MDBs have been processing and at what times. This can help us to determine if weblogic is lunching
     * more of a given mdb to the same queue.
     */
    synchronized public void logIntoServerLogHistory() {
        StringBuilder sb = new StringBuilder();

        for (String mdb : historyEventy.keySet()) {
            sb.append(String.format("START (%1$s). -----------------------------------------%n", mdb));
            for (MapValue historyEvent : historyEventy.get(mdb)) {
                sb.append(String.format("%1$s%n", historyEvent.toString()));
            }
            sb.append(String.format("END (%1$s). -----------------------------------------%n%n%n", mdb));
        }
        LOGGER.info(sb.toString());
    }

}
