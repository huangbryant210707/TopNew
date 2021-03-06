package com.kw.top.redpacket;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.kw.top.base.EaseTokenBean;
import com.kw.top.bean.BaseBean;
import com.kw.top.bean.event.AppLoginEvent;
import com.kw.top.bean.event.UserAvatarEvent;
import com.kw.top.retrofit.Api;
import com.kw.top.retrofit.HttpHost;
import com.kw.top.tools.ConstantValue;
import com.kw.top.utils.RxToast;
import com.kw.top.utils.SPUtils;
import com.netease.nim.avchatkit.AVChatKit;
import com.netease.nim.avchatkit.model.IUserInfoProvider;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.business.session.module.MsgForwardFilter;
import com.netease.nim.uikit.business.session.module.MsgRevokeFilter;
import com.netease.nim.uikit.business.uinfo.UserInfoHelper;
import com.netease.nim.uikit.impl.cache.DataCacheManager;
import com.netease.nim.uikit.impl.cache.FriendDataCache;
import com.netease.nim.uikit.impl.cache.NimUserInfoCache;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.auth.AuthService;
import com.netease.nimlib.sdk.auth.LoginInfo;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.uinfo.UserService;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;
import com.netease.nimlib.sdk.uinfo.model.UserInfo;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Des:云信操作统一管理类
 * Created by huang on 2018/9/28.
 */

public class NimManger implements MsgForwardFilter, MsgRevokeFilter {
    private String token;
    private Context mContext;
    private static final NimManger ourInstance = new NimManger();

    public static NimManger instance() {
        return ourInstance;
    }

    public void init(Context context) {
        mContext = context;
        NimUIKit.setMsgRevokeFilter(this);
        NimUIKit.setMsgForwardFilter(this);
        EventBus.getDefault().register(this);
        String netEaseToken = SPUtils.getString(context, ConstantValue.NET_EASE_TOKEN);
        if (!TextUtils.isEmpty(netEaseToken)) {
            netEaseLogin(netEaseToken);
        }
    }

    private NimManger() {
    }

    @Override
    public boolean shouldIgnore(IMMessage message) {
        return true;
    }

    /***
     * 获取网易云token  并保存在本地
     * @param token 自己平台token
     */
    private void netEaseToken(String token) {
        Api.getApiService().EaseToken(token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<BaseBean<EaseTokenBean>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BaseBean<EaseTokenBean> baseBean) {
                        if (baseBean != null && baseBean.isSuccess()) {
                            netEaseLogin(baseBean.getData().getToken());
                        }
                    }
                });
    }

    /***
     * 登录到云信平台
     * @param netEaseToken
     */
    public void netEaseLogin(String netEaseToken) {
        final String account = SPUtils.getString(mContext, ConstantValue.KEY_ACCOUNT);
        NIMClient.getService(AuthService.class)
                .login(new LoginInfo(account, netEaseToken))
                .setCallback(new RequestCallbackWrapper<LoginInfo>() {
                    @Override
                    public void onResult(int code, LoginInfo param, Throwable exception) {
                        if (param != null && code == ResponseCode.RES_SUCCESS) {
                            NimUIKit.loginSuccess(account);
                            AVChatKit.setAccount(account);
                            AVChatKit.setUserInfoProvider(new UserInfoProviderImpl());
                            SPUtils.saveString(mContext, ConstantValue.NET_EASE_TOKEN, param.getToken());

                            updateUserState(ConstantValue.USER_ONLINE);
                        } else {
                            RxToast.normal("登录云信失败");
                        }
                    }
                });
    }


    /***
     * 退出云信平台
     */
    public void netEaseLogOut() {
        NIMClient.getService(AuthService.class).logout();
    }

    /**
     * App登录登出事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginEvent(AppLoginEvent event) {
        if (event.isLogin()) {
            token = event.getToken();
            netEaseToken(event.getToken());
        } else {
            netEaseLogOut();
            NimUIKit.logout();
            DataCacheManager.clearDataCache();
            FriendDataCache.getInstance().clear();
            NimUserInfoCache.getInstance().clear();
            updateUserState(ConstantValue.USER_OFFLINE);
        }
    }

    /**
     * 头像改变事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAvatarEvent(UserAvatarEvent event) {
        String name = TextUtils.isEmpty(event.getName()) ? SPUtils.getString(mContext, ConstantValue.KEY_NAME) : event.getName();
        String avatar = TextUtils.isEmpty(event.getAvatar()) ? SPUtils.getString(mContext, ConstantValue.KEY_HEAD) : event.getAvatar();

        Map<UserInfoFieldEnum, Object> fields = new HashMap<>();
        fields.put(UserInfoFieldEnum.Name, name);
        fields.put(UserInfoFieldEnum.AVATAR, HttpHost.qiNiu + avatar);
        NIMClient.getService(UserService.class).updateUserInfo(fields);
    }

    /***
     * 更新用户在线状态
     */
    public void updateUserState(String state) {
        if (TextUtils.isEmpty(token)) {
            token = SPUtils.getString(mContext, ConstantValue.KEY_TOKEN);
        }
        Api.getApiService().updateUserState(state, token)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<BaseBean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(BaseBean baseBean) {
                        if (baseBean.isSuccess()) {
                            Log.e("----", "用户在线状态更新成功！");
                        }
                    }
                });
    }

    public void onDestroy() {
        updateUserState(ConstantValue.USER_OFFLINE);
        EventBus.getDefault().unregister(this);
    }

    class UserInfoProviderImpl extends IUserInfoProvider {
        @Override
        public UserInfo getUserInfo(final String account) {
            return NimUIKit.getUserInfoProvider().getUserInfo(account);
        }

        @Override
        public String getUserDisplayName(String account) {
            return UserInfoHelper.getUserName(account);
        }
    }
}
