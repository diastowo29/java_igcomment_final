package com.example;

import java.util.Timer;
import java.util.TimerTask;

public class TimeringTicket {
	Timer timer;

	public TimeringTicket(String itemid) {
		timer = new Timer();
		timer.schedule(new DoIt(itemid), 6000);
	}

	class DoIt extends TimerTask {
		String item;

		public DoIt(String itemid) {
			item = itemid;
		}

		@Override
		public void run() {
			System.out.println("Hey its timer: " + item);
			timer.cancel();
		}
	}

}
