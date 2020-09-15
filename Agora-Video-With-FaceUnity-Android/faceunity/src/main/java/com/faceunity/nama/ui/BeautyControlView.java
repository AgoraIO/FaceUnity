package com.faceunity.nama.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.faceunity.nama.R;
import com.faceunity.nama.entity.Filter;
import com.faceunity.nama.entity.FilterEnum;
import com.faceunity.nama.module.IFaceBeautyModule;
import com.faceunity.nama.ui.dialog.BaseDialogFragment;
import com.faceunity.nama.ui.dialog.ConfirmDialogFragment;
import com.faceunity.nama.ui.seekbar.DiscreteSeekBar;
import com.faceunity.nama.utils.DecimalUtils;

import java.util.List;

/**
 * 美颜 UI
 * Created by tujh on 2017/8/15.
 */
public class BeautyControlView extends FrameLayout implements TouchStateImageView.OnTouchStateListener {
    private static final String TAG = "BeautyControlView";
    private Context mContext;
    private IFaceBeautyModule mFaceBeautyManager;

    private CheckGroup mBottomCheckGroup;
    private FrameLayout mFlFaceSkinItems;
    private BeautyBoxGroup mSkinBeautyBoxGroup;
    private BeautyBoxGroup mShapeBeautyBoxGroup;
    private FrameLayout mFlFaceShapeItems;
    private ImageView mIvRecoverFaceShape;
    private TextView mTvRecoverFaceShape;
    private ImageView mIvRecoverFaceSkin;
    private TextView mTvRecoverFaceSkin;
    private View mBottomView;
    private RecyclerView mFilterRecyclerView;
    private FilterRecyclerAdapter mFilterRecyclerAdapter;
    private DiscreteSeekBar mBeautySeekBar;
    private TouchStateImageView mIvCompare;
    private boolean isShown;
    private List<Filter> mFilters;
    private int mFilterPositionSelect = 1;

    public BeautyControlView(Context context) {
        this(context, null);
    }

