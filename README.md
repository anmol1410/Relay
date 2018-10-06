# Relay
Messaging service to send and receive any type of messages across the application.

Using it:
To use Relay, you need to start the service, which is the one time operation.

Initialize and then start it by: 

    private MessageService relay = Relay.instance();
    relay.start();

And the service is now in running state.

You can send message at say any topic "x" and the receiver which is suubscribed to that topic will receive it.

It does not matter when was the receiver initialized or when was the message sent. 

Messages are stored in a queue, and will be sent to the receiver whenever the receiver is subscribed to that topic.

Lets see this with an example:

Send any message from anywhere in application, say to topic "Anmol" by:

    relay.send("Anmol", "message"); 
And thats all.

Now the receiver can subscribe to this topic, and can receive the message anywhere by:

    Relay.instance().register("Anmol", new Receiver());

Here the second parameter expects the receiver. 
Note: the receiver must implement the Callback interface, and the messages will be sent in its onReceive() method.

E.g. : 

    class Receiver implements Callback {

        @Override
        public void onReceive(Object message) {
            System.out.println(message);
        }
    }

And thats all.
