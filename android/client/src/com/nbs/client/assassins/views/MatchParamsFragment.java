package com.nbs.client.assassins.views;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.SeekBarProgressChange;
import com.googlecode.androidannotations.annotations.ViewById;
import com.nbs.client.assassins.R;
import com.nbs.client.assassins.utils.LocationUtils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

@EFragment(R.layout.gameplay_dialog)
public class MatchParamsFragment extends SherlockFragment {

	private static final String TAG = "GameplayFragment";
	
    private GameplaySettingsListener mListener;
    
	public static final double FEET_PER_MILE = 5280.0f;
	public static final int DEFAULT_HUNT_RANGE = 520; //ft
	public static final int DEFAULT_ATTACK_RANGE = 200; //ft
	public static final int DEFAULT_ATTACK_DELAY = 180; //sec

	private static final int DONE_ID = 1;
	
	@ViewById(R.id.seekBarHuntRange)
	public SeekBar huntRange;
	
	@ViewById(R.id.seekBarAttackRange)
	public SeekBar attackRange;
	
	@ViewById(R.id.seekBarAttackDelay)
	public SeekBar delayAfterAttack;
	
	@ViewById(R.id.textViewHuntRangeValue)
	public TextView huntRangeValueText;
	
	@ViewById(R.id.textViewAttackRangeValue)
	public TextView attackRangeValueText;
	
	@ViewById(R.id.textViewAttackDelayValue)
	public TextView attackDelayValueText;
	
	public int huntRangeVal;
	public int attackRangeVal;
	public int delayAfterAttackVal;
	
	public interface GameplaySettingsListener {
    	public void onGameplaySettingsFinished(int waitAfterHit, double huntRange, double attackRange);
    	public void onGameplaySettingsCancelled();
    }
	
	public MatchParamsFragment(){}

 	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof GameplaySettingsListener){
            mListener = (GameplaySettingsListener) activity;
        } else {
            throw new ClassCastException(activity.toString() + " must implement GameplaySettingsListener");
        }
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		setMenuVisibility(true);
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		
		huntRangeVal = DEFAULT_HUNT_RANGE;
		attackRangeVal = DEFAULT_ATTACK_RANGE;
		delayAfterAttackVal = DEFAULT_ATTACK_DELAY;
		
		attackRange.setProgress(attackRangeVal);	
		huntRange.setProgress(huntRangeVal);
		delayAfterAttack.setProgress(delayAfterAttackVal);
		
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		Log.d(TAG, "onCreateOptionsMenu()");
		menu.add(Menu.NONE, DONE_ID, Menu.FIRST, "Done")
			.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
	    	case DONE_ID:
	    		mListener.onGameplaySettingsFinished(
	    				getAttackDelay(), getHuntRangeMiles(), getAttackRangeMiles());
	    	    return true;
	    }
	    
		return super.onOptionsItemSelected(item);
	}
	
	@SeekBarProgressChange(R.id.seekBarHuntRange)
	void onHuntRangeChanged(SeekBar seekBar, int progress, boolean fromUser) {
		huntRangeValueText.setText(progress + " ft");
		huntRangeVal = progress;
		
		//attack range must be lower than hunt range
		int currAttackVal = attackRange.getProgress();
		attackRange.setMax((huntRange.getProgress() > 0) ? huntRange.getProgress() -1 : 0);
		attackRange.setProgress(currAttackVal);
		
		//force onProgressChange to be called to keep it in sync with hunt range
		if(currAttackVal < attackRange.getMax())
		{
			attackRange.incrementProgressBy(1);
			attackRange.incrementProgressBy(-1);
		}
	}
	
	@SeekBarProgressChange(R.id.seekBarAttackRange)
	void onAttackRangeChanged(SeekBar seekBar, int progress, boolean fromUser) {
		attackRangeValueText.setText(progress + " ft");
		attackRangeVal = progress;
	}
	
	@SeekBarProgressChange(R.id.seekBarAttackDelay)
	void onAttackDelayChanged(SeekBar seekBar, int progress, boolean fromUser) {
		attackDelayValueText.setText(progress + " sec");
		delayAfterAttackVal = progress;
	}
	
	public double getHuntRangeMiles() {
		
		return LocationUtils.roundDouble(((double)huntRangeVal)/FEET_PER_MILE);
	}
	public void setHuntRange(int huntRangeVal) {
		this.huntRangeVal = huntRangeVal;
	}
	public double getAttackRangeMiles() {
		return LocationUtils.roundDouble(((double)attackRangeVal)/FEET_PER_MILE);
	}
	public void setAttackRange(int attackRangeVal) {
		this.attackRangeVal = attackRangeVal;
	}
	public int getAttackDelay() {
		return delayAfterAttackVal;
	}
	public void setAttackDelay(int delayAfterAttackVal) {
		this.delayAfterAttackVal = delayAfterAttackVal;
	}
}
