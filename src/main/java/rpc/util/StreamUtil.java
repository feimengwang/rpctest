package rpc.util;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Stream 工具类
 */
public class StreamUtil {

    /**
     * Socket 读取对象
     * @param socket
     * @return
     */
    public static Object readObject(SocketChannel socket) {
        try {
            if (!socket.isConnected()) {
                return null;
            }
            ByteBuffer bf = ByteBuffer.allocate(1024 * 4);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            int len = 0;
            byte[] res = new byte[1024 * 4];
            while ((len = socket.read(bf)) != 0) {
                bf.flip();
                bf.get(res, 0, len);
                bf.clear();
                byteArrayOutputStream.write(res, 0, len);
            }
            byteArrayOutputStream.flush();
            byte[] bytes = byteArrayOutputStream.toByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectOutputStream = new ObjectInputStream(byteArrayInputStream);
            return objectOutputStream.readObject();
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 把对象转成字节数组，对象实现Serializable
     * @param object
     * @return
     */
    public static byte[] readObject(Object object) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(object);
            objectOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
