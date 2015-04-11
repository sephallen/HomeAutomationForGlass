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
    private boolean mPreparePanelCalled;
    private boolean menuLight;

    private static final int SPEECH_REQUEST = 0;

    Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFromLiveCardVoice = getIntent().getBooleanExtra(LiveCard.EXTRA_FROM_LIVECARD_VOICE, false);
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
        if (mPreparePanelCalled) {
            getWindow().invalidatePanelMenu(WindowUtils.FEATURE_VOICE_COMMANDS);
        }
        if (!mFromLiveCardVoice) {
            openOptionsMenu();
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (isMyMenu(featureId)) {
            shouldFinishOnMenuClose = true;
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        mPreparePanelCalled = true;
        if (isMyMenu(featureId)) {
            shouldFinishOnMenuClose = true;

            MenuItem menuLightOn = menu.findItem(R.id.action_turn_on_light);
            MenuItem menuLightOff = menu.findItem(R.id.action_turn_off_light);
            if (menuLight) {
                menuLightOn.setVisible(false);
                menuLightOff.setVisible(true);
            } else {
                menuLightOn.setVisible(true);
                menuLightOff.setVisible(false);
            }
            return !mIsFinishing;
        }
        return super.onPreparePanel(featureId, view, menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (isMyMenu(featureId)) {
            shouldFinishOnMenuClose = true;
            // Handle item selection.
            switch (item.getItemId()) {
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
                case R.id.action_turn_on_kettle:
                    try {
                        handleTurnOnKettle();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.action_turn_off_kettle:
                    try {
                        handleTurnOffKettle();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.action_stop:
                    handleStop();
                    break;
            }
        }

        invalidateOptionsMenu();
        getWindow().invalidatePanelMenu(WindowUtils.FEATURE_VOICE_COMMANDS);
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

    private void handleUnlockDoor() throws IOException {
        String retrievedData = null;
        try {
            retrievedData = new RetrieveData().execute(ServerUrl.serverUrl + "/unlock").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, retrievedData, Toast.LENGTH_LONG).show();
        LiveCardService.refreshLiveCard(this);
    }

    private void handleLockDoor() throws IOException {
        String retrievedData = null;
        try {
            retrievedData = new RetrieveData().execute(ServerUrl.serverUrl + "/lock").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, retrievedData, Toast.LENGTH_LONG).show();
        LiveCardService.refreshLiveCard(this);
    }

    private void handleTurnOnLight() throws IOException {
        String retrievedData = null;
        try {
            retrievedData = new RetrieveData().execute(ServerUrl.serverUrl + "/lighton").get();
            menuLight = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, retrievedData, Toast.LENGTH_LONG).show();
        LiveCardService.refreshLiveCard(this);

    }

    private void handleTurnOffLight() throws IOException {
        String retrievedData = null;
        try {
            retrievedData = new RetrieveData().execute(ServerUrl.serverUrl + "/lightoff").get();
            menuLight = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, retrievedData, Toast.LENGTH_LONG).show();
        LiveCardService.refreshLiveCard(this);
    }

    private void handleSetThermostat() {
        shouldFinishOnMenuClose = false;

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a number between 0-30");
        startActivityForResult(intent, SPEECH_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            if (spokenText != null) {
                spokenText = spokenText.replaceAll("[^0-9]", "");

                if (spokenText != "") {
                    String retrievedData;
                    try {
                        retrievedData = new RetrieveData().execute(ServerUrl.serverUrl + "/thermostat/" + spokenText).get();
                        Toast.makeText(this, retrievedData, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "Not a valid temperature", Toast.LENGTH_LONG).show();
                }
            }
            LiveCardService.refreshLiveCard(this);
        }
        finish();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleTurnOnKettle() throws IOException {
        String retrievedData = null;
        try {
            retrievedData = new RetrieveData().execute(ServerUrl.serverUrl + "/relayon").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, retrievedData, Toast.LENGTH_LONG).show();
        LiveCardService.refreshLiveCard(this);
    }

    private void handleTurnOffKettle() throws IOException {
        String retrievedData = null;
        try {
            retrievedData = new RetrieveData().execute(ServerUrl.serverUrl + "/relayoff").get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(this, retrievedData, Toast.LENGTH_LONG).show();
        LiveCardService.refreshLiveCard(this);
    }

    private void handleStop() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                stopService(new Intent(MenuActivity.this, LiveCardService.class));
            }
        });

    }
}
