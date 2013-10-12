package com.nbs.client.assassins.views;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.rest.RestService;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.models.App;
import com.nbs.client.assassins.models.Match;
import com.nbs.client.assassins.models.Player;
import com.nbs.client.assassins.models.PlayerModel;
import com.nbs.client.assassins.models.Repository;
import com.nbs.client.assassins.models.User;
import com.nbs.client.assassins.network.AttackResponse;
import com.nbs.client.assassins.network.HuntedRestClient;
import com.nbs.client.assassins.network.UpdateLocationRequest;
import com.nbs.client.assassins.sensors.BearingProvider;
import com.nbs.client.assassins.sensors.BearingReceiver;

@EFragment(R.layout.hud)
public class HUDFragment extends SherlockFragment implements BearingReceiver {

	private static final String TAG = "HUDFragment";

	private CountDownTimer escapeTimer;
	
	@RestService
	HuntedRestClient restClient;
	
	@ViewById(R.id.hud_tbearing)
	TextView tBearingView;
	
	@ViewById(R.id.hud_target_life)
	ProgressBar tLifeView;
	
	@ViewById(R.id.hud_my_life)
	ProgressBar lifeView;
	
	private BearingProvider bearingProvider;

	@ViewById(R.id.hud_attack)
	TextView attackButton;
	
	@ViewById(R.id.hud_escape_time_remaining)
	TextView escapeTimeText;
	
	@ViewById(R.id.hud_trange)
	TextView targetRange;
	
	@ViewById(R.id.hud_erange)
	TextView enemyRange;

