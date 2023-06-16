package com.desay.iwan2.module.share;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.desay.iwan2.module.loadfile.Util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class CutScreen {
	
	private  String savename="sport.png";
    public String getBitmappath(View v)
    {
    	String rangkPath = Util.getSdDirectory() + "/ds_download/";;
    	String savepath="";
    	boolean oksave=false;
    	Bitmap BP=shot(v);     	
        if(BP!=null)
        {	
           File f = null;
           try {
        	   f = new File(rangkPath);
           } catch (Exception e) {
			e.printStackTrace();
           }
           finally
           {
        	   if(!f.exists())
     			  f.mkdir();
           }
        	savepath=rangkPath+"DS_"+savename;
    		oksave=savePic(BP,savepath);
        	if(!oksave) 
        		return "";
        }
		return savepath;
    }
    public Bitmap shot(View v) { 
		v.setDrawingCacheEnabled(true);
		Bitmap bmp = Bitmap.createBitmap(v.getDrawingCache()); 
		v.setDrawingCacheEnabled(false); 
		return bmp;

     } 
	 private boolean savePic(Bitmap b, String strFileName) {
	        FileOutputStream fos = null;
	        try {
	            fos = new FileOutputStream(strFileName);
	            if (null != fos) {
	                b.compress(Bitmap.CompressFormat.PNG, 100, fos);
	                fos.flush();
	                fos.close();
	                return true;
	            }
	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
			return false;

	    } 
}
