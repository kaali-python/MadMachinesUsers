package com.sports.unity.messages.controller.viewhelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.sports.unity.R;
import com.sports.unity.messages.controller.activity.NativeCameraActivity;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Created by amandeep on 17/11/15.
 */
public class ChatKeyboardHelper {

    private static ChatKeyboardHelper CHAT_KEYBOARD_HELPER = null;

    public static ChatKeyboardHelper getInstance(boolean newInstance){
        if( CHAT_KEYBOARD_HELPER == null || newInstance ){
            CHAT_KEYBOARD_HELPER = new ChatKeyboardHelper();
        }
        return CHAT_KEYBOARD_HELPER;
    }

    public static void clean(){
        CHAT_KEYBOARD_HELPER = null;
    }

    private View popUpView;
    private PopupWindow popupWindow;

    private int keyboardHeight;
    private int previuosKeyboardHeight;
    private boolean isKeyBoardVisible = false;

    private ViewGroup parentLayout;

    private ChatKeyboardHelper(){

    }

    public void createPopupWindowOnKeyBoard(ViewGroup parentLayout, Activity activity) {
        this.parentLayout = parentLayout;

        popUpView = activity.getLayoutInflater().inflate(R.layout.parent_layout_media_keyboard, null);
        popupWindow = new PopupWindow(popUpView, ViewGroup.LayoutParams.MATCH_PARENT, (int) keyboardHeight, false);
    }

