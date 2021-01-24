package com.zk.tools;

import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import java.io.UnsupportedEncodingException;

/**
 * 序列化类
 */
public class MyZkSerializer implements ZkSerializer {

    public Object deserialize(byte[] bytes) throws ZkMarshallingError {
        try {
            return new String(bytes, "utf-8");
        } catch (final UnsupportedEncodingException e) {
            throw new ZkMarshallingError(e);
        }
    }

    public byte[] serialize(Object obj) throws ZkMarshallingError {
        try {
            return String.valueOf(obj).getBytes("UTF-8");
        } catch (final UnsupportedEncodingException e) {
            throw new ZkMarshallingError(e);
        }

    }

}
