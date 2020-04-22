package rpc.consumer;

import rpc.core.Request;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Transport {

    public void transport(Request request){
        try {
            SocketChannel socket = SocketChannel.open();
            socket.configureBlocking(false);
            Selector selector = Selector.open();
            socket.register(selector, SelectionKey.OP_CONNECT);
            socket.connect(new InetSocketAddress("host", 0));
            while (true) {
                if (socket.isOpen()) {
                    //在注册的键中选择已准备就绪的事件
                    selector.select();
                    //已选择键集
                    Set<SelectionKey> keys = selector.selectedKeys();
                    Iterator<SelectionKey> iterator = keys.iterator();
                    //处理准备就绪的事件
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        //删除当前键，避免重复消费
                        iterator.remove();
                        //连接
                        if (key.isConnectable()) {
                            //在非阻塞模式下connect也是非阻塞的，所以要确保连接已经建立完成
                            while (!socket.finishConnect()) {
                                System.out.println("连接中");
                            }

                            socket.register(selector, SelectionKey.OP_WRITE);
                        }
                        //控制台监听到有输入，注册OP_WRITE,然后将消息附在attachment中
                        if (key.isWritable()) {
                            //发送消息给服务端
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                            objectOutputStream.writeObject(request);
                            byte[] bytes = byteArrayOutputStream.toByteArray();
                            objectOutputStream.flush();
                            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
                            socket.write(byteBuffer);
                            /*
	                            已处理完此次输入，但OP_WRITE只要当前通道输出方向没有被占用
	                            就会准备就绪，select()不会阻塞（但我们需要控制台触发,在没有输入时
	                            select()需要阻塞），因此改为监听OP_READ事件，该事件只有在socket
	                            有输入时select()才会返回。
                            */
                            socket.register(selector, SelectionKey.OP_READ);
                        }
                        //处理输入事件
                        if (key.isReadable()) {

                            ByteBuffer byteBuffer = ByteBuffer.allocate(1024 * 4);
                            int len = 0;
                            //捕获异常，因为在服务端关闭后会发送FIN报文，会触发read事件，但连接已关闭,此时read()会产生异常
                            try {

                                if ((len = socket.read(byteBuffer)) > 0) {
                                    System.out.println("接收到來自服务器的消息\t");
                                    System.out.println(new String(byteBuffer.array(), 0, len));
                                }
                            } catch (IOException e) {
                                System.out.println("服务器异常，请联系客服人员!正在关闭客户端.........");
                                key.cancel();
                                socket.close();
                            }
                        }
                    }
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("客户端异常，请重启！");
        }
    }
}
