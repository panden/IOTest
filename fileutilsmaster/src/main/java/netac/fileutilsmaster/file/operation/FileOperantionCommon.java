package netac.fileutilsmaster.file.operation;

import java.util.concurrent.ExecutorService;

import netac.fileutilsmaster.utils.Logger;

/**
 * Created by siwei.zhao on 2016/11/7.
 * 文件操作执行线程
 */

public abstract class FileOperantionCommon extends Thread{

    private ThreadListener mThreadListener;//线程监听
    private boolean hasExcute;//线程是否已被执行

    /**获取任务id*/
    public abstract long getTaskId();

    /**文件操作执行*/
    protected abstract void fileOperantionRun();

    /**开始操作*/
    public void startOperation(ExecutorService service){
        if(service==null || hasExcute)return;
        hasExcute=true;
        service.execute(FileOperantionCommon.this);
    };

    /**结束操作*/
    public void stopOperation(){
        hasExcute=false;
    };

    /**当前是否继续运行*/
    protected boolean isCancleExcute(){
        return !hasExcute;
    }

    public void setThreadListener(ThreadListener listener){
        this.mThreadListener=listener;
    }


    @Override
    public void run() {
        super.run();
        Logger.d("FileOperantionCommon run");
        if(mThreadListener!=null)mThreadListener.onThreadStart(getTaskId());
        try {
            fileOperantionRun();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(mThreadListener!=null)mThreadListener.onThreadStop(getTaskId());
    }

    public interface ThreadListener{

        void onThreadStart(long taskId);

        void onThreadStop(long taskId);
    }
}
