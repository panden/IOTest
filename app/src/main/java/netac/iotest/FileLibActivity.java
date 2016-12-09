package netac.iotest;

import android.annotation.TargetApi;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CompoundButton;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import netac.fileutilsmaster.file.ExtrageFile;
import netac.fileutilsmaster.file.FileCommon;
import netac.fileutilsmaster.file.FileFactory;
import netac.fileutilsmaster.file.manager.FileOperationManager;
import netac.fileutilsmaster.file.vo.StorageDeviceInfo;
import netac.fileutilsmaster.file.wrapper.StorageDeviceWrapper;
import netac.fileutilsmaster.utils.DocumentTreePermissionUtils;
import netac.fileutilsmaster.utils.Logger;

import static android.webkit.MimeTypeMap.getSingleton;

public class FileLibActivity extends AppCompatActivity {

    private Button get_permission_btn, same_check_test, operation_file_btn;
    private List<String> names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_lib);
        initView();
        initData();
        initEvent();
    }

    private void initView(){
        get_permission_btn= (Button) findViewById(R.id.get_permission_btn);
        same_check_test= (Button) findViewById(R.id.same_check_test);
        operation_file_btn= (Button) findViewById(R.id.operation_file_btn);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void initData(){
        DocumentFile file;
        System.out.println("modal="+Build.BRAND);
        FileFactory.getInstance().getFileWrapper().registerStorageDeviceChanged(mStorageDeviceChangeListener);
//        Uri newUri=Uri.parse("content://com.android.externalstorage.documents/document/A023-786C/DCIM");
//        try {
//            System.out.println("grant permission");
//            ContentProviderClient providerClient=getContentResolver().acquireUnstableContentProviderClient("com.android.externalstorage.documents");
//            System.out.println("providerClient="+providerClient);
//            ContentProvider provider=providerClient.getLocalContentProvider();
//            System.out.println("ContentProvider="+provider);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //System.out.println("query name="+ GetPathFromUri4kitkat.getDataColumn(this, newUri, null, null));

        Map<String, String> envs=System.getenv();
        Iterator iterator1=envs.entrySet().iterator();
        while(iterator1.hasNext()){
            Map.Entry entry= (Map.Entry) iterator1.next();
            Logger.d("key=%s;  value=%s;", entry.getKey(), entry.getValue());
        }


        names=new ArrayList<>();
        names.add("aaaa.txt");
        names.add("bbbb.txt");
        names.add("cccc.txt");
        names.add("dddd.txt");
        names.add("eeee.txt");
        names.add("ffff.txt");
        String type1= getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl("aaa"));
        String type2=MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl("aaa.abc"));
        System.out.println("========mime1="+type1+"====mime2="+type2);
        boolean bool=new File("/storage/5699-0266/test.apk").renameTo(new File("/storage/5699-0266/ppt/test2.apk"));
        System.out.println("file rename="+bool);
        bool=FileFactory.getInstance().createFile("/storage/5699-0226/test_rename.txt").renameTo("/storage/5699-0226/ppt/test_rename.txt");
        System.out.println("Rename Document="+bool);
    }

    private void initEvent(){
        get_permission_btn.setOnClickListener(mClickListener);
        same_check_test.setOnClickListener(mClickListener);
        operation_file_btn.setOnClickListener(mClickListener);
    }

    private void operationFile(){
        String fromPath="/storage/5699-0226/DCIM";
        String fromPath2="/storage/5699-0226/音乐";
        String fromPath3="/storage/5699-0226/ppt";
        String fromPath4="/storage/5699-0226/ppt2";
        String fromPath5="/storage/5699-0226/ppt3";
        String fromPath6="/storage/5699-0226/test.txt";
        String toDir="/storage/5699-0226/Android";
        String toDir2="/storage/5699-0226";
        String toDir3="/storage/5699-0226/test";
//        String toDir="/storage/emulated/0/face";
        System.out.println("from path="+ fromPath +" list file="+new File(fromPath).listFiles());
        List<FileCommon> fileCommons=Arrays.asList(FileFactory.getInstance().createFile(fromPath3), FileFactory.getInstance().createFile(fromPath4), FileFactory.getInstance().createFile(fromPath5));
        long id=FileOperationManager.getInstance().copyFiles(FileLibActivity.this, fileCommons, toDir3);
        Logger.d("FileOperationManager copyFiles id="+id);

//        FileOperationTask operationTask= (FileOperationTask) FileOperationTask.createFileOperationTask(this, fileCommons, toDir3, FileTaskInfo.TASK_TYPE_COPY);
//        ExecutorService service=Executors.newFixedThreadPool(5);
//        operationTask.startOperation(service);
    }

    View.OnClickListener mClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.get_permission_btn:
                    getPermission();
                    break;
                case R.id.same_check_test:
                    operationCheck(new ArrayList<String>(names), 0);
                    break;
                case R.id.operation_file_btn:
                    operationFile();
                    break;
            }
        }
    };

    private void checkSuccess(List<String> names){
        System.out.println("checkSuccess names length="+names.size());
    }

    /**任务确认的时候，确认任务的列表中文件夹任务必须在前，文件任务必须在后</br>
     * 在确认文件夹重复任务的时候，文件夹只包含覆盖和取消的功能
     * 在确认文件重复任务的时候，文件包含覆盖、重命名、取消的功能*/
    private void operationCheck(final List<String> wrappers, int pos){
        System.out.println("wrappers.size()="+wrappers.size());
        if(wrappers==null || wrappers.size()<=0 || pos==wrappers.size()){
            checkSuccess(wrappers);
            return;
        }
        final String wrapper=wrappers.get(pos);
        pos++;
        if(true){
            final boolean isFile=true;
            final boolean[] checkAll = {false};
            MaterialDialog.Builder builder=new MaterialDialog.Builder(this);
            builder.title("操作文件重复提示")
                    .content(wrapper+"已存在，请选择您要进行的操作？")
                    .neutralText("取消")
                    .positiveText("覆盖")
                    .autoDismiss(false)
                    .checkBoxPrompt("应用到所有", false, new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            checkAll[0] =isChecked;
                        }
                    })
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            int tagNextPos= (int) dialog.getTitleView().getTag();
                            switch (which){
                                case POSITIVE://覆盖(文件和目录都进行覆盖)
                                    if(checkAll[0]){//应用到所有
                                        for(int i=tagNextPos-1; i< wrappers.size(); i++){
                                            System.out.println(wrappers.get(i)+" SAME_FILE_DIR_OVERRIED");
                                        }
                                        dialog.dismiss();
                                        operationCheck(wrappers, wrappers.size());
                                    }else{
                                        System.out.println(wrapper+" SAME_FILE_DIR_OVERRIED");
                                        dialog.dismiss();
                                        operationCheck(wrappers, tagNextPos);
                                    }
                                    break;
                                case NEGATIVE://重命名(只包含文件)
                                    //生成一个新的文件名并确保该文件名对应的文件不存在
                                    if(checkAll[0]){//应用到所有
                                        for(int i=tagNextPos-1; i< wrappers.size(); i++){
                                            //自动生成一个新的文件名且和之前的文件不重复
                                            System.out.println(wrappers.get(i)+"  SAME_FILE_RENAME   newName="+getNewFileName(wrappers.get(i)));
                                        }
                                        dialog.dismiss();
                                        operationCheck(wrappers, wrappers.size());
                                    }else{
                                        System.out.println(wrapper+"  SAME_FILE_RENAME   newName="+getNewFileName(wrapper));
                                        dialog.dismiss();
                                        operationCheck(wrappers, tagNextPos);
                                    }
                                    break;
                                case NEUTRAL://取消
                                    if(checkAll[0]){
                                        wrappers.clear();
                                        dialog.dismiss();
                                        operationCheck(wrappers, wrappers.size());
                                    }else{
                                        wrappers.remove(tagNextPos);
                                        dialog.dismiss();
                                        operationCheck(wrappers, tagNextPos);
                                    }
                                    break;
                            }

                        }
                    });
            if(isFile)builder.negativeText("重命名");
            MaterialDialog operationCheckDialog=builder.build();
            operationCheckDialog.getTitleView().setTag(pos);
            operationCheckDialog.show();
        }else {
            if(pos<wrappers.size())operationCheck(wrappers, pos);
        }
    }

    /**获取一个新的文件名，在文件的名称后追加(Num)，以保证生成的文件是不存在的*/
    private String getNewFileName(String fileName){
        int nameTag=1;
        String name=spliteFileName(fileName),format=spliteFileFormat(fileName), newFileName="";
        boolean exists=true;
        while(exists){
            newFileName=String.format("%s(%s)%s", name, String.valueOf(nameTag), format);
            System.out.println("newFileName="+newFileName);
            nameTag++;
            exists=false;
        }
        return newFileName;
    }

    private String spliteFileName(String fileName){
        if(fileName.indexOf(".")<0){
            return "";
        }else{
            return fileName.substring(0, fileName.lastIndexOf(".")-1);
        }
    }

    private String spliteFileFormat(String fileName){
        if(fileName.indexOf(".")<0){
            return "";
        }else{
            return fileName.substring(fileName.lastIndexOf("."), fileName.length());
        }
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void getPermission(){
        DocumentTreePermissionUtils.getInstance().getDocumentTreePermission(FileLibActivity.this, StorageDeviceInfo.StorageDeviceType.UsbDevice);
//        try {
//            Intent intent=new Intent("com.android.documentsui.DIRECTORY_COPY");
//            intent.setData(DocumentsContract.buildRootUri("com.android.externalstorage.documents", "A023-786C"));
//            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
//                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
//                    | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
//            startActivity(intent);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(String.format("onActivityResult requestCode=%d, resultCode=%d, data=%s", requestCode, resultCode, data));
        if(requestCode==1346 && data!=null){
            System.out.println("get permission url="+data.getData()+"  auth="+data.getData().getAuthority());
            //getProvider(data.getData());
            StorageDeviceInfo.StorageDeviceType type=DocumentTreePermissionUtils.getInstance().onPermissionReciver(data, this);
//            FileCommon fileCommon=FileFactory.getInstance().createFile("/storage/sdcard1/test/test.txt");
//            System.out.println("create file="+fileCommon.createNewFile());
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void getProvider(Uri uri){
        //content://com.android.externalstorage.documents/document/A023-786C%3ADCIM%2FCamera%2FIMG_20150717_162704.jpg
        //uri持久化
        getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        System.out.println("getContentResolver class="+getContentResolver().getClass().getPackage());
        ContentProviderClient contentProviderClient = getContentResolver().acquireUnstableContentProviderClient(uri.getAuthority());
        System.out.println("get ContentProviderClient=" + contentProviderClient);
        Uri rootUri = DocumentsContract.buildDocumentUri(uri.getAuthority(), "A023-786C");
        System.out.println("get root id=" + rootUri);
        try {
            Class storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            StorageManager storageManager= (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            Method getVolumeList=storageManager.getClass().getMethod("getVolumeList");
            Method isPrimary=storageVolumeClazz.getMethod("isPrimary");
            Method isEmulated=storageVolumeClazz.getMethod("isEmulated");
            Method getUuid=storageVolumeClazz.getMethod("getUuid");
            Method getPathFile=storageVolumeClazz.getMethod("getPathFile");
            Object result = getVolumeList.invoke(storageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                boolean isP= (boolean) isPrimary.invoke(storageVolumeElement);
                boolean isE= (boolean) isEmulated.invoke(storageVolumeElement);
                if(isP && isE){
                    System.out.println("root id=ROOT_ID_PRIMARY_EMULATED");
                }else{
                    System.out.println("root id="+getUuid.invoke(storageVolumeElement));
                }
                File file= (File) getPathFile.invoke(storageVolumeElement);
                System.out.println("path file="+file.getAbsolutePath());

            }

//            Class activityManagerNativeClazz= Class.forName("android.app.ActivityManagerNative");
//            Class activityManagerProxyClazz=Class.forName("android.app.ActivityManagerProxy");
//            Method getDefault=activityManagerNativeClazz.getMethod("getDefault");
//            Class iApplicationThread=Class.forName("android.app.IApplicationThread");
//            Method grantUriPermission=activityManagerProxyClazz.getMethod("grantUriPermission", new Class[]{iApplicationThread, String.class, Uri.class, int.class});
//            //Method onTransact=activityManagerNativeClazz.getMethod("onTransact", new Class[]{int.class, Parcel.class, Parcel.class, int.class});
//            //int GRANT_URI_PERMISSION_TRANSACTION=activityManagerNativeClazz.getField("GRANT_URI_PERMISSION_TRANSACTION").getInt(activityManagerNativeClazz);
//            Object activityManagerProxy=getDefault.invoke(activityManagerNativeClazz);
//            Parcel data=Parcel.obtain();
//            data.enforceInterface("android.app.IActivityManager");
//            Class pracelClazz=Class.forName("android.os.Parcel");
//            Method readStrongBinder=pracelClazz.getMethod("readStrongBinder");
//            IBinder iBinder= (IBinder) readStrongBinder.invoke(data);
//            Class applicationThreadNativeClazz=Class.forName("android.app.ApplicationThreadNative");
//            Method asInterface=applicationThreadNativeClazz.getMethod("asInterface", IBinder.class);
//            Object app=asInterface.invoke(applicationThreadNativeClazz, iBinder);
//            String packageName=getApplication().getPackageName();
//            Uri uri1=Uri.CREATOR.createFromParcel(data);
//            grantUriPermission.invoke(activityManagerProxy, app, packageName, uri1, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        } catch (Exception e) {
            e.printStackTrace();
        }


        DocumentFile documentFile=DocumentFile.fromFile(new File(System.getenv("SECONDARY_STORAGE")));
        DocumentFile childDoc=documentFile.createFile(ExtrageFile.DIRECTORY_MIMETYPE, "testDir");
        System.out.println("documentFile.getName="+documentFile.getName()+"  childDoc="+childDoc);
    }

    StorageDeviceWrapper.StorageDeviceChangeListener mStorageDeviceChangeListener=new StorageDeviceWrapper.StorageDeviceChangeListener() {
        @Override
        public void onDeviceMountedStatusChanged(boolean isMounted, StorageDeviceInfo.StorageDeviceType storageDeviceType) {
            System.out.println("onDeviceMountedStatusChanged isMounted="+isMounted+"; type="+storageDeviceType);
        }

        @Override
        public void onStorageDeviceChanged(List<StorageDeviceInfo> deviceInfos) {
            System.out.println("onStorageDeviceChanged size="+deviceInfos.size());
        }
    };



}
