package assassins.ui.prototypes;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;


public class AdjustBoundaryLayout extends LinearLayout {
	
    private float mX, mY = 0;
    private float mWidth, mHeight = 200;
    private static final float TOUCH_TOLERANCE = 4;
    private Paint boundsRect = null;
    private Paint boundsBorder = null;
	
	public AdjustBoundaryLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public AdjustBoundaryLayout(Context context) {
		super(context);
		init();
	}
	
	private void init() {
		//set up the rectangle and drawables
		boundsBorder = new Paint();
		boundsBorder.setAntiAlias(true);
		//boundsBorder.setDither(true);
		boundsBorder.setColor(0xFFFF0000);
		boundsBorder.setStyle(Paint.Style.STROKE);
		boundsBorder.setStrokeWidth(8);
		
	}
	
	 @Override
    protected void dispatchDraw(Canvas canvas) {
    	
    	RectF drawRect = new RectF();
    	drawRect.set(mX,mY, mWidth, mHeight);
    	
    	//canvas.drawRoundRect(drawRect, 5, 5, innerPaint);
		canvas.drawRoundRect(drawRect, 1, 1, boundsBorder);
		
		super.dispatchDraw(canvas);
    }
	 


     private void touch_start(float x, float y) {
         mX = x;
         mY = y;
     }
     private void touch_move(float x, float y) {
         float dx = Math.abs(x - mX);
         float dy = Math.abs(y - mY);
         if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
             mX = x;
             mY = y;
         }
     }
     private void touch_up() {

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
