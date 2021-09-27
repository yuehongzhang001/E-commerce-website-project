var item = {

  api_name:'/api/item',

  // Get sku detailed information
  get(skuId) {
    return request({
      url: this.api_name +'/' + skuId,
      method:'get'
    })
  },

  //Get basic information of sku
  getSkuInfo(skuId) {
    return request({
      url: this.api_name +'/getSkuInfo/' + skuId,
      method:'get'
    })
  }
}