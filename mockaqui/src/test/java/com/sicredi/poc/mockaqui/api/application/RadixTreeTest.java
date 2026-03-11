package com.sicredi.poc.mockaqui.api.application;

import com.sicredi.poc.mockaqui.shared.model.RadixTree;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RadixTreeTest {
    @Test
    void contextLoads() {
        assertEquals(0, RadixTree.getInstance().insert("owner/pets", null));
    }
}