package com.yalantis.ucrop;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yalantis.ucrop.model.AspectRatio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

/**
 * Created by Oleksii Shliama (https://github.com/shliama).
 * <p/>
 * Builder class to ease Intent setup.
 */
public class UCrop {
    public static final int RESULT_ERROR = 96;
    public static final int MIN_SIZE = 10;

    private static final String EXTRA_PREFIX = "com.yalantis.ucrop";

    public static final String EXTRA_INPUT_URI = EXTRA_PREFIX + ".InputUri";
    public static final String EXTRA_OUTPUT_URI = EXTRA_PREFIX + ".OutputUri";
    public static final String EXTRA_OUTPUT_CROP_ASPECT_RATIO = EXTRA_PREFIX + ".CropAspectRatio";
    public static final String EXTRA_OUTPUT_IMAGE_WIDTH = EXTRA_PREFIX + ".ImageWidth";
    public static final String EXTRA_OUTPUT_IMAGE_HEIGHT = EXTRA_PREFIX + ".ImageHeight";
    public static final String EXTRA_OUTPUT_OFFSET_X = EXTRA_PREFIX + ".OffsetX";
    public static final String EXTRA_OUTPUT_OFFSET_Y = EXTRA_PREFIX + ".OffsetY";
    public static final String EXTRA_ERROR = EXTRA_PREFIX + ".Error";

    public static final String EXTRA_ASPECT_RATIO_X = EXTRA_PREFIX + ".AspectRatioX";
    public static final String EXTRA_ASPECT_RATIO_Y = EXTRA_PREFIX + ".AspectRatioY";

    public static final String EXTRA_MAX_SIZE_X = EXTRA_PREFIX + ".MaxSizeX";
    public static final String EXTRA_MAX_SIZE_Y = EXTRA_PREFIX + ".MaxSizeY";

    private final Intent mCropIntent;
    private final Bundle mCropOptionsBundle;

    private UCrop(@NonNull Uri source, @NonNull Uri destination) {
        mCropIntent = new Intent();
        mCropOptionsBundle = new Bundle();
        mCropOptionsBundle.putParcelable(EXTRA_INPUT_URI, source);
        mCropOptionsBundle.putParcelable(EXTRA_OUTPUT_URI, destination);
    }

    /**
     * This method creates new Intent builder and sets both source and destination image URIs.
     *
     * @param source      Uri for image to crop
     * @param destination Uri for saving the cropped image
     */
    public static UCrop of(@NonNull Uri source, @NonNull Uri destination) {
        return new UCrop(source, destination);
    }

    /**
     * Retrieve cropped image Uri from the result Intent
     *
     * @param intent crop result intent
     */

