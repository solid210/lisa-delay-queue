local key = KEYS[1]
local group = ARGV[1]
local range_start = ARGV[2]
local range_end = ARGV[3]
local count = ARGV[4]

local results = redis.call('XPENDING', key, group, range_start, range_end, count)
--local results = redis.call('XPENDING', key, group)
--local results = redis.call('XPENDING', key, group, "-", "+", count)
return results