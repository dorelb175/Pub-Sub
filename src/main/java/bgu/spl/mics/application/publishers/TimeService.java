package bgu.spl.mics.application.publishers;

import bgu.spl.mics.Publisher;

import bgu.spl.mics.application.messages.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * TimeService is the global system timer There is only one instance of this Publisher.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other subscribers about the current time tick using {@link Tick Broadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends Publisher {
	private final int duration;
	private int tick;
	private Timer timer;

	//----------------Constructor----------------
	public TimeService(String name, int duration) {
		super(name);
		this.duration = duration;
		tick = 1;
		timer = new Timer();
	}

	@Override
	protected void initialize() {

	}

	@Override
	public void run() {
		TimerTask task = new TimerTask() {
			int count = tick;

			@Override
			public void run() {
				//Create a new TickBroadcast
				TickBroadcast broadcast = new TickBroadcast(count);
				getSimplePublisher().sendBroadcast(broadcast);

				//if the current tick is bigger than the total duration of the program - cancel the timer
				if (count >= duration) {
					timer.cancel();

					getSimplePublisher().sendBroadcast(new TerminateBroadcast());
				}
				count++;

			}
		};

		//Send a new TickBroadcast every 100 ms
		timer.scheduleAtFixedRate(task, 0, 100);
	}
}