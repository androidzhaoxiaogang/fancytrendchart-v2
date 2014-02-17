package xg.fancytrendchart;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Bitmap.Config;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;


public class TrendChartView extends View {
	private final String MAGIC_STRING = "11.11";
	private boolean drawGradient = false;
	
	private int xTitleColor = 0xFF129FCD;
	private int xAxisColor = 0xFF129FCD;
	private int yAxisColor = 0xFF129FCD;
	
	private int xAxisTextColor = 0xFF129FCD;
	private int yAxisTextColor = 0xFF129FCD;
	
	private int xAxisGridColor = 0xFF129FCD;
	private int xAxisGridEndColor = Color.WHITE;
	private int yAxisGridColor = 0xFF129FCD;
	
	private int lineColor = 0xFFFC7722;
	private int lineStartColor = 0x4cffffff;
	private int windowColor = 0xFFFC7722;
	
	private int titleTextSize = 18;
	private int xAxisTextSize = 12;
	
	private int lineStrokeWidth = 4;
	private int titleStrokeWidth = 2;
	private int xAxisStrokeWidth = 2;
	
	private int xGridCount = 7;
	private int yGridCount = 5;
	
	private Rect textBounds;
	private float yGridSpace;
	private float xGridSpace;
	
	private int xAxispadding = 50;
	private int yAxisPadding = 20;
	private int yTextpadding = 5;
	private int windowheight = 30;
	private int windowWidth = 80;
	
	private int touchState;
	private int currentIndex = -1;
	
	private float width;
	private float height;
	private float titleHeight;
	private float startXaxis;
	
	private float[][] vertex;
	
	private Paint paint;
	private Paint xGridPaint;
	private Paint yGridPaint;
	private Paint blockPaint;
	private Paint vertextPaint;
	private Paint circlePaint;
	private Paint windowPaint;
	
	private boolean disableTouch;
	
	private String title;
	private String popString = "AAAAAA";
	private List<String> xValueList;
	private List<String> yValueList;
	
	private Bitmap canvasBitmap;

