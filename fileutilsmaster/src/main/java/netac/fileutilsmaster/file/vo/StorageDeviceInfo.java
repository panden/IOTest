package netac.fileutilsmaster.file.vo;

/**
 * Created by siwei.zhao on 2016/9/14.
 * 存储设备的信息
 */
public class StorageDeviceInfo {

    private String rootPath;//根目录路径
    private long capacity;//总容量
    private long freeSpace;//可用空间
    private long usedSpace;//已用空间
    private StorageDeviceType deviceType;//设备类型

    /**存储设备类型*/
    public enum StorageDeviceType {

        UnKnow,

        /**手机自带存储*/
        ExtrageDevice,

        /**手机外置存储*/
        SecondExtrageDevice,

        /**usb存储设备*/
        UsbDevice;

    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public long getCapacity() {
        return capacity;
    }

    public void setCapacity(long capacity) {
        this.capacity = capacity;
    }

    public long getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(long freeSpace) {
        this.freeSpace = freeSpace;
    }

    public long getUsedSpace() {
        return usedSpace;
    }

    public void setUsedSpace(long usedSpace) {
        this.usedSpace = usedSpace;
    }

    public StorageDeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(StorageDeviceType deviceType) {
        this.deviceType = deviceType;
    }


}
