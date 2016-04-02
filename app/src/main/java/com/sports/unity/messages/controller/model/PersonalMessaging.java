package com.sports.unity.messages.controller.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.sports.unity.Database.DBUtil;
import com.sports.unity.Database.SportsUnityDBHelper;
import com.sports.unity.XMPPManager.XMPPClient;
import com.sports.unity.XMPPManager.XMPPService;
import com.sports.unity.util.ActivityActionHandler;
import com.sports.unity.util.CommonUtil;
import com.sports.unity.util.Constants;
import com.sports.unity.util.ImageUtil;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.chatstates.packet.ChatStateExtension;
import org.jivesoftware.smackx.jiveproperties.JivePropertiesManager;
import org.jivesoftware.smackx.privacy.PrivacyList;
import org.jivesoftware.smackx.privacy.PrivacyListManager;
import org.jivesoftware.smackx.privacy.packet.PrivacyItem;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by madmachines on 16/10/15.
 */
public class PersonalMessaging {

    public static int RECEIPT_KIND_READ = 1;
    public static int RECEIPT_KIND_SERVER = 2;
    public static int RECEIPT_KIND_CLIENT = 3;

    private static final String PRIVACY_LIST_NAME = "spuBlockedList";

    private static PersonalMessaging PERSONAL_MESSAGING = null;

    synchronized public static PersonalMessaging getInstance(Context context) {
        if (PERSONAL_MESSAGING == null) {
            PERSONAL_MESSAGING = new PersonalMessaging(context);
        }
        return PERSONAL_MESSAGING;
    }

    private final Map<Chat, ChatState> chatStates = new WeakHashMap<Chat, ChatState>();

    private SportsUnityDBHelper sportsUnityDBHelper = null;

    private PersonalMessaging(Context context) {
        sportsUnityDBHelper = SportsUnityDBHelper.getInstance(context);
    }

    public void sendTextMessage(String msg, Chat chat, String fromJID, int chatId, boolean otherChat) {
        Message message = new Message();
        message.setBody(msg);

        String time = String.valueOf(CommonUtil.getCurrentGMTTimeInEpoch());
        String stanzaId = sendMessage(message, chat, time, SportsUnityDBHelper.MIME_TYPE_TEXT, otherChat);

        int messageId = sportsUnityDBHelper.addMessage(message.getBody(), SportsUnityDBHelper.MIME_TYPE_TEXT, fromJID, true, time,
                stanzaId, null, null, chatId, SportsUnityDBHelper.DEFAULT_READ_STATUS);
        sportsUnityDBHelper.updateChatEntry(messageId, chatId);
    }

    public void sendStickerMessage(String msg, Chat chat, String fromJID, int chatId, boolean otherChat) {
        Message message = new Message();
        message.setBody(msg);

        String time = String.valueOf(CommonUtil.getCurrentGMTTimeInEpoch());
        String stanzaId = sendMessage(message, chat, time, SportsUnityDBHelper.MIME_TYPE_STICKER, otherChat);

        int messageId = sportsUnityDBHelper.addMessage(message.getBody(), SportsUnityDBHelper.MIME_TYPE_STICKER, fromJID, true, time,
                stanzaId, null, null, chatId, SportsUnityDBHelper.DEFAULT_READ_STATUS);
        sportsUnityDBHelper.updateChatEntry(messageId, chatId);
    }

    public void sendMediaMessage(String contentChecksum, String thumbnailImageAsBase64, Chat chat, int messageId, String mimeType, boolean nearByChat) {
        String messageBody = null;
        if (thumbnailImageAsBase64 != null) {
            messageBody = contentChecksum + ":" + thumbnailImageAsBase64;
        } else {
            messageBody = contentChecksum;
        }

        Message message = new Message();
        message.setBody(messageBody);

        long time = CommonUtil.getCurrentGMTTimeInEpoch();
        String stanzaId = sendMessage(message, chat, String.valueOf(time), mimeType, nearByChat);

        sportsUnityDBHelper.updateMediaMessage_ContentUploaded(messageId, stanzaId, contentChecksum);
    }

