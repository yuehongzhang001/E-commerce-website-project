function getQueryString(name){
    let reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
    let r = window.location.search.substr(1).match(reg);
    if(r!=null)return unescape(r[2]); return null;
}


// Extension of Date, convert Date to String in specified format
// Month (M), day (d), hour (h), minute (m), second (s), quarter (q) can use 1-2 placeholders,
// year (y) can use 1-4 placeholders, milliseconds (S) can only use 1 placeholder (is 1-3 digits)
// example:
// (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423
// (new Date()).Format("yyyy-M-d h:m:s.S") ==> 2006-7-2 8:9:4.18
Date.prototype.Format = function (fmt) {//author: meizz
    var o = {
        "M+": this.getMonth() + 1, //month
        "d+": this.getDate(), //day
        "h+": this.getHours(), //hours
        "m+": this.getMinutes(), //Minutes
        "s+": this.getSeconds(), //second
        "q+": Math.floor((this.getMonth() + 3) / 3), //Quarter
        "S": this.getMilliseconds() //Milliseconds
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4-RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1)? (o[k]): ( ("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
}