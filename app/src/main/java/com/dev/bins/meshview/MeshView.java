package com.dev.bins.meshview;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import static android.R.attr.fraction;
import static android.R.attr.path;

/**
 * Created by bin on 14/01/2017.
 */

public class MeshView extends View {


    Matrix mMatrix;
    PointF center;
    Bitmap mBitmap;
    Path mPath1, mPath2;
    int width, height;
    int bitmapW, bitmapH;
    private float[] verts;
    private float[] oriVerts;
    private Paint mPaint;
    private ValueAnimator valueAnimator;

    public MeshView(Context context) {
        this(context, null);
    }

    public MeshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MeshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.a);
        mPath1 = new Path();
        mPath2 = new Path();
        verts = new float[41 * 41 * 2];
        oriVerts = new float[41 * 41 * 2];
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(20);
        bitmapW = mBitmap.getWidth();
        bitmapH = mBitmap.getHeight();
        mMatrix = new Matrix();
        buildVerts();
        valueAnimator = ValueAnimator.ofInt(0, bitmapH);
        valueAnimator.setDuration(5000);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int value = (int) valueAnimator.getAnimatedValue();
                buildMeshByPathOnVertical(value);
                invalidate();
            }
        });
    }

    private void buildVerts() {
        int xOffset = bitmapW / 40;
        int yOffset = bitmapH / 40;
        int index = 0;
        for (int i = 0; i < 41; i++) {
            for (int i1 = 0; i1 < 41; i1++) {
//                i*41+i1
                oriVerts[index * 2] = verts[index * 2] = i1 * xOffset;
                oriVerts[index * 2 + 1] = verts[index * 2 + 1] = i * yOffset;
                index++;
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        center = new PointF(w / 2, h);
        mPath1.moveTo(0, 0);
        mPath1.lineTo(0, 200);
        mPath1.quadTo(0, h, center.x, center.y);

        mPath2.moveTo(w, 0);
        mPath2.lineTo(w, 200);
        mPath2.quadTo(w, h, center.x, center.y);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmapMesh(mBitmap, 40, 40, verts, 0, null, 0, null);
//        canvas.drawPath(mPath1, mPaint);
//        canvas.drawPath(mPath2, mPaint);

    }

    private void buildVerts(float fraction) {
        PathMeasure pathMeasure = new PathMeasure(mPath1, false);
        float[] pos = new float[2];
        pathMeasure.getPosTan(pathMeasure.getLength() * fraction, pos, null);
        int xOffset = bitmapW / 40;
        int yOffset = bitmapH / 40;
        int index = 0;
        for (int i = 0; i < 41; i++) {
            for (int i1 = 0; i1 < 41; i1++) {
//                i*41+i1
                verts[index * 2] = i1 * xOffset+pos[0];
                verts[index * 2 + 1] = i * yOffset+pos[1];
                index++;
            }
        }
    }

    private void buildMeshByPathOnVertical(int timeIndex)
    {
        PathMeasure mFirstPathMeasure = new PathMeasure();
        PathMeasure mSecondPathMeasure = new PathMeasure();
        mFirstPathMeasure.setPath(mPath1, false);
        mSecondPathMeasure.setPath(mPath2, false);

        int index = 0;
        float[] pos1 = {0.0f, 0.0f};
        float[] pos2 = {0.0f, 0.0f};
        float firstLen  = mFirstPathMeasure.getLength();
        float secondLen = mSecondPathMeasure.getLength();

        float len1 = firstLen / 40;
        float len2 = secondLen / 40;

        float firstPointDist  = timeIndex * len1;
        float secondPointDist = timeIndex * len2;
        float height = bitmapH;

        mFirstPathMeasure.getPosTan(firstPointDist, pos1, null);
        mFirstPathMeasure.getPosTan(firstPointDist + height, pos2, null);
        float x1 = pos1[0];
        float x2 = pos2[0];
        float y1 = pos1[1];
        float y2 = pos2[1];
        float FIRST_DIST  = (float)Math.sqrt( (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) );
        float FIRST_H = FIRST_DIST / 40;

        mSecondPathMeasure.getPosTan(secondPointDist, pos1, null);
        mSecondPathMeasure.getPosTan(secondPointDist + height, pos2, null);
        x1 = pos1[0];
        x2 = pos2[0];
        y1 = pos1[1];
        y2 = pos2[1];

        float SECOND_DIST = (float)Math.sqrt( (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) );
        float SECOND_H = SECOND_DIST / 40;

        for (int y = 0; y <= 40; ++y)
        {
            mFirstPathMeasure.getPosTan(y * FIRST_H + firstPointDist, pos1, null);
            mSecondPathMeasure.getPosTan(y * SECOND_H + secondPointDist, pos2, null);

            float w = pos2[0] - pos1[0];
            float fx1 = pos1[0];
            float fx2 = pos2[0];
            float fy1 = pos1[1];
            float fy2 = pos2[1];
            float dy = fy2 - fy1;
            float dx = fx2 - fx1;

            for (int x = 0; x <= 40; ++x)
            {
                // y = x * dy / dx
                float fx = x * w / 40;
                float fy = fx * dy / dx;

                verts[index * 2 + 0] = fx + fx1;
                verts[index * 2 + 1] = fy + fy1;

                index += 1;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        valueAnimator.start();
        return super.onTouchEvent(event);
    }
}
