package com.andyrobo.hello.core.lexi;

class Response {

	private String query;
	private String response;
	
	public final int MOVE;
	public final int FACE;

	public Response(String q, String r, int move, int face) {
		this.query = q;
		this.response = r;
		this.MOVE = move;
		this.FACE = face;
	}
	
	
	public String getResponse() {
		return response;
	}
	
	public String getQuery() {
		return query;
	}
}
