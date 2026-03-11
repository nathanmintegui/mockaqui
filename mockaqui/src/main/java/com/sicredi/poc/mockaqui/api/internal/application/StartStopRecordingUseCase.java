package com.sicredi.poc.mockaqui.api.internal.application;

import com.sicredi.poc.mockaqui.anotation.UseCase;
import com.sicredi.poc.mockaqui.api.IStartStopRecordingUseCase;
import com.sicredi.poc.mockaqui.cache.IRecordingsCacheManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@UseCase
public class StartStopRecordingUseCase implements IStartStopRecordingUseCase {

    private final IRecordingsCacheManager recordingsCacheManager;

    @Override
    public int execute(final String ldap, final String uri) {
        try {
            // @@TODO: validate ldap content

            if (recordingsCacheManager.get(ldap) != null) {
                if (recordingsCacheManager.delete(ldap) == -1)
                    throw new RuntimeException(
                            String.format("Could not delete key %s on recordings cache", ldap)
                    );
            } else {
                if (recordingsCacheManager.put(ldap, uri) == -1) {
                    throw new RuntimeException(
                            String.format("Could not put data on key %s of recordings cache", ldap)
                    );
                }
            }

            return 0;
        } catch (Exception e) {
            log.error("ERROR : [StartStopRecordingUseCase.execute]: An error has occurred, reason, {}.", e.getMessage());
            return -1;
        }
    }
}
