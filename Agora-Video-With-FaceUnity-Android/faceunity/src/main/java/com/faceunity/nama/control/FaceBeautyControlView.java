package com.faceunity.nama.control;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.faceunity.core.utils.DecimalUtils;
import com.faceunity.nama.R;
import com.faceunity.nama.base.BaseDelegate;
import com.faceunity.nama.base.BaseListAdapter;
import com.faceunity.nama.base.BaseViewHolder;
import com.faceunity.nama.checkbox.CheckGroup;
import com.faceunity.nama.dialog.ToastHelper;
import com.faceunity.nama.entity.FaceBeautyBean;
import com.faceunity.nama.entity.FaceBeautyFilterBean;
import com.faceunity.nama.entity.ModelAttributeData;
import com.faceunity.nama.infe.AbstractFaceBeautyDataFactory;
import com.faceunity.nama.seekbar.DiscreteSeekBar;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * DESC：
 * Created on 2021/4/26
 */
public class FaceBeautyControlView extends BaseControlView {

    private AbstractFaceBeautyDataFactory mDataFactory;

    /*  美颜、美型 */
    private HashMap<String, ModelAttributeData> mModelAttributeRange;
    private ArrayList<FaceBeautyBean> mSkinBeauty;
    private ArrayList<FaceBeautyBean> mShapeBeauty;
    private int mSkinIndex = 0;
    private int mShapeIndex = 1;
    private BaseListAdapter<FaceBeautyBean> mBeautyAdapter;

    /* 滤镜 */
    private ArrayList<FaceBeautyFilterBean> mFilters;
    private BaseListAdapter<FaceBeautyFilterBean> mFiltersAdapter;


    private RecyclerView recyclerView;
    private DiscreteSeekBar discreteSeekBar;
    private CheckGroup checkGroup;
    private LinearLayout recoverLayout;
    private ImageView recoverImageView;
    private TextView recoverTextView;
    private View lineView;
    private LinearLayout bottomLayout;
    private SwitchCompat switchCompat;

    public FaceBeautyControlView(@NonNull Context context) {
        super(context);
        init();
    }

    public FaceBeautyControlView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FaceBeautyControlView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    /**
     * 给控制绑定FaceBeautyController，数据工厂
     *
     * @param dataFactory IFaceBeautyDataFactory
     */
    public void bindDataFactory(AbstractFaceBeautyDataFactory dataFactory) {
        mDataFactory = dataFactory;
        mModelAttributeRange = dataFactory.getModelAttributeRange();
        mSkinBeauty = dataFactory.getSkinBeauty();
        mShapeBeauty = dataFactory.getShapeBeauty();
        mFilters = dataFactory.getBeautyFilters();
        mFiltersAdapter.setData(mFilters);
        checkGroup.check(View.NO_ID);
    }


    private void init() {
        LayoutInflater.from(mContext).inflate(R.layout.layout_face_beauty_control, this);
        initView();
        initAdapter();
        bindListener();

    }
    // region  init

    private void initView() {
        recyclerView = findViewById(R.id.recycler_view);
        discreteSeekBar = findViewById(R.id.seek_bar);
        checkGroup = findViewById(R.id.beauty_radio_group);
        recoverLayout = findViewById(R.id.lyt_beauty_recover);
        recoverImageView = findViewById(R.id.iv_beauty_recover);
        recoverTextView = findViewById(R.id.tv_beauty_recover);
        lineView = findViewById(R.id.iv_line);
        bottomLayout = findViewById(R.id.fyt_bottom_view);
        switchCompat = findViewById(R.id.switch_compat);
        initHorizontalRecycleView(recyclerView);
    }


