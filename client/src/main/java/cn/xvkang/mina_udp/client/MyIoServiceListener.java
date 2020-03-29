package cn.xvkang.mina_udp.client;


import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyIoServiceListener implements IoServiceListener {
    public static Logger logger = LoggerFactory.getLogger(MyIoServiceListener.class);

    @Override
    public void serviceActivated(IoService ioService) throws Exception {
        logger.debug("serviceActivated");
    }

    @Override
    public void serviceIdle(IoService ioService, IdleStatus idleStatus) throws Exception {
        logger.debug("serviceIdle");
    }

    @Override
    public void serviceDeactivated(IoService ioService) throws Exception {
        logger.debug("serviceDeactivated");
    }

    @Override
    public void sessionCreated(IoSession ioSession) throws Exception {
        logger.debug("sessionCreated");
    }

    @Override
    public void sessionClosed(IoSession ioSession) throws Exception {
        logger.debug("sessionClosed");
    }

    @Override
    public void sessionDestroyed(IoSession ioSession) throws Exception {
        logger.debug("sessionDestroyed");
    }
}
