var list = {

  api_name:'/api/list',

  // search
  getPageList(searchObj) {
    return request({
      url: this.api_name +'/search',
      method:'post',
      data: searchObj // url query string or form key-value pair
    })
  },
}