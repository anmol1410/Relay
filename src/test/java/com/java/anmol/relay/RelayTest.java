package com.java.anmol.relay;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

@SuppressWarnings({"unused", "UnusedAssignment"})
public class RelayTest {

    private MessageService relay;
    private static boolean flagForTesting;
    private static int totalChanges;
    private static String flagForString;
    private boolean isTeardownRequired = true;

    private enum Topics {
        TOPIC_ONE("TOPIC_ONE", "TOPIC_ONE_MESSAGE"),
        TOPIC_TWO("TOPIC_TWO", "TOPIC_TWO_MESSAGE"),
        TOPIC_THREE("TOPIC_THREE", "TOPIC_THREE_MESSAGE");

        private String topicName;
        private String topicMsg;

        Topics(String topicName, String topicMsg) {
            this.topicName = topicName;
            this.topicMsg = topicMsg;
        }

        public String getTopicName() {
            return topicName;
        }

        public String getTopicMsg() {
            return topicMsg;
        }
    }

    private class ReceiverWhichWorksOnBooleanFlag implements Callback {

        private ReceiverWhichWorksOnBooleanFlag() {
            Relay.instance().register(Topics.TOPIC_ONE.getTopicName(), this);
        }

        @Override
        public void onReceive(Object message) {
            flagForTesting = (boolean) message;
        }
    }


    private class ReceiverWhichWorksOnStringFlag implements Callback {

        private ReceiverWhichWorksOnStringFlag() {
            Relay.instance().register(Topics.TOPIC_THREE.getTopicName(), this);
        }

        @Override
        public void onReceive(Object message) {
            flagForString += message;
        }
    }

    private class ReceiverWhichIncrementsIntFlag implements Callback {

        private ReceiverWhichIncrementsIntFlag() {
            Relay.instance().register(Topics.TOPIC_TWO.getTopicName(), this);
        }

        @Override
        public void onReceive(Object message) {
            totalChanges += (Integer) message;
        }
    }

    @org.junit.Before
    public void setUp() {
        relay = Relay.instance();
        relay.start();
        totalChanges = 0;
        flagForString = "";
        flagForTesting = false;
    }

    @org.junit.After
    public void tearDown() {
        if (isTeardownRequired) {
            relay.quit();
        }
    }

    @Test
    public void verifyMessageSentOnce() throws InterruptedException {
        ReceiverWhichWorksOnBooleanFlag receiverWhichWorksOnBooleanFlag = new ReceiverWhichWorksOnBooleanFlag();
        relay.send(Topics.TOPIC_ONE.getTopicName(), true);
        Thread.sleep(5);
        Assert.assertTrue(flagForTesting);
        receiverWhichWorksOnBooleanFlag = null;
    }

    @Test
    public void verifyMessageSentTwiceWithDelay() throws InterruptedException {
        ReceiverWhichIncrementsIntFlag receiverWhichIncrementsIntFlag = new ReceiverWhichIncrementsIntFlag();
        relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        Thread.sleep(5000);
        relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        Thread.sleep(5);
        Assert.assertEquals(2, totalChanges);
        receiverWhichIncrementsIntFlag = null;
    }

    @Test
    public void verifyMessageSentMultipleTimes() throws InterruptedException {
        ReceiverWhichIncrementsIntFlag receiverWhichIncrementsIntFlag = new ReceiverWhichIncrementsIntFlag();
        relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        Thread.sleep(5);
        Assert.assertEquals(3, totalChanges);
        receiverWhichIncrementsIntFlag = null;
    }

    @Test
    public void verifyMessageSentMultipleTimesToMultReceivers() throws InterruptedException {
        ReceiverWhichIncrementsIntFlag receiverWhichIncrementsIntFlag = new ReceiverWhichIncrementsIntFlag();
        ReceiverWhichIncrementsIntFlag receiverWhichIncrementsIntFlag1 = new ReceiverWhichIncrementsIntFlag();
        ReceiverWhichIncrementsIntFlag receiverWhichIncrementsIntFlag2 = new ReceiverWhichIncrementsIntFlag();
        relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        Thread.sleep(5);
        Assert.assertEquals(9, totalChanges);
        receiverWhichIncrementsIntFlag = null;
        receiverWhichIncrementsIntFlag1 = null;
        receiverWhichIncrementsIntFlag2 = null;
    }

