package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import java.util.List;

public class AgentsAvailableEvent implements Event<Integer> {
    private List<String> agentsSerialNumbers;

    public AgentsAvailableEvent(List<String> agentsSerialNumbers) {
        this.agentsSerialNumbers = agentsSerialNumbers;
    }

    public List<String> getAgentsSerialNumbers() {
        return agentsSerialNumbers;
    }
}
