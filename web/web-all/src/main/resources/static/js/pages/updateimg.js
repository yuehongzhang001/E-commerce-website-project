window.onload = function () {
	var input = document.getElementById("upgteimg");
	var showui = document.getElementById("showui");
	var upimg = document.getElementById("image-upload");
	var result;
	var dataArr = []; // save the result of the selected picture (file name and base64 data)
	var fd; //Send request in FormData mode
	var showinput = document.getElementById("showinput");
	var oSubmit = document.getElementById("submit");
	var dateli, dateinput;

	function randomString(len) {
		len = len || 32;
		var $chars ='ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678'; /****The confusing characters oOLl,9gq,Vv,Uu,I1****/ 　　 are removed by default
		var maxPos = $chars.length;
		var pwd ='';
		for (i = 0; i <len; i++) {
			pwd += $chars.charAt(Math.floor(Math.random() * maxPos));
		}
		return pwd;
	}
	console.log()
	if (typeof FileReader ==='undefined') {
		alert("Sorry, your browser does not support FileReader");
		input.setAttribute('disabled','disabled');
	} else {
		input.addEventListener('change', readFile, false);
	}

	function readFile() {
		fd = new FormData();
		var iLen = this.files.length;
		var index = 0;
		var currentReViewImgIndex = 0;
		for (var i = 0; i <iLen; i++) {
			if (!input['value'].match(/.jpg|.gif|.png|.jpeg|.bmp/i)) {　　 //Judging the upload file format
				return alert("The format of the uploaded picture is incorrect, please select again");
			}
			var reader = new FileReader();
			reader.index = i;
			fd.append(i, this.files[i]);
			reader.readAsDataURL(this.files[i]); //Convert to base64
			reader.fileName = this.files[i].name;
			reader.files = this.files[i];
			reader.onload = function (e) {
				var imgMsg = {
					name: randomString(5), //Get the file name
					base64: this.result, //After the reader.readAsDataURL method is executed, the base64 data is stored in reader.result
				}
				dataArr.push(imgMsg);
				for (var j = 0; j <dataArr.length; j++) {
					currentReViewImgIndex = j
				}
				result ='<img id="img' + currentReViewImgIndex + randomString(1) + randomString(2) + randomString(5) +'" class="showimg" src="' + this.result +'" />';
				var li = document.createElement('li');
				li.innerHTML = result;
				showui.appendChild(li);
				index++;
				var lilength = document.querySelectorAll('ul#showui li').length;
				var tip ='Uploaded' + lilength + 'pieces, can be uploaded' + (9-lilength) +'张';

				var span = document.createElement('span');

//span.html(tip);
// span.innerHTML = tip;
				upimg.appendChild(span);
// p1.html(text1.val());
				console.log(tip)


			}
		}








	}

	function onclickimg() {
		var dataArrlist = dataArr.length;
		var lilength = document.querySelectorAll('ul#showui li');
		for (var i = 0; i <lilength.length; i++) {
			lilength[i].getElementsByTagName('img')[0].onclick = function (num) {
				return function () {
					if (num == 0) {} else {
						var list = 0;
						for (var j = 0; j <dataArr.length; j++) {
							list = j
						}
						var up = num-1;
						dataArr.splice(up, 0, dataArr[num]);
						dataArr.splice(num + 1, 1)
						var lists = $("ul#showui li").length;
						for (var j = 0; j <lists; j++) {
							var usid = $("ul#showui li")[j].getElementsByTagName('img')[3];
							$("#" + usid.id + "").attr("src", dataArr[j].base64)
						}
					}
				}
			}(i)
//Delete icon
			lilength[i].getElementsByTagName('img')[1].onclick = function (num) {
				return function () {
					if (dataArr.length == 1) {
						dataArr = [];
						$("ul#showui").html("")
					} else {
						$("ul#showui li:eq(" + num + ")").remove()
						dataArr.splice(num, 1)
					}

				}
			}(i)
//Right arrow icon
			lilength[i].getElementsByTagName('img')[2].onclick = function (num) {
				return function () {
					var list = 0;
					for (var j = 0; j <dataArr.length; j++) {
						list = j
					}
					var datalist = list + 1;
					dataArr.splice(datalist, 0, dataArr[num]);
					dataArr.splice(num, 1)
					var lists = $("ul#showui li").length;
					for (var j = 0; j <lists; j++) {
						var usid = $("ul#showui li")[j].getElementsByTagName('img')[3];
						$("#" + usid.id + "").attr("src", dataArr[j].base64)
					}

				}
			}(i)

		}
	}
	showui.addEventListener("click", function () {
		onclickimg();
	}, true)

	function send() {
		for (var j = 0; j <dataArr.length; j++) {
			console.log(dataArr[j].name)
		}
	}



}