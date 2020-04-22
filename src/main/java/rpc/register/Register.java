package rpc.register;

import rpc.provider.HelloService;
import rpc.provider.impl.HelloServiceImpl;

import java.util.HashMap;
import java.util.Map;

public class Register {
    private static Map<String, Map<String,Object>> register = new HashMap<>();

    public static void addService(String serviceName, Map map) {
        register.put(serviceName, map);
    }

    public static Map<String,Object> getServiceInfo(String serviceName) {
        return register.get(serviceName);
    }
    public static void registerService() {
        Map<String,Object> info =new HashMap<>();
        info.put("host","127.0.0.1");
        info.put("port",39390);
        info.put("service", HelloServiceImpl.class.getName());
        Register.addService(HelloService.class.getName(), info);
    }
}
