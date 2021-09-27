$.fn.drag = function(options) {
    var x, drag = this, isMove = false, defaults = {
    };
    var options = $.extend(defaults, options);
    //Add background, text, slider
    var html ='<div class="drag_bg"></div>' +
        '<div class="drag_text" onselectstart="return false;" unselectable="on">drag the slider to verify</div>' +
        '<div class="handler handler_bg"></div>';
    this.append(html);

    var handler = drag.find('.handler');
    var drag_bg = drag.find('.drag_bg');
    var text = drag.find('.drag_text');
    var maxWidth = drag.width()-handler.width(); //Maximum sliding distance

    //The position of the x-axis when the mouse is pressed
    handler.mousedown(function(e) {
        isMove = true;
        x = e.pageX-parseInt(handler.css('left'), 10);
    });

    //When the mouse pointer moves in the context, the movement distance is greater than 0 and less than the maximum distance, and the x-axis position of the slider is equal to the mouse movement distance
    $(document).mousemove(function(e) {
        var _x = e.pageX-x;
        if (isMove) {
            if (_x> 0 && _x <= maxWidth) {
                handler.css({'left': _x});
                drag_bg.css({'width': _x});
            } else if (_x> maxWidth) {//Clear the event when the mouse pointer moves to the maximum distance
                dragOk();
            }
        }
    }).mouseup(function(e) {
        isMove = false;
        var _x = e.pageX-x;
        if (_x <maxWidth) {//When the mouse is released, if the maximum distance position is not reached, the slider returns to the initial position
            handler.css({'left': 0});
            drag_bg.css({'width': 0});
        }
    });

    //Clear the event
    function dragOk() {
        handler.removeClass('handler_bg').addClass('handler_ok_bg');
        text.text('Verification passed');
        drag.css({'color':'#fff'});
        handler.unbind('mousedown');
        $(document).unbind('mousemove');
        $(document).unbind('mouseup');
    }
};