local value = ARGV[1]
local key = KEYS[1]

redis.call('SET', key, value)

local result = redis.call('GET', key)
local msg = {}
msg[1] = "hello"
msg[2] = "world"

return json.encode(msg)