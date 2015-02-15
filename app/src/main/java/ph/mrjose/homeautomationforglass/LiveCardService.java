package ph.mrjose.homeautomationforglass;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.google.android.glass.app.Card;
import com.google.android.glass.timeline.LiveCard;

public class LiveCardService extends Service {

    private static final String LIVE_CARD_TAG = "Home Automation For Glass Status";
    private static final String ACTION_STOP = "Stop";

    RemoteViews mLiveCardViews;
    LiveCard mLiveCard;

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
                mLiveCard.navigate();
        }

        return START_STICKY;
    }
}
