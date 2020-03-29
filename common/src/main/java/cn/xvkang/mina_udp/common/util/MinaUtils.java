package cn.xvkang.mina_udp.common.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;

public class MinaUtils {
    public static void sendData(IoSession session, String data) throws Exception {
        if (StringUtils.isBlank(data)) {
            return;
        }
        byte[] bytes = data.getBytes("UTF-8");
        IoBuffer buffer = IoBuffer.allocate(bytes.length);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();
        session.write(buffer);
    }

    public static void sendData(IoSession session, byte[] bytes) throws Exception {
        if ((bytes == null) || (bytes != null && bytes.length == 0)) {
            return;
        }
        IoBuffer buffer = IoBuffer.allocate(bytes.length);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();
        session.write(buffer);
    }
}
