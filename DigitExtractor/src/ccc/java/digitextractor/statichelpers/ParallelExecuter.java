package ccc.java.digitextractor.statichelpers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelExecuter
{
	private static ExecutorService threadPool;
	public static final String NUM_THREADS = "org.bytedeco.javacv.numthreads";

	public static void initialize()
	{
		threadPool = Executors.newCachedThreadPool();
	}

	public static int getNumThreads()
	{
		try
		{
			String s = System.getProperty(NUM_THREADS);

			if (s != null)
			{
				return Integer.valueOf(s);
			}
		}
		catch (NumberFormatException e)
		{
			throw new RuntimeException(e);
		}
		return getNumCores();
	}

	public static void setNumThreads(int numThreads)
	{
		System.setProperty(NUM_THREADS, Integer.toString(numThreads));
	}

	public static int getNumCores()
	{
		int i = Runtime.getRuntime().availableProcessors();
		return i;
	}

	public static void run(Runnable... runnables)
	{
		if (runnables.length == 1)
		{
			runnables[0].run();
			return;
		}

		Future[] futures = new Future[runnables.length];
		for (int i = 0; i < runnables.length; i++)
		{
			futures[i] = threadPool.submit(runnables[i]);
		}

		Throwable error = null;
		try
		{
			for (Future f : futures)
			{
				if (!f.isDone())
				{
					f.get();
				}
			}
		}
		catch (Throwable t)
		{
			error = t;
		}

		if (error != null)
		{
			for (Future f : futures)
			{
				f.cancel(true);
			}

			// throw new RuntimeException(error);
		}
	}

	public interface Looper
	{
		void loop(int from, int to, int looperID);
	}

	public static void loop(int from, int to, final Looper looper)
	{
		loop(from, to, getNumThreads(), looper);
	}

	public static void loop(int from, int to, int numThreads, final Looper looper)
	{
		int numLoopers = Math.min(to - from, numThreads > 0 ? numThreads : getNumCores());
		Runnable[] runnables = new Runnable[numLoopers];
		for (int i = 0; i < numLoopers; i++)
		{
			final int subFrom = (to - from) * i / numLoopers + from;
			final int subTo = (to - from) * (i + 1) / numLoopers + from;
			final int looperID = i;
			runnables[i] = new Runnable()
			{
				public void run()
				{
					looper.loop(subFrom, subTo, looperID);
				}
			};
		}
		run(runnables);
	}

	public static void shutdown()
	{
		threadPool.shutdown();
	}
}
