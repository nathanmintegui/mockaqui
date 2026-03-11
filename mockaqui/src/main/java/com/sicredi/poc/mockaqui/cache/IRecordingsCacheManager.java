package com.sicredi.poc.mockaqui.cache;

import com.sicredi.poc.mockaqui.shared.model.RadixTree;

import java.util.Map;

public interface IRecordingsCacheManager {

    int put(String ldap, String uri);

    RadixTree get(String ldap);

    int delete(String ldap);

    void clear();
}
