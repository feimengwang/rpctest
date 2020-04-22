package rpc.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class DefaultRequest implements Serializable, Request {

    private static final long serialVersionUID = 7478520607109127572L;

    private String interfaceName;
    private String methodName;
    private Object[] arguments;
    private Class<?>[] parameterTypes;

    @Override
    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    @Override
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    @Override
    public Class<?>[] getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(Class<?>[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }


}
