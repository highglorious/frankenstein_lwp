/*
 * Copyright 2014, FANGYTASK
 * http://fangytask.com
 * FRANKENSTEIN
 */

// no more scrolling version

package com.fangytask.livewallpaper.frank;


import com.fangytask.livewallpaper.frank.R;

//import android.R.color;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
//import android.graphics.PorterDuff;
//import android.graphics.PorterDuff.Mode;
//import android.graphics.PorterDuffColorFilter;
//import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.PowerManager;
//import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
//import android.util.DisplayMetrics;
//import android.view.WindowManager;
import android.util.Log;
//import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.graphics.Bitmap;
//import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import java.lang.Math;
/*
 * This live wallpaper draws mummy poster art like kh guitar
 */
public class franklwp extends WallpaperService {

	public static final String SHARED_PREFS_NAME = "appsettings";
	public static final String SHARED_PREFS_COUNTER = "appcounter";
	//public static final String SHARED_PREFS_PREVTOUCHES = "ptouches";
	//public static final String SHARED_PREFS_REPEAT = "repeat";
	
	//public static final int vDay = 86400000;
	//public static final int vHour = 3600000;
	//public static final int vMin = 60000;
	//SET PERIOD OF COLLECTING TOUCHES
	public static final int mPeriod = 60000;
	public static final int dayPeriod=360;
	private static int   dayCounter=0;
	
	private static final String TAG ="FRANK_TEST";
	
	private static int scaleWidth;
    private static int scaleHeight;
    
    private static int MscaleWidth;
    private static int MscaleHeight;
    //created from landscape sprites!!!
    private static int landscapeTextureScaleWidth;
    private static int landscapeTextureScaleHeight;
    //created from portrait sprites!!
    private static int portraitTextureScaleWidth;
    private static int portraitTextureScaleHeight;
    //single
    private static int singleTextureScaleWidth;
    private static int singleTextureScaleHeight;
    
    
    // CONSTANT HEIGHT OF TEXTURE FOR SCALEFACTOR
    //must be 1600px NOT SPRITE TEXTURE
    private static final float textureHeight=1600;
    //number of frames sprite
    private static final int frameCount=10;
    
    
    //LAND SPRITE DIMENSIONS 
    private static final int landscapeSpriteWidth=479;
    private static final int landscapeSpriteHeight=140;
    private static int landscapeSpriteScaleWidth;
    private static int landscapeSpriteScaleHeight;
    //PORT SPRITE DIMENSIONS 
    private static final int portraitSpriteWidth=126;
    private static final int portraitSpriteHeight=914;
    private static int portraitSpriteScaleWidth;
    private static int portraitSpriteScaleHeight;
  //SINGLE SPRITE DIMENSIONS 
    private static final int singleSpriteWidth=187;
    private static final int singleSpriteHeight=127;
    private static int singleSpriteScaleWidth;
    private static int singleSpriteScaleHeight;
    //XY POSITION FOR LANDSCAPE SPRITE (LEFT-TOP)
    private static int xLand;
    private static int yLand;
    //XY POSITION FOR PORTRAIT SPRITE (LEFT-TOP)
    private static int xPort;
    private static int yPort;
    //XY POSITION FOR SINGLE SPRITE (LEFT-TOP)
    private static int xSingle;
    private static int ySingle;
    
   //SET STRAT COUNTER TO APPEAR MASKS
    private static final int mRan1=2;
    private static final int mRan2=7;
    private static final int mRan3=16;
    private static final int mRan4=25;
    //SET COUNTER TO APPEAR SINGLE SPRITE
    private static final int sRan=9;
    
    //CONSTANTS POSITION SPRITE 0..1
    //LAND
    private static final float xL=0.27f;
    private static final float yL=1.0f; // 0-top 1-bottom
    //PORT
    private static final float xP=0.0f; //0-left 1-right
    private static final float yP=0.1f; 
    //SINGLE
    private static final float xS=0.89f;
    private static final float yS=0.1f;
    

    
    private static Bitmap srcTemp;
    
    private static Bitmap portraitArt; //argb8888
    private static Bitmap landscapeArt;
    
    private static Bitmap landscapeSpriteTexture;
    private static Bitmap portraitSpriteTexture;
    private static Bitmap singleSpriteTexture;
    
    private static Bitmap landscapeMask1;//alpha8
    private static Bitmap landscapeMask2;
    private static Bitmap landscapeMask3;
    private static Bitmap landscapeMask4;
    
    private static boolean checkBox;
    private static int agingSpeed=0;
    
