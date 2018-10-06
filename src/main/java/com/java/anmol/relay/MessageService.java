package com.java.anmol.relay;

/**
 * Framework for the Message service.
 */
public interface MessageService {

    /**
     * Initialize the service once and for all.
     * Does basic setup and should be called only once or may result in Exception.
     */
    void start();

    /**
     * Stop the service and hence the thread associated with it.
     */
    void quit();

    /**
     * Register the receiver.
     *
     * @param topic    Topic which the receiver will receive the messages on.
     * @param callback Receiver which will receive the messages for the topic.
     */
    void register(String topic, Callback callback);

    /**
     * Unregister all the receivers for a specific topic.
     *
     * @param topic Topic to unregister the receivers against.
     */
    void unregister(String topic);

    /**
     * Unregister the specific receiver for the particular topic only.
     *
     * @param topic    Topic to unregister the receiver for.
     * @param callback Receiver to be unregistered.
     */
    void unregister(String topic, Callback callback);

    /**
     * Send the Message at a particular topic.
     * Ant receiver which is subscribed or may subscribe in the future(provided no quit() called) will receive this message.
     *
     * @param topic   Topic to send the message against.
     * @param message Message to send.
     */
    void send(String topic, Object message);

    /**
     * Clear all the messages for a particular topic.
     *
     * @param topic Topic to clear the messages for.
     */
    void clearMsgsFor(String topic);
}
