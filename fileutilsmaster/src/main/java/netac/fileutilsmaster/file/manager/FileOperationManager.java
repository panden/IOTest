package netac.fileutilsmaster.file.manager;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import netac.fileutilsmaster.file.FileCommon;
import netac.fileutilsmaster.file.FileFactory;
import netac.fileutilsmaster.file.operation.FileOperationTask;
import netac.fileutilsmaster.file.operation.FileOperationTaskCommon;
import netac.fileutilsmaster.file.operation.FileOperationWrapper;
import netac.fileutilsmaster.file.operation.FileTaskInfo;
import netac.fileutilsmaster.utils.DocumentTreePermissionUtils;
import netac.fileutilsmaster.utils.FileUtils;

/**
 * Created by siwei.zhao on 2016/11/14.
 * 文件操作的任务管理器
 */

public class FileOperationManager {

    /**文件操作的最大线程数*/
    private final int MAX_FILE_OPERATION_THREAD_COUNT=5;

    private static FileOperationManager sOperationManager;
    private ExecutorService mExecutorService;
    private List<OperationChangeListener> mOperationChangeListeners;

    private FileOperationManager(){
        mExecutorService= Executors.newFixedThreadPool(MAX_FILE_OPERATION_THREAD_COUNT);
        mOperationChangeListeners=new ArrayList<>();
    };

    public static FileOperationManager getInstance(){
        if(sOperationManager==null)sOperationManager=new FileOperationManager();
        return sOperationManager;
    }


    /**开始执行线程*/
    private void startOperation(@NonNull FileOperationWrapper wrapper){
        //判断任务时单文件任务还是文件夹任务
//        mExecutorService.submit(common);
    }

    private void startOperations(@NonNull List<FileOperationWrapper> wrappers){
        if(wrappers==null)return;
        for(FileOperationWrapper wrapper : wrappers)startOperation(wrapper);
    }

    //任务状态监听****************************************************************

    //任务状态获取****************************************************************


    //任务创建********************************************************************
    //复制========================================================================

    /**复制文件,创建任务失败返回-1*/
    public long copyFile(@NonNull Activity context, FileCommon fileCommon, String toDir){
        if(!DocumentTreePermissionUtils.getInstance().checkPathPermission(context, fileCommon.getAbsolutePath(), toDir))return -1;
        if(checkFileUsing(context, fileCommon))return -1;
        FileOperationTaskCommon task= FileOperationTask.createFileOperationTask(context, fileCommon, toDir, FileTaskInfo.TASK_TYPE_COPY);
        return task.getTaskId();
    }

    /**多文件复制,创建任务失败返回-1*/
    public long copyFiles(@NonNull Activity context, List<FileCommon> fileCommons, String toDir){
        if(!DocumentTreePermissionUtils.getInstance().checkPathPermission(context, fileCommons.get(0).getAbsolutePath(), toDir))return -1;
        if(checkFileUsing(context, fileCommons))return -1;
        FileOperationTaskCommon task= FileOperationTask.createFileOperationTask(context, fileCommons, toDir, FileTaskInfo.TASK_TYPE_COPY);
        task.startOperation(mExecutorService);
        return task.getTaskId();
    }

    //剪切=========================================================================
    /**剪切文件,创建任务失败返回-1*/
    public long cutFile(@NonNull Activity context, FileCommon fileCommon, String toDir){
        if(!DocumentTreePermissionUtils.getInstance().checkPathPermission(context, fileCommon.getAbsolutePath(), toDir))return -1;
        if(checkFileUsing(context, fileCommon))return -1;
        FileOperationTaskCommon task= FileOperationTask.createFileOperationTask(context, fileCommon, toDir, FileTaskInfo.TASK_TYPE_MOVE);
        return task.getTaskId();
    }

    /**剪切多个文件,创建任务失败返回-1*/
    public long cutFiles(@NonNull Activity context, List<FileCommon> fileCommons, String toDir){
        if(!DocumentTreePermissionUtils.getInstance().checkPathPermission(context, fileCommons.get(0).getAbsolutePath(), toDir))return -1;
        if(checkFileUsing(context, fileCommons))return -1;
        FileOperationTaskCommon task= FileOperationTask.createFileOperationTask(context, fileCommons, toDir, FileTaskInfo.TASK_TYPE_MOVE);
        return task.getTaskId();
    }

