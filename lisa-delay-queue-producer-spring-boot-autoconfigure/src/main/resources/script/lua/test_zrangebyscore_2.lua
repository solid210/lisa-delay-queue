local key_zset = KEYS[1]
local key_stream = KEYS[2]
local score = ARGV[1]
local count = ARGV[2]

local msgIList = redis.call('zrangebyscore', key_zset, 0, score, 'withscores', 'limit', 0, count)

-- 将取出的msgId存入stream中
redis.call('xadd', key_stream, msgIList)