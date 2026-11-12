package edu.utech.helpdesk.client.util;

/** Implemented by controllers that own background threads or subscriptions. */
public interface Disposable {
    void dispose();
}
