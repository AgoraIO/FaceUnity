package io.agora.rtcwithfu.activities;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.util.HashMap;

import io.agora.rtcwithfu.R;
import io.agora.rtcwithfu.view.EffectPanel;

/**
 * Abstract activity which has FU UI, waiting sub activities
 * to implements how to deal with FU rendering parameters.
 */
public abstract class FUBaseActivity extends RTCBaseActivity
        implements View.OnClickListener, View.OnTouchListener {
    private final String TAG = "FUBaseUIActivity";

    protected boolean broadcastingStatus = true;
    protected boolean mirrorVideoPreviewStatus = true;
    protected EffectPanel mEffectPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = 0.7f;
        getWindow().setAttributes(params);
    }

    private HashMap<View, int[]> mTouchPointMap = new HashMap<>();

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                int last_X = (int) event.getRawX();
                int last_Y = (int) event.getRawY();
                mTouchPointMap.put(v, new int[]{last_X, last_Y});
                break;
            case MotionEvent.ACTION_MOVE:
                int[] lastPoint = mTouchPointMap.get(v);
                if (lastPoint != null) {
                    int dx = (int) event.getRawX() - lastPoint[0];
                    int dy = (int) event.getRawY() - lastPoint[1];

                    int left = (int) v.getX() + dx;
                    int top = (int) v.getY() + dy;
                    v.setX(left);
                    v.setY(top);
                    lastPoint[0] = (int) event.getRawX();
                    lastPoint[1] = (int) event.getRawY();

                    mTouchPointMap.put(v, lastPoint);
                    v.getParent().requestLayout();
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_choose_camera:
                onCameraChangeRequested();
                break;
            case R.id.btn_switch_view:
                onViewSwitchRequested();
                break;
            case R.id.btn_mirror_video_preview:
                mirrorVideoPreviewStatus = !mirrorVideoPreviewStatus;
                onMirrorPreviewRequested(mirrorVideoPreviewStatus);
                break;
            case R.id.btn_switch_client_role:
                broadcastingStatus = !broadcastingStatus;
                onChangedToBroadcaster(broadcastingStatus);
                setRoleButtonText();
                break;
        }
    }

    protected void setRoleButtonText() {
        Button button = findViewById(R.id.btn_switch_client_role);
        if (broadcastingStatus) {
            button.setText(R.string.btn_switch_client_role_audience);
        } else {
            button.setText(R.string.btn_switch_client_role_broadcaster);
        }
    }

    abstract protected void onCameraChangeRequested();

    abstract protected void onViewSwitchRequested();

    abstract protected void onMirrorPreviewRequested(boolean mirror);

    abstract protected void onChangedToBroadcaster(boolean broadcaster);
}
