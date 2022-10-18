local streamKey = KEYS[1]
local retryQueueKey = KEYS[2]
local retryCountKey = KEYS[3]
local garbageKey = KEYS[4]
local group = ARGV[1]
local rangeStart = ARGV[2]
local rangeEnd = ARGV[3]
local count = ARGV[4]
local timeout = ARGV[5]
local delayTime = ARGV[6]
local maxRetryCount = ARGV[7]

-- 命令举例：xpending stream:ready_queue:mystream group-1 - + 20
local pendingMessages = redis.call('xpending', streamKey, group, rangeStart, rangeEnd, count)
if (#(pendingMessages) > 0) then
    for i = 1,#pendingMessages,1 do
        local tables = pendingMessages[i]
        if(tables[3] > tonumber(timeout)) then
            local recordId = tables[1]
            -- 从stream中读取该消息， xrange stream:ready_queue:mystream 1665485172129-3 1665485172129-3
            local data = redis.call('xrange', streamKey, recordId, recordId)
            local msg = data[1][2][2]
            local position = string.find(msg, '|')
            local msgId = string.sub(msg, 1, position - 1)
            local score = string.sub(msg, position + 1)
            -- 从stream中移除该消息
            redis.call('xdel', streamKey, recordId)

            -- 从stream中ack掉该消息
            redis.call('xack', streamKey, group, recordId)

            -- 查询该消息的重试次数
            local retryCount = redis.call('hget', retryCountKey, msgId)
            if not(retryCount) then
                retryCount = maxRetryCount
                redis.call('hset', retryCountKey, msgId, retryCount)
            end

            if tonumber(retryCount) > 0  then
                -- 将该消息加入到retry queue(延迟delayTime毫秒后执行)
                local newScore = tonumber(score) + tonumber(delayTime)
                redis.call('zadd', retryQueueKey, newScore, msgId)
                redis.call('hincrby', retryCountKey, msgId, -1)
            else
                -- 超过最大重试次数，从hash中删掉，加入到garbageKeys中
                redis.call('hdel', retryCountKey, msgId)
                redis.call('sadd', garbageKey, msgId)
            end
        end
    end
end