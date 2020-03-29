package cn.xvkang.mina_udp.client;

import cn.xvkang.mina_udp.client.thread.ReconnectRunnalbe;
import cn.xvkang.mina_udp.client.thread.SendHeartBeatRunnalbe;
import cn.xvkang.phone.netty.protobuf.MyMessage;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;

import org.apache.mina.core.service.IoServiceListener;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MinaClient {
    public static void main(String[] args) {
        MinaClient minaClient = new MinaClient(args[0], Integer.parseInt(args[1]));
        minaClient.start();


        //minaClient.stop();
//        Thread t=new Thread(new Runnable() {
//            @Override
//            public void run() {
//                for(;;) {
//                    try {
//                        Thread.sleep(10000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
//                    System.out.println("目前有线程：" + threadSet);
//                    System.out.println("目前有线程：" + threadSet.size());
//                }
//            }
//        });
//        t.start();

    }

    public static Logger logger = LoggerFactory.getLogger(MinaClient.class);

    private IoSession session;
    private NioDatagramConnector connector;
    private ConectorHandler conectorHandler;
    private IoFutureListener ioFutureListener;
    private String serverHostname;
    private int serverPort;
    private ConnectFuture connFuture;
    private IoServiceListener ioServiceListener;

    private long lastReceiveHeartBeatTime=0L;
    //是否在线
    private boolean onLine = false;
    //是否正在进行重连
    private boolean isReconnecting = false;
    //连续几次没有收到心跳包了
    private int notReceiveHeartBeatCount = 0;

    //重连线程 和 发送心跳包线程 使用的锁
    private Lock lock = new ReentrantLock();
    private Condition sendHeartBeatCondition = lock.newCondition();
    private Condition reconnectCondition = lock.newCondition();
    //重连线程 和 发送心跳包线程
    private ReconnectRunnalbe reconnectRunnalbe;
    private SendHeartBeatRunnalbe sendHeartBeatRunnalbe;

    public MinaClient(String serverHostname, int serverPort) {
        this.serverHostname = serverHostname;
        this.serverPort = serverPort;
        init();
    }

    private void init() {

    }

    private class MyIoFutureListener implements IoFutureListener {

        @Override
        public void operationComplete(IoFuture future) {
            ConnectFuture connFuture = (ConnectFuture) future;
            if (connFuture.isConnected()) {
                IoSession session = future.getSession();
                InetSocketAddress localAddress = (InetSocketAddress) session.getLocalAddress();
                logger.debug("客户端自己端口：" + localAddress.getAddress().toString());
                logger.debug(localAddress.getPort() + "");

                InetSocketAddress remoteAddress = (InetSocketAddress) session.getRemoteAddress();
                logger.debug("连接上服务器：" + remoteAddress.getAddress().toString());
                logger.debug(remoteAddress.getPort() + "");
                MinaClient.this.setSession(session);
                MinaClient.this.setOnLine(true);
                MinaClient.this.setReconnecting(false);
                try {
                    MinaClient.this.getLock().lock();
                    //开始发心跳包
                    MinaClient.this.getSendHeartBeatCondition().signalAll();
                }finally{
                    MinaClient.this.getLock().unlock();
                }
            } else {
                logger.info("Not connected...exiting");
            }

        }
    }

    public void start() {
        logger.debug("minaClient start()");
        sendHeartBeatRunnalbe = new SendHeartBeatRunnalbe(this);
        reconnectRunnalbe = new ReconnectRunnalbe(this);

        connector = new NioDatagramConnector();
        connector.addListener(ioServiceListener);
        conectorHandler = new ConectorHandler(this);
        connector.setHandler(conectorHandler);
        ioFutureListener = new MyIoFutureListener();
        connFuture = connector.connect(new InetSocketAddress(serverHostname, serverPort));
        // connFuture.awaitUninterruptibly();
        connFuture.addListener(ioFutureListener);
        ioServiceListener = new MyIoServiceListener();

        logger.debug("启动重连线程和心跳线程");
        //启动重连线程
        Thread reconnectThread = new Thread(reconnectRunnalbe);
        reconnectThread.start();
        //启动心跳线程
        Thread sendHeartBeatThread = new Thread(sendHeartBeatRunnalbe);
        sendHeartBeatThread.start();

    }

    public void stop() {
        logger.debug("minaClient stop()");
        sendHeartBeatRunnalbe.setStop(true);
        reconnectRunnalbe.setStop(true);
        try {
            lock.lock();
            sendHeartBeatCondition.signalAll();
            reconnectCondition.signalAll();
        }finally {
            lock.unlock();
        }
        connector.dispose();
    }

    public void restart() {
        logger.debug("minaClient restart()");
        stop();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        start();
    }

    public void sendData(MyMessage.Data data) {

    }

    public IoSession getSession() {
        return session;
    }

    public void setSession(IoSession session) {
        this.session = session;
    }

    public NioDatagramConnector getConnector() {
        return connector;
    }

    public void setConnector(NioDatagramConnector connector) {
        this.connector = connector;
    }

    public ConectorHandler getConectorHandler() {
        return conectorHandler;
    }

    public void setConectorHandler(ConectorHandler conectorHandler) {
        this.conectorHandler = conectorHandler;
    }

    public IoFutureListener getIoFutureListener() {
        return ioFutureListener;
    }

    public void setIoFutureListener(IoFutureListener ioFutureListener) {
        this.ioFutureListener = ioFutureListener;
    }

    public String getServerHostname() {
        return serverHostname;
    }

    public void setServerHostname(String serverHostname) {
        this.serverHostname = serverHostname;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }


    public boolean isOnLine() {
        return onLine;
    }

    public void setOnLine(boolean onLine) {
        this.onLine = onLine;
    }

    public ConnectFuture getConnFuture() {
        return connFuture;
    }

    public void setConnFuture(ConnectFuture connFuture) {
        this.connFuture = connFuture;
    }

    public int getNotReceiveHeartBeatCount() {
        return notReceiveHeartBeatCount;
    }

    public void setNotReceiveHeartBeatCount(int notReceiveHeartBeatCount) {
        this.notReceiveHeartBeatCount = notReceiveHeartBeatCount;
    }

    public Lock getLock() {
        return lock;
    }

    public void setLock(Lock lock) {
        this.lock = lock;
    }

    public Condition getSendHeartBeatCondition() {
        return sendHeartBeatCondition;
    }

    public void setSendHeartBeatCondition(Condition sendHeartBeatCondition) {
        this.sendHeartBeatCondition = sendHeartBeatCondition;
    }

    public Condition getReconnectCondition() {
        return reconnectCondition;
    }

    public void setReconnectCondition(Condition reconnectCondition) {
        this.reconnectCondition = reconnectCondition;
    }

    public boolean isReconnecting() {
        return isReconnecting;
    }

    public void setReconnecting(boolean reconnecting) {
        isReconnecting = reconnecting;
    }

    public ReconnectRunnalbe getReconnectRunnalbe() {
        return reconnectRunnalbe;
    }

    public void setReconnectRunnalbe(ReconnectRunnalbe reconnectRunnalbe) {
        this.reconnectRunnalbe = reconnectRunnalbe;
    }

    public SendHeartBeatRunnalbe getSendHeartBeatRunnalbe() {
        return sendHeartBeatRunnalbe;
    }

    public void setSendHeartBeatRunnalbe(SendHeartBeatRunnalbe sendHeartBeatRunnalbe) {
        this.sendHeartBeatRunnalbe = sendHeartBeatRunnalbe;
    }

    public long getLastReceiveHeartBeatTime() {
        return lastReceiveHeartBeatTime;
    }

    public void setLastReceiveHeartBeatTime(long lastReceiveHeartBeatTime) {
        this.lastReceiveHeartBeatTime = lastReceiveHeartBeatTime;
    }
}
