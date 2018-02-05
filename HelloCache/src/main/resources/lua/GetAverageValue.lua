--[[
Get Average Value 
KEYS[1]:key
KEYS[2]:
ARGV[1]:
--]]
local currentval = tonumber(redis.call('get', KEYS[1])) or 0
local count = redis.call('incr', KEYS[2])
currentval = tostring(currentval * (count - 1)/count + (ARGV[1]/count))
redis.call('set', KEYS[1], currentval)
return currentval