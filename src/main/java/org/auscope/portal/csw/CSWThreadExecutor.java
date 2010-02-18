package org.auscope.portal.csw;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple wrapper around a thread pool
 */
@Repository
public class CSWThreadExecutor implements Executor {
	public static final int THREADPOOL_SIZE = 5;
	
	private ExecutorService threadPool = Executors.newFixedThreadPool(THREADPOOL_SIZE); 
	
    public void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }
    
    public ExecutorService getExecutorService() {
    	return threadPool;
    }
    
    public void setExecutorService(ExecutorService service) {
    	threadPool = service;
    }
}