    /**
     * 构造Adapter
     */
    private void initAdapter() {
        mFiltersAdapter = new BaseListAdapter<>(
                new ArrayList<>(), new BaseDelegate<FaceBeautyFilterBean>() {
            @Override
            public void convert(int viewType, BaseViewHolder helper, FaceBeautyFilterBean data, int position) {
                helper.setText(R.id.tv_control, data.getDesRes());
                helper.setImageResource(R.id.iv_control, data.getImageRes());
                helper.itemView.setSelected(mDataFactory.getCurrentFilterIndex() == position);
            }

            @Override
            public void onItemClickListener(View view, FaceBeautyFilterBean data, int position) {
                if (mDataFactory.getCurrentFilterIndex() != position) {
                    changeAdapterSelected(mFiltersAdapter, mDataFactory.getCurrentFilterIndex(), position);
                    mDataFactory.setCurrentFilterIndex(position);
                    mDataFactory.onFilterSelected(data.getKey(), data.getIntensity(), data.getDesRes());
                    if (position == 0) {
                        discreteSeekBar.setVisibility(View.INVISIBLE);
                    } else {
                        seekToSeekBar(data.getIntensity(), 0.0, 1.0);
                    }
                }

            }
        }, R.layout.list_item_control_title_image_square);


        mBeautyAdapter = new BaseListAdapter<>(new ArrayList<>(), new BaseDelegate<FaceBeautyBean>() {
            @Override
            public void convert(int viewType, BaseViewHolder helper, FaceBeautyBean data, int position) {
                helper.setText(R.id.tv_control, data.getDesRes());
                double value = mDataFactory.getParamIntensity(data.getKey());
                double stand = mModelAttributeRange.get(data.getKey()).getStandV();
                if (DecimalUtils.doubleEquals(value, stand)) {
                    helper.setImageResource(R.id.iv_control, data.getCloseRes());
                } else {
                    helper.setImageResource(R.id.iv_control, data.getOpenRes());
                }
                boolean isShinSelected = checkGroup.getCheckedCheckBoxId() == R.id.beauty_radio_skin_beauty;
                helper.itemView.setSelected(isShinSelected ? mSkinIndex == position : mShapeIndex == position);
            }

            @Override
            public void onItemClickListener(View view, FaceBeautyBean data, int position) {
                boolean isShinSelected = checkGroup.getCheckedCheckBoxId() == R.id.beauty_radio_skin_beauty;
                if ((isShinSelected && position == mSkinIndex) || (!isShinSelected && position == mShapeIndex)) {
                    return;
                }
                if (isShinSelected) {
                    changeAdapterSelected(mBeautyAdapter, mSkinIndex, position);
                    mSkinIndex = position;
                } else {
                    changeAdapterSelected(mBeautyAdapter, mShapeIndex, position);
                    mShapeIndex = position;
                }
                double value = mDataFactory.getParamIntensity(data.getKey());
                double stand = mModelAttributeRange.get(data.getKey()).getStandV();
                double maxRange = mModelAttributeRange.get(data.getKey()).getMaxRange();
                seekToSeekBar(value, stand, maxRange);
            }
        }, R.layout.list_item_control_title_image_circle);
    }


