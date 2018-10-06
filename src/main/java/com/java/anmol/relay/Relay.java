package com.java.anmol.relay;

import java.io.Serializable;

@SuppressWarnings("WeakerAccess")
public class Relay extends AbstractMessageService implements Cloneable, Serializable {

    /**
     * Get the singleton instance.
     * <br>
     * Don't forget to call the start() method on it to start the Relay service.
     * <br>
     * You can then use Relay to send/receive messages for the topics.
     * <br> e.g.
     * <br> Relay.instance() -> will initialise Relay.
     * <br> Relay.instance().start() -> start the Service.
     *
     * @return Singleton Relay Instance.
     */
    public static Relay instance() {
        return InstanceHolder.instance;
    }

    private Relay() {
        // For singleton.
        if (instance() != null) {
            throw new RuntimeException("Use instance() method to initialize Relay...");
        }
    }

    @Override
    public Relay clone() {
        return instance();
    }

    protected Object readResolve() {
        return instance();
    }

    private static class InstanceHolder {
        private static final Relay instance = new Relay();
    }
}