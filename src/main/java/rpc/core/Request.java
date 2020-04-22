package rpc.core;



public interface Request {

    String getInterfaceName();

    String getMethodName();

    Object[] getArguments();

    Class<?>[] getParameterTypes();

}
