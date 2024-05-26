package com.jpoker24.jms;

import java.io.Serializable;

public interface MessageProcessor {
    void processMessage(Serializable message);
}