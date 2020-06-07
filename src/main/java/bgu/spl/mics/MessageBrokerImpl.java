package bgu.spl.mics;

import bgu.spl.mics.application.messages.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The {@link MessageBrokerImpl class is the implementation of the MessageBroker interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBrokerImpl implements MessageBroker {
    private Map<Subscriber, LinkedList<Message>> subscribersMap; //contains the queue of messages for every subscriber
    private ConcurrentHashMap<Class<? extends Message>, LinkedList<Subscriber>> messagesMap; //contains the list of subscribers for every message
    private Map<Event, Future> futureMap;

    private static class MessageBrokerHolder {
        private static MessageBrokerImpl instance = new MessageBrokerImpl();
    }

    //----------------Constructor----------------
    private MessageBrokerImpl() {
        subscribersMap = new HashMap<>();
        messagesMap = new ConcurrentHashMap<>();
        futureMap = new HashMap<>();
    }

    /**
     * Retrieves the single instance of this class.
     */
    public static MessageBroker getInstance() {
        return MessageBrokerHolder.instance;
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, Subscriber m) {
        //if this type of event doesn't exist in the messagesMap - create for it new list of subscribers
        messagesMap.putIfAbsent(type, new LinkedList<>());
        synchronized (messagesMap.get(type)) {
            //add to this event's list the Subscriber m
            if (!messagesMap.get(type).contains(m))
                messagesMap.get(type).add(m);
        }
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, Subscriber m) {
        //if this type of broadcast doesn't exist in the messagesMap - create for it new list of subscribers
        messagesMap.putIfAbsent(type, new LinkedList<>());
        synchronized (messagesMap.get(type)) {
            //add to this broadcast's list the Subscriber m
            if (!messagesMap.get(type).contains(m))
                messagesMap.get(type).add(m);
        }
    }

    @Override
    public <T> void complete(Event<T> e, T result) {
        Future<T> future = futureMap.get(e);
        if (future != null)
            future.resolve(result);

//        System.out.println("FUTURE of the event " + e.toString() + "changed to " + result);
    }


    @Override
    public void sendBroadcast(Broadcast b) {

        LinkedList<Subscriber> subListBroadcast = messagesMap.get(b.getClass());
        if (subListBroadcast != null) {
            synchronized (subListBroadcast) {
                //check if there is a subscriber for the broadcast
                if (subListBroadcast != null && !subListBroadcast.isEmpty()) {
                    //adds the broadcast message to the queue of every relevant subscriber
                    for (Subscriber s : subListBroadcast) {
                        LinkedList<Message> msgQueueOfs = subscribersMap.get(s);
                        if (msgQueueOfs != null)
                            synchronized (msgQueueOfs) {
                                msgQueueOfs.add(b);

                                //notify that a new message received in the queue
                                msgQueueOfs.notifyAll();
                            }
                    }
                }
            }
        }
    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {
        LinkedList<Subscriber> subList = messagesMap.get(e.getClass());
        synchronized (subList) {
            //if there is subscriber for this event
            if (subList != null && !subList.isEmpty()) {
                Future<T> future = new Future<>();

                //----------------------------round-robin----------------------------
                //get the first from the list and add it to the end of the list
                Subscriber s = subList.removeFirst();
                subList.add(s);

                LinkedList<Message> msgQueueOfs = subscribersMap.get(s);
                if (msgQueueOfs != null)
                    synchronized (msgQueueOfs) {
                        msgQueueOfs.add(e); //add the event to s's queue
                        futureMap.put(e, future); //add to the future map

//                        System.out.println("Event: " + e.getClass() + " was sent to " + s.toString());

                        //notify that a new message received in the queue
                        msgQueueOfs.notifyAll();
                        return future;
                    }
            }
        }
        return null;
    }

    @Override
    public void register(Subscriber m) {
        subscribersMap.put(m, new LinkedList<Message>());
//        System.out.println(m.toString() + " was registered");
    }

    @Override
    public void unregister(Subscriber m) {
        //the message queue of the subscriber m
        LinkedList<Message> msgQueueOfm = subscribersMap.get(m);
        synchronized (msgQueueOfm) {
            //Delete all references from the queue
            while (!msgQueueOfm.isEmpty()) {
                Message message = (Message) msgQueueOfm.poll();
                //there is an event that still hasn't resolved
                if (message instanceof Event) {
                    if (!futureMap.get(message).isDone())
                        futureMap.get(message).resolve(null);
                }
            }
        }
        //delete the subscriber from the subscribers map
        subscribersMap.remove(m);

//        System.out.println(m.toString() + " was removed from the subscribersMap");

            //unsubscribe m from the messages
            Iterator iter = messagesMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<Message, List<Subscriber>> entry = (Map.Entry) iter.next();
                synchronized (entry.getValue()) {
//                System.out.println("a message of " + m.toString() + " was removed from the messagesMap");

                entry.getValue().remove(m);
            }
        }
    }

    @Override
    public Message awaitMessage(Subscriber m) throws InterruptedException {
        //if m is not registered - throw IllegalStateException
        if (!subscribersMap.containsKey(m))
            throw new IllegalStateException(m.toString() + " not registered");

        LinkedList<Message> msgQueueOfm = subscribersMap.get(m);
        synchronized (msgQueueOfm) {
            //while there aren't messages in m's queue - wait to receive a message
            while (msgQueueOfm.isEmpty()) {
                //wait if there are no messages in the queue
                try { msgQueueOfm.wait(); }
                catch (InterruptedException e) { throw e; }
            }
        }
        Message msg = msgQueueOfm.poll();
//        System.out.println(m.toString() + " took a new message: " + msg.toString());
        return msg;
    }
}