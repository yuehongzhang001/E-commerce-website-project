var hm = {

    /**
     * default allocation
     */
    default: {
        // log collection address (need to configure)
        // sendUrl: "http://47.93.148.192:7070/log.gif",
        sendUrl: "https://gmall.goho.co/applog",
        // sendUrl: "http://gmalllog.viphk.ngrok.org/applog",

        // Expose the custom DOM element class name of the buried point
        displayClass: "hm-display-statistics",
        // Expose the attributes of the DOM element of the buried point
        displayAttr: "hm-display",


        // The class name of the custom DOM element of the behavior buried point
        actionClass: "hm-action-statistics",
        // Attributes of DOM elements that act as buried points
        actionAttr: "hm-action",
    },

    /**
     * Page data
     */
    data: {
        //Initialize page data
        pageInitData: null,

        // All data collections exposed
        displaysData: [],

        // All data collection of event behavior
        actionsData: []
    },

    /**
     * Public information
     */
    common: {
        mid:'', // device id
        uid:'', // unique user ID
        vc: '2.0', //Program version number
        ch:'web', // channel number, which channel the application came from
        os:'', // system version
        ar:'Beijing', // area
        md: navigator.userAgent, // phone model
        ba: navigator.appName, // mobile phone brand
        is_new: '0'//Is it a new user
    },
    // common: {
    // browser: navigator.userAgent, // browser information
    // title: encodeURIComponent(document.title), // page title
    // resolve: window.screen.width +'x' + window.screen.height +'(px)', // device resolution
    // // deviceType: null, // device type
    // language: navigator.language, // client language
    // mid:'', // device id
    // userId:'', // unique user ID
    // url: location.href, // page link
    // referrer: document.referrer ||''//The URL of the source page of the current page
    // },

    /**
     * Enter the page to initialize
     */
    init: function () {
        let that = this

        // Initialize the device id
        that.common.mid = that.util.getMid()
        // Initialize user id, currently logged in user id
        that.common.uid = auth.getUserInfo() != null? auth.getUserInfo().userId:''
        that.common.os = that.util.getOsInfo()
        that.common.is_new = that.util.getIsNew()

        // Record initialization page information
        that.data.pageInitData = {
            prePath: that.getPrePath(), // Get the id of the previous page
            time: new Date().getTime() // Get the time of entering the page
        };

        // User event behavior is embedded in monitoring, because some pages use vue asynchronous rendering, due to the problem of page loading sequence, to ensure that the monitoring content is fully monitored, so the monitoring is delayed
        setTimeout('hm.actionStatistics()',1000);

        // The user visualization area shows the buried point monitoring, because some pages use vue asynchronous rendering, due to the page loading sequence problem, to ensure that the monitoring content is fully monitored, so the monitoring is delayed
        setTimeout('hm.displayStatistics()',1000);
    },

    /**
     * Get the id of the previous page
     * @returns {*|string}
     */
    getPrePath: function() {
        let that = this
        var prePath = that.util.getCookie('prePath')
        if(prePath =='' || prePath =='undefined') {
            prePath = ""
        }
        // Record the current page id
        that.util.setCookie('prePath', window.page.page_id, 1*60*1000)
        return prePath
    },

    /**
     * User visualization area display buried point monitoring
     */
    displayStatistics: function () {
        let that = this
        let observer = new IntersectionObserver(function (entries) {
            entries.forEach((entry, index) => {
                // This logic is triggered when every observed component enters the window
                if (entry.isIntersecting) {
                    // Add the component data that enters the viewport into the data object to be reported
                    that.data.displaysData.push(JSON.parse(entry.target.attributes[that.default.displayAttr].value.replaceAll("'", "\"")))
                    // Stop observing the components entering the viewport
                    observer.unobserve(entry.target)
                }
            })
        }, {
            root: null, // The default root node is the viewport
            rootMargin: '0px',
            threshold: 1 // Observe when you enter the viewport. This threshold is between 0 and 1.
        })

        var displayItem = document.querySelectorAll('.' + that.default.displayClass)
        displayItem.forEach(item => {
            observer.observe(item) // Observe every area that enters the viewport
        })
    },

    /**
     * User event behavior monitoring
     */
    actionStatistics: function () {
        let that = this
        $("." + that.default.actionClass).each(function(){
            $(this).click(function(){
                var action = JSON.parse($(this).attr(that.default.actionAttr).replaceAll("'", "\""))
                action.ts = new Date().getTime()

                that.data.actionsData.push(action)
            });
        });
    },

    /**
     * Page information
     */
    getPageData: function() {
        var that = this
        var page = {
            "page_id": window.page.page_id,
            "last_page_id": that.data.pageInitData.prePath,
            //"item": window.page.item,
            "item_type": window.page.page_item_type,
            "item": window.page.page_item,
            "sourceType": window.page.sourceType,
            "during_time": new Date().getTime() - that.data.pageInitData.time
        };
        return page
    },

    /**
     * 离开页面上报数据  r
     */
    postData: function () {
        //上报数据
        var data = {
            common: this.common,
            page: this.getPageData(),
            displays: this.data.displaysData,
            actions: this.data.actionsData,
            ts: this.data.pageInitData.time
        };

        // $.ajax({
        //     type: "POST",
        //     url: this.default.sendUrl+"?v="+Math.random(),
        //     contentType: "application/json; charset=utf-8",
        //     data: JSON.stringify(data),
        //     dataType: "json",
        //     success: function (message) {
        //         console.log('result:'+message);
        //
        //     },
        //     error: function (message) {
        //         console.log('error:'+message);
        //     }
        // });
        console.log(JSON.stringify(data))
        //图片上报
        new Image().src = this.default.sendUrl+"?param="+JSON.stringify(data)+"&v="+Math.random();
        new Image().src = "http://47.93.148.192:7070/log.gif?param="+JSON.stringify(data)+"&v="+Math.random();
    },

    /**
     * 工具函数
     */
    util: {
        setCookie: function (name, value, time) {
            var date = new Date()
            date.setTime(date.getTime() + time)
            $.cookie(name, encodeURIComponent(value), {domain: 'gmall.com', expires: date, path: '/'})
        },

        getCookie: function (name) {
            return decodeURIComponent($.cookie(name))
        },

        /**
         * 获取设备id
         * @returns {string|*|string}
         */
        getMid: function () {
            var mid = this.getCookie('MID');
            if(mid != '' && mid != 'undefined') {
                return mid;
            } else {
                var d = new Date().getTime();
                if (window.performance && typeof window.performance.now === "function") {
                    d += performance.now(); //use high-precision timer if available
                }
                var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
                    var r = (d + Math.random() * 16) % 16 | 0;
                    d = Math.floor(d / 16);
                    return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
                });
                this.setCookie('MID', uuid, 365*24*60*60*1000);
                return uuid;
            }
        },

        /**
         * 是否新用户
         * @returns {string|*|string}
         */
        getIsNew: function () {
            var initDate = this.getCookie('INIT_DATE');
            if(initDate != '' && initDate != 'undefined') {
                var nowDate = this.getNowFormatDate()
                if(initDate != nowDate) {
                    return "0";
                }
            } else {
                initDate = this.getNowFormatDate();
                this.setCookie('INIT_DATE', initDate, 365*24*60*60*1000);
            }
            return "1";
        },

        getNowFormatDate: function() {
            var date = new Date();
            var seperator1 = "-";
            var year = date.getFullYear();
            var month = date.getMonth() + 1;
            var strDate = date.getDate();
            if (month >= 1 && month <= 9) {
                month = "0" + month;
            }
            if (strDate >= 0 && strDate <= 9) {
                strDate = "0" + strDate;
            }
            var currentdate = year + seperator1 + month + seperator1 + strDate;
            return currentdate;
        },

        // Get operating system information
        getOsInfo: function () {
            var userAgent = navigator.userAgent.toLowerCase();
            var name ='Unknown';
            var version ='Unknown';
            if (userAgent.indexOf('win')> -1) {
                name ='Windows';
                if (userAgent.indexOf('windows nt 5.0')> -1) {
                    version ='Windows 2000';
                } else if (userAgent.indexOf('windows nt 5.1')> -1 || userAgent.indexOf('windows nt 5.2')> -1) {
                    version ='Windows XP';
                } else if (userAgent.indexOf('windows nt 6.0')> -1) {
                    version ='Windows Vista';
                } else if (userAgent.indexOf('windows nt 6.1')> -1 || userAgent.indexOf('windows 7')> -1) {
                    version ='Windows 7';
                } else if (userAgent.indexOf('windows nt 6.2')> -1 || userAgent.indexOf('windows 8')> -1) {
                    version ='Windows 8';
                } else if (userAgent.indexOf('windows nt 6.3')> -1) {
                    version ='Windows 8.1';
                } else if (userAgent.indexOf('windows nt 6.2')> -1 || userAgent.indexOf('windows nt 10.0')> -1) {
                    version ='Windows 10';
                } else {
                    version ='Unknown';
                }
            } else if (userAgent.indexOf('iphone')> -1) {
                name ='Iphone';
            } else if (userAgent.indexOf('mac')> -1) {
                name ='Mac';
            } else if (userAgent.indexOf('x11')> -1 || userAgent.indexOf('unix')> -1 || userAgent.indexOf('sunname')> -1 || userAgent.indexOf('bsd' )> -1) {
                name ='Unix';
            } else if (userAgent.indexOf('linux')> -1) {
                if (userAgent.indexOf('android')> -1) {
                    name ='Android';
                } else {
                    name ='Linux';
                }
            } else {
                name ='Unknown';
            }
            return version;
        }
    }
};

(function(){
    //Enter the page initialization
    hm.init();

    // window.isRun = 0
    // //Leave the page to report data
    // window.onbeforeunload = function () {
    // if(window.isRun == 0) {
    // window.isRun = 1
    // hm.postData();
    //}
    //}
    //
    // //Leave the page to report data
    // window.onpagehide=function(){
    // if(window.isRun == 0) {
    // window.isRun = 1
    // hm.postData();
    //}
    // };
})();

