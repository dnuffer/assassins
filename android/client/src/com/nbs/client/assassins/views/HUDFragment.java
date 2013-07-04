/**
 * 
 */
package com.nbs.client.assassins.views;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.model.LatLng;
import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;
import com.googlecode.androidannotations.annotations.rest.RestService;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.models.MatchModel;
import com.nbs.client.assassins.models.PlayerModel;
import com.nbs.client.assassins.models.UserModel;
import com.nbs.client.assassins.network.AttackResponse;
import com.nbs.client.assassins.network.HuntedRestClient;
import com.nbs.client.assassins.network.LocationMessage;
import com.nbs.client.assassins.sensors.BearingProvider;
import com.nbs.client.assassins.sensors.BearingReceiver;

/**
 * @author cam
 *
 */
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
	
	@ViewById(R.id.hud_erange)
	TextView targetRange;
	
	@ViewById(R.id.hud_trange)
	TextView enemyRange;

	public HUDFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		targetRange.setText(PlayerModel.getTargetProximity(getSherlockActivity()));
		enemyRange.setText(PlayerModel.getEnemyProximity(getSherlockActivity()));
		lifeView.setProgress(PlayerModel.getMyLife(getSherlockActivity()));
		tLifeView.setProgress(PlayerModel.getTargetLife(getSherlockActivity()));
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);
	}
	
	@AfterInject
	public void afterInjection() {
		//subvert a bug in HttpUrlConnection
		//see: http://www.sapandiwakar.in/technical/eofexception-with-spring-rest-template-android/
		restClient.getRestTemplate().setRequestFactory(
				new HttpComponentsClientHttpRequestFactory());
	}

	@Override
	public void onPause() {
		stopSensorUpdates();
		super.onPause();
	}

	@Override
	public void onResume() {
		registerForSensorUpdates();
		super.onResume();
	}
	
	private void stopSensorUpdates() {
		bearingProvider.unregisterForBearingUpdates(this);
	}

	private void registerForSensorUpdates() {
		bearingProvider.registerForBearingUpdates(this);
	}
	
	@Override
	public void setBearingProvider(BearingProvider provider) {
		bearingProvider = provider;	
	}
	
	private void setAttackButtonEnabled(boolean enabled) {
		attackButton.setEnabled(enabled);
		if(enabled) {
			escapeTimeText.setText("");
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
		tBearingView.setText(Float.toString(tBearing));
	}
	
	public void onMyLifeChanged(int life) {
		lifeView.setProgress(life);
	}
	
	public void onTargetLifeChanged(int tLife) {
		tLifeView.setProgress(tLife);
	}
	
	public void onTargetRangeChanged(String tRange) {
		targetRange.setText(tRange);
		setAttackButtonEnabled(tRange.equals(PlayerModel.ATTACK_RANGE));
	}
	
	public void onEnemyRangeChanged(String eRange) {
		enemyRange.setText(eRange);
	}
	
	private void onEscapeTimeChanged(long escapeTimeRemaining) {
		if(escapeTimeRemaining > 1000) {
			escapeTimeText.setText(escapeTimeRemaining+" s");
		} else {
			setAttackButtonEnabled(true);
		}
	}
	
	@Click(R.id.hud_attack)
	public void onAttackClicked() {
		setAttackButtonEnabled(false);
		attack();
	}
	
	@Background
	public void attack() {
		Log.d(TAG, "attack in background");
		
		AttackResponse response = null;
		
		try {
			LocationMessage msg = 
				new LocationMessage(UserModel.getLocation(getSherlockActivity()),
									UserModel.getInstallId(getSherlockActivity()));
			response = restClient.attack(UserModel.getToken(getSherlockActivity()), msg);
			
		}
		catch(Exception e) {
			Log.d(TAG, e.getMessage());
		}
		
		attackFinished(response);
	}
	
	@UiThread
	public void attackFinished(AttackResponse response)
	{
		Log.d(TAG, "attackFinished status:" + response.ok());
		
		attackButton.setEnabled(!(response != null && response.ok() && response.hit && response.targetLife > 0));
		
		if(response != null && response.ok() && MatchModel.inActiveMatch(getSherlockActivity())) {
				
			PlayerModel.setTargetLife(getSherlockActivity(), response.targetLife);
			//TODO set time of last attack in order to initialize escapeTimer on app relaunch
			//PlayerModel.setTimeOfLastSuccessfulAttack(getSherlockActivity(), response.time);
			Toast.makeText(getSherlockActivity(), response.message, 
					Toast.LENGTH_SHORT).show();
			
			Integer escapeTime = MatchModel.getEscapeTime(getSherlockActivity());
			
			if(response.targetLife > 0 && escapeTime != null) {
				long escapeTimeRemaining = (response.time + (long)(escapeTime*1000)) - System.currentTimeMillis();
				
				escapeTimer = new CountDownTimer(escapeTimeRemaining, 1000/*tick time in millis*/) {
				     public void onTick(long millisUntilFinished) {
				         onEscapeTimeChanged(millisUntilFinished);
				     }
	
				     public void onFinish() {
				    	 onEscapeTimeChanged(0);
				     }
				}.start();
			}
		}
	}
	
}
