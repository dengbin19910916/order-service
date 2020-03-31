--[[
扣减库存并保存订单信息。
--]]

-- 检查库存是否满足
local checkRemaining = function(items)
    local result = true

    for i = 1, #items do
        local item = items[i]
        local pid = KEYS[i + 1]

        local remaining = tonumber(redis.call('get', pid))
        if remaining == nil then
            result = false
        else
            if tonumber(redis.call('get', pid)) < item.num then
                result = false
            end
        end
    end

    return result
end

-- 扣减库存
local incrStock = function(items)
    for i = 1, #items do
        redis.call('incrby', KEYS[i + 1], -tonumber(items[i].num))
    end
end

-- 保存订单
local saveOrder = function()
    redis.call('set', KEYS[1], ARGV[1])
end

-- main
local order = cjson.decode(ARGV[1])
local items = order['items']

local result = checkRemaining(items)
if result then
    incrStock(items)
    saveOrder(order)
end

return result

