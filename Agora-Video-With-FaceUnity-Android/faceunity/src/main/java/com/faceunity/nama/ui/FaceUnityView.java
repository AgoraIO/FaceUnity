package com.faceunity.nama.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.faceunity.nama.FURenderer;
import com.faceunity.nama.IModuleManager;
import com.faceunity.nama.R;
import com.faceunity.nama.entity.Makeup;
import com.faceunity.nama.entity.MakeupEnum;
import com.faceunity.nama.entity.Sticker;
import com.faceunity.nama.entity.StickerEnum;
import com.faceunity.nama.module.IMakeupModule;
import com.faceunity.nama.module.IStickerModule;
import com.faceunity.nama.ui.seekbar.DiscreteSeekBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Richie on 2020.06.20
 */
public class FaceUnityView extends FrameLayout {
    private static final String TAG = "FaceUnityView";
    private IModuleManager mFURenderer;
    private IMakeupModule mMakeupModule;
    private IStickerModule mStickerModule;
    private BeautyControlView beautyControlView;
    private BeautifyBodyControlView bodySlimControlView;
    private RecyclerView rvStickEffect;
    private View mClMakeup;
    private DiscreteSeekBar makeupSeekBar;
    private Map<String, Float> makeupIntensitys;
    private MakeupListAdapter makeupListAdapter;

    public FaceUnityView(@NonNull Context context) {
        this(context, null);
    }

