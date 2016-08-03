package netac.iotest.adapter;

import android.content.Context;
import android.text.format.Formatter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import netac.iotest.R;

/**
 * Created by siwei.zhao on 2016/6/16.
 */
public class ChooseFileAdapter extends AdapterCommon<ChooseFileAdapter.MyViewHolder>{

    private List<File> mFiles;
    private Context mContext;

    public ChooseFileAdapter(List<File> files, Context ctx){
        this.mFiles=files;
        this.mContext=ctx;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        MyViewHolder holder=new MyViewHolder(View.inflate(mContext, R.layout.choose_file_item, null));
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        File file=mFiles.get(position);
        holder.file_img.setImageResource(file.isFile()?R.drawable.ic_insert_drive_file_black_48dp:R.drawable.ic_folder_black_48dp);
        holder.file_name_tv.setText(file.getName());
        holder.file_length_tv.setVisibility(file.isFile()?View.VISIBLE:View.GONE);
        if(file.isFile())holder.file_length_tv.setText(Formatter.formatFileSize(mContext, file.length()));
    }

    @Override
    public int getCount() {
        return mFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return mFiles.get(position);
    }

    protected class MyViewHolder extends AdapterCommon.ViewHolder{

        private ImageView file_img;

        private TextView file_name_tv, file_length_tv;

        public MyViewHolder(View convertView) {
            super(convertView);
            file_img= (ImageView) convertView.findViewById(R.id.file_img);
            file_name_tv= (TextView) convertView.findViewById(R.id.file_name_tv);
            file_length_tv= (TextView) convertView.findViewById(R.id.file_length_tv);
        }
    }
}
