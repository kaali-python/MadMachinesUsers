package com.sports.unity.messages.controller.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.sports.unity.Database.SportsUnityDBHelper;
import com.sports.unity.R;
import com.sports.unity.common.model.FontTypeface;
import com.sports.unity.messages.controller.model.Message;
import com.sports.unity.messages.controller.model.Stickers;
import com.sports.unity.messages.controller.viewhelper.AudioRecordingHelper;
import com.sports.unity.util.CommonUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by madmachines on 8/9/15.
 */
public class ChatScreenAdapter extends BaseAdapter {

    private ArrayList<Message> messageList;
    private static LayoutInflater inflater = null;
    private Activity activity;

    private HashMap<String, byte[]> mediaMap = null;
    AudioRecordingHelper audioRecordingHelper = null;
//    SoundPoolHelper soundPoolHelper = null;

    public ChatScreenAdapter(ChatScreenActivity chatScreenActivity, ArrayList<Message> messagelist) {
        this.messageList = messagelist;
        activity = chatScreenActivity;
        mediaMap = chatScreenActivity.getMediaMap();
        inflater = (LayoutInflater) activity.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        soundPoolHelper = SoundPoolHelper.getInstance(activity);
        audioRecordingHelper = AudioRecordingHelper.getInstance(activity);
        audioRecordingHelper.clearProgressMap();
    }

    @Override
    public int getCount() {
        return messageList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        int flag;
        if (messageList.get(position).iAmSender) {
            flag = 1;
        } else {
            flag = 0;
        }
        return flag;
    }


    public static class ViewHolder {

        public TextView message;
        public TextView timeStamp;
        public ImageView receivedStatus;
        private FrameLayout mediaContentLayout;
        public LinearLayout mediaPlayer;
        public ImageButton playandPause;
        public SeekBar seekBar;
        public TextView duration;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;

        Message message = messageList.get(position);
        if (convertView == null) {
            holder = new ViewHolder();
            switch (getItemViewType(position)) {
                case 0:
                    vi = inflater.inflate(R.layout.chat_msg_recieve, parent, false);
                    holder.message = (TextView) vi.findViewById(R.id.singleMessageLeft);
                    holder.message.setTypeface(FontTypeface.getInstance(activity.getApplicationContext()).getRobotoRegular());
                    holder.mediaContentLayout = (FrameLayout) vi.findViewById(R.id.image_message_parent);
                    holder.timeStamp = (TextView) vi.findViewById(R.id.timestampLeft);
                    holder.timeStamp.setTypeface(FontTypeface.getInstance(activity.getApplicationContext()).getRobotoCondensedRegular());
                    holder.playandPause = (ImageButton) vi.findViewById(R.id.playAndPause);
                    holder.seekBar = (SeekBar) vi.findViewById(R.id.seekbar);
                    holder.mediaPlayer = (LinearLayout) vi.findViewById(R.id.mediaPlayer);
                    holder.duration = (TextView) vi.findViewById(R.id.duration);
                    holder.receivedStatus = null;
                    vi.setTag(holder);
                    break;
                case 1:
                    vi = inflater.inflate(R.layout.chat_msg_send, parent, false);
                    holder.message = (TextView) vi.findViewById(R.id.singleMessageRight);
                    holder.message.setTypeface(FontTypeface.getInstance(activity.getApplicationContext()).getRobotoRegular());
                    holder.mediaContentLayout = (FrameLayout) vi.findViewById(R.id.image_message_parent);
                    holder.timeStamp = (TextView) vi.findViewById(R.id.timestampRight);
                    holder.timeStamp.setTypeface(FontTypeface.getInstance(activity.getApplicationContext()).getRobotoCondensedRegular());
                    holder.receivedStatus = (ImageView) vi.findViewById(R.id.receivedStatus);
                    holder.playandPause = (ImageButton) vi.findViewById(R.id.playAndPause);
                    holder.seekBar = (SeekBar) vi.findViewById(R.id.seekbar);
                    holder.mediaPlayer = (LinearLayout) vi.findViewById(R.id.mediaPlayer);
                    holder.duration = (TextView) vi.findViewById(R.id.duration);
                    vi.setTag(holder);
                    break;
            }

        } else {
            holder = (ViewHolder) vi.getTag();
        }

        if (!message.mimeType.equals(SportsUnityDBHelper.MIME_TYPE_STICKER)) {
            if (message.iAmSender) {
                ((LinearLayout) holder.message.getParent()).setBackgroundResource(R.drawable.chat_blue);
            } else {
                ((LinearLayout) holder.message.getParent()).setBackgroundResource(R.drawable.chat_white);
            }
        } else {
            ((LinearLayout) holder.message.getParent()).setBackgroundResource(android.R.color.transparent);
        }

