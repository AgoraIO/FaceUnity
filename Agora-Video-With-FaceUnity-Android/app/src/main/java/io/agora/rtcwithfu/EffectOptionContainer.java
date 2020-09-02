package io.agora.rtcwithfu;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

public class EffectOptionContainer extends FrameLayout implements View.OnClickListener {
    public interface OnEffectOptionContainerItemClickListener {
        void onEffectOptionItemClicked(int index, int textResource, boolean selected);
        void onEffectNotSupported(int index, int textResource);
    }

    private static int COLOR_ENABLED = Color.parseColor("#ffcb15");
    private static int COLOR_UNSUPPORTED = Color.parseColor("#857570");

    private AppCompatTextView mEffectOption1;
    private AppCompatTextView mEffectOption2;
    private AppCompatTextView mEffectOption3;
    private AppCompatTextView mEffectOption4;

    private boolean[] mSelectedArray = new boolean[4];
    private AppCompatTextView[] mOptionViews = new AppCompatTextView[mSelectedArray.length];

    private OnEffectOptionContainerItemClickListener mListener;

    public EffectOptionContainer(@NonNull Context context) {
        super(context);
        init();
    }

    public EffectOptionContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(
                R.layout.effect_option_container_layout, this, true);

        mEffectOption1 = findViewById(R.id.effect_option_item_1);
        mEffectOption2 = findViewById(R.id.effect_option_item_2);
        mEffectOption3 = findViewById(R.id.effect_option_item_3);
        mEffectOption4 = findViewById(R.id.effect_option_item_4);

        mEffectOption1.setOnClickListener(this);
        mEffectOption2.setOnClickListener(this);
        mEffectOption3.setOnClickListener(this);
        mEffectOption4.setOnClickListener(this);

        mOptionViews[0] = mEffectOption1;
        mOptionViews[1] = mEffectOption2;
        mOptionViews[2] = mEffectOption3;
        mOptionViews[3] = mEffectOption4;

        for (int i = 0; i < mSelectedArray.length; i++) {
            setViewStyle(mOptionViews[i], mSelectedArray[i], true);
        }
    }

    @Override
    public void onClick(View v) {
        int selected;
        int textRes;
        AppCompatTextView view;
        switch (v.getId()) {
            case R.id.effect_option_item_2:
                textRes = R.string.home_function_name_makeup;
                view = mEffectOption2;
                selected = 1;
                break;
            case R.id.effect_option_item_3:
                textRes = R.string.home_function_name_sticker;
                view = mEffectOption3;
                selected = 2;
                break;
            case R.id.effect_option_item_4:
                textRes = R.string.home_function_name_beauty_body;
                view = mEffectOption4;
                selected = 3;
                break;
            default:
                textRes = R.string.home_function_name_beauty;
                view = mEffectOption1;
                selected = 0;
                break;
        }

        mSelectedArray[selected] = !mSelectedArray[selected];
        setViewStyle(view, mSelectedArray[selected], true);
        if (mListener != null) mListener.onEffectOptionItemClicked(selected, textRes, mSelectedArray[selected]);
    }

    private void setViewStyle(AppCompatTextView view, boolean enabled, boolean supported) {
        if (!supported) {
            view.setTextColor(COLOR_UNSUPPORTED);
        } else if (enabled) {
            view.setTextColor(COLOR_ENABLED);
        } else {
            view.setTextColor(Color.WHITE);
        }
    }

    public void setItemViewStyles(int index, boolean enabled, boolean supported) {
        setViewStyle(mOptionViews[index], enabled, supported);
    }

    public void setEffectOptionItemListener(OnEffectOptionContainerItemClickListener listener) {
        mListener = listener;
    }
}
