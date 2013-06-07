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

public class TransparentPanel extends LinearLayout 
{ 
	
	private final float TOUCH_TOLERANCE = 40;
	
	//the boundary rectangle
	RectF drawRect;

	private final float MAX_Y = 670;
	private final float MIN_Y = 10;
	private final float MAX_X = 470;
	private final float MIN_X = 10;
	private final float MIN_WIDTH = 100;
	private final float MIN_HEIGHT = 100;
	
	private float touchedX = -1;
	private float touchedY = -1;
	
	private Paint	innerPaint, borderPaint;
    
	private boolean touchedInside = false;
	private boolean touchedUpperLeft = false;
	private boolean touchedLowerRight = false;
	private boolean touchedDragging = false;
	
	Drawable handleLt;
	Drawable handleRt;
	Drawable handleTranslate;

	public TransparentPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TransparentPanel(Context context) {
		super(context);
		init(context);
	}
	public void resetBounds(Context context)
	{
		init(context);
	}

	private void init(Context context) {
		
		handleLt = context.getResources().getDrawable(R.drawable.ic_maps_indicator_current_position);
		handleRt = context.getResources().getDrawable(R.drawable.ic_maps_indicator_current_position);
		handleTranslate = context.getResources().getDrawable(android.R.drawable.btn_star_big_on);
		
		drawRect = new RectF(75,125, 300, 400);
		innerPaint = new Paint();
		innerPaint.setARGB(30, 75, 75, 75); //gray
		innerPaint.setAntiAlias(true);

		borderPaint = new Paint();
		borderPaint.setARGB(255, 255, 255, 255);
		borderPaint.setAntiAlias(true);
		borderPaint.setStyle(Style.STROKE);
		borderPaint.setStrokeWidth(2);
	}
	
	
	public RectF getRect(){return drawRect;}
	
	public void setInnerPaint(Paint innerPaint) {
		this.innerPaint = innerPaint;
	}

	public void setBorderPaint(Paint borderPaint) {
		this.borderPaint = borderPaint;
	}

    @Override
    protected void dispatchDraw(Canvas canvas) {
    	
    	//drawRect.set(leftX,upperY, leftX+lowerY, upperY+rightX);
    	
    	canvas.drawRoundRect(drawRect, 5, 5, innerPaint);
		canvas.drawRoundRect(drawRect, 5, 5, borderPaint);
		
		if(touchedInside)
		{
			int w = handleTranslate.getIntrinsicWidth();
			int h = handleTranslate.getIntrinsicHeight();
			int x = (int) drawRect.centerX() - w/2;
			int y = (int) drawRect.centerY() - h/2;
			
			//draw arrows in center of box to hint at translate
			handleTranslate.setBounds( x, y, x + w, y + h );
			handleTranslate.draw( canvas );
    	}
		else
		{
			//draw the corner items to hint at scale
			int w = handleLt.getIntrinsicWidth();
			int h = handleLt.getIntrinsicHeight();
			int x = (int)(drawRect.left) - w/2;
			int y = (int)(drawRect.top) - h/2;
	
			handleLt.setBounds( x, y, x + w, y + h );
			handleLt.draw( canvas );
			
			w = handleRt.getIntrinsicWidth();
			h = handleRt.getIntrinsicHeight();
			x = (int)(drawRect.right) - w/2;
			y = (int)(drawRect.bottom) - h/2;
	
			handleRt.setBounds( x, y, x + w, y + h );
			handleRt.draw( canvas );
		}
		
		super.dispatchDraw(canvas);
    }
    
    private void touch_start(float x, float y) {
        
    	Log.d("touch_start:  ","touched at: x: " + x +" y: "+ y);
    	if(touchedUpperLeft(x, y)) //touch upper left
    	{
    		touchedUpperLeft = true;
    	}
    	else if(touchedLowerRight(x, y)) //touch upper right
    	{
    		touchedLowerRight = true;
    	}
    	else if(drawRect.contains(x,y))
    	{
    		//display a drawable inside the square indicating ability to move in all directions
    		touchedInside = true;
    		
    		//store the last touch to get delta on touch_move
    		touchedX = x;
    		touchedY = y;
    		Log.d("touch_start:  ","touched inside box at: x: " + x +" y: "+ y);
    	}
    	
    }