        if (message.mimeType.equals(SportsUnityDBHelper.MIME_TYPE_AUDIO)) {
            holder.mediaContentLayout.setVisibility(View.GONE);
            holder.message.setVisibility(View.GONE);

            holder.mediaPlayer.setVisibility(View.VISIBLE);


            final String filename = message.mediaFileName;
            ProgressBar progressBar = (ProgressBar) holder.mediaPlayer.findViewById(R.id.progressBarAudio);
            if (message.textData.length() == 0 && message.iAmSender == true || message.mediaFileName == null && message.iAmSender == false) {
                Log.i("Progressbar", "true");
                /*RelativeLayout relativeLayout = new RelativeLayout(activity);
                ProgressBar progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleSmall);
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(holder.playandPause.getHeight(), holder.playandPause.getWidth());
                params.addRule(RelativeLayout.CENTER_IN_PARENT);
                relativeLayout.addView(progressBar, params);*/
                holder.playandPause.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
                holder.playandPause.setVisibility(View.VISIBLE);

            }

            final int id = message.id;
            final Object messageObject = message;

            holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    audioRecordingHelper.setProgress(id, seekBar.getProgress());
                }
            });

            audioRecordingHelper.addMediaBarProgress(message.id, 0);
            audioRecordingHelper.createMediaPlayer(filename, id, holder);
            audioRecordingHelper.initUI(holder);

            holder.playandPause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (audioRecordingHelper.isPlaying()) {
                        Log.i("isPlaying", "true");
                        audioRecordingHelper.pauseMediaPlayer((Message) messageObject, id, holder);
                    } else {
                        Log.i("isPlaying", "false");
                        audioRecordingHelper.createMediaPlayer(filename, id, holder);
                        audioRecordingHelper.startMediaPlayer();
                        holder.playandPause.setImageResource(android.R.drawable.ic_media_pause);
                        audioRecordingHelper.startTimer(0, 10, holder);
                    }
                }
            });

        } else if (message.mimeType.equals(SportsUnityDBHelper.MIME_TYPE_TEXT)) {
            holder.message.setVisibility(View.VISIBLE);
            holder.mediaPlayer.setVisibility(View.GONE);
            holder.mediaContentLayout.setVisibility(View.GONE);
            holder.message.setText(message.textData);
        } else if (message.mimeType.equals(SportsUnityDBHelper.MIME_TYPE_IMAGE)) {
            holder.message.setVisibility(View.GONE);
            holder.mediaPlayer.setVisibility(View.GONE);
            holder.mediaContentLayout.setVisibility(View.VISIBLE);

            holder.message.setText("");

            byte[] content = null;
            if (mediaMap.containsKey(message.mediaFileName)) {
                content = mediaMap.get(message.mediaFileName);
            } else {
                content = message.media;
            }

            ImageView image = (ImageView) holder.mediaContentLayout.findViewById(R.id.image_message);
            image.setVisibility(View.VISIBLE);

            int size = activity.getResources().getDimensionPixelSize(R.dimen.media_msg_content_size);
            image.setLayoutParams(new FrameLayout.LayoutParams(size, size));

            if (content != null) {
                image.setImageBitmap(BitmapFactory.decodeByteArray(content, 0, content.length));
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                image.setImageResource(R.drawable.grey_bg_rectangle);
            }

            ProgressBar progressBar = (ProgressBar) holder.mediaContentLayout.findViewById(R.id.progressBar);
            if ((message.textData.length() == 0 && message.iAmSender == true) || (message.mediaFileName == null && message.iAmSender == false)) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        } else if (message.mimeType.equals(SportsUnityDBHelper.MIME_TYPE_STICKER)) {
            holder.message.setText("");
            holder.message.setVisibility(View.GONE);
            holder.mediaPlayer.setVisibility(View.GONE);
            holder.mediaContentLayout.setVisibility(View.VISIBLE);

            ImageView image = (ImageView) holder.mediaContentLayout.findViewById(R.id.image_message);
            ProgressBar progressBar = (ProgressBar) holder.mediaContentLayout.findViewById(R.id.progressBar);

            progressBar.setVisibility(View.GONE);
            image.setVisibility(View.VISIBLE);

            String content = message.textData;
            int separatorIndex = content.indexOf('/');
            String folderName = content.substring(0, separatorIndex);
            String name = content.substring(separatorIndex + 1);

            //TODO remove this call from here
            Stickers.getInstance().loadStickerFromAsset(activity, folderName, name);

            Bitmap bitmap = Stickers.getInstance().getStickerBitmap(folderName, name);
            if (bitmap != null) {
                int size = activity.getResources().getDimensionPixelSize(R.dimen.sticker_msg_content_size);
                image.setImageBitmap(bitmap);
                image.setVisibility(View.VISIBLE);
                image.setLayoutParams(new FrameLayout.LayoutParams(size, size));
                image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            } else {
                image.setVisibility(View.GONE);
            }
        }

        switch (getItemViewType(position)) {
            case 0:
                holder.timeStamp.setText(CommonUtil.getDefaultTimezoneTimeInAMANDPM(Long.parseLong(message.sendTime)));
                break;
            case 1:
                holder.timeStamp.setText(CommonUtil.getDefaultTimezoneTimeInAMANDPM(Long.parseLong(message.sendTime)));
                break;

        }

        if (holder.receivedStatus == null) {
            //do nothing
        } else {

            if ( message.messagesRead == true ) {
                holder.receivedStatus.setImageResource(R.drawable.ic_msg_read);
            } else if (message.recipientR != null) {
                holder.receivedStatus.setImageResource(R.drawable.ic_msg_delivered);
            } else if (message.serverR != null) {
                holder.receivedStatus.setImageResource(R.drawable.ic_msg_sent);
            } else {
                holder.receivedStatus.setImageResource(R.drawable.ic_msg_pending);
            }

        }

        return vi;
    }


    public void notifydataset(ArrayList<Message> messagelist) {

        this.messageList = messagelist;
        notifyDataSetChanged();
    }


}
