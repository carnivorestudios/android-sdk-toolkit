package fr.voxeet.sdk.sample.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.voxeet.sdk.core.VoxeetSdk;
import com.voxeet.sdk.core.preferences.VoxeetPreferences;
import com.voxeet.sdk.events.sdk.SocketConnectEvent;
import com.voxeet.sdk.events.sdk.SocketStateChangeEvent;
import com.voxeet.sdk.json.UserInfo;
import com.voxeet.sdk.json.internal.MetadataHolder;
import com.voxeet.toolkit.activities.VoxeetAppCompatActivity;
import com.voxeet.toolkit.controllers.VoxeetToolkit;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.webrtc.CodecDescriptorFactory;
import org.webrtc.MediaCodecVideoHelperFactory;
import org.webrtc.VideoCodecType;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.codlab.simplepromise.Promise;
import eu.codlab.simplepromise.solve.ErrorPromise;
import eu.codlab.simplepromise.solve.PromiseExec;
import eu.codlab.simplepromise.solve.Solver;
import fr.voxeet.sdk.sample.R;
import fr.voxeet.sdk.sample.application.SampleApplication;
import fr.voxeet.sdk.sample.main_screen.UserAdapter;
import fr.voxeet.sdk.sample.main_screen.UserItem;
import fr.voxeet.sdk.sample.users.UsersHelper;

public class MainActivity extends VoxeetAppCompatActivity implements UserAdapter.UserClickListener {

    private static final int RECORD_AUDIO_RESULT = 0x20;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.join_conf_text)
    EditText joinConfEditText;

    @Bind(R.id.join_conf)
    protected Button joinConf;

    @Bind(R.id.disconnect)
    protected View disconnect;

    @Bind(R.id.recycler_users)
    protected RecyclerView users;

    @Nullable
    @Bind(R.id.force_test_overlay_switch)
    View force_test_overlay_switch;

    private SampleApplication _application;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        EventBus.getDefault().register(this);

        ButterKnife.bind(this);

        users.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        users.setAdapter(new UserAdapter(this, UsersHelper.USER_ITEMS));

        if(null != force_test_overlay_switch) {
            force_test_overlay_switch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityToTestOverlay.start(MainActivity.this);
                }
            });
        }
    }

    @OnClick(R.id.join_conf)
    public void joinButton() {
        joinCall();
    }

    @OnClick(R.id.disconnect)
    public void onDisconnectClick() {
        VoxeetSdk.user().logout()
                .then(defaultConsume())
                .error(createErrorDump());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case RECORD_AUDIO_RESULT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    joinCall();
                }
                return;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getApplication() instanceof SampleApplication) {
            _application = (SampleApplication) getApplication();
        }
    }

    @Override
    public void onBackPressed() {
        if (null != VoxeetSdk.getInstance() && VoxeetSdk.getInstance().getConferenceService().isLive()) {
            VoxeetSdk.getInstance().getConferenceService().leave()
                    .then(defaultConsume())
                    .error(createErrorDump());
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onUserSelected(UserItem user_item) {
        _application.selectUser(user_item.getUserInfo());
    }

    private void joinCall() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RECORD_AUDIO_RESULT);
        } else {
            String conferenceAlias = joinConfEditText.getText().toString();

            VoxeetToolkit.getInstance().enable(VoxeetToolkit.getInstance().getConferenceToolkit());

            Promise<Boolean> promise = VoxeetToolkit.getInstance().getConferenceToolkit().join(conferenceAlias);

            if (VoxeetSdk.getInstance().getConferenceService().isLive()) {
                VoxeetSdk.getInstance().getConferenceService()
                        .leave()
                        .then(promise)
                        //.then(VoxeetSdk.conference().startVideo())
                        .then(defaultConsume())
                        .error(createErrorDump());
            } else {
                promise//.then(VoxeetSdk.conference().startVideo())
                        .then(defaultConsume())
                        .error(createErrorDump());
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final SocketConnectEvent event) {
        Log.d("MainActivity", "SocketConnectEvent" + event.message());
        joinConf.setEnabled(true);
        disconnect.setVisibility(View.VISIBLE);

        //TODO resume select the current logged user
        ((UserAdapter) users.getAdapter()).setSelected(_application.getCurrentUser());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(SocketStateChangeEvent event) {
        Log.d("MainActivity", "SocketStateChangeEvent " + event.message());

        switch (event.message()) {
            case "CLOSING":
            case "CLOSED":
                joinConf.setEnabled(false);
                disconnect.setVisibility(View.GONE);
                ((UserAdapter) users.getAdapter()).reset();
        }
    }

    @Override
    protected void onConferenceJoinedSuccessEvent() {
        List<UserInfo> external_ids = UsersHelper.getExternalIds(VoxeetPreferences.id());

        VoxeetToolkit.getInstance().getConferenceToolkit()
                .invite(external_ids)
                .then(defaultConsume())
                .error(createErrorDump());

        /*VoxeetSdk.conference()
                .startVideo()
                .then(defaultConsume())
                .error(createErrorDump());*/
    }

    private <TYPE> PromiseExec<TYPE, Object> defaultConsume() {
        return new PromiseExec<TYPE, Object>() {
            @Override
            public void onCall(@Nullable TYPE result, @NonNull Solver<Object> solver) {
                Log.d(TAG, "onCall: promise managed done");
            }
        };
    }

    private ErrorPromise createErrorDump() {
        return new ErrorPromise() {
            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
            }
        };
    }
}
