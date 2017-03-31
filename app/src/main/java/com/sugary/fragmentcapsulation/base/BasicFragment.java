package com.sugary.fragmentcapsulation.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sugary.fragmentcapsulation.utils.RxBus;
import com.zhy.m.permission.MPermissions;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 *
 * Created by Ethan on 2017/03/30.
 * 1.绑定视图  2.RxBus订阅管理  3.Fragment回退栈回调管理  4.危险权限适配
 * 5.bundle解析
 */
public abstract class BasicFragment extends Fragment {

    private BasicActivity mActivity;
    private int mLastChildStackEntryCount = 0;
    private int mLastSupportStackEntryCount = 0;

    /**
     * hide后，再次show一个Fragment时传入的bundle
     */
    private Bundle mSupportArguments;
    /**
     * Subscription对象的容器
     */
    private CompositeSubscription mCompositeSubscription;
    private Unbinder mBind;

    /**
     * 防止fragment onDetach后调用getActivity空指针，不足：有可能内存泄漏
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (BasicActivity)context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseArguments(getArguments());
        setHasOptionsMenu(true);

        getChildFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int currentStackEntryCount = getChildFragmentManager().getBackStackEntryCount();
                if(currentStackEntryCount - mLastChildStackEntryCount < 0){
                    configureToolbar();
                    onChildBackStack();
                }
                mLastChildStackEntryCount = currentStackEntryCount;
            }
        });

        final FragmentManager manager = mActivity.getSupportFragmentManager();
        manager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                int currentStackEntryCount = manager.getBackStackEntryCount();
                if(currentStackEntryCount - mLastSupportStackEntryCount < 0){
                    onSupportBackStack(getAttachActivity().getSupportBackStackArguments());
                }
                mLastSupportStackEntryCount = currentStackEntryCount;
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(getLayoutResID(),container,false);
        mBind = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            parseArguments(getSupportArguments());
        }
    }

    /**
     * Fragment Argument解析
     * @param arguments
     */
    protected void parseArguments(Bundle arguments){

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBind.unbind();
    }

    @Override
    public void onDestroy() {
        if(mCompositeSubscription != null){
            mCompositeSubscription.unsubscribe();
        }
        super.onDestroy();
    }


    private CompositeSubscription getCompositeSubscription(){
        if(mCompositeSubscription == null){
            mCompositeSubscription = new CompositeSubscription();
        }
        return mCompositeSubscription;
    }

    /**
     * 添加RxBus订阅
     * @param tClass
     * @param action1
     * @param errorAction1
     * @param <T>
     */
    protected <T> void addSubscription(Class<T> tClass, Action1<T> action1, Action1<Throwable> errorAction1){
        getCompositeSubscription()
                .add(RxBus.getInstance().toSubscription(tClass, action1,errorAction1));
    }

    public BasicActivity getAttachActivity(){
        return mActivity;
    }


    private Bundle getSupportArguments() {
        return mSupportArguments;
    }

    public void setSupportArguments(Bundle supportArguments) {
        mSupportArguments = supportArguments;
    }

    /**
     * 当replace的置换Fragment时，重新show时，需要更新toolbar信息
     */
    protected void configureToolbar() {
    }

    /**
     * 子Fragment回退栈 回调
     */
    protected void onChildBackStack(){

    }

    /**
     * Fragment里监听虚拟按键和实体按键的返回事件
     * @param bundle
     */
    protected void onSupportBackStack(Bundle bundle){

    }

    /**
     * Fragment 监听回退键
     * @return true 消费，回退到此不再往下
     */
    protected boolean onBackPressed(){

        return false;
    }


    /**
     * 获取要替换的布局ID
     *
     * @return 替换的布局ID
     */
    protected int getChildrenFragmentContainerResID() {
        return -1;
    }

    protected abstract @LayoutRes
    int getLayoutResID();


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MPermissions.onRequestPermissionsResult(this,requestCode,permissions,grantResults);
    }
}
