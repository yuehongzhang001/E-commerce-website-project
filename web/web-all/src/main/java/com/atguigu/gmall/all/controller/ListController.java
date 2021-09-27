package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Yuehong Zhang
 */
@Controller
public class ListController {

    @Autowired
    private ListFeignClient listFeignClient;


    @RequestMapping("list.html")
    public String listPage(SearchParam searchParam, Model model){
        // call service-list-client remotely;
        // If only one parameter is passed in the remote call, then call Feign remotely! You can directly use this parameter name to receive!
        // If multiple parameters are passed in the remote call, Long category3Id String trademark; No! Only use Json transmission

        Result<Map> result = listFeignClient.list(searchParam);
        // SearchResponseVo lists = listFeignClient.lists(searchParam);
        // ${urlParam} should store a variable in the background!
        String urlParam = makeUrlParam(searchParam);
        // Pass the brand data passed by the user into the method
        String trademarkParam = makeTrademarkParam(searchParam.getTrademark());

        // ${propsParamList} This breadcrumb of platform properties! Multiple platform attribute breadcrumbs: a collection can be used to represent List<>
        List<Map> propsParamList = makeProps(searchParam.getProps());

        // orderMap needs back-end storage!
        Map<String,Object> orderMap = this.dealOrder(searchParam.getOrder());
        model.addAttribute("orderMap",orderMap);
        // Brand breadcrumbs trademarkParam Brand: Brand name can be seen as a string! Brand: Xiaomi
        model.addAttribute("propsParamList",propsParamList);
        model.addAttribute("trademarkParam",trademarkParam);
        model.addAttribute("searchParam",searchParam);
        model.addAttribute("urlParam",urlParam);
        model.addAllAttributes(result.getData());
        return "list/index";
    }

    // Get user sorting rules order=1:asc order=1:desc
    private Map<String, Object> dealOrder(String order) {
        Map<String, Object> map = new HashMap<>();
        //  judge
        if (!StringUtils.isEmpty(order)){
            String[] split = order.split(":");
            if (split!=null && split.length==2){
                map.put("type",split[0]);
                map.put("sort",split[1]);
                return map;
            }
        }
        map.put("type","1");
        map.put("sort","asc");
        return map;
    }

    // can pass multiple
    // &props=2:6.25-6.34 inches: screen size &props=4:64GB: body storage
    private List<Map> makeProps(String[] props) {
        List<Map> list = new ArrayList<>();
        //  judge
        if(props!=null && props.length>0){
            // loop traversal
            for (String prop: props) {
                // prop = 2: 6.25-6.34 inches: screen size
                // Follow the page requirements: create a map object
                // <a th:href="@{${#strings.replace(urlParam+'&order='+searchParam.order,'props='+prop.attrId+':'+prop.attrValue+':'+prop.attrName ,'')}}">×</a>
                // Split the prop
                String[] split = prop.split(":");
                if (split!=null && split.length==3){
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("attrId",split[0]);
                    hashMap.put("attrValue",split[1]);
                    hashMap.put("attrName",split[2]);
                    // Add the platform attributes and attribute values ​​clicked by the user to the map
                    list.add(hashMap);
                }
            }
        }
        return list;
    }

    /**
     * Get branded breadcrumbs
     * @param trademark
     * @return
     */
    private String makeTrademarkParam(String trademark) {
        //  judge
        if (!StringUtils.isEmpty(trademark)){
            // trademark=2: Huawei finally obtains the brand name
            String[] split = trademark.split(":");
            if (split!=null && split.length==2){
                return "Brand:" + split[1];
            }
        }
        return null;
    }

    /**
     * Get the request parameters in the request path
     * @param searchParam
     * @return
     */
    private String makeUrlParam(SearchParam searchParam) {
        // Declare an object
        StringBuilder sb = new StringBuilder();

        // Determine whether the user passed the key search:
        // http://list.gmall.com/list.html?keyword=Mobile
        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            sb.append("keyword=").append(searchParam.getKeyword());
        }
        // http://list.gmall.com/list.html?category3Id=61
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())){
            sb.append("category3Id=").append(searchParam.getCategory3Id());
        }
        // http://list.gmall.com/list.html?category2Id=13
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())){
            sb.append("category2Id=").append(searchParam.getCategory2Id());
        }
        // http://list.gmall.com/list.html?category1Id=2
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())){
            sb.append("category1Id=").append(searchParam.getCategory1Id());
        }

        // Determine whether the user has passed the brand search
        // http://list.gmall.com/list.html?category3Id=61&trademark=4: Xiaomi
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)){
            // Make splicing
            if (sb.length()>0){
                sb.append("&trademark=").append(trademark);
            }
        }
        // Determine whether the user is searching through platform attributes:
        // http://list.gmall.com/list.html?category3Id=61&trademark=4: Xiaomi&props=2342: 4.7 inches: screen&props=2345:1:1
        String[] props = searchParam.getProps();
        if (props!=null && props.length>0){
            // loop traversal
            for (String prop: props) {
                if (sb.length()>0){
                    sb.append("&props=").append(prop);
                }
            }
        }

        // sb list.html following parameter list
        return "list.html?"+sb.toString();
    }

}
