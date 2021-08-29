package com.github.md5sha256.regionshop.util;

@FunctionalInterface
public interface Callback<K> {

    void callback(K k) throws Exception;

}
