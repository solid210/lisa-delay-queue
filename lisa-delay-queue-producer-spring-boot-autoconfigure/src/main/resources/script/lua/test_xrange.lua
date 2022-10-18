local streamKey = KEYS[1]
local recordId = ARGV[1]

local data = redis.call('xrange', streamKey, recordId, recordId)
local msg = data[1][2][2]
local position = string.find(msg, '|')
local msgId = string.sub(msg, 1, position - 1)
local score = string.sub(msg, position + 1)
return score