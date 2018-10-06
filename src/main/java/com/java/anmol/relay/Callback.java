package com.java.anmol.relay;

/**
 * This will be called by the Internal worker thread.
 * So any operation inside the onReceiver method will be called on worker thread.
 */
public interface Callback {

    /**
     * Called on worker thread.
     *
     * @param message Message received.
     */
    void onReceive(Object message);
}
