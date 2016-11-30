package com.example.q.renderscriptexample.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.support.v8.renderscript.ScriptIntrinsicHistogram;
import android.support.v8.renderscript.ScriptIntrinsicLUT;
import android.support.v8.renderscript.Type;
import android.util.Log;

import com.example.q.renderscriptexample.ScriptC_histEq;
import com.xrigau.renderscripting.ScriptC_grayscale;

/**
 * Created by q on 18/04/2016.
 */
public final class RenderScriptImageEdit {

    private RenderScriptImageEdit(){
        //private constructor for utility class
    }

    public static Bitmap blurBitmap(Bitmap bitmap, float radius, Context context) {
        //Create renderscript
        RenderScript rs = RenderScript.create(context);

        //Create allocation from Bitmap
        Allocation allocation = Allocation.createFromBitmap(rs, bitmap);

        Type t = allocation.getType();

        //Create allocation with the same type
        Allocation blurredAllocation = Allocation.createTyped(rs, t);

        //Create script
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        //Set blur radius (maximum 25.0)
        blurScript.setRadius(radius);
        //Set input for script
        blurScript.setInput(allocation);
        //Call script for output allocation
        blurScript.forEach(blurredAllocation);

        //Copy script result into bitmap
        blurredAllocation.copyTo(bitmap);

        //Destroy everything to free memory
        allocation.destroy();
        blurredAllocation.destroy();
        blurScript.destroy();
        t.destroy();

        return bitmap;
    }

    public static Bitmap histogramEqualization(Bitmap image, Context context) {
        //Get image size
        int width = image.getWidth();
        int height = image.getHeight();

        //Create new bitmap
        Bitmap res = image.copy(image.getConfig(), true);

        //Create renderscript
        RenderScript rs = RenderScript.create(context);

        //Create allocation from Bitmap
        Allocation allocationA = Allocation.createFromBitmap(rs, res);

        //Create allocation with same type
        Allocation allocationB = Allocation.createTyped(rs, allocationA.getType());

        ScriptC_grayscale scriptCGrayscale = new ScriptC_grayscale(rs);

        //Create script from rs file.
        ScriptC_histEq histEqScript = new ScriptC_histEq(rs);
        long time = System.nanoTime();
        scriptCGrayscale.forEach_grayscale(allocationA, allocationA);
        //Set size in script
        histEqScript.set_size(width*height);

        //Call the first kernel.
        histEqScript.forEach_root(allocationA, allocationB);

        //Call the rs method to compute the remap array
        histEqScript.invoke_createRemapArray();

        //Call the second kernel
        histEqScript.forEach_remaptoRGB(allocationB, allocationA);

        //Copy script result into bitmap
        allocationA.copyTo(res);
        Log.e("TEST!","time = " + (System.nanoTime() - time));
        //Destroy everything to free memory
        allocationA.destroy();
        allocationB.destroy();
        histEqScript.destroy();
        rs.destroy();

        return res;
    }

    public static Bitmap histogramEqualization2(Bitmap image, Context context) {
        //Get image size
        int width = image.getWidth();
        int height = image.getHeight();

        //Create new bitmap
        Bitmap res = image.copy(image.getConfig(), true);

        //Create renderscript
        RenderScript rs = RenderScript.create(context);

        //Create allocation from Bitmap
        Allocation allocationA = Allocation.createFromBitmap(rs, res);

        //Create allocation with same type
        Allocation allocationB = Allocation.createTyped(rs, allocationA.getType());

        ScriptC_grayscale scriptCGrayscale = new ScriptC_grayscale(rs);


        ScriptIntrinsicHistogram scriptIntrinsicHistogram = ScriptIntrinsicHistogram.create(rs, Element.U8(rs));
        ScriptIntrinsicLUT scriptIntrinsicLUT = ScriptIntrinsicLUT.create(rs, Element.U8(rs));
        long time = System.nanoTime();

        scriptCGrayscale.forEach_grayscale(allocationA, allocationA);
        int[] histo2 = new int[256];
        Allocation histo2Alloc = Allocation.createSized(rs, Element.U32(rs), 256);
        scriptIntrinsicHistogram.setOutput(histo2Alloc);
        scriptIntrinsicHistogram.forEach(allocationA);
        histo2Alloc.copyTo(histo2);
        int size = width * height;
        float sum = 0;
        for (int i = 0; i < 256; i++) {
            sum += histo2[i];
            scriptIntrinsicLUT.setAlpha(i, 255);
            scriptIntrinsicLUT.setRed(i, (int) (sum / size * 255));
            scriptIntrinsicLUT.setGreen(i, (int) (sum / size * 255));
            scriptIntrinsicLUT.setBlue(i, (int) (sum / size * 255));
        }
        scriptIntrinsicLUT.forEach(allocationA, allocationB);
        allocationB.copyTo(res);
        Log.e("TEST!","time = " + (System.nanoTime() - time));

        allocationA.destroy();
        allocationB.destroy();
        scriptIntrinsicHistogram.destroy();
        scriptIntrinsicLUT.destroy();
        rs.destroy();
        return res;
    }


}
