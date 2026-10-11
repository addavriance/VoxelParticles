package me.adda.voxelparticles.render;

import me.adda.voxelparticles.VoxelParticles;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class RenderGuard {
	private static final long WINDOW_NS = 1_000_000_000L;
	private static final long BUDGET_NS = 400_000_000L;
	private static final long HITCH_NS = 50_000_000L;
	private static final long COOLDOWN_NS = 5_000_000_000L;
	private static final long STALL_NS = 2_000_000_000L;

	private static long windowStart;
	private static long spent;
	private static long trippedUntil;
	private static boolean warned;

	private static volatile long busySince;
	private static volatile Thread renderThread;
	private static Thread watchdog;

	private RenderGuard() {}

	/** True while particles should be drawn flat because the mod recently used too much time */
	public static boolean tripped() {
		return trippedUntil != 0 && System.nanoTime() < trippedUntil;
	}

	public static void enter() {
		busySince = System.nanoTime();

		if (renderThread == null) {
			startWatchdog();
		}
	}

	public static void exit() {
		long now = System.nanoTime();
		long start = busySince;

		busySince = 0;

		if (start != 0) {
			account(now - start, now);
		}
	}

	private static void account(long nanos, long now) {
		if (now - windowStart > WINDOW_NS) {
			windowStart = now;
			spent = 0;
		}

		spent += nanos;

		if ((spent > BUDGET_NS || nanos > HITCH_NS) && !tripped()) {
			trippedUntil = now + COOLDOWN_NS;

			if (!warned) {
				warned = true;
				VoxelParticles.LOG.warn("Voxel particles used {} ms in the last second; drawing flat for a moment", spent / 1_000_000);
			}
		}
	}

	private static String formattedStackTrace() {
		return Arrays.stream(renderThread.getStackTrace()).limit(30).map(s -> "\tat " + s).collect(Collectors.joining("\n"));
	}

	private static synchronized void startWatchdog() {
		if (watchdog != null) {
			return;
		}

		renderThread = Thread.currentThread();

		watchdog = new Thread(RenderGuard::watch, "Voxel Particles watchdog");
		watchdog.setDaemon(true);
		watchdog.start();
	}

	private static void watch() {
		while (true) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				return;
			}

			long start = busySince;
			if (start != 0 && System.nanoTime() - start > STALL_NS) {
				VoxelParticles.LOG.error("Render thread stuck in Voxel Particles for {} ms:\n{}",
					(System.nanoTime() - start) / 1_000_000, formattedStackTrace()
				);

				while (busySince == start) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		}
	}
}
