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

    private volatile long count = 0L;
    private volatile boolean isStop = false;
    private AtomicLong current = new AtomicLong(0L);
    private Semaphore semaphore = new Semaphore(1);

    public long getCount() {
        return count;
    }
    public void setCount(long count) {
        this.count = count;
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean isStop) {
        this.isStop = isStop;
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
