package netac.iotest;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.storage.StorageManager;
import android.provider.DocumentsProvider;
import android.support.design.widget.Snackbar;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import netac.fileutilsmaster.file.FileFactory;
import netac.fileutilsmaster.file.vo.StorageDeviceInfo;
import netac.fileutilsmaster.file.wrapper.StorageDeviceWrapper;
import netac.fileutilsmaster.filebroadcast.BroadCastReciverManager;
import netac.fileutilsmaster.filebroadcast.DeviceBroadcast;
import netac.fileutilsmaster.utils.GetPathFromUri4kitkat;
import netac.iotest.test.IOTestUtils;

public class MainActivity extends AppCompatActivity {

    private List<File> testFiles = new ArrayList<File>();
    private List<String> diskTypes = new ArrayList<String>();
    private Button test_btn, test_cancle_btn, choose_test_file_btn, test_write_btn, create_file_btn, delete_file_btn;
    private IOTestUtils.ReadTestContral mContral;
    private TextView msg_tv, choose_path_tv;
    private Spinner disktype_sp, read_length_sp;
    private EditText name_et;

    private String choose_disk_type_path, choose_path;
    private List<Integer> readLengths = Arrays.asList(4, 8, 16);
    private int readLength;
    private DocumentFile rootFile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startActivity(new Intent(MainActivity.this, FileLibActivity.class));
        finish();
        initView();
        initData();
        initEvent();
    }

    private void initView() {
        test_btn = (Button) findViewById(R.id.test_btn);
        msg_tv = (TextView) findViewById(R.id.msg_tv);
        test_cancle_btn = (Button) findViewById(R.id.test_cancle_btn);
        disktype_sp = (Spinner) findViewById(R.id.disktype_sp);
        choose_test_file_btn = (Button) findViewById(R.id.choose_test_file_btn);
        choose_path_tv = (TextView) findViewById(R.id.choose_path_tv);
        read_length_sp = (Spinner) findViewById(R.id.read_length_sp);
        test_write_btn = (Button) findViewById(R.id.test_write_btn);
        name_et = (EditText) findViewById(R.id.name_et);
        create_file_btn = (Button) findViewById(R.id.create_file_btn);
        delete_file_btn = (Button) findViewById(R.id.delete_file_btn);
    }

    private void initData() {
        addTestFiles();
        for (File file : testFiles) diskTypes.add(file.getName());
        ArrayAdapter<String> diskTypeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, diskTypes);
        disktype_sp.setAdapter(diskTypeAdapter);

        ArrayAdapter<Integer> readLengthAdapter = new ArrayAdapter<Integer>(this, android.R.layout.simple_expandable_list_item_1, readLengths);
        read_length_sp.setAdapter(readLengthAdapter);


        BroadCastReciverManager.getManager().getBroadCastReciver(DeviceBroadcast.class).registerBroadCastListener(mCallBack);

        BroadCastReciverManager.BroadCastUtils.getUtils().sendLocalBroadCast(DeviceBroadcast.ACTION_TEST);

        FileFactory.getInstance().getFileWrapper().registerStorageDeviceChanged(mChangeListener);

        //mimetype
        //System.out.println("getMimeTypeFromExtension="+ MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl("aaa.mp3")));

        mountSdCard();
    }

    private void initEvent() {
        test_btn.setOnClickListener(mClickListener);
        choose_test_file_btn.setOnClickListener(mClickListener);
        test_cancle_btn.setOnClickListener(mClickListener);
        disktype_sp.setOnItemSelectedListener(mDiskTypeItemClickListener);
        read_length_sp.setOnItemSelectedListener(mReadLengthItemClickListener);
        test_write_btn.setOnClickListener(mClickListener);
        create_file_btn.setOnClickListener(mClickListener);
        delete_file_btn.setOnClickListener(mClickListener);
    }

    StorageDeviceWrapper.StorageDeviceChangeListener mChangeListener=new StorageDeviceWrapper.StorageDeviceChangeListener() {
        @Override
        public void onDeviceMountedStatusChanged(boolean isMounted, StorageDeviceInfo.StorageDeviceType storageDeviceType) {
            System.out.println("isMounted="+isMounted+"; storageDeviceType="+storageDeviceType);
        }

        @Override
        public void onStorageDeviceChanged(List<StorageDeviceInfo> deviceInfos) {
            msg_tv.append("=====StorageDeviceWrapper.StorageDeviceChangeListener size="+deviceInfos.size());
            for(int i=0; i<deviceInfos.size(); i++){
                msg_tv.append("\n"+(i+1)+"、Path="+deviceInfos.get(i).getRootPath());
            }
        }
    };

    BroadCastReciverManager.BroadCastCallBack mCallBack = new BroadCastReciverManager.BroadCastCallBack() {
        @Override
        public void onReceiver(Context context, Intent intent) {
            String action = intent.getAction();
            msg_tv.append("\nreciver action=" + action + "; data=" + intent.getDataString() + "\n");
            if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                //USB设备移除
                msg_tv.append("\nUsb 设备移除" + intent.getDataString() + "\n");
            } else if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                //USB设备挂载
                msg_tv.append("\nUsb 设备挂载 " + intent.getDataString() + "\n");
            } else if (action.equals(Intent.ACTION_MEDIA_REMOVED)) {
                msg_tv.append("\nUsb ACTION_MEDIA_REMOVED" + "\n");
            } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {

            } else if(action.equals(Intent.ACTION_MEDIA_CHECKING)){
                msg_tv.append("\nACTION_MEDIA_CHECKING  "+intent.getData());

            }
        }
    };

    AdapterView.OnItemSelectedListener mDiskTypeItemClickListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            choose_disk_type_path = testFiles.get(position).getAbsolutePath();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    AdapterView.OnItemSelectedListener mReadLengthItemClickListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            readLength = readLengths.get(position) * 1024;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    View.OnClickListener mClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.test_btn://开始测试
                    if (!checkData()) break;
                    msg_tv.setText("");
                    test_btn.setEnabled(false);
                    test_cancle_btn.setEnabled(true);
                    mContral = IOTestUtils.getIOTest().readSpeed(choose_path, readLength, mTestCallBack);
                    break;
                case R.id.choose_test_file_btn://选择读取的测试文件
                    Intent intent = new Intent(MainActivity.this, ChooseFileActivity.class);
                    intent.putExtra(ChooseFileActivity.INTENT_DATA_ROOT_PATH, choose_disk_type_path);
                    startActivityForResult(intent, 100);
                    break;
                case R.id.test_cancle_btn://取消测试
                    if (mContral != null) mContral.stopRun();
                    test_cancle_btn.setEnabled(false);
                    test_btn.setEnabled(true);
                    break;
                case R.id.test_write_btn:
