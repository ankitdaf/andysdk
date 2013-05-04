package com.andyrobo.base.behaviours;

import com.andyrobo.base.graphics.AndyFace;
import com.andyrobo.base.motion.AndyMotion;

public class BehaviourAngry implements IAndyBehaviour {

	private int rounds;
	private long moveTime;
	private boolean isCompleted;

	public BehaviourAngry(int rounds, long time) {
		this.rounds = rounds;
		this.moveTime = (long) (time / (2 * rounds));
	}

	@Override
	public void start() {
		AndyFace.angry();
		new Thread() {
			public void run() {
				isCompleted = false;
				for (int i = 0; i < rounds; i++) {
					AndyMotion.setRawSpeeds(255, 255, moveTime);
					AndyMotion.setRawSpeeds(-255, -255, moveTime);
				}
				isCompleted = true;
			}
		}.start();
	}

	@Override
	public void stop() {
		AndyMotion.setRawSpeeds(0, 0);
	}
	
	@Override
	public boolean isCompleted() {
		return isCompleted;
	}

}
