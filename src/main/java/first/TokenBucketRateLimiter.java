package first;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TokenBucketRateLimiter {

    private final int maxTokens;
    private final long refillIntervalMillis;
    private final ConcurrentHashMap<Integer, Bucket> userBuckets = new ConcurrentHashMap<>();

    public TokenBucketRateLimiter(int maxTokens, long refillInterval, TimeUnit timeUnit) {
        this.maxTokens = maxTokens;
        this.refillIntervalMillis = timeUnit.toMillis(refillInterval);
    }

    public boolean allowRequest(int userId) {
        Bucket bucket = userBuckets.computeIfAbsent(userId, k -> new Bucket(maxTokens, refillIntervalMillis));

        synchronized (bucket) {
            bucket.refillTokens();
            if (bucket.tokens > 0) {
                bucket.tokens--;
                return true;
            } else {
                return false;
            }
        }
    }

    private static class Bucket {
        private int tokens;
        private final int maxTokens;
        private final long refillIntervalMillis;
        private long lastRefillTimestamp;

        public Bucket(int maxTokens, long refillIntervalMillis) {
            this.tokens = maxTokens;
            this.maxTokens = maxTokens;
            this.refillIntervalMillis = refillIntervalMillis;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        public void refillTokens() {
            long now = System.currentTimeMillis();
            long elapsedTime = now - lastRefillTimestamp;
            int tokensToAdd = (int) (elapsedTime / refillIntervalMillis);
            if (tokensToAdd > 0) {
                tokens = Math.min(maxTokens, tokens + tokensToAdd);
                lastRefillTimestamp += tokensToAdd * refillIntervalMillis;
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        TokenBucketRateLimiter tokenBucketRateLimiter = new TokenBucketRateLimiter(5, 1, TimeUnit.SECONDS);

        int userId = 1366;
        for (int i = 0; i < 10; i++) {
            System.out.println("Request " + (i + 1) + ": " + (tokenBucketRateLimiter.allowRequest(userId) ? "Allowed" : "Rejected"));
            Thread.sleep(200);
        }
    }
}
