package com.habitrpg.android.habitica.helpers.notifications;

import android.content.Context;
import android.util.Log;

/**
 * Created by keithholliday on 6/28/16.
 */
public class HabiticaLocalNotificationFactory {
    private static final String DEBUG_TAG = "H...LocalNotifFact";

    //use getShape method to get object of type shape
    public HabiticaLocalNotification build(String notificationType, Context context) {
        Log.d(DEBUG_TAG, "in build");
        if (notificationType.equalsIgnoreCase(PushNotificationManager.PARTY_INVITE_PUSH_NOTIFICATION_KEY)) {
            return new PartyInviteLocalNotification(context, notificationType);
        } else if (notificationType.contains(PushNotificationManager.RECEIVED_PRIVATE_MESSAGE_PUSH_NOTIFICATION_KEY)) {
            return new ReceivedPrivateMessageLocalNotification(context, notificationType);
        } else if (notificationType.contains(PushNotificationManager.RECEIVED_GEMS_PUSH_NOTIFICATION_KEY)) {
            return new ReceivedGemsGiftLocalNotification(context, notificationType);
        } else if (notificationType.contains(PushNotificationManager.RECEIVED_SUBSCRIPTION_GIFT_PUSH_NOTIFICATION_KEY)) {
            return new ReceivedSubscriptionGiftLocalNotification(context, notificationType);
        } else if (notificationType.contains(PushNotificationManager.GUILD_INVITE_PUSH_NOTIFICATION_KEY)) {
            return new GuildInviteLocalNotification(context, notificationType);
        } else if (notificationType.contains(PushNotificationManager.QUEST_INVITE_PUSH_NOTIFICATION_KEY)) {
            return new QuestInviteLocalNotification(context, notificationType);
        } else if (notificationType.contains(PushNotificationManager.QUEST_BEGUN_PUSH_NOTIFICATION_KEY)) {
            return new QuestBegunLocalNotification(context, notificationType);
        } else if (notificationType.contains(PushNotificationManager.WON_CHALLENGE_PUSH_NOTIFICATION_KEY)) {
            return new WonChallengeLocalNotification(context, notificationType);
        } else if (notificationType.contains(PushNotificationManager.CHANGE_USERNAME_PUSH_NOTIFICATION_KEY)) {
            return new ChangeUsernameLocalNotification(context, notificationType);
        } else if (notificationType.contains(PushNotificationManager.GIFT_ONE_GET_ONE_PUSH_NOTIFICATION_KEY)) {
            return new GiftOneGetOneLocalNotification(context, notificationType);
        } else {
            return new GenericLocalNotification(context, notificationType);
        }
    }
}
