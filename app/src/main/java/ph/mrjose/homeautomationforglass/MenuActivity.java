package ph.mrjose.homeautomationforglass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.view.WindowUtils;

import java.io.IOException;

public class MenuActivity extends Activity {
    private boolean mAttachedToWindow;
    private boolean mOptionsMenuOpen;
    private boolean mFromLiveCardVoice;
    private boolean mIsFinishing;

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFromLiveCardVoice =
                getIntent().getBooleanExtra(LiveCard.EXTRA_FROM_LIVECARD_VOICE, false);
        if (mFromLiveCardVoice) {
            // When activated by voice from a live card, enable voice commands. The menu
            // will automatically "jump" ahead to the items (skipping the guard phrase
            // that was already said at the live card).
            getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        }
    }

    @Override
    public void openOptionsMenu() {
        if(!mOptionsMenuOpen && mAttachedToWindow) {
            mOptionsMenuOpen = true;
            super.openOptionsMenu();
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
        if (!mFromLiveCardVoice) {
            openOptionsMenu();
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (isMyMenu(featureId)) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        if (isMyMenu(featureId)) {
            return !mIsFinishing;
        }
        return super.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (isMyMenu(featureId)) {
            // Handle item selection.
            switch (item.getItemId()) {
                case R.id.action_unlock_door:
                    try {
                        handleUnlockDoor();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                case R.id.action_lock_door:
                    try {
                        handleLockDoor();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                case R.id.action_turn_on_light:
                    try {
                        handleTurnOnLight();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                case R.id.action_turn_off_light:
                    try {
                        handleTurnOffLight();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                case R.id.action_stop:
                    handleStop();
                    return true;
            }
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        super.onPanelClosed(featureId, menu);
        if (isMyMenu(featureId)) {
            mIsFinishing = true;
            LiveCardService.refreshLiveCard(this);
            finish();
        }
    }

    private boolean isMyMenu(int featureId) {
        return featureId == Window.FEATURE_OPTIONS_PANEL ||
                featureId == WindowUtils.FEATURE_VOICE_COMMANDS;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttachedToWindow = false;
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        super.onOptionsMenuClosed(menu);
        mOptionsMenuOpen = false;
        LiveCardService.refreshLiveCard(this);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        boolean handled = true;
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_unlock_door:
                try {
                    handleUnlockDoor();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.action_lock_door:
                try {
                    handleLockDoor();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.action_turn_on_light:
                try {
                    handleTurnOnLight();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.action_turn_off_light:
                try {
                    handleTurnOffLight();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.action_stop:
                handleStop();
                break;
            default:
                handled = super.onOptionsItemSelected(item);
        }

        return handled;
    }

    private void handleUnlockDoor() throws IOException {
        String retrievedData;
        try {
            retrievedData = new RetrieveData().execute(ServerUrl.serverUrl + "/unlock").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LiveCardService.refreshLiveCard(this);
    }

    private void handleLockDoor() throws IOException {
        String retrievedData;
        try {
            retrievedData = new RetrieveData().execute(ServerUrl.serverUrl + "/lock").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LiveCardService.refreshLiveCard(this);
    }

    private void handleTurnOnLight() throws IOException {
//        Toast.makeText(this, "Turn on light selected", Toast.LENGTH_LONG).show();
        String retrievedData;
        try {
            retrievedData = new RetrieveData().execute(ServerUrl.serverUrl + "/lighton").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LiveCardService.refreshLiveCard(this);
    }

    private void handleTurnOffLight() throws IOException {
        String retrievedData;
        try {
            retrievedData = new RetrieveData().execute(ServerUrl.serverUrl + "/lightoff").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LiveCardService.refreshLiveCard(this);
    }

    private void handleStop() {
//        Toast.makeText(this, "Closing app", Toast.LENGTH_SHORT).show();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                stopService(new Intent(MenuActivity.this, LiveCardService.class));
            }
        });

    }
}
