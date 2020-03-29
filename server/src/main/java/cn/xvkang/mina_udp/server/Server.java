package cn.xvkang.mina_udp.server;

import org.apache.mina.core.filterchain.DefaultIoFilterChainBuilder;
import org.apache.mina.filter.logging.LoggingFilter;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.apache.mina.transport.socket.nio.NioDatagramAcceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class Server {
    public static Logger logger= LoggerFactory.getLogger(Server.class);
    public static void main(String[] args) throws Exception{
        NioDatagramAcceptor acceptor = new NioDatagramAcceptor();
        acceptor.setHandler(new AcceptorHandler());
        DefaultIoFilterChainBuilder chain = acceptor.getFilterChain();
        chain.addLast("logger", new LoggingFilter());
        DatagramSessionConfig dcfg = acceptor.getSessionConfig();
        dcfg.setReuseAddress(true);
        SocketAddress socketAddress=new InetSocketAddress(Integer.parseInt(args[0]));
        acceptor.bind(socketAddress);
    }
}
