local key_zset = KEYS[1]
local score = ARGV[1]
local count = ARGV[2]

local msgIdAndScoreList = redis.call('zrangebyscore', key_zset, 0, score, 'withscores', 'limit', 0, count)
local data = {}
for i = 1, #msgIdAndScoreList, 2 do
    local msg = {}
    msg[1] = msgIdAndScoreList[i]
    msg[2] = msgIdAndScoreList[i + 1]
    table.insert(data, msg)
    -- 从zset中删除这些msgId
    redis.call('zrem', key_zset, tostring(msg[1]))
end
return data