    @Test
    public void verifyMessageSentMultipleTimesToMultReceivesWithInitCancelCalled() throws InterruptedException {
        ReceiverWhichIncrementsIntFlag receiverWhichIncrementsIntFlag = new ReceiverWhichIncrementsIntFlag();
        ReceiverWhichIncrementsIntFlag receiverWhichIncrementsIntFlag1 = new ReceiverWhichIncrementsIntFlag();
        relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        Thread.sleep(5);
        relay.quit();
        relay.start();
        ReceiverWhichIncrementsIntFlag receiverWhichIncrementsIntFlag2 = new ReceiverWhichIncrementsIntFlag();
        relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        Thread.sleep(5);
        Assert.assertEquals(5, totalChanges);
        receiverWhichIncrementsIntFlag = null;
        receiverWhichIncrementsIntFlag1 = null;
        receiverWhichIncrementsIntFlag2 = null;
    }

    @Test
    public void verifyNoMessageSentAfterQuit() throws InterruptedException {
        relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        Thread.sleep(5);
        relay.clearMsgsFor(Topics.TOPIC_TWO.getTopicName());
        ReceiverWhichIncrementsIntFlag receiverWhichIncrementsIntFlag = new ReceiverWhichIncrementsIntFlag();
        Thread.sleep(5);
        Assert.assertEquals(0, totalChanges);
        receiverWhichIncrementsIntFlag = null;
    }

    @Test
    public void verifyNoMessageReceivedAfterDeregister() throws InterruptedException {
        relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        Thread.sleep(5);
        ReceiverWhichIncrementsIntFlag receiverWhichIncrementsIntFlag = new ReceiverWhichIncrementsIntFlag();
        Thread.sleep(5);
        relay.unregister(Topics.TOPIC_TWO.getTopicName());
        Thread.sleep(5);
        relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        Thread.sleep(5);
        Assert.assertEquals(1, totalChanges);
        receiverWhichIncrementsIntFlag = null;
    }

