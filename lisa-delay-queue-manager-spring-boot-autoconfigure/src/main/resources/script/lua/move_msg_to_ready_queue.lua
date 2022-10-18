-- 将到期需要执行的msgId从zset移动到stream中，有两个地方需要用到
-- 1. move message from waiting queue to ready queue
-- 2. move message from retry queue to ready queue
local keyZset = KEYS[1]
local keyStream = KEYS[2]
local startScore = ARGV[1]
local endScore = ARGV[2]
local count = ARGV[3]

-- 从zset中取出count条到期的msgId
local msgIdAndScoreList = redis.call('zrangebyscore', keyZset, startScore, endScore, 'withscores', 'limit', 0, count)
local data = {}
for i = 1, #msgIdAndScoreList, 2 do
    local msgId = msgIdAndScoreList[i]
    local msg = msgIdAndScoreList[i] .. '|' .. msgIdAndScoreList[i + 1]
    table.insert(data, msg)
    -- 将取出的msgId存入stream中
    redis.call('xadd', keyStream, '*', 'msg', msg)
    -- 从zset中删除这些msgId
    redis.call('zrem', keyZset, tostring(msgId))
end
return data