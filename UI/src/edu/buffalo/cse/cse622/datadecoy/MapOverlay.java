package edu.buffalo.cse.cse622.datadecoy;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapOverlay extends ItemizedOverlay<OverlayItem>{

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
    
 // Define the interface we will interact with from our Map
    public interface OnLongpressListener {
        public void onLongpress(MapView view, GeoPoint longpressLocation);
    }
    
	public MapOverlay(Drawable defaultMarker, Context context) {
		  super(boundCenterBottom(defaultMarker));
		  mContext = context;
	}
	
	@Override
	protected boolean onTap(int index) {
	  /*OverlayItem item = mOverlays.get(index);
	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.show();*/
	  return true;
	}
	
	@Override
    public boolean onTouchEvent(final MotionEvent event, final MapView mapView) 
    {   
		
		return false;
	}
	
	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    
	    populate();
	}
	
    public void draw(Canvas canvas, MapView mapv, boolean shadow){
        super.draw(canvas, mapv, shadow);
        
        if(mOverlays.size() > 1){
        	for(int i =0; i < mOverlays.size(); i++){
        		GeoPoint secondLast;
    			GeoPoint last;
        		if(i == mOverlays.size()-1 && mOverlays.size() > 2){
        			// Draw a line back to the start
        			secondLast = mOverlays.get(i).getPoint();
        			last = mOverlays.get(0).getPoint();
        		}
        		else if(i == mOverlays.size()-1){
        			// No point in redrawing a line
        			break;
        		}
        		else{
        			// Draw normal lines between points
        			secondLast = mOverlays.get(i).getPoint();
        			last = mOverlays.get(i+1).getPoint();
        		}
        		Paint   mPaint = new Paint();
        		mPaint.setDither(true);
        		mPaint.setColor(Color.RED);
        		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        		mPaint.setStrokeJoin(Paint.Join.ROUND);
        		mPaint.setStrokeCap(Paint.Cap.ROUND);
        		mPaint.setStrokeWidth(2);

        		Point p1 = new Point();
        		Point p2 = new Point();
        		Path path = new Path();

        		mapv.getProjection().toPixels(secondLast, p1);
        		mapv.getProjection().toPixels(last, p2);

        		path.moveTo(p2.x, p2.y);
        		path.lineTo(p1.x,p1.y);

        		canvas.drawPath(path, mPaint);
        	}
        }
    }
	
	@Override
	protected OverlayItem createItem(int arg0) {
		// TODO Auto-generated method stub
		return mOverlays.get(arg0);
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return mOverlays.size();
	}

}
