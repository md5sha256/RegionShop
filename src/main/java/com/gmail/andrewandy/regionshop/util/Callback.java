package com.gmail.andrewandy.regionshop.util;

@FunctionalInterface
public interface Callback<K> {

    void callback(K k) throws Exception;

}
