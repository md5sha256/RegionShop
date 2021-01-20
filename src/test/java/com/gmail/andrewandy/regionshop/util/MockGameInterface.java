package com.gmail.andrewandy.regionshop.util;

import co.aikar.taskchain.AsyncQueue;
import co.aikar.taskchain.GameInterface;
import co.aikar.taskchain.TaskChainAsyncQueue;
import co.aikar.taskchain.TaskChainFactory;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MockGameInterface implements GameInterface {

    private final ScheduledExecutorService main =
            Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("main-%s").build());
    private final ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors
            .newScheduledThreadPool(3, new ThreadFactoryBuilder().setNameFormat("worker-%s").build());
    private final AsyncQueue queue = new TaskChainAsyncQueue(pool);

    @Override
    public boolean isMainThread() {
        return Thread.currentThread().getName().contains("main");
    }

    @Override
    public AsyncQueue getAsyncQueue() {
        return queue;
    }

    @Override
    public void postToMain(Runnable run) {
        main.submit(run);
    }

    @Override
    public void scheduleTask(int gameUnits, Runnable run) {
        main.schedule(run, gameUnits * 50L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void registerShutdownHandler(TaskChainFactory factory) {
        main.execute(pool::shutdown);
        main.shutdown();
    }

}