    @Test
    public void verifyNoMessageReceivedAfterDeregisterWithTopic() throws InterruptedException {
        relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        ReceiverWhichIncrementsIntFlag receiverWhichIncrementsIntFlag = new ReceiverWhichIncrementsIntFlag();
        Thread.sleep(5);
        relay.unregister(Topics.TOPIC_TWO.getTopicName(), receiverWhichIncrementsIntFlag);
        Thread.sleep(5);
        relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        Thread.sleep(5);
        Assert.assertEquals(1, totalChanges);
        receiverWhichIncrementsIntFlag = null;
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenInitCalledTwiceThenExceptionThrownTest() {
        relay.start();
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenAnyMethodCalledBeforeInitThenExceptionThrownTest() {
        relay.quit();
        isTeardownRequired = false;
        relay.send(",", "");
    }

    @Test
    public void verifyLargeNumOfMsgsSendAcross() throws InterruptedException {
        ReceiverWhichIncrementsIntFlag receiverWhichIncrementsIntFlag = new ReceiverWhichIncrementsIntFlag();
        for (int x = 0; x < 200; x++) {
            relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        }
        Thread.sleep(50);
        Assert.assertEquals(200, totalChanges);
        receiverWhichIncrementsIntFlag = null;
    }

    @Test
    public void verifyOutputWhenSendAndRemovedTogether() throws InterruptedException {
        ReceiverWhichIncrementsIntFlag receiverWhichIncrementsIntFlag = new ReceiverWhichIncrementsIntFlag();
        for (int x = 0; x < 200; x++) {
            relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
            relay.clearMsgsFor(Topics.TOPIC_TWO.getTopicName());
        }
        Thread.sleep(50);
        Assert.assertEquals(0, totalChanges);
        receiverWhichIncrementsIntFlag = null;
    }

    @Test
    public void verifyWhenSendAndRemovedOneAfterAnother() throws InterruptedException {
        ReceiverWhichIncrementsIntFlag receiverWhichIncrementsIntFlag = new ReceiverWhichIncrementsIntFlag();
        for (int x = 0; x < 200; x++) {
            relay.send(Topics.TOPIC_TWO.getTopicName(), 1);
        }
        Thread.sleep(50);
        relay.clearMsgsFor(Topics.TOPIC_TWO.getTopicName());
        Assert.assertEquals(200, totalChanges);
        receiverWhichIncrementsIntFlag = null;
    }

    @Test
    public void verifyCalledAndReceivedOnSameThread() throws InterruptedException {
        ReceiverWhichWorksOnBooleanFlag receiverWhichWorksOnBooleanFlag = new ReceiverWhichWorksOnBooleanFlag();
        relay.send(Topics.TOPIC_ONE.getTopicName(), true);

        Thread.sleep(50);
        receiverWhichWorksOnBooleanFlag = null;
    }

    @Test
    public void verifyMsgsReceivedInCorrectOrder() throws InterruptedException {
        ReceiverWhichWorksOnStringFlag receiverWhichWorksOnStringFlag = new ReceiverWhichWorksOnStringFlag();
        StringBuilder expected = new StringBuilder();
        for (int x = 0; x < 100; x++) {
            relay.send(Topics.TOPIC_THREE.getTopicName(), x);
            expected.append(x);
        }
        Thread.sleep(50);
        Assert.assertEquals(expected.toString(), flagForString);
        receiverWhichWorksOnStringFlag = null;
    }

    @Test
    public void verifySingleReceiverRegisteredIfMoreThanOnce() throws InterruptedException {
        ReceiverWhichWorksOnStringFlag receiverWhichWorksOnStringFlag = new ReceiverWhichWorksOnStringFlag();
        Relay.instance().register(Topics.TOPIC_THREE.getTopicName(), receiverWhichWorksOnStringFlag);
        Relay.instance().register(Topics.TOPIC_THREE.getTopicName(), receiverWhichWorksOnStringFlag);
        Relay.instance().register(Topics.TOPIC_THREE.getTopicName(), receiverWhichWorksOnStringFlag);
        Relay.instance().register(Topics.TOPIC_THREE.getTopicName(), receiverWhichWorksOnStringFlag);
        Relay.instance().register(Topics.TOPIC_THREE.getTopicName(), receiverWhichWorksOnStringFlag);
        StringBuilder expected = new StringBuilder();
        for (int x = 0; x < 100; x++) {
            relay.send(Topics.TOPIC_THREE.getTopicName(), x);
            expected.append(x);
        }
        Thread.sleep(50);
        Assert.assertEquals(expected.toString(), flagForString);
        receiverWhichWorksOnStringFlag = null;
    }

    @Test(expected = InvocationTargetException.class)
    public void verifyExceptionIfSingletonBreakByReflection() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<?>[] constructors = Relay.class.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (Modifier.isPrivate(constructor.getModifiers())) {
                constructor.setAccessible(true);
                Relay instance = (Relay) constructor.newInstance();
            }
        }
    }

    @Test
    public void verifySameInstanceIfSingletonBreakByCloning() {
        Relay instance1 = Relay.instance();
        Relay instance2 = instance1.clone();
        Assert.assertEquals(instance1, instance2);
    }

    @Test
    public void verifySameInstanceIfSingletonBreakBySerialization() throws IOException, ClassNotFoundException {
        String fileName = "file.text";
        Relay instance1 = Relay.instance();
        ObjectOutput out = new ObjectOutputStream(new FileOutputStream(fileName));
        out.writeObject(instance1);
        out.close();

        ObjectInput in = new ObjectInputStream(new FileInputStream(fileName));
        Relay instance2 = (Relay) in.readObject();

        Assert.assertEquals(instance1, instance2);
    }
}