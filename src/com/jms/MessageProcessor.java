package com.jms;

import java.io.Serializable;

public interface MessageProcessor {
    void processMessage(Serializable message);
}