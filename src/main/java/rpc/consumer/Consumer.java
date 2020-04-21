package rpc.consumer;

import rpc.register.Register;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

public class Consumer {

    public <T> T getService(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),clazz.getInterfaces(),new MyInvocationHandler(clazz));
    }

    private class MyInvocationHandler implements InvocationHandler {
        Class clazz;

        public MyInvocationHandler(Class clazz) {
            this.clazz = clazz;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            return null;
        }
    }

    private Object getResult(Class clazz){
        Map<String, Object> serviceInfo = Register.getServiceInfo(clazz.getName());
        String host = (String) serviceInfo.get("host");
        int port = (int) serviceInfo.get("port");
        String service = (String) serviceInfo.get("service");

    }
}
