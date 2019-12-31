package com.fzk.stress.util;

import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.*;


/**
 * @author LuoTao
 * @email taomee517@qq.com
 * @date 2019/3/25
 * @time 11:12
 */
public class ThreadPoolUtil {
    public static ThreadFactory threadFactory = new DefaultThreadFactory("PressureTest");
    public static final ExecutorService pool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors()*2,Runtime.getRuntime().availableProcessors()*2,0L, TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(1024),threadFactory);

    public static final ScheduledThreadPoolExecutor schedule = new ScheduledThreadPoolExecutor(8);
}
