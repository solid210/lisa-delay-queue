-- 将消息的msgId加入zset中，同时存储msgId与msgValue映射数据

local waitingQueueKey = KEYS[1]
local readyQueueKey = KEYS[2]
local msgId = ARGV[1]
local score = ARGV[2]
local msgBodyKey = ARGV[3]
local msgBodyValue = ARGV[4]
local now = ARGV[5]

redis.call('set', msgBodyKey, msgBodyValue)
if now >= score then
    -- 当前时间大于期望执行时间，直接将消息加入stream中(立即执行)
    local msg = msgId .. '|' .. score
    redis.call('xadd', readyQueueKey, '*', 'msg', msg)
else
    redis.call('zadd', waitingQueueKey, score, msgId)
end
return true