    private static int brightness;
    private static float contrast;
    
    private static int   vCounter=0;
    
    private static int   sPosit1=0;
    private static int   sPosit2=0;
    private static float mCurrTouch=0; 
     
    // AVERAGE TOUCHES TRESHOLD
    private static final int mAvgTouch=10;
    
    private static boolean fStart=true;
   
    private final Handler mHandler = new Handler();    

//------------------------------------------------------------------------------------
// return new engine
//------------------------------------------------------------------------------------
    @Override
    public Engine onCreateEngine() {
        return new VintageEngine();
    }
    
    
//------------------------------------------------------------------------------------
// Vintage Engine here
//------------------------------------------------------------------------------------
    class VintageEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener {
 	
    	// initialize	
        private final Paint mPaint = new Paint();
        private final Paint maskPaint = new Paint();
        private final Paint artPaint = new Paint();
        
        private final Rect sRectDst = new Rect();
        private final Rect sRectSrc = new Rect();
        
       // private float mOffset;
       
       // private boolean mVisible=true;
       // private boolean isScreenOn;
        private boolean isChecked=true;
        
       // private float ratio;
        
        private int xheight;
        private int xwidth;
             
        private SharedPreferences mPrefs;
        
        private int CurrentPosterIndex=0;
                                                //R.drawable.16:9, R.drawable.3:2
        private int[] portraitArtId  = new int[] {R.drawable.m169v, R.drawable.m32v};
        private int[] landscapeArtId  = new int[] {R.drawable.m169h, R.drawable.m32h};
        
        private int[] lSpriteTextureId   = new int[] {R.drawable.landtex, R.drawable.landtex};
        private int[] pSpriteTextureId   = new int[] {R.drawable.porttex, R.drawable.porttex};
        private int[] sSpriteTextureId   = new int[] {R.drawable.singtex, R.drawable.singtex};
        
        private int[] landscapeMaskId1 = new int[] {R.drawable.mask1, R.drawable.mask1};
        private int[] landscapeMaskId2 = new int[] {R.drawable.mask2, R.drawable.mask2};
        private int[] landscapeMaskId3 = new int[] {R.drawable.mask3, R.drawable.mask3};
        private int[] landscapeMaskId4 = new int[] {R.drawable.mask4, R.drawable.mask4};
        
//------------------------------------------------------------------------------------
// clock updater
//------------------------------------------------------------------------------------
        private final Runnable mUpdateTimeTask = new Runnable() {
            public void run() {
            	
        	if(checkBox) {
            dayCounter++;
	         if(dayCounter==dayPeriod) {	        	 
	        	 if(mCurrTouch > mAvgTouch) { 
	        	      vCounter++;
            		//save counter
                	Editor editor = mPrefs.edit();
                    editor.putInt(SHARED_PREFS_COUNTER, vCounter);
                    editor.commit();
	        	 }
	        	 	//clear touches
	        	 	mCurrTouch=0;
            		dayCounter=0;
	         	}

        	//Log.i(TAG, "TIME_TASK");
        	//Log.i(TAG, "DAYCOUNT" + dayCounter);
        	//Log.v(TAG, "VCOUNT" + vCounter);
            	//drawframe only for test
            	//drawFrame();
            	//end
        	}
            	mHandler.removeCallbacks(mUpdateTimeTask);
            	mHandler.postDelayed(mUpdateTimeTask, mPeriod*agingSpeed);//set period of task
        }
      };     
        
