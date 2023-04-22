package com.prisyazhnuy.radioplayer;

import android.content.Context;
import android.util.DisplayMetrics;

import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

public class Utils {
    public static float convertDpToPixel(Context context, float dp) {
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    public static CircularProgressDrawable createProgressAnimation(Context context, int strokeWidth, int color, int radius) {
        CircularProgressDrawable animation = new CircularProgressDrawable(context);
        animation.setStrokeWidth(Utils.convertDpToPixel(context, strokeWidth));
        animation.setColorSchemeColors(color);
        animation.setCenterRadius(Utils.convertDpToPixel(context, radius));
        animation.start();
        return animation;
    }
}
