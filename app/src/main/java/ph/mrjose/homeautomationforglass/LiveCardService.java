package ph.mrjose.homeautomationforglass;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.google.android.glass.timeline.LiveCard;

import org.json.JSONException;
import org.json.JSONObject;

public class LiveCardService extends Service {

    private static final String LIVE_CARD_TAG = "Home Automation For Glass Status";
    private static final String ACTION_STOP = "Stop";
    private static final String ACTION_REFRESH = "Refresh";

    RemoteViews mLiveCardViews;
    LiveCard mLiveCard;

    public static void refreshLiveCard(Context context) {
        Intent intent = new Intent(context, LiveCardService.class);
        intent.setAction(ACTION_REFRESH);
        context.startService(intent);
    }

    public LiveCardService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        if(mLiveCard != null)
            mLiveCard.unpublish();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(mLiveCard == null) {
            mLiveCardViews = new RemoteViews(getPackageName(), R.layout.status_live_card_layout);
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

            mLiveCard.setViews(mLiveCardViews);
            Intent cardActionIntent = new Intent(this, MenuActivity.class);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, cardActionIntent, 0));

            mLiveCard.setVoiceActionEnabled(true);
            mLiveCard.publish(LiveCard.PublishMode.REVEAL);
        } else {
//            if(ACTION_STOP == intent.getAction())
//                stopSelf();
//            else
            if (!ACTION_REFRESH.equals(intent.getAction()))
                mLiveCard.navigate();
        }

        displayLiveCardContent();
        return START_STICKY;
    }

    public void displayLiveCardContent() {
        String retrievedData = null;

        try {
            retrievedData = new RetrieveData().execute(ServerUrl.serverUrl + "/json").get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        JSONObject serverStatus = null;
        try {
            serverStatus = new JSONObject(retrievedData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String lightStatus = null;
        try {
            if (serverStatus != null) {
                lightStatus = serverStatus.getString("light").toLowerCase();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mLiveCardViews.setTextViewText(R.id.message, "The light is currently " + lightStatus);
//        mLiveCardViews.setTextViewText(R.id.message, "The light is currently " + lightStatus + "\nNew line test");
        mLiveCardViews.setTextViewText(R.id.footer, "Home automation");

        mLiveCard.setViews(mLiveCardViews);
    }
}
