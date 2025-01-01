package second;

import java.util.*;

class RateLimiter {
    public int expire;
    public HashMap<Integer, Integer> hm = new HashMap<>();

    public RateLimiter(int expire) {
        this.expire = expire;
    }

    public boolean limit(int uid, int timestamp) {
       String a= new String();
       String b;
       String c= null;
        if (hm.containsKey(uid)) {
            if (hm.get(uid) <= timestamp) {
                hm.put(uid, timestamp + expire);
                return false;
            }
            return true;

        } else {
            hm.put(uid, timestamp + expire);
            return false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        RateLimiter rateLimiter = new RateLimiter(5);
        for (int i = 0; i < 10; i++) {
            System.out.println("Request " + (i + 1) + ": " + (rateLimiter.limit(1366, i) ? "Rejected" : "Allowed"));
            Thread.sleep(200);

        }
    }
}