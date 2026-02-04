package com.example.rate_limiter.strategy;

import java.util.Set;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import java.util.Set;

@Component
public class SlidingWindowStrategy implements RateLimitStrategy{ // use sorted set to store time stamps

    private final RedisTemplate<String, String> redisTemplate; // stored as plain strings

    public SlidingWindowStrategy(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean isAllowed(String identifier, int maxRequests, int windowSeconds) {
        // check if request is valid
        String key = getKey(identifier);
        Long now = System.currentTimeMillis();
        Long windowStart = now- (windowSeconds * 1000L);

        // remove older entries if not within window
        redisTemplate.opsForZSet().removeRangeByScore(key,0,windowStart);

        Long count = redisTemplate.opsForZSet().zCard(key);

        if(count == null || count < maxRequests){

            //if the window is empty or count is less then permitted requests add it in set
            // zset stores member and score
            redisTemplate.opsForZSet().add(key , String.valueOf(now),now);

            // set expiry for the entry
            redisTemplate.expire(key,windowSeconds,TimeUnit.SECONDS);
            return true;

        }
        return false;

    }
    
    @Override
    public long getRemainingRequests(String identifier, int maxRequests, int windowSeconds) {
        // check remaining request in the window
        String key = getKey(identifier);
        Long now = System.currentTimeMillis();
        Long windowStart = now- (windowSeconds * 1000L);

        // remove older entries if not within window
        redisTemplate.opsForZSet().removeRangeByScore(key,0,windowStart);

        Long count = redisTemplate.opsForZSet().zCard(key);

        if(count == null){
            return maxRequests;
        }


        return (long)maxRequests - count;
    }
    
    @Override
    public long getResetTime(String identifier, int windowSeconds) {
        // the window resets when the oldest entry in the set expires , only then can a req be added to set
        // get the oldest req timestamp, add expiry to it, subtract it from curr time , thats our rest time

        String key = getKey(identifier);
        Long now = System.currentTimeMillis();
        Long windowStart = now- (windowSeconds * 1000L);

        // remove older entries if not within window
        redisTemplate.opsForZSet().removeRangeByScore(key,0,windowStart);

        // get the oldest entry from redis, returned as a typed tuple containing member and score
        Set<ZSetOperations.TypedTuple<String>> oldestEntry = redisTemplate.opsForZSet().rangeWithScores(key,0,0);

        if(oldestEntry==null || oldestEntry.isEmpty()){
            return 0;
        }

        // use iterator to get element from set 
        ZSetOperations.TypedTuple<String> oldest = oldestEntry.iterator().next();
        Double score = oldest.getScore();

        if (score == null) {
            return 0;
        }

        //  we add the window seconds to oldest time stamp, thats when it will expire, subtract it from our current time, thats our retry time
        long oldestTime = score.longValue(); // convert to long
        long expire = oldestTime+ windowSeconds*1000L; //add window 
        long retry = expire-now; // get time when to retry

         
        return Math.max(0, retry/1000);
    }
    
    private String getKey(String identifier) {
        return "rate:sliding:" + identifier;
    }

}
