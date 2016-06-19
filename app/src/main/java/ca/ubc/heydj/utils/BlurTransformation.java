package ca.ubc.heydj.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;

import com.squareup.picasso.Transformation;

import java.lang.ref.WeakReference;

/**
 * Blur transformation that can be applied to bitmaps, to be used with Picasso
 * <p/>
 * Created by Chris Li on 12/12/2015.
 */
public class BlurTransformation implements Transformation {

    private WeakReference<Context> mContext;

    public BlurTransformation(Context context) {
        super();
        mContext = new WeakReference<Context>(context);
    }

    @Override
    public Bitmap transform(Bitmap source) {

        RenderScript rs = RenderScript.create(mContext.get());

        // Create another bitmap that will hold the results of the filter.
        Bitmap blurredBitmap = Bitmap.createBitmap(source);

        // Allocate memory for Renderscript to work with
        Allocation input = Allocation.createFromBitmap(rs, source, Allocation.MipmapControl.MIPMAP_FULL, Allocation.USAGE_SHARED);
        Allocation output = Allocation.createTyped(rs, input.getType());

        // Load up an instance of the specific script that we want to use.
        ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        script.setInput(input);

        // Set the blur radius
        script.setRadius(25);
        // Start the ScriptIntrinisicBlur
        script.forEach(output);

        // Copy the output to the blurred bitmap
        output.copyTo(blurredBitmap);
        source.recycle();

        return blurredBitmap;
    }

    @Override
    public String key() {
        return "blur";
    }
}
