package netac.fileutilsmaster.utils;

import android.util.Log;

/**
 * Created by siwei.zhao on 2016/10/11.
 */
public class Logger {

    private static boolean isLogEnable = true;//是否输出log信息

    private static boolean isLineTagEnable=true;//是否显示调用输出log信息的类名，方法名和行数

    public static String tag = "FileMaster";

    public static void debug(boolean isEnable) {
        debug(tag, isEnable, false);
    }

    public static void debug(boolean isDebugEnable, boolean isShowLineEnable) {
        debug(tag, isDebugEnable, isShowLineEnable);
    }

    public static void debug(String logTag, boolean isDebugEnable, boolean isShowLine) {
        tag = logTag;
        isLogEnable = isDebugEnable;
        isLineTagEnable=isShowLine;
    }

    public static void v(String msg, Object... args) {
        String lineStr="";
        if(isLineTagEnable){
            String className=Thread.currentThread().getStackTrace()[2].getClassName();
            int lineNum=Thread.currentThread().getStackTrace()[2].getLineNumber();
            lineStr=String.format("%s-%s ", className, String.valueOf(lineNum));
        }
        if (isLogEnable) Log.v(tag, String.format(lineStr+msg, args));
    }

    public static void d(String msg, Object... args) {
        String lineStr="";
        if(isLineTagEnable){
            String className=Thread.currentThread().getStackTrace()[2].getClassName();
            int lineNum=Thread.currentThread().getStackTrace()[2].getLineNumber();
            lineStr=String.format("%s-%s ", className, String.valueOf(lineNum));
        }
        if (isLogEnable) Log.i(tag, String.format(lineStr+msg, args));
    }

    public static void i(String msg, Object... args) {
        String lineStr="";
        if(isLineTagEnable){
            String className=Thread.currentThread().getStackTrace()[2].getClassName();
            int lineNum=Thread.currentThread().getStackTrace()[2].getLineNumber();
            lineStr=String.format("%s-%s ", className, String.valueOf(lineNum));
        }
        if (isLogEnable) Log.i(tag, String.format(lineStr+msg, args));
    }

    public static void w(String msg, Object... args) {
        String lineStr="";
        if(isLineTagEnable){
            String className=Thread.currentThread().getStackTrace()[2].getClassName();
            int lineNum=Thread.currentThread().getStackTrace()[2].getLineNumber();
            lineStr=String.format("%s-%s ", className, String.valueOf(lineNum));
        }
        if (isLogEnable) Log.w(tag, String.format(lineStr+msg, args));
    }


    public static void e(String msg, Object... args) {
        String lineStr="";
        if(isLineTagEnable){
            String className=Thread.currentThread().getStackTrace()[2].getClassName();
            int lineNum=Thread.currentThread().getStackTrace()[2].getLineNumber();
            lineStr=String.format("%s-%s ", className, String.valueOf(lineNum));
        }
        if (isLogEnable) Log.e(tag, String.format(lineStr+msg, args));
    }


    public static void e(Throwable t) {
        if (isLogEnable) t.printStackTrace();
    }
}
