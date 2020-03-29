package cn.xvkang.mina_udp.client.thread;

import cn.xvkang.mina_udp.client.MinaClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 重连线程
 */
public class ReconnectRunnalbe implements Runnable {
    public static Logger logger = LoggerFactory.getLogger(ReconnectRunnalbe.class);
    private boolean isStop = false;
    private MinaClient minaClient;

    public ReconnectRunnalbe(MinaClient minaClient) {
        this.minaClient = minaClient;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (!isStop) {
            try {
                minaClient.getLock().lock();
                while (!((minaClient.getNotReceiveHeartBeatCount() > 10) && ((System.currentTimeMillis() - minaClient.getLastReceiveHeartBeatTime()) > 10000))) {
                    try {
//                        if(minaClient.getNotReceiveHeartBeatCount()>10){
//                            minaClient.setNotReceiveHeartBeatCount(0);
//                        }
                        logger.debug("未收到的心跳包响应<=10 不进行重连:"+minaClient.getNotReceiveHeartBeatCount());
                        minaClient.getReconnectCondition().await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (isStop) {
                    continue;
                }
                //进行重连
                logger.debug("进行重连");
                minaClient.setNotReceiveHeartBeatCount(0);
                minaClient.setReconnecting(true);
                minaClient.setOnLine(false);
                minaClient.restart();
            } finally {
                minaClient.getLock().unlock();
            }
        }
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }

    public MinaClient getMinaClient() {
        return minaClient;
    }

    public void setMinaClient(MinaClient minaClient) {
        this.minaClient = minaClient;
    }
}
