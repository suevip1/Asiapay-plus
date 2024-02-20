/*
 * Copyright (c) 2021-2031
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.core.cache;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayPassage;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/*
 * Redis工具类
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/5/24 17:58
 */
@Slf4j
public class RedisUtil {

    private static StringRedisTemplate stringRedisTemplate = null;

    /**
     * 获取RedisTemplate对象, 默认使用 StringRedisTemplate, 客户端可查询
     **/
    private static final RedisTemplate getStringRedisTemplate() {

        if (stringRedisTemplate == null) {

            if (SpringBeansUtil.getApplicationContext().containsBean("defaultStringRedisTemplate")) {
                stringRedisTemplate = SpringBeansUtil.getBean("defaultStringRedisTemplate", StringRedisTemplate.class);
            } else {
                stringRedisTemplate = SpringBeansUtil.getBean(StringRedisTemplate.class);
            }
        }
        return stringRedisTemplate;
    }

    /**
     * 获取缓存数据, String类型
     */
    public static String getString(String key) {
        if (key == null) {
            return null;
        }
        return (String) getStringRedisTemplate().opsForValue().get(key);
    }

    /**
     * 获取缓存数据对象
     */
    public static <T> T getObject(String key, Class<T> cls) {

        String val = getString(key);
        return JSON.parseObject(val, cls);
    }

    /**
     * 放置缓存对象
     */
    public static void setString(String key, String value) {
        getStringRedisTemplate().opsForValue().set(key, value);
    }


    /**
     * 添加到队列
     *
     * @param queueName
     * @param element
     */
    public static void addToQueue(String queueName, Object element) {
        getStringRedisTemplate().opsForList().leftPush(queueName, JSON.toJSONString(element));
    }

    /**
     * 移除队列
     *
     * @param queueName
     * @return
     */
    public static <T> T removeFromQueue(String queueName, Class<T> cls) {
        String val = (String) getStringRedisTemplate().opsForList().rightPop(queueName);
        return JSON.parseObject(val, cls);
    }

    /**
     * 获取队列长度
     *
     * @param queueName
     * @return
     */
    public static Long getQueueLength(String queueName) {
        return getStringRedisTemplate().opsForList().size(queueName);
    }

    /**
     * 获取队首元素
     *
     * @param queueName
     * @return
     */
    public static <T> T getFirstElement(String queueName, Class<T> cls) {
        String val = (String) getStringRedisTemplate().opsForList().index(queueName, 0);
        return JSON.parseObject(val, cls);
    }


