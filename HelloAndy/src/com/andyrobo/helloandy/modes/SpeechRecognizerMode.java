package com.andyrobo.helloandy.modes;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.andyrobo.base.behaviours.BehaviourAngry;
import com.andyrobo.base.behaviours.IAndyBehaviour;
import com.andyrobo.base.comm.AndyWifiManager;
import com.andyrobo.base.graphics.AndyFace;
import com.andyrobo.base.motion.AndyMotion;
import com.andyrobo.base.utils.AndyUtils;
import com.andyrobo.helloandy.R;
import com.andyrobo.helloandy.core.lexi.IWPHandler;
import com.andyrobo.helloandy.core.lexi.WordProcessor;

public class SpeechRecognizerMode extends AbstractMode implements IWPHandler {

	public static final String TAG = "VoiceCommand";

	private TextView status;
	int set = 0;
	private final Handler h;
	private final WordProcessor wp;

	private SpeechRecognizer mSpeechRecognizer;

	private Intent mRecognizerIntent;

	private RecognitionListener mRecognitionListener = new RecognitionListener() {
		@Override
		public void onBufferReceived(byte[] buffer) {
			// TODO Auto-generated method stub
			// Log.d(TAG, "onBufferReceived");
		}

		@Override
		public void onError(int error) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onError: " + error);
			mSpeechRecognizer.startListening(mRecognizerIntent);
		}

		@Override
		public void onEvent(int eventType, Bundle params) {
			// TODO Auto-generated method stub
			// Log.d(TAG, "onEvent");
		}

		@Override
		public void onPartialResults(Bundle partialResults) {
			// TODO Auto-generated method stub
			// Log.d(TAG, "onPartialResults");
		}

		@Override
		public void onReadyForSpeech(Bundle params) {
			// TODO Auto-generated method stub
			Log.d(TAG, "onReadyForSpeech");
		}

		@Override
		public void onResults(Bundle results) {
			Log.d(TAG, "onResults");
			Toast.makeText(activity.getBaseContext(), "got voice results!",
					Toast.LENGTH_SHORT).show();

			ArrayList<String> matches = results
					.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			for (int i = 0; i < matches.size(); i++) {
				Log.i("Results: " , matches.get(i));
			}
			mSpeechRecognizer.startListening(mRecognizerIntent);
		}

		@Override
		public void onRmsChanged(float rmsdB) {
			// TODO Auto-generated method stub
			// Log.d(TAG, "onRmsChanged");
		}

		@Override
		public void onBeginningOfSpeech() {
			// TODO Auto-generated method stub
			// Log.d(TAG, "onBeginningOfSpeech");
		}

		@Override
		public void onEndOfSpeech() {
			// TODO Auto-generated method stub
			Log.d(TAG, "onEndOfSpeech");
		}

	};

	public SpeechRecognizerMode(Activity a) {
		super(a);
		h = new Handler();
		wp = new WordProcessor(this, WordProcessor.BASIC_RESPONSES);
	}

	@Override
	public String getName() {
		return "Andy Voice Command";
	}

	@Override
	public String getDescription() {
		return "Speak to Andy and watch the fun!";
	}

	@Override
	public int getImageResourceID() {
		return R.drawable.speak;
	}

	@Override
	public void start() {
		setStatus("Activating Wifi....");
		AndyWifiManager.activateWifi(false, activity);

		Log.d(TAG,
				"speech recognition available: "
						+ SpeechRecognizer.isRecognitionAvailable(activity
								.getBaseContext()));
		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity
				.getBaseContext());
		mSpeechRecognizer.setRecognitionListener(mRecognitionListener);
		mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Andy Voice Recognition");

		
		mSpeechRecognizer.startListening(mRecognizerIntent);
	}

	private void setStatus(final String message) {
		h.post(new Runnable() {

			@Override
			public void run() {
				status.setText(message);
			}
		});
	}

	@Override
	public void initView(ViewGroup rootView) {
		AndyUtils.getLayoutInflater(activity).inflate(R.layout.voice_comm,
				rootView);
		status = (TextView) activity.findViewById(R.id.tvVoiceStatus);
	}

	@Override
	public void stop() {
	}

	@Override
	public void handleResponse(String responseString, int move, int face) {
		setStatus(responseString);
		if (move < 10) {
			AndyFace.showFace(face);
			doMove(move);
		} else {
			final IAndyBehaviour b = new BehaviourAngry(10, 2500);
			b.start();
			new Thread() {
				public void run() {
					while (!b.isCompleted())
						;
				};
			}.start();
		}
	}

	private void doMove(final int move) {
		new Thread() {
			@Override
			public void run() {
				AndyMotion.move(move, 1000);
			}
		}.start();

	}

}