 //----------------------------------------------------------------------------------
 //Constructor engine
 //----------------------------------------------------------------------------------
        VintageEngine() {
              	
        	//portraitArt set paint filter for scaled bitmaps
            final Paint pfilter = artPaint;
            pfilter.setAntiAlias(true); //used when drawing - no effect
            pfilter.setFilterBitmap(true); //use bilinear filtr - take effect
        	//pfilter.setDither(true);  //used when blitting - no effect        	
            //pfilter.setAlpha(255);
          
            //mask set paint filter for scaled bitmap
        	Paint mfilter = maskPaint;
        	mfilter.setAntiAlias(true); //used when drawing - no effect
            mfilter.setFilterBitmap(true); //use bilinear filtr - take effect
            //mfilter.setDither(true);  //used when blitting - no effect
        	//mfilter.setAlpha(0); // initialize alpha
            mfilter.setColor(Color.WHITE);
        	
        	//xfer.setColor(Color.WHITE);
        	//xfer.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        	//xfer.setColorFilter(new PorterDuffColorFilter(Color.WHITE,Mode.DST_IN));
        	
            /*
            // Create a Paint to draw the lines for our cube
            final Paint paint = mPaint;
            paint.setColor(0xffffffff);
            paint.setAntiAlias(true);
            paint.setStrokeWidth(2);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStyle(Paint.Style.STROKE);
            paint.setTextSize(60);
            */
            brightness=0;
            contrast=1.0f;
            
           // mStartTime = SystemClock.elapsedRealtime();
            
            mPrefs = franklwp.this.getSharedPreferences(SHARED_PREFS_NAME, 0);
            mPrefs.registerOnSharedPreferenceChangeListener(this);
            onSharedPreferenceChanged(mPrefs, null);
            
        }
//------------------------------------------------------------------------------------
//start engine
//------------------------------------------------------------------------------------
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            
//            Log.i(TAG, "CREATE_ENGINE");
            //load saved  counter
            vCounter = mPrefs.getInt(SHARED_PREFS_COUNTER, 0);
           
//            Log.i(TAG, "LOAD_PREFS");
//            Log.i(TAG, "VCOUNT "+vCounter);
            //Log.i(TAG, "PREVTOUCH "+mPrevTouch);
            // By default we don't get touch events, so enable them.
            setTouchEventsEnabled(true);
            //setOffsetNotificationsEnabled(false); //on api 15(4.0.3)
            
        }
//-----------------------------------------------------------------------------------
// stop engine
//-----------------------------------------------------------------------------------
        @Override
        public void onDestroy() {
            super.onDestroy();
//            Log.i(TAG, "DESTROY_ENGINE");
//            Log.i(TAG, "SAVECOUNT "+vCounter);
            //Log.i(TAG, "SAVEPREVT "+mPrevTouch);
            //Log.i(TAG, "SAVECURRT "+mCurrTouch);
            
            //Editor editor = mPrefs.edit();
            //editor.putInt(SHARED_PREFS_COUNTER, vCounter);
            //editor.putFloat(SHARED_PREFS_PREVTOUCHES, mPrevTouch);
            //editor.commit();
            
            mHandler.removeCallbacks(mUpdateTimeTask);
          
        }
//------------------------------------------------------------------------------------
// method called when lwp is come hidden or visible
//------------------------------------------------------------------------------------        
        @Override
        public void onVisibilityChanged(boolean visible) {
        	
 //           Log.i(TAG, "VISIBLE_CHANGED");
  
         PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
         
         if(!pm.isScreenOn()) { 
  //      	 Log.i(TAG, "SCREEN_OFF");
        	 drawFrame();      	 
         }
         
         if (isPreview() && visible) {
 //       	 Log.i(TAG, "PREVIEW_DRAW");
        	 drawFrame();
         }
         
        //redraw when aging setting change  	
            if(!isPreview() && (isChecked!=checkBox)) {
            	drawFrame();
            	isChecked=checkBox;
 //           	Log.i(TAG, "CHECKBOX_DRAW");
            }
            
          //  Log.i(TAG, "VISIBLE" + visible);
          // if (!mVisible) {
        //	   drawFrame();
         //  }
         //  		mVisible=visible;
            
        }
//------------------------------------------------------------------------------------
// loading images method
//------------------------------------------------------------------------------------
   void PosterLoader(){
	   
	   calculateRatio(xwidth,xheight);
	   
       //sprite textures
       srcTemp=decodeSampledBitmapFromResource(getResources(), lSpriteTextureId[CurrentPosterIndex],MscaleWidth,MscaleHeight,0);
       landscapeSpriteTexture=Bitmap.createScaledBitmap(srcTemp,landscapeTextureScaleWidth,landscapeTextureScaleHeight, true);
       
       srcTemp=decodeSampledBitmapFromResource(getResources(), pSpriteTextureId[CurrentPosterIndex],MscaleWidth,MscaleHeight,0);
       portraitSpriteTexture=Bitmap.createScaledBitmap(srcTemp, portraitTextureScaleWidth, portraitTextureScaleHeight, true);
       
       srcTemp=decodeSampledBitmapFromResource(getResources(), sSpriteTextureId[CurrentPosterIndex],MscaleWidth,MscaleHeight,0);
       singleSpriteTexture=Bitmap.createScaledBitmap(srcTemp, singleTextureScaleWidth, singleTextureScaleHeight, true);
	   
	   //load masks
       srcTemp=decodeSampledBitmapFromResource(getResources(), landscapeMaskId1[CurrentPosterIndex],MscaleWidth,MscaleHeight,0);
       srcTemp=Bitmap.createScaledBitmap(srcTemp, MscaleWidth, MscaleHeight, true);
       landscapeMask1=srcTemp.extractAlpha();
       //srcTemp.recycle();
       //srcTemp=null;
       
       srcTemp=decodeSampledBitmapFromResource(getResources(), landscapeMaskId2[CurrentPosterIndex],MscaleWidth,MscaleHeight,0);
       srcTemp=Bitmap.createScaledBitmap(srcTemp, MscaleWidth, MscaleHeight, true);
       landscapeMask2=srcTemp.extractAlpha();
       //srcTemp.recycle();
       //srcTemp=null
       
       srcTemp=decodeSampledBitmapFromResource(getResources(), landscapeMaskId3[CurrentPosterIndex],MscaleWidth,MscaleHeight,0);
       srcTemp=Bitmap.createScaledBitmap(srcTemp, MscaleWidth, MscaleHeight, true);
       landscapeMask3=srcTemp.extractAlpha();
       //srcTemp.recycle();
       //srcTemp=null;
       
       srcTemp=decodeSampledBitmapFromResource(getResources(), landscapeMaskId4[CurrentPosterIndex],MscaleWidth,MscaleHeight,0);
       srcTemp=Bitmap.createScaledBitmap(srcTemp, MscaleWidth, MscaleHeight, true);
       landscapeMask4=srcTemp.extractAlpha();
       //srcTemp.recycle();
       //srcTemp=null;
       Log.i(TAG, "MASKS LOADED");
       
	   // portrait bitmap
       srcTemp=decodeSampledBitmapFromResource(getResources(), portraitArtId[CurrentPosterIndex],scaleHeight,scaleWidth,0);
       portraitArt=Bitmap.createScaledBitmap(srcTemp, scaleHeight, scaleWidth, true);
       //srcTemp.recycle();
       //srcTemp=null;
       
       // landscape bitmaps
       srcTemp=decodeSampledBitmapFromResource(getResources(), landscapeArtId[CurrentPosterIndex],scaleWidth,scaleHeight,0);             
       landscapeArt=Bitmap.createScaledBitmap(srcTemp, scaleWidth, scaleHeight, true);               
       srcTemp.recycle();
       srcTemp=null;

 //      Log.i(TAG,"IMG_LOADED");
       
   }
        
//------------------------------------------------------------------------------------
//method first calling after surface created and each time when change orientation
//------------------------------------------------------------------------------------
        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            // store the center of the surface, so we can draw the cube in the right spot
            
//            Log.i(TAG, "SURF_CHANGED");
            