    /**
     * 绑定监听事件
     */
    @SuppressLint("ClickableViewAccessibility")
    private void bindListener() {
        /*拦截触碰事件*/
        findViewById(R.id.fyt_bottom_view).setOnTouchListener((v, event) -> true);
        /*菜单控制*/
        bindBottomRadioListener();
        /*滑动条控制*/
        bindSeekBarListener();
        /*还原数据*/
        recoverLayout.setOnClickListener((view) -> showDialog(mContext.getString(R.string.dialog_reset_avatar_model), () -> recoverData()));
        /*渲染开关*/
        switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> mDataFactory.enableFaceBeauty(isChecked));
    }


    /**
     * 滚动条绑定事件
     */
    private void bindSeekBarListener() {
        discreteSeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnSimpleProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                double valueF = 1.0 * (value - seekBar.getMin()) / 100;
                if (checkGroup.getCheckedCheckBoxId() == R.id.beauty_radio_skin_beauty) {
                    FaceBeautyBean bean = mSkinBeauty.get(mSkinIndex);
                    double range = mModelAttributeRange.get(bean.getKey()).getMaxRange();
                    double res = valueF * range;
                    double intensity = mDataFactory.getParamIntensity(bean.getKey());
                    if (!DecimalUtils.doubleEquals(res, intensity)) {
                        mDataFactory.updateParamIntensity(bean.getKey(), res);
                        setRecoverEnable(checkFaceSkinChanged());
                        updateBeautyItemUI(mBeautyAdapter.getViewHolderByPosition(mSkinIndex), bean);
                    }
                } else if (checkGroup.getCheckedCheckBoxId() == R.id.beauty_radio_face_shape) {
                    FaceBeautyBean bean = mShapeBeauty.get(mShapeIndex);
                    double range = mModelAttributeRange.get(bean.getKey()).getMaxRange();
                    double res = valueF * range;
                    double intensity = mDataFactory.getParamIntensity(bean.getKey());
                    if (!DecimalUtils.doubleEquals(res, intensity)) {
                        mDataFactory.updateParamIntensity(bean.getKey(), res);
                        setRecoverEnable(checkFaceShapeChanged());
                        updateBeautyItemUI(mBeautyAdapter.getViewHolderByPosition(mShapeIndex), bean);
                    }
                } else if (checkGroup.getCheckedCheckBoxId() == R.id.beauty_radio_filter) {
                    FaceBeautyFilterBean bean = mFilters.get(mDataFactory.getCurrentFilterIndex());
                    if (!DecimalUtils.doubleEquals(bean.getIntensity(), valueF)) {
                        bean.setIntensity(valueF);
                        mDataFactory.updateFilterIntensity(valueF);
                    }
                }
            }
        });
    }

    /**
     * 底部导航栏绑定监听事件，处理RecycleView等相关布局变更
     */
    private void bindBottomRadioListener() {
        checkGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.beauty_radio_skin_beauty || checkedId == R.id.beauty_radio_face_shape) {
                discreteSeekBar.setVisibility(View.VISIBLE);
                recoverLayout.setVisibility(View.VISIBLE);
                lineView.setVisibility(View.VISIBLE);
                switchCompat.setVisibility(View.VISIBLE);
            } else if (checkedId == R.id.beauty_radio_filter) {
                discreteSeekBar.setVisibility((mDataFactory.getCurrentFilterIndex() == 0) ? View.INVISIBLE : View.VISIBLE);
                recoverLayout.setVisibility(View.GONE);
                lineView.setVisibility(View.GONE);
                switchCompat.setVisibility(View.VISIBLE);
            } else if (checkedId == View.NO_ID) {
                mDataFactory.enableFaceBeauty(true);
                switchCompat.setVisibility(View.INVISIBLE);
            }
            if (checkedId == R.id.beauty_radio_skin_beauty) {
                mBeautyAdapter.setData(mSkinBeauty);
                recyclerView.setAdapter(mBeautyAdapter);
                FaceBeautyBean item = mSkinBeauty.get(mSkinIndex);
                double value = mDataFactory.getParamIntensity(item.getKey());
                double stand = mModelAttributeRange.get(item.getKey()).getStandV();
                double maxRange = mModelAttributeRange.get(item.getKey()).getMaxRange();
                seekToSeekBar(value, stand, maxRange);
                setRecoverEnable(checkFaceSkinChanged());
                changeBottomLayoutAnimator(true);
            } else if (checkedId == R.id.beauty_radio_face_shape) {
                mBeautyAdapter.setData(mShapeBeauty);
                recyclerView.setAdapter(mBeautyAdapter);
                FaceBeautyBean item = mShapeBeauty.get(mShapeIndex);
                double value = mDataFactory.getParamIntensity(item.getKey());
                double stand = mModelAttributeRange.get(item.getKey()).getStandV();
                double maxRange = mModelAttributeRange.get(item.getKey()).getMaxRange();
                seekToSeekBar(value, stand, maxRange);
                setRecoverEnable(checkFaceShapeChanged());
                changeBottomLayoutAnimator(true);
            } else if (checkedId == R.id.beauty_radio_filter) {
                recyclerView.setAdapter(mFiltersAdapter);
                recyclerView.scrollToPosition(mDataFactory.getCurrentFilterIndex());
                if (mDataFactory.getCurrentFilterIndex() == 0) {
                    discreteSeekBar.setVisibility(View.INVISIBLE);
                } else {
                    seekToSeekBar(mFilters.get(mDataFactory.getCurrentFilterIndex()).getIntensity(), 0.0, 1.0);
                }
                changeBottomLayoutAnimator(true);
            } else if (checkedId == View.NO_ID) {
                changeBottomLayoutAnimator(false);
                mDataFactory.enableFaceBeauty(true);
            }
        });
    }
    // endregion
    // region  业务处理

    /**
     * 设置滚动条数值
     *
     * @param value Double 结果值
     * @param stand Double 标准值
     * @param range Double 范围区间
     */
    private void seekToSeekBar(double value, double stand, double range) {
        if (stand == 0.5) {
            discreteSeekBar.setMin(-50);
            discreteSeekBar.setMax(50);
            discreteSeekBar.setProgress((int) (value * 100 / range - 50));
        } else {
            discreteSeekBar.setMin(0);
            discreteSeekBar.setMax(100);
            discreteSeekBar.setProgress((int) (value * 100 / range));
        }
        discreteSeekBar.setVisibility(View.VISIBLE);
    }

    /**
     * 更新单项是否为基准值显示
     */
    private void updateBeautyItemUI(BaseViewHolder viewHolder, FaceBeautyBean bean) {
        double value = mDataFactory.getParamIntensity(bean.getKey());
        double stand = mModelAttributeRange.get(bean.getKey()).getStandV();
        if (viewHolder == null) {
            return;
        }
        if (DecimalUtils.doubleEquals(value, stand)) {
            viewHolder.setImageResource(R.id.iv_control, bean.getCloseRes());
        } else {
            viewHolder.setImageResource(R.id.iv_control, bean.getOpenRes());
        }
    }

    /**
     * 重置还原按钮状态
     *
     * @param enable Boolean
     */
    private void setRecoverEnable(Boolean enable) {
        if (enable) {
            recoverImageView.setAlpha(1f);
            recoverTextView.setAlpha(1f);
        } else {
            recoverImageView.setAlpha(0.6f);
            recoverTextView.setAlpha(0.6f);
        }
        recoverLayout.setEnabled(enable);
    }


    /**
     * 遍历美肤数据确认还原按钮是否可以点击
     *
     * @return Boolean
     */
    private boolean checkFaceSkinChanged() {
        FaceBeautyBean bean = mSkinBeauty.get(mSkinIndex);
        double value = mDataFactory.getParamIntensity(bean.getKey());
        double defaultV = mModelAttributeRange.get(bean.getKey()).getDefaultV();
        if (!DecimalUtils.doubleEquals(value, defaultV)) {
            return true;
        }
        for (FaceBeautyBean beautyBean : mSkinBeauty) {
            value = mDataFactory.getParamIntensity(beautyBean.getKey());
            defaultV = mModelAttributeRange.get(beautyBean.getKey()).getDefaultV();
            if (!DecimalUtils.doubleEquals(value, defaultV)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 遍历美型数据确认还原按钮是否可以点击
     *
     * @return Boolean
     */
    private boolean checkFaceShapeChanged() {
        FaceBeautyBean bean = mShapeBeauty.get(mShapeIndex);
        double value = mDataFactory.getParamIntensity(bean.getKey());
        double defaultV = mModelAttributeRange.get(bean.getKey()).getDefaultV();
        if (!DecimalUtils.doubleEquals(value, defaultV)) {
            return true;
        }
        for (FaceBeautyBean beautyBean : mShapeBeauty) {
            value = mDataFactory.getParamIntensity(beautyBean.getKey());
            defaultV = mModelAttributeRange.get(beautyBean.getKey()).getDefaultV();
            if (!DecimalUtils.doubleEquals(value, defaultV)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 还原 美型、美肤数据
     */
    private void recoverData() {
        if (checkGroup.getCheckedCheckBoxId() == R.id.beauty_radio_skin_beauty) {
            recoverData(mSkinBeauty, mSkinIndex);
        } else if (checkGroup.getCheckedCheckBoxId() == R.id.beauty_radio_face_shape) {
            recoverData(mShapeBeauty, mShapeIndex);
        }
    }

    /**
     * 重置数据
     *
     * @param beautyBeans
     * @param currentIndex
     */
    private void recoverData(ArrayList<FaceBeautyBean> beautyBeans, int currentIndex) {
        for (FaceBeautyBean bean : beautyBeans) {
            double intensity = mModelAttributeRange.get(bean.getKey()).getDefaultV();
            mDataFactory.updateParamIntensity(bean.getKey(), intensity);
        }
        FaceBeautyBean data = beautyBeans.get(currentIndex);
        double value = mDataFactory.getParamIntensity(data.getKey());
        double stand = mModelAttributeRange.get(data.getKey()).getStandV();
        double maxRange = mModelAttributeRange.get(data.getKey()).getMaxRange();
        seekToSeekBar(value, stand, maxRange);
        mBeautyAdapter.notifyDataSetChanged();
        setRecoverEnable(false);
    }

    /**
     * 底部动画处理
     *
     * @param isOpen Boolean
     */
    private void changeBottomLayoutAnimator(boolean isOpen) {
        if (isBottomShow == isOpen) {
            return;
        }
        int start = isOpen ? getResources().getDimensionPixelSize(R.dimen.x1) : getResources().getDimensionPixelSize(R.dimen.x268);
        int end = isOpen ? getResources().getDimensionPixelSize(R.dimen.x268) : getResources().getDimensionPixelSize(R.dimen.x1);
        if (bottomLayoutAnimator != null && bottomLayoutAnimator.isRunning()) {
            bottomLayoutAnimator.end();
        }
        bottomLayoutAnimator = ValueAnimator.ofInt(start, end).setDuration(150);
        bottomLayoutAnimator.addUpdateListener(animation -> {
            int height = (int) animation.getAnimatedValue();
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) bottomLayout.getLayoutParams();
            params.height = height;
            bottomLayout.setLayoutParams(params);
            if (onBottomAnimatorChangeListener != null) {
                float showRate = 1.0f * (height - start) / (end - start);
                onBottomAnimatorChangeListener.onBottomAnimatorChangeListener(isOpen ? showRate : 1 - showRate);
            }
            if (DecimalUtils.floatEquals(animation.getAnimatedFraction(), 1.0f) && isOpen) {
                switchCompat.setVisibility(View.VISIBLE);
            }
        });
        bottomLayoutAnimator.start();
        isBottomShow = isOpen;
    }

}
