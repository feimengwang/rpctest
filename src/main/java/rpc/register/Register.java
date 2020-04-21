package rpc.register;

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
}