    public BeautyControlView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BeautyControlView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mFilters = FilterEnum.getFilters();
        LayoutInflater.from(context).inflate(R.layout.layout_beauty_control, this);
        initView();
    }

    private void initView() {
        mBottomView = findViewById(R.id.cl_bottom_view);
        mBottomView.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        mIvCompare = findViewById(R.id.iv_compare);
        mIvCompare.setOnTouchStateListener(this);
        initViewBottomRadio();
        initViewSkinBeauty();
        initViewFaceShape();
        initViewFilterRecycler();
        initViewTop();
    }

    public void setFaceBeautyManager(@NonNull IFaceBeautyModule faceBeautyManager) {
        mFaceBeautyManager = faceBeautyManager;
        updateViewState();
    }

    private void updateViewState() {
        updateViewSkinBeauty();
        updateViewFaceShape();
        updateViewFilterRecycler();
        hideBottomLayoutAnimator();
    }

    @Override
    public boolean isShown() {
        return isShown;
    }

    private void initViewBottomRadio() {
        mBottomCheckGroup = findViewById(R.id.beauty_radio_group);
        mBottomCheckGroup.setOnCheckedChangeListener(new CheckGroup.OnCheckedChangeListener() {
            private int checkedIdOld = View.NO_ID;

            @Override
            public void onCheckedChanged(CheckGroup group, int checkedId) {
                clickViewBottomRadio(checkedId);
                if (checkedId != View.NO_ID) {
                    if (checkedId == R.id.beauty_radio_skin_beauty) {
                        seekToSeekBar(mSkinBeautyBoxGroup.getCheckedBeautyBoxId());
                    } else if (checkedId == R.id.beauty_radio_face_shape) {
                        seekToSeekBar(mShapeBeautyBoxGroup.getCheckedBeautyBoxId());
                    } else if (checkedId == R.id.beauty_radio_filter) {
                        Float valueObj = BeautyParameterModel.sFilterLevel.get(BeautyParameterModel.STR_FILTER_LEVEL + BeautyParameterModel.sFilter.getName());
                        if (valueObj == null) {
                            valueObj = BeautyParameterModel.DEFAULT_FILTER_LEVEL;
                        }
                        if (mFilterPositionSelect > 0) {
                            seekToSeekBar(valueObj);
                        } else {
                            mBeautySeekBar.setVisibility(INVISIBLE);
                        }
                    }
                }
                if ((checkedId == View.NO_ID || checkedId == checkedIdOld) && checkedIdOld != View.NO_ID) {
                    int endHeight = (int) getResources().getDimension(R.dimen.x1);
                    int startHeight = (int) getResources().getDimension(R.dimen.x268);
                    changeBottomLayoutAnimator(startHeight, endHeight);
                    mIvCompare.setVisibility(INVISIBLE);
                    isShown = false;
                } else if (checkedId != View.NO_ID && checkedIdOld == View.NO_ID) {
                    int startHeight = (int) getResources().getDimension(R.dimen.x1);
                    int endHeight = (int) getResources().getDimension(R.dimen.x268);
                    changeBottomLayoutAnimator(startHeight, endHeight);
                    isShown = true;
                }
                checkedIdOld = checkedId;
            }
        });
    }

    private void updateViewSkinBeauty() {
        onChangeFaceBeautyLevel(R.id.beauty_box_blur_level);
        onChangeFaceBeautyLevel(R.id.beauty_box_color_level);
        onChangeFaceBeautyLevel(R.id.beauty_box_red_level);
        onChangeFaceBeautyLevel(R.id.beauty_box_pouch);
        onChangeFaceBeautyLevel(R.id.beauty_box_nasolabial);
        onChangeFaceBeautyLevel(R.id.beauty_box_eye_bright);
        onChangeFaceBeautyLevel(R.id.beauty_box_tooth_whiten);
    }

    private void initViewSkinBeauty() {
        mFlFaceSkinItems = findViewById(R.id.fl_face_skin_items);
        mIvRecoverFaceSkin = findViewById(R.id.iv_recover_face_skin);
        mIvRecoverFaceSkin.setOnClickListener(new OnMultiClickListener() {
            @Override
            protected void onMultiClick(View v) {
                ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(mContext.getString(R.string.dialog_reset_avatar_model), new BaseDialogFragment.OnClickListener() {
                    @Override
                    public void onConfirm() {
                        // recover params
                        BeautyParameterModel.recoverFaceSkinToDefValue();
                        updateViewSkinBeauty();
                        int checkedId = mSkinBeautyBoxGroup.getCheckedBeautyBoxId();
                        seekToSeekBar(checkedId);
                        setRecoverFaceSkinEnable(false);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                confirmDialogFragment.show(((FragmentActivity) mContext).getSupportFragmentManager(), "ConfirmDialogFragmentReset");
            }
        });
        mTvRecoverFaceSkin = findViewById(R.id.tv_recover_face_skin);

        mSkinBeautyBoxGroup = findViewById(R.id.beauty_group_skin_beauty);
        mSkinBeautyBoxGroup.setOnCheckedChangeListener(new BeautyBoxGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(BeautyBoxGroup group, int checkedId) {
                mBeautySeekBar.setVisibility(INVISIBLE);
                seekToSeekBar(checkedId);
                onChangeFaceBeautyLevel(checkedId);
            }
        });
        checkFaceSkinChanged();
    }

    private void updateViewFaceShape() {
        onChangeFaceBeautyLevel(R.id.beauty_box_eye_enlarge);
        onChangeFaceBeautyLevel(R.id.beauty_box_cheek_thinning);
        onChangeFaceBeautyLevel(R.id.beauty_box_cheek_v);
        onChangeFaceBeautyLevel(R.id.beauty_box_cheek_narrow);
        onChangeFaceBeautyLevel(R.id.beauty_box_cheek_small);
        onChangeFaceBeautyLevel(R.id.beauty_box_intensity_chin);
        onChangeFaceBeautyLevel(R.id.beauty_box_intensity_forehead);
        onChangeFaceBeautyLevel(R.id.beauty_box_intensity_nose);
        onChangeFaceBeautyLevel(R.id.beauty_box_intensity_mouth);
        onChangeFaceBeautyLevel(R.id.beauty_box_canthus);
        onChangeFaceBeautyLevel(R.id.beauty_box_eye_space);
        onChangeFaceBeautyLevel(R.id.beauty_box_eye_rotate);
        onChangeFaceBeautyLevel(R.id.beauty_box_long_nose);
        onChangeFaceBeautyLevel(R.id.beauty_box_philtrum);
        onChangeFaceBeautyLevel(R.id.beauty_box_smile);
    }

    private void initViewFilterRecycler() {
        mFilterRecyclerView = findViewById(R.id.filter_recycle_view);
        mFilterRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mFilterRecyclerView.setAdapter(mFilterRecyclerAdapter = new FilterRecyclerAdapter());
        ((SimpleItemAnimator) mFilterRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void updateViewFilterRecycler() {
        mFilterRecyclerAdapter.setFilter(BeautyParameterModel.sFilter);
        float filterLevel = getFilterLevel(BeautyParameterModel.sFilter.getName());
        if (mFaceBeautyManager != null) {
            mFaceBeautyManager.setFilterName(BeautyParameterModel.sFilter.getName());
            mFaceBeautyManager.setFilterLevel(filterLevel);
        }
    }

    private void initViewFaceShape() {
        mFlFaceShapeItems = findViewById(R.id.fl_face_shape_items);
        mIvRecoverFaceShape = findViewById(R.id.iv_recover_face_shape);
        mIvRecoverFaceShape.setOnClickListener(new OnMultiClickListener() {
            @Override
            protected void onMultiClick(View v) {
                ConfirmDialogFragment confirmDialogFragment = ConfirmDialogFragment.newInstance(mContext.getString(R.string.dialog_reset_avatar_model), new BaseDialogFragment.OnClickListener() {
                    @Override
                    public void onConfirm() {
                        // recover params
                        BeautyParameterModel.recoverFaceShapeToDefValue();
                        updateViewFaceShape();
                        seekToSeekBar(mShapeBeautyBoxGroup.getCheckedBeautyBoxId());
                        setRecoverFaceShapeEnable(false);
                    }

                    @Override
                    public void onCancel() {

                    }
                });
                confirmDialogFragment.show(((FragmentActivity) mContext).getSupportFragmentManager(), "ConfirmDialogFragmentReset");
            }
        });
        mTvRecoverFaceShape = findViewById(R.id.tv_recover_face_shape);
        mShapeBeautyBoxGroup = findViewById(R.id.beauty_group_face_shape);
        mShapeBeautyBoxGroup.setOnCheckedChangeListener(new BeautyBoxGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(BeautyBoxGroup group, int checkedId) {
                mBeautySeekBar.setVisibility(GONE);
                seekToSeekBar(checkedId);
                onChangeFaceBeautyLevel(checkedId);
            }
        });
        checkFaceShapeChanged();
    }

    private void setRecoverFaceShapeEnable(boolean enable) {
        if (enable) {
            mIvRecoverFaceShape.setAlpha(1f);
            mTvRecoverFaceShape.setAlpha(1f);
        } else {
            mIvRecoverFaceShape.setAlpha(0.6f);
            mTvRecoverFaceShape.setAlpha(0.6f);
        }
        mIvRecoverFaceShape.setEnabled(enable);
        mTvRecoverFaceShape.setEnabled(enable);
    }

    private void setRecoverFaceSkinEnable(boolean enable) {
        if (enable) {
            mIvRecoverFaceSkin.setAlpha(1f);
            mTvRecoverFaceSkin.setAlpha(1f);
        } else {
            mIvRecoverFaceSkin.setAlpha(0.6f);
            mTvRecoverFaceSkin.setAlpha(0.6f);
        }
        mIvRecoverFaceSkin.setEnabled(enable);
        mTvRecoverFaceSkin.setEnabled(enable);
    }

    private void onChangeFaceBeautyLevel(int viewId) {
        if (viewId == View.NO_ID) {
            return;
        }
        View view = findViewById(viewId);
        if (view instanceof BaseBeautyBox) {
            boolean open = BeautyParameterModel.isOpen(viewId);
            ((BaseBeautyBox) view).setOpen(open);
        }
        if (mFaceBeautyManager == null) {
            return;
        }
        if (viewId == R.id.beauty_box_blur_level) {
            mFaceBeautyManager.setBlurLevel(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_color_level) {
            mFaceBeautyManager.setColorLevel(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_red_level) {
            mFaceBeautyManager.setRedLevel(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_pouch) {
            mFaceBeautyManager.setRemovePouchStrength(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_nasolabial) {
            mFaceBeautyManager.setRemoveNasolabialFoldsStrength(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_eye_bright) {
            mFaceBeautyManager.setEyeBright(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_tooth_whiten) {
            mFaceBeautyManager.setToothWhiten(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_eye_enlarge) {
            mFaceBeautyManager.setEyeEnlarging(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_cheek_thinning) {
            mFaceBeautyManager.setCheekThinning(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_cheek_narrow) {
            mFaceBeautyManager.setCheekNarrow(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_cheek_v) {
            mFaceBeautyManager.setCheekV(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_cheek_small) {
            mFaceBeautyManager.setCheekSmall(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_intensity_chin) {
            mFaceBeautyManager.setIntensityChin(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_intensity_forehead) {
            mFaceBeautyManager.setIntensityForehead(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_intensity_nose) {
            mFaceBeautyManager.setIntensityNose(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_intensity_mouth) {
            mFaceBeautyManager.setIntensityMouth(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_canthus) {
            mFaceBeautyManager.setIntensityCanthus(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_eye_space) {
            mFaceBeautyManager.setIntensityEyeSpace(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_eye_rotate) {
            mFaceBeautyManager.setIntensityEyeRotate(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_long_nose) {
            mFaceBeautyManager.setIntensityLongNose(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_philtrum) {
            mFaceBeautyManager.setIntensityPhiltrum(BeautyParameterModel.getValue(viewId));
        } else if (viewId == R.id.beauty_box_smile) {
            mFaceBeautyManager.setIntensitySmile(BeautyParameterModel.getValue(viewId));
        }
    }

    private void initViewTop() {
        mBeautySeekBar = findViewById(R.id.beauty_seek_bar);
        mBeautySeekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnSimpleProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                if (!fromUser) {
                    return;
                }

                float valueF = 1.0f * (value - seekBar.getMin()) / 100;
                int checkedCheckBoxId = mBottomCheckGroup.getCheckedCheckBoxId();
                if (checkedCheckBoxId == R.id.beauty_radio_skin_beauty) {
                    int skinCheckedId = mSkinBeautyBoxGroup.getCheckedBeautyBoxId();
                    BeautyParameterModel.setValue(skinCheckedId, valueF);
                    onChangeFaceBeautyLevel(skinCheckedId);
                    checkFaceSkinChanged();
                } else if (checkedCheckBoxId == R.id.beauty_radio_face_shape) {
                    BeautyParameterModel.setValue(mShapeBeautyBoxGroup.getCheckedBeautyBoxId(), valueF);
                    onChangeFaceBeautyLevel(mShapeBeautyBoxGroup.getCheckedBeautyBoxId());
                    checkFaceShapeChanged();
                } else if (checkedCheckBoxId == R.id.beauty_radio_filter) {
                    mFilterRecyclerAdapter.setFilterLevels(valueF);
                }
            }
        });
    }

    private void checkFaceShapeChanged() {
        if (BeautyParameterModel.checkIfFaceShapeChanged()) {
            setRecoverFaceShapeEnable(true);
        } else {
            setRecoverFaceShapeEnable(false);
        }
    }

    private void checkFaceSkinChanged() {
        if (BeautyParameterModel.checkIfFaceSkinChanged()) {
            setRecoverFaceSkinEnable(true);
        } else {
            setRecoverFaceSkinEnable(false);
        }
    }

    /**
     * 点击底部 tab
     *
     * @param viewId
     */
    private void clickViewBottomRadio(int viewId) {
        mFlFaceShapeItems.setVisibility(GONE);
        mFlFaceSkinItems.setVisibility(GONE);
        mFilterRecyclerView.setVisibility(GONE);
        mBeautySeekBar.setVisibility(GONE);
        if (viewId == R.id.beauty_radio_skin_beauty) {
            mFlFaceSkinItems.setVisibility(VISIBLE);
            mIvCompare.setVisibility(VISIBLE);
        } else if (viewId == R.id.beauty_radio_face_shape) {
            mFlFaceShapeItems.setVisibility(VISIBLE);
            int id = mShapeBeautyBoxGroup.getCheckedBeautyBoxId();
            seekToSeekBar(id);
            mIvCompare.setVisibility(VISIBLE);
        } else if (viewId == R.id.beauty_radio_filter) {
            mFilterRecyclerView.setVisibility(VISIBLE);
            mFilterRecyclerAdapter.setFilterProgress();
            mIvCompare.setVisibility(VISIBLE);
        }
    }

    private void seekToSeekBar(float value) {
        seekToSeekBar(value, 0, 100);
    }

    private void seekToSeekBar(float value, int min, int max) {
        mBeautySeekBar.setVisibility(VISIBLE);
        mBeautySeekBar.setMin(min);
        mBeautySeekBar.setMax(max);
        mBeautySeekBar.setProgress((int) (value * (max - min) + min));
    }

    private void seekToSeekBar(int checkedId) {
        if (checkedId == View.NO_ID) {
            return;
        }

        float value = BeautyParameterModel.getValue(checkedId);
        int min = 0;
        int max = 100;
        if (checkedId == R.id.beauty_box_intensity_chin || checkedId == R.id.beauty_box_intensity_forehead
                || checkedId == R.id.beauty_box_intensity_mouth || checkedId == R.id.beauty_box_long_nose
                || checkedId == R.id.beauty_box_eye_space || checkedId == R.id.beauty_box_eye_rotate
                || checkedId == R.id.beauty_box_philtrum) {
            min = -50;
            max = 50;
        }
        seekToSeekBar(value, min, max);
    }

    private void changeBottomLayoutAnimator(final int startHeight, final int endHeight) {
        if (mBottomLayoutAnimator != null && mBottomLayoutAnimator.isRunning()) {
            mBottomLayoutAnimator.end();
        }
        mBottomLayoutAnimator = ValueAnimator.ofInt(startHeight, endHeight).setDuration(150);
        mBottomLayoutAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int height = (int) animation.getAnimatedValue();
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) mBottomView.getLayoutParams();
                params.height = height;
                mBottomView.setLayoutParams(params);
                if (DecimalUtils.floatEquals(animation.getAnimatedFraction(), 1.0f) && startHeight < endHeight) {
                    mIvCompare.setVisibility(VISIBLE);
                }
            }
        });
        mBottomLayoutAnimator.start();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mFaceBeautyManager == null) {
            return false;
        }
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            v.setAlpha(0.7f);
            mFaceBeautyManager.setIsBeautyOn(0);
        } else if (action == MotionEvent.ACTION_UP) {
            v.setAlpha(1f);
            mFaceBeautyManager.setIsBeautyOn(1);
        }
        return true;
    }

    private ValueAnimator mBottomLayoutAnimator;

    public void hideBottomLayoutAnimator() {
        mBottomCheckGroup.check(View.NO_ID);
    }

    public void setFilterLevel(String filterName, float faceBeautyFilterLevel) {
        BeautyParameterModel.sFilterLevel.put(BeautyParameterModel.STR_FILTER_LEVEL + filterName, faceBeautyFilterLevel);
        if (mFaceBeautyManager != null) {
            mFaceBeautyManager.setFilterLevel(faceBeautyFilterLevel);
        }
    }

    public float getFilterLevel(String filterName) {
        String key = BeautyParameterModel.STR_FILTER_LEVEL + filterName;
        Float level = BeautyParameterModel.sFilterLevel.get(key);
        if (level == null) {
            level = BeautyParameterModel.DEFAULT_FILTER_LEVEL;
            BeautyParameterModel.sFilterLevel.put(key, level);
        }
        setFilterLevel(filterName, level);
        return level;
    }

    class FilterRecyclerAdapter extends RecyclerView.Adapter<FilterRecyclerAdapter.HomeRecyclerHolder> {

        @Override
        public FilterRecyclerAdapter.HomeRecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FilterRecyclerAdapter.HomeRecyclerHolder(LayoutInflater.from(mContext).inflate(R.layout.layout_beauty_recycler, parent, false));
        }

        @Override
        public void onBindViewHolder(FilterRecyclerAdapter.HomeRecyclerHolder holder, final int position) {
            final List<Filter> filters = mFilters;
            holder.filterImg.setImageResource(filters.get(position).getIconId());
            holder.filterName.setText(filters.get(position).getDescription());
            if (mFilterPositionSelect == position) {
                holder.filterImg.setBackgroundResource(R.drawable.control_filter_select);
                holder.filterName.setSelected(true);
            } else {
                holder.filterImg.setBackgroundResource(0);
                holder.filterName.setSelected(false);
            }
            holder.itemView.setOnClickListener(new OnMultiClickListener() {
                @Override
                protected void onMultiClick(View v) {
                    mFilterPositionSelect = position;
                    setFilterProgress();
                    notifyDataSetChanged();
                    BeautyParameterModel.sFilter = filters.get(mFilterPositionSelect);
                    if (mFaceBeautyManager != null) {
                        mFaceBeautyManager.setFilterName(BeautyParameterModel.sFilter.getName());
                    }
                    ToastUtil.showNormalToast(mContext, BeautyParameterModel.sFilter.getDescription());
                }
            });
        }

        @Override
        public int getItemCount() {
            return mFilters.size();
        }

        public void setFilterLevels(float filterLevels) {
            if (mFilterPositionSelect >= 0) {
                setFilterLevel(mFilters.get(mFilterPositionSelect).getName(), filterLevels);
            }
        }

        public void setFilter(Filter filter) {
            mFilterPositionSelect = mFilters.indexOf(filter);
        }

        public void setFilterProgress() {
            if (mFilterPositionSelect > 0) {
                seekToSeekBar(getFilterLevel(mFilters.get(mFilterPositionSelect).getName()));
            } else {
                mBeautySeekBar.setVisibility(INVISIBLE);
            }
        }

        class HomeRecyclerHolder extends RecyclerView.ViewHolder {
            ImageView filterImg;
            TextView filterName;

            public HomeRecyclerHolder(View itemView) {
                super(itemView);
                filterImg = itemView.findViewById(R.id.control_recycler_img);
                filterName = itemView.findViewById(R.id.control_recycler_text);
            }
        }
    }

}