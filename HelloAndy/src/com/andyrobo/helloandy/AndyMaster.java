package com.andyrobo.helloandy;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.andyrobo.base.AndyActivity;
import com.andyrobo.base.graphics.AndyFace;
import com.andyrobo.base.net.AndyWifiManager;
import com.andyrobo.base.utils.AndyUtils;
import com.andyrobo.helloandy.core.SwipeListener;
import com.andyrobo.helloandy.modes.AbstractMode;
import com.andyrobo.helloandy.modes.DemoMode;
import com.andyrobo.helloandy.modes.IMode;
import com.andyrobo.helloandy.modes.ModeAdapter;
import com.andyrobo.helloandy.modes.RemoteCarMode;
import com.andyrobo.helloandy.modes.SpyBotMode;
import com.andyrobo.helloandy.modes.VoiceCommandMode;

public class AndyMaster extends AndyActivity {

	private final List<IMode> andyModes = new ArrayList<IMode>();
	boolean firstRun = true;

	private FrameLayout rootLayout;
	private View firstHintLayout;
	private ListView lvModes;
	private ImageView face;
	private IMode activeMode;

	@Override
	protected View createContentView() {
		// TODO: lock screen orientation
		rootLayout = AndyFace.init(this, true);
		face = AndyFace.getFaceLayer();
		return rootLayout;
	}

	private GestureDetector gestureDetector;

	private void activateListeners() {
		SwipeListener glh = new SwipeListener() {
			@Override
			protected void handleSwipeDown() {
				showAndyModes();
			}
		};
		gestureDetector = new GestureDetector(glh);
	}

	@Override
	public void onBackPressed() {
		showAndyModes();
	}

	private void showAndyModes() {
		if (firstHintLayout != null) {
			rootLayout.removeView(firstHintLayout);
		}

		// remove just to be sure!
		rootLayout.removeView(lvModes);
		rootLayout.addView(lvModes);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector != null) {
			return gestureDetector.onTouchEvent(event);
		}
		return false;
	}

	@Override
	protected void initViews() {
		activateListeners();

		if (firstRun) {
			firstHintLayout = AndyUtils.getLayoutInflater(this).inflate(
					R.layout.first_hint, null);
			rootLayout.addView(firstHintLayout);

			/*
			 * TODO: Typeface native error! TextView tvFirstHint = (TextView)
			 * findViewById(R.id.tvFirstHint); Typeface tf =
			 * Typeface.createFromAsset(this.getAssets(),
			 * "fonts/BubblegumsSans-Regular.otf"); tvFirstHint.setTypeface(tf);
			 */

			TextView hintText = (TextView) findViewById(R.id.tvFirstHint);
			hintText.setTextColor(Color.BLUE);
		}

		if (lvModes == null) {
			lvModes = createModes();
		}

		AndyWifiManager.activateWifi(false, this);
	}

	// TODO: Dynamically add mode classes here (plugin architecture)
	@Override
	protected void initBackend() {
		andyModes.clear();
		andyModes.add(new DemoMode(this));
		andyModes.add(new RemoteCarMode(this));
		andyModes.add(new SpyBotMode(this));
		andyModes.add(new VoiceCommandMode(this));
		andyModes.add(new AbstractMode(this) {

			@Override
			public void start() {
				AndyMaster.this.exit();
			}

			@Override
			public String getName() {
				return "Exit";
			}

			@Override
			public int getImageResourceID() {
				return R.drawable.exit;
			}

			@Override
			public String getDescription() {
				return "Exit Application";
			}

			@Override
			public void initView(ViewGroup rootView) {
			}

			@Override
			public void stop() {
			}
		});
	}

	private ListView createModes() {
		ListView v = new ListView(getApplicationContext());
		v.setBackgroundResource(R.drawable.back);

		v.setAdapter(new ModeAdapter(this, andyModes));

		v.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> av, View v, int position,
					long id) {
				if (position >= andyModes.size()) {
					exit();
				} else {
					IMode m = andyModes.get(position);
					if (m != null) {
						runMode(m);
					} else {
						// Error!
					}
				}
			}
		});
		return v;
	}

	protected void runMode(IMode m) {
		// before running the new mode
		// if (m != activeMode) {
		stopActiveMode();

		this.activeMode = m;
		rootLayout.removeAllViews();
		rootLayout.addView(face);

		m.initView(rootLayout);
		m.start();
		// }
	}

	private void stopActiveMode() {
		if (activeMode != null) {
			activeMode.stop();
		}
	}

	protected void exit() {
		AndyWifiManager.rollBackWifiState(this);
		setKillOnDestroy(true);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (activeMode != null) {
			activeMode.handleActivityResult(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	public static String getName() {
		return "Rubik";
	}
}
