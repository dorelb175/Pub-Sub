package bgu.spl.mics.application.subscribers;

import bgu.spl.mics.Subscriber;
import bgu.spl.mics.application.messages.GadgetAvailableEvent;
import bgu.spl.mics.application.messages.TerminateBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.passiveObjects.Inventory;

/**
 * Q is the only Subscriber\Publisher that has access to the {@link bgu.spl.mics.application.passiveObjects.Inventory}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class Q extends Subscriber {
	private Inventory inventory;
	private int currentTimeTick;

	//----------------Constructor----------------
	public Q(String name, Inventory inventory) {
		super(name);
		this.inventory = inventory;
		currentTimeTick = -1;
	}

	@Override
	protected void initialize() {
		//Subscribe for TickBroadcast
		this.subscribeBroadcast(TickBroadcast.class, (TickBroadcast e) -> {
			currentTimeTick = e.getTick();
		});

		//Subscribe for GadgetAvailableEvent
		this.subscribeEvent(GadgetAvailableEvent.class, (GadgetAvailableEvent e) -> {
			//Takes the gadget for the mission and remove it from the inventory. False - if the gadget doesn't exists in the inventory
			int result = -1;
			if(inventory.getItem(e.getGadget()))
				result = currentTimeTick;

			this.complete(e, result);
		});

		//subscribe for TerminateBroadcast
		this.subscribeBroadcast(TerminateBroadcast.class, (TerminateBroadcast e) -> {
			terminate();
		});
	}

	@Override
	public String toString() {
		return "Q";
	}
}