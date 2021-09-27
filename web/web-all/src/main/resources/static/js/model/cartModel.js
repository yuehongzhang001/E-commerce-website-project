/**
 * Created by hans on 2016/10/31.
 * Shopping cart data
 */
var cartModel = {

    // Add items to the shopping cart
    add: function (data, success) {
        czHttp.getJSON('./data/success.json', data, function (responseData) {
            if(responseData.isok){
                success(responseData);
            }
        });
    },

    // Delete items in the shopping cart
    remove: function (data, success) {
        czHttp.getJSON('./data/success.json', data, function (responseData) {
            if(responseData.isok){
                success(responseData);
            }
        });
    },

    // modify the quantity of goods
    changeNumber: function (data, success) {
        czHttp.getJSON('./data/success.json', data, function (responseData) {
            if(responseData.isok){
                success(responseData);
            }
        });
    },

    // Shopping cart statistics
    subtotal: function (success) {
        czHttp.getJSON('./data/orders.json', data, function (responseData) {
            if(responseData.isok){
                success(responseData);
            }
        });
    },

    // Shopping cart list
    list: function (success) {

        czHttp.getJSON('./data/orders.json', {}, function(responseData){
            success(responseData);
        });
    }
};