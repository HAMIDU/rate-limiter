package first;

import java.util.concurrent.LinkedBlockingQueue;

public class LeakyBucketRateLimiter {

    private final LinkedBlockingQueue<Long> bucket;
    private final int capacity;
    private final long leakIntervalMillis;

    public LeakyBucketRateLimiter(int capacity, long leakIntervalMillis) {
        this.bucket = new LinkedBlockingQueue<>(capacity);
        this.capacity = capacity;
        this.leakIntervalMillis = leakIntervalMillis;

        // Start the leaky process
        new Thread(this::leak).start();
    }

    public boolean allowRequest() {
        synchronized (bucket) {
            if (bucket.remainingCapacity() > 0) {
                bucket.offer(System.currentTimeMillis());
                return true; // Allow the request
            } else {
                return false; // Deny the request
            }
        }
    }

    private void leak() {
        while (true) {
            try {
                Thread.sleep(leakIntervalMillis);
                synchronized (bucket) {
                    if (!bucket.isEmpty()) {
                        bucket.poll(); // Leak one token
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LeakyBucketRateLimiter rateLimiter = new LeakyBucketRateLimiter(5, 200);

        for (int i = 0; i < 10; i++) {
            System.out.println("Request " + (i + 1) + ": " + (rateLimiter.allowRequest() ? "Allowed" : "Rejected"));
            Thread.sleep(100);
        }
    }
}
