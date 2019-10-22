package com.lhq.loader.bean;

import java.io.Serializable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 *
 * @author lhq
 *
 */
public class DownloadTask implements Serializable {
    private static final long serialVersionUID = 1L;

    private volatile long count = 0L; // 总数
    private volatile int state = 0;// 0:启动 1:暂停 2:停止
    private double percent = 0;// 进度百分比，页面查询时才进行计算
    private AtomicLong current = new AtomicLong(0L);// 当前状态
    private Semaphore semaphore = new Semaphore(1);// 信号量，没有信号就是暂停状态
    public long getCount() {
        return count;
    }
    public void setCount(long count) {
        this.count = count;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }
    public AtomicLong getCurrent() {
        return current;
    }
    public void setCurrent(AtomicLong current) {
        this.current = current;
    }
    public Semaphore getSemaphore() {
        return semaphore;
    }
    public void setSemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
    }



}
