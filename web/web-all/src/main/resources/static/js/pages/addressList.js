$(function(){
// select element
	var $selectorProvince = $("#province");
	var $selectorCity = $("#city");
	var $selectorDistrict = $("#district");

// The default value of the region, obtained through the default-data of select
	var defaultProvince = $selectorProvince.attr('data-default');
	var defaultCity = $selectorCity.attr('data-default');
	var defaultDistrict = $selectorDistrict.attr('data-default');

	if(!defaultProvince) defaultProvince = currentProvince;
	if(!defaultCity) defaultCity = currentCity;

// Initialize
	initSelector($selectorProvince,provinces);
	initSelector($selectorCity,getCities(defaultProvince));
	initSelector($selectorDistrict,getDistricts(defaultProvince,defaultCity));

// select province
	$selectorProvince.change(function(){
		currentProvince = $(this).val();
		initSelector($selectorCity,getCities(currentProvince));
		$selectorCity.trigger('change');
	})

	// select the city
	$selectorCity.change(function(){
		currentCity = $(this).val();
		initSelector($selectorDistrict,getDistricts(currentProvince,currentCity));
	})

// Initialize the selection box, where data represents an array containing all selected items
	function initSelector(selectObj,data){
// The empty data directly hides the select element
		if(data == ""){
			selectObj.hide();
			selectObj.html("");
		}else{
			selectObj.show();
		}

		var str = "";
		var selected = selectObj.attr('data-default');
		for (var i = 0; i <data.length; i++) {
			var _data = data[i];
			if(_data === selected){
				str +='<option selected="selected" value="'+_data+'">'+_data+'</option>';
			}else{
				str +='<option value="'+_data+'">'+_data+'</option>';
			}
		}
		selectObj.html(str);
	}
})