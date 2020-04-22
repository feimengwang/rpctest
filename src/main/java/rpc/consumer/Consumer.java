package rpc.consumer;

import rpc.core.DefaultRequest;
import rpc.core.Request;
import rpc.core.Response;
import rpc.provider.HelloService;
import rpc.register.Register;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class Consumer {

    public <T> T getService(Class<T> clazz) {
        return (T) Proxy.newProxyInstance(Consumer.class.getClassLoader(), new Class[]{clazz}, new MyInvocationHandler(clazz));
    }

    private class MyInvocationHandler implements InvocationHandler {
        Class clazz;

        public MyInvocationHandler(Class clazz) {
            this.clazz = clazz;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Request request = buildRequest(method, args, clazz);
            return getResult(request).getData();
        }
    }

    private Response getResult(Request request) {
        Transport transport = new Transport();
        return transport.transport(request);
    }

    private Request buildRequest(Method method, Object[] args, Class clazz) {
        Request request = new DefaultRequest();
        ((DefaultRequest) request).setMethodName(method.getName());
        ((DefaultRequest) request).setInterfaceName(clazz.getName());
        ((DefaultRequest) request).setArguments(args);
        ((DefaultRequest) request).setParameterTypes(method.getParameterTypes());
        return request;
    }

    public static void main(String[] args) {
        Consumer consumer = new Consumer();
        Register.registerService();
        HelloService service = consumer.getService(HelloService.class);
        service.sayHello("world!");
        System.out.println(service.getName());
    }
}
