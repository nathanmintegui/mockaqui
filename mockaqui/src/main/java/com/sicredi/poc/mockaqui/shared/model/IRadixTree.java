package com.sicredi.poc.mockaqui.shared.model;

public interface IRadixTree {
    int insert(String key, Endpoint value);

    Endpoint search(String key);

    int delete(String key);

    void clear();
}
