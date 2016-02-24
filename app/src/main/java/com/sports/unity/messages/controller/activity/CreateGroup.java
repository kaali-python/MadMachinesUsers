package com.sports.unity.messages.controller.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sports.unity.Database.SportsUnityDBHelper;
import com.sports.unity.R;
import com.sports.unity.common.controller.CustomAppCompatActivity;
import com.sports.unity.common.model.FontTypeface;
import com.sports.unity.common.model.TinyDB;
import com.sports.unity.messages.controller.fragment.ContactsFragment;
import com.sports.unity.messages.controller.fragment.GroupDetailFragment;
import com.sports.unity.messages.controller.model.Contacts;
import com.sports.unity.messages.controller.model.GroupMessaging;
import com.sports.unity.messages.controller.model.PubSubMessaging;
import com.sports.unity.util.Constants;

import java.util.ArrayList;

public class CreateGroup extends CustomAppCompatActivity {

    private String groupName = null;
    private String groupDescription = null;

    private String currentUserJID = null;
    private byte[] groupImageArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);

        currentUserJID = TinyDB.getInstance(this).getString(TinyDB.KEY_USER_JID);

        addGroupDetailFragment();
    }

    private void addGroupDetailFragment() {
        GroupDetailFragment groupDetailFragment = new GroupDetailFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, groupDetailFragment).commit();
    }

    public void moveToMembersListFragment() {
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.INTENT_KEY_CONTACT_FRAGMENT_USAGE, ContactsFragment.USAGE_FOR_MEMBERS);

        ContactsFragment fragment = new ContactsFragment();
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, fragment, "as_member");
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        setToolBarForMembersList();
    }

    public void setGroupDetails(String groupName, String groupDescription, byte[] groupImageArray) {
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.groupImageArray = groupImageArray;
    }

    private void setToolBarForMembersList() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        ((ImageView) toolbar.findViewById(R.id.backarrow)).setImageResource(R.drawable.ic_menu_back);
        toolbar.findViewById(R.id.backarrow).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView title = (TextView) toolbar.findViewById(R.id.title);
        title.setText(R.string.group_title_add_members);

        TextView actionView = (TextView) toolbar.findViewById(R.id.actionButton);
        actionView.setText(R.string.done);
        actionView.setTypeface(FontTypeface.getInstance(this).getRobotoCondensedBold());
        actionView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //TODO
                FragmentManager fragmentManager = getSupportFragmentManager();
                ContactsFragment fragment = (ContactsFragment) fragmentManager.findFragmentByTag("as_member");

                createGroup(fragment.getSelectedMembersList());
            }

        });

        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void createGroup(ArrayList<Contacts> selectedMembers) {
        GroupMessaging groupMessaging = GroupMessaging.getInstance(this);
        String roomName = currentUserJID + "" + System.currentTimeMillis();
        roomName = roomName + "%" + groupName + "%%";
        String subject = groupName;

        boolean success = groupMessaging.createGroup(roomName, currentUserJID, subject);
        Contacts owner = SportsUnityDBHelper.getInstance(this).getContactByJid(currentUserJID);
        if (success) {
            groupMessaging.setGroupConfigDetail(roomName, groupName, groupDescription);
            groupMessaging.joinGroup(roomName, currentUserJID);

            /*long chatId = SportsUnityDBHelper.getInstance(this).createGroupChatEntry(subject, owner.id, null, roomName);
            SportsUnityDBHelper.getInstance(this).updateChatEntry(SportsUnityDBHelper.getDummyMessageRowId(), chatId, roomName);

            ArrayList<Long> members = new ArrayList<>();
            for (Contacts c :
                    selectedMembers) {
                members.add(c.id);
            }
            SportsUnityDBHelper.getInstance(getApplicationContext()).createGroupUserEntry(chatId, members);*/

            PubSubMessaging pubSubMessaging = PubSubMessaging.getInstance(this);
            boolean nodesuccess = pubSubMessaging.createNode(roomName, this);
            if (nodesuccess) {

                long chatId = SportsUnityDBHelper.getInstance(this).createGroupChatEntry(subject, owner.id, groupImageArray, roomName);
                SportsUnityDBHelper.getInstance(this).updateChatEntry(SportsUnityDBHelper.getDummyMessageRowId(), chatId, roomName);

                String groupmembersnumbers = "";
                ArrayList<Long> members = new ArrayList<>();
                for (Contacts c :
                        selectedMembers) {
                    members.add(c.id);
                    groupmembersnumbers += c.id + ",";
                }
                SportsUnityDBHelper.getInstance(getApplicationContext()).createGroupUserEntry(chatId, members);
                SportsUnityDBHelper.getInstance(getApplicationContext()).updateAdmin(TinyDB.getInstance(getApplicationContext()).KEY_USER_JID, roomName);
                Log.i("send Invites for group", "true");
                groupMessaging.inviteMembers(roomName, selectedMembers, "");
                finish();
            } else {
                Toast.makeText(this, R.string.pubsub_message_try_again, Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(this, R.string.group_message_try_again, Toast.LENGTH_SHORT).show();
        }
    }

}