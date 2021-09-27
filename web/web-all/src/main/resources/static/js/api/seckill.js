var seckill = {

    api_name:'/api/activity/seckill',

    // add shopping cart
    findAll() {
        return request({
            url: this.api_name +'/findAll',
            method:'get'
        })
    },

    // Get seckill products
    getSeckillGoods(skuId) {
        return request({
            url: this.api_name +'/getSeckillGoods/' + skuId,
            method:'get'
        })
    },

    // Get the spike parameters
    getSeckillSkuIdStr(skuId) {
        return request({
            url: this.api_name +'/auth/getSeckillSkuIdStr/' + skuId,
            method:'get'
        })
    },

    // place an order
    seckillOrder(skuId, skuIdStr) {
        return request({
            url: this.api_name +'/auth/seckillOrder/' + skuId +'?skuIdStr=' + skuIdStr,
            method:'post'
        })
    },

    // checking order
    checkOrder(skuId) {
        return request({
            url: this.api_name +'/auth/checkOrder/' + skuId,
            method:'get'
        })
    },

    // Submit orders
    submitOrder(order) {
        return request({
            url: this.api_name +'/auth/submitOrder',
            method:'post',
            data: order
        })
    },
}