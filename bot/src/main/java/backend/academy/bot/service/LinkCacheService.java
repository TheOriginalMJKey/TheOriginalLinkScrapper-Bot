package backend.academy.bot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LinkCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String CACHE_PREFIX = "links:";
    
    public void cacheLinks(Long chatId, List<String> links) {
        String cacheKey = CACHE_PREFIX + chatId;
        redisTemplate.delete(cacheKey);
        if (!links.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(cacheKey, links.toArray());
        }
    }
    
    public List<Object> getCachedLinks(Long chatId) {
        String cacheKey = CACHE_PREFIX + chatId;
        return redisTemplate.opsForList().range(cacheKey, 0, -1);
    }
    
    public void invalidateCache(Long chatId) {
        String cacheKey = CACHE_PREFIX + chatId;
        redisTemplate.delete(cacheKey);
    }
    
    public void addLink(Long chatId, String link) {
        String cacheKey = CACHE_PREFIX + chatId;
        redisTemplate.opsForList().rightPush(cacheKey, link);
    }
    
    public void removeLink(Long chatId, String link) {
        String cacheKey = CACHE_PREFIX + chatId;
        redisTemplate.opsForList().remove(cacheKey, 0, link);
    }
} 