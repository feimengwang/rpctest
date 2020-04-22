package rpc.provider;

import rpc.core.Request;
import rpc.provider.impl.HelloServiceImpl;
import rpc.register.Register;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    private Map<String,Class> cache = new HashMap();


    public void start() {
        Register.registerService();
        cache.put(HelloService.class.getName(),HelloServiceImpl.class);
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
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        int len = 0;
                        byte[] res = new byte[1024 * 4];
                        try {
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
                            Request request = (Request) objectOutputStream.readObject();
                            invoke(request);
                        } catch (IOException e) {
                            key.cancel();
                            socket.close();
                            System.out.println("客戶端已断开");
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("服务器异常，即将关闭..........");
        }
    }

    private void invoke(Request request) {
        String interfaceName = request.getInterfaceName();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] arguments = request.getArguments();

        Class clazz = cache.get(interfaceName);
        try {
            Object obj = clazz.newInstance();
            Method declaredMethod = clazz.getDeclaredMethod(methodName, parameterTypes);
            Object invoke = declaredMethod.invoke(obj, arguments);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        new Provider().start();
    }
}



