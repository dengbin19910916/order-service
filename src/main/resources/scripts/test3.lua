redis.call('SET', KEYS[1], ARGV[1]);
return KEYS[1] .. ' -> ' .. ARGV[1]