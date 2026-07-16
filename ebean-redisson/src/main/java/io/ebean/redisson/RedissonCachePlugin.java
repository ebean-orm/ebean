package io.ebean.redisson;

import io.ebean.BackgroundExecutor;
import io.ebean.DatabaseBuilder;
import io.ebean.cache.ServerCacheFactory;
import io.ebean.cache.ServerCachePlugin;

public class RedissonCachePlugin implements ServerCachePlugin {
    @Override
    public ServerCacheFactory create(DatabaseBuilder config, BackgroundExecutor executor) {
        return new RedissonCacheFactory(config.settings(), executor);
    }
}
