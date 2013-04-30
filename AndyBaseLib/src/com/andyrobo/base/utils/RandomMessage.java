package com.andyrobo.base.utils;


public class RandomMessage {
	
	public static final String[] CONNECTION_MESSAGES = new String[] {
			"Ok, someone connect!",
			"Is anyone there?",
			"I am waiting....",
			"This is really boring!",
			"Am I that unlovable?"
	};

	public static String getRandomMessage(String[] messages) {
		int max = messages.length;
		int m = (int) (Math.random() * max);
		return messages[m];
	}
}
