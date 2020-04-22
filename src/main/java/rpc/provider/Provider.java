package rpc.provider;

import rpc.core.DefaultResponse;
import rpc.core.Request;
import rpc.core.Response;
import rpc.provider.impl.HelloServiceImpl;
import rpc.register.Register;
import rpc.util.StreamUtil;

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
    private Map<String, Class> cache = new HashMap();


    public void start() {
        Register.registerService();
        cache.put(HelloService.class.getName(), HelloServiceImpl.class);
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
                        Request request = (Request) StreamUtil.readObject(socket);
                        if(request !=null) {
                            Object result = invoke(request);
                            Response response = new DefaultResponse();
                            ((DefaultResponse) response).setData(result);
                            ByteBuffer byteBuffer = ByteBuffer.wrap(StreamUtil.readObject(response));
                            socket.write(byteBuffer);
                        }
                        socket.register(selector, SelectionKey.OP_READ);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("服务器异常，即将关闭..........");
        }
    }

    private Object invoke(Request request) {
        String interfaceName = request.getInterfaceName();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] arguments = request.getArguments();

        Class clazz = cache.get(interfaceName);
        try {
            Object obj = clazz.newInstance();
            Method declaredMethod = clazz.getDeclaredMethod(methodName, parameterTypes);
            return declaredMethod.invoke(obj, arguments);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        new Provider().start();
    }
}



