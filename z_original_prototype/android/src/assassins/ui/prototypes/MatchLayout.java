package assassins.ui.prototypes;

import java.util.List;

import org.bson.types.ObjectId;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MatchLayout extends RelativeLayout {

	private GameSnapshotManager locManager;
	private ObjectId id;
	
	private MapView matchMapView;
	private ImageView arrow;
	private ImageView compass;
	private TextView title;
	private Button btnAttack;
	private RotateAnimation rotateArrow;
	private CountDownTimer afterAttackDelayTimer;
	
	private float oldBearingInDegrees;
	private boolean haveAnimatedToMyLocation;
	private boolean isAttackDelayActive;
	
	private static final int FULL_HEALTH_PLUS_ONE = 4;
	
	public MatchLayout(Context context) {
		super(context);
		init();
	}

	public MatchLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}
	
	public MatchLayout(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
		
	}

	public void setGameSnapshotManager(GameSnapshotManager gsm)
	{
	     locManager = gsm;
	}
	
	public void init()
	{
		Log.d("Project Assassins", "MatchLayout::init()");
		haveAnimatedToMyLocation = false;
		isAttackDelayActive = false;
	}

	@Override
	protected void dispatchDraw(Canvas c)
	{
		super.dispatchDraw(c);
		
		Log.d("Project Assassins","MatchLayout.DISPATCH_DRAW");
		
		title = (TextView)findViewById(R.id.match_header);
		arrow = (ImageView)findViewById(R.id.compass_arrow);
		compass = (ImageView)findViewById(R.id.compass);
		btnAttack = (Button)findViewById(R.id.attack_button);
		matchMapView = (MapView)findViewById(R.id.map_view);
		matchMapView.getOverlays().clear();
		
		GeoPoint targetLoc = locManager.getTargetLocation();
		GeoPoint myLoc = locManager.getMyLocation();
		
		if(!haveAnimatedToMyLocation && myLoc != null)
		{
			matchMapView.getController().animateTo(myLoc);
			matchMapView.getController().zoomToSpan(1, 1);
			haveAnimatedToMyLocation = true;
		}
		
		int health = locManager.getMyLife();
		ProgressBar lifeRemaining = (ProgressBar)findViewById(R.id.life_remaining);
		lifeRemaining.setProgress(health);
		
		int targetHealth = locManager.getTargetLife();
		if(targetHealth > 0)
		{
			ProgressBar targetLifeRemaining = (ProgressBar)findViewById(R.id.target_life_remaining);
			targetLifeRemaining.setProgress(targetHealth);
		}
		
		//float oldBearingInDegrees = locManager.getOldBearingToTarget();
		float bearingInDegrees = locManager.getBearingToTarget();
		
		Log.d("Project Assassins", "MatchLayout.DISPATCH_DRAW::my loc "+myLoc+", my health "+health);
		Log.d("Project Assassins", "MatchLayout.DISPATCH_DRAW::target loc "+targetLoc);
		Log.d("Project Assassins", "MatchLayout.DISPATCH_DRAW::old bearing: "+oldBearingInDegrees+
															", new bearing: "+bearingInDegrees);
		Log.d("Project Assassins", "MatchLayout.DISPATCH_DRAW::my proximity to target "+
															locManager.getPlayerProximityToEnemy());
		logDistanceBtwnPlayerAndTarget(targetLoc, myLoc);

		drawTargetProximity();
		handleAttackResults();
		drawAttackButton();

		Drawable myIcon = getMyHealthIcon(health);
		Drawable targetIcon = getTargetIcon(targetHealth);
		drawCompass(targetLoc, oldBearingInDegrees, bearingInDegrees);
		drawPlayerIcons(targetLoc, myLoc, myIcon, targetIcon);
		
		locManager.clearTargetSlainFlag();
	}
	

	private void logDistanceBtwnPlayerAndTarget(GeoPoint targetLoc,
			GeoPoint myLoc) {
		float distanceInMeters = 10000000; //Arbitrarily high distance
		
		if(targetLoc != null)
		{
			distanceInMeters = PlayerLocation.getMetersBetween(myLoc, targetLoc);
			Log.d("Project Assassins", "MatchLayout.DISPATCH_DRAW::Distance in Meters: "+distanceInMeters);
		}
	}

	private Drawable getMyHealthIcon(int health) {
		Drawable myIcon = this.getResources().getDrawable(R.drawable.assassin_icon);
		myIcon.setAlpha(255/(FULL_HEALTH_PLUS_ONE-health));
		
		//Handle my status display
		if(locManager.wasAttacked())
		{	
			if (health == 0)
			{
				Toast.makeText(getContext(), "you've been slain.", 3).show();
				myIcon = this.getResources().getDrawable(R.drawable.x_mark);
			}
			else
			{
				Toast.makeText(getContext(), "you've been attacked.", 3).show();
			}
			//clear flag
			locManager.setAttacked(false);
		}
		return myIcon;
	}

	private void drawAttackButton() {
		if(!isAttackDelayActive && 
		   !locManager.isAttackAttemptInProgress() &&
		   locManager.getPlayerProximityToEnemy() == Proximity.ATTACK_RANGE)
		{
			
			this.setAttackButtonState(true);
		}
		else
		{
			this.setAttackButtonState(false);
		}
	}

	private void drawTargetProximity() {

		if(!isAttackDelayActive)
		{
			if(locManager.getPlayerProximityToEnemy() == Proximity.HUNT_RANGE ||
			   locManager.getPlayerProximityToEnemy() == Proximity.ATTACK_RANGE)
			{
				setTitleString("Hunt Range", R.color.Red);
			}
			else
			{
				setTitleString("Search Range", R.color.gray);
			}
		}
	}
	
	private Drawable getTargetIcon(int targetHealth) {
		Drawable targetIcon = null;
		
		//only draw target if in range
		if(locManager.getPlayerProximityToEnemy() == Proximity.HUNT_RANGE ||
		   locManager.getPlayerProximityToEnemy() == Proximity.ATTACK_RANGE)
		{
			
			if(targetHealth > 0)
			{
				Log.d("Project Assassins", "MatchLayout.DISPATCH_DRAW::adding target location to view");
				targetIcon = this.getResources().getDrawable(R.drawable.guard);
				targetIcon.setAlpha(255/(FULL_HEALTH_PLUS_ONE-targetHealth));
			}
			else if(locManager.isTargetSlain())
			{
				Log.d("Project Assassins", "MatchLayout.DISPATCH_DRAW::target health is 0");
				Toast.makeText(getContext(), "target is slain.", 3).show();
				targetIcon = this.getResources().getDrawable(R.drawable.x_mark);
			}
		}

		return targetIcon;
	}

	private void handleAttackResults() {

		if(locManager.attackSucceeded() && !locManager.isTargetSlain())
		{	
			Log.d("Project Assassins", "MatchLayout.DISPATCH_DRAW::attack successful");
			Toast.makeText(getContext(), "your enemy has taken damage.", 3).show();

			int attackDelay = 1; 
			Conspiracy currMatch = locManager.getCurrentMatch();
			if(currMatch != null)
			{
				attackDelay = currMatch.getAttackDelay();
			}
			
			Log.d("Project Assassins", "MatchLayout.handleAttackResults() Attack Delay: " + attackDelay);
			
			afterAttackDelayTimer =  
					new CountDownTimer(attackDelay/*sec*/*1000/*ms per sec*/, 1000/*tick ms duration*/) {

			     public void onTick(long millisUntilFinished) {
			         setAttackString("Attack delay: " + millisUntilFinished / 1000);
			     }

			     public void onFinish() {
			    	 setAttackString("Attack Delay complete.");
			    	 setAttackButtonState(true);
			    	 isAttackDelayActive = false;
			     }
			  }.start();
			  
			isAttackDelayActive = true;
			locManager.setAttackSucceeded(false);
		}
	}

	protected void setAttackString(String string) {
		TextView attackStr = (TextView)findViewById(R.id.attack_delay);
		attackStr.setText(string);
	}

	private void drawPlayerIcons(GeoPoint targetLoc, GeoPoint myLoc,
			Drawable myIcon, Drawable targetIcon) {
		Log.d("Project Assassins", "MatchLayout.DISPATCH_DRAW::adding my location to view");
		addLocationToOverlays(myLoc, "My Location", "", myIcon);
		
		if(targetIcon != null)
		{
			Log.d("Project Assassins", "MatchLayout.DISPATCH_DRAW::adding target location to view");
			addLocationToOverlays(targetLoc, "Target Location", "", targetIcon);
		}
	}

	private void drawCompass(GeoPoint targetLoc, float oldBearingInDegrees,
			float bearingInDegrees) {
		
		if ((targetLoc != null && arrow != null) &&
			(oldBearingInDegrees != bearingInDegrees) && 
			(rotateArrow == null || rotateArrow.hasEnded()))
		{
			
			AnimationSet arrowAnimSet = new AnimationSet(true);
			arrowAnimSet.setFillEnabled(true);
			arrowAnimSet.setFillAfter(true);
			
			rotateArrow = new RotateAnimation(oldBearingInDegrees, bearingInDegrees, 
															Animation.RELATIVE_TO_SELF, 0.5f, 
															Animation.RELATIVE_TO_SELF, 0.5f);
			rotateArrow.setFillEnabled(true);
			rotateArrow.setFillAfter(true);
			rotateArrow.setDuration(2000);
			
			arrowAnimSet.addAnimation(rotateArrow);

			
			//if the compass and arrow are not showing, 
			//make them visible and then fade them in
			if(compass.getVisibility() != View.VISIBLE && 
			   arrow.getVisibility() != View.VISIBLE)
			{
				compass.setVisibility(View.VISIBLE);
				arrow.setVisibility(View.VISIBLE);
				
				AlphaAnimation compassFadeIn = new AlphaAnimation(0.0f, 1.0f);
				AlphaAnimation arrowFadeIn = new AlphaAnimation(0.0f, 1.0f);
				
				compassFadeIn.setDuration(700);
				arrowFadeIn.setDuration(1000);
				
				arrowAnimSet.addAnimation(arrowFadeIn);
				compass.startAnimation(compassFadeIn);
			}
			arrow.startAnimation(arrowAnimSet);
			
			Log.d("Project Assassins", "setting old bearing "+ oldBearingInDegrees + " to new bearing "+bearingInDegrees);
			this.oldBearingInDegrees = bearingInDegrees;
		}
	}
	
	
	private void setTitleString(String text, int colorResource)
	{
		if(title != null)
		{
			Log.d("Project Assassins", "MatchLayout.DISPATCH_DRAW::changing title string to: "+text);
			title.setText(text);
			title.setTextColor(getContext().getResources().getColor(colorResource));
		}
	}
	
	
	private void setAttackButtonState(Boolean state)
	{

		if(btnAttack != null)
		{
			Log.d("Project Assassins", "MatchLayout.DISPATCH_DRAW::changing attack button enabled state to: "+state);
			btnAttack.setEnabled(state);
		}
	}
	
	private void addLocationToOverlays(GeoPoint point, String title, String snippet, Drawable drawable)
	{
		if(point != null)
		{
			 List<Overlay> mapOverlays = matchMapView.getOverlays();
		     
		     CustomItemizedOverlay itemizedOverlay = new CustomItemizedOverlay(drawable, this.getContext());
		     OverlayItem overlayitem = new OverlayItem(point, title, snippet);
		     
		     itemizedOverlay.addOverlay(overlayitem);
		     mapOverlays.add(itemizedOverlay); 
		}
		else
		{
			 Log.d("Project Assassins", "MatchLayout.addLocationToOverlays() "+title+" is null"); 
		}
	}
	

	
}