    private boolean touchedUpperLeft(float x, float y)
    {
    	Log.d("TESTING if touched Upper Left: ","touched at: x: " + x +" y: "+ y + " range: x: " +Math.abs(drawRect.left - x) + " y: "+ Math.abs(drawRect.top - y));
    	
    	if(Math.abs(drawRect.left - x) < TOUCH_TOLERANCE &&
    		Math.abs(drawRect.top - y) < TOUCH_TOLERANCE){
    		Log.d("touched Upper Left:  ","touched at: x: " + x +" y: "+ y);
    		return true;
    	}	
    	return false;
    }
    
    private boolean touchedLowerRight(float x, float y)
    {
    	
    	Log.d("TESTING if touched Lower Right: ","touched at: x: " + x +" y: "+ y + " range: x: " +Math.abs(drawRect.left - x) + " y: "+ Math.abs(drawRect.top - y));
    	
    	if(Math.abs(drawRect.right - x) < TOUCH_TOLERANCE &&
    		Math.abs(drawRect.bottom - y) < TOUCH_TOLERANCE){
    		Log.d("touched Lower Right:  ","touched at: x: " + x +" y: "+ y);
    		return true;
        }	
    	return false;
    }
    
    //FIX ME! When opposite handle is in on edge or corner, handle will not scale correctly
    private void touch_move(float x, float y) {
    	//going to move the box
    	if(touchedInside || touchedUpperLeft || touchedLowerRight)
    	{
    		//width and height will stay same, box will relocate
    		float deltaX = 0;
    		float deltaY = 0;
    		
    		if(touchedDragging)
    		{
    			deltaX = x-touchedX;
    			deltaY = y-touchedY;
    		}
    		else
    		{
    			deltaX = 0;
    			deltaY = 0;
    		}
    		
    		//check if on board
            if(drawRect.left + deltaX < MIN_X) 
            {
            	deltaX = MIN_X-drawRect.left;
            }
            else if(drawRect.right +deltaX > MAX_X)
            {
            	deltaX = MAX_X-drawRect.right;
            }
            
            if(drawRect.top + deltaY < MIN_Y)
            {
            	deltaY = MIN_Y-drawRect.top;
            }	
            else if(drawRect.bottom + deltaY > MAX_Y)
            {
            	deltaY = MAX_Y-drawRect.bottom; 
            }
            
            //scaling upper left or translating
            if(touchedUpperLeft){
            	if(drawRect.right -(drawRect.left+deltaX) > MIN_WIDTH)
            		drawRect.left+=deltaX;
            	if(drawRect.bottom -(drawRect.top+deltaX) > MIN_HEIGHT)
	            	drawRect.top+=deltaY;
            }            
            //scaling lower right or translating
            else if(touchedLowerRight){
            	if(drawRect.right+deltaX -drawRect.left > MIN_WIDTH)
            		drawRect.right+=deltaX;
            	if(drawRect.bottom+deltaX -drawRect.top > MIN_HEIGHT)
            		drawRect.bottom+=deltaY;
            }
            else if(touchedInside)
            {
        		drawRect.left+=deltaX;
            	drawRect.top+=deltaY;
        		drawRect.right+=deltaX;
        		drawRect.bottom+=deltaY;
            }
            touchedX = x;
            touchedY = y;
            
            touchedDragging = true;
    	}

    }
    
    private void touch_up() 
    {
    	touchedInside = false;
    	touchedLowerRight = false;
    	touchedUpperLeft = false;
    	touchedDragging = false;
    	touchedX = -1;
    	touchedY = -1;
    	Log.d("touch_up: ","touch event over");
    }
	 
	 @Override
	public boolean onTouchEvent(MotionEvent event)
	{
		float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touch_start(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touch_move(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touch_up();
                invalidate();
                break;
        }
        return true;
		 
	 }
}