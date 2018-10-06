package com.java.anmol.relay;

import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings({"unused", "UnusedAssignment"})
public class RelayTest {

    private MessageService relay;
    private static boolean flagForTesting;
    private static int totalChanges;
    private boolean isTeardownRequired = true;

    private enum Topics {
        TOPIC_ONE("TOPIC_ONE", "TOPIC_ONE_MESSAGE"),
        TOPIC_TWO("TOPIC_TWO", "TOPIC_TWO_MESSAGE");

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
}