            xheight=height;
            xwidth=width;
            
            //executes one time when first created surface in change surface method
            //calculate ratio to choose one of three bitmaps closer to ratio screen
            //and calculate new dimensions to scale bitmap for scrolling
            
            if (fStart){         	
            	fStart=false;
                PosterLoader();
                //mHandler.postDelayed(mUpdateTimeTask, mPeriod);
                //Log.i(TAG, "START_HANDLER");
            	} 
            drawFrame(); 
            mHandler.removeCallbacks(mUpdateTimeTask);
            mHandler.postDelayed(mUpdateTimeTask, mPeriod*agingSpeed);
 //           Log.i(TAG, "START_HANDLER");        
        }
//------------------------------------------------------------------------------------
//  method calling immediately after surface is created
//------------------------------------------------------------------------------------        
        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
 //           Log.i(TAG, "SURF_CREATED");
           // mHandler.postDelayed(mUpdateTimeTask, mPeriod);
        }
//------------------------------------------------------------------------------------
// method calling before destroy surface
//------------------------------------------------------------------------------------
        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            
//            Log.i(TAG, "SURF_DESTROYED");

           // mVisible = false;
            mHandler.removeCallbacks(mUpdateTimeTask);
            
          //garbage collector clean recycled bitmaps memory
            System.gc();     
            
        }
//-----------------------------------------------------------------------------------
// method for scrolling lwp
//-----------------------------------------------------------------------------------
        @Override
        public void onOffsetsChanged(float xOffset, float yOffset,
                float xStep, float yStep, int xPixels, int yPixels) {
           
        //	mOffset = xOffset;
        //	pOffset=xPixels;
        	// drawing frame with scrolling
        //	if ((detectOrientation(xwidth,xheight)==0) && (listR==0)) {
        //	drawFrame();
        	//}
 //           Log.i(TAG, "SCROLLING...");
 //           Log.v(TAG, "OFFSET" + mOffset);
           
        }
        

