package com.kw.top.ui.fragment.club;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.kw.top.R;
import com.kw.top.base.BaseFragment;

import butterknife.BindView;

/**
 * author: Administrator
 * data  : 2018/5/6
 * des   : 俱乐部
 */

public class ClubFragment extends BaseFragment implements RadioGroup.OnCheckedChangeListener,View.OnClickListener{

    @BindView(R.id.rb_left)
    RadioButton mRbLeft;
    @BindView(R.id.rb_right)
    RadioButton mRbRight;
    @BindView(R.id.radio_group_two)
    RadioGroup mRadioGroupTwo;
    @BindView(R.id.frame_layout_two)
    FrameLayout mFrameLayoutTwo;

    private ClubContentFragment mMyJoinFragment,mAllClubFragment;
    private FragmentManager mFragmentManager;
    private FragmentTransaction mTransaction;

    public static ClubFragment newInstance() {
        ClubFragment fragment = new ClubFragment();
        return fragment;
    }

    @Override
    public int getContentView() {
        return R.layout.frament_club;
    }

    @Override
    public void initView(View view, Bundle savedInstanceState) {

    }

    @Override
    public void initListener() {
        mRadioGroupTwo.setOnCheckedChangeListener(this);
    }

    @Override
    public void initData() {
        mFragmentManager = getActivity().getSupportFragmentManager();
        mTransaction = mFragmentManager.beginTransaction();
        if (null == mMyJoinFragment){
            mMyJoinFragment = ClubContentFragment.newInstance(0);
            mTransaction.add(R.id.frame_layout_two,mMyJoinFragment);
        }
        mTransaction.show(mMyJoinFragment);
        mTransaction.commit();
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        mTransaction = mFragmentManager.beginTransaction();
        switch (checkedId){
            case R.id.rb_left:
                if (null != mAllClubFragment){
                    mTransaction.hide(mAllClubFragment);
                }
                if (null == mMyJoinFragment){
                    mMyJoinFragment = ClubContentFragment.newInstance(0);
                    mTransaction.add(R.id.frame_layout_two,mMyJoinFragment);
                }
                mTransaction.show(mMyJoinFragment);
                break;
            case R.id.rb_right:
                if (null != mMyJoinFragment){
                    mTransaction.hide(mMyJoinFragment);
                }
                if (null == mAllClubFragment){
                    mAllClubFragment = ClubContentFragment.newInstance(1);
                    mTransaction.add(R.id.frame_layout_two,mAllClubFragment);
                }
                mTransaction.show(mAllClubFragment);
                break;
        }
        mTransaction.commit();
    }
}
