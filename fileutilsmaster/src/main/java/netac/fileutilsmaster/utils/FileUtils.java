package netac.fileutilsmaster.utils;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by siwei.zhao on 2016/11/15.
 */

public class FileUtils {

    /**当前正在显示的Activity的Context，用于显示弹窗使用*/
    public static Context currentActivityContext;

    /**拆分文件名并获取文件的文件名中的文件名称（不包含目录）
     * @param fileName 文件名
     * */
    public static String spliteFileName(String fileName){
        if(fileName.indexOf(".")<0){
            return "";
        }else{
            return fileName.substring(0, fileName.lastIndexOf(".")-1);
        }

    }

    /**拆分文件名并获取文件的文件格式（不包含目录）
     * @param fileName 文件名
     * */
    public static String spliteFileFormat(String fileName){
        if(fileName.indexOf(".")<0){
            return "";
        }else{
            return fileName.substring(fileName.lastIndexOf("."), fileName.length());
        }

    }

    /**获取一个新的文件名，在文件的名称后追加(Num)，以保证生成的文件是不存在的*/
    public static String getNewFileName(String dir, String fileName){
        int nameTag=1;
        String name= FileUtils.spliteFileName(fileName),format=FileUtils.spliteFileFormat(fileName);
        File file=new File(dir, fileName);
        while(file.exists()){
            file=new File(dir, String.format("%s(%s)%s", name, String.valueOf(nameTag), format));
            nameTag++;
        }
        return file.getName();
    }

    /**检查文件名规范
     * @param  isFile 检查的文件名规范是按照文件去检查还是按照目录的规范去检查
     * @param fileName 文件名
     * @return boolean 名称是否符合规范
     * */
    public static boolean checkFileNameStandard(boolean isFile, String fileName){
        if(fileName==null || TextUtils.isEmpty(fileName))return false;
        boolean fileCheck=true;
        //检查文件名的格式是否存在
        fileCheck=(isFile && fileName.indexOf(".")>0);
        // 正则匹配是否含有\/:*?"<>|
        fileCheck=(fileCheck && !fileName.matches("^(.*)[\\u005C/:\\u002A\\u003F\"<>\'\\u007C’‘“”：？](.*)$"));
        return fileCheck;
    }

    /**低于4.4，不包含4.4*/
    public static boolean BlowKitKat(){
        return Build.VERSION.SDK_INT<19;
    }

    /**4.4以上，包含4.4*/
    public static boolean AboveKitKat(){
        return Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT;
    }

}
