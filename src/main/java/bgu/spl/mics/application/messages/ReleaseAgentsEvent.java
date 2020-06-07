package bgu.spl.mics.application.messages;

import bgu.spl.mics.Event;
import java.util.List;

public class ReleaseAgentsEvent implements Event {
    private List<String> agentsSerialNumbers;

    public ReleaseAgentsEvent(List<String> agentsSerialNumbers) {
        this.agentsSerialNumbers = agentsSerialNumbers;
    }

    public List<String> getAgentsSerialNumbers() {
        return agentsSerialNumbers;
    }
}
