package first;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class SlidingWindowRateLimiter {

    private final int requestLimit;
    private final long windowDurationMillis;
    private final ConcurrentHashMap<String, Long> userTimestamps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicInteger> userRequestCounts = new ConcurrentHashMap<>();

    public SlidingWindowRateLimiter(int requestLimit, long windowDurationMillis) {
        this.requestLimit = requestLimit;
        this.windowDurationMillis = windowDurationMillis;
    }

    public boolean allowRequest(String userId) {
        long currentTime = System.currentTimeMillis();
        userTimestamps.putIfAbsent(userId, currentTime);
        userRequestCounts.putIfAbsent(userId, new AtomicInteger(0));

        synchronized (userId.intern()) {
            long startTime = userTimestamps.get(userId);
            if (currentTime - startTime >= windowDurationMillis) {
                // Reset the window
                userTimestamps.put(userId, currentTime);
                userRequestCounts.get(userId).set(0);
            }

            if (userRequestCounts.get(userId).incrementAndGet() <= requestLimit) {
                return true; // Allow request
            } else {
                return false; // Deny request
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        SlidingWindowRateLimiter rateLimiter = new SlidingWindowRateLimiter(5, 1000); // 5 requests per second

        String userId = "user1";
        for (int i = 0; i < 10; i++) {
            System.out.println("Request " + (i + 1) + ": " + (rateLimiter.allowRequest(userId) ? "Allowed" : "Rejected"));
            Thread.sleep(200);
        }
    }
}

