package first;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FixedWindowRateLimiter {

    private final int requestLimit;
    private final long windowDurationMillis;
    private final ConcurrentHashMap<String, Window> userWindows = new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(int requestLimit, long windowDurationMillis) {
        this.requestLimit = requestLimit;
        this.windowDurationMillis = windowDurationMillis;
    }

    public boolean allowRequest(String userId) {
        long currentTime = System.currentTimeMillis();
        Window window = userWindows.computeIfAbsent(userId, k -> new Window(currentTime));

        synchronized (window) {
            if (currentTime - window.startTime >= windowDurationMillis) {
                // Reset the window
                window.startTime = currentTime;
                window.requestCount.set(0);
            }

            if (window.requestCount.incrementAndGet() <= requestLimit) {
                return true; // Allow the request
            } else {
                return false; // Deny the request
            }
        }
    }

    private static class Window {
        long startTime;
        AtomicInteger requestCount;

        public Window(long startTime) {
            this.startTime = startTime;
            this.requestCount = new AtomicInteger(0);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        FixedWindowRateLimiter rateLimiter = new FixedWindowRateLimiter(5, 1000); // 5 requests per second

        String userId = "user1";
        for (int i = 0; i < 10; i++) {
            System.out.println("Request " + (i + 1) + ": " + (rateLimiter.allowRequest(userId) ? "Allowed" : "Rejected"));
            Thread.sleep(200);
        }
    }
}
