package rpc.provider;

import rpc.provider.impl.HelloServiceImpl;
import rpc.register.Register;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Provider {

    private void registerService() {
        Map<String,Object> info =new HashMap<>();
        info.put("host","127.0.0.1");
        info.put("port",39390);
        info.put("service",HelloServiceImpl.class.getName());
        Register.addService(HelloService.class.getName(), info);
    }

    public void start() {
        registerService();
        try {
            ServerSocketChannel serverSocket = ServerSocketChannel.open();
            serverSocket.configureBlocking(false);
            serverSocket.bind(new InetSocketAddress("127.0.0.1", 39390));
            Selector selector = Selector.open();
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("服务端开启了");
            while (true) {
                selector.select();
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();
                    if (key.isAcceptable()) {
                        SocketChannel socket = serverSocket.accept();
                        socket.configureBlocking(false);
                        socket.register(selector, SelectionKey.OP_READ);
                    }
                    if (key.isReadable()) {
                        SocketChannel socket = (SocketChannel) key.channel();
                        ByteBuffer bf = ByteBuffer.allocate(1024 * 4);
                        int len = 0;
                        byte[] res = new byte[1024 * 4];
                        try {
                            while ((len = socket.read(bf)) != 0) {
                                bf.flip();
                                bf.get(res, 0, len);
                                System.out.println(new String(res, 0, len));
                                bf.clear();
                            }
                        } catch (IOException e) {
                            key.cancel();
                            socket.close();
                            System.out.println("客戶端已断开");
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("服务器异常，即将关闭..........");
        }
    }
}



