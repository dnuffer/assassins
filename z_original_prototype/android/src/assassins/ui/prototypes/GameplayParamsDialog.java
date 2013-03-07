package assassins.ui.prototypes;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class GameplayParamsDialog extends AlertDialog {

	protected static final float FEET_PER_MILE = 5280;
	private static final int DEFAULT_HUNT_RANGE = 520; //ft
	private static final int DEFAULT_ATTACK_RANGE = 200; //ft
	private static final int DEFAULT_ATTACK_DELAY = 180; //sec
	
	private SeekBar huntRange;
	private SeekBar attackRange;
	private SeekBar delayAfterAttack;
	
	TextView huntRangeValueText;
	TextView attackRangeValueText;
	TextView attackDelayValueText;
	
	private int huntRangeVal;
	private int attackRangeVal;
	private int delayAfterAttackVal;
	
	private View view;
	
	protected GameplayParamsDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}
	protected GameplayParamsDialog(Context context, int val) {
		super(context, val);
		// TODO Auto-generated constructor stub
	}
	protected GameplayParamsDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		LayoutInflater li = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = li.inflate(R.layout.gameplay_params_assassins, null, true);
        setView(view);
        
        
    	setTitle("Set Gameplay Parameters");
    	setIcon(R.drawable.assassin_icon);
    	

		
		huntRangeVal = DEFAULT_HUNT_RANGE;
		attackRangeVal = DEFAULT_ATTACK_RANGE;
		delayAfterAttackVal = DEFAULT_ATTACK_DELAY;
		
		huntRange        = (SeekBar) (view.findViewById(R.id.seekBarHuntRange));
		attackRange      = (SeekBar) (view.findViewById(R.id.seekBarAttackRange));
		delayAfterAttack = (SeekBar) (view.findViewById(R.id.seekBarAttackDelay));

		huntRangeValueText = (TextView)(view.findViewById(R.id.textViewHuntRangeValue));
		attackRangeValueText = (TextView)(view.findViewById(R.id.textViewAttackRangeValue));
		attackDelayValueText = (TextView)(view.findViewById(R.id.textViewAttackDelayValue));

		
		huntRange.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

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

			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}

			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}});
		attackRange.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

					attackRangeValueText.setText(progress + " ft");
					attackRangeVal = progress;
			}

			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}

			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}});
		delayAfterAttack.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

					attackDelayValueText.setText(progress + " sec");
					delayAfterAttackVal = progress;
			}

			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}

			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}});
		
		attackRange.setProgress(attackRangeVal);	
		huntRange.setProgress(huntRangeVal);
		delayAfterAttack.setProgress(delayAfterAttackVal);
		
		super.onCreate(savedInstanceState);
	}

	public float getHuntRangeVal() {
		return ((float)huntRangeVal)/FEET_PER_MILE;
	}
	public void setHuntRangeVal(int huntRangeVal) {
		this.huntRangeVal = huntRangeVal;
	}
	public float getAttackRangeVal() {
		return ((float)attackRangeVal)/FEET_PER_MILE;
	}
	public void setAttackRangeVal(int attackRangeVal) {
		this.attackRangeVal = attackRangeVal;
	}
	public int getDelayAfterAttackVal() {
		return delayAfterAttackVal;
	}
	public void setDelayAfterAttackVal(int delayAfterAttackVal) {
		this.delayAfterAttackVal = delayAfterAttackVal;
	}


}
