package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import java.util.List;

public class SendAgentsEvent implements Event<List<String>> {
    private List<String> agentsSerialNumbers;
    private int time;

    public SendAgentsEvent(List<String> agentsSerialNumbers, int time) {
        this.agentsSerialNumbers = agentsSerialNumbers;
        this.time = time;
    }

    public int getTime() {
        return time;
    }

    public List<String> getAgentsSerialNumbers() {
        return agentsSerialNumbers;
    }
}
