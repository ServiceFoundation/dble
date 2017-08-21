package io.mycat.config.loader.zkprocess.zktoxml.listen;

import io.mycat.MycatServer;
import io.mycat.config.loader.zkprocess.comm.NotifyService;
import io.mycat.config.loader.zkprocess.comm.ZkConfig;
import io.mycat.config.loader.zkprocess.comm.ZkParamCfg;
import io.mycat.config.loader.zkprocess.comm.ZookeeperProcessListen;
import io.mycat.config.loader.zkprocess.zookeeper.DiretoryInf;
import io.mycat.config.loader.zkprocess.zookeeper.process.ConfStatus;
import io.mycat.config.loader.zkprocess.zookeeper.process.ZkDirectoryImpl;
import io.mycat.config.loader.zkprocess.zookeeper.process.ZkMultLoader;
import io.mycat.manager.response.ReloadConfig;
import io.mycat.manager.response.RollbackConfig;
import io.mycat.util.KVPathUtil;
import io.mycat.util.ZKUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by huqing.yan on 2017/6/23.
 */
public class ConfigStatusListener extends ZkMultLoader implements NotifyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BinlogPauseStatusListener.class);
    public static final String SUCCESS = "SUCCESS";
    private final String currZkPath;
    private Set<NotifyService> childService = new HashSet<>();

    public ConfigStatusListener(ZookeeperProcessListen zookeeperListen, CuratorFramework curator) {
        this.setCurator(curator);
        currZkPath = KVPathUtil.getConfStatusPath();
        zookeeperListen.addWatch(currZkPath, this);
    }

    public void addChild(NotifyService service) {
        childService.add(service);
    }

    @Override
    public boolean notifyProcess() throws Exception {
        if (MycatServer.getInstance().getProcessors() != null) {
            // 通过组合模式进行zk目录树的加载
            DiretoryInf StatusDirectory = new ZkDirectoryImpl(currZkPath, null);
            // 进行递归的数据获取
            this.getTreeDirectory(currZkPath, KVPathUtil.CONF_STATUS, StatusDirectory);
            // 从当前的下一级开始进行遍历,获得到
            ZkDirectoryImpl zkDdata = (ZkDirectoryImpl) StatusDirectory.getSubordinateInfo().get(0);
            ConfStatus status = new ConfStatus(zkDdata.getValue());
            if (status.getFrom().equals(ZkConfig.getInstance().getValue(ZkParamCfg.ZK_CFG_MYID))) {
                return true; //self node
            }
            LOGGER.info("ConfigStatusListener notifyProcess zk to object  :" + status);
            if (status.getStatus() == ConfStatus.Status.ROLLBACK) {
                try {
                    RollbackConfig.rollback();
                    ZKUtils.createTempNode(KVPathUtil.getConfStatusPath(), ZkConfig.getInstance().getValue(ZkParamCfg.ZK_CFG_MYID), SUCCESS.getBytes(StandardCharsets.UTF_8));
                } catch (Exception e) {
                    String ErrorInfo = e.getMessage() == null ? e.toString() : e.getMessage();
                    ZKUtils.createTempNode(KVPathUtil.getConfStatusPath(), ZkConfig.getInstance().getValue(ZkParamCfg.ZK_CFG_MYID), ErrorInfo.getBytes(StandardCharsets.UTF_8));
                }

                return true;
            }
            for (NotifyService service : childService) {
                try {
                    service.notifyProcess();
                } catch (Exception e) {
                    LOGGER.error("ConfigStatusListener notify  error :" + service + " ,Exception info:", e);
                }
            }
            try {
                if (status.getStatus() == ConfStatus.Status.RELOAD_ALL) {
                    ReloadConfig.reload_all();
                } else {
                    ReloadConfig.reload();
                }
                ZKUtils.createTempNode(KVPathUtil.getConfStatusPath(), ZkConfig.getInstance().getValue(ZkParamCfg.ZK_CFG_MYID), SUCCESS.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                String ErrorInfo = e.getMessage() == null ? e.toString() : e.getMessage();
                ZKUtils.createTempNode(KVPathUtil.getConfStatusPath(), ZkConfig.getInstance().getValue(ZkParamCfg.ZK_CFG_MYID), ErrorInfo.getBytes(StandardCharsets.UTF_8));
            }
        }
        return true;
    }
}
