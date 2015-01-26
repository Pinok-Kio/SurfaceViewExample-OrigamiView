package com.serega.origamicontrol;

import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by bobrischev on 26.01.2015.
 */
public class BitmapUtils {

    public static BitmapRegionDecoder getDecoder(Bitmap bitmap){
        byte[] array = bitmapToByteArray(bitmap);
        try {
            return BitmapRegionDecoder.newInstance(array, 0, array.length, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        return out.toByteArray();
    }
}
