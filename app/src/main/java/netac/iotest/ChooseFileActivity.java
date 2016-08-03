package netac.iotest;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import netac.iotest.adapter.ChooseFileAdapter;

public class ChooseFileActivity extends AppCompatActivity {

    private TextView path_tv;
    private ListView file_list;
    private ChooseFileAdapter mFileAdapter;
    private List<File> mFiles=new ArrayList<File>();
    private String root_path;
    private File parentFile;
    private Button back_btn;
    private boolean showHidenFile=false;//是否显示隐藏文件

    /**当前显示的根目录*/
    public static final String INTENT_DATA_ROOT_PATH="intent_data_root_path";

    /**当前选择的文件的路径*/
    public static final String INTENT_DATA_CHOOSE_FILE_PATH="intent_data_choose_file_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_file);
        initView();
        initData();
        initEvent();
    }

    private void initView(){
        path_tv= (TextView) findViewById(R.id.path_tv);
        file_list= (ListView) findViewById(R.id.file_list);
        back_btn= (Button) findViewById(R.id.back_btn);
    }

    private void initData(){
        root_path=getIntent().getStringExtra(INTENT_DATA_ROOT_PATH);
        parentFile=new File(root_path);
        path_tv.setText(parentFile.getAbsolutePath());
        arry2List(parentFile.listFiles(), mFiles);
        mFileAdapter=new ChooseFileAdapter(mFiles, this);
        file_list.setAdapter(mFileAdapter);
    }

    private void initEvent(){
        file_list.setOnItemClickListener(mItemClickListener);
        back_btn.setOnClickListener(mClickListener);
    }

    private void arry2List(File[] fs, List<File> files){
        if(files==null)files=new ArrayList<File>();
        files.clear();
        if(fs!=null)for(File file : fs){
            if(file.getName().startsWith(".") && !showHidenFile){
                continue;
            }
            files.add(file);
        }
    }

    //访问下个目录
    private void nextFloder(File file){
        parentFile=file;
        path_tv.setText(parentFile.getAbsolutePath());
        arry2List(parentFile.listFiles(), mFiles);
        mFileAdapter.notifyDataSetChanged();
    }

    //访问上个目录
    private void upFloder(){
        if(parentFile.getAbsolutePath().equals(root_path)){
            Toast.makeText(this, "Is currently the root directory", Toast.LENGTH_SHORT).show();
        }else{
            parentFile=parentFile.getParentFile();
            path_tv.setText(parentFile.getAbsolutePath());
            arry2List(parentFile.listFiles(), mFiles);
            mFileAdapter.notifyDataSetChanged();
        }
    }

    View.OnClickListener mClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            upFloder();
        }
    };

    AdapterView.OnItemClickListener mItemClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            File openFile = (File) mFileAdapter.getItem(position);
            if(openFile.isDirectory())nextFloder(openFile);
            else showChooseDialog(openFile);
        }
    };

    private void showChooseDialog(final File file){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Prompt").setMessage(String.format("Select %s to test?", file.getName())).setPositiveButton("OK", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent=new Intent();
                intent.putExtra(INTENT_DATA_CHOOSE_FILE_PATH, file.getAbsolutePath());
                setResult(100, intent);
                ChooseFileActivity.this.finish();
            }
        }).setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        }).create();
        builder.show();
    }

}
