package com.atguigu.gmall.common.constant;

/**
 * Redis constant configuration class
 * set name admin
 */
public class RedisConst {

    // String set(key,value) set(sku:100016773624:info,value);
    public static final String SKUKEY_PREFIX = "sku:";
    public static final String SKUKEY_SUFFIX = ":info";
    //Unit: seconds
    public static final long SKUKEY_TIMEOUT = 24 * 60 * 60;
    // Define variables to record the cache expiration time of empty objects
    public static final long SKUKEY_TEMPORARY_TIMEOUT = 10 * 60;

    //Unit: seconds The maximum waiting time for trying to acquire a lock
    public static final long SKULOCK_EXPIRE_PX1 = 1;
    //Unit: seconds The holding time of the lock
    public static final long SKULOCK_EXPIRE_PX2 = 1;
    public static final String SKULOCK_SUFFIX = ":lock";

    public static final String USER_KEY_PREFIX = "user:";
    public static final String USER_CART_KEY_SUFFIX = ":cart";
    public static final long USER_CART_EXPIRE = 60 * 60 * 24 * 7;

    //User login
    public static final String USER_LOGIN_KEY_PREFIX = "user:login:";
    // public static final String userinfoKey_suffix = ":info";
    public static final int USERKEY_TIMEOUT = 60 * 60 * 24 * 7;

    //Scill product prefix
    public static final String SECKILL_GOODS = "seckill:goods";
    public static final String SECKILL_ORDERS = "seckill:orders";
    public static final String SECKILL_ORDERS_USERS = "seckill:orders:users";
    public static final String SECKILL_STOCK_PREFIX = "seckill:stock:";
    public static final String SECKILL_USER = "seckill:user:";
    //User lock time unit: second
    public static final int SECKILL__TIMEOUT = 60 * 60 * 1;


}