//                    getPermissions();
                    //otgFileWriteTest("/storage/usbotg/otg_test_file.txt");
//                    openDocumentProvider();
                    openDoucmentProviderTree();
//                    createDoucmentProvider();
                    break;
                case R.id.create_file_btn:
                    createOTGFile(name_et.getText().toString());
                    break;
                case R.id.delete_file_btn:
                    deleteOTGFile(name_et.getText().toString());
                    break;
            }
        }
    };

    /**
     * 检查界面数据是否符合要求
     */
    private boolean checkData() {
        if (TextUtils.isEmpty(choose_path_tv.getText())) {
            Snackbar.make(msg_tv, "Please select the file you need to test", Snackbar.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    IOTestUtils.IOTestCallBack mTestCallBack = new IOTestUtils.IOTestCallBack() {
        @Override
        public void onStartTest(IOTestUtils.ReadTestContral contral, IOTestUtils.TestType type) {
            msg_tv.append(String.format("Start reading test, test file: %s;\n", contral.getSpeedInfo().getTestPath()));
        }

        @Override
        public void onEndTest(IOTestUtils.ReadTestContral contral, IOTestUtils.TestType type, boolean isStop) {
            if (!isStop) {
                msg_tv.append(String.format("Read test completed,Test speed is %s/S; \n Test file size %s; \nRead each time the size of %sKB;",
                        Formatter.formatFileSize(MainActivity.this, (long) contral.getSpeed()),
                        Formatter.formatFileSize(MainActivity.this, contral.getSpeedInfo().getTestFileSize()),
                        readLength / 1024));
            } else msg_tv.append("Cancel test success.");
            test_cancle_btn.setEnabled(false);
            test_btn.setEnabled(true);
        }

        @Override
        public void onErrorTest(IOTestUtils.ReadTestContral contral, IOTestUtils.TestType type, String error) {
            msg_tv.append(String.format("Test failed, exception:%s;\n", error));
            test_cancle_btn.setEnabled(false);
            test_btn.setEnabled(true);
        }
    };

    /**
     * 添加测试文件数据
     */
    private void addTestFiles() {
        testFiles.clear();
        Map<String, String> maps = System.getenv();
        String storage = maps.get("ANDROID_STORAGE");
        File storageFile = new File(storage);
        File[] fs = storageFile.listFiles();
        for (File f : fs) {
            testFiles.add(f);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == resultCode && requestCode == 100) {
            choose_path = data.getStringExtra(ChooseFileActivity.INTENT_DATA_CHOOSE_FILE_PATH);
            choose_path_tv.setText(String.format("Test file:%s", choose_path));
        } else if (requestCode == 103) {
            //获取文件操作权限返回
            if (data == null) return;
            Uri uri = data.getData();
            System.out.println("result=" + uri.toString());
            msg_tv.append("result=" + uri.toString());
            //API 19以上 Android4.4
            //grant write permissions
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            //使用DocumentFile可以操作所有的文件，包括内置，外置存储，OTG
            //参考：https://developer.android.com/reference/android/support/v4/provider/DocumentFile.html
            rootFile = DocumentFile.fromTreeUri(this, uri);
            msg_tv.append("onActivityResult="+rootFile.getType()+"======"+rootFile.getUri()+"======"+rootFile.getUri().getPath()+"\n");
            System.out.println("onActivityResult="+rootFile.getType()+"======"+rootFile.getUri()+"======"+rootFile.getUri().getPath()+" aut="+uri.getAuthority()+" path="+ GetPathFromUri4kitkat.getPath(MainActivity.this, uri)+"\n");
            //DocumentFile dFile = rootFile.findFile("DCIM").findFile("Camera");
            //storage/0123-4567/DCIM/
            //System.out.println(dFile.getType()+"======"+dFile.getUri()+"======"+dFile.getUri().getPath()+"====="+dFile.createFile("*/*", "1.txt"));

            File file=new File("/storage/0123-4567/DCIM/");

            DocumentFile documentFile=DocumentFile.fromSingleUri(this, Uri.fromFile(file));
            System.out.println("file path="+file.getPath()+"====exits="+file.exists()+"=====uri="+documentFile.getUri().getPath());
            //documentFile1.createFile("vnd.android.document/directory", "testDir3");

        }
    }

    private void getPermissions() {
        try {
            String perPath = "/system/etc/permissions/platform.xml";
            File perFile = new File(perPath);
            FileInputStream in = new FileInputStream(perFile);
            byte[] buff = new byte[in.available()];
            in.read(buff);
            String perStr = new String(buff, "UTF-8");
            System.out.println("Permissions is " + perStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openDocumentProvider() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, 101);
        DocumentsProvider provider;
    }

    private void createDoucmentProvider() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, 102);
    }

    private void openDoucmentProviderTree() {
        //获取一个目录的操作权限,访问Android的存储框架，SAF。
        //参考：https://developer.android.com/guide/topics/providers/document-provider.html
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, 103);
        //返回执行的uri，result=content://com.android.externalstorage.documents/tree/9016-4EF8%3A%E6%AD%8C%E6%9B%B2
    }

    //OTG创建文件
    private void createOTGFile(String fileName) {
        String rootPath = choose_disk_type_path;
        final File testFile = new File(rootPath + fileName);
        if (testFile.exists()) testFile.delete();
        final DocumentFile file = rootFile.createFile("*/*", fileName);
        msg_tv.setText("create new File " + file + "\n");
        if (file != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //OTG文件写入
                        OutputStream out = getContentResolver().openOutputStream(file.getUri(), "rw");
                        out.write("write test data to otg, use fileoutputstream.".getBytes());
                        out.flush();
                        out.close();
                        mHandler.sendMessage(mHandler.obtainMessage(1, "write data success!"));
                    } catch (Exception e) {
                        System.out.println("write data faild e=" + e.getMessage() + "\n");
                        mHandler.sendMessage(mHandler.obtainMessage(1, "write data faild e=" + e.getMessage() + "\n"));
                        e.printStackTrace();
                    }
                }
            }).start();


        }
    }

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    msg_tv.append(msg.obj.toString() + "\n\n");
                    break;
                case 2:
                    msg_tv.setText(msg.obj.toString());
                    break;
            }
        }
    };

    //OTG删除文件
    private void deleteOTGFile(String fileName) {
        DocumentFile delete = rootFile.findFile(fileName);
        msg_tv.setText("delete file " + delete.delete());
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            msg_tv.append("\nreciver 2 action=" + action);
        }
    };


    //mount sd卡，判断sd卡的拔插状态
    private void mountSdCard() {
        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        System.out.println("getExternalStorageDirectory="+Environment.getExternalStorageDirectory().getAbsolutePath());
//        System.out.println("getStorageState="+Environment.getStorageState(new File(System.getenv("EXTERNAL_STORAGE") + "/DCIM")));
        System.out.println("EXTERNAL_STORAGE=" + System.getenv("EXTERNAL_STORAGE"));
        Map<String, String> maps = System.getenv();
        Iterator iterator = maps.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            System.out.println("key="+entry.getKey()+";  value="+entry.getValue());
        }

        String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
        String s="";
        try {
            final Process process = new ProcessBuilder().directory(new File("/")).command("df").redirectErrorStream(true).start();
            process.waitFor();
            final InputStream is = process.getInputStream();
            final byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                s = s + new String(buffer);
            }
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // parse output
        final String[] lines = s.split("\n");
        for (String line : lines) {
            System.out.println("line ="+line);
            if (!line.toLowerCase().contains("asec") && line.toLowerCase().contains("/storage/")) {
                if (line.matches(reg)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/")){
                            if (!part.toLowerCase().contains("vold")){
                                //System.out.println("path="+part);
                            }
                        }

                    }
                }
            }
        }
        //System.out.println("process result="+s);
    }



    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }


    private void getFilePath(){
        StorageManager storageManager= (StorageManager) getSystemService(Context.STORAGE_SERVICE);
    }
}
