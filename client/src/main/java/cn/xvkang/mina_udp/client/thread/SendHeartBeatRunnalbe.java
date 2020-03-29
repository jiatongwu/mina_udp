package cn.xvkang.mina_udp.client.thread;

import cn.xvkang.mina_udp.client.MinaClient;
import cn.xvkang.mina_udp.common.util.MinaUtils;
import cn.xvkang.phone.netty.protobuf.MyMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 发送心跳包线程
 */
public class SendHeartBeatRunnalbe implements Runnable {
    public static Logger logger = LoggerFactory.getLogger(SendHeartBeatRunnalbe.class);
    private MinaClient minaClient;
    private boolean isStop = false;
    private long lastSendHeartBeatTime = 0L;

    public SendHeartBeatRunnalbe(MinaClient minaClient) {
        this.minaClient = minaClient;
    }

    public MinaClient getMinaClient() {
        return minaClient;
    }

    public void setMinaClient(MinaClient minaClient) {
        this.minaClient = minaClient;
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        while (!isStop) {
            try {
                minaClient.getLock().lock();
                while (minaClient.isReconnecting()) {
                    try {
                        logger.debug("正在进行重连，await发心跳包");
                        minaClient.getSendHeartBeatCondition().await();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (isStop) {
                    continue;
                }

                try {
                    long currentTimeMillis = System.currentTimeMillis();
                    //每隔2秒发送一个心跳包
                    if (currentTimeMillis % 2000 == 0 && currentTimeMillis != lastSendHeartBeatTime) {
                        lastSendHeartBeatTime = currentTimeMillis;

                        //Thread.sleep(2000);
                        //发送心跳包
                        logger.debug("发心跳包");
                        MyMessage.Data data = MyMessage.Data.newBuilder()
                                .setDataType(MyMessage.Data.DataType.HeartRequest)
                                .build();
                        try {
                            MinaUtils.sendData(minaClient.getSession(), data.toByteArray());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        minaClient.setNotReceiveHeartBeatCount(minaClient.getNotReceiveHeartBeatCount() + 1);
                        minaClient.getReconnectCondition().signalAll();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } finally {
                minaClient.getLock().unlock();
            }
        }
    }
}
