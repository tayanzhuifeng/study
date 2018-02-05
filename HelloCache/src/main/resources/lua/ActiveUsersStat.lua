 --[[
统计活跃用户
KEYS[1]:key
ARGV[1]:增加的值
--]]
 
 if redis.call("EXISTS",KEYS[1]) == 1 then
     return redis.call("INCRBY",KEYS[1],ARGV[1])
   else
     return nil
   end  