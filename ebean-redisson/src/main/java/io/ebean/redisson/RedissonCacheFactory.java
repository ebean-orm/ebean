package io.ebean.redisson;


import io.avaje.applog.AppLog;
import io.ebean.BackgroundExecutor;
import io.ebean.DatabaseBuilder;
import io.ebean.cache.*;
import io.ebean.meta.MetricVisitor;
import io.ebean.metric.MetricFactory;
import io.ebean.metric.TimedMetric;
import io.ebean.redisson.dto.*;
import io.ebean.redisson.encode.CachedBeanDataCodec;
import io.ebean.redisson.encode.CachedManyIdsCodec;
import io.ebean.redisson.encode.SerializableCodec;
import io.ebean.redisson.encode.VersionGatedCodec;
import io.ebean.redisson.near.NearCacheInvalidate;
import io.ebean.redisson.near.NearCacheNotify;
import io.ebeaninternal.server.cache.DefaultServerCache;
import io.ebeaninternal.server.cache.DefaultServerCacheConfig;
import io.ebeaninternal.server.cache.DefaultServerQueryCache;
import org.redisson.Redisson;
import org.redisson.api.RReliableTopic;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.io.*;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.Logger.Level.*;

public class RedissonCacheFactory implements ServerCacheFactory {

    private static final System.Logger log = AppLog.getLogger(RedissonCacheFactory.class);

    /**
     * Channel for standard L2 cache messages.
     */
    private static final String CHANNEL_L2 = "ebean.l2cache";
    /**
     * Channel specifically for near cache invalidation messages.
     */
    private static final String CHANNEL_NEAR = "ebean.l2near";

    private final ConcurrentHashMap<String, RQueryCache> queryCaches = new ConcurrentHashMap<>();
    private final Map<String, NearCacheInvalidate> nearCacheMap = new ConcurrentHashMap<>();
    private final SerializableCodec serializableCodec = new SerializableCodec();
    private final CachedBeanDataCodec cachedBeanDataCodec = new CachedBeanDataCodec();
    private final CachedManyIdsCodec cachedManyIdsCodec = new CachedManyIdsCodec();
    private final BackgroundExecutor executor;
    private final RedissonClient redissonClient;
    private final NearCacheNotify nearCacheNotify;
    private final TimedMetric metricOutNearCache;
    private final TimedMetric metricOutTableMod;
    private final TimedMetric metricOutQueryCache;
    private final TimedMetric metricInNearCache;
    private final TimedMetric metricInTableMod;
    private final TimedMetric metricInQueryCache;
    private final String serverId = ModId.id();
    private final ReentrantLock lock = new ReentrantLock();
    private final RReliableTopic topicL2;
    private final RReliableTopic topicNear;
    private ServerCacheNotify listener;

    RedissonCacheFactory(DatabaseBuilder.Settings config, BackgroundExecutor executor) {
        this.executor = executor;
        this.nearCacheNotify = new DNearCacheNotify();
        MetricFactory factory = MetricFactory.get();
        this.metricOutTableMod = factory.createTimedMetric("l2a.outTableMod");
        this.metricOutQueryCache = factory.createTimedMetric("l2a.outQueryCache");
        this.metricOutNearCache = factory.createTimedMetric("l2a.outNearKeys");
        this.metricInTableMod = factory.createTimedMetric("l2a.inTableMod");
        this.metricInQueryCache = factory.createTimedMetric("l2a.inQueryCache");
        this.metricInNearCache = factory.createTimedMetric("l2a.inNearKeys");
        this.redissonClient = getRedissonClient(config);
        this.topicL2 = redissonClient.getReliableTopic(CHANNEL_L2);
        this.topicNear = redissonClient.getReliableTopic(CHANNEL_NEAR);
        subscribeToMessages();
    }

