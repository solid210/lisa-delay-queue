-- 将消息的msgId加入zset中，同时存储msgId与msgValue映射数据

local waitingQueueKey = KEYS[1]
local msgId = ARGV[1]
local score = ARGV[2]
local msgBodyKey = ARGV[3]
local msgBodyValue = ARGV[4]

redis.call('zadd', waitingQueueKey, score, msgId)
redis.call('set', msgBodyKey, msgBodyValue)

return true