//-----------------------------------------------------------------------------------
// Store the position of the touch event so we can use it for drawing later
//-----------------------------------------------------------------------------------
        @Override
        public void onTouchEvent(MotionEvent event) {
          if(checkBox) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                mCurrTouch+=1;            
            }

 //           Log.v(TAG, "TOUCHES "+mCurrTouch); 
            
           // drawFrame();// for testing vintageengine
            
          }
            super.onTouchEvent(event);
        }

//----------------------------------------------------------------------------------      
// Drawing one frame 
//----------------------------------------------------------------------------------         
  void drawFrame() {
  final SurfaceHolder holder = getSurfaceHolder();
         
  ColorMatrix cm = new ColorMatrix(new float[]
          {
              contrast, 0, 0, 0, brightness,
              0, contrast, 0, 0, brightness,
              0, 0, contrast, 0, brightness,
              0, 0, 0, 1, 0
          });
  artPaint.setColorFilter(new ColorMatrixColorFilter(cm));
  
  sPosit1=spriteTexturePosition(mRan1);
  sPosit2=spriteTexturePosition(mRan2);
  
  if (detectOrientation(xwidth,xheight)==0){
  xSingle=(int) (xheight*xS);
  ySingle=(int) (xwidth*yS);
  
  xLand=(int) ((xheight-landscapeSpriteScaleWidth)*xL);
  yLand=(int)((xwidth-landscapeSpriteScaleHeight)*yL);
  
  xPort=(int)((xheight-portraitSpriteScaleWidth)*xP);
  yPort=(int) ((xwidth-portraitSpriteScaleHeight)*yP);
  
  	} else {
  		
  xSingle=(int) (xwidth*xS);
  ySingle=(int) (xheight*yS);
  
  xLand=(int) ((xwidth-landscapeSpriteScaleWidth)*xL);
  yLand=(int)((xheight-landscapeSpriteScaleHeight)*yL);
  
  xPort=(int)((xwidth-portraitSpriteScaleWidth)*xP);
  yPort=(int)((xheight-portraitSpriteScaleHeight)*yP);
  }
		  
  Canvas c = null;
   try {
    c = holder.lockCanvas();
    if (c != null) {               	
                
	// portrait mode
	if (detectOrientation(xwidth,xheight)==0) {
            		
	c.save();
	c.drawBitmap(portraitArt,0,(int)(-(scaleWidth-xheight)*0.5f),artPaint);//draw on center canvas               		
	if(checkBox) { //draw masked if checkbox on
		//section for rotate and mirror image
		c.rotate(90);
		c.scale(-1, 1);
		c.translate(-xheight, -xwidth);
		
	if(vCounter>=mRan1){c.drawBitmap(landscapeMask1,(int)(-(MscaleWidth-xheight)*0.5f), 0, maskPaint);}
	if(vCounter>=mRan2){c.drawBitmap(landscapeMask2,(int)(-(MscaleWidth-xheight)*0.5f), 0, maskPaint);}
	if(vCounter>=mRan3){c.drawBitmap(landscapeMask3,(int)(-(MscaleWidth-xheight)*0.5f), 0, maskPaint);}
	if(vCounter>=mRan4){c.drawBitmap(landscapeMask4,(int)(-(MscaleWidth-xheight)*0.5f), 0, maskPaint);}
	//c.drawBitmap(portraitMask,0, -(scaleWidth-xheight)*0.5f, maskPaint);
	
	
	//Rect src = new Rect(0,vCounter*landscapeSpriteScaleHeight,landscapeSpriteScaleWidth,vCounter*landscapeSpriteScaleHeight+landscapeSpriteScaleHeight);
	//Rect dst = new Rect (200,xwidth-landscapeSpriteScaleHeight,200+landscapeSpriteScaleWidth,xwidth);
	if(vCounter>mRan1) {
	sRectSrc.set(0,sPosit1*landscapeSpriteScaleHeight,landscapeSpriteScaleWidth,sPosit1*landscapeSpriteScaleHeight+landscapeSpriteScaleHeight);
	sRectDst.set(xLand,yLand,xLand+landscapeSpriteScaleWidth,yLand+landscapeSpriteScaleHeight);
	c.drawBitmap(landscapeSpriteTexture, sRectSrc, sRectDst, null);
	}
	//Rect psrc = new Rect(vCounter*portraitSpriteScaleWidth,0,vCounter*portraitSpriteScaleWidth+portraitSpriteScaleWidth,portraitSpriteScaleHeight);
	//Rect pdst = new Rect (0,50,portraitSpriteScaleWidth,50+portraitSpriteScaleHeight);
	if(vCounter>mRan2) {
	sRectSrc.set(sPosit2*portraitSpriteScaleWidth,0,sPosit2*portraitSpriteScaleWidth+portraitSpriteScaleWidth,portraitSpriteScaleHeight);
	sRectDst.set(xPort,yPort,xPort+portraitSpriteScaleWidth,yPort+portraitSpriteScaleHeight);
	c.drawBitmap(portraitSpriteTexture, sRectSrc, sRectDst, null);
	}
	if(vCounter>=sRan) {
	c.drawBitmap(singleSpriteTexture,xSingle,ySingle, null);
		}
	}
	c.restore();
                
	} else {
             	
	c.save();
	c.drawBitmap(landscapeArt, (int)(-(scaleWidth-xwidth)*0.5f), 0, artPaint);//draw on center
	if(checkBox) { //draw mask if checkbox
	if(vCounter>=mRan1){c.drawBitmap(landscapeMask1, (int)(-(MscaleWidth-xwidth)*0.5f), 0, maskPaint);}
	if(vCounter>=mRan2){c.drawBitmap(landscapeMask2, (int)(-(MscaleWidth-xwidth)*0.5f), 0, maskPaint);}
	if(vCounter>=mRan3){c.drawBitmap(landscapeMask3, (int)(-(MscaleWidth-xwidth)*0.5f), 0, maskPaint);}
	if(vCounter>=mRan4){c.drawBitmap(landscapeMask4, (int)(-(MscaleWidth-xwidth)*0.5f), 0, maskPaint);}

	
	if(vCounter>mRan1) {
	sRectSrc.set(0,sPosit1*landscapeSpriteScaleHeight,landscapeSpriteScaleWidth,sPosit1*landscapeSpriteScaleHeight+landscapeSpriteScaleHeight);
	sRectDst.set(xLand,yLand,xLand+landscapeSpriteScaleWidth,yLand+landscapeSpriteScaleHeight);
	c.drawBitmap(landscapeSpriteTexture, sRectSrc, sRectDst, null);
	}
	if(vCounter>mRan2) {
	sRectSrc.set(sPosit2*portraitSpriteScaleWidth,0,sPosit2*portraitSpriteScaleWidth+portraitSpriteScaleWidth,portraitSpriteScaleHeight);
	sRectDst.set(xPort,yPort,xPort+portraitSpriteScaleWidth,yPort+portraitSpriteScaleHeight);
	c.drawBitmap(portraitSpriteTexture, sRectSrc, sRectDst, null);
	}
	if(vCounter>=sRan) {
	c.drawBitmap(singleSpriteTexture,xSingle,ySingle, null);
	   }
	}
	c.restore();
	}
	
              	
}

                  // drawTouchPoint(c);// for tests
           //Log.v(TAG, "VCOUNT" + vCounter);
                    //Log.v(TAG,"Density" + c.getDensity());
//                    Log.v(TAG,"CanvasHeight "+c.getHeight());
//                    Log.v(TAG,"CanvasWidth "+c.getWidth());
//                    Log.v(TAG,"SHEGHT "+scaleHeight);
//                    Log.v(TAG,"SWIDTH "+scaleWidth);
                    
                    //
                   // Log.v(TAG,"screenHeight"+getDesiredMinimumHeight());
                   // Log.v(TAG,"screenWidth"+getDesiredMinimumWidth());
                   
 //                   Log.v(TAG,"XHEIGHT "+xheight);
 //                   Log.v(TAG,"XWIDTH "+xwidth);
                    //Log.i(TAG,"SCALEF "+scaleFactor);
 //                   Log.i(TAG,"lSTEX_H "+landscapeTextureScaleHeight);
 //                   Log.i(TAG,"lSTEX_W "+landscapeTextureScaleWidth);
 //                   Log.i(TAG,"lSPRITE_H "+landscapeSpriteScaleHeight);
 //                   Log.i(TAG,"lSPRITE_W "+landscapeSpriteScaleWidth);
 //                   Log.i(TAG,"pTEX_H "+portraitTextureScaleHeight);
 //                   Log.i(TAG,"pTEX_W "+portraitTextureScaleWidth);
 //                   Log.i(TAG,"pSPRITE_H "+portraitSpriteScaleHeight);
 //                   Log.i(TAG,"pSPRITE_W "+portraitSpriteScaleWidth);
 //                   Log.i(TAG,"sTEX_H "+singleTextureScaleHeight);
 //                   Log.i(TAG,"sTEX_W "+singleTextureScaleWidth);
 //                   Log.i(TAG,"sSPRITE_H "+singleSpriteScaleHeight);
 //                   Log.i(TAG,"sSPRITE_W "+singleSpriteScaleWidth);
                   
                   // Log.v(TAG,"SPRITE1HEIGHT "+sprite1scaleHeight);
                   // Log.v(TAG,"SPRITE1WIDTH "+sprite1scaleWidth);
                    
                   // Log.v(TAG,"bg_mute " +bg.isMutable());
                  //  Log.v(TAG,"mask_mute " +mask.isMutable());
                  //  Log.v(TAG,"bg_size " +bg.getByteCount());
                  //  Log.v(TAG,"mask_size " +mask.getByteCount());
                  //  Log.v(TAG, "surftrig "+surftrigger);
                  //  Log.v(TAG, "LIST"+listR);
 //                   Log.v(TAG, "CHECK "+checkBox);
 //                   Log.v(TAG, "RATIO "+ratio);
 //                   Log.v(TAG, "POSTERSEL "+CurrentPosterIndex);
                    
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
            }

            // Reschedule the next redraw
         //   mHandler.removeCallbacks(mDrawCube);
           // if (mVisible) {
           //     mHandler.postDelayed(mDrawCube, 2000);
           // }
         mHandler.removeCallbacks(mUpdateTimeTask);
	     mHandler.postDelayed(mUpdateTimeTask, mPeriod*agingSpeed);
        }
        
