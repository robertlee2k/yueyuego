package yueyueGo.utility;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BlockedThreadPoolExecutor {
	

	public static ExecutorService newFixedThreadPool(int nThreads) {

		BlockingQueue<Runnable> workingQueue = new ArrayBlockingQueue<Runnable>(nThreads);
		RejectedExecutionHandler rejectedExecutionHandler =
				new ThreadPoolExecutor.CallerRunsPolicy();
		ExecutorService threadPool = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
				workingQueue, rejectedExecutionHandler);
		return threadPool;

	}
}
