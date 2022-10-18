--确认消费message，即ack
local streamKey = KEYS[1]
local retryCountKey = KEYS[2]
local garbageKey = KEYS[3]
local group = ARGV[1]
local msgId = ARGV[2]
local msgBodyKey = ARGV[3]
local recordId = ARGV[4]

-- 向stream发出ack命令
redis.call('xack', streamKey, group, recordId)

-- 同时删除msgId对应的msgValue，释放内存
local count = redis.call('del', msgBodyKey)
redis.call('hdel', retryCountKey, msgId)
redis.call('srem', garbageKey, msgId)

return count