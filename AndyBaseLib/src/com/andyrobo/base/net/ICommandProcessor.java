package com.andyrobo.base.net;

public interface ICommandProcessor {

	public static final byte CMD_STOP = 0x00;
	public static final byte CMD_FORWARD = 0x01;
	public static final byte CMD_REVERSE = 0x02;
	public static final byte CMD_LEFT = 0x03;
	public static final byte CMD_RIGHT = 0x04;

	public static final byte CMD_FORWARD_LEFT = 0x06;
	public static final byte CMD_FORWARD_RIGHT = 0x07;
	public static final byte CMD_REVERSE_LEFT = 0x08;
	public static final byte CMD_REVERSE_RIGHT = 0x09;

	public void processCommand(byte[] data);
	public void init();

}
