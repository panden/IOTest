package netac.fileutilsmaster.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import netac.fileutilsmaster.activity.DocumentPermissionActivity;
import netac.fileutilsmaster.file.FileFactory;
import netac.fileutilsmaster.file.serializable.DocumentUriSerializable;
import netac.fileutilsmaster.file.vo.StorageDeviceInfo;
import netac.fileutilsmaster.file.wrapper.StorageDeviceWrapper;

/**
 * Created by siwei.zhao on 2016/9/18.
 * 只有在API 19以上才需要执行
 */
public class DocumentTreePermissionUtils {

    private static DocumentTreePermissionUtils sUtils;

    private Map<StorageDeviceInfo.StorageDeviceType, DocumentFile> mDocumentPermissionMaps;



    private DocumentTreePermissionUtils(){
        mDocumentPermissionMaps=new HashMap<>();
    };

    public static DocumentTreePermissionUtils getInstance(){
        if(sUtils==null)sUtils=new DocumentTreePermissionUtils();
        return sUtils;
    }


    /**获取document tree的访问权限,会弹出指导页面
     * Android 4.4 API 19
     * */
    public void getDocumentTreePermission(Activity activity, StorageDeviceInfo.StorageDeviceType type){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT || hasGrantPermission(type))return;
        Intent intent=new Intent(activity, DocumentPermissionActivity.class);
        intent.putExtra(DocumentPermissionActivity.EXTRAL_STORAGE_TYPE, type.ordinal());
        activity.startActivity(intent);
    }

    /**获取document tree的访问权限
     * Android 4.4 API 19
     * */
    public void getDocumentTreePermission(Activity activity, int requestCode){
        if(Build.VERSION.SDK_INT<19)return;
        Intent intent=null;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)intent=new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            else{
                intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                intent.setType("*/*");
            }
        }
        activity.startActivityForResult(intent, requestCode);
    }

    /**权限返回*/
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public StorageDeviceInfo.StorageDeviceType onPermissionReciver(Intent data, Context context){
        StorageDeviceInfo.StorageDeviceType type = StorageDeviceInfo.StorageDeviceType.UnKnow;
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT || data==null)return type;
        try {
            Uri uri=data.getData();

            //uri持久化
            context.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            String path=GetPathFromUri4kitkat.getPath(context, uri);
            Logger.d("reciver uri path="+path);
            StorageDeviceWrapper wrapper=FileFactory.getInstance().getFileWrapper();
            type = wrapper.getPathStorageType(path);
            StorageDeviceInfo info=wrapper.getStorageDevice(type);
            if(info==null)return type;
            Logger.d("uri type="+type);
            //判断uri是外置sd卡还是usb存储设备
            saveRootFileUri(type, info.getStorageId(), uri, context);
            mDocumentPermissionMaps.put(type, getRootFileByUri(uri));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return type;
    }

    /**根据授权uri去获取对应的DocumentFile*/
    private DocumentFile getRootFileByUri(Uri uri){
        DocumentFile documentFile=null;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)documentFile=DocumentFile.fromTreeUri(FileFactory.getInstance().getContext(), uri);
        else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT)documentFile=DocumentFile.fromSingleUri(FileFactory.getInstance().getContext(), uri);
        return documentFile;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private Uri testProvider(Uri uri, Context context) throws RemoteException {
        ContentProviderClient contentProviderClient=context.getContentResolver().acquireContentProviderClient(uri);
        ContentProvider provider=contentProviderClient.getLocalContentProvider();
        contentProviderClient.canonicalize(uri);
        System.out.println("uri get provider="+provider);
        return uri;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void testContent(Context context, Uri uri){
        try {
            Uri newUri=Uri.parse("content://com.android.externalstorage.documents/document/A023-786C%3ADCIM");
//            newUri=testProvider(newUri, context);
//            context.grantUriPermission("neatc.iotest", newUri, Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            Uri buildUri=DocumentsContract.buildRootsUri("com.android.externalstorage.documents");
            System.out.println("query name2="+GetPathFromUri4kitkat.getDataColumn(context, buildUri, null, null));
            Uri.Builder builder=new Uri.Builder();
            builder.scheme(uri.getScheme());
            builder.authority(uri.getAuthority());
            builder.path(FileFactory.getInstance().getFileWrapper().getStorageDevice(StorageDeviceInfo.StorageDeviceType.ExtrageDevice).getRootPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private void test(Context context, Uri uri){
        //测试创建文件夹
        Uri uri1=DocumentsContract.buildTreeDocumentUri(uri.getAuthority(), DocumentsContract.getTreeDocumentId(uri));
        DocumentFile file = DocumentFile.fromTreeUri(context, uri1);
        Logger.d("uri=%s  uri1=%s", uri.toString(), uri1.toString());
        file.createFile("vnd.android.document/directory", "testDir2");
    }

    //root uri序列化
    private void saveRootFileUri(StorageDeviceInfo.StorageDeviceType type, String storageId, Uri uri, Context context){
        //uri序列化存起来，下次使用
        ObjectOutputStream oos=null;
        try {
            //document_tree_uri_permission_usb_device_data.oos
            String filePath= getPermissionSavePath(type, storageId, context);
            File dataFile=new File(filePath);
            if(!dataFile.getParentFile().exists())dataFile.getParentFile().mkdirs();
            if(dataFile.exists())dataFile.delete();
            dataFile.createNewFile();
            oos=new ObjectOutputStream(new FileOutputStream(dataFile));
            oos.writeObject(new DocumentUriSerializable(uri, true));
            Logger.d("write document permission uri success");
        } catch (IOException e) {
            Logger.e("write document permission uri error e=%s", e.getMessage());
            e.printStackTrace();
        }finally {
            try {
                if(oos!=null){
                    oos.flush();
                    oos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //获取权限存储的地址
    private String getPermissionSavePath(StorageDeviceInfo.StorageDeviceType type, String storageId, Context context){

        String filePath=context.getApplicationContext().getFilesDir().getAbsolutePath()+"/permission";
        switch (type){
            case ExtrageDevice:
                filePath+="/document_tree_uri_extrage_"+storageId+".oos";
                break;
            case SecondExtrageDevice:
                filePath+="/document_tree_uri_second_extrage_"+storageId+".oos";
                break;
            case UsbDevice:
                filePath+="/document_tree_uri_usb_device_"+storageId+".oos";
                break;
            default:
                filePath+="/unknow_"+storageId+".oos";
                break;
        }
        Logger.d("get %s save document permission path=%s;", type, filePath);
        return filePath;
    }

    /**读取uri权限,如果没有对应的权限记录，则返回null*/
    @SuppressLint("NewApi")
    private Uri readRootFileUri(StorageDeviceInfo.StorageDeviceType type, String storageId, Context context){
        Uri uri=null;
        ObjectInputStream ois=null;
        try {
            File saveFile=new File(getPermissionSavePath(type, storageId, context));
            if(!saveFile.exists())return null;
            ois=new ObjectInputStream(new FileInputStream(saveFile));
            DocumentUriSerializable documentUriSerializable= (DocumentUriSerializable) ois.readObject();
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
                uri=DocumentsContract.buildTreeDocumentUri(documentUriSerializable.getAuthority(), documentUriSerializable.getDocumentId());
            }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){
                uri=DocumentsContract.buildDocumentUri(documentUriSerializable.getAuthority(), documentUriSerializable.getDocumentId());
            }
            Logger.d("read document uri permission success.");
        } catch (Exception e) {
            Logger.e("read document uri permission error. exception="+e.getMessage());
            e.printStackTrace();
        }finally {
            try {
                if(ois!=null)ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return uri;
    }

    /**获取root document file,如果没有持久化权限或者没申请权限，则会返回null。需要重新申请权限*/
    private DocumentFile getRootFileUri(StorageDeviceInfo.StorageDeviceType type, String storageId, Context context){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT)return null;
        DocumentFile documentFile=null;
        Uri uri=readRootFileUri(type, storageId, context);
        if(uri!=null)documentFile=DocumentFile.fromTreeUri(context, uri);
        return documentFile;
    }

    /**初始化以获取到的权限信息，当存储设备变化之后，需要更新对应的权限信息
     * @param devices 已挂载的存储设备类型
     * @param context context
     * */
    public void initDocumentPermission(StorageDeviceInfo[] devices, Context context){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT)return;
        for(StorageDeviceInfo device : devices){
            DocumentFile documentFile=getRootFileUri(device.getDeviceType(), device.getStorageId(), context);
            Logger.d("initDocumentPermission type=%s hasPermission=%s", String.valueOf(device.getDeviceType()), String.valueOf(documentFile!=null));
            if(documentFile!=null){
                mDocumentPermissionMaps.put(device.getDeviceType(), documentFile);
            }
        }
    }

    /**获取指定存储设备的根DocumentFile，如果为获取到权限，则会返回null*/
    public DocumentFile getStorageRootDocumentFile(StorageDeviceInfo.StorageDeviceType type){
        return mDocumentPermissionMaps.get(type);
    }

    /**指定的存储设备是否已获取到权限*/
    private boolean hasGrantPermission(StorageDeviceInfo.StorageDeviceType type){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.KITKAT)return true;
        if(type== StorageDeviceInfo.StorageDeviceType.ExtrageDevice)return true;
        return mDocumentPermissionMaps.containsKey(type);
    }

    /**检查操作的路径是否以获取权限*/
    private StorageDeviceInfo.StorageDeviceType getStorageType(String path){
        StorageDeviceInfo.StorageDeviceType type= FileFactory.getInstance().getFileWrapper().getPathStorageType(path);
        return type;
    }

    /**检查所给出的路径是不是都已获取权限*/
    public boolean checkPathPermission(Activity context, String... paths){
        for(String path : paths){
            StorageDeviceInfo.StorageDeviceType fromType=getStorageType(path);
            DocumentTreePermissionUtils utils=DocumentTreePermissionUtils.getInstance();
            if(!utils.hasGrantPermission(fromType)){
                utils.getDocumentTreePermission(context, fromType);
                return false;
            }
        }
        return true;
    }
}
