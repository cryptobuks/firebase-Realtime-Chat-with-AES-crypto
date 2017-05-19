package com.developer.ilhamsuaib.securechatapp.adapter;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.developer.ilhamsuaib.securechatapp.R;
import com.developer.ilhamsuaib.securechatapp.helper.AESHelper;
import com.developer.ilhamsuaib.securechatapp.model.Chat;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.Query;

/**
 * Created by ilham on 07/12/2016.
 */

public class ChatAdapter extends FirebaseListAdapter<Chat>{
    /**
     * @param activity    The activity containing the ListView
     * @param modelClass  Firebase will marshall the data at a location into an instance of a class that you provide
     * @param modelLayout This is the layout used to represent a single list item. You will be responsible for populating an
     *                    instance of the corresponding view with the data from an instance of modelClass.
     * @param ref         The Firebase location to watch for data changes. Can also be a slice of a location, using some
     *                    combination of {@code limit()}, {@code startAt()}, and {@code endAt()}.
     */
    private String username;
    public ChatAdapter(Activity activity, Class<Chat> modelClass, Query ref, String username) {
        super(activity, modelClass, R.layout.chat_list_item, ref);
        this.username = username;
    }

    @Override
    protected void populateView(View v, Chat model, int position) {
        ViewHolder holder = new ViewHolder();
        holder.txtTime = (TextView) v.findViewById(R.id.txt_time);
        holder.txtMessage = (TextView) v.findViewById(R.id.txt_message);
        holder.txtPlay = (TextView) v.findViewById(R.id.txt_play);
        holder.contentWithBg = (LinearLayout) v.findViewById(R.id.contentWithBg);
        holder.content = (LinearLayout) v.findViewById(R.id.content);
        setAlignment(holder, model.getUsername().equals(username));
        holder.txtTime.setText(model.getTime());
        holder.txtTime.setPadding(20, 0, 20, 5);
        if (model.getMessageType().equals(Chat.MessageType.Text)){
            String message = null;
            try {
                message = AESHelper.decrypt(String.valueOf(R.string.myKey), model.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            holder.txtMessage.setText(message);
            holder.txtPlay.setVisibility(View.GONE);
        }else{
            holder.txtMessage.setText("Audio Message");
            holder.txtMessage.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        }
    }

    private void setAlignment(ViewHolder holder, boolean isMe){
        int bgResource, grafity, rule1, rule2;
        if (!isMe){
            bgResource = R.drawable.out_message_bg;
            grafity = Gravity.LEFT;
            rule1 = RelativeLayout.ALIGN_PARENT_RIGHT;
            rule2 = RelativeLayout.ALIGN_PARENT_LEFT;
        }else{
            bgResource = R.drawable.in_message_bg;
            grafity = Gravity.RIGHT;
            rule1 = RelativeLayout.ALIGN_PARENT_LEFT;
            rule2 = RelativeLayout.ALIGN_PARENT_RIGHT;
        }

        holder.contentWithBg.setBackgroundResource(bgResource);
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) holder.contentWithBg.getLayoutParams();
        layoutParams.gravity = grafity;
        holder.contentWithBg.setLayoutParams(layoutParams);
        holder.txtTime.setLayoutParams(layoutParams);

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) holder.content.getLayoutParams();
        lp.addRule(rule1, 0);
        lp.addRule(rule2);
        holder.content.setLayoutParams(lp);

        layoutParams = (LinearLayout.LayoutParams) holder.txtMessage.getLayoutParams();
        layoutParams.gravity = grafity;
        holder.txtMessage.setLayoutParams(layoutParams);
    }

    static class ViewHolder{
        TextView txtTime, txtMessage, txtPlay;
        LinearLayout content, contentWithBg;
    }
}