    public static String getChecksumOutOfMessageBody(String messageBody) {
        int separatorIndex = messageBody.indexOf(':');
        String checksum = null;
        if (separatorIndex == -1) {
            checksum = messageBody;
        } else {
            checksum = messageBody.substring(0, separatorIndex);
        }
        return checksum;
    }

    public static String getEncodedImageOutOfImage(String messageBody) {
        int separatorIndex = messageBody.indexOf(':');
        String encodedImage = null;
        if (separatorIndex == -1) {
            encodedImage = null;
        } else {
            encodedImage = messageBody.substring(separatorIndex + 1);
        }
        return encodedImage;
    }

    private String sendMessage(Message message, Chat chat, String currentTime, String mimeType, boolean otherChat) {
        String id = null;
        try {
            JivePropertiesManager.addProperty(message, Constants.PARAM_TIME, currentTime);
            JivePropertiesManager.addProperty(message, Constants.PARAM_MIME_TYPE, mimeType);
            if (otherChat) {
                JivePropertiesManager.addProperty(message, Constants.PARAM_CHAT_TYPE_OTHERS, otherChat);
            }

            DeliveryReceiptRequest.addTo(message);
            id = message.getStanzaId();
            chat.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }

    public void sendStatus(ChatState newState, Chat chat) {

//        if (chat == null || newState == null) {
//            throw new IllegalArgumentException("Arguments cannot be null.");
//        }

        try {

            if (!updateChatState(chat, newState)) {
                return;
            }

            Message message = new Message();
            long time = CommonUtil.getCurrentGMTTimeInEpoch();
            JivePropertiesManager.addProperty(message, Constants.PARAM_TIME, time);
            ChatStateExtension extension = new ChatStateExtension(newState);
            message.addExtension(extension);

            chat.sendMessage(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getLastTime(String jid) {
        Message msg = new Message("gettimedev@mm.io", Message.Type.headline);
        msg.setBody(jid);
        try {
            XMPPClient.getConnection().sendPacket(msg);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

    }

    public void sendOfflinePresence() {
        if (XMPPClient.getInstance().isConnectionAuthenticated()) {
            Presence presence = new Presence(Presence.Type.available);
            presence.setStatus("Offline");
            try {
                XMPPClient.getConnection().sendStanza(presence);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }

            Message message = new Message("settimedev@mm.io", Message.Type.headline);
            message.setBody(String.valueOf(CommonUtil.getCurrentGMTTimeInEpoch()));
            try {
                XMPPClient.getConnection().sendStanza(message);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        } else {
            //nothing
        }
    }

    public void sendOnlinePresence() {
        if (XMPPClient.getInstance().isConnectionAuthenticated()) {
            Presence presence = new Presence(Presence.Type.available);
            presence.setStatus("Online");
            Message message = new Message("settimedev@mm.io", Message.Type.headline);
            message.setBody(String.valueOf(CommonUtil.getCurrentGMTTimeInEpoch()));
            try {
                XMPPClient.getConnection().sendStanza(presence);
                XMPPClient.getConnection().sendStanza(message);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
        } else {
            //nothing
        }
    }

    public void updateBlockList(Context context) {
        List<PrivacyItem> privacyItems = getPrivacyList();

        if (privacyItems.size() > -1) {
            ArrayList<String> blockedUserList = SportsUnityDBHelper.getInstance(context).getUserBlockedList();
            if (blockedUserList.size() != 0) {
                privacyItems = new ArrayList<>();
                for (String jid : blockedUserList) {
                    jid += "@mm.io";
                    privacyItems.add(new PrivacyItem(PrivacyItem.Type.jid, jid, false, 1));
                }
                sendPrivacyList(privacyItems);
            } else {
                //nothing
            }

        } else {
            //nothing
        }
    }

    public boolean changeUserBlockStatus(String phoneNumber, boolean status) {
        boolean success = false;
        phoneNumber += "@mm.io";

        List<PrivacyItem> privacyItems = getPrivacyList();

        /*
         * remove item if already exist.
         */
        for (PrivacyItem item : privacyItems) {
            String from = item.getValue();
            if (from.equalsIgnoreCase(phoneNumber)) {
                privacyItems.remove(item);
                break;
            }
        }

        /*
         * add new privacy item
         */
        PrivacyItem item = new PrivacyItem(PrivacyItem.Type.jid, phoneNumber, !status, 1);
        privacyItems.add(item);

        success = sendPrivacyList(privacyItems);

        return success;
    }

    public String createMediaMessageBody(Context context, String checksum, String mimeType, String fileName) {
        String messageBody = checksum;
        String thumbnailImage = createThumbnailImageAsBase64(context, mimeType, fileName);

        if (thumbnailImage != null) {
            messageBody = checksum + ":" + thumbnailImage;
        }

        return messageBody;
    }

    public static String createThumbnailImageAsBase64(Context context, String mimeType, String fileName) {
        String thumbnailImage = null;
        if (mimeType.equals(SportsUnityDBHelper.MIME_TYPE_IMAGE) || mimeType.equals(SportsUnityDBHelper.MIME_TYPE_VIDEO)) {
            try {
                if (mimeType.equals(SportsUnityDBHelper.MIME_TYPE_VIDEO)) {
                    Bitmap videoThumbnail = ThumbnailUtils.createVideoThumbnail(DBUtil.getFilePath(context, mimeType, fileName), MediaStore.Images.Thumbnails.MINI_KIND);

                    byte[] content = ImageUtil.getCompressedBytes(videoThumbnail, 20);
                    thumbnailImage = Base64.encodeToString(content, Base64.DEFAULT);
                } else {
                    thumbnailImage = ImageUtil.getBaseEncoded_ThumbnailImage(context, DBUtil.getFilePath(context, mimeType, fileName));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            //nothing
        }
        return thumbnailImage;
    }

    private List<PrivacyItem> getPrivacyList() {
        List<PrivacyItem> privacyItems = new ArrayList<>();

        try {
            PrivacyListManager privacyManager = PrivacyListManager.getInstanceFor(XMPPClient.getConnection());
            PrivacyList plist = privacyManager.getPrivacyList(PRIVACY_LIST_NAME);
            privacyItems = plist.getItems();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return privacyItems;
    }

    private boolean sendPrivacyList(List<PrivacyItem> privacyItems) {
        boolean success = false;
        PrivacyListManager privacyManager = PrivacyListManager.getInstanceFor(XMPPClient.getConnection());

        try {
            privacyManager.updatePrivacyList(PRIVACY_LIST_NAME, privacyItems);
            privacyManager.setActiveListName(PRIVACY_LIST_NAME);

            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return success;
    }

    public void setReceivedReceipts(String fromJid, String receiptId, Context applicationContext) {

        /**
         * set read receipts in database
         */

        if (fromJid.substring(0, fromJid.indexOf("@")).equals("dev")) {
            sportsUnityDBHelper.updateServerReceived(receiptId);
            updateReceipts(RECEIPT_KIND_SERVER);
        } else {
            sportsUnityDBHelper.updateClientReceived(receiptId);
            updateReceipts(RECEIPT_KIND_CLIENT);
        }

    }

    public void readReceiptReceived(String fromJid, String toJid, String packetId) {
        sportsUnityDBHelper.updateReadStatus(packetId);

        updateReceipts(RECEIPT_KIND_READ);
    }

    public void sendReadStatus(String to, String messageStanzaId) {
        boolean success = XMPPClient.getInstance().sendReadStatus(to + "@mm.io", messageStanzaId);
        if (success) {
            sportsUnityDBHelper.updateReadStatus(messageStanzaId);
        } else {
            //nothing
        }
    }

    public void setActiveStatus(String phoneNumber, String status) {

        /**
         * query to set active status in the database and then call getActiveStatus to display it
         */

    }

    public String getActiveStatus(String phoneNumber) {

        /**
         * query to get active status from the database and return it in status
         */
        String status = null;

        return status;

    }

    private synchronized boolean updateChatState(Chat chat, ChatState newState) {
        ChatState lastChatState = chatStates.get(chat);
        if (lastChatState != newState) {
            chatStates.put(chat, newState);
            return true;
        }
        return false;
    }

    public void updateReceipts(int receiptKind) {

        /**
         * get read receipts in database and then update the double ticks in the corresponding chats
         */

        ActivityActionHandler.getInstance().dispatchReceiptEvent(ActivityActionHandler.CHAT_SCREEN_KEY, receiptKind);
    }

    public void handleChatMessage(Context context, Message message) {
        Object value = JivePropertiesManager.getProperty(message, Constants.PARAM_TIME);
        String fromId = message.getFrom().substring(0, message.getFrom().indexOf("@"));
        String to = message.getTo().substring(0, message.getTo().indexOf("@"));
        String mimeType = (String) JivePropertiesManager.getProperty(message, Constants.PARAM_MIME_TYPE);
        boolean nearByChat = false;
        Object object = JivePropertiesManager.getProperty(message, Constants.PARAM_CHAT_TYPE_OTHERS);
        if (object != null) {
            nearByChat = (boolean) object;
        }

        boolean success = true;
        String messageFrom = fromId;
//        if (isGroupChat) {
//            groupServerId = fromId;
//            messageFrom = message.getFrom().substring(message.getFrom().indexOf("/") + 1);
//
//            if (!to.equals(messageFrom)) {
//                chatId = XMPPService.getChatIdOrCreateIfNotExist( context, isGroupChat, messageFrom, groupServerId, false);
//                handleMessage(message, value, chatId, messageFrom, groupServerId, false);
//            } else {
//                success = false;
//            }
//        } else {

        int chatId = XMPPService.getChatIdOrCreateIfNotExist(context, false, fromId, nearByChat);
        handleMessage(message, value, chatId, fromId, nearByChat);
//        }


        if (success == true && chatId != SportsUnityDBHelper.DEFAULT_ENTRY_ID) {

            boolean eventDispatched = ActivityActionHandler.getInstance().dispatchCommonEvent(ActivityActionHandler.CHAT_SCREEN_KEY, fromId);
            if (eventDispatched) {
                //nothing
            } else {
                Contacts contact = sportsUnityDBHelper.getContactByJid(fromId);
                try {
                    sportsUnityDBHelper.updateUnreadCount(chatId, fromId);
                    if (contact.availableStatus != Contacts.AVAILABLE_BY_MY_CONTACTS) {
                        ActivityActionHandler.getInstance().dispatchCommonEvent(ActivityActionHandler.CHAT_OTHERS_LIST_KEY);
                    } else {
                        ActivityActionHandler.getInstance().dispatchCommonEvent(ActivityActionHandler.CHAT_LIST_KEY);
                    }
                    byte[] image = contact.image;
                    XMPPService.displayNotification(context, message.getBody(), messageFrom, mimeType, chatId, false, fromId, image, contact.availableStatus, contact.status);
                } catch (XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                } catch (SmackException.NoResponseException e) {
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void handleMessage(Message message, Object value, int chatId, String from, boolean nearByChat) {
        String mimeType = (String) JivePropertiesManager.getProperty(message, Constants.PARAM_MIME_TYPE);

        if (mimeType.equals(SportsUnityDBHelper.MIME_TYPE_IMAGE)) {
            String checksum = PersonalMessaging.getChecksumOutOfMessageBody(message.getBody());
            String thumbnail = PersonalMessaging.getEncodedImageOutOfImage(message.getBody());

            byte[] bytesOfThumbnail = null;
            if (thumbnail != null) {
                bytesOfThumbnail = Base64.decode(thumbnail, Base64.DEFAULT);
            }

            int messageId = sportsUnityDBHelper.addMediaMessage(checksum, mimeType, from, false,
                    value.toString(), message.getStanzaId(), null, null, chatId, SportsUnityDBHelper.DEFAULT_READ_STATUS, null, bytesOfThumbnail);
            sportsUnityDBHelper.updateChatEntry(messageId, from);

            ActivityActionHandler.getInstance().dispatchIncomingMediaEvent(ActivityActionHandler.CHAT_SCREEN_KEY, from, mimeType, checksum, Integer.valueOf(messageId));
        } else if (mimeType.equals(SportsUnityDBHelper.MIME_TYPE_TEXT)) {
            int messageId = sportsUnityDBHelper.addMessage(message.getBody().toString(), mimeType, from, false,
                    value.toString(), message.getStanzaId(), null, null, chatId, SportsUnityDBHelper.DEFAULT_READ_STATUS);
            sportsUnityDBHelper.updateChatEntry(messageId, from);
        } else if (mimeType.equals(SportsUnityDBHelper.MIME_TYPE_AUDIO)) {
            String checksum = message.getBody();

            int messageId = sportsUnityDBHelper.addMessage(message.getBody().toString(), mimeType, from, false,
                    value.toString(), message.getStanzaId(), null, null, chatId, SportsUnityDBHelper.DEFAULT_READ_STATUS);
            sportsUnityDBHelper.updateChatEntry(messageId, from);

            ActivityActionHandler.getInstance().dispatchIncomingMediaEvent(ActivityActionHandler.CHAT_SCREEN_KEY, from, mimeType, checksum, Integer.valueOf(messageId));
        } else if (mimeType.equals(SportsUnityDBHelper.MIME_TYPE_VIDEO)) {
            String checksum = PersonalMessaging.getChecksumOutOfMessageBody(message.getBody());
            String thumbnail = PersonalMessaging.getEncodedImageOutOfImage(message.getBody());

            byte[] bytesOfThumbnail = null;
            if (thumbnail != null) {
                bytesOfThumbnail = Base64.decode(thumbnail, Base64.DEFAULT);
            }

//            long messageId = sportsUnityDBHelper.addMessage(message.getBody().toString(), mimeType, from, false,
//                    value.toString(), message.getStanzaId(), null, null, chatId, SportsUnityDBHelper.DEFAULT_READ_STATUS);
            int messageId = sportsUnityDBHelper.addMediaMessage(checksum, mimeType, from, false,
                    value.toString(), message.getStanzaId(), null, null, chatId, SportsUnityDBHelper.DEFAULT_READ_STATUS, null, bytesOfThumbnail);
            sportsUnityDBHelper.updateChatEntry(messageId, from);

            ActivityActionHandler.getInstance().dispatchIncomingMediaEvent(ActivityActionHandler.CHAT_SCREEN_KEY, from, mimeType, checksum, Integer.valueOf(messageId));
        } else if (mimeType.equals(SportsUnityDBHelper.MIME_TYPE_STICKER)) {
            int messageId = sportsUnityDBHelper.addMessage(message.getBody().toString(), mimeType, from, false,
                    value.toString(), message.getStanzaId(), null, null,
                    chatId, SportsUnityDBHelper.DEFAULT_READ_STATUS);
            sportsUnityDBHelper.updateChatEntry(messageId, from);
        }
    }

    public void handleStatus(Message message) {
        String jid = message.getFrom().substring(0, message.getFrom().indexOf("@mm.io"));
        Log.i("handle status :", "");
        if (message.hasExtension(ChatState.composing.toString(), ChatStateExtension.NAMESPACE)) {
            Log.i("status :", "composing");
            ActivityActionHandler.getInstance().dispatchUserStatusOnChat(ActivityActionHandler.CHAT_SCREEN_KEY, jid, ChatState.composing.toString());
        } else if (message.hasExtension(ChatState.active.toString(), ChatStateExtension.NAMESPACE)) {
            Log.i("status :", "active");
        } else if (message.hasExtension(ChatState.gone.toString(), ChatStateExtension.NAMESPACE)) {
            Log.i("status :", "gone");
        } else if (message.hasExtension(ChatState.paused.toString(), ChatStateExtension.NAMESPACE)) {
            Log.i("status :", "paused");
            ActivityActionHandler.getInstance().dispatchUserStatusOnChat(ActivityActionHandler.CHAT_SCREEN_KEY, jid, ChatState.paused.toString());
        } else if (message.hasExtension(ChatState.inactive.toString(), ChatStateExtension.NAMESPACE)) {
            Log.i("status :", "inactive");
        }
    }

}
