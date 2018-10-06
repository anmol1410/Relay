package com.java.anmol.relay;

import java.util.*;

public abstract class AbstractMessageService implements MessageService {
    private volatile Map<String, List<Object>> messages;
    private volatile Map<String, List<Callback>> receivers;
    private volatile boolean isInterrupted;
    private volatile boolean isStarted;

    AbstractMessageService() {
        messages = new LinkedHashMap<>();
        receivers = new HashMap<>();
    }

    @Override
    public void register(String topic, Callback callback) {
        checkInitilization("register");
        if (receivers.containsKey(topic)) {
            if (!receivers.get(topic).contains(callback)) {
                receivers.get(topic).add(callback);
            }
        } else {
            List<Callback> callbackList = new LinkedList<>();
            callbackList.add(callback);
            receivers.put(topic, callbackList);
        }
    }

    @Override
    public void unregister(String topic) {
        checkInitilization("unregister");
        receivers.remove(topic);
    }

    @Override
    public void unregister(String topic, Callback callback) {
        checkInitilization("unregister");
        if (receivers.containsKey(topic)) {
            receivers.get(topic).remove(callback);
        }
    }

    @Override
    public void send(String topic, Object message) {
        checkInitilization("send");
        if (messages.containsKey(topic)) {
            messages.get(topic).add(message);
        } else {
            List<Object> msgsForTopic = new LinkedList<>();
            msgsForTopic.add(message);
            messages.put(topic, msgsForTopic);
        }
    }

    private void checkInitilization(String method) {
        if (!isStarted) {
            throw new IllegalArgumentException("Must call start() before " + method + "().");
        }
    }

    @Override
    public void start() {
        if (isStarted) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " is already initiated.");
        }
        isStarted = true;
        isInterrupted = false;
        new Thread(new MsgSenderRunnable()).start();
    }

    public void quit() {
        checkInitilization("quit");
        isStarted = false;
        isInterrupted = true;
        messages.clear();
        receivers.clear();
    }

    @Override
    public void clearMsgsFor(String topic) {
        checkInitilization("clearMsgsFor");
        messages.remove(topic);
    }

    private class MsgSenderRunnable implements Runnable {
        @Override
        public void run() {
            while (!isInterrupted) {
                Map<String, List<Object>> messagesCopy = new HashMap<>(messages);
                for (Map.Entry<String, List<Object>> entry : messagesCopy.entrySet()) {
                    if (isInterrupted) {
                        return;
                    }
                    String topic = entry.getKey();
                    List<Callback> callbacks = receivers.get(topic);
                    if (callbacks == null || callbacks.isEmpty()) {
                        continue;
                    }
                    List<Object> msgs = entry.getValue();
                    List<Object> msgsCopy = new LinkedList<>(msgs);
                    List<Object> msgsSent = new LinkedList<>();
                    for (Object msg : msgsCopy) {
                        List<Callback> callbacksCopy = new LinkedList<>(callbacks);
                        for (Callback callback : callbacksCopy) {
                            if (isInterrupted) {
                                return;
                            }
                            callback.onReceive(msg);
                        }
                        msgsSent.add(msg);
                    }
                    if (msgsSent.size() == msgs.size()) {
                        clearMsgsFor(topic);
                    } else {
                        messages.get(topic).removeAll(msgsSent);
                    }
                }
            }
        }
    }
}