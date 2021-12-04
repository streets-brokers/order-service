package com.streets.ordersvc.common.types;

// This is just a helper type for returning multiple values from a method
public class Tuple2<K, V> {
    private K isValid;
    private V msg;

    public Tuple2(K first, V second) {
        this.isValid = first;
        this.msg = second;
    }

    public V getMsg() {
        return msg;
    }

    public void setMsg(V msg) {
        this.msg = msg;
    }

    public K getIsValid() {
        return isValid;
    }

    public void setIsValid(K isValid) {
        this.isValid = isValid;
    }
}
