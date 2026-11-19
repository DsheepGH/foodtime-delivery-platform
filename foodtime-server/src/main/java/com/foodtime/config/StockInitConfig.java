package com.foodtime.config;

import com.foodtime.entity.Dish;
import com.foodtime.entity.Setmeal;
import com.foodtime.mapper.DishMapper;
import com.foodtime.mapper.SetmealMapper;
import com.foodtime.utils.StockUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 库存初始化配置类
 * 系统启动时将数据库中的菜品/套餐库存加载到Redis
 */
@Component
@Slf4j
public class StockInitConfig implements CommandLineRunner {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private StockUtil stockUtil;


    @Override
    public void run(String... args) throws Exception {
        log.info("========== 开始初始化库存到Redis ==========");

        //初始化菜品库存
        List<Dish> dishList = dishMapper.list(new Dish());
        HashMap<Long, Integer> dishStockMap = new HashMap<>();
        for (Dish dish : dishList) {
            // 假设Dish实体添加了stock字段（数据库需要添加）
            // 如果没有stock字段，可以根据业务需求设置默认值
            int stock = dish.getStock() != null ? dish.getStock() : 100;
            dishStockMap.put(dish.getId(), stock);
        }
        stockUtil.batchInitStock("dish", dishStockMap);
        log.info("菜品库存初始化完成，共{}个菜品", dishList.size());

        // 初始化套餐库存
        List<Setmeal> setmealList = setmealMapper.list(new Setmeal());
        Map<Long, Integer> setmealStockMap = new HashMap<>();
        for (Setmeal setmeal : setmealList) {
            int stock = setmeal.getStock() != null ? setmeal.getStock() : 100; // 默认100
            setmealStockMap.put(setmeal.getId(), stock);
        }
        stockUtil.batchInitStock("setmeal", setmealStockMap);
        log.info("套餐库存初始化完成，共{}个套餐", setmealList.size());

        log.info("========== 库存初始化完成 ==========");

    }
}
