package com.kw.top.ui.activity.news;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.google.gson.internal.LinkedTreeMap;
import com.kw.top.R;
import com.kw.top.base.MyEaseBaseActivity;
import com.kw.top.bean.BaseBean;
import com.kw.top.bean.event.ExitClubEvent;
import com.kw.top.retrofit.Api;
import com.kw.top.runtimepermissions.PermissionsManager;
import com.kw.top.tools.NotificationTools;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * author  ： zy
 * date    ： 2018/7/1
 * des     ：
 */

public class ChatActivity extends MyEaseBaseActivity {

    public static ChatActivity activityInstance;
    //private EaseChatFragment chatFragment;
    public String toChatUsername;
    public String toolbarTitle;
    private boolean isBackground;
    private TextView mTaskState;

    public static void startActivity(Context context, String userid, String title, String receive_headurl, String receive_name) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("userId", userid);
        intent.putExtra("toolbar_title", title);
        //intent.putExtra(EaseConstant.RECEIVE_HEAD_REL, receive_headurl);
       // intent.putExtra(EaseConstant.RECEIVE_NAME, receive_name);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        EventBus.getDefault().register(this);
        setContentView(R.layout.em_activity_chat);
        mTaskState = findViewById(R.id.tv_task_state);
        activityInstance = this;
        //get user id or group id
        toChatUsername = getIntent().getExtras().getString("userId");
        //use EaseChatFratFragment
       // chatFragment = new ChatFragment();
        //pass parameters to chat fragment
     //   chatFragment.setArguments(getIntent().getExtras());
     //   getSupportFragmentManager().beginTransaction().add(R.id.container, chatFragment).commit();

       // int chatType = getIntent().getIntExtra(EaseConstant.EXTRA_CHAT_TYPE, EaseConstant.CHATTYPE_SINGLE);
       // if (chatType == EaseConstant.CHATTYPE_GROUP) {
            getClubTaskState();
        }
       // mTaskState.setOnClickListener(new View.OnClickListener() {
           //@Override
          //  public void onClick(View v) {
          //      ClubTaskListActivity.startActivity(ChatActivity.this, toChatUsername, false);
      //      }
       // });

    //}

    private void getClubTaskState() {
        Api.getApiService().userClubTaskState(toChatUsername, getToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Action1<BaseBean>() {
                    @Override
                    public void call(BaseBean baseBean) {
                        if (baseBean.isSuccess()) {
                            String state = ((LinkedTreeMap<String, String>) baseBean.getData()).get("finishTaskState");
                            if (state.equals("0")) {
                                mTaskState.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        activityInstance = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // make sure only one chat activity is opened
        String username = intent.getStringExtra("userId");
        if (toChatUsername.equals(username))
            super.onNewIntent(intent);
        else {
            finish();
            startActivity(intent);
        }

    }

   /* @Override
    public void onBackPressed() {
        chatFragment.onBackPressed();
        if (EasyUtils.isSingleActivity(this)) {
            Intent intent = new Intent(this, NewMainActivity.class);
            startActivity(intent);
        }
    }*/

    public String getToChatUsername() {
        return toChatUsername;
    }

    public boolean getBackgroud() {
        return isBackground;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionsManager.getInstance().notifyPermissionsChange(permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isBackground = false;
        NotificationTools.cancleNotification(NotificationTools.NEWS_ID);
      //  DemoHelper.getInstance().getNotifier().cancelNotificatonForeground();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isBackground = true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void exitClubEvent(ExitClubEvent exitClubEvent) {
        if (!TextUtils.isEmpty(exitClubEvent.getGroupId()))
            ChatActivity.this.finish();
    }
}
