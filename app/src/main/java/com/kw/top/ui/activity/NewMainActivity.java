package com.kw.top.ui.activity;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.kw.top.R;
import com.kw.top.app.AppManager;
import com.kw.top.base.EaseTokenBean;
import com.kw.top.base.MyEaseBaseActivity;
import com.kw.top.bean.BaseBean;
import com.kw.top.bean.FriendApplyBean;
import com.kw.top.bean.VersionBean;
import com.kw.top.bean.event.AppLoginEvent;
import com.kw.top.bean.event.MsgCountEvent;
import com.kw.top.redpacket.NimManger;
import com.kw.top.retrofit.Api;
import com.kw.top.tools.ConstantValue;
import com.kw.top.tools.Logger;
import com.kw.top.ui.activity.login.LoginActivity;
import com.kw.top.ui.fragment.active.NewActivityFragment;
import com.kw.top.ui.fragment.center.CenterFragment;
import com.kw.top.ui.fragment.circle.CircleContentFragment;
import com.kw.top.ui.fragment.find.FindFrament;
import com.kw.top.ui.fragment.find.HomePageFragmnet;
import com.kw.top.ui.fragment.news.NewsFragment;
import com.kw.top.utils.RxToast;
import com.kw.top.utils.SPUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.jpush.android.api.JPushInterface;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 */

public class NewMainActivity extends MyEaseBaseActivity implements TabLayout.OnTabSelectedListener {


    public static boolean isForeground;
    @BindView(R.id.main_container)
    FrameLayout frameLayout;
    @BindView(R.id.tablayout)
    TabLayout tabLayout;
    private List<Fragment> listFragment;
    private String VerName;
    private long mExitTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_main);
        ButterKnife.bind(this);
        initTab();
        initSdk();
        VersionCode();
        updateRegistrationId();   //更新极光id
        isForeground = true;
        NimManger.instance().updateUserState(ConstantValue.USER_ONLINE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        showFragment(HomePageFragmnet.newInstance());
        tabLayout.getTabAt(0).select();
        VersionCode();
        updateRegistrationId();   //更新极光id
    }

    /**
     * 底部导航栏
     */
    private void initTab() {
        VerName = SPUtils.getVerName(this);
        listFragment = new ArrayList<>();
        List<String> listString = Arrays.asList("首页", "消息", "互动圈", "活动", "我");
        for (int i = 0; i < listString.size(); i++) {
            tabLayout.addTab(tabLayout.newTab().setText(listString.get(i)));
        }
        tabLayout.addOnTabSelectedListener(this);
        showFragment(HomePageFragmnet.newInstance());
    }

    private void initSdk() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                try {
                    //some device doesn't has activity to handle this intent
                    //so add try catch
                    Intent intent = new Intent();
                    intent.setAction(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                    intent.setData(Uri.parse("package:" + packageName));
                    startActivity(intent);
                } catch (Exception e) {
                }
            }
        }
    }


    public void refreshUIWithMessage() {
        runOnUiThread(new Runnable() {
            public void run() {
                updateUnreadLabel();
            }
        });
    }


    /**
     * update unread message count
     */
    public void updateUnreadLabel() {
        //   int count = getUnreadMsgCountTotal();
        // if (count > 0) {
//            unreadLabel.setText(String.valueOf(count));
//            unreadLabel.setVisibility(View.VISIBLE);
        //   if (count > 99) {
        // mTvNewsCount.setText("99+");
        //  } else {
        // mTvNewsCount.setText(count + "");
        //  }
        // mTvMsgNew.setVisibility(View.VISIBLE);
        // mTvNewsCount.setVisibility(View.VISIBLE);
        //} else {
//            unreadLabel.setVisibility(View.INVISIBLE);
        // mTvMsgNew.setVisibility(View.GONE);
        // mTvNewsCount.setText("");
        // mTvNewsCount.setVisibility(View.GONE);
        // }
    }

    public void updateRegistrationId() {
        final String registrationid = JPushInterface.getRegistrationID(this);
        Api.getApiService().updateRegistrationId(registrationid, getToken())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        Log.e("-----registrationid", registrationid);
                    }
                })
                .subscribe(new Action1<BaseBean>() {
                    @Override
                    public void call(BaseBean baseBean) {
                        if (baseBean == null) {
                            RxToast.normal("极光id更新失败");
                        }

                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    /**
     * get unread event notification count, including application, accepted, etc
     *
     * @return
     */
    public int getUnreadAddressCountTotal() {
        int unreadAddressCountTotal = 0;
        return unreadAddressCountTotal;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshMsgNum(MsgCountEvent countEvent) {
        if (countEvent.isMsg())
            refreshUIWithMessage();
    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {

        switch (tab.getPosition()) {
            case 0:
                showFragment(HomePageFragmnet.newInstance());
                break;
            case 1:
                showFragment(NewsFragment.newInstance());
                break;
            case 2:
                showFragment(CircleContentFragment.newInstance());
                break;
            case 3:
                showFragment(NewActivityFragment.newInstance());
                break;
            case 4:
                showFragment(CenterFragment.newInstance());
                break;
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


    /**
     * 显示Fragment
     */
    private void showFragment(Fragment fragment) {
        if (fragment == null) {
            return;
        }
        if (!listFragment.contains(fragment)) {
            listFragment.add(fragment);
        }
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (!fragment.isAdded()) {
            transaction.add(R.id.main_container, fragment);
        }

        hideFragment(transaction);
        transaction.show(fragment);
        transaction.commit();
    }


    /**
     * 隐藏所有的fragment
     */
    private void hideFragment(FragmentTransaction transaction) {
        for (int i = 0; i < listFragment.size(); i++) {
            if (listFragment.get(i) != null) {
                transaction.hide(listFragment.get(i));
            }
        }
    }


    private void VersionCode() {
        String token = getToken();
        Api.getApiService().queryVersion("02", token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Action1<BaseBean<VersionBean>>() {
                    @Override
                    public void call(BaseBean<VersionBean> baseBean) {
                        if (VerName.equals(baseBean.getData().getVersion())) {
                            return;
                        }
                        showVerSionDialog();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }


    /**
     * 版本更新dialog
     */
    private void showVerSionDialog() {
        new AlertDialog.Builder(this)
                .setMessage("发现新版本，是否更新？")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse("https://fir.im/6phf");//此处填链接
                        intent.setData(content_url);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * 点击系统返回键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (System.currentTimeMillis() - mExitTime > 2000) {
                Toast.makeText(this, getResources().getString(R.string.back_again), Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                isForeground = false;
                NimManger.instance().onDestroy();
                AppManager.getAppManager().finishAllActivity();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static void logout() {
        //NimManger.instance().onLoginEvent(new AppLoginEvent(false));
    }

    @SuppressLint("MissingSuperCall")
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //不做任何保存操作  防止程序意外崩溃重启后fragment的视图叠加
    }
}
