var cartPanelView = {

    cartCellTemplate : "",

    // Initialize
    setup: function (callback) {

        $('.tbar-cart-item').hover(function (){ $(this).find('.p-del').show(); },function(){ $(this).find( '.p-del').hide(); });
        $('.jth-item').hover(function (){ $(this).find('.add-cart-button').show(); },function(){ $(this).find( '.add-cart-button').hide(); });

        // Floating button
        $('.toolbar-tab').hover(function (){
            $(this).find('.tab-text').html($(this).attr("data"));
            $(this).find('.tab-text').addClass("tbar-tab-hover");
            $(this).find('.footer-tab-text').addClass("tbar-tab-footer-hover");
            $(this).addClass("tbar-tab-selected");
        },function(){
            $(this).find('.tab-text').removeClass("tbar-tab-hover");
            $(this).find('.footer-tab-text').removeClass("tbar-tab-footer-hover");
            $(this).removeClass("tbar-tab-selected");
        });

        cartPanelView.cartCellTemplate = $("#tbar-cart-item-template").html();

        callback();
    },

    // Click on the sidebar button
    tabItemClick: function (typeName) {
        if($('.toolbar-wrap').hasClass('toolbar-open')){
            cartPanelView.tbar_panel_close(typeName);
        }else{
            cartPanelView.tbar_panel_show(typeName);
        }
    },

// Panel display
    tbar_panel_show: function(panelName) {

// Floating button
        $('.toolbar-tab').removeClass('tbar-tab-click-selected');
        $('.tbar-tab-'+panelName).addClass('tbar-tab-click-selected');
        $('.tbar-tab-'+panelName).find('.tab-text').remove();

// Panel content
        $('.toolbar-panel').css('visibility','hidden');
        $('.tbar-panel-'+panelName).css({'visibility':"visible","z-index":"1"});

// Panel display
        $('.toolbar-wrap').addClass('toolbar-open');
    },

// close the panel
    tbar_panel_close: function(panelName) {

        if($('.tbar-tab-'+panelName).find('.tab-text').length> 0){
            $('.toolbar-tab').each(function(i){
                var tagValue = $(this).attr("tag");
                if((tagValue != panelName)&&(tagValue != undefined)){
                    var info = "<em class='tab-text'>"+$(this).attr('data')+"</em>";
                    $(this).append(info);
                    $(this).removeClass('tbar-tab-click-selected');
                    $('.tbar-panel-'+$(this).attr('tag')).css({'visibility':"hidden","z-index":"-1"});
                }
            });
            $('.tbar-tab-'+panelName).addClass('tbar-tab-click-selected');
            $('.tbar-tab-'+panelName).find('.tab-text').remove();
            $('.tbar-panel-'+panelName).css({'visibility':"visible","z-index":"1"});
        }else{
            $('.toolbar-wrap').removeClass('toolbar-open');
            var info = "<em class='tab-text'>"+$('.tbar-tab-'+panelName).attr("data")+"</em>";
            $('.tbar-tab-'+panelName).append(info);
            $('.tbar-tab-'+panelName).removeClass('tbar-tab-click-selected');
            $('.tbar-panel-'+panelName).css({'visibility':"hidden","z-index":"-1"});
        }
    },

    // Fill in the shopping cart data
    fillCart: function(dataJSON) {
        // Shopping cart list
        var rowsHtml = "";
        for(var i = 0; i <dataJSON.orders.length; i++){
            var shops = dataJSON.orders[i];
            for(var x = 0; x <shops.orderItems.length; x++){
                var it = shops.orderItems[x];
                rowsHtml += String.format(
                    cartPanelView.cartCellTemplate,
                    it.pid,
                    it.title,
                    it.image,
                    it.unitPrice*it.quantity,
                    it.quantity
                );
            }
        }
        $("#cart-list").html(rowsHtml);
        // Shopping cart subtotal
        $("#cart-number").html(dataJSON.totalQuantity);
        $("#cart-sum").html(String.format("¥{0}",dataJSON.totalPrices));
        // sidebar number of items purchased
        $("#tab-sub-cart-count").html(dataJSON.totalQuantity);
    }
};

$(function() {

    // Initial shopping cart sidebar
    cartPanelView.setup(function(){
        // Load the shopping cart list
        cartPanelView.fillCart(orderData);
    });

});



var orderData = {
    "totalQuantity":2,
    "totalPrices":8998,
    "orders":[
        {
            "shop":"Tuanlong Official Flagship Store",
            "orderItems":[
                {
                    "pid":"10803521657",
                    "image":"http://img10.360buyimg.com/cms/s80x80_jfs/t2941/284/2460981288/184644/79d9d20b/57bd9890N73efbc30.jpg",
                    "title":"Turion Destroyer DC desktop processor GTX950M 4G large video memory 15.6-inch gaming laptop 1",
                    "color":"black",
                    "size":"I3-6100/8G/500G+128G",
                    "unitPrice":4499,
                    "quantity":1,
                    "gift":[
                        {
                            "pid":"10633272618",
                            "title":"[Hyunlong] Wired Gaming Mouse Exclusively for Notebook Computers Dragon Soul Mecha"
                        },
                        {
                            "pid":"10629228032",
                            "title":"[Turning Dragon] Exclusive gaming laptop mouse pad black thickened and softer black"
                        }
                    ]
                },
                {
                    "pid":"10803523232",
                    "image":"http://img10.360buyimg.com/cms/s80x80_jfs/t2941/284/2460981288/184644/79d9d20b/57bd9890N73efbc30.jpg",
                    "title":"Turion Destroyer DC desktop processor GTX950M 4G large video memory 15.6-inch gaming laptop 2",
                    "color":"black",
                    "size":"I3-6100/8G/500G+128G",
                    "unitPrice":4499,
                    "quantity":1,
                    "gift":[
                        {
                            "pid":"10633272618",
                            "title":"[Hyunlong] Wired Gaming Mouse Exclusively for Notebook Computers Dragon Soul Mecha"
                        },
                        {
                            "pid":"10629228032",
                            "title":"[Turning Dragon] Exclusive gaming laptop mouse pad black thickened and softer black"
                        }
                    ]
                }
      ]
    }
  ]
};