    private RedissonClient getRedissonClient(DatabaseBuilder.Settings config) {
        RedissonClient existingClient = config.getServiceObject(RedissonClient.class);
        if (existingClient != null) {
            return existingClient;
        }

        Config redisConfig = config.getServiceObject(Config.class);
        if (redisConfig != null) {
            return Redisson.create(redisConfig);
        }

        Config loadedConfig = null;
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream is = cl.getResourceAsStream("redisson-config.yaml");

            if (is != null) {
                loadedConfig = Config.fromYAML(is);
                log.log(INFO, "Loaded Redisson config from classpath: redisson-config.yaml");
            } else {
                log.log(WARNING, "redisson-config.yaml not found in classpath. Falling back to default config.");
            }
        } catch (IllegalArgumentException e) {
            log.log(WARNING, "Failed to load redisson-config.yaml from classpath. Falling back to default config.", e);
        }

        if (loadedConfig == null) {
            loadedConfig = new Config();
            loadedConfig.useSingleServer().setAddress("redis://localhost:6379");
            log.log(WARNING, "Using default Redisson config: redis://localhost:6379");
        }

        return Redisson.create(loadedConfig);
    }

    @Override
    public void visit(MetricVisitor visitor) {
        metricOutQueryCache.visit(visitor);
        metricOutTableMod.visit(visitor);
        metricOutNearCache.visit(visitor);
        metricInTableMod.visit(visitor);
        metricInQueryCache.visit(visitor);
        metricInNearCache.visit(visitor);
    }

    @Override
    public ServerCache createCache(ServerCacheConfig config) {
        if (config.isQueryCache()) {
            return createQueryCache(config);
        }
        return createNormalCache(config);
    }

    private ServerCache createNormalCache(ServerCacheConfig config) {
        RedissonCache redissonCache = createRedisCache(config);
        boolean nearCache = config.getCacheOptions().isNearCache();
        if (!nearCache) {
            return config.tenantAware(redissonCache);
        }

        String cacheKey = config.getCacheKey();
        DefaultServerCache near = new DefaultServerCache(new DefaultServerCacheConfig(config));
        near.periodicTrim(executor);
        DuelCache duelCache = new DuelCache(near, redissonCache, cacheKey, nearCacheNotify);
        nearCacheMap.put(cacheKey, duelCache);
        return config.tenantAware(duelCache);
    }

    private RedissonCache createRedisCache(ServerCacheConfig config) {
        switch (config.getType()) {
            case NATURAL_KEY:
                return new RedissonCache(redissonClient, config, serializableCodec, executor, false);
            case BEAN: {
                VersionGatedCodec codec = new VersionGatedCodec(cachedBeanDataCodec);
                return new RedissonCache(redissonClient, config, codec, executor, true);
            }
            case COLLECTION_IDS:
                return new RedissonCache(redissonClient, config, cachedManyIdsCodec, executor, false);
            default:
                throw new IllegalArgumentException("Unexpected cache type? " + config.getType());
        }
    }

    private ServerCache createQueryCache(ServerCacheConfig config) {
        lock.lock();
        try {
            RQueryCache cache = queryCaches.get(config.getCacheKey());
            if (cache == null) {
                log.log(DEBUG, config.getCacheKey());
                cache = new RQueryCache(new DefaultServerCacheConfig(config));
                cache.periodicTrim(executor);
                queryCaches.put(config.getCacheKey(), cache);
            }
            return config.tenantAware(cache);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public ServerCacheNotify createCacheNotify(ServerCacheNotify listener) {
        this.listener = listener;
        return new RServerCacheNotify();
    }

    private void sendQueryCacheInvalidation(String name) {
        long nanos = System.nanoTime();
        try {
            L2QueryInvalidMessage message = new L2QueryInvalidMessage();
            message.setServerId(serverId);
            message.setKey(name);

            topicL2.publish(message);
        } finally {
            metricOutQueryCache.addSinceNanos(nanos);
        }
    }

    private void sendTableMod(Set<String> dependentTables) {
        long nanos = System.nanoTime();
        try {

            L2TableModMessage message = new L2TableModMessage();
            message.setTables(dependentTables);
            message.setServerId(serverId);

            topicL2.publish(message);
        } finally {
            metricOutTableMod.addSinceNanos(nanos);
        }
    }

    /**
     * Clear the query cache if we have it.
     */
    private void queryCacheInvalidate(L2QueryInvalidMessage message) {
        if (serverId.equals(message.getServerId())) {
            // ignore this message as we are the server that sent it
            return;
        }

        long nanos = System.nanoTime();
        try {
            RQueryCache queryCache = queryCaches.get(message.getKey());
            if (queryCache != null) {
                queryCache.invalidate();
            }
        } finally {
            metricInQueryCache.addSinceNanos(nanos);
        }
    }

    /**
     * Process a remote-dependent table modify event.
     */
    private void processTableNotify(L2TableModMessage message) {
        if (serverId.equals(message.getServerId())) {
            // ignore this message as we are the server that sent it
            return;
        }
        if (listener == null) {
            log.log(DEBUG, "Ignoring tableMod, listener not registered yet");
            return;
        }
        long nanos = System.nanoTime();
        try {
            listener.notify(new ServerCacheNotification(message.getTables()));
        } finally {
            metricInTableMod.addSinceNanos(nanos);
        }
    }

    /**
     * Invalidate key for a local near cache.
     */
    private void nearCacheInvalidateKey(NearCacheInvalidateKeyMessage message) {
        String sourceServerId = message.getServerId();
        if (sourceServerId.equals(serverId)) {
            // ignore this message as we are the server that sent it
            return;
        }

        String cacheKey = message.getCacheKey();
        long nanos = System.nanoTime();
        try (ObjectInputStream oi = new ObjectInputStream(new ByteArrayInputStream(message.getKey()))) {
            Object key = oi.readObject();
            NearCacheInvalidate invalidate = nearCacheMap.get(cacheKey);
            if (invalidate == null) {
                warnNearCacheNotFound(cacheKey);
            } else {
                invalidate.invalidateKey(key);
            }
        } catch (IOException | ClassNotFoundException e) {
            log.log(ERROR, "failed to decode near cache message [" + message + "] for cache:" + cacheKey, e);
            if (cacheKey != null) {
                nearCacheInvalidateClear(cacheKey);
            }
        } finally {
            metricInNearCache.addSinceNanos(nanos);
        }
    }

    /**
     * Invalidate keys for a local near cache.
     */
    private void nearCacheInvalidateKeys(NearCacheInvalidateKeysMessage message) {
        String sourceServerId = message.getServerId();
        if (sourceServerId.equals(serverId)) {
            // ignore this message as we are the server that sent it
            return;
        }

        String cacheKey = message.getCacheKey();
        long nanos = System.nanoTime();
        try (ObjectInputStream oi = new ObjectInputStream(new ByteArrayInputStream(message.getKeys()))) {
            int total = oi.readInt();
            Set<Object> keys = new LinkedHashSet<>();
            for (int i = 0; i < total; i++) {
                keys.add(oi.readObject());
            }
            NearCacheInvalidate invalidate = nearCacheMap.get(cacheKey);
            if (invalidate == null) {
                warnNearCacheNotFound(cacheKey);
            } else {
                invalidate.invalidateKeys(keys);
            }
        } catch (IOException | ClassNotFoundException e) {
            log.log(ERROR, "failed to decode near cache message [" + message + "] for cache:" + cacheKey, e);
            if (cacheKey != null) {
                nearCacheInvalidateClear(cacheKey);
            }
        } finally {
            metricInNearCache.addSinceNanos(nanos);
        }
    }

    /**
     * Invalidate clear for a local near cache.
     */
    private void nearCacheInvalidateClear(String cacheKey) {
        NearCacheInvalidate invalidate = nearCacheMap.get(cacheKey);
        if (invalidate == null) {
            warnNearCacheNotFound(cacheKey);
        } else {
            invalidate.invalidateClear();
        }
    }

    private void nearCacheInvalidateClear(NearCacheClearMessage message) {
        String sourceServerId = message.getServerId();
        if (sourceServerId.equals(serverId)) {
            // ignore this message as we are the server that sent it
            return;
        }

        String cacheKey = message.getCacheKey();
        long nanos = System.nanoTime();
        try {
            NearCacheInvalidate invalidate = nearCacheMap.get(cacheKey);
            if (invalidate == null) {
                warnNearCacheNotFound(cacheKey);
            } else {
                invalidate.invalidateClear();
            }
        } finally {
            metricInNearCache.addSinceNanos(nanos);
        }
    }

    private void warnNearCacheNotFound(String cacheKey) {
        log.log(WARNING, "No near cache found for cacheKey [" + cacheKey + "] yet - probably on startup");
    }

    private void subscribeToMessages() {
        topicL2.addListener(L2QueryInvalidMessage.class, (channel, message) -> queryCacheInvalidate(message));
        topicL2.addListener(L2TableModMessage.class, (channel, message) -> processTableNotify(message));
        topicNear.addListener(NearCacheClearMessage.class, (channel, message) -> nearCacheInvalidateClear(message));
        topicNear.addListener(NearCacheInvalidateKeyMessage.class, (channel, message) -> nearCacheInvalidateKey(message));
        topicNear.addListener(NearCacheInvalidateKeysMessage.class, (channel, message) -> nearCacheInvalidateKeys(message));
    }

    /**
     * Query cache implementation using a Redis channel for message notifications.
     */
    private class RQueryCache extends DefaultServerQueryCache {

        RQueryCache(DefaultServerCacheConfig config) {
            super(config);
        }

        @Override
        public void clear() {
            super.clear();
            sendQueryCacheInvalidation(name);
        }

        /**
         * Process the invalidation message coming from the cluster.
         */
        private void invalidate() {
            super.clear();
        }
    }

    /**
     * Publish table modifications using a Redis channel (to other cluster members)
     */
    private class RServerCacheNotify implements ServerCacheNotify {

        @Override
        public void notify(ServerCacheNotification tableModifications) {
            Set<String> dependentTables = tableModifications.getDependentTables();
            if (dependentTables != null && !dependentTables.isEmpty()) {
                sendTableMod(dependentTables);
            }
        }
    }

    private class DNearCacheNotify implements NearCacheNotify {

        @Override
        public void invalidateKeys(String cacheKey, Set<Object> keySet) {
            try {
                ByteArrayOutputStream ba = new ByteArrayOutputStream(100);
                ObjectOutputStream os = new ObjectOutputStream(ba);
                os.writeInt(keySet.size());
                for (Object key : keySet) {
                    os.writeObject(key);
                }
                os.flush();
                os.close();

                NearCacheInvalidateKeysMessage message = new NearCacheInvalidateKeysMessage();
                message.setServerId(serverId);
                message.setCacheKey(cacheKey);
                message.setKeys(ba.toByteArray());
                sendMessage(message);
            } catch (IOException e) {
                log.log(ERROR, "failed to transmit invalidateKeys() message", e);
            }
        }

        @Override
        public void invalidateKey(String cacheKey, Object id) {
            try {
                ByteArrayOutputStream ba = new ByteArrayOutputStream(100);
                ObjectOutputStream os = new ObjectOutputStream(ba);
                os.writeObject(id);
                os.flush();
                os.close();

                NearCacheInvalidateKeyMessage message = new NearCacheInvalidateKeyMessage();
                message.setServerId(serverId);
                message.setCacheKey(cacheKey);
                message.setKey(ba.toByteArray());
                sendMessage(message);
            } catch (IOException e) {
                log.log(ERROR, "failed to transmit invalidateKeys() message", e);
            }
        }

        @Override
        public void invalidateClear(String cacheKey) {
            NearCacheClearMessage message = new NearCacheClearMessage();
            message.setServerId(serverId);
            message.setCacheKey(cacheKey);
            sendMessage(message);
        }

        private void sendMessage(NearMessage message) {
            long nanos = System.nanoTime();
            try {
                topicNear.publish(message);
            } finally {
                metricOutNearCache.addSinceNanos(nanos);
            }
        }
    }
}