    /**
     * 普通缓存放入并设置时间, 默认单位：秒
     */
    public static void setString(String key, String value, long time) {
        getStringRedisTemplate().opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    /**
     * 普通缓存放入并设置时间
     */
    public static void setString(String key, String value, long time, TimeUnit timeUnit) {
        getStringRedisTemplate().opsForValue().set(key, value, time, timeUnit);
    }

    /**
     * 放置缓存对象
     */
    public static void set(String key, Object value) {
        setString(key, JSON.toJSONString(value));
    }

    /**
     * 普通缓存放入并设置时间, 默认单位：秒
     */
    public static void set(String key, Object value, long time) {
        setString(key, JSON.toJSONString(value), time);
    }

    /**
     * 普通缓存放入并设置时间
     */
    public static void set(String key, Object value, long time, TimeUnit timeUnit) {
        setString(key, JSON.toJSONString(value), time, timeUnit);
    }

    /**
     * 指定缓存失效时间
     */
    public static void expire(String key, long time) {
        getStringRedisTemplate().expire(key, time, TimeUnit.SECONDS);
    }

    /**
     * 指定缓存失效时间
     */
    public static void expire(String key, long time, TimeUnit timeUnit) {
        getStringRedisTemplate().expire(key, time, timeUnit);
    }

    /**
     * 根据key 获取过期时间
     *
     * @param key 键 不能为null
     * @return 时间(秒) 返回0代表为永久有效
     */
    public static long getExpire(String key) {
        return getStringRedisTemplate().getExpire(key, TimeUnit.SECONDS);
    }

    /**
     * 判断key是否存在
     */
    public static boolean hasKey(String key) {
        return getStringRedisTemplate().hasKey(key);
    }

    /**
     * 删除缓存
     **/
    public static void del(String... key) {
        if (key != null && key.length > 0) {
            if (key.length == 1) {
                getStringRedisTemplate().delete(key[0]);
            } else {
                getStringRedisTemplate().delete(CollectionUtils.arrayToList(key));
            }
        }
    }

    /**
     * 从集合中读取全部数据
     *
     * @param key
     * @return
     */
    public static List<Object> retrieveData(String key) {
        return getStringRedisTemplate().opsForList().range(key, 0, -1);
    }

    public static void storeObjectWithExpiration(String key, Object data, String collectionKey) {
        HashOperations<String, String, Object> hashOperations = getStringRedisTemplate().opsForHash();
        hashOperations.put(collectionKey, key, JSON.toJSONString(data));
    }

    public static Map<String, Object> retrieveCollection(String collectionKey) {
        HashOperations<String, String, Object> hashOperations = getStringRedisTemplate().opsForHash();
        return hashOperations.entries(collectionKey);
    }

    /**
     * 从缓存集合中中获取
     *
     * @param collectionKey
     * @param key
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T getObjectWithExpiration(String collectionKey, String key, Class<T> cls) {
        HashOperations<String, String, Object> hashOperations = getStringRedisTemplate().opsForHash();
        Map<String, Object> dataMap = hashOperations.entries(collectionKey);
        if (dataMap.containsKey(key)) {
            return JSON.parseObject((String) dataMap.get(key), cls);
        } else {
            return null;
        }
    }

    public static Collection<String> keys(String pattern) {
        return getStringRedisTemplate().keys(pattern);
    }

    /**
     * 删除哈希表中的数据
     *
     * @param hashKey  集合名
     * @param fieldKey 字段名
     */
    public static void deleteFromHash(String hashKey, String fieldKey) {
        HashOperations<String, String, Object> hashOps = getStringRedisTemplate().opsForHash();
        hashOps.delete(hashKey, fieldKey);
    }


    private static final String TEMP_ORDER_SUFFIX = "pollingOrder_";

    /**
     * redis 锁,防止重复下单
     *
     * @param mchNo
     * @param mchOrderNo
     * @return
     */
    public static boolean tryLockOrder(String mchNo, String mchOrderNo) {
        String key = TEMP_ORDER_SUFFIX + mchNo + mchOrderNo;
        Boolean success = getStringRedisTemplate().opsForValue().setIfAbsent(key, "locked", 60, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(success);
    }

    /**
     * @param mchNo
     * @param mchOrderNo
     */
    public static void unlockOrder(String mchNo, String mchOrderNo) {
        String key = TEMP_ORDER_SUFFIX + mchNo + mchOrderNo;
        getStringRedisTemplate().delete(key);
    }

    /**
     * 存入异常通道信息
     *
     * @param passageId
     * @param timestamp
     */
    public static void savePassageErrorInfo(Long passageId, Long timestamp) {
        HashOperations<String, String, Object> hashOperations = getStringRedisTemplate().opsForHash();
        //判断 对应的 List<Long> 是否存在
        Object tempObj = hashOperations.get(CS.ERROR_PASSAGE, passageId.toString());
        JSONArray tempList = null;
        if (tempObj == null) {
            tempList = new JSONArray();
        } else {
            tempList = JSONArray.parseArray(tempObj.toString());
        }
        tempList.add(timestamp);

        hashOperations.put(CS.ERROR_PASSAGE, passageId.toString(), tempList.toString());
    }

    /**
     * +
     * 检查并清除数据
     *
     * @param trigger
     * @return
     */
    public static Map<Long, Integer> checkAndCleanPassageErrorInfo(int trigger) {
        Map<String, Object> errorInfoMap = retrieveCollection(CS.ERROR_PASSAGE);
        long now = System.currentTimeMillis(); // 当前时间的毫秒级时间戳
        HashOperations<String, String, Object> hashOperations = getStringRedisTemplate().opsForHash();

        Map<Long, Integer> resultIds = new HashMap<>();

        if (!errorInfoMap.isEmpty()) {
            for (Map.Entry<String, Object> entry : errorInfoMap.entrySet()) {
                JSONArray jsonArray = JSONArray.parseArray(entry.getValue().toString());
                //移除超过一分钟的数据
                for (int i = 0; i < jsonArray.size(); ) {
                    long timestamp = jsonArray.getLong(i); // 获取数组中的时间戳
                    if (now - timestamp > 60000) { // 检查时间差距是否大于一分钟（60000毫秒）
                        jsonArray.remove(i); // 移除时间戳
                        // 注意：不增加索引i，因为移除操作会导致后面的元素前移
                    } else {
                        i++; // 只有当不移除元素时才增加索引
                    }
                }

                if (jsonArray.size() >= trigger) {
                    resultIds.put(Long.parseLong(entry.getKey()), jsonArray.size());
                }
                if (jsonArray.isEmpty()) {
                    //删除这个ID的键
                    deleteFromHash(CS.ERROR_PASSAGE, entry.getKey());
                } else {
                    //替换过期数据
                    hashOperations.put(CS.ERROR_PASSAGE, entry.getKey(), jsonArray.toString());
                }

            }
        }

        return resultIds;
    }
}
