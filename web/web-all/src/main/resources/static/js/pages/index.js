// window.onload = function() {
// cartListController.setup();
//}

/*Categories*/
$(function() {
	$('.all-sort-list2> .item').hover(function() {
//The height of the parent category list container

		$(this).addClass('hover');
		$(this).children('.item-list').css('display','block');
	}, function() {
		$(this).removeClass('hover');
		$(this).children('.item-list').css('display','none');
	});

	$('.item> .item-list> .close').click(function() {
		$(this).parent().parent().removeClass('hover');
		$(this).parent().hide();
	});
});