package com.foodtime.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 库存管理工具类 - 使用Redis Lua脚本实现原子操作
 */
@Component
@Slf4j
public class StockUtil {

    @Autowired
    private RedisTemplate redisTemplate;

    //库存Key前缀
    private static final String STOCK_KEY_PREFIX = "stock";

    //加载Lua脚本
    private final DefaultRedisScript<Long> stockDeductionScript;
    private final DefaultRedisScript<Long> stockReturnScript;

    public StockUtil() {
        //初始化扣减库存脚本
        this.stockDeductionScript = new DefaultRedisScript<>();
        this.stockDeductionScript.setLocation(
                new ClassPathResource("lua/stock_deduction.lua")
        );
        this.stockDeductionScript.setResultType(Long.class);

        //初始化归还库存脚本
        this.stockReturnScript = new DefaultRedisScript<>();
        this.stockReturnScript.setLocation(
                new ClassPathResource("lua/stock_return.lua")
        );
        this.stockReturnScript.setResultType(Long.class);
    }

    /**
     * 初始化菜品/套餐库存到Redis
     * @param type 类型 (dish/setmeal)
     * @param id 菜品/套餐ID
     * @param stock 库存数量
     */
    public void initStock(String type, Long id, Integer stock) {
        String key = STOCK_KEY_PREFIX + type + ":" + id;
        redisTemplate.opsForValue().set(key, String.valueOf(stock));
        log.info("初始化库存: key={}, stock={}", key, stock);
    }

    /**
     * 批量初始化库存
     * @param type 类型 (dish/setmeal)
     * @param stockMap Map<ID, 库存数量>
     */
    public void batchInitStock(String type, Map<Long, Integer> stockMap) {
        for (Map.Entry<Long, Integer> entry : stockMap.entrySet()) {
            initStock(type, entry.getKey(), entry.getValue());
        }
    }

    /**
     * 原子扣减库存（使用Lua脚本）
     * @param type 类型 (dish/setmeal)
     * @param id 菜品/套餐ID
     * @param count 扣减数量
     * @return 1-成功, 0-库存不足, -1-Key不存在
     */
    public Long deductStock(String type, Long id, Integer count) {
        String key = STOCK_KEY_PREFIX + type + ":" + id;
        List<String> keys = Collections.singletonList(key);

        Long result = (Long) redisTemplate.execute(
                stockDeductionScript, keys, String.valueOf(count));

        log.info("扣减库存: key={}, count={}, result={}", key, count, result);
        return result;
    }

    /**
     * 批量扣减库存
     * @param items 扣减项列表
     * @return 所有扣减是否成功
     */
    public boolean batchDeductStock(List<StockDeductionItem> items) {
        for (StockDeductionItem item : items) {
            Long result = deductStock(item.getType(), item.getId(), item.getCount());
            if (result != 1L) {
                //扣减失败，归还已扣减的库存
                log.warn("扣减库存失败，开始归还已扣减的库存");
                for (int i = 0;i < items.indexOf(item); i++) {
                    StockDeductionItem doneItem = items.get(i);
                    returnStock(doneItem.getType(), doneItem.getId(), doneItem.getCount());
                }
                return false;
            }
        }
        return true;
    }
    /**
     * 归还库存（下单失败/取消订单时调用）
     * @param type 类型 (dish/setmeal)
     * @param id 菜品/套餐ID
     * @param count 归还数量
     */
    public void returnStock(String type, Long id, Integer count) {
        String key = STOCK_KEY_PREFIX + type + ":" + id;
        List<String> keys = Collections.singletonList(key);
        redisTemplate.execute(
                stockReturnScript, keys, String.valueOf(count));

        log.info("归还库存: key={}, count={}", key, count);
    }

    /**
     * 查询当前库存
     * @param type 类型 (dish/setmeal)
     * @param id 菜品/套餐ID
     * @return 库存数量，如果Key不存在返回-1
     */
    public Integer getStock(String type, Long id) {
        String key = STOCK_KEY_PREFIX + type + ":" + id;
        String stock = (String) redisTemplate.opsForValue().get(key);
        if (stock == null) {
            return -1;
        }
        return Integer.parseInt(stock);
    }
    /**
     * 库存扣减项内部类
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class StockDeductionItem {
        private String type;  // dish 或 setmeal
        private Long id;
        private Integer count;
    }
}
