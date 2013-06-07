package assassins.ui.prototypes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class TransparentPanelStatic extends LinearLayout 
{ 
	
	//the boundary rectangle
	RectF drawRect;
	
	private Paint	innerPaint, borderPaint;


	public TransparentPanelStatic(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TransparentPanelStatic(Context context) {
		super(context);
		init(context);
	}
	public void resetBounds(Context context)
	{
		init(context);
	}

	private void init(Context context) {

		innerPaint = new Paint();
		innerPaint.setARGB(30, 75, 75, 75); //gray
		innerPaint.setAntiAlias(true);

		borderPaint = new Paint();
		borderPaint.setARGB(255, 255, 255, 255);
		borderPaint.setAntiAlias(true);
		borderPaint.setStyle(Style.STROKE);
		borderPaint.setStrokeWidth(2);
	}
	
	
	public RectF getRect(){
		return drawRect;
	}
	
	public void setRect(RectF r){ 
		this.drawRect = r; 
	}
	
	public void setInnerPaint(Paint innerPaint) {
		this.innerPaint = innerPaint;
	}

	public void setBorderPaint(Paint borderPaint) {
		this.borderPaint = borderPaint;
	}

    @Override
    protected void dispatchDraw(Canvas canvas) {
    	
    	if(drawRect != null)
    	{
	    	Log.d("Project Assassins", "Drawing bounds rectangle");
    		canvas.drawRoundRect(drawRect, 5, 5, innerPaint);
			canvas.drawRoundRect(drawRect, 5, 5, borderPaint);
    	}

		super.dispatchDraw(canvas);
    }

}