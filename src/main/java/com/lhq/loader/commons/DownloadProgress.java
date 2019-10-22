package com.lhq.loader.commons;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.lhq.loader.bean.DownloadTask;
import com.lhq.loader.bean.SysConfig;
import com.lhq.loader.exception.BaseException;

/**
 * @author lhq
 * 下载进度管理器
 * key: 任务ID
 * value: 下载总数，已下载数，下载状态
 */
@Component
public class DownloadProgress extends ConcurrentHashMap<String, DownloadTask> {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(DownloadProgress.class);

    @Autowired
    private SysConfig sysConfig;

    /**
     * 是否允许开启一个新的下载任务
     * 
     * @param id
     * @return
     */
    public synchronized boolean canStartNewTask(String id) {
        boolean flag = false;
        DownloadTask task = this.get(id);
        // 当前任务不存在
        if (task == null) {
            // 当前进行中任务数量小于maxTask，允许操作
            if (this.inStartingNum() < sysConfig.getMaxTask()) {
                task = new DownloadTask();
                this.put(id, task);
                flag = true;
            }
        } else {
            // 当前任务已经存在
            throw new BaseException("当前任务已经存在");
        }
        return flag;
    }


    /**
     * 设置当前任务的瓦片总数
     * 
     * @param id
     * @param count
     */
    public void setTaskCount(String id, Long count) {
        DownloadTask task = this.get(id);
        if (task == null) {
            throw new BaseException("下载任务" + id + "不存在");
        }
        task.setCount(count);
    }

    /**
     * 累加当前任务已下载瓦片数量
     * 
     * @param id
     * @param current
     */
    public void taskIncrement(String id) {
        DownloadTask task = this.get(id);
        if (task == null) {
            throw new BaseException("下载任务" + id + "不存在");
        }
        task.getCurrent().incrementAndGet();
        // 瓦片下载结束后，将状态标记为stop
        if (task.getCurrent().get() == task.getCount()) {
            task.setStop(true);
        }
    }

    /**
     * 启动下载进程
     * 
     * @param id
     */
    public boolean start(String id) {
        DownloadTask task = this.get(id);
        if (task == null) {
            throw new BaseException("下载任务" + id + "不存在");
        }
        if (task.isStop()) {
            throw new BaseException("该任务已经停止，不能启动");
        }
        if (task.getCurrent().get() == task.getCount()) {
            task.setStop(true);
            throw new BaseException("该任务已经下载结束，不能启动");
        }
        synchronized (this) {
            if (this.inStartingNum() >= sysConfig.getMaxTask()) {
                throw new BaseException("下载任务最大支持" + sysConfig.getMaxTask() + "个，请等待其他任务下载结束或暂停其他任务");
            }
            task.getSemaphore().release(1);
        }
        return true;
    }

    /**
     * 暂停下载进程
     * 
     * @param id
     */
    public boolean pause(String id) {
        DownloadTask task = this.get(id);
        if (task == null) {
            throw new BaseException("下载任务" + id + "不存在");
        }
        if (task.isStop()) {
            throw new BaseException("该任务已经停止，不能启动");
        }
        if (task.getCurrent().get() == task.getCount()) {
            task.setStop(true);
            throw new BaseException("该任务已经下载结束，不能暂停");
        }
        synchronized (this) {
            try {
                task.getSemaphore().acquire(1);
            } catch (InterruptedException e) {
                logger.error(e.getLocalizedMessage(), e);
                Thread.currentThread().interrupt();
            }
        }
        return true;
    }

    /**
     * 停止下载进程
     * 
     * @param id
     */
    public boolean stop(String id) {
        DownloadTask task = this.get(id);
        if (task == null) {
            throw new BaseException("下载任务" + id + "不存在");
        }
        task.setStop(true);
        return true;
    }

    /**
     * 正在下载的任务数量
     * 
     * @return
     */
    private int inStartingNum() {
        int num = 0;
        for (Entry<String, DownloadTask> entry : this.entrySet()) {
            DownloadTask task = entry.getValue();
            if (!task.isStop() && task.getSemaphore().availablePermits() > 0) {
                num++;
            }
        }
        return num;
    }
}
