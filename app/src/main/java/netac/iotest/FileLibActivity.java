package netac.iotest;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.List;

import netac.fileutilsmaster.file.FileCommon;
import netac.fileutilsmaster.file.FileFactory;
import netac.fileutilsmaster.file.vo.StorageDeviceInfo;
import netac.fileutilsmaster.file.wrapper.StorageDeviceWrapper;
import netac.fileutilsmaster.utils.DocumentTreePermissionUtils;

public class FileLibActivity extends AppCompatActivity {

    private Button get_permission_btn;

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
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void initData(){
        FileFactory.getInstance().getFileWrapper().registerStorageDeviceChanged(mStorageDeviceChangeListener);
        Uri newUri=Uri.parse("content://com.android.externalstorage.documents/document/A023-786C/DCIM");
        try {
            System.out.println("grant permission");
            ContentProviderClient providerClient=getContentResolver().acquireContentProviderClient("com.android.externalstorage.documents");
            System.out.println("providerClient="+providerClient);
            ContentProvider provider=providerClient.getLocalContentProvider();
            System.out.println("ContentProvider="+provider);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("query name="+ GetPathFromUri4kitkat.getDataColumn(this, newUri, null, null));
    }

    private void initEvent(){
        get_permission_btn.setOnClickListener(mClickListener);
    }

    View.OnClickListener mClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.get_permission_btn:
                    getPermission();
                    break;
            }
        }
    };

    private void getPermission(){
        DocumentTreePermissionUtils.getInstance().getDocumentTreePermission(FileLibActivity.this, 1346);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1346 && data!=null){
            System.out.println("get permission url="+data.getData());
            StorageDeviceInfo.StorageDeviceType type=DocumentTreePermissionUtils.getInstance().onPermissionReciver(data, this);
            FileCommon fileCommon=FileFactory.getInstance().createFile("/storage/sdcard1/test/test.txt");
            System.out.println("create file="+fileCommon.createNewFile());
        }
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
