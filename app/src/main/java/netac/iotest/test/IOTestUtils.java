package netac.iotest.test;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by siwei.zhao on 2016/6/15.
 * IO读写数度测试
 */
public class IOTestUtils {

    private static IOTestUtils sUtils;

    private IOTestUtils(){};

    public static IOTestUtils getIOTest(){
        if(sUtils==null)sUtils=new IOTestUtils();
        return sUtils;
    }

    /**测试读取文件速度
     * @param  readFile 读取的测试文件的目录
     * @param  readLength 一次读取文件长度
     * */
    public ReadTestContral readSpeed(String readFile, int readLength, IOTestCallBack callBack){
        ReadThread read=new ReadThread(readFile, readLength, callBack);
        read.start();
        return read.mContral;
    }

    public interface IOTestCallBack{

        /**开始测试
         * @param  contral 测试速度控制器
         * @param  type 测试类型（读取、写入）
         * */
        void onStartTest(ReadTestContral contral, TestType type);

        /**测试结束
         * @param  contral 测试速度控制器
         * @param  type 测试类型（读取、写入）
         * @param  isStop 当前是否问停止测试（不为停止测试，就表示正常结束测试）
         * */
        void onEndTest(ReadTestContral contral, TestType type, boolean isStop);

        /**测试异常
         * @param  contral 测试速度控制器
         * @param  type 测试类型（读取、写入）
         * @param  error 错误信息
         * */
        void onErrorTest(ReadTestContral contral, TestType type, String error);
    }

    public enum TestType{

        IORead, IOWrite;

    }


    private class ReadThread extends Thread{

        private String mTestPath;
        private int mReadLength;
        private IOTestCallBack mCallBack;
        private ReadTestContral mContral;
        private File mTestFile;
        private long speedTime;
        private MainHandler mHandler;

        public ReadThread(String testPath, int readLegth, IOTestCallBack callBack){
            this.mCallBack=callBack;
            this.mReadLength=readLegth;
            this.mTestPath=testPath;
            mTestFile=new File(testPath);
            mContral=new ReadTestContral();
            mContral.hasRun=true;
            mContral.type=TestType.IORead;
            mContral.speedInfo=new IOSpeedInfo();
            mContral.speedInfo.setTestPath(testPath);
            mContral.speedInfo.setTestFileSize(mTestFile.length());
            mHandler=new MainHandler(callBack);
        }

        @Override
        public void run() {
            super.run();
            int length;
            FileInputStream in=null;
            try {
                in=new FileInputStream(mTestFile);
                byte[] bs=new byte[mReadLength];
                mContral.speedInfo.setStartTime(System.currentTimeMillis());
                mHandler.sendMessage(mHandler.obtainMessage(1, mContral));
                while((length=in.read(bs))>0 && mContral.hasRun){}
                mContral.speedInfo.setEndTime(System.currentTimeMillis());
                mContral.speedInfo.setSpeed((float) (mContral.speedInfo.getTestFileSize()*1.0/((mContral.speedInfo.getEndTime()-mContral.speedInfo.getStartTime())/1000)));
                mHandler.sendMessage(mHandler.obtainMessage(2, mContral));
            } catch (Exception e) {
                mContral.error=e;
                mHandler.sendMessage(mHandler.obtainMessage(3, mContral));
                e.printStackTrace();
            } finally {
                if(in!=null){
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class MainHandler extends Handler{

        private IOTestCallBack mCallBack;

        public MainHandler(IOTestCallBack callBack){
            super(Looper.getMainLooper());
            mCallBack=callBack;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ReadTestContral contral= (ReadTestContral) msg.obj;
            switch (msg.what){
                case 1://onStartTest
                    mCallBack.onStartTest(contral, contral.type);
                    break;
                case 2://onEndTest
                    mCallBack.onEndTest(contral, contral.type, !contral.hasRun);
                    break;
                case 3://onErrorTest
                    mCallBack.onErrorTest(contral, contral.type, contral.error.getMessage());
                    break;
            }
        }
    }

    /**
     * Created by siwei.zhao on 2016/6/16.
     * 测试读取速度控制器
     */
    public class ReadTestContral {

        private boolean hasRun;//当前测试是否在执行

        private TestType type;

        private IOSpeedInfo speedInfo=new IOSpeedInfo();//读写信息测试

        private Exception error;

        public boolean isHasRun() {
            return hasRun;
        }

        public IOSpeedInfo getSpeedInfo() {
            return speedInfo;
        }

        public void stopRun() {
            this.hasRun = false;
        }

        /**获取当前写入速度byte/s*/
        public float getSpeed(){
            return speedInfo.getSpeed();
        }

        public Exception getError() {
            return error;
        }

        public TestType getType() {
            return type;
        }
    }


}
