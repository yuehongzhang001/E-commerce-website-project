var cart = {

  api_name:'/api/cart',

  // add shopping cart
  addToCart(skuId, skuNum) {
    return request({
      url: this.api_name +'/addToCart/' + skuId +'/' + skuNum,
      method:'post'
    })
  },

  // my shopping cart
  cartList() {
    return request({
      url: this.api_name +'/cartList',
      method:'get'
    })
  },

  // Update the selected state
  checkCart(skuId, isChecked) {
    return request({
      url: this.api_name +'/checkCart/' + skuId +'/' + isChecked,
      method:'get'
    })
  },

// delete
  deleteCart(skuId) {
    return request({
      url: this.api_name +'/deleteCart/' + skuId,
      method:'delete'
    })
  }
}