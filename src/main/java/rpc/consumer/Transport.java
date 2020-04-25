package rpc.consumer;

import rpc.core.Request;
import rpc.core.Response;
import rpc.registry.Registry;
import rpc.util.StreamUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Transport {

    public Response transport(Request request) {
        Map<String, Object> serviceInfo = Registry.getServiceInfo(request.getInterfaceName());
        String host = (String) serviceInfo.get("host");
        Integer port = (Integer) serviceInfo.get("port");
        String service = (String) serviceInfo.get("service");
        Response response = null;
        SocketChannel socket = null;
        try {
            socket = SocketChannel.open();
            socket.configureBlocking(false);
            Selector selector = Selector.open();
            socket.register(selector, SelectionKey.OP_CONNECT);
            socket.connect(new InetSocketAddress(host, port));
            while (true) {
                if (socket.isOpen()) {
                    selector.select();
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = keys.iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isConnectable()) {
                            while (!socket.finishConnect()) {
                                System.out.println("连接中");
                            }
                            socket.register(selector, SelectionKey.OP_WRITE);
                        }
                        if (key.isWritable()) {
                            byte[] bytes = StreamUtil.readObject(request);
                            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                            socket.write(byteBuffer);
                            socket.register(selector, SelectionKey.OP_READ);
                        }
                        if (key.isReadable()) {
                            response = (Response) StreamUtil.readObject(socket);
                        }
                    }
                    if(response != null){
                        break;
                    }
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("客户端异常，请重启！");
        }
        return response;
    }
}
