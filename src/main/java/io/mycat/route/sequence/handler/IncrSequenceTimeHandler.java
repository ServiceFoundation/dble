package io.mycat.route.sequence.handler;

import io.mycat.route.util.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class IncrSequenceTimeHandler implements SequenceHandler {
    protected static final Logger LOGGER = LoggerFactory.getLogger(IncrSequenceTimeHandler.class);

    private static final String SEQUENCE_TIME_PROPS = "sequence_time_conf.properties";
    private static final IncrSequenceTimeHandler instance = new IncrSequenceTimeHandler();
    private IdWorker workey;


    public static IncrSequenceTimeHandler getInstance() {
        return IncrSequenceTimeHandler.instance;
    }

    private IncrSequenceTimeHandler() {
        load();
    }


    public void load() {
        // load sequnce properties
        Properties props = PropertiesUtil.loadProps(SEQUENCE_TIME_PROPS);

        long workid = Long.parseLong(props.getProperty("WORKID"));
        long dataCenterId = Long.parseLong(props.getProperty("DATAACENTERID"));

        workey = new IdWorker(workid, dataCenterId);
    }

    @Override
    public long nextId(String prefixName) {
        return workey.nextId();
    }


    /**
     * Deprecated:
     * 64位ID (42(毫秒)+5(机器ID)+5(业务编码)+12(重复累加))
     *
     * @author sw
     * <p>
     * Now:
     * 64位ID (30(毫秒)+5(机器ID)+5(业务编码)+12(重复累加)+12(毫秒))
     */
    static class IdWorker {
        private final static long twepoch = 1288834974657L;
        private final static long timestampLowBits = 12L;
        private final static long timestampLowMask = 0xFFF;

        // 机器标识位数
        private final static long workerIdBits = 5L;
        // 数据中心标识位数
        private final static long datacenterIdBits = 5L;
        // 机器ID最大值 31
        private final static long maxWorkerId = -1L ^ (-1L << workerIdBits);
        // 数据中心ID最大值 31
        private final static long maxDatacenterId = -1L ^ (-1L << datacenterIdBits);
        // 毫秒内自增位
        private final static long sequenceBits = 12L;

        private final static long sequenceShift = timestampLowBits;
        private final static long workerIdShift = sequenceBits + timestampLowBits;
        private final static long datacenterIdShift = workerIdBits + sequenceBits + timestampLowBits;
        private final static long timestampHighShift = datacenterIdBits + workerIdBits + sequenceBits + timestampLowBits;

        private final static long sequenceMask = -1L ^ (-1L << sequenceBits);

        private static long lastTimestamp = -1L;

        private long sequence = 0L;
        private final long workerId;
        private final long datacenterId;

        public IdWorker(long workerId, long datacenterId) {
            if (workerId > maxWorkerId || workerId < 0) {
                throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", maxWorkerId));
            }
            if (datacenterId > maxDatacenterId || datacenterId < 0) {
                throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", maxDatacenterId));
            }
            this.workerId = workerId;
            this.datacenterId = datacenterId;
        }

        public synchronized long nextId() {
            long timestamp = timeGen();
            if (timestamp < lastTimestamp) {
                try {
                    throw new Exception("Clock moved backwards.  Refusing to generate id for "
                            + (lastTimestamp - timestamp) + " milliseconds");
                } catch (Exception e) {
                    LOGGER.error("error", e);
                }
            }

            if (lastTimestamp == timestamp) {
                // 当前毫秒内，则+1
                sequence = (sequence + 1) & sequenceMask;
                if (sequence == 0) {
                    // 当前毫秒内计数满了，则等待下一秒
                    timestamp = tilNextMillis(lastTimestamp);
                }
            } else {
                sequence = 0;
            }
            lastTimestamp = timestamp;

            // ID偏移组合生成最终的ID，并返回ID

            //42 bit timestamp, right shift 12 bit ,get high 30 bit,than left shift 34 bit
            long nextId = (((timestamp - twepoch) >> timestampLowBits) << timestampHighShift)
                    | (datacenterId << datacenterIdShift)
                    | (workerId << workerIdShift)
                    | (sequence << sequenceShift)
                    | ((timestamp - twepoch) & timestampLowMask);

            return nextId;
        }

        private long tilNextMillis(final long lastTimestamp) {
            long timestamp = this.timeGen();
            while (timestamp <= lastTimestamp) {
                timestamp = this.timeGen();
            }
            return timestamp;
        }

        private long timeGen() {
            return System.currentTimeMillis();
        }
    }

}