	public HUDFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);	
		Log.d(TAG, "onViewCreate()");
	}
	
	@AfterViews
	public void refreshHUDData()
	{
		Log.d(TAG, "refreshHUDData()");
		Repository model = ((App)(getActivity().getApplication())).getRepo();
		Player player  = model.getMyFocusedPlayer();
		String tRange  = player.targetRange;
		String eRange  = player.enemyRange;
		Integer myLife = player.health;
		Integer tLife  = player.targetHealth;

		targetRange.setText((tRange != null ? getRangeString(tRange) : "UNKNOWN"));
		enemyRange.setText((eRange != null ? getRangeString(eRange) : "UNKNOWN"));
		lifeView.setProgress((myLife != null ? myLife : 0));
		tLifeView.setProgress((tLife != null ? tLife : 0));
		
		initAttackState(tRange);
	}

	@Override
	public void onPause() {
		stopSensorUpdates();
		if(escapeTimer != null) escapeTimer.cancel();
		super.onPause();
	}

	@Override
	public void onResume() {
		registerForSensorUpdates();
		refreshHUDData();
		super.onResume();
	}
	
	private void initAttackState(String targetRange) {
		Log.d(TAG, "initAttackState("+targetRange+")");
		long escapeTimeRemaining = getRemainingEscapeTime();
		if(targetRange != null && escapeTimeRemaining == 0) {
			setAttackEnabled(targetRange.equals(PlayerModel.ATTACK_RANGE));
		} else {
			setAttackEnabled(false);
			initEscapeTimer(escapeTimeRemaining);
		}
	}
	
	public long getRemainingEscapeTime() {
		Repository model = ((App)(getActivity().getApplication())).getRepo();
		Match match = model.getFocusedMatch();
		Player player = model.getMyFocusedPlayer();
		if(match.escapeTime == null || player.lastAttackTime == null) return 0;
		else return (long)Math.floor(((player.lastAttackTime/1000) + match.escapeTime) - (System.currentTimeMillis()/1000));
	}

	private void initEscapeTimer(long seconds) {
		if(escapeTimer != null) escapeTimer.cancel();
		
		if(seconds > 0) {
			Log.d(TAG, "starting escape time countdown: "+ seconds + "s");
			escapeTimer = new CountDownTimer(seconds*1000, 1000/*tick time in millis*/) {
			     public void onTick(long millisUntilFinished) {
			    	 onEscapeTimeChanged(millisUntilFinished); 
			     }

			     public void onFinish() {
			    	 onEscapeTimeChanged(0);
			     }
			}.start();
		}	
	}

	private void stopSensorUpdates() {
		//if(bearingProvider != null) bearingProvider.unregisterForBearingUpdates(this);
	}

	private void registerForSensorUpdates() {
		//if(bearingProvider != null) bearingProvider.registerForBearingUpdates(this);
	}
	
	@Override
	public void setBearingProvider(BearingProvider provider) {
		bearingProvider = provider;	
	}
	
	private void setAttackEnabled(boolean enabled) {
		attackButton.setEnabled(enabled);
		if(enabled) {
			attackButton.setTextColor(getResources().getColor(R.color.white));
		} else {
			attackButton.setTextColor(getResources().getColor(R.color.DarkGray));
		}
	}

	@Override
	public void onBearingChanged(float bearing) {
		Log.d(TAG, Float.toString(bearing));
	}
	
	public void onTargetBearingChanged(float tBearing) {
		Log.d(TAG, "onTargetBearingChanged("+tBearing+")");
		tBearingView.setText(Float.toString(tBearing));
	}
	
	public void onMyLifeChanged(int life) {
		lifeView.setProgress(life);
	}
	
	public void onTargetLifeChanged(int tLife) {
		tLifeView.setProgress(tLife);
	}
	
	public void onTargetRangeChanged(String tRange) {
		targetRange.setText(getRangeString(tRange));
		initAttackState(tRange);
	}
	
	public void onEnemyRangeChanged(String eRange) {
		enemyRange.setText(getRangeString(eRange));
	}
	
	private static String getRangeString(String range) {
		if(range.equals(PlayerModel.ATTACK_RANGE))
			return "ATTACK";
		else if (range.equals(PlayerModel.HUNT_RANGE))
			return "HUNT";
		else {
			return range;
		}
	}
	
	private void onEscapeTimeChanged(long escapeTimeRemaining) {
		Log.d(TAG, "onEscapeTimeChanged("+escapeTimeRemaining+")");
		if(escapeTimeRemaining > 1000) {
			escapeTimeText.setText(escapeTimeRemaining/1000+" s");
		} else {
			escapeTimeText.setText("");
			setAttackEnabled(true);
		}
	}
	
	@Click(R.id.hud_attack)
	public void onAttackClicked() {
		setAttackEnabled(false);
		attack();
	}
	
	@Background
	public void attack() {
		Log.d(TAG, "attack()");
		
		Context c  = getSherlockActivity();
		AttackResponse response = null;
		Repository model = ((App)(getActivity().getApplication())).getRepo();
		User user = model.getUser();
		Player player = model.getMyFocusedPlayer();
		
		try {
			response = restClient.attack(user.getFocusedMatch(), user.getToken(),
				new UpdateLocationRequest(user.getLocation(),
						user.getInstallId()));
			
			if(response != null && response.hit) {
				player.setLastSuccessfulAttackTime(System.currentTimeMillis());
			}
			
		}
		catch(Exception e) {
			Log.d(TAG, e.getMessage());
		}
		
		attackFinished(response);
	}
	
	@UiThread
	public void attackFinished(AttackResponse response)
	{
		Log.d(TAG, "attackFinished(" + response + ")");
		
		if(response != null && response.ok()) {
			Toast.makeText(getSherlockActivity(), response.message, Toast.LENGTH_SHORT).show();
			if(response.player != null) {
				if(response.player.targetHealth > 0) {
					Repository model = ((App)(getActivity().getApplication())).getRepo();
					model.updatePlayer(response.player);
					initEscapeTimer(getRemainingEscapeTime());
				}
			}
		}
	}
	
	@AfterInject
	public void afterInjection() {
		//subvert a bug in HttpUrlConnection
		//see: http://www.sapandiwakar.in/technical/eofexception-with-spring-rest-template-android/
		restClient.getRestTemplate().setRequestFactory(
				new HttpComponentsClientHttpRequestFactory());

	}
}
