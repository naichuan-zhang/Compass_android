package com.example.compass;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;

public class CompassView extends View {

    private Paint markerPaint;
    private Paint textPaint;
    private Paint circlePaint;
    private String northString;
    private String southString;
    private String westString;
    private String eastString;
    private int textHeight;

    int[] borderGradientColors;
    float[] borderGradientPositions;
    int[] glassGradientColors;
    float[] glassGradientPositions;

    int skyHorizonColorFrom;
    int skyHorizonColorTo;
    int groundHorizonColorFrom;
    int groundHorizonColorTo;

    public CompassView(Context context) {
        super(context);
        init();
    }

    public CompassView(Context context, AttributeSet ats) {
        super(context, ats);
        init();
    }
    public CompassView(Context context, AttributeSet ats, int defaultStyle) {
        super(context, ats, defaultStyle);
        init();
    }

    private void init() {
        setFocusable(true);

        Resources r = this.getResources();

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(r.getColor(R.color.background));
        circlePaint.setStrokeWidth(1);
        circlePaint.setStyle(Paint.Style.STROKE);

        northString = r.getString(R.string.cardinal_north);
        southString = r.getString(R.string.cardinal_south);
        westString = r.getString(R.string.cardinal_west);
        eastString = r.getString(R.string.cardinal_east);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(r.getColor(R.color.text_color));
        textPaint.setFakeBoldText(true);
        textPaint.setSubpixelText(true);
        textPaint.setTextAlign(Paint.Align.LEFT);

        textHeight = (int) textPaint.measureText("yY");

        markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(r.getColor(R.color.marker_color));
        markerPaint.setAlpha(200);
        markerPaint.setStrokeWidth(1);
        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setShadowLayer(2,1, 1, r.getColor(R.color.shadow_color));

        borderGradientColors = new int[4];
        borderGradientPositions = new float[4];

        borderGradientColors[3] = r.getColor(R.color.outer_color);
        borderGradientColors[2] = r.getColor(R.color.inner_border_one);
        borderGradientColors[1] = r.getColor(R.color.inner_border_two);
        borderGradientColors[0] = r.getColor(R.color.inner_border);
        borderGradientPositions[3] = 0.0f;
        borderGradientPositions[2] = 1-0.03f;
        borderGradientPositions[1] = 1-0.06f;
        borderGradientPositions[0] = 1.0f;

        glassGradientColors = new int[5];
        glassGradientPositions = new float[5];

        int glassColor = 245;
        glassGradientColors[4] =Color.argb(65, glassColor, glassColor, glassColor);
        glassGradientColors[3] =Color.argb(100, glassColor, glassColor, glassColor);
        glassGradientColors[2] =Color.argb(50, glassColor, glassColor, glassColor);
        glassGradientColors[1] =Color.argb(0, glassColor, glassColor, glassColor);
        glassGradientColors[0] =Color.argb(0, glassColor, glassColor, glassColor);
        glassGradientPositions[4] = 1-0.0f;
        glassGradientPositions[3] = 1-0.06f;
        glassGradientPositions[2] = 1-0.10f;
        glassGradientPositions[1] = 1-0.20f;
        glassGradientPositions[0] = 1-1.0f;

        skyHorizonColorFrom = r.getColor(R.color.horizon_sky_from);
        skyHorizonColorTo = r.getColor(R.color.horizon_sky_to);
        groundHorizonColorFrom = r.getColor(R.color.horizon_ground_from);
        groundHorizonColorTo = r.getColor(R.color.horizon_ground_to);
    }

    private enum CompassDirection {
        N, NNE, NE, ENE,
        E, ESE, SE, SSE,
        S, SSW, SW, WSW,
        W, WNW, NW, NNW
    };

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureWidth = measure(widthMeasureSpec);
        int measureHeight = measure(heightMeasureSpec);

