package com.sicredi.poc.mockaqui.cache.internal;

import com.sicredi.poc.mockaqui.cache.IRecordingsCacheManager;
import com.sicredi.poc.mockaqui.shared.model.RadixTree;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
final class RecordingsRecordingsCacheManager implements IRecordingsCacheManager {

    private final Cache cache;

    RecordingsRecordingsCacheManager(CacheManager cacheManager) {
        this.cache = cacheManager.getCache("recordings");
        if (this.cache == null) {
            throw new IllegalStateException("Cache 'recordings' not found");
        }
    }

    @Override
    public int put(final String ldap, final String uri) {
        RadixTree radixTree = RadixTree.create();
        radixTree.insert(uri, null);
        cache.put(ldap, radixTree);
        return 0;
    }

    @Override
    public RadixTree get(final String ldap) {
        return cache.get(ldap, RadixTree.class);
    }

    @Override
    public int delete(final String ldap) {
        cache.evict(ldap);
        return 0;
    }

    @Override
    public void clear() {
        cache.clear();
    }
}
