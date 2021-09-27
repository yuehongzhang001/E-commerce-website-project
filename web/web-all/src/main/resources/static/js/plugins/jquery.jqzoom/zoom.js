/*Magnifying glass effect*/
//=====================Global function=======================
//Tab control function
function tabs(tabId, tabNum){
//Set the switching style after clicking
	$(tabId + ".tab li").removeClass("curr");
	$(tabId + ".tab li").eq(tabNum).addClass("curr");
//Decide the display content according to the parameters
	$(tabId + ".tabcon").hide();
	$(tabId + ".tabcon").eq(tabNum).show();
}
//=====================Global function=======================

//==================Picture detail page function=====================
//Hover over the preview picture function
function preview(img){
	$("#preview .jqzoom img").attr("src",$(img).attr("src"));
	$("#preview .jqzoom img").attr("jqimg",$(img).attr("bimg"));
}

//Picture magnifying glass effect
$(function(){
	$(".jqzoom").jqueryzoom({xzoom:400,yzoom:400});
});

//Image preview small image moving effect, triggered when the page loads
$(function(){
	var tempLength = 0; //Temporary variable, the length of the current move
	var viewNum = 5; //Set the number of pictures displayed each time
	var moveNum = 2; //The number of each move
	var moveTime = 300; //Movement speed, milliseconds
	var scrollDiv = $(".spec-scroll .items ul"); //Container for moving animation
	var scrollItems = $(".spec-scroll .items ul li"); //Move the collection in the container
	var moveLength = scrollItems.eq(0).width() * moveNum; //Calculate the length of each move
	var countLength = (scrollItems.length-viewNum) * scrollItems.eq(0).width(); //Calculate the total length, total number * single length

//Next
	$(".spec-scroll .next").bind("click",function(){
		if(tempLength <countLength){
			if((countLength-tempLength)> moveLength){
				scrollDiv.animate({left:"-=" + moveLength + "px"}, moveTime);
				tempLength += moveLength;
			}else{
				scrollDiv.animate({left:"-=" + (countLength-tempLength) + "px"}, moveTime);
				tempLength += (countLength-tempLength);
			}
		}
	});
	//Previous
	$(".spec-scroll .prev").bind("click",function(){
		if(tempLength> 0){
			if(tempLength> moveLength){
				scrollDiv.animate({left: "+=" + moveLength + "px"}, moveTime);
				tempLength -= moveLength;
			}else{
				scrollDiv.animate({left: "+=" + tempLength + "px"}, moveTime);
				tempLength = 0;
			}
		}
	});
});
//==================Picture detail page function=====================