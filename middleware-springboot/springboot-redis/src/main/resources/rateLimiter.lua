--获取KEY
local key1 = KEYS[1]

local val = redis.call('incr', key1) -- 先自增
local ttl = redis.call('ttl', key1)

--获取ARGV内的参数并打印
local expire = ARGV[1] --注解中指定的时间区间
local times = ARGV[2] --注解中指定的一定时间区间内访问的次数

redis.log(redis.LOG_DEBUG,tostring(times))
redis.log(redis.LOG_DEBUG,tostring(expire))

redis.log(redis.LOG_NOTICE, "incr "..key1.." "..val);
if val == 1 then --代表第一次进来，需要为key1设置超时时间
    redis.call('expire', key1, tonumber(expire))
else
    if ttl == -1 then
        redis.call('expire', key1, tonumber(expire))
    end
end

if val > tonumber(times) then --这里比较的其实是每一次进来经过自增之后的数字是否大于注解中指定的一定时间内访问的次数
    return 0
end

return 1