        int d = Math.min(measureWidth, measureHeight);
        setMeasuredDimension(d, d);
    }

    private int measure(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.UNSPECIFIED) {
            result = 200;
        } else {
            result = specSize;
        }

        return result;
    }

    @Override
    public void onDraw(Canvas canvas) {
        /*int mMeasuredWidth = getMeasuredWidth();
        int mMeasuredHeight = getMeasuredHeight();

        int px = mMeasuredWidth / 2;
        int py = mMeasuredHeight / 2;

        int radius = Math.min(px, py);

        canvas.drawCircle(px, py, radius, circlePaint);
        canvas.save();
        canvas.rotate(-bearing, px, py);

        int textWidth = (int)textPaint.measureText("W");
        int cardinalX = px - textWidth / 2;
        int cardinalY = py - radius + textHeight;

        for (int i = 0; i < 24; i++) {
            canvas.drawLine(px, py-radius, px, py - radius + 10, markerPaint);

            canvas.save();
            canvas.translate(0, textHeight);

            if (i % 6 == 0) {
                String dirString = "";
                switch (i) {
                    case(0) : {
                        dirString = northString;
                        int arrowY = 2 * textHeight;
                        canvas.drawLine(px, arrowY, px-5, 3*textHeight, markerPaint);
                        canvas.drawLine(px, arrowY, px+5, 3*textHeight, markerPaint);
                        break;
                    }
                    case(6) : dirString = eastString; break;
                    case(12) : dirString = southString; break;
                    case(18) : dirString = westString; break;
                }

                canvas.drawText(dirString, cardinalX, cardinalY, textPaint);
            } else if (i % 3 == 0) {
                String angle = String.valueOf(i * 15);
                float angleTextWidth = textPaint.measureText(angle);

                int angleTextX = (int)(px - angleTextWidth/2);
                int angleTextY = (int)(py - radius + textHeight);
                canvas.drawText(angle, angleTextX, angleTextY, textPaint);
            }
            canvas.restore();
            canvas.rotate(15, px, py);
        }
        canvas.restore();*/
        float ringWidth = textHeight + 4;
        int height = getMeasuredHeight();
        int width = getMeasuredWidth();

        int px = width / 2;
        int py = height / 2;
        Point center = new Point(px, py);
        int radius = Math.min(px, py) - 2;

        RectF boundingBox = new RectF(center.x - radius, center.y - radius, center.x + radius, center.y + radius);
        RectF innerBoundingBox = new RectF(center.x - radius + ringWidth, center.y - radius + ringWidth, center.x + radius - ringWidth, center.y + radius - ringWidth);

        float innerRadius = innerBoundingBox.height()/2;

        RadialGradient borderGradient = new RadialGradient(px, py, radius, borderGradientColors, borderGradientPositions, Shader.TileMode.CLAMP);
        Paint pgb = new Paint();
        pgb.setShader(borderGradient);
        Path outerRingPath = new Path();
        outerRingPath.addOval(boundingBox, Path.Direction.CW);
        canvas.drawPath(outerRingPath, pgb);

        LinearGradient skyShader = new LinearGradient(center.x, innerBoundingBox.top,
                center.x, innerBoundingBox.bottom,
                skyHorizonColorFrom, skyHorizonColorTo, Shader.TileMode.CLAMP);
        Paint skyPaint = new Paint();
        skyPaint.setShader(skyShader);
        LinearGradient groundShader = new LinearGradient(center.x, innerBoundingBox.top, center.x,
                innerBoundingBox.bottom, groundHorizonColorFrom, groundHorizonColorTo, Shader.TileMode.CLAMP);

        Paint groundPaint = new Paint();
        groundPaint.setShader(groundShader);

        float tiltDegree = pitch;
        while (tiltDegree > 90 || tiltDegree < -90) {
            if (tiltDegree > 90) tiltDegree = -90 + (tiltDegree - 90);
            if (tiltDegree < -90) tiltDegree = 90 - (tiltDegree + 90);
        }

        float rollDegree = roll;
        while (rollDegree > 180 || rollDegree < -180) {
            if (rollDegree > 180) rollDegree = -180 + (rollDegree - 180);
            if (rollDegree < -180) rollDegree = 180 - (rollDegree + 180);
        }

        Path skyPath = new Path();
        skyPath.addArc(innerBoundingBox, -tiltDegree, (180 + (2 * tiltDegree)));
        canvas.save();
        canvas.rotate(-rollDegree, px, py);
        canvas.drawOval(innerBoundingBox, groundPaint);
        canvas.drawPath(skyPath, skyPaint);
        canvas.drawPath(skyPath, markerPaint);

        int markWidth = radius / 3;
        int startX = center.x - markWidth;
        int endX = center.x + markWidth;
        double h = innerRadius*Math.cos(Math.toRadians(90-tiltDegree));
        double justTiltY = center.y - h;
        float pxPerDegree = (innerBoundingBox.height()/2)/45f;

        for (int i = 90; i >= -90; i -= 10) {
            double ypos = justTiltY + i * pxPerDegree;

            if ((ypos < (innerBoundingBox.top + textHeight)) || (ypos > innerBoundingBox.bottom - textHeight))
                continue;

            canvas.drawLine(startX, (float)ypos, endX, (float)ypos, markerPaint);
            int displayPos = (int)(tiltDegree - i);
            String displayString = String.valueOf(displayPos);
            float stringSizeWidth = textPaint.measureText(displayString);
            canvas.drawText(displayString,
                    (int)(center.x - stringSizeWidth/2),
                    (int)(ypos) + 1,
                    textPaint);
        }

        markerPaint.setStrokeWidth(2);
        canvas.drawLine(center.x-radius/2, (float)justTiltY, center.x+radius/2, (float)justTiltY, markerPaint);
        markerPaint.setStrokeWidth(1);

        Path rollArrow = new Path();
        rollArrow.moveTo(center.x - 3, (int)innerBoundingBox.top + 14);
        rollArrow.lineTo(center.x, (int)innerBoundingBox.top+10);
        rollArrow.moveTo(center.x +3, innerBoundingBox.top+10);
        canvas.drawPath(rollArrow, markerPaint);

        String rollText = String.valueOf(rollDegree);
        double rollTextWidth = textPaint.measureText(rollText);
        canvas.drawText(rollText, (float)(center.x - rollTextWidth/2), innerBoundingBox.top+textHeight+2, textPaint);

        canvas.restore();
        canvas.save();
        canvas.rotate(180, center.x, center.y);
        for (int i = -180; i< 180; i+=10) {
            if (i % 30 == 0) {
                String rollString = String.valueOf(i*-1);
                float rollStringWidth = textPaint.measureText(rollString);
                PointF  rollStringCenter = new PointF(center.x-rollStringWidth/2, innerBoundingBox.top+1+textHeight);
                canvas.drawText(rollString, rollStringCenter.x, rollStringCenter.y,textPaint);
            } else {
                canvas.drawLine(center.x, (int)innerBoundingBox.top, center.x, (int)innerBoundingBox.top+5, markerPaint);
            }
            canvas.rotate(10, center.x, center.y);
        }
        canvas.restore();

        canvas.save();
        canvas.rotate(-1*(bearing), px, py);
        double increment = 22.5;

        for (double i = 0; i<360; i+= increment) {
            CompassDirection cd = CompassDirection.values()[(int)(i/22.5)];
            String headString = cd.toString();

            float headStringWidth = textPaint.measureText(headString);
            PointF headStringCenter = new PointF(center.x - headStringWidth/2, boundingBox.top+1+textHeight);

            if (i % increment == 0) {
                canvas.drawText(headString, headStringCenter.x, headStringCenter.y, textPaint);
            }
            else
                canvas.drawLine(center.x, (int)boundingBox.top, center.x, (int)boundingBox.top+3, markerPaint);

            canvas.rotate((int)increment, center.x, center.y);
        }
        canvas.restore();

        RadialGradient glassShader = new RadialGradient(px, py, (int)innerRadius, glassGradientColors, glassGradientPositions, Shader.TileMode.CLAMP);
        Paint glassPaint = new Paint();
        glassPaint.setShader(glassShader);

        canvas.drawOval(innerBoundingBox, glassPaint);

        canvas.drawOval(boundingBox, circlePaint);
        circlePaint.setStrokeWidth(2);
        canvas.drawOval(innerBoundingBox, circlePaint);
    }

    private float bearing;
    private float pitch;
    private float roll;

    public void setBearing(float bearing) {
        this.bearing = bearing;
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }

    public float getBearing() {
        return bearing;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }

    public float getPitch() {
        return pitch;
    }

    public void setRoll(float roll) {
        this.roll = roll;
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
    }

    public float getRoll() {
        return roll;
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(final AccessibilityEvent event) {
        super.dispatchPopulateAccessibilityEvent(event);

        if (isShown()) {
            String bearingStr = String.valueOf(bearing);
            if (bearingStr.length() > AccessibilityEvent.MAX_TEXT_LENGTH)
                bearingStr = bearingStr.substring(0, AccessibilityEvent.MAX_TEXT_LENGTH);
            event.getText().add(bearingStr);
            return true;
        }
        else
            return false;
    }
}
