package app;

import app.supported.interceptor.GetIdInterceptor;
import app.supported.interceptor.CheckIdentity;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;


/**
 * @Author Fizz Pu
 * @Date 2020/10/4 上午12:52
 * @Version 1.0
 * 失之毫厘，缪之千里！
 */

// java代码进行配置
@Configuration
@EnableWebMvc
@EnableCaching // 开启缓存
public class AppConfig {

    /**
     * 创建第三方Bean,函数返回值自动装入ioc容器
     * @return SessionFactory
     */
    @Bean(name="Hibernate")
    public SessionFactory createSessionFactory(){
        org.hibernate.cfg.Configuration config = new org.hibernate.cfg.Configuration();
        return config.configure().buildSessionFactory();
    }

    /**
     * 处理前端通过form data二进制流的方式提交的数据
     * @return CommonsMultipartResolver
     */
    @Bean(name = "multipartResolver")
    public CommonsMultipartResolver createMulitipartResolver(){
        return new CommonsMultipartResolver();
    }

    /**
     * 使用java配置springmvc的监听器, 当然, 也可以使用配置文件进行配置
     */

    @Autowired
    GetIdInterceptor getIdInterceptor;

    @Autowired
    CheckIdentity checkIdentity;

    @Bean
    public WebMvcConfigurer configWebMvc(){
        return new WebMvcConfigurerAdapter(){
            @Override
            public void addInterceptors(InterceptorRegistry interceptorRegistry) {
                interceptorRegistry.addInterceptor(getIdInterceptor).addPathPatterns("/**").
                        excludePathPatterns("/cumt/web/login").excludePathPatterns("/cumt/web/register");

                interceptorRegistry.addInterceptor(checkIdentity).addPathPatterns("/**").excludePathPatterns("/").
                        excludePathPatterns("/cumt/web/login").excludePathPatterns("/cumt/web/register");
            }
        };
    }

    // 配置缓存
    @Bean(name = "redisTemplate")
    public RedisTemplate initRedisTemplate() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        // 最大空闲数
        poolConfig.setMaxIdle(50);
        // 最大连接数
        poolConfig.setMaxTotal(100);
        // 最大等待亳秒数
        poolConfig.setMaxWaitMillis(20000);
        // 创建Jedis连接工厂
        JedisConnectionFactory connectionFactory = JedisConnectionFactory(poolConfig);
        connectionFactory.setHostName("localhost");
        connectionFactory.setPort(6379);
        // 调用后初始化方法，没有它将抛出异常
        connectionFactory.afterPropertiesSet();
        // 自定Redis序列化器
        RedisSerializer jdkSerializationRedisSerializer = new JdkSerializationRedisSerializer();
        RedisSerializer stringRedisSerializer = new StringRedisSerializer();
        // 定义RedisTemplate，并设置连接工程
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(connectionFactory);
        // 设置序列化器
        redisTemplate.setDefaultSerializer(stringRedisSerializer);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(jdkSerializationRedisSerializer);
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(jdkSerializationRedisSerializer);
        return redisTemplate;
    }
    @Bean(name = "redisCacheManager")
    public CacheManager initRedisCacheManager(@Autowired RedisTemplate redisTempate) {
        RedisCacheManager cacheManager = new RedisCacheManager(redisTempate);
        // 设置超时时间为10分钟，单位为秒
        cacheManager.setDefaultExpiration(600);
        // 设置缓存名称
        List<String> cacheNames = new ArrayList<String>();
        cacheNames.add("redisCacheManager");
        cacheManager.setCacheNames(cacheNames);
        return cacheManager;
    }


}

