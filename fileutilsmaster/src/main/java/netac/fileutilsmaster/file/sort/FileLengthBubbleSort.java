package netac.fileutilsmaster.file.sort;

import android.support.annotation.Nullable;

import netac.fileutilsmaster.file.FileCommon;

/**
 * Created by siwei.zhao on 2016/12/24.
 * 默认采用文件由大到小的方式去进行排序
 */

public class FileLengthBubbleSort extends FileCommon.BubbleSort{
    @Override
    protected boolean changeSort(@Nullable FileCommon common1, @Nullable FileCommon common2) {
        if(common1==null || common2==null)return false;
        if(common1.isDirectory() || common2.isDirectory())return false;
        return common1.length()<common2.length();
    }
}
