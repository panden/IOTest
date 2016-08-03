package netac.iotest.utils.filebroadcast;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *<pre>
 *Created by siwei.zhao on 2016/6/30.
 *实现对多个BroadCastReciverCommon方便的管理
 *</pre>
 * */
public class BroadCastReciverManager{

    protected static BroadCastReciverManager sManager;
    protected Context mContext;
    protected List<BroadCastReciverCommon> mBroadCastReciverCommons;

    protected BroadCastReciverManager(Application app){
        mContext=app.getApplicationContext();
        BroadCastUtils.initBroadCast(app);
        mBroadCastReciverCommons=new ArrayList<BroadCastReciverCommon>();
    }

    /**请在Application的onCreate中初始化Manager*/
    public static void initManager(Application app){
        sManager=new BroadCastReciverManager(app);
    }

    /**获取Manager*/
    public static BroadCastReciverManager getManager(){
        if(sManager==null)throw new IllegalStateException("The BroadCastReciverManager not init. Please init BroadCastReciverManager in Application");
        return sManager;
    }

    //添加已注册的广播
    private void addBroadCastReciver(BroadCastReciverCommon common){
        if(!mBroadCastReciverCommons.contains(common))mBroadCastReciverCommons.add(common);
    }

    //移除已注销的广播
    private void removeBroadcastReciver(BroadCastReciverCommon common){
        if(mBroadCastReciverCommons.contains(common))mBroadCastReciverCommons.remove(common);
    }

    /**在已注册的广播中获取指定的广播*/
    public BroadCastReciverCommon getBroadCastReciver(Class<? extends BroadCastReciverCommon> clz){
        BroadCastReciverCommon common=null;
        for(BroadCastReciverCommon b : mBroadCastReciverCommons){
            if(b.getClass().getName()==clz.getName()){
                common=b;
                break;
            }
        }
        return common;
    }

    /**注销所有已注册的广播*/
    public void unRegistAllRegistedBroadcast(){
        for(BroadCastReciverCommon b:mBroadCastReciverCommons){
            if(b.hasRegister())b.unRegisterBroadCastReciver();
        }
    }

    /**
     * <pre>
     * Created by siwei.zhao on 2016/6/24.
     * #BroadCastReciver 扩展:
     *  1.让BroadCastReciver能够自己进行广播注册和注销(能方便代码的分层);
     *  2.能把广播分发到许多个BroadCastCallBack监听器中，能对BroadCastReciver很好的复用。
     * #说明：BroadCastReciver的广播注册使用的是LocalBroadcastManager(support.v4)，发送的广播只能在APP里发送和接受（安全），比系统全局广播更加高效。
     * #广播的发送使用：BroadCastUtils.getUtils().sendLocalBroadCast()或者BroadCastUtils.getUtils().sendLocalBroadCastSync()。
     * #广播的监听使用：BroadCastReciver需要继承BroadCastReciverCommon；
     *  a.初始化，在Application的onCreate中调用BroadCastUtils.initBroadCast(application)进行一次初始化即可。
     *  b.监听广播(Action)的注册；在继承BroadCastReciverCommon需要重写getRegisterIntentFilter方法，在该方法return出你需要注册监听的广播即可。
     *  c.BroadCastReciverCommon的使用说明:
     *      1.如果你只需要BroadcastReceiver处理简单的事情，就像正常的使用BroadCastReciver一样重写onReciver方法即可；
     *      2.如果你需要处理比较复杂的事情的时候，且不止一个地方需要处理到这些广播，你可以调用registerBroadCastListener，去注册监听器，
     *      当接收到广播之后会把广播分发到各个监听器中，各个监听器对相同的广播去做不同的事情。
     *      注意：如果需要事件能够正常分发到BroadCastCallBack，在重写onReceive方法的时候不要删除super.onReceive方法）。
     * </pre>
     */
    public static abstract class BroadCastReciverCommon extends BroadcastReceiver {

        protected IntentFilter mFilter,mLocalFilter;

        protected List<BroadCastCallBack> mCallBacks;

        /**获取全局广播注册的IntentFilter*/
        public abstract IntentFilter getRegisterIntentFilter();

        /**获取Local广播注册的IntentFilter*/
        public abstract IntentFilter getLocalRegisterIntentFilter();

        protected LocalBroadcastManager getLocalBroadcastManager(){
            return BroadCastUtils.getUtils().getBroadcastManager();
        }

        /**注册广播*/
        public void registerBroadCastReciver(){
            mFilter=getRegisterIntentFilter();
            mLocalFilter=getLocalRegisterIntentFilter();
            boolean reg=BroadCastUtils.getUtils().registerReceiver(this, mFilter, mLocalFilter);
            if(reg)BroadCastReciverManager.getManager().addBroadCastReciver(this);
        }

        /**注销广播*/
        public void unRegisterBroadCastReciver(){
            boolean unReg=BroadCastUtils.getUtils().unRegisterReceiver(this);
            if(unReg)BroadCastReciverManager.getManager().removeBroadcastReciver(this);
        }

        /**是否已注册广播*/
        public boolean hasRegister(){
            return BroadCastUtils.getUtils().hasRegisterReciver(this);
        }

