package com.andyrobo.base.behaviours;

import com.andyrobo.base.graphics.AndyFace;
import com.andyrobo.base.motion.AndyMotion;

public class BehaviourScared implements IAndyBehaviour {

	private long moveTime;
	private boolean isCompleted;

	public BehaviourScared(long time) {
		this.moveTime = time;
	}

	@Override
	public void start() {
		AndyFace.scared();
		new Thread() {

			public void run() {
				isCompleted = false;
				AndyMotion.setRawSpeeds(-255, -255, moveTime);
				AndyFace.confused();
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