//--------------------------------------------------------------------------------
// Draw a circle around the current touch point, if any.
//--------------------------------------------------------------------------------
        void drawTouchPoint(Canvas c) {
           // if (mTouchX >=0 && mTouchY >= 0) {
               // c.drawCircle(mTouchX, mTouchY, 80, mPaint);
               // c.drawText(""+Acount,mTouchX,mTouchY-100,mPaint);
                c.drawText(""+vCounter,xwidth/2,xheight/2,mPaint);
           //// }
        }
//-----------------------------------------------------------------------------------
//Calculate downsamples count to use in decodeSampleBitmapFromResource        
//---------------------------------------------------------------------------
        
        public  int calculateInSampleSize(BitmapFactory.Options options,
                int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }

            }
 //           Log.i(TAG, "SampleSize"+inSampleSize);

            return inSampleSize;
        }
        
//----------------------------------------------------------------------------------------
// decoding sampled bitmap without loading in memory
//------------------------------------------------------------------------------------
       public  Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                int reqWidth, int reqHeight, int pixelf) {

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            
            //added new options
          //  options.inDither=true; // dithering when decode 
          //  options.inPreferQualityOverSpeed=true; //only jpeg decoding min API 10
            
            // choose color byte format for bitmap
            if(pixelf == 4){  //4 bytes
            	options.inPreferredConfig=Config.ARGB_8888;
            }
            if(pixelf == 2){ //2 bytes
            	options.inPreferredConfig=Config.RGB_565;	
            }
            if (pixelf == 1){ //1 byte
            	options.inPreferredConfig=Config.ALPHA_8;
            }	
            //ifelse set default
            
            BitmapFactory.decodeResource(res, resId, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // If we're running on Honeycomb or newer, try to use inBitmap
            // if (Utils.hasHoneycomb()) {
            //     addInBitmapOptions(options, cache);
            // }

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            
//            Log.i(TAG, "Finish_decoding!!!");
            
            return BitmapFactory.decodeResource(res, resId, options);
        }
//------------------------------------------------------------------------------------ 
// calculate ratio screen (surface) 
//----------------------------------------------------------------------------------------- 
   public float calculateRatio(float scrWidth, float scrHeight){
	  
	  float ratio=1;
	  float scaleFactor;
	 // float maxpixel;
	  float minpixel;
	  
	   if (scrHeight > scrWidth)        // portrait orientation
	   {
		   ratio=scrHeight/scrWidth;
		  // maxpixel=scrHeight;
		   minpixel=scrWidth;
		   
	   }else{                           // landscape orientation
		   ratio=scrWidth/scrHeight;
		  // maxpixel=scrWidth;
		   minpixel=scrHeight;		   
	   }
	   
	   	scaleFactor=textureHeight/minpixel;
	   	//landscape sprite
	   	landscapeSpriteScaleWidth=Math.round(landscapeSpriteWidth/scaleFactor);
	   	landscapeSpriteScaleHeight=Math.round(landscapeSpriteHeight/scaleFactor);
	   	landscapeTextureScaleWidth=landscapeSpriteScaleWidth;
	   	landscapeTextureScaleHeight=landscapeSpriteScaleHeight*frameCount;
	    //portrait sprite
	   	portraitSpriteScaleWidth=Math.round(portraitSpriteWidth/scaleFactor);
	   	portraitSpriteScaleHeight=Math.round(portraitSpriteHeight/scaleFactor);
	   	portraitTextureScaleWidth=portraitSpriteScaleWidth*frameCount;
	   	portraitTextureScaleHeight=portraitSpriteScaleHeight;
	    //single sprite
	   	singleSpriteScaleWidth=Math.round(singleSpriteWidth/scaleFactor);
	   	singleSpriteScaleHeight=Math.round(singleSpriteHeight/scaleFactor);
	   	singleTextureScaleWidth=singleSpriteScaleWidth;
	   	singleTextureScaleHeight=singleSpriteScaleHeight;
	   
	   if (posterSelecter(ratio)==0) { //use ratio 16:9	   		   
		  scaleWidth=Math.round(16.0f/9.0f*minpixel);
		  scaleHeight=(int)minpixel;
		  //Mscale used 16:9
		  MscaleWidth=scaleWidth;
		  MscaleHeight=scaleHeight;	  
		  CurrentPosterIndex=0;
	      }
	   if (posterSelecter(ratio)==1) {  //use ratio 3:2
		  scaleWidth=Math.round(3.0f/2.0f*minpixel);
		  scaleHeight=(int)minpixel;
		  //Mscale used 16:9
		  MscaleWidth=Math.round(16.0f/9.0f*minpixel);
		  MscaleHeight=(int)minpixel;
		  CurrentPosterIndex=1;
          }
	   
//	     Log.v(TAG,"calcH="+scaleHeight);
//       Log.v(TAG,"calcW="+scaleWidth);
//       Log.v(TAG,"scaleFactor "+scaleFactor);
//       Log.v(TAG, "POSTERSEL "+CurrentPosterIndex);
	   return ratio;  
   }
 //-----------------------------------------------------------------------------------  
 // detectOrientation
 //-----------------------------------------------------------------------------------
 public int detectOrientation(float scrWidth, float scrHeight){
	 int orientation;
	 if (scrHeight > scrWidth) // portrait orientation
	   {
		   orientation=0;
	   }else{                           // landscape orientation
		   orientation=1;
	   }
	 return orientation;
 }
//-----------------------------------------------------------------------------------
// method PosterSelector
// return CurrentPosterIndex
//-----------------------------------------------------------------------------------
 public int posterSelecter(float ratio){
	 int Index=0;
	 if (ratio>=1.6f){ //for ratios 16:9,5:3,16:10 
		 Index=0;}
	 if (ratio<=1.5f){
		 Index=1;      //for ratios 3:2, 4:3
	 } 
	 return Index;
 }
  
//-----------------------------------------------------------------------------------
// Called when preferences has changed
//-----------------------------------------------------------------------------------
@Override
public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

    checkBox = prefs.getBoolean("agingsettings", true);
    String strCover=prefs.getString("coversettings","nitro");
    
    if(strCover.equals("nitro")) {
    	agingSpeed=1; //nitro cover - fast aging
    } else {
    	agingSpeed=3; //poly cover - default aging(slow)
    }
    
//    Log.i(TAG," PREFS_CHANGED");
//    Log.v(TAG," CHECKBOX "+checkBox);
//    Log.v(TAG," COVER "+agingSpeed);
    
    //drawFrame();
    
    //if(!checkBox) {
    //	maskPaint.setAlpha(0);
    	//mHandler.removeCallbacks(mUpdateTimeTask);
   //     }
   // if(checkBox) {
    //	maskPaint.setAlpha(vCounter);
    	//mHandler.postDelayed(mUpdateTimeTask, mPeriod);
   // }
 }

//------------------------------------------------------------------------------------
//Calculate position of sprite in texture map
//------------------------------------------------------------------------------------
public int spriteTexturePosition (int ranCounter) {
	int spriteCount=0;
    	
	 if(vCounter>=ranCounter) {
		 spriteCount=(vCounter-ranCounter)/2; 
	 } 
	 
	 if(spriteCount>=frameCount) {
		 spriteCount=frameCount-1;
	 }
	 
//	Log.i(TAG," SPRITECOUNT"+spriteCount);
	return spriteCount;
}

//------------------------------------------------------------------------------------    
    }  //end of vintage engine
}      //end of class

//------------------------------------------------------------------------------------
