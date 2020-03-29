package cn.xvkang.mina_udp.client;

import cn.xvkang.mina_udp.common.util.MinaUtils;
import cn.xvkang.phone.netty.protobuf.MyMessage;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.FilterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.InetSocketAddress;

public class ConectorHandler extends IoHandlerAdapter {
    public static Logger logger = LoggerFactory.getLogger(ConectorHandler.class);
    private MinaClient minaClient;

    public ConectorHandler(MinaClient minaClient) {
        this.minaClient = minaClient;
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        super.sessionCreated(session);
        logger.debug("sessionCreated");
        InetSocketAddress remoteAddress = (InetSocketAddress) session.getRemoteAddress();
        logger.debug("服务器：" + remoteAddress.getAddress().toString());
        logger.debug(remoteAddress.getPort() + "");
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        super.sessionOpened(session);
        logger.debug("sessionOpened");
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        super.sessionClosed(session);
        logger.debug("sessionClosed");
        InetSocketAddress remoteAddress = (InetSocketAddress) session.getRemoteAddress();
        logger.debug("服务器：" + remoteAddress.getAddress().toString());
        logger.debug(remoteAddress.getPort() + "");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        super.sessionIdle(session, status);
        logger.debug("sessionIdle");
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        //super.exceptionCaught(session, cause);
        cause.printStackTrace();
        logger.debug("exceptionCaught");

    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        //super.messageReceived(session, message);
        logger.debug("messageReceived");
        if (message instanceof IoBuffer) {
            //IoBuffer buffer = (IoBuffer) message;
            InetSocketAddress remoteAddress = (InetSocketAddress) session.getRemoteAddress();
            logger.debug("服务器：" + remoteAddress.getAddress().toString());
            logger.debug(remoteAddress.getPort() + "");


            InetSocketAddress localAddress = (InetSocketAddress) session.getLocalAddress();
            logger.debug("自己客户端端口：" + localAddress.getAddress().toString());
            logger.debug(localAddress.getPort() + "");


            IoBuffer buffer = (IoBuffer) message;
            byte[] buf = new byte[buffer.limit()];
            buffer.get(buf);

            MyMessage.Data msg = MyMessage.Data.parseFrom(buf);
            MyMessage.Data.DataType dataType = msg.getDataType();
            if (dataType == MyMessage.Data.DataType.CLOSE) {
                String number = msg.getCloseData().getNumber();
                System.out.println("客户端收到：" + number);
            } else if (dataType == MyMessage.Data.DataType.OPEN) {
                MyMessage.Open open = msg.getOpenData();
                String email = open.getEmail();
                System.out.println("客户端收到：" + email);
            } else if (dataType == MyMessage.Data.DataType.HeartResponse) {
                logger.debug("客户端收到心跳响应");
                minaClient.setOnLine(true);
                int notReceiveHeartBeatCount = minaClient.getNotReceiveHeartBeatCount();
                minaClient.setLastReceiveHeartBeatTime(System.currentTimeMillis());
                if(notReceiveHeartBeatCount>0) {
                    minaClient.setNotReceiveHeartBeatCount(notReceiveHeartBeatCount-1);
                }
            }
        }
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        super.messageSent(session, message);
        logger.debug("messageSent");
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {
        super.inputClosed(session);
        logger.debug("inputClosed");
    }

    @Override
    public void event(IoSession session, FilterEvent event) throws Exception {
        super.event(session, event);
        logger.debug("event");
    }
}
