package netac.iotest.test;

/**
 * Created by siwei.zhao on 2016/6/16.
 * IO速度信息
 */
public class IOSpeedInfo {

    private String testPath;//测试文件的路径

    private long startTime;//开始时间

    private long endTime;//执行完成的时间

    private long testFileSize;//测试文件大小

    private float speed;//读取速度

    private int testLength;//一次读取或者写入的长度

    public int getTestLength() {
        return testLength;
    }

    public void setTestLength(int testLength) {
        this.testLength = testLength;
    }

    public String getTestPath() {
        return testPath;
    }

    public void setTestPath(String testPath) {
        this.testPath = testPath;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public long getTestFileSize() {
        return testFileSize;
    }

    public void setTestFileSize(long testFileSize) {
        this.testFileSize = testFileSize;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