    @SuppressWarnings("deprecation")
    @Nullable
    public static Uri getOutput(@NonNull Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getParcelableExtra(EXTRA_OUTPUT_URI, Uri.class);
        } else {
            return intent.getParcelableExtra(EXTRA_OUTPUT_URI);
        }
    }

    /**
     * Retrieve the width of the cropped image
     *
     * @param intent crop result intent
     */
    public static int getOutputImageWidth(@NonNull Intent intent) {
        return intent.getIntExtra(EXTRA_OUTPUT_IMAGE_WIDTH, -1);
    }

    /**
     * Retrieve the height of the cropped image
     *
     * @param intent crop result intent
     */
    public static int getOutputImageHeight(@NonNull Intent intent) {
        return intent.getIntExtra(EXTRA_OUTPUT_IMAGE_HEIGHT, -1);
    }

    /**
     * Retrieve cropped image aspect ratio from the result Intent
     *
     * @param intent crop result intent
     * @return aspect ratio as a floating point value (x:y) - so it will be 1 for 1:1 or 4/3 for 4:3
     */
    public static float getOutputCropAspectRatio(@NonNull Intent intent) {
        return intent.getFloatExtra(EXTRA_OUTPUT_CROP_ASPECT_RATIO, 0f);
    }

    /**
     * Method retrieves error from the result intent.
     *
     * @param result crop result Intent
     * @return Throwable that could happen while image processing
     */
    @SuppressWarnings("deprecation")
    @Nullable
    public static Throwable getError(@NonNull Intent result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return result.getSerializableExtra(EXTRA_ERROR, Throwable.class);
        } else {
            return (Throwable) result.getSerializableExtra(EXTRA_ERROR);
        }
    }

    /**
     * Set an aspect ratio for crop bounds.
     * User won't see the menu with other ratios options.
     *
     * @param x aspect ratio X
     * @param y aspect ratio Y
     */
    public UCrop withAspectRatio(float x, float y) {
        mCropOptionsBundle.putFloat(EXTRA_ASPECT_RATIO_X, x);
        mCropOptionsBundle.putFloat(EXTRA_ASPECT_RATIO_Y, y);
        return this;
    }

    /**
     * Set an aspect ratio for crop bounds that is evaluated from source image width and height.
     * User won't see the menu with other ratios options.
     */
    public UCrop useSourceImageAspectRatio() {
        mCropOptionsBundle.putFloat(EXTRA_ASPECT_RATIO_X, 0);
        mCropOptionsBundle.putFloat(EXTRA_ASPECT_RATIO_Y, 0);
        return this;
    }

    /**
     * Set maximum size for result cropped image. Maximum size cannot be less then {@value MIN_SIZE}
     *
     * @param width  max cropped image width
     * @param height max cropped image height
     */
    public UCrop withMaxResultSize(@IntRange(from = MIN_SIZE) int width, @IntRange(from = MIN_SIZE) int height) {
        if (width < MIN_SIZE) {
            width = MIN_SIZE;
        }

        if (height < MIN_SIZE) {
            height = MIN_SIZE;
        }

        mCropOptionsBundle.putInt(EXTRA_MAX_SIZE_X, width);
        mCropOptionsBundle.putInt(EXTRA_MAX_SIZE_Y, height);
        return this;
    }

    public UCrop withOptions(@NonNull Options options) {
        mCropOptionsBundle.putAll(options.getOptionBundle());
        return this;
    }

    /**
     * Get Intent to start {@link UCropActivity}
     *
     * @return Intent for {@link UCropActivity}
     */
    public Intent getIntent(@NonNull Context context) {
        mCropIntent.setClass(context, UCropActivity.class);
        mCropIntent.putExtras(mCropOptionsBundle);
        return mCropIntent;
    }

    /**
     * Class that helps to setup advanced configs that are not commonly used.
     * Use it with method {@link #withOptions(Options)}
     */
    public static class Options {

        public static final String EXTRA_COMPRESSION_FORMAT_NAME = EXTRA_PREFIX + ".CompressionFormatName";
        public static final String EXTRA_COMPRESSION_QUALITY = EXTRA_PREFIX + ".CompressionQuality";

        public static final String EXTRA_ALLOWED_GESTURES = EXTRA_PREFIX + ".AllowedGestures";

        public static final String EXTRA_MAX_BITMAP_SIZE = EXTRA_PREFIX + ".MaxBitmapSize";
        public static final String EXTRA_MAX_SCALE_MULTIPLIER = EXTRA_PREFIX + ".MaxScaleMultiplier";
        public static final String EXTRA_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION = EXTRA_PREFIX + ".ImageToCropBoundsAnimDuration";

        public static final String EXTRA_DIMMED_LAYER_COLOR = EXTRA_PREFIX + ".DimmedLayerColor";
        public static final String EXTRA_CIRCLE_DIMMED_LAYER = EXTRA_PREFIX + ".CircleDimmedLayer";

        public static final String EXTRA_SHOW_CROP_FRAME = EXTRA_PREFIX + ".ShowCropFrame";
        public static final String EXTRA_CROP_FRAME_COLOR = EXTRA_PREFIX + ".CropFrameColor";
        public static final String EXTRA_CROP_FRAME_STROKE_WIDTH = EXTRA_PREFIX + ".CropFrameStrokeWidth";

        public static final String EXTRA_SHOW_CROP_GRID = EXTRA_PREFIX + ".ShowCropGrid";
        public static final String EXTRA_CROP_GRID_ROW_COUNT = EXTRA_PREFIX + ".CropGridRowCount";
        public static final String EXTRA_CROP_GRID_COLUMN_COUNT = EXTRA_PREFIX + ".CropGridColumnCount";
        public static final String EXTRA_CROP_GRID_COLOR = EXTRA_PREFIX + ".CropGridColor";
        public static final String EXTRA_CROP_GRID_STROKE_WIDTH = EXTRA_PREFIX + ".CropGridStrokeWidth";

        public static final String EXTRA_TOOL_BAR_COLOR = EXTRA_PREFIX + ".ToolbarColor";
        public static final String EXTRA_STATUS_BAR_COLOR = EXTRA_PREFIX + ".StatusBarColor";
        public static final String EXTRA_UCROP_COLOR_CONTROLS_WIDGET_ACTIVE = EXTRA_PREFIX + ".UcropColorControlsWidgetActive";

        public static final String EXTRA_UCROP_WIDGET_COLOR_TOOLBAR = EXTRA_PREFIX + ".UcropToolbarWidgetColor";
        public static final String EXTRA_UCROP_TITLE_TEXT_TOOLBAR = EXTRA_PREFIX + ".UcropToolbarTitleText";
        public static final String EXTRA_UCROP_WIDGET_CANCEL_DRAWABLE = EXTRA_PREFIX + ".UcropToolbarCancelDrawable";
        public static final String EXTRA_UCROP_WIDGET_CROP_DRAWABLE = EXTRA_PREFIX + ".UcropToolbarCropDrawable";

        public static final String EXTRA_UCROP_LOGO_COLOR = EXTRA_PREFIX + ".UcropLogoColor";

        public static final String EXTRA_HIDE_BOTTOM_CONTROLS = EXTRA_PREFIX + ".HideBottomControls";
        public static final String EXTRA_FREE_STYLE_CROP = EXTRA_PREFIX + ".FreeStyleCrop";

        public static final String EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT = EXTRA_PREFIX + ".AspectRatioSelectedByDefault";
        public static final String EXTRA_ASPECT_RATIO_OPTIONS = EXTRA_PREFIX + ".AspectRatioOptions";

        public static final String EXTRA_UCROP_ROOT_VIEW_BACKGROUND_COLOR = EXTRA_PREFIX + ".UcropRootViewBackgroundColor";


        private final Bundle mOptionBundle;

        public Options() {
            mOptionBundle = new Bundle();
        }

        @NonNull
        public Bundle getOptionBundle() {
            return mOptionBundle;
        }

        /**
         * Set one of {@link android.graphics.Bitmap.CompressFormat} that will be used to save resulting Bitmap.
         */
        public Options setCompressionFormat(@NonNull Bitmap.CompressFormat format) {
            mOptionBundle.putString(EXTRA_COMPRESSION_FORMAT_NAME, format.name());
            return this;
        }

        /**
         * Set compression quality [0-100] that will be used to save resulting Bitmap.
         */
        public Options setCompressionQuality(@IntRange(from = 0) int compressQuality) {
            mOptionBundle.putInt(EXTRA_COMPRESSION_QUALITY, compressQuality);
            return this;
        }

        /**
         * Choose what set of gestures will be enabled on each tab - if any.
         */
        public Options setAllowedGestures(@UCropActivity.GestureTypes int tabScale,
                                          @UCropActivity.GestureTypes int tabRotate,
                                          @UCropActivity.GestureTypes int tabAspectRatio) {
            mOptionBundle.putIntArray(EXTRA_ALLOWED_GESTURES, new int[]{tabScale, tabRotate, tabAspectRatio});
            return this;
        }

        /**
         * This method sets multiplier that is used to calculate max image scale from min image scale.
         *
         * @param maxScaleMultiplier - (minScale * maxScaleMultiplier) = maxScale
         */
        public Options setMaxScaleMultiplier(@FloatRange(from = 1.0, fromInclusive = false) float maxScaleMultiplier) {
            mOptionBundle.putFloat(EXTRA_MAX_SCALE_MULTIPLIER, maxScaleMultiplier);
            return this;
        }

        /**
         * This method sets animation duration for image to wrap the crop bounds
         *
         * @param durationMillis - duration in milliseconds
         */
        public Options setImageToCropBoundsAnimDuration(@IntRange(from = MIN_SIZE) int durationMillis) {
            mOptionBundle.putInt(EXTRA_IMAGE_TO_CROP_BOUNDS_ANIM_DURATION, durationMillis);
            return this;
        }

        /**
         * Setter for max size for both width and height of bitmap that will be decoded from an input Uri and used in the view.
         *
         * @param maxBitmapSize - size in pixels
         */
        public Options setMaxBitmapSize(@IntRange(from = MIN_SIZE) int maxBitmapSize) {
            mOptionBundle.putInt(EXTRA_MAX_BITMAP_SIZE, maxBitmapSize);
            return this;
        }

        /**
         * @param color - desired color of dimmed area around the crop bounds
         */
        public Options setDimmedLayerColor(@ColorInt int color) {
            mOptionBundle.putInt(EXTRA_DIMMED_LAYER_COLOR, color);
            return this;
        }

        /**
         * @param isCircle - set it to true if you want dimmed layer to have an circle inside
         */
        public Options setCircleDimmedLayer(boolean isCircle) {
            mOptionBundle.putBoolean(EXTRA_CIRCLE_DIMMED_LAYER, isCircle);
            return this;
        }

        /**
         * @param show - set to true if you want to see a crop frame rectangle on top of an image
         */
        public Options setShowCropFrame(boolean show) {
            mOptionBundle.putBoolean(EXTRA_SHOW_CROP_FRAME, show);
            return this;
        }

        /**
         * @param color - desired color of crop frame
         */
        public Options setCropFrameColor(@ColorInt int color) {
            mOptionBundle.putInt(EXTRA_CROP_FRAME_COLOR, color);
            return this;
        }

        /**
         * @param width - desired width of crop frame line in pixels
         */
        public Options setCropFrameStrokeWidth(@IntRange(from = 0) int width) {
            mOptionBundle.putInt(EXTRA_CROP_FRAME_STROKE_WIDTH, width);
            return this;
        }

        /**
         * @param show - set to true if you want to see a crop grid/guidelines on top of an image
         */
        public Options setShowCropGrid(boolean show) {
            mOptionBundle.putBoolean(EXTRA_SHOW_CROP_GRID, show);
            return this;
        }

        /**
         * @param count - crop grid rows count.
         */
        public Options setCropGridRowCount(@IntRange(from = 0) int count) {
            mOptionBundle.putInt(EXTRA_CROP_GRID_ROW_COUNT, count);
            return this;
        }

        /**
         * @param count - crop grid columns count.
         */
        public Options setCropGridColumnCount(@IntRange(from = 0) int count) {
            mOptionBundle.putInt(EXTRA_CROP_GRID_COLUMN_COUNT, count);
            return this;
        }

        /**
         * @param color - desired color of crop grid/guidelines
         */
        public Options setCropGridColor(@ColorInt int color) {
            mOptionBundle.putInt(EXTRA_CROP_GRID_COLOR, color);
            return this;
        }

        /**
         * @param width - desired width of crop grid lines in pixels
         */
        public Options setCropGridStrokeWidth(@IntRange(from = 0) int width) {
            mOptionBundle.putInt(EXTRA_CROP_GRID_STROKE_WIDTH, width);
            return this;
        }

        /**
         * @param color - desired resolved color of the toolbar
         */
        public Options setToolbarColor(@ColorInt int color) {
            mOptionBundle.putInt(EXTRA_TOOL_BAR_COLOR, color);
            return this;
        }

        /**
         * @param color - desired resolved color of the statusbar
         */
        public Options setStatusBarColor(@ColorInt int color) {
            mOptionBundle.putInt(EXTRA_STATUS_BAR_COLOR, color);
            return this;
        }

        /**
         * @param color - desired resolved color of the active and selected widget and progress wheel middle line (default is white)
         */
        public Options setActiveControlsWidgetColor(@ColorInt int color) {
            mOptionBundle.putInt(EXTRA_UCROP_COLOR_CONTROLS_WIDGET_ACTIVE, color);
            return this;
        }

        /**
         * @param color - desired resolved color of Toolbar text and buttons (default is darker orange)
         */
        public Options setToolbarWidgetColor(@ColorInt int color) {
            mOptionBundle.putInt(EXTRA_UCROP_WIDGET_COLOR_TOOLBAR, color);
            return this;
        }

        /**
         * @param text - desired text for Toolbar title
         */
        public Options setToolbarTitle(@Nullable String text) {
            mOptionBundle.putString(EXTRA_UCROP_TITLE_TEXT_TOOLBAR, text);
            return this;
        }

        /**
         * @param drawable - desired drawable for the Toolbar left cancel icon
         */
        public Options setToolbarCancelDrawable(@DrawableRes int drawable) {
            mOptionBundle.putInt(EXTRA_UCROP_WIDGET_CANCEL_DRAWABLE, drawable);
            return this;
        }

        /**
         * @param drawable - desired drawable for the Toolbar right crop icon
         */
        public Options setToolbarCropDrawable(@DrawableRes int drawable) {
            mOptionBundle.putInt(EXTRA_UCROP_WIDGET_CROP_DRAWABLE, drawable);
            return this;
        }

        /**
         * @param color - desired resolved color of logo fill (default is darker grey)
         */
        public Options setLogoColor(@ColorInt int color) {
            mOptionBundle.putInt(EXTRA_UCROP_LOGO_COLOR, color);
            return this;
        }

        /**
         * @param hide - set to true to hide the bottom controls (shown by default)
         */
        public Options setHideBottomControls(boolean hide) {
            mOptionBundle.putBoolean(EXTRA_HIDE_BOTTOM_CONTROLS, hide);
            return this;
        }

        /**
         * @param enabled - set to true to let user resize crop bounds (disabled by default)
         */
        public Options setFreeStyleCropEnabled(boolean enabled) {
            mOptionBundle.putBoolean(EXTRA_FREE_STYLE_CROP, enabled);
            return this;
        }

        /**
         * Pass an ordered list of desired aspect ratios that should be available for a user.
         *
         * @param selectedByDefault - index of aspect ratio option that is selected by default (starts with 0).
         * @param aspectRatio       - list of aspect ratio options that are available to user
         */
        public Options setAspectRatioOptions(int selectedByDefault, AspectRatio... aspectRatio) {
            if (selectedByDefault > aspectRatio.length) {
                throw new IllegalArgumentException(String.format(Locale.US,
                        "Index [selectedByDefault = %d] cannot be higher than aspect ratio options count [count = %d].",
                        selectedByDefault, aspectRatio.length));
            }
            mOptionBundle.putInt(EXTRA_ASPECT_RATIO_SELECTED_BY_DEFAULT, selectedByDefault);
            mOptionBundle.putParcelableArrayList(EXTRA_ASPECT_RATIO_OPTIONS, new ArrayList<Parcelable>(Arrays.asList(aspectRatio)));
            return this;
        }

        /**
         * @param color - desired background color that should be applied to the root view
         */
        public Options setRootViewBackgroundColor(@ColorInt int color) {
            mOptionBundle.putInt(EXTRA_UCROP_ROOT_VIEW_BACKGROUND_COLOR, color);
            return this;
        }

        /**
         * Set an aspect ratio for crop bounds.
         * User won't see the menu with other ratios options.
         *
         * @param x aspect ratio X
         * @param y aspect ratio Y
         */
        public Options withAspectRatio(float x, float y) {
            mOptionBundle.putFloat(EXTRA_ASPECT_RATIO_X, x);
            mOptionBundle.putFloat(EXTRA_ASPECT_RATIO_Y, y);
            return this;
        }

        /**
         * Set an aspect ratio for crop bounds that is evaluated from source image width and height.
         * User won't see the menu with other ratios options.
         */
        public Options useSourceImageAspectRatio() {
            mOptionBundle.putFloat(EXTRA_ASPECT_RATIO_X, 0);
            mOptionBundle.putFloat(EXTRA_ASPECT_RATIO_Y, 0);
            return this;
        }

        /**
         * Set maximum size for result cropped image.
         *
         * @param width  max cropped image width
         * @param height max cropped image height
         */
        public Options withMaxResultSize(@IntRange(from = MIN_SIZE) int width, @IntRange(from = MIN_SIZE) int height) {
            mOptionBundle.putInt(EXTRA_MAX_SIZE_X, width);
            mOptionBundle.putInt(EXTRA_MAX_SIZE_Y, height);
            return this;
        }

    }

}
