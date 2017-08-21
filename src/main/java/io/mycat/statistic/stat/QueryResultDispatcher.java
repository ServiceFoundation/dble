package io.mycat.statistic.stat;

import io.mycat.MycatServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SQL执行后的派发  QueryResult 事件
 *
 * @author zhuam
 */
public class QueryResultDispatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryResultDispatcher.class);

    private static List<QueryResultListener> listeners = new CopyOnWriteArrayList<QueryResultListener>();

    // 初始化强制加载
    static {
        listeners.add(UserStatAnalyzer.getInstance());
        listeners.add(TableStatAnalyzer.getInstance());
        listeners.add(QueryConditionAnalyzer.getInstance());
    }

    public static void addListener(QueryResultListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        listeners.add(listener);
    }

    public static void removeListener(QueryResultListener listener) {
        listeners.remove(listener);
    }

    public static void removeAllListener() {
        listeners.clear();
    }

    public static void dispatchQuery(final QueryResult queryResult) {
        //TODO：异步分发，待进一步调优
        MycatServer.getInstance().getBusinessExecutor().execute(new Runnable() {

            public void run() {

                for (QueryResultListener listener : listeners) {
                    try {
                        listener.onQueryResult(queryResult);
                    } catch (Exception e) {
                        LOGGER.error("error:", e);
                    }
                }
            }
        });
    }

}