package com.andyrobo.hello.core.lexi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.util.Log;

import com.andyrobo.base.graphics.AndyFace;
import com.andyrobo.hello.core.bktree.BKTree;
import com.andyrobo.hello.core.bktree.LevenshteinDistance;

public class WordProcessor {

	public final static Response[] BASIC_RESPONSES = new Response[] {
			new Response("hello",
					"hey! i am great... how are you?", 0, AndyFace.LAUGH),
			new Response("hi", "hello! whassup?", 1, AndyFace.LAUGH),
			new Response("forward", "okay as you wish!", 1,
					AndyFace.SMILE),
			new Response("come", "coming!", 1, AndyFace.SMILE),
			new Response("back", "why? whats wrong?", 2, AndyFace.SCARED),
			new Response("away", "really?", 2, AndyFace.SCARED),
			new Response("reverse", "backing up...", 2, AndyFace.LAUGH),
			new Response("left", "turning left!", 3, AndyFace.SMILE),
			new Response("right", "turning right!", 4, AndyFace.SMILE),
			new Response("stupid", "OK! I dont appreciate that", 10,
					AndyFace.ANGRY),
			new Response("bad robot", "I'm so sorry!", 2, AndyFace.SCARED),
			new Response("sorry", "it's okay buddy!", 1, AndyFace.SMILE),
			new Response("stop", "OK!", 0, AndyFace.LAUGH) };

	private static final String TAG = "WordProcessor";

	private final BKTree<String> bkTree;
	private final Map<String, Integer> wordList = new HashMap<String, Integer>();

	private IWPHandler handler;
	private Response[] responses;

	public WordProcessor(IWPHandler h, Response[] responses) {
		this.handler = h;
		this.responses = responses;

		bkTree = new BKTree<String>(new LevenshteinDistance());

		for (int i = 0; i < responses.length; i++) {
			bkTree.add(responses[i].getQuery());
		}
	}

	public void processMatches(ArrayList<String> matches) {
		wordList.clear();
		Response r = null;

		for (int i = 0; i < matches.size(); i++) {
			String match = matches.get(i);
			//Log.i("Matched", match);
			r = compareToResponse(match.toLowerCase(Locale.ENGLISH).trim());
			if (r != null) {
				break;
			}
		}

		if (r != null) {
			Log.e(TAG, r.getQuery());
			handler.handleResponse(r.getResponse(), r.MOVE, r.FACE);
		} else {
			handler.handleResponse(null, 0, 0);
		}
	}

	private Response compareToResponse(String match) {
		for (int i = 0; i < responses.length; i++) {
			Response r = responses[i];
			String query = r.getQuery().toLowerCase(Locale.ENGLISH);
			if (match.contains(query)) {
				return r;
			}
		}

		return null;
	}

	// private final String getTopRank(Map<String, Integer> mp) {
	// Iterator<Entry<String, Integer>> it = mp.entrySet().iterator();
	// String top_rank = "";
	// int top = 0;
	// while (it.hasNext()) {
	// Entry<String, Integer> pairs = it.next();
	// // System.out.println(pairs.getKey() + " = " + pairs.getValue());
	// int r = pairs.getValue();
	// if (r > top) {
	// top = r;
	// top_rank = pairs.getKey();
	// }
	// }
	//
	// return top_rank;
	// }
	//
	// private void compareToResponse(String searchTerm) {
	// String bestMatch = bkTree.findBestWordMatch(searchTerm);
	// // Log.i(TAG, "Best Match for " + searchTerm + " : " + bestMatch);
	//
	// if (wordList.containsKey(bestMatch)) {
	// int f = wordList.get(bestMatch);
	// wordList.remove(bestMatch);
	// wordList.put(bestMatch, f + 1);
	// } else {
	// wordList.put(bestMatch, 1);
	// }
	// }
}
