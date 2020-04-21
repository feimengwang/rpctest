package rpc.provider.impl;

import rpc.provider.HelloService;

public class HelloServiceImpl implements HelloService {
    public void sayHello(String name) {
        System.out.println("Hello " + name);
    }
}
