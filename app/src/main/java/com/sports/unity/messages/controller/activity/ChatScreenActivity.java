package com.sports.unity.messages.controller.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.sports.unity.ChatScreenApplication;
import com.sports.unity.Database.SportsUnityDBHelper;
import com.sports.unity.R;

import com.sports.unity.XMPPManager.XMPPClient;
import com.sports.unity.common.model.FontTypeface;
import com.sports.unity.messages.controller.model.PersonalMessaging;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.chatstates.ChatState;
import org.jivesoftware.smackx.iqlast.LastActivityManager;
import org.jivesoftware.smackx.iqlast.packet.LastActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class ChatScreenActivity extends AppCompatActivity {

    private static ArrayList<SportsUnityDBHelper.Message> messageList;
    private static ChatScreenAdapter chatScreenAdapter;
    private static String JABBERID;
    private static String JABBERNAME;

    private static long chatID = SportsUnityDBHelper.DEFAULT_ENTRY_ID;

    private long contactID = SportsUnityDBHelper.DEFAULT_ENTRY_ID;

    private EditText mMsg;
    private Chat chat;
    private MessageRecieved messageRecieved;
    private TextView status;

    private ViewGroup parentLayout;

    private View popUpView;
    private PopupWindow popupWindow;

    private int keyboardHeight;
    private boolean isKeyBoardVisible;
    private static XMPPTCPConnection con;

    private SportsUnityDBHelper sportsUnityDBHelper = SportsUnityDBHelper.getInstance(this);
    private PersonalMessaging personalMessaging = PersonalMessaging.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_screen);

        parentLayout = (ViewGroup) findViewById(R.id.list_parent);
        checkKeyboardHeight(parentLayout);
        createPopupWindowOnKeyBoard();

        /**
         * Initialising toolbar for chat screen activity
         */
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar_chat);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


        JABBERID = getIntent().getStringExtra("number");                       //name of the user you are messaging with
        JABBERNAME = getIntent().getStringExtra("name");                  //phone number or jid of the user you are chatting with
        contactID = getIntent().getLongExtra("contactId", SportsUnityDBHelper.DEFAULT_ENTRY_ID);
        chatID = getIntent().getLongExtra("chatId", SportsUnityDBHelper.DEFAULT_ENTRY_ID);


        ListView mChatView = (ListView) findViewById(R.id.msgview);                // List for messages

        messageRecieved = new MessageRecieved();

        con = XMPPClient.getConnection();

        sportsUnityDBHelper.clearUnreadCount(sportsUnityDBHelper.getContactId(JABBERID));
        TextView user = (TextView) toolbar.findViewById(R.id.chat_username);
        user.setText(JABBERNAME);
        user.setTypeface(FontTypeface.getInstance(this).getRobotoRegular());
        ImageButton back = (ImageButton) toolbar.findViewById(R.id.backarrow);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mMsg = (EditText) findViewById(R.id.msg);

        mMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    personalMessaging.sendStatus(ChatState.composing, chat);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        status = (TextView) toolbar.findViewById(R.id.status_active);

        try {
            getLastSeen();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }

        Button mSend = (Button) findViewById(R.id.send);
        mSend.setTypeface(FontTypeface.getInstance(this).getRobotoCondensedRegular());
        ChatManager chatManager = ChatManager.getInstanceFor(XMPPClient.getConnection());
        if (mMsg.getText().toString() != null) {
            chat = chatManager.getThreadChat(JABBERID);
            if (chat == null) {
                chat = chatManager.createChat(JABBERID + "@mm.io");
            }

            initChatId();

            messageList = sportsUnityDBHelper.getMessages(chatID, SportsUnityDBHelper.DEFAULT_ENTRY_ID, false);
            chatScreenAdapter = new ChatScreenAdapter(ChatScreenActivity.this, messageList);
            mChatView.setAdapter(chatScreenAdapter);
            mSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage();
                }
            });
        }
    }

    private void initChatId() {
        if (chatID == SportsUnityDBHelper.DEFAULT_ENTRY_ID) {
            chatID = sportsUnityDBHelper.getChatEntryID(contactID);
        } else {
            //nothing
        }
    }


    private void createChatEntryifNotExists() {
        if (chatID == SportsUnityDBHelper.DEFAULT_ENTRY_ID) {
            chatID = sportsUnityDBHelper.getChatEntryID(contactID);
            if (chatID != SportsUnityDBHelper.DEFAULT_ENTRY_ID) {
                //nothing
            } else {
                chatID = sportsUnityDBHelper.createChatEntry(JABBERNAME, contactID);
                Log.i("ChatEntry : ", "chat entry made " + chatID + " : " + contactID);
            }
        } else {
            //nothing
        }
    }

    public void getLastSeen() throws SmackException.NotConnectedException {
        /*Roster roster = Roster.getInstanceFor(con);
        Collection<RosterEntry> entries = roster.getEntries();
        Presence presence;

        for (RosterEntry entry : entries) {
            presence = roster.getPresence(entry.getUser());

            System.out.println(entry.getUser());
            System.out.println(presence.getType().name());
            System.out.println(presence.getStatus());
        }*/

        try {
            LastActivityManager lActivityManager = LastActivityManager.getInstanceFor(XMPPClient.getConnection());
            Log.d("LAST ACTIVITY", lActivityManager.getLastActivity(JABBERID + "@mm.io") + "");
        } catch (XMPPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        }

    }

    private void sendMessage() {
        createChatEntryifNotExists();

        if (mMsg.getText().toString().equals("")) {
            //Do nothing
        } else {
            Log.i("Message Entry", "adding message chat " + chatID);
            personalMessaging.sendMessageToPeer(mMsg.getText().toString(), chat, JABBERID, chatID, JABBERNAME);

            personalMessaging.sendStatus(ChatState.paused, chat);

            /**
             *  update the chatscreen list of messages
             */

            messageList = sportsUnityDBHelper.getMessages(chatID, SportsUnityDBHelper.DEFAULT_ENTRY_ID, false);
            chatScreenAdapter.notifydataset(messageList);
        }

        mMsg.setText("");
    }

    /**
     * Overriding onKeyDown for dismissing keyboard on key down
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * Checking keyboard height and keyboard visibility
     */
    int previousHeightDiffrence = 0;

    private void checkKeyboardHeight(final View parentLayout) {

        parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        Rect r = new Rect();
                        parentLayout.getWindowVisibleDisplayFrame(r);

                        int screenHeight = parentLayout.getRootView()
                                .getHeight();
                        int heightDifference = screenHeight - (r.bottom);

                        if (previousHeightDiffrence - heightDifference > 50) {
                            popupWindow.dismiss();
                        }

                        previousHeightDiffrence = heightDifference;
                        if (heightDifference > 100) {

                            isKeyBoardVisible = true;
                            changeKeyboardHeight(heightDifference);

                        } else {

                            isKeyBoardVisible = false;

                        }

                    }
                });

    }

    /**
     * change height of emoticons keyboard according to height of actual
     * keyboard
     *
     * @param height minimum height by which we can make sure actual keyboard is
     *               open or not
     */
    private void changeKeyboardHeight(int height) {

        if (height > 100) {
            keyboardHeight = height;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, keyboardHeight);
//            emoticonsCover.setLayoutParams(params);
        }

    }

    public void openCamera(View view) {
        if (isKeyBoardVisible) {
            enablePopupWindowOnKeyBoard();
            ViewGroup viewGroup = (ViewGroup) popupWindow.getContentView().findViewById(R.id.popup_window_camera);
            viewGroup.setVisibility(View.VISIBLE);
            Intent intent=new Intent(ChatScreenActivity.this,CameraActivity.class);
            startActivity(intent);

        }

    }


    public void emojipopup(View view) {
        if (isKeyBoardVisible) {
            enablePopupWindowOnKeyBoard();
            ViewGroup viewGroup = (ViewGroup) popupWindow.getContentView().findViewById(R.id.popup_window_emoji);
            viewGroup.setVisibility(View.VISIBLE);
        }
    }


    public void galleryPopup(View view) {
        if (isKeyBoardVisible) {
            enablePopupWindowOnKeyBoard();
            ViewGroup viewGroup = (ViewGroup) popupWindow.getContentView().findViewById(R.id.popup_window_gallery);
            viewGroup.setVisibility(View.VISIBLE);
            Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, 1);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri chosenImageUri = data.getData();

            Bitmap mBitmap = null;
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), chosenImageUri);
                Log.i("getBitmap? :", "yes");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void voicePopup(View view) {
        if (isKeyBoardVisible) {
            enablePopupWindowOnKeyBoard();
            ViewGroup viewGroup = (ViewGroup) popupWindow.getContentView().findViewById(R.id.popup_window_voice);
            viewGroup.setVisibility(View.VISIBLE);
        }
    }

    public void showKeyboard(View view) {

        if (popupWindow.isShowing())
            popupWindow.dismiss();

    }

    private void createPopupWindowOnKeyBoard() {

        popUpView = getLayoutInflater().inflate(R.layout.parent_layout_media_keyboard, null);

        // Creating a pop window for emoticons keyboard
        popupWindow = new PopupWindow(popUpView, ViewGroup.LayoutParams.MATCH_PARENT, (int) keyboardHeight, false);

    }

    private void enablePopupWindowOnKeyBoard() {
        if (!popupWindow.isShowing()) {
            popupWindow.setHeight((int) (keyboardHeight));
            popupWindow.showAtLocation(parentLayout, Gravity.BOTTOM, 0, 0);
        } else {
            //nothing
        }

        ViewGroup contentView = ((ViewGroup) popupWindow.getContentView());
        for (int loop = 0; loop < contentView.getChildCount(); loop++) {
            contentView.getChildAt(loop).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(messageRecieved, new IntentFilter("com.madmachine.SINGLE_MESSAGE_RECEIVED"));
        Log.i("reciever :", "registered");
        ChatScreenApplication.activityResumed();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_clear_chat) {
            sportsUnityDBHelper.clearChat(chatID);
            messageList = sportsUnityDBHelper.getMessages(chatID, SportsUnityDBHelper.DEFAULT_ENTRY_ID, false);
            chatScreenAdapter.notifydataset(messageList);

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        ChatScreenApplication.activityDestroyed();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        ChatScreenApplication.activityPaused();
        super.onPause();
        unregisterReceiver(messageRecieved);
        Log.i("receiver :", "unregistered");

    }

    @Override
    public void onStop() {
        ChatScreenApplication.activityStopped();
        super.onStop();

    }

    public static String getJABBERID() {
        return JABBERID;
    }

    public static class MessageRecieved extends BroadcastReceiver {

        public MessageRecieved() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            messageList = SportsUnityDBHelper.getInstance(context).getMessages(chatID, SportsUnityDBHelper.DEFAULT_ENTRY_ID, false);
            chatScreenAdapter.notifydataset(messageList);
            Log.i("yoyo :", "yoyo");

        }
    }
}
