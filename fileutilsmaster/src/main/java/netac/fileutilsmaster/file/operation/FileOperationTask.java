package netac.fileutilsmaster.file.operation;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;

import netac.fileutilsmaster.file.FileCommon;
import netac.fileutilsmaster.file.FileFactory;
import netac.fileutilsmaster.utils.FileMainCallBackHandler;
import netac.fileutilsmaster.utils.Logger;

/**
 * Created by siwei.zhao on 2016/11/16.
 * 文件操作任务
 */

public class FileOperationTask extends FileOperationTaskCommon {

    private MaterialDialog mDialog;

    /**当前dialog能否弹出并更新view*/
    private boolean dialogCanShow=true;


    /**一次读取的最小字节长度*/
    private final int READ_BYTE_MIN_LENGTH=1024*1024;//1M

    protected FileOperationTask(Context context, List<FileCommon> fileCommons, String toDir, @FileTaskInfo.TaskType int taskType) {
        super(context, fileCommons, toDir, taskType);
    }

    @Override
    protected void onOperationCancle() {

    }

    @Override
    protected void onOperationCompleted(boolean isCancleCompleted) {

    }

    @Override
    protected void operationFileCaculationChange(int filesCount, long filesSzie) {

    }

    @Override
    protected @FileTaskInfo.TaskErrorType int excuteSingleTask(FileOperationStatusInfo statusInfo, FileTaskInfo taskInfo, boolean isOverride) {
        @FileTaskInfo.TaskErrorType int taskError=FileTaskInfo.TASK_ERROR_NONE;
        try {
            switch (taskInfo.taskType){
                case FileTaskInfo.TASK_TYPE_COPY://复制文件
                    taskError=copyFile(taskInfo, isOverride);
                    break;
                case FileTaskInfo.TASK_TYPE_DELETE://删除文件
                    taskError=deleteFile(taskInfo);
                    break;
                case FileTaskInfo.TASK_TYPE_DELETE_TO_RECYCLE_BIN://删除到回收站
                    break;
                case FileTaskInfo.TASK_TYPE_MOVE://移动文件
                    taskError=moveFile(taskInfo, isOverride);
                    break;
                case FileTaskInfo.TASK_TYPE_CREATE_DIR://创建目录
                    taskError=createDir(taskInfo);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            taskError=FileTaskInfo.TASK_ERROR_OTHER_FAILD;
        }
        taskInfo.taskErrorType=taskError;
        //一个任务执行完成，更新任务执行进度
        onMainDialogOpeartionViewNotify();
        return taskError;
    }

    /**复制文件*/
    private @FileTaskInfo.TaskErrorType int createDir(FileTaskInfo taskInfo){
        Logger.d("excute createDir=%s", taskInfo.toString());
        boolean bool=FileFactory.getInstance().createFile(taskInfo.toDir).mkdirs();
        return bool?FileTaskInfo.TASK_ERROR_NONE:FileTaskInfo.TASK_ERROR_OTHER_FAILD;
    };

    /**复制文件,并计算文件写入的速度*/
    private @FileTaskInfo.TaskErrorType int copyFile(FileTaskInfo taskInfo, boolean isOverried){
        Logger.d("excute copy=%s", taskInfo.toString());
        @FileTaskInfo.TaskErrorType int errorType=FileTaskInfo.TASK_ERROR_NONE;
        FileChannel in=null, out=null;
        FileInputStream fis=null;
        FileOutputStream fos=null;
        try {
            FileCommon fromFile=FileFactory.getInstance().createFile(taskInfo.fromDir, taskInfo.oldFileName);
            FileCommon toFile=FileFactory.getInstance().createFile(taskInfo.toDir, taskInfo.newFileName);

            fis=fromFile.getFileInputStream();
            fos=toFile.getFileOutputStream();
            long allLength=fromFile.length(), writed=0;
            if(toFile.exists() && isOverried)toFile.delete();
            in=fis.getChannel();
            out=fos.getChannel();
            ByteBuffer buffer=ByteBuffer.allocate(READ_BYTE_MIN_LENGTH);
            int readLength;
            //大文件快速读写，效率比较高
            while((readLength=in.read(buffer))>0){
                buffer.flip();
                out.write(buffer);
                writed+=readLength;
                Logger.d("copy file all=%s, writed=%s, progress=%s", allLength, writed, writed*1.0/allLength);
                onTaskNotify(taskInfo, (float) (writed*1.0/allLength));
                buffer.clear();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            errorType=FileTaskInfo.TASK_ERROR_FILE_NOT_FOUND;
        }catch (IOException e2){
            e2.printStackTrace();
            errorType=FileTaskInfo.TASK_ERROR_IO_FAILD;
        }finally {
            if(fis!=null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(fos!=null){
                try {
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(in!=null){
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(out!=null){
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return errorType;
    };


    /**移动文件，先复制后删除*/
    private @FileTaskInfo.TaskErrorType int moveFile(FileTaskInfo taskInfo, boolean isOverried){
        Logger.d("excute move=%s", taskInfo.toString());
        @FileTaskInfo.TaskErrorType int errorType=copyFile(taskInfo, isOverried);
        if(errorType==FileTaskInfo.TASK_ERROR_NONE)errorType=deleteFile(taskInfo);

        return errorType;
    }

    /**删除目录或者文件, delete from*/
    private int deleteFile(FileTaskInfo taskInfo){
        Logger.d("excute delete=%s", taskInfo.toString());
        FileCommon fileCommon=FileFactory.getInstance().createFile(taskInfo.fromDir, taskInfo.oldFileName);
        @FileTaskInfo.TaskErrorType int errorType=fileCommon.delete()?FileTaskInfo.TASK_ERROR_NONE: FileTaskInfo.TASK_ERROR_OTHER_FAILD;
        return errorType;
    }

    /**单项任务进度改变*/
    private void onTaskNotify(FileTaskInfo taskInfo, float progress){

    }

    /**整个进度的进度改变*/
    private void onOperationNotify(FileOperationWrapper wrapper, float progress){

    }

    /**主线程里更新dialog数据*/
    private void onMainDialogTaskViewNotify(){
        FileMainCallBackHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                dialogViewShow();
            }
        });
    }

    /**主线程里更新dialog数据*/
    private void onMainDialogOpeartionViewNotify(){
        FileMainCallBackHandler.getInstance().post(new Runnable() {
            @Override
            public void run() {
                dialogViewShow();
            }
        });
    }

    /**更新执行的任务的view信息*/
    private void dialogTaskViewNotify(){
        dialogViewShow();
        //更新弹窗内控件的数据
    }

    /**更新总任务进度view信息*/
    private void dialogOperationViewNotify(){
        dialogViewShow();
        //更新弹窗内控件的数据
    }

    /**更新弹窗里控件的信息，需要在主线程里去执行*/
    private void dialogViewShow(){
        if(!dialogCanShow)return;//当前状态下不能弹窗且不能更新view
        if(mDialog==null){
            //创建弹窗
        }
        if(mDialog!=null && !mDialog.isShowing())mDialog.show();


    }


    @Override
    protected void showOperationTaskStatusDialog(Context context) {
        dialogCanShow=true;
        MaterialDialog.Builder builder;
        dialogOperationViewNotify();//更新总任务的任务量
        dialogTaskViewNotify();//更新当前执行任务的任务量
    }

    @Override
    protected void dismissOperationTaskStatusDialog() {
        dialogCanShow=false;
        if(mDialog!=null && mDialog.isShowing())mDialog.dismiss();
    }


    /**创建多文件执行任务*/
    public static FileOperationTaskCommon createFileOperationTask(Context context, List<FileCommon> fileCommons, String toDir, @FileTaskInfo.TaskType int taskType){
        FileOperationTask fileOperationTask=new FileOperationTask(context, fileCommons, toDir, taskType);
        return fileOperationTask;
    }

    /**创建文件执行任务*/
    public static FileOperationTaskCommon createFileOperationTask(Context context, FileCommon fileCommon, String toDir, @FileTaskInfo.TaskType int taskType){
        FileOperationTask fileOperationTask=new FileOperationTask(context, Arrays.asList(fileCommon), toDir, taskType);
        return fileOperationTask;
    }
}
