package rpc.core;

import java.io.Serializable;

public class DefaultResponse implements Response,Serializable {
    private Object data;

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public Object getData() {
        return data;
    }
}
