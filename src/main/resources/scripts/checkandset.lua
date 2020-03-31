-- checkandset.lua
local current = redis.call('GET', 'foo1')
if current == 0
then redis.call('SET', 'foo1', 10)
    return true
end
return false