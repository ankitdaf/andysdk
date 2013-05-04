package com.andyrobo.helloandy.modes;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.InputType;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.andyrobo.base.AndyActivity;
import com.andyrobo.base.comm.ICommandProcessor;
import com.andyrobo.base.graphics.AndyFace;
import com.andyrobo.base.utils.AndyUtils;

@SuppressLint("DefaultLocale")
public class AndyVoiceCommand extends AndyActivity {

	private static final String[] voiceCommands = new String[] { "forward",
			"back", "reverse", "left", "right", "smile", "stupid", "away",
			"lost", "exit" };

	private static final SparseArray<Byte> motorCommands = new SparseArray<Byte>();

	// setting up the motor commands based on the array index of voice commands
	static {
		motorCommands.append(0, VoiceCommandProcessor.CMD_FORWARD);
		motorCommands.append(1, VoiceCommandProcessor.CMD_REVERSE);
		motorCommands.append(2, VoiceCommandProcessor.CMD_REVERSE);
		motorCommands.append(3, VoiceCommandProcessor.CMD_LEFT);
		motorCommands.append(4, VoiceCommandProcessor.CMD_RIGHT);

		// now give expressions
		motorCommands.append(5, AndyFace.SMILE); // smile
		motorCommands.append(6, AndyFace.ANGRY); // stupid
		motorCommands.append(6, AndyFace.SCARED); // away
		motorCommands.append(7, AndyFace.SCARED); // lost
		motorCommands.append(8, AndyFace.ANGRY);
		motorCommands.append(9, AndyFace.SCARED);
	}

	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

	private static final byte MOTION_TIME = (byte) 1000;
	private TextView commandText;
	private ImageButton commandButton;

	@Override
	protected void setContentView() {
		setContentView(R.layout.layout_voicecommand);

		commandText = (TextView) findViewById(R.id.messageView);
		commandButton = (ImageButton) findViewById(R.id.logo);

		commandButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				startVoiceRecognitionActivity();
			}
		});

		// Check to see if a recognition activity is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() != 0) {
			commandText.setText("Your command is my wish ;)");
		} else {
			commandText.setText("Not is a mood to follow your commands :(");
			commandText.setEnabled(false);
		}

		if (getSavedIP() == null) {
			openDialog();
		}
		startTimedCommandSend();
	}

	/**
	 * Fire an intent to start the speech recognition activity.
	 */
	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Andy Voice Recognition");

		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
	}

	/**
	 * Handle the results from the recognition activity.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
				&& resultCode == RESULT_OK) {

			ArrayList<String> matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

			int commandIndex = getClosestMatch(matches);
			// showMessage(commandIndex + "");
			processVoiceCommand(commandIndex);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private int getClosestMatch(ArrayList<String> matches) {
		for (int i = 0; i < matches.size(); i++) {
			String testCommand = matches.get(i).toLowerCase();

			for (int j = 0; j < voiceCommands.length; j++) {
				String andyCommand = voiceCommands[j].toLowerCase();
				if (testCommand.contains(andyCommand)) {
					return j;
				}
			}
		}
		return -1;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		killApp();
	}

	@Override
	protected void onStop() {
		super.onStop();
		killApp();
	}

	private void killApp() {
		// theCommand = IAndyMoveCommands.CMD_STOP;
		if (timer != null)
			timer.cancel();
		finish();
	}

	private void processVoiceCommand(int commandIndex) {
		if (commandIndex >= 0) {
			String theCommand = voiceCommands[commandIndex];
			showMessage(theCommand);

			if (theCommand.equalsIgnoreCase("exit")) {
				// exit out
				setResult(RESULT_OK);
				finishActivity(VOICE_RECOGNITION_REQUEST_CODE);
				finish();
			}

			// conduct the actual process here
			this.theCommand = motorCommands.get(commandIndex);

		} else {
			// ANDY CONFUSED!
			showMessage("Huh? Say Again?");
			this.theCommand = AndyFace.CONFUSED;
			this.theCommand = ICommandProcessor.CMD_STOP;
		}
	}

	private static final int SERVER_PORT = 9090;
	private InetAddress IPAddress;
	private DatagramSocket clientSocket;
	private Timer timer;

	private byte theCommand = ICommandProcessor.CMD_STOP;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.activity_andy_voice_command, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.setIP:
			openDialog();
			break;

		case R.id.showLocalIP:
			Toast.makeText(getApplicationContext(),
					AndyUtils.getWifiAddress(this), Toast.LENGTH_LONG).show();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private static final String robotIPKey = "com.andy.remote.robotIP";

	private String getSavedIP() {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		return prefs.getString(robotIPKey, null);
	}

	private void saveIP(String toSave) {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor e = prefs.edit();
		e.putString(robotIPKey, toSave);
		e.commit();
	}

	private void openDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Set Andy IP");
		alert.setMessage("Set the Andy Robot IP Address here");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		input.setEms(10);
		String ip = getSavedIP();
		if (ip == null) {
			ip = AndyUtils.getWifiAddress(this);
		}
		input.setText(ip);

		input.setInputType(InputType.TYPE_CLASS_NUMBER
				| InputType.TYPE_NUMBER_FLAG_DECIMAL);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Editable value = input.getText();
				setIP(value.toString());
			}
		});

		alert.setNeutralButton("Reset IP",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface i, int arg1) {
						String wifi = AndyUtils
								.getWifiAddress(AndyVoiceCommand.this);
						input.setText(wifi);
						setIP(wifi);
					}
				});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}

	protected void setIP(String ipAddress) {
		String message = "IP Address Error!";
		try {
			IPAddress = InetAddress.getByName(ipAddress);
			message = "IP address changed to " + ipAddress;
			saveIP(ipAddress);
		} catch (Exception e) {
		}

		Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG)
				.show();
	}

	private void startTimedCommandSend() {
		try {
			IPAddress = InetAddress.getByName(getSavedIP());
			clientSocket = new DatagramSocket();
			sendCommand(theCommand);

			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {

				private int i = 0;

				@Override
				public void run() {
					sendCommand(theCommand);
					if (i > 30) {
						i = 0;
						theCommand = ICommandProcessor.CMD_STOP;
					}
					i++;
				}
			}, 0, 100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void sleepAndSend(byte cmdStop) {
		try {
			Thread.sleep(MOTION_TIME);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void sendCommand(byte command) {
		byte[] commandArr = new byte[] { command };
		try {
			DatagramPacket sendPacket = new DatagramPacket(commandArr,
					commandArr.length, IPAddress, SERVER_PORT);
			clientSocket.send(sendPacket);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showMessage(final String command) {
		this.runOnUiThread(new Runnable() {

			public void run() {
				// Toast.makeText(getApplicationContext(), command,
				// Toast.LENGTH_LONG).show();
				commandText.setText(command.toUpperCase());
			}
		});
	}
}