    //删除=========================================================================

    /**删除文件,创建任务失败返回-1*/
    public long deleteFile(@NonNull Activity context, FileCommon fileCommon){
        if(!DocumentTreePermissionUtils.getInstance().checkPathPermission(context, fileCommon.getAbsolutePath()))return -1;
        if(checkFileUsing(context, fileCommon))return -1;
        FileOperationTaskCommon task= FileOperationTask.createFileOperationTask(context, fileCommon, null, FileTaskInfo.TASK_TYPE_DELETE);
        return task.getTaskId();
    }

    /**删除多个文件,创建任务失败返回-1*/
    public long deleteFiles(@NonNull Activity context, List<FileCommon> fileCommons){
        if(!DocumentTreePermissionUtils.getInstance().checkPathPermission(context, fileCommons.get(0).getAbsolutePath()))return -1;
        if(checkFileUsing(context, fileCommons))return -1;
        FileOperationTaskCommon task= FileOperationTask.createFileOperationTask(context, fileCommons, null, FileTaskInfo.TASK_TYPE_DELETE);
        return task.getTaskId();
    }


    //创建=========================================================================
    public void createFile(@NonNull final Activity context, final boolean isFile, final String parentPath){
        final MaterialDialog.Builder builder=new MaterialDialog.Builder(context);
        builder.title(isFile?"创建文件":"创建文件")
                .content("请输入文件名称")
                .positiveText("创建")
                .negativeText("取消")
                .input("请输入文件名", isFile?"新建文件.txt":"新建文件夹", new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                    }
                }).onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which){
                            case NEGATIVE://取消
                                dialog.dismiss();
                                break;
                            case POSITIVE://创建
                                if(!FileUtils.checkFileNameStandard(isFile, dialog.getInputEditText().getText().toString())){
                                    dialog.getInputEditText().setText("");
                                    dialog.getInputEditText().setHint("文件名格式不对");
                                }else{
                                    //创建文件或者文件夹
                                    if(DocumentTreePermissionUtils.getInstance().checkPathPermission(context, parentPath)){//检查是否有操作权限
                                        FileCommon common=FileFactory.getInstance().createFile(parentPath, dialog.getInputEditText().getText().toString());
                                        if(common.exists()){//判断文件是否存在
                                            dialog.getInputEditText().setText("");
                                            dialog.getInputEditText().setHint("文件名已存在");
                                        }else{//创建文件
                                            boolean bool;
                                            if(isFile)bool=common.createNewFile();
                                            else bool=common.mkdirs();
                                            dialog.dismiss();
                                            if(!bool) Toast.makeText(context, "创建失败", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                break;
                        }
                    }
                });
        MaterialDialog dialog=builder.build();
        dialog.show();
    }


    //任务检查**********************************************************************

    /**检查文件是否正在进行任务操作*/
    private boolean checkFileUsing(FileCommon fileCommon){
        //检查该文件是否在其他任务中被占用
        return false;
    }

    /**检查文件是否正在进行任务操作，如果已被占用会给出提示*/
    private boolean checkFileUsing(@NonNull Context context, List<FileCommon> fileCommons){
        for(FileCommon common : fileCommons){
            if(checkFileUsing(common)){
                Toast.makeText(context, String.format("%s文件已被占用，无法操作", common.getName()), Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    /**检查文件是否正在进行任务操作，如果已被占用会给出提示*/
    private boolean checkFileUsing(@NonNull Context context, FileCommon common){
        if(checkFileUsing(common)){
            Toast.makeText(context, String.format("%s文件已被占用，无法操作", common.getName()), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    //监听回调接口================================================================

    /**注册任务状态改变监听*/
    public boolean registerOperationChangeListener(OperationChangeListener listener){
        if(listener==null || mOperationChangeListeners.contains(listener))return false;
        mOperationChangeListeners.add(listener);
        return true;
    }

    /**注销任务状态改变监听*/
    public boolean unRegisterOperationChangeListener(OperationChangeListener listener){
        if(listener==null || !mOperationChangeListeners.contains(listener))return false;
        mOperationChangeListeners.remove(listener);
        return true;
    }

    /**任务状态或者数量发生改变回调*/
    public interface OperationChangeListener{

        /**任务发生改变*/
        void onOperationChange();

    }
}
