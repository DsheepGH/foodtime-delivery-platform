-- 库存扣减Lua脚本（原子操作）
-- KEYS[1]: 菜品/套餐的Redis Key (如: stock:dish:1)
-- ARGV[1]: 要扣减的数量
-- 返回值: 1-成功, 0-库存不足, -1-Key不存在

local stockKey = KEYS[1]
local deductCount = tonumber(ARGV[1])

-- 获取当前库存
local stock = redis.call('GET', stockKey)

-- 如果Key不存在，返回-1
if stock == false then
    return -1
end

-- 转换为数字
stock = tonumber(stock)

-- 判断库存是否足够
if stock < deductCount then
    return 0  -- 库存不足
end

-- 扣减库存
redis.call('DECRBY', stockKey, deductCount)

return 1  -- 扣减成功