    /**
     * Checking keyboard height and keyboard visibility
     */
    public void checkKeyboardHeight() {

        parentLayout.findViewById(R.id.type_msg).getViewTreeObserver().addOnGlobalLayoutListener(

                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {

                        Rect r = new Rect();
                        parentLayout.getWindowVisibleDisplayFrame(r);

                        int screenHeight = parentLayout.getRootView().getHeight();
                        int heightDifference = screenHeight - (r.bottom);

                        if (previuosKeyboardHeight != heightDifference) {
                            changeKeyboardHeight(heightDifference, parentLayout);
                        }
                        previuosKeyboardHeight = heightDifference;

                        Log.i("Height Diff ", "" + heightDifference);

                        if (heightDifference > 100) {

                            if (!isKeyBoardVisible) {
                                Log.i("is KeyBoard Visible", "true");
                                isKeyBoardVisible = true;

                                {
                                    ViewGroup viewGroup = (ViewGroup) popupWindow.getContentView().findViewById(R.id.popup_window_text);
                                    int visibility = viewGroup.getVisibility();

                                    if (visibility == View.VISIBLE) {
                                        popupWindow.dismiss();

                                    } else {
                                        if (isAnyInputLayoutVisible()) {
                                            showPopupWindow(parentLayout);
                                        } else {
                                            viewGroup.setVisibility(View.VISIBLE);

                                            popupWindow.dismiss();
                                        }
                                    }
                                }
                            }
                        } else {
                            if (isKeyBoardVisible) {
                                Log.i("is KeyBoard Visible", "false");
                                isKeyBoardVisible = false;

                                popupWindow.dismiss();

                                hideAllInputLayouts();

                                ViewGroup sendMessageLayout = (ViewGroup) parentLayout.findViewById(R.id.send_message_layout);
                                sendMessageLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    }

                });
    }

    public void openTextKeyBoard(Activity activity) {
        ViewGroup viewGroup = (ViewGroup) popupWindow.getContentView().findViewById(R.id.popup_window_text);
        int visibility = viewGroup.getVisibility();

        hideAllInputLayouts();
        if ( isKeyBoardVisible ) {
            if( visibility == View.VISIBLE ){
                toggleSystemKeyboard(parentLayout, activity.getApplicationContext());
            } else {
                viewGroup.setVisibility(View.VISIBLE);
                popupWindow.dismiss();

                postActionOnOpeningTextKeyboard(activity);
            }

        } else {
            toggleSystemKeyboard(parentLayout, activity.getApplicationContext());

            viewGroup.setVisibility(View.VISIBLE);
            postActionOnOpeningTextKeyboard(activity);
        }

    }

    public void openCamera(Activity activity) {
        ViewGroup viewGroup = (ViewGroup) popupWindow.getContentView().findViewById(R.id.popup_window_camera);
        int visibility = viewGroup.getVisibility();

        hideAllInputLayouts();
        if ( isKeyBoardVisible ) {
            if( visibility == View.VISIBLE ){
                toggleSystemKeyboard(parentLayout, activity.getApplicationContext());
            } else {
                viewGroup.setVisibility(View.VISIBLE);
                showPopupWindow(parentLayout);

                postActionOnOpeningCameraKeyboard(activity);
                toggleSystemKeyboard(parentLayout, activity.getApplicationContext());
            }

        } else {
//            viewGroup.setVisibility(View.VISIBLE);
//            postActionOnOpeningCameraKeyboard(activity);

            Intent intent = new Intent(activity, NativeCameraActivity.class);
            activity.startActivity(intent);
        }
    }

    public void openEmoji(Activity activity) {
        ViewGroup viewGroup = (ViewGroup) popupWindow.getContentView().findViewById(R.id.popup_window_emoji);
        int visibility = viewGroup.getVisibility();

        hideAllInputLayouts();
        if ( isKeyBoardVisible ) {
            if( visibility == View.VISIBLE ){
                 toggleSystemKeyboard(parentLayout, activity.getApplicationContext());
            } else {
                viewGroup.setVisibility(View.VISIBLE);
                showPopupWindow(parentLayout);

                postActionOnOpeningEmojiKeyboard(activity);
            }

        } else {
            toggleSystemKeyboard(parentLayout, activity.getApplicationContext());

            viewGroup.setVisibility(View.VISIBLE);
            postActionOnOpeningEmojiKeyboard(activity);
        }
    }


    public void openGallery(Activity activity) {
        ViewGroup viewGroup = (ViewGroup) popupWindow.getContentView().findViewById(R.id.popup_window_gallery);
        int visibility = viewGroup.getVisibility();

        hideAllInputLayouts();
        if ( isKeyBoardVisible ) {
            if( visibility == View.VISIBLE ){
                toggleSystemKeyboard(parentLayout, activity.getApplicationContext());
            } else {
                viewGroup.setVisibility(View.VISIBLE);
                showPopupWindow(parentLayout);

                postActionOnOpeningGalleryKeyboard(activity);
            }

        } else {
            toggleSystemKeyboard(parentLayout, activity.getApplicationContext());

            viewGroup.setVisibility(View.VISIBLE);
            postActionOnOpeningGalleryKeyboard(activity);
        }
    }

    public void openVoiceRecorder(Activity activity) {
        ViewGroup viewGroup = (ViewGroup) popupWindow.getContentView().findViewById(R.id.popup_window_voice);
        int visibility = viewGroup.getVisibility();

        hideAllInputLayouts();
        if ( isKeyBoardVisible ) {
            if( visibility == View.VISIBLE ){
                toggleSystemKeyboard(parentLayout, activity.getApplicationContext());
            } else {
                viewGroup.setVisibility(View.VISIBLE);
                showPopupWindow(parentLayout);

                postActionOnOpeningVoiceKeyboard(activity);
            }

        } else {
            toggleSystemKeyboard(parentLayout, activity.getApplicationContext());

            viewGroup.setVisibility(View.VISIBLE);
            postActionOnOpeningVoiceKeyboard(activity);
        }

    }

    private void postActionOnOpeningTextKeyboard(Activity activity){
        ViewGroup sendMessageLayout = (ViewGroup)activity.findViewById(R.id.send_message_layout);
        sendMessageLayout.setVisibility(View.VISIBLE);

        EditText editText = (EditText)sendMessageLayout.findViewById(R.id.msg);
        editText.requestFocus();
    }

    private void postActionOnOpeningCameraKeyboard(Activity activity){
        ViewGroup sendMessageLayout = (ViewGroup)activity.findViewById(R.id.send_message_layout);
        sendMessageLayout.setVisibility(View.GONE);

        Intent intent = new Intent(activity, NativeCameraActivity.class);
        activity.startActivity(intent);
    }

    private void postActionOnOpeningEmojiKeyboard(Activity activity){
        ViewGroup sendMessageLayout = (ViewGroup)activity.findViewById(R.id.send_message_layout);
        sendMessageLayout.setVisibility(View.GONE);

        //TODO
    }

    private void postActionOnOpeningGalleryKeyboard(Activity activity){
        ViewGroup sendMessageLayout = (ViewGroup)activity.findViewById(R.id.send_message_layout);
        sendMessageLayout.setVisibility(View.GONE);

//      Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
//      photoPickerIntent.setType("image/*");
//      activity.startActivityForResult(photoPickerIntent, 1);
    }

    private void postActionOnOpeningVoiceKeyboard(Activity activity){
        ViewGroup sendMessageLayout = (ViewGroup)activity.findViewById(R.id.send_message_layout);
        sendMessageLayout.setVisibility(View.GONE);

        //TODO
    }

    private void toggleSystemKeyboard(ViewGroup layout, Context context){
        InputMethodManager inputMethodManager=(InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(layout.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideAllInputLayouts(){
        ViewGroup contentView = ((ViewGroup) popupWindow.getContentView());
        for (int loop = 0; loop < contentView.getChildCount(); loop++) {
            contentView.getChildAt(loop).setVisibility(View.GONE);
        }
    }

    private boolean isAnyInputLayoutVisible(){
        boolean visible = false;

        ViewGroup contentView = ((ViewGroup) popupWindow.getContentView());
        for (int loop = 0; loop < contentView.getChildCount(); loop++) {
            if( contentView.getChildAt(loop).getVisibility() == View.VISIBLE ){
                visible = true;
                break;
            }
        }

        return visible;
    }

    private void changeKeyboardHeight(int height, View parentLayout) {
        if (height > 100) {
            keyboardHeight = height;
            popupWindow.setHeight(keyboardHeight);

            if( popupWindow.isShowing() ) {
                popupWindow.dismiss();
                showPopupWindow(parentLayout);
            }
        }
    }

    private void showPopupWindow(View parentLayout){
        if( ! popupWindow.isShowing() ) {
            popupWindow.showAtLocation(parentLayout, Gravity.BOTTOM, 0, 0);
        } else {
            //nothing
        }
    }

}
