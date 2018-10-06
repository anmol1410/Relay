package com.java.anmol.relay;

@SuppressWarnings("WeakerAccess")
public class Relay extends AbstractMessageService {

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
    }

    private static class InstanceHolder {
        private static final Relay instance = new Relay();
    }
}