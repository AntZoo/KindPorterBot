package eu.zujev.milovidobot

import redis.clients.jedis.Jedis

class DB {
    private static Jedis jedis

    static connect() {
        jedis = new Jedis('127.0.0.1', 32768)
    }

    static connect(address, port) {
        jedis = new Jedis(address, port)
    }

    static getInstance() {
        return jedis
    }
}
