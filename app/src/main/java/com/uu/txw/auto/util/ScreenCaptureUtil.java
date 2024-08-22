package com.uu.txw.auto.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.WindowManager;

import com.uu.txw.auto.common.utils.Logger;

import java.nio.ByteBuffer;

public class ScreenCaptureUtil {

    private static final int REQUEST_MEDIA_PROJECTION = 998;
    private static ScreenCaptureUtil mInstance;
    private int resultCode;
    private Intent resultIntent;
    private MediaProjection mp;
    private VirtualDisplay vd;
    private ImageReader mImageReader;

    private ScreenCaptureUtil() {

    }

    public static ScreenCaptureUtil getInstance() {
        if (mInstance != null) {
            return mInstance;
        } else {
            return mInstance = new ScreenCaptureUtil();
        }
    }

    public boolean hasPermission() {
        return resultIntent != null;
    }


    public void requestSCPermission(Activity activity) {
        if (activity != null) {
            activity.startActivityForResult(((MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE)).createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION);
        }
    }

    public boolean onSCPermissionResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == REQUEST_MEDIA_PROJECTION) {
            this.resultCode = resultCode;
            this.resultIntent = result;
            if (this.resultCode == Activity.RESULT_OK) {
                return true;
            }
        }
        return false;
    }

    public Bitmap captureScreen(Context context) throws Exception {
//        L.i("captureScreen");
        if (resultIntent == null) {
            Logger.e("没有截屏权限");
            return null;
        }
        createProjection(context);
        if (vd == null) {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(metrics);
            int windowWidth = metrics.widthPixels;
            int windowHeight = metrics.heightPixels;
            int mScreenDensity = metrics.densityDpi;
            mImageReader = ImageReader.newInstance(windowWidth, windowHeight, 0x1, 2);
            Surface surface = mImageReader.getSurface();
            vd = mp.createVirtualDisplay("screen-mirror",
                    windowWidth, windowHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    surface, null, null);
        }
        Image image = mImageReader.acquireLatestImage();
        Bitmap bitmap = null;
        if (image != null) {
            int width = image.getWidth();
            int height = image.getHeight();
            final Image.Plane[] planes = image.getPlanes();
            final ByteBuffer buffer = planes[0].getBuffer();
            int pixelStride = planes[0].getPixelStride();
            int rowStride = planes[0].getRowStride();
            int rowPadding = rowStride - pixelStride * width;

            bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(buffer);
            image.close();
        }
        return bitmap;
    }

    public void releaseScrCap() {
        if (vd != null) {
            vd.release();
            mImageReader.close();
            vd = null;
        }
    }

    public MediaProjection createProjection(Context context) {
        if (mp == null && resultIntent != null) {
            MediaProjectionManager mpm = (MediaProjectionManager) context.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mp = mpm.getMediaProjection(Activity.RESULT_OK, resultIntent);
        }
        return mp;
    }
}
