package ph.mrjose.homeautomationforglass;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.view.WindowUtils;

import java.io.IOException;
import java.util.List;

public class MenuActivity extends Activity {
    private boolean mAttachedToWindow;
    private boolean mOptionsMenuOpen;
    private boolean mFromLiveCardVoice;
    private boolean mIsFinishing;
    private boolean shouldFinishOnMenuClose;

    private static final int SPEECH_REQUEST = 0;

    private static final String LOG_TAG = "JOSEPH_DEBUG";

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFromLiveCardVoice =
                getIntent().getBooleanExtra(LiveCard.EXTRA_FROM_LIVECARD_VOICE, false);
        if (mFromLiveCardVoice) {
            // When activated by voice from a live card, enable voice commands. The menu
            // will automatically "jump" ahead to the items (skipping the guard phrase
            // that was already said private boolean shouldFinishOnMenuClose;at the live card).
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
                case R.id.action_set_thermostat:
                    handleSetThermostat();
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
            if (shouldFinishOnMenuClose) {
                LiveCardService.refreshLiveCard(this);
                finish();
            }
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
        if (shouldFinishOnMenuClose) {
            LiveCardService.refreshLiveCard(this);
            finish();
        }
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

        shouldFinishOnMenuClose = true;
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
            case R.id.action_set_thermostat:
                handleSetThermostat();
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

    private void handleSetThermostat() {
        shouldFinishOnMenuClose = false;

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        startActivityForResult(intent, SPEECH_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            // Do something with spokenText.
            Log.v(LOG_TAG, spokenText);

            finish();
        }
//        LiveCardService.refreshLiveCard(this);
        super.onActivityResult(requestCode, resultCode, data);
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
