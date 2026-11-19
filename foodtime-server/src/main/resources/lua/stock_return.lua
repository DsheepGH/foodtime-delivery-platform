-- 库存归还Lua脚本（原子操作）
-- KEYS[1]: 菜品/套餐的Redis Key (如: stock:dish:1)
-- ARGV[1]: 要归还的数量

local stockKey = KEYS[1]
local returnCount = tonumber(ARGV[1])

-- 归还库存（增加）
redis.call('INCRBY', stockKey, returnCount)

return 1  -- 归还成功