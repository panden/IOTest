package netac.fileutilsmaster.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by siwei.zhao on 2016/9/20.
 * 主线程回调Handler
 */
public class FileMainCallBackHandler extends Handler {

    private final int WHAT_MAIN_CLASS_CALLBACK =1;

    /**默认延时时长*/
    private final int DEFAULT_DELAY_TIME=0;

    private ReentrantLock lock;//原子变量实现线程锁

    private static FileMainCallBackHandler sMainCallBackHandler;

    protected FileMainCallBackHandler(){
        super(Looper.getMainLooper());
        lock=new ReentrantLock();
    }

    public static FileMainCallBackHandler getInstance(){
        if(sMainCallBackHandler==null)sMainCallBackHandler=new FileMainCallBackHandler();
        return sMainCallBackHandler;
    }

    public static ClassCallBackBuilder createBuilder(){
        return new ClassCallBackBuilder();
    }


    /**在主线程执行回调(高频率回调会出现延时的情况，只适合少频率的回调，回调会被先后一个一个的去执行)*/
    public void onMainCallBack(ClassCallBackBuilder builder){
        if(builder==null)return;
        this.sendMessage(this.obtainMessage(WHAT_MAIN_CLASS_CALLBACK, builder));
    }

    /**主线程进行回调*/
    public void onMainCallBackRun(Runnable runnable){
        if(runnable==null)return;
        this.post(runnable);
    }

    /**在主线程进行延时回调*/
    public void onMainCallBackRunDelay(Runnable runnable, int delayTime){
        if(runnable==null)return;
        int time=delayTime<0?DEFAULT_DELAY_TIME:delayTime;
        this.postDelayed(runnable, time);
    }

    /**在主线程执行*/
    private void onMainExcute(ClassCallBackBuilder builder){
        try {
            Method m=builder.clazz.getDeclaredMethod(builder.method, builder.parameterTypes);
            m.setAccessible(true);
            m.invoke(builder.objClazz, builder.params);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what){
            case WHAT_MAIN_CLASS_CALLBACK:
                onMainExcute((ClassCallBackBuilder) msg.obj);
                break;
        }
    }


    public static class ClassCallBackBuilder {

        private Object objClazz;

        private Class clazz;

        private String method;

        private Class[] parameterTypes;

        private Object[] params;

        public ClassCallBackBuilder setObjClazz(Object objClazz) {
            this.objClazz = objClazz;
            return this;
        }

        public ClassCallBackBuilder setMethod(String method) {
            this.method = method;
            return this;
        }

        public ClassCallBackBuilder setClazz(Class clazz) {
            this.clazz = clazz;
            return this;
        }

        public ClassCallBackBuilder setParameterTypes(Class<?>... parameterTypes) {
            this.parameterTypes = parameterTypes;
            return this;
        }

        public ClassCallBackBuilder setParams(Object... params) {
            this.params = params;
            return this;
        }

        private ClassCallBackBuilder(){};

    }

}
