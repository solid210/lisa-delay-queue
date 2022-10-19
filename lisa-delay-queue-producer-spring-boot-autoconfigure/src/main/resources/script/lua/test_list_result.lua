local function test(val)
    local ret1 = {1, 2}
    local ret2 = "hello"
    local ret3 = val
    local ret = {}
    ret[1] = ret1
    ret[2] = ret2
    ret[3] = ret3
    return ret
end
return test(KEYS[1])