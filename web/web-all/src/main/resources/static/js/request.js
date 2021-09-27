var request = axios.create({
    baseURL:'http://api.gmall.com',
    timeout:100000
});

//Add a request interceptor
request.interceptors.request.use(function(config){
    //Perform some operations before the request is sent
    //debugger
    if(auth.getToken()) {
        config.headers['token'] = auth.getToken();
    }
    if(auth.getUserTempId()) {
        config.headers['userTempId'] = auth.getUserTempId();
    }
    return config;
},function(error){
    //Do something with request error
    return Promise.reject(error);
});
//Add a response interceptor
request.interceptors.response.use(function(response){
    //Process the returned data here
    // debugger
    console.log(JSON.stringify(response))

    if (response.data.code == 208) {
        window.location.href ='http://passport.gmall.com/login.html?originUrl='+window.location.href
    } else {
        // debugger
        if (response.data.code == 200) {
            return response
        } else {
            //Seckill business and payment business
            if ((response.data.code >= 210 && response.data.code <220) || response.data.code == 205) {
                return response
            } else {
                console.log("response.data:" + JSON.stringify(response.data))
                alert(response.data.message ||'error')
                return Promise.reject(response)
            }
        }
    }
},function(error){
    //Do something with response error
    return Promise.reject(error);
})