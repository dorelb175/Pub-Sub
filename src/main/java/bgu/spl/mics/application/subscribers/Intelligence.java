package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Future;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.MissionReceivedEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.MissionInfo;

import java.util.List;

/**
 * A Publisher\Subscriber.
 * Holds a list of Info objects and sends them
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Intelligence extends Subscriber {
	private List<MissionInfo> missions;
	private int currentTimeTick;

	//----------------Constructor----------------
	public Intelligence(String name, List<MissionInfo> missions) {
		super(name);
		this.missions = missions;
		currentTimeTick = -1;
	}

	@Override
	protected void initialize() {
		//subscribe for TickBroadcast
		this.subscribeBroadcast(TickBroadcast.class, (TickBroadcast e) -> {
			currentTimeTick = e.getTick();
			//Searching for missions that need to be published in this current time tick
			for(MissionInfo mission: missions) {
				if (mission.getTimeIssued() == currentTimeTick) {
					//create a new MissionReceivedEvent for the mission we found
					MissionReceivedEvent missionEvent = new MissionReceivedEvent(mission);
					Future<Boolean> future = this.getSimplePublisher().sendEvent(missionEvent);
				}
			}
		});

		//subscribe for TerminateBroadcast
		this.subscribeBroadcast(TerminateBroadcast.class, (TerminateBroadcast e) -> {
			terminate();
		});
	}

	@Override
	public String toString() {
		return "Intelligence";
	}
}