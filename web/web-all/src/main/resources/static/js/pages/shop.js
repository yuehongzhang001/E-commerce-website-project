//List data loading
$(function () {
    // $.getJSON("../data/shoplist.json",function (data) {
    // $.each(data,function (index,type) {
    // $("#listall").append(
    // "<li>"+ type["name"] + "</li>"
    // );
    // })
    // });
    // $.getJSON("../data/shoplist-data.json", function (data) {
    // $.each(data, function (index, list) {
    // $("#goods-list").append(
    // "<li class='yui3-u-1-4'><div class='list-wrap' ><div class='p-img'><img src='" + list["img"] + "'alt=''></div><div class='price'><strong><em>¥</em> <i>" + list["n-price"] + "</i> </strong></div>"
    // + "<div class='attr'><em>" + list["desc"] + "</em></div><div class='cu'><em><span>Promote</ span>" + list["cu"] + "</em></div>"
    // + "<div class='operate'><a href='success-cart.html' target='blank' class='sui-btn btn-bordered btn-danger'>Add to cart</a>"
    // + "<a href='javascript:void(0);' class='sui-btn btn-bordered'>Comparison</a>"
    // + "<a href='javascript:void(0);' class='sui-btn btn-bordered'>Follow</a>"
    // + "</div></div></li >"
    // );

    // })
    // });
    var lileg = $(".sui-nav").children().length;
    if (lileg <8) {
        $("#li-1").css({"display":"none"});
    }
})

$(document).ready(function () {
    // nav-li hover e
    var num;
    $('.sui-nav>li[id]').hover(function () {
        /*Icon rotate upward*/
        $(this).children().removeClass().addClass('hover-up');
        /*The drop-down box appears*/
        var Obj = $(this).attr('id');
        num = Obj.substring(3, Obj.length);
        $('#box-' + num).slideDown(300);
    }, function () {
        /*Icon rotate down*/
        $(this).children().removeClass().addClass('hover-down');
        /*The drop-down box disappears*/
        $('#box-' + num).hide();
    });
    // hidden-box hover e
    $('.hidden-box').hover(function () {
        /*Keep the icon up*/
        $('#li-' + num).children().removeClass().addClass('hover-up');
        $(this).show();
    }, function () {
        $(this).slideUp(200);
        $('#li-' + num).children().removeClass().addClass('hover-down');
    });
});

$(function () {
    var navH = $("#headnav-fixed").offset().top; //Get the distance to the top
    // Scroll bar event
    $(window).scroll(function () {
        var scroH = $(this).scrollTop(); //Get the sliding distance of the scroll bar
        if (scroH >= navH) {
            $("#headnav-fixed").css({ "position": "fixed", "top": 0,"width":"inherit" ,"border-bottom":"1px solid #B1191A"});
        } else if (scroH <navH) {
            $("#headnav-fixed").css({ "position": "static","border-bottom":0});
        }
    })
})