package com.habitrpg.android.habitica.helpers.notifications;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.habitrpg.android.habitica.HabiticaApplication;

import java.util.Objects;

import javax.inject.Inject;

/**
 * Created by keithholliday on 6/24/16.
 */
public class HabiticaFirebaseMessagingService extends FirebaseMessagingService {
    private static final String DEBUG_TAG = "H...FirebaseMssgingSvc";

    @Inject
    public PushNotificationManager pushNotificationManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(DEBUG_TAG, "In onMessageReceived");
        Objects.requireNonNull(HabiticaApplication.Companion.getComponent()).inject(this);
        pushNotificationManager.displayNotification(remoteMessage);
    }
}