        /**注册广播监听器*/
        public void registerBroadCastListener(BroadCastCallBack listener){
            if(mCallBacks==null)mCallBacks=new ArrayList<BroadCastCallBack>();
            if(listener!=null && !mCallBacks.contains(listener)){
                listener.mBroadCastReciverCommon=BroadCastReciverCommon.this;
                mCallBacks.add(listener);
            }
        }

        /**注销广播监听器*/
        public void unRegisterBroadCastListener(BroadCastCallBack listener){
            if(mCallBacks!=null && mCallBacks.contains(listener)){
                mCallBacks.remove(listener);
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if(mCallBacks!=null){
                for(BroadCastCallBack callBack : mCallBacks){//监听分发
                    if(callBack!=null)callBack.onReceiver(context, intent);
                }
            }
        }


    }

    /**广播监听器*/
    public static abstract class BroadCastCallBack{

        private BroadCastReciverCommon mBroadCastReciverCommon;

        /**获取到广播监听器的广播接收者（BroadCastReciver）*/
        public BroadCastReciverCommon getBroadCastReciverCommon(){
            return mBroadCastReciverCommon;
        }

        /**注销监听*/
        public void unRegisterListener(){
            getBroadCastReciverCommon().unRegisterBroadCastListener(BroadCastCallBack.this);
        }

        /**广播接收（UI线程内，可直接更新UI）*/
        public abstract void onReceiver(Context context, Intent intent);
    }

    /**
     * Created by siwei.zhao on 2016/6/24.
     */
    public static class BroadCastUtils {

        private LocalBroadcastManager mBroadcastManager;
        private Context mContext;
        private static BroadCastUtils sBroadCastUtils;
        private Map<BroadCastReciverCommon, IntentFilter> mBroadcastReceiverIntentFilterMap;

        private BroadCastUtils(Context context){
            mBroadcastManager=LocalBroadcastManager.getInstance(context);
            mContext=context;
        }

        /**广播注册器初始化*/
        public static void initBroadCast(Application application){
            sBroadCastUtils=new BroadCastUtils(application.getApplicationContext());
        }

        public static BroadCastUtils getUtils(){
            if(sBroadCastUtils==null)throw new IllegalStateException("The BroadCastUtils not has init in Application.Please use BroadCastUtils.initBroadCast(application) on Application onCreate() init.");
            return sBroadCastUtils;
        }

        /**获取发送广播的LocalBroadcastManager*/
        public LocalBroadcastManager getBroadcastManager(){
            return mBroadcastManager;
        }

        /**发送广播*/
        public void sendLocalBroadCast(Intent intent){
            mBroadcastManager.sendBroadcast(intent);
        }

        /**发送广播*/
        public void sendLocalBroadCast(String action){
            sendLocalBroadCast(new Intent(action));
        }

        /**发送同步式的广播,在广播事件被处理完之前当前线程会被阻塞住(会阻塞主线程，不推荐使用)*/
        public void sendLocalBroadCastSync(Intent intent){
            mBroadcastManager.sendBroadcastSync(intent);
        }

        /**发送同步式的广播,在广播事件被处理完之前当前线程会被阻塞住(会阻塞主线程，不推荐使用)*/
        public void sendLocalBroadCastSync(String action){
            sendLocalBroadCastSync(new Intent(action));
        }

        /**发送全局广播*/
        public void sendBroadCast(String action){
            sendBroadCast(new Intent(action));
        }

        /**发送全局广播*/
        public void sendBroadCast(Intent intent){
            mContext.sendBroadcast(intent);
        }

        /**注册广播*/
        public boolean registerReceiver(BroadCastReciverCommon receiver, IntentFilter filter, IntentFilter localFilter){
            if(receiver==null)return false;
            if(mBroadcastReceiverIntentFilterMap==null)mBroadcastReceiverIntentFilterMap=new HashMap<BroadCastReciverCommon, IntentFilter>();
            if(mBroadcastReceiverIntentFilterMap.containsKey(receiver))return false;
            if(localFilter!=null)mBroadcastManager.registerReceiver(receiver, localFilter);
            if(filter!=null)mContext.registerReceiver(receiver, filter);
            mBroadcastReceiverIntentFilterMap.put(receiver, filter);
            return filter!=null || localFilter!=null;
        }

        /**注销广播*/
        public boolean unRegisterReceiver(BroadCastReciverCommon receiver){
            if(mBroadcastReceiverIntentFilterMap!=null && mBroadcastReceiverIntentFilterMap.containsKey(receiver)){
                mBroadcastManager.unregisterReceiver(receiver);
                mContext.unregisterReceiver(receiver);
                mBroadcastReceiverIntentFilterMap.remove(receiver);
                return true;
            }
            return false;
        }

        /**判断当前广播是否已注册*/
        public boolean hasRegisterReciver(BroadCastReciverCommon receiver){
            return mBroadcastReceiverIntentFilterMap!=null && mBroadcastReceiverIntentFilterMap.containsKey(receiver);
        }

        public BroadcastReceiver getReciver(IntentFilter filter){
            BroadCastReciverCommon receiver=null;
            if(mBroadcastReceiverIntentFilterMap!=null){
                Iterator iterator=mBroadcastReceiverIntentFilterMap.entrySet().iterator();
                while(iterator.hasNext()){
                    Map.Entry entry= (Map.Entry) iterator.next();
                    if(entry.getValue().equals(receiver)){
                        receiver= (BroadCastReciverCommon) entry.getKey();
                        break;
                    }
                }
            }
            return receiver;
        }

    }

}


