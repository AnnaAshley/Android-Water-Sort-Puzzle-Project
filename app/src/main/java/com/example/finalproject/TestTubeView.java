package com.example.finalproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

//Defining a custom test tube view to be used in the water puzzles.
// More can be created based on puzzle difficulty

public class TestTubeView  extends View {
    private int maxLayers = 4; //default value
    private float tubeHeightPx;
    private Paint paint;
    //define an OnClickListener for easier button logic implementation
    private OnClickListener externalClickListener;

    //define a field to store the list of color layers within the test tube
    private List<Integer> colorLayers = new ArrayList<>();
    public TestTubeView(Context context){
        super(context);
        //make the test tube clickable to allow for game play
        setClickable(true);
        paint = new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        //set width and height according to attributes.
        //may eventually have standard sizes, but as puzzles get more complex with more test tubes the sizes will probably change
        int width = getWidth();
        int height = getHeight();

        float tubeHeightDp = 600f;
        float layerHeight = (tubeHeightDp - 20F) / (float) maxLayers;
        float tubeWidth = 100f;
        float left = width / 2f - tubeWidth / 2f;
        float right= width / 2f + tubeWidth / 2f;
        float top = height - tubeHeightDp;
        //add the colored water layers

        if (!colorLayers.isEmpty()) {
            for (int i = 0; i < colorLayers.size(); i++) {
                int color = colorLayers.get(i);
                paint.setColor(color);
                paint.setStyle(Paint.Style.FILL);

                float layerTop = top + (maxLayers - i - 1) * layerHeight;
                float bottom = layerTop + layerHeight;

                if (i == 0) {
                    // Draw curved bottom only for the **bottommost layer**
                    Path curvedBottom = new Path();
                    curvedBottom.moveTo(left + 10, layerTop);
                    curvedBottom.lineTo(left + 10, bottom - 10);
                    curvedBottom.arcTo(left + 10, bottom - 20, right - 10, bottom, 180, -180, false);
                    curvedBottom.lineTo(right - 10, layerTop);
                    curvedBottom.close();
                    canvas.drawPath(curvedBottom, paint);
                } else {
                    canvas.drawRect(left + 10, layerTop, right - 10, bottom, paint);
                }
            }
        }


        //defining test tube outline

        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(5f);
        paint.setStyle(Paint.Style.STROKE);
        //create the test tube outline
        //Creating the path
        Path testTube = new Path();
        testTube.moveTo(left, top);
        testTube.lineTo(left, height - 20);
        testTube.arcTo(left, height - 20, right, height, 180, -180, false);
        testTube.lineTo(right, top);

        canvas.drawPath(testTube, paint);
    }

    public void setColorLayers(List<Integer> colors) {
        colorLayers = new ArrayList<>(colors);
        //invalidate the view to force a redraw with the updated colors
        invalidate();
    }

    public void setMaxLayers(int maxLayers) {
        this.maxLayers = maxLayers;
        invalidate(); // Redraw with new layer count
    }

    @Override
    //define the on touch event
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            performClick();
            return true;
        }
        return super.onTouchEvent(event);

    }

    @Override
    public boolean performClick(){
        super.performClick();
        if(externalClickListener != null) {
            externalClickListener.onClick(this);
        }
        return true;
    }
    //allow the OnClickListener to be defined in the activity.
    public void setExternalClickListener(OnClickListener listener){
        this.externalClickListener = listener;
    }
}
