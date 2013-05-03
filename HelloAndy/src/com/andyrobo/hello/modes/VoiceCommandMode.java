package com.andyrobo.hello.modes;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andyrobo.base.behaviours.BehaviourAngry;
import com.andyrobo.base.behaviours.IAndyBehaviour;
import com.andyrobo.base.graphics.AndyFace;
import com.andyrobo.base.motion.AndyMotion;
import com.andyrobo.base.net.AndyWifiManager;
import com.andyrobo.base.utils.AndyUtils;
import com.andyrobo.hello.R;
import com.andyrobo.hello.core.lexi.IWPHandler;
import com.andyrobo.hello.core.lexi.WordProcessor;

public class VoiceCommandMode extends AbstractMode implements IWPHandler {

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

	public static final String TAG = "VoiceCommand";

	private TextView status;
	int set = 0;
	private final Handler h;
	private final WordProcessor wp;

	public VoiceCommandMode(Activity a) {
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
		this.ready = true;
		if (initVoiceRecognizer()) {
			startTimedVoiceReceiver();
		}
	}

	private boolean ready = true;

	private Timer tvr;

	private Intent intent;

	// TODO: Check for internet connectivity and not wifi connectivity
	private void startTimedVoiceReceiver() {
		tvr = new Timer();
		tvr.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				if (ready && AndyWifiManager.isActive(activity)) {
					setStatus("Okay! Say something");
					startVoiceRecognitionActivity();
					ready = false;
				}
			}
		}, 0, 3000);
	}

	private void setStatus(final String message) {
		h.post(new Runnable() {

			@Override
			public void run() {
				status.setText(message);
			}
		});
	}

	private boolean initVoiceRecognizer() {
		PackageManager pm = activity.getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			AndyFace.laugh();
			// setStatus("Your command is my wish ;)");
			return true;
		} else {
			AndyFace.angry();
			setStatus("Not in a mood to follow your commands :(");
			status.setEnabled(false);
		}
		return false;
	}

	private void startVoiceRecognitionActivity() {
		intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Andy Voice Recognition");

		activity.startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	@Override
	public void handleActivityResult(int requestCode, int resultCode,
			Intent data) {

		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {

				ArrayList<String> matches = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				wp.processMatches(matches);
			}

			if (resultCode == Activity.RESULT_CANCELED) {
				stop();
				setStatus("You hung up on me?");
				AndyFace.confused();
			}

			if (resultCode == RecognizerIntent.RESULT_AUDIO_ERROR
					|| resultCode == RecognizerIntent.RESULT_CLIENT_ERROR
					|| resultCode == RecognizerIntent.RESULT_NETWORK_ERROR
					|| resultCode == RecognizerIntent.RESULT_NO_MATCH) {

				// try again
				Log.e(TAG, "Error with RecognizerIntent " + resultCode);
			}
		}
	}

	@Override
	public void initView(ViewGroup rootView) {
		AndyUtils.getLayoutInflater(activity).inflate(R.layout.voice_comm,
				rootView);
		status = (TextView) activity.findViewById(R.id.tvVoiceStatus);
	}

	@Override
	public void stop() {
		if (tvr != null) {
			tvr.cancel();
		}
	}

	@Override
	public void handleResponse(String responseString, int move, int face) {
		if (responseString == null) {
			AndyFace.confused();
			setStatus("Huh ?");
			ready = true;
			return;
		}
		
		setStatus(responseString);
		if (move < 10) {
			AndyFace.showFace(face);
			doMove(move);
		} else {
			ready = false;
			final IAndyBehaviour b = new BehaviourAngry(10, 2500);
			b.start();
			new Thread() {
				public void run() {
					while (!b.isCompleted())
						;
					ready = true;
				};
			}.start();
		}
	}

	private void doMove(final int move) {
		new Thread() {
			@Override
			public void run() {
				ready = false;
				AndyMotion.move(move, 1000);
				ready = true;
			}
		}.start();

	}

}