    public FaceUnityView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FaceUnityView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_faceunity, this);
        beautyControlView = view.findViewById(R.id.beauty_control_view);
        bodySlimControlView = view.findViewById(R.id.body_slim_control_view);

        rvStickEffect = view.findViewById(R.id.rv_sticker_effect);
        rvStickEffect.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvStickEffect.setHasFixedSize(true);
        ((SimpleItemAnimator) rvStickEffect.getItemAnimator()).setSupportsChangeAnimations(false);
        final StickerListAdapter stickerListAdapter = new StickerListAdapter(StickerEnum.getStickers());
        stickerListAdapter.setOnItemClickListener(new BaseRecyclerAdapter.OnItemClickListener<Sticker>() {
            @Override
            public void onItemClick(BaseRecyclerAdapter<Sticker> adapter, View view, int position) {
                if (mStickerModule != null) {
                    mStickerModule.selectSticker(adapter.getItem(position));
                }
            }
        });
        rvStickEffect.setAdapter(stickerListAdapter);

        mClMakeup = view.findViewById(R.id.cl_makeup);
        RecyclerView rvMakeup = view.findViewById(R.id.rv_makeup);
        rvMakeup.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvMakeup.setHasFixedSize(true);
        ((SimpleItemAnimator) rvMakeup.getItemAnimator()).setSupportsChangeAnimations(false);
        ArrayList<Makeup> makeupEntities = MakeupEnum.getMakeupEntities();
        makeupListAdapter = new MakeupListAdapter(makeupEntities);
        makeupIntensitys = new HashMap<>(16);
        for (Makeup makeupEntity : makeupEntities) {
            makeupIntensitys.put(makeupEntity.getName(), 1f);
        }
        makeupListAdapter.setOnItemClickListener(new MakeupClickListener());
        rvMakeup.setAdapter(makeupListAdapter);
        makeupSeekBar = view.findViewById(R.id.makeup_seek_bar);
        makeupSeekBar.setOnProgressChangeListener(new MakeupSeekChangedListener());
        makeupSeekBar.setProgress(100);

        CheckGroup checkGroup = view.findViewById(R.id.cg_nav_bar);
        checkGroup.setOnCheckedChangeListener(new CheckGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CheckGroup group, int checkedId) {
                if (checkedId == R.id.cb_face_beauty) {
                    beautyControlView.setVisibility(VISIBLE);
                    bodySlimControlView.setVisibility(GONE);
                    rvStickEffect.setVisibility(GONE);
                    mClMakeup.setVisibility(GONE);
                } else if (checkedId == R.id.cb_sticker) {
                    rvStickEffect.setVisibility(VISIBLE);
                    beautyControlView.setVisibility(GONE);
                    mClMakeup.setVisibility(GONE);
                    bodySlimControlView.setVisibility(GONE);
                    mFURenderer.createStickerModule();
                    mFURenderer.destroyMakeupModule();
                    mFURenderer.destroyBodySlimModule();
                } else if (checkedId == R.id.cb_makeup) {
                    mClMakeup.setVisibility(VISIBLE);
                    beautyControlView.setVisibility(GONE);
                    rvStickEffect.setVisibility(GONE);
                    bodySlimControlView.setVisibility(GONE);
                    mFURenderer.createMakeupModule();
                    mFURenderer.destroyStickerModule();
                    mFURenderer.destroyBodySlimModule();
                } else if (checkedId == R.id.cb_body_slim) {
                    bodySlimControlView.setVisibility(VISIBLE);
                    beautyControlView.setVisibility(GONE);
                    rvStickEffect.setVisibility(GONE);
                    mClMakeup.setVisibility(GONE);
                    mFURenderer.createBodySlimModule();
                    mFURenderer.destroyStickerModule();
                    mFURenderer.destroyMakeupModule();
                }
            }
        });
    }

    public void setModuleManager(FURenderer fuRenderer) {
        mFURenderer = fuRenderer;
        beautyControlView.setFaceBeautyManager(fuRenderer.getFaceBeautyModule());
        mMakeupModule = fuRenderer.getMakeupModule();
        mStickerModule = fuRenderer.getStickerModule();
        bodySlimControlView.setBodySlimModule(fuRenderer.getBodySlimModule());
    }

    private class MakeupClickListener implements BaseRecyclerAdapter.OnItemClickListener<Makeup> {

        @Override
        public void onItemClick(BaseRecyclerAdapter<Makeup> adapter, View view, int position) {
            Makeup item = adapter.getItem(position);
            if (mMakeupModule != null) {
                mMakeupModule.selectMakeup(item);
            }
            float intensity = 1f;
            if (position == 0) {
                makeupSeekBar.setVisibility(INVISIBLE);
            } else {
                makeupSeekBar.setVisibility(VISIBLE);
                intensity = makeupIntensitys.get(item.getName());
                makeupSeekBar.setProgress((int) (100 * intensity));
            }
            if (mMakeupModule != null) {
                mMakeupModule.setMakeupIntensity(intensity);
            }
        }
    }

    private class MakeupSeekChangedListener implements DiscreteSeekBar.OnProgressChangeListener {

        @Override
        public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
            if (!fromUser) {
                return;
            }
            float val = (float) value / 100;
            if (mMakeupModule != null) {
                mMakeupModule.setMakeupIntensity(val);
            }
            Makeup makeup = makeupListAdapter.getSelectedItems().valueAt(0);
            makeupIntensitys.put(makeup.getName(), val);
        }

        @Override
        public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

        }
    }

    public static class MakeupListAdapter extends BaseRecyclerAdapter<Makeup> {

        MakeupListAdapter(@NonNull List<Makeup> data) {
            super(data, R.layout.layout_beauty_recycler);
        }

        @Override
        protected void bindViewHolder(BaseViewHolder viewHolder, Makeup item) {
            viewHolder.setText(R.id.control_recycler_text, item.getName())
                    .setImageResource(R.id.control_recycler_img, item.getIconId());
        }

        @Override
        protected void handleSelectedState(BaseViewHolder viewHolder, Makeup data, boolean selected) {
            super.handleSelectedState(viewHolder, data, selected);
            viewHolder.setBackground(R.id.control_recycler_img, selected ? R.drawable.control_filter_select : android.R.color.transparent);
        }
    }

    public static class StickerListAdapter extends BaseRecyclerAdapter<Sticker> {

        StickerListAdapter(@NonNull List<Sticker> data) {
            super(data, R.layout.layout_sticker_recycler);
        }

        @Override
        protected void bindViewHolder(BaseViewHolder viewHolder, Sticker item) {
            viewHolder.setImageResource(R.id.iv_sticker_icon, item.getIconId());
        }

        @Override
        protected void handleSelectedState(BaseViewHolder viewHolder, Sticker data, boolean selected) {
            super.handleSelectedState(viewHolder, data, selected);
            viewHolder.setBackground(R.id.iv_sticker_icon, selected ? R.drawable.shape_sticker_select : android.R.color.transparent);
        }
    }

}
