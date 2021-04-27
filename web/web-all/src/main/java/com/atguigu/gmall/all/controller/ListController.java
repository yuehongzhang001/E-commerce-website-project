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
 * @author mqx
 */
@Controller
public class ListController {

    @Autowired
    private ListFeignClient listFeignClient;

    //  http://list.gmall.com/list.html?category3Id=61
    //  http://list.gmall.com/list.html?category3Id=61&trademark=5:小米
    @RequestMapping("list.html")
    public String listPage(SearchParam searchParam, Model model){
        // 远程调用service-list-client;
        //  远程调用如果只传递一个参数时，那么在远程调用Feign 上 ！ 可以直接用这个参数名去接收！
        //  远程调用如果传递多个参数，Long category3Id  String trademark; 不能！只能使用Json 传输

        Result<Map> result = listFeignClient.list(searchParam);
        //        SearchResponseVo lists = listFeignClient.lists(searchParam);
        //  ${urlParam} 后台应该存储一个变量！
        String urlParam = makeUrlParam(searchParam);
        //  将用户传递的品牌数据传递到方法中
        String trademarkParam = makeTrademarkParam(searchParam.getTrademark());

        //  ${propsParamList} 这个表示平台属性的面包屑！ 多个平台属性面包屑：可以用集合表示List<>
        List<Map> propsParamList = makeProps(searchParam.getProps());

        //  orderMap 需要后台存储！
        Map<String,Object> orderMap = this.dealOrder(searchParam.getOrder());
        model.addAttribute("orderMap",orderMap);
        //  品牌面包屑 trademarkParam  品牌：品牌名 可以看做一个字符串！品牌:小米
        model.addAttribute("propsParamList",propsParamList);
        model.addAttribute("trademarkParam",trademarkParam);
        model.addAttribute("searchParam",searchParam);
        model.addAttribute("urlParam",urlParam);
        model.addAllAttributes(result.getData());
        return "list/index";
    }

    //  获取用户排序规则 order=1:asc order=1:desc
    private Map<String, Object> dealOrder(String order) {
        Map<String, Object> map = new HashMap<>();
        //  判断
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

    //  可以传递多个
    //  &props=2:6.25-6.34英寸:屏幕尺寸&props=4:64GB:机身存储
    private List<Map> makeProps(String[] props) {
        List<Map> list = new ArrayList<>();
        //  判断
        if(props!=null && props.length>0){
            //  循环遍历
            for (String prop : props) {
                //  prop = 2:6.25-6.34英寸:屏幕尺寸
                //  跟页面需求：创建一个map 对象
                //  <a th:href="@{${#strings.replace(urlParam+'&order='+searchParam.order,'props='+prop.attrId+':'+prop.attrValue+':'+prop.attrName,'')}}">×</a>
                //  将prop 进行分割
                String[] split = prop.split(":");
                if (split!=null && split.length==3){
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("attrId",split[0]);
                    hashMap.put("attrValue",split[1]);
                    hashMap.put("attrName",split[2]);
                    //  将用户点击的平台属性，属性值添加到map 中
                    list.add(hashMap);
                }
            }
        }
        return list;
    }

    /**
     * 获取品牌面包屑
     * @param trademark
     * @return
     */
    private String makeTrademarkParam(String trademark) {
        //  判断
        if (!StringUtils.isEmpty(trademark)){
            //  trademark=2:华为 最终获取到品牌名称
            String[] split = trademark.split(":");
            if (split!=null && split.length==2){
                 return "品牌:" + split[1];
            }
        }
        return null;
    }

    /**
     * 获取到请求路径中的请求参数
     * @param searchParam
     * @return
     */
    private String makeUrlParam(SearchParam searchParam) {
        //  声明一个对象
        StringBuilder sb = new StringBuilder();

        //  判断用户是否通过关键检索：
        //  http://list.gmall.com/list.html?keyword=手机
        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            sb.append("keyword=").append(searchParam.getKeyword());
        }
        //  http://list.gmall.com/list.html?category3Id=61
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())){
            sb.append("category3Id=").append(searchParam.getCategory3Id());
        }
        //  http://list.gmall.com/list.html?category2Id=13
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())){
            sb.append("category2Id=").append(searchParam.getCategory2Id());
        }
        //  http://list.gmall.com/list.html?category1Id=2
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())){
            sb.append("category1Id=").append(searchParam.getCategory1Id());
        }

        //  判断用户是否通过品牌检索
        //  http://list.gmall.com/list.html?category3Id=61&trademark=4:小米
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)){
            //  进行拼接
            if (sb.length()>0){
                sb.append("&trademark=").append(trademark);
            }
        }
        //  判断用户是否通过平台属性进行检索：
        //  http://list.gmall.com/list.html?category3Id=61&trademark=4:小米&props=2342:4.7英寸:屏幕&props=2345:1:1
        String[] props = searchParam.getProps();
        if (props!=null && props.length>0){
            //  循环遍历
            for (String prop : props) {
                if (sb.length()>0){
                    sb.append("&props=").append(prop);
                }
            }
        }

        //  sb list.html 后面的参数列表
        return "list.html?"+sb.toString();
    }

}
