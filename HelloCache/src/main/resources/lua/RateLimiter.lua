--[[
Judge status 
KEYS[1]:key
ARGV[1]:request numbers
ARGV[2]:expires times seconds
--]]
 
local key, rqn, exp  = KEYS[1], ARGV[1], ARGV[2];
local value=redis.call("incr", key);
redis.log(redis.LOG_NOTICE, "incr "..key);
if(tonumber(value) == 1)then
   redis.call("expire", key,  exp);
   redis.log(redis.LOG_NOTICE, "expire "..key.." "..exp)
   return true;
else
   return tonumber(value) <= tonumber(rqn);
end