local key = KEYS[1]
local field = ARGV[1]

local retryCount = redis.call('hget', key, field)
if not(retryCount) then
    return 0
end
return retryCount