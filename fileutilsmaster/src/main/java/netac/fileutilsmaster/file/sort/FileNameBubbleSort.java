package netac.fileutilsmaster.file.sort;

import android.support.annotation.Nullable;

import netac.fileutilsmaster.file.FileCommon;

/**
 * Created by siwei.zhao on 2016/12/24.
 * 默认的排序规则，按照文件名去进行排序,汉字-数字-字母
 */

public class FileNameBubbleSort extends FileCommon.BubbleSort {
    @Override
    protected boolean changeSort(@Nullable  FileCommon common1, @Nullable FileCommon common2) {
        if(common1==null || common2==null)return false;
        byte[] name1=common1.getName().getBytes();
        byte[] name2=common2.getName().getBytes();
        int min=Math.min(name1.length, name2.length);
        for(int i=0; i< min; i++){
            if(name1[i]==name2[i])continue;
            return name1[i]>name2[i];
        }
        return false;
    }
}
