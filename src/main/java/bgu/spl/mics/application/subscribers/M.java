package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Future;
import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Report;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * M handles ReadyEvent - fills a report and sends agents to mission.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class M extends Subscriber {
	private final int serialNumber;
	private Diary diary;
	private int currentTimeTick;

	//----------------Constructor----------------
	public M(String name, int serialNumber) {
		super(name);
		this.serialNumber = serialNumber;
		diary = Diary.getInstance();
		currentTimeTick = -1;
	}

	@Override
	protected void initialize() {
		//Subscribe for TickBroadcast
		this.subscribeBroadcast(TickBroadcast.class, (TickBroadcast e) -> {
			currentTimeTick = e.getTick();
		});

		//Subscribe for MissionReceivedEvent
		this.subscribeEvent(MissionReceivedEvent.class, (MissionReceivedEvent mission) -> {
			diary.incrementTotal();

			//check the availability of the necessary agents for the mission
			AgentsAvailableEvent agentEvent = new AgentsAvailableEvent(mission.getMissionInfo().getSerialAgentsNumbers());
			Future<Integer> agentsFuture = this.getSimplePublisher().sendEvent(agentEvent);

			if (agentsFuture != null) {
				//Wait for the future while it's still relevant - wait for: (TimeExpired - currentTick)
				Integer resultAgents = agentsFuture.get((mission.getMissionInfo().getTimeExpired() - currentTimeTick), TimeUnit.MILLISECONDS);

				if (resultAgents != null && resultAgents != -1) {

					//check the availability of the necessary gadget for the mission
					GadgetAvailableEvent gadgetEvent = new GadgetAvailableEvent(mission.getMissionInfo().getGadget());
					Future<Integer> gadgetFuture = this.getSimplePublisher().sendEvent(gadgetEvent);

					if (gadgetFuture != null) {
						//Wait for the future while it's still relevant - wait for: (TimeExpired - currentTick)
						Integer resultGadget = gadgetFuture.get(mission.getMissionInfo().getTimeExpired() - currentTimeTick, TimeUnit.MILLISECONDS);

						if (resultGadget != null && resultGadget != -1) {
							if (currentTimeTick <= mission.getMissionInfo().getTimeExpired()) {
								//Send the agents to the mission
								SendAgentsEvent sendAgentsEvent = new SendAgentsEvent(mission.getMissionInfo().getSerialAgentsNumbers(), mission.getMissionInfo().getDuration());
								Future<List<String>> sendAgentsFuture = this.getSimplePublisher().sendEvent(sendAgentsEvent);

								//Create and fill the report
								Report report = fillReport(mission, resultAgents, resultGadget, sendAgentsFuture);

								//add the report to the diary
								diary.addReport(report);
								//Resolve the future of the MissionReceivedEvent to True = mission completed
								this.complete(mission, true);
							}
							else
								abortMission(mission);
						}
						else
							abortMission(mission);
					}
					else
						abortMission(mission);
				}
				else
					abortMission(mission);
			}
			else
				abortMission(mission);
		});

		//subscribe for TerminateBroadcast
		this.subscribeBroadcast(TerminateBroadcast.class, (TerminateBroadcast e) -> {
			terminate();
		});
	}

	private Report fillReport(MissionReceivedEvent mission, int resultAgents, int resultGadget, Future<List<String>> sendAgentsFuture) {
		Report report = new Report();
		report.setMissionName(mission.getMissionInfo().getMissionName());
		report.setM(serialNumber);
		report.setMoneypenny(resultAgents);
		report.setAgentsSerialNumbers(mission.getMissionInfo().getSerialAgentsNumbers());
		report.setAgentsNames(sendAgentsFuture.get());
		report.setGadgetName(mission.getMissionInfo().getGadget());
		report.setTimeIssued(mission.getMissionInfo().getTimeIssued());
		report.setQTime(resultGadget);
		report.setTimeCreated(currentTimeTick);
		return report;
	}

	private void abortMission(MissionReceivedEvent mission) {
		//Release the agents - abort the mission
		ReleaseAgentsEvent releaseAgentsEvent = new ReleaseAgentsEvent(mission.getMissionInfo().getSerialAgentsNumbers());
		this.getSimplePublisher().sendEvent(releaseAgentsEvent);
		//Resolve the future of the MissionReceivedEvent to False = mission aborted
		this.complete(mission, false);
	}

	@Override
	public String toString() {
		return "M " + serialNumber;
	}
}