	public TrendChartView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initTrendChart(context, attrs);
	}

	public TrendChartView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TrendChartView(Context context, String title) {
		super(context, null);
		this.title = title;
	}
	
	/**
	 * Initialize chart data.
	 */
	private void initTrendChart(Context context, AttributeSet attrs) {
		DisplayMetrics dm = getResources().getDisplayMetrics();
		xAxispadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, xAxispadding, dm);
		yAxisPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, yAxisPadding, dm);
		yTextpadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, yTextpadding, dm);
		titleTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, titleTextSize, dm);
		xAxisTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, xAxisTextSize, dm);
		windowheight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, windowheight, dm);
		windowWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, windowWidth, dm);
		
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FancyTrendChart);
		drawGradient = a.getBoolean(R.styleable.FancyTrendChart_drawGradient, drawGradient);
		xAxisColor = a.getInt(R.styleable.FancyTrendChart_xAxisColor, xAxisColor);
		yAxisColor = a.getInt(R.styleable.FancyTrendChart_yAxisColor, yAxisColor);
		xAxisTextColor = a.getInt(R.styleable.FancyTrendChart_xAxisTextColor, xAxisTextColor);
		xAxisTextColor = a.getInt(R.styleable.FancyTrendChart_xAxisTextColor, yAxisTextColor);
		xAxisGridColor = a.getInt(R.styleable.FancyTrendChart_xGridColor, xAxisGridColor);
		yAxisGridColor = a.getInt(R.styleable.FancyTrendChart_yGridColor, yAxisGridColor);
		lineColor = a.getInt(R.styleable.FancyTrendChart_lineColor, lineColor);
		xAxispadding = a.getInt(R.styleable.FancyTrendChart_xAxisPadding, xAxispadding);
		yAxisPadding = a.getInt(R.styleable.FancyTrendChart_yAxisPadding, yAxisPadding);
		a.recycle();
		
		initChartData();
	}
	
	private void initChartData() {
		xValueList = new ArrayList<String>();
		yValueList = new ArrayList<String>();
		
		vertex = new float[xGridCount][2];
		textBounds = new Rect();
		touchState = -2;
		
		paint = new Paint();
		paint.setAntiAlias(true);
		
		xGridPaint = new Paint();
		xGridPaint.setAntiAlias(true);
		
		yGridPaint = new Paint();
		yGridPaint.setAntiAlias(true);
		yGridPaint.setStyle(Paint.Style.STROKE);
		
		blockPaint = new Paint();
		blockPaint.setAntiAlias(true);
		blockPaint.setStyle(Paint.Style.FILL);
		
		vertextPaint = new Paint();
		vertextPaint.setAntiAlias(true);
		vertextPaint.setStyle(Paint.Style.STROKE);
		vertextPaint.setStrokeWidth(lineStrokeWidth);
		vertextPaint.setColor(windowColor);
		
		circlePaint = new Paint();
		circlePaint.setAntiAlias(true);
		circlePaint.setColor(Color.WHITE);
		
		windowPaint = new Paint();
		windowPaint.setAntiAlias(true);
		windowPaint.setStyle(Paint.Style.FILL);
		windowPaint.setColor(windowColor);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		width = this.getWidth();
		height = this.getHeight();
		
		startDrawChart();
		handleTouchEvent(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(disableTouch) {
			return super.onTouchEvent(event);
		}
		
		touchState = event.getAction();
		
		final float lastTouchEventX = event.getX();
		for (int i = 0; i < xGridCount; i++) {
			if (lastTouchEventX <  vertex[i][0] + xGridSpace / 2
					&& lastTouchEventX >  vertex[i][0] - xGridSpace / 2
					&& lastTouchEventX >= vertex[0][0]
					&& lastTouchEventX <= vertex[xGridCount - 1][0]) {
				currentIndex = i;
			}
		}
		
		invalidate();
		
		return true;
	}

	/**
	 * Sets the x axis color.
	 *
	 * @param color the new x axis color
	 */
	public void setXaxisColor(int color) {
		xAxisColor = color;
	}

	/**
	 * Sets the y axis color.
	 *
	 * @param color the new y axis color
	 */
	public void setYaxisColor(int color) {
		yAxisColor = color;
	}
	
	public void setLineStrokeWidth(int width) {
		lineStrokeWidth = width;
	}
	
	public void setTopTile(String title) {
		this.title = title;
	}
	
	public int getTitleTextSize() {
		return titleTextSize;
	}

	public void setTitleTextSize(int titleTextSize) {
		this.titleTextSize = titleTextSize;
	}
	
	public int getTitleStrokeWidth() {
		return titleStrokeWidth;
	}

	public void setTitleStrokeWidth(int titleStrokeWidth) {
		this.titleStrokeWidth = titleStrokeWidth;
	}
	
	public boolean isDisableTouch() {
		return disableTouch;
	}

	public void setDisableTouch(boolean disableTouch) {
		this.disableTouch = disableTouch;
	}
	
	public List<String> getxValueList() {
		return xValueList;
	}

	public void setxValueList(List<String> xValueList) {
		this.xValueList.clear();
		this.xValueList.addAll(xValueList);
		xGridCount = xValueList.size();
	}
	
	public List<String> getyValueList() {
		return yValueList;
	}

	public void setyValueList(List<String> yValueList) {
		this.yValueList.clear();
		this.yValueList.addAll(yValueList);
		yGridCount = yValueList.size();
	}
	
	public String getPopString() {
		return popString;
	}

	public void setPopString(String popString) {
		this.popString = popString;
	}
	
	private void startDrawChart(){
		if(touchState > 0) return;
		canvasBitmap = Bitmap.createBitmap((int)width,
				(int)height, Config.ARGB_8888);
		Canvas canvas = new Canvas(canvasBitmap);
		drawTopTitle(canvas);
		drawXYAxises(canvas);
		drawTrendLine(canvas);
		drawTrendBlock(canvas);
		canvas.save();
	}
	
	private void handleTouchEvent(Canvas canvas) {
		paint.setColor(xAxisColor);
		canvas.drawBitmap(canvasBitmap, 0, 0, paint);
		
		for (int i = 0; i < xGridCount; i++) {
			if(touchState < 0)  {
				canvas.drawCircle(vertex[i][0], height - vertex[i][1], 6, vertextPaint);
				canvas.drawCircle(vertex[i][0], height - vertex[i][1], 4, circlePaint);
			} else {
				drawTouchCircle(i, canvas);
			}
		}
		
		drawPopupWindow(canvas);
	}
	
	private void drawTouchCircle(int i, Canvas canvas) {
		if (i == currentIndex) {
			canvas.drawCircle(vertex[i][0], height - vertex[i][1], 12,vertextPaint);
			canvas.drawCircle(vertex[i][0], height - vertex[i][1], 10,circlePaint);
		} else {
			canvas.drawCircle(vertex[i][0], height - vertex[i][1], 6,vertextPaint);
			canvas.drawCircle(vertex[i][0], height - vertex[i][1], 4,circlePaint);
		}
	}
	
	private void drawTopTitle(Canvas canvas) {
		paint.setColor(xTitleColor);
		paint.setStrokeWidth(titleStrokeWidth);
		paint.setTextSize(titleTextSize);
		paint.getTextBounds(title, 0, 1, textBounds);
		
		titleHeight = textBounds.height();
		canvas.drawText(title, xAxispadding / 2, yAxisPadding / 2 + titleHeight, paint);
	}
	
	private void drawXYAxises(Canvas canvas) {
		drawXaxis(canvas);
		
		drawXGridLines(canvas);
		drawYGridLines(canvas);
	}
	
	private void drawXaxis(Canvas canvas) {
		paint.getTextBounds(MAGIC_STRING, 0, 1, textBounds);
		startXaxis = textBounds.width() + xAxispadding / 2;
		paint.setColor(xAxisColor);
		paint.setStrokeWidth(xAxisStrokeWidth);
		paint.setTextSize(xAxisTextSize);
		canvas.drawLine(startXaxis, height - yAxisPadding, width - xAxispadding/2, 
				height - yAxisPadding, paint); //bottom line
	}
	
	private void drawXGridLines(Canvas canvas) {
		paint.setColor(xAxisColor);
		paint.setStrokeWidth(xAxisStrokeWidth);
		paint.setTextSize(xAxisTextSize);
		paint.getTextBounds(MAGIC_STRING, 0, 1, textBounds);
		
		xGridSpace = (width - xAxispadding / 2 - startXaxis) / (xGridCount - 1);
		float xGridStart = startXaxis;
		float xValueHeight = textBounds.height();
		
		if ( xValueList == null || xValueList.size() < xGridCount) {
			return ;
		}
		
		for (int i = 0; i < xGridCount; i++) {
			if (i == 0) {
				final float textHeight =  height - yAxisPadding + xValueHeight * 2;
				canvas.drawText(xValueList.get(i), startXaxis, textHeight, paint);
			} else if (i == xGridCount - 1) {
				final int length = xValueList.get(i).length();
				paint.getTextBounds(xValueList.get(i), 0, length, textBounds);
				
				final float xValuewidth = textBounds.width();
				final float textHeight = height - yAxisPadding + xValueHeight * 2;
				final float textWidth = xGridStart - xValuewidth;
				
				canvas.drawText(xValueList.get(i), textWidth, textHeight, paint);
			}
			
			LinearGradient gradient = new LinearGradient(xGridStart, 
					height - yAxisPadding, xGridStart, yAxisPadding, 
					xAxisColor, xAxisGridEndColor, Shader.TileMode.MIRROR);
			xGridPaint.setShader(gradient);
			canvas.drawLine(xGridStart, height - yAxisPadding, xGridStart, 
					yAxisPadding + titleHeight, xGridPaint);
			xGridStart += xGridSpace;
		}
	}
	
	private void drawYGridLines(Canvas canvas) {
		yGridSpace = (height - yAxisPadding * 2 - titleHeight) / yGridCount;
		float yGridStart = height - yAxisPadding - yGridSpace;
		
		final PathEffect effects = new DashPathEffect(new float[]{4, 4, 4, 4}, 1);
		final DecimalFormat format = new DecimalFormat("##0.00"); 
		
		final double minYValue = getMinY(yValueList);
		final double maxYValue = getMaxY(yValueList);
		
		String tempValue;
		
		if (yValueList == null || yValueList.size() < yGridCount)  {
			return ;
		}
		
		float yTextWidth;
		float yTextHeight;
		float textWidth;
		float textHeight;
		
		for (int i = 0; i < yGridCount; i++) {
			if (i == 0) {
				tempValue = format.format(minYValue);
				paint.getTextBounds(tempValue, 0, tempValue.length(), textBounds);
				
				yTextWidth = textBounds.width();
				yTextHeight = textBounds.height();
				textWidth = startXaxis - yTextWidth - yTextpadding;
				textHeight = yGridStart + yTextHeight / 2;
				canvas.drawText(tempValue, textWidth, textHeight, paint);
			} else if (i == yGridCount - 1) {
				tempValue = format.format(maxYValue);
				paint.getTextBounds(tempValue, 0, tempValue.length(), textBounds);
				
				yTextWidth = textBounds.width();
				yTextHeight = textBounds.height();
				textWidth = startXaxis - yTextWidth - yTextpadding;
				textHeight = yGridStart + yTextHeight / 2;
				canvas.drawText(tempValue, textWidth, textHeight, paint);
			} else {
				double avg = (maxYValue - minYValue) / (yGridCount - 1);
				tempValue = format.format(minYValue + avg * i); 
				paint.getTextBounds(tempValue, 0, tempValue.length(), textBounds);
				
				yTextWidth = textBounds.width();
				yTextHeight = textBounds.height();
				textWidth = startXaxis - yTextWidth - yTextpadding;
				textHeight = yGridStart + yTextHeight / 2;
				canvas.drawText(tempValue, textWidth, textHeight, paint);
			}
			
			yGridPaint.setColor(yAxisColor);
			yGridPaint.setStrokeWidth(2);
			Path path = new Path();       
	        path.moveTo(startXaxis, yGridStart);  
	        path.lineTo(width - xAxispadding / 2,yGridStart);        
	        yGridPaint.setPathEffect(effects);  
	        canvas.drawPath(path, yGridPaint); 
	        yGridStart -= yGridSpace;
		}
		
		//Initialize shader for trend block background.
		final float blockStart = startXaxis;
		LinearGradient gradient = new LinearGradient(blockStart, height
				- yAxisPadding, blockStart, yAxisPadding + yGridSpace,
				lineStartColor, 0x4cf7d621, Shader.TileMode.MIRROR);
		blockPaint.setShader(gradient);
	}
	
	private void drawTrendLine(Canvas canvas) {
		final float lineStart = startXaxis;
		final int size = Math.min(xGridCount, yGridCount);

		for (int i = 0; i < size; i++) {
			vertex[i][0] = lineStart + i * xGridSpace;
			// relative height
			vertex[i][1] = yAxisPadding + yGridSpace + getPointY(yValueList.get(i));
		}

		paint.setStrokeWidth(lineStrokeWidth);
		paint.setColor(lineColor);
		
		for (int i = 0; i < size; i++) {
			if(i < size - 1) {
				canvas.drawLine(vertex[i][0], height - vertex[i][1], 
						vertex[i+1][0], height - vertex[i+1][1], paint);
			}
		}
	}
	
	private void drawPopupWindow(Canvas canvas) {
		if(touchState > 0) {
			drawWindowRect(canvas);
		}
	}
	
	private void drawWindowRect(Canvas canvas) {
		float left = 0;
		float top = 0;
		float right = 0;
		float bottom = 0;
		
		final float textWidth = paint.measureText(popString);
		
		if (currentIndex == 0) {
			left = startXaxis - 3 * yTextpadding;
		}else if (currentIndex == xGridCount - 1) {
			left = vertex[currentIndex][0] - windowWidth / 5 - xAxispadding;
		}else {
			left = vertex[currentIndex][0] - windowWidth / 5 - xAxispadding / 2;
		}
		
		if (vertex[currentIndex][1] >= (yAxisPadding + yGridSpace * 2 )) {
			top = height - vertex[currentIndex][1] + windowheight - 50;
		}else {
			top = height - vertex[currentIndex][1] - windowheight - 40;
		}
		
		right = left + windowWidth;
		bottom = top + windowheight;
		
		RectF rect = new RectF(left, top, right, bottom);
        canvas.drawRoundRect(rect, 8, 8, windowPaint);
        
        paint.setColor(Color.WHITE);
		final float textLeft = left + ( windowWidth - textWidth) / 2;
        canvas.drawText(popString, textLeft, top + windowheight / 2 + 10, paint);
	}
	
	private void drawTrendBlock(Canvas canvas) {
		for (int i = 0; i < xGridCount - 1; i++) {
			Path path = new Path();
			path.moveTo(vertex[i][0], height - vertex[i][1]);
			path.lineTo(vertex[i][0], height - yAxisPadding);
			path.lineTo(vertex[i + 1][0], height - yAxisPadding);
			path.lineTo(vertex[i + 1][0], height - vertex[i + 1][1]);
			path.close();
			canvas.drawPath(path, blockPaint);
		}
	}
	
	private float getPointY(String valueY) {
		double minYValue = getMinY(yValueList);
		double maxYValue = getMaxY(yValueList);
		float temp = Float.valueOf(valueY);
		
		if(minYValue == maxYValue){
			minYValue = 0;
		}
		float result = (float) (((temp - minYValue) / 
				(maxYValue - minYValue)) * (yGridCount-1) * yGridSpace);
		return result;
	}
	
	private double getMaxY(List<String> yValueList) {
		double max = 0;
		if (!yValueList.isEmpty()) {
		    max = Double.valueOf(yValueList.get(0));
		    for (String value : yValueList) {
		        double temp = Double.valueOf(value);
		        if (temp > max)  {
		            max = temp;
		        }
		    } 
		}
		return max;
	}
	
	private double getMinY(List<String> yValueList) {
		double min = 0;
		if (!yValueList.isEmpty()) {
		    min = Double.valueOf(yValueList.get(0));
		    for (String value : yValueList) {
		        double temp = Double.valueOf(value);
		        if (temp < min)  {
		        	min = temp;
		        }
		    } 
		}
		return min;
	}

}
