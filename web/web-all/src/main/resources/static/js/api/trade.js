var trade = {

  api_name:'/api/order',

  // add shopping cart
  trade() {
    return request({
      url: this.api_name +'/auth/trade',
      method:'get'
    })
  },

  // Submit orders
  submitOrder(order, tradeNo) {
    return request({
      url: this.api_name +'/auth/submitOrder?tradeNo=' + tradeNo,
      method:'post',
      data: order
    })
  },

  // Get order
  getPayOrderInfo(orderId) {
    return request({
      url: this.api_name +'/auth/getPayOrderInfo/' + orderId,
      method:'get'
    })
  },

  // Get order
  getOrderPageList(page, limit) {
    return request({
      url: this.api_name + `/auth/${page}/${limit}`,
      method:'get'
    })
  }
}