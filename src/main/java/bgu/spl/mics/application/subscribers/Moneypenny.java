package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.*;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.Agent;
import bgu.spl.mics.application.passiveObjects.Squad;

import java.util.Iterator;
import java.util.Map;

/**
 * Only this type of Subscriber can access the squad.
 * Three are several Moneypenny-instances - each of them holds a unique serial number that will later be printed on the report.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Moneypenny extends Subscriber {
	private final int serialNumber;
	private Squad squad;

	//----------------Constructor----------------
	public Moneypenny(String name, int serialNumber) {
		super(name);
		this.serialNumber = serialNumber;
		squad = Squad.getInstance();
	}

	@Override
	protected void initialize() {
		//odd numbered MP will deal with AgentsAvailableEvents
		if (serialNumber % 2 == 0) {
			this.subscribeEvent(AgentsAvailableEvent.class, (AgentsAvailableEvent e) -> {
				//Acquires the agents for the mission
				int result = -1;
				if (squad.getAgents(e.getAgentsSerialNumbers())) {
					result = serialNumber;
				}
				this.complete(e, result);
			});
		}
		//Even numbered MP will deal with SendAgentsEvents
		else {
			this.subscribeEvent(SendAgentsEvent.class, (SendAgentsEvent e) -> {
				squad.sendAgents(e.getAgentsSerialNumbers(), e.getTime());
				this.complete(e, squad.getAgentsNames(e.getAgentsSerialNumbers()));
			});
		}

		//Every monneypenny Subscribes for ReleaseAgentsEvent
		this.subscribeEvent(ReleaseAgentsEvent.class, (ReleaseAgentsEvent e) -> {
			squad.releaseAgents(e.getAgentsSerialNumbers());
		});

		//Every monneypenny subscribes for TerminateBroadcast
		this.subscribeBroadcast(TerminateBroadcast.class, (TerminateBroadcast e) -> {
			Map<String, Agent> agents = squad.getAgentsMap();
			//Release all agents
			Iterator iter = agents.entrySet().iterator();
			while (iter.hasNext()){
				Map.Entry<String, Agent> entry = (Map.Entry) iter.next();
				Agent agent = entry.getValue();
				synchronized (agent) {
					agent.release();
					agent.notifyAll();
				}
			}

			terminate();
		});
	}

	@Override
	public String toString() {
		return "Moneypenny" + serialNumber;
	}
}