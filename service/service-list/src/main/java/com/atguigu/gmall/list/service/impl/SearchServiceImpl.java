package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.SearchService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import lombok.SneakyThrows;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author mqx
 */
@Service
public class SearchServiceImpl implements SearchService {

    //  服务层调用 客户端！  GoodsRepository 自定义的数据接口！ 继承 ElasticsearchRepository<T, ID> 当前这个类数据接口就具有了CRUD 方法！
    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public void upperGoods(Long skuId) {
        //  声明一个Goods 对象
        Goods goods = new Goods();

        //  异步编排！
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        if (skuInfo!=null){
            goods.setId(skuId);
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());
            goods.setTitle(skuInfo.getSkuName());
            goods.setPrice(skuInfo.getPrice().doubleValue());
            goods.setCreateTime(new Date());

            //  赋值品牌数据
            BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
            goods.setTmId(skuInfo.getTmId());
            goods.setTmName(trademark.getTmName());
            goods.setTmLogoUrl(trademark.getLogoUrl());

            //  赋值分类数据：
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            goods.setCategory1Id(categoryView.getCategory1Id());
            goods.setCategory2Id(categoryView.getCategory2Id());
            goods.setCategory3Id(categoryView.getCategory3Id());
            goods.setCategory1Name(categoryView.getCategory1Name());
            goods.setCategory2Name(categoryView.getCategory2Name());
            goods.setCategory3Name(categoryView.getCategory3Name());

            //  赋值平台属性集0
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            //  Function R apply(T t)
            //  Stream() 流式编程
            List<SearchAttr> searchAttrList = attrList.stream().map(baseAttrInfo -> {
                //  创建一个对象
                SearchAttr searchAttr = new SearchAttr();
                searchAttr.setAttrId(baseAttrInfo.getId());
                searchAttr.setAttrName(baseAttrInfo.getAttrName());
                searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
                return searchAttr;
            }).collect(Collectors.toList());

            goods.setAttrs(searchAttrList);
        }

        goodsRepository.save(goods);
    }

    @Override
    public void lowerGoods(Long skuId) {
        //  根据id 删除
        goodsRepository.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {
        //  借助redis 使用redis 必须先考虑啥? 数据类型！Zset ZINCRBY salary 2000 tom   # tom 加薪啦！  key起名！ 缓存常见的三种问题！
        String hotScoreKey = "hotScore"; // key=hotScore score 递增的数据  skuId:45

        Double count = redisTemplate.opsForZSet().incrementScore(hotScoreKey, "skuId:" + skuId, 1);
        //  es 中 hotScore 从缓存累加的结果！
        if (count%10==0){
            //   更新一次es！
            Optional<Goods> optional = this.goodsRepository.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(count.longValue());
            //  保存！
            this.goodsRepository.save(goods);
        }
    }

    @SneakyThrows
    @Override
    public SearchResponseVo search(SearchParam searchParam) {
        /*
        1.  根据用户的检索条件生成对应的dsl语句 {方法}
        2.  执行dsl 语句并获取到结果集{如何操作es，引入客户端}
        3.  将查询出来的结果集进行封装{方法}
         */
        SearchRequest searchRequest = this.buildQueryDsl(searchParam);
        //  调用search 查询数据得到结果集
        SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //  将查询出来的结果集进行封装
        //  total总记录数在parseSearchResult 去赋值！
        SearchResponseVo responseVO = this.parseSearchResult(searchResponse);

        responseVO.setPageNo(searchParam.getPageNo()); // 1
        responseVO.setPageSize(searchParam.getPageSize()); // 3
        //  10 3 4  9 3 3
        //  总数%每页显示的条数==0?总数/每页显示的条数:总数/每页显示的条数+1
        long totalPages = (responseVO.getTotal()+responseVO.getPageSize()-1)/responseVO.getPageSize();
        responseVO.setTotalPages(totalPages); // 总页数
        return responseVO;
    }

    /**
     * 查询数据结果集的转换
     * @param searchResponse
     * @return
     */
    private SearchResponseVo parseSearchResult(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        /*
            private List<SearchResponseTmVo> trademarkList;
            private List<SearchResponseAttrVo> attrsList = new ArrayList<>();
            private List<Goods> goodsList = new ArrayList<>();
            private Long total;//总记录数
         */
        //  品牌数据从聚合中获取！
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();
        //  数据类型转换：获取到桶的集合
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");
        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map((bucket) -> {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            //  获取品牌Id
            String keyAsString = ((Terms.Bucket) bucket).getKeyAsString();
            searchResponseTmVo.setTmId(Long.parseLong(keyAsString));
            //  获取到品牌名称
            ParsedStringTerms tmNameAgg = ((Terms.Bucket) bucket).getAggregations().get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);
            //  获取到品牌的URL
            ParsedStringTerms tmLogoUrlAgg = ((Terms.Bucket) bucket).getAggregations().get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            //  返回当前对象
            return searchResponseTmVo;
        }).collect(Collectors.toList());
        //  设置品牌数据
        searchResponseVo.setTrademarkList(trademarkList);

        //  获取平台属性集合：聚合 attrAgg --- nested
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<SearchResponseAttrVo> attrsList = attrIdAgg.getBuckets().stream().map((bucket) -> {
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            Number keyAsNumber = ((Terms.Bucket) bucket).getKeyAsNumber();
            //  attrId
            searchResponseAttrVo.setAttrId(keyAsNumber.longValue());
            //  attrName
            ParsedStringTerms attrNameAgg = ((Terms.Bucket) bucket).getAggregations().get("attrNameAgg");
            //  attrNameAgg.getBuckets().get(0).getKeyAsString() == "key" : "价格",
            searchResponseAttrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            //  attrValueList
            ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
            //  获取平台属性值集合
            //  流式编程！   Terms.Bucket::getKeyAsString 表示通过key 来获取里面的数据！
            List<String> stringList = attrValueAgg.getBuckets().stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
            //            List<String> strings = new ArrayList<>();
            //            List<? extends Terms.Bucket> buckets = attrValueAgg.getBuckets();
            //            for (Terms.Bucket bucket1 : buckets) {
            //                String values = bucket1.getKeyAsString();
            //                strings.add(values);
            //            }
            //            searchResponseAttrVo.setAttrValueList(strings);
            //  赋值平台属性值集合
            searchResponseAttrVo.setAttrValueList(stringList);
            //  返回数据
            return searchResponseAttrVo;
        }).collect(Collectors.toList());

        //  赋值平台属性集合
        searchResponseVo.setAttrsList(attrsList);

        SearchHits hits = searchResponse.getHits();
        //  获取内层hits
        SearchHit[] subHits = hits.getHits();
        //  创建一个goodsList 集合
        List<Goods> goodsList = new ArrayList<>();
        //  赋值goodsList;
        if (subHits!=null && subHits.length>0){
            //  循环遍历
            for (SearchHit subHit : subHits) {
                //  sourceAsString 这个是json 数据格式
                String sourceAsString = subHit.getSourceAsString();
                //  将其转换为java 对象 goods的名称没有高亮显示！
                Goods goods = JSON.parseObject(sourceAsString, Goods.class);
                //  如果高亮中的数据不为空，应该获取高亮的名称
                if(subHit.getHighlightFields().get("title")!=null){
                    //  获取到高亮中的数据
                    Text title = subHit.getHighlightFields().get("title").getFragments()[0];
                    goods.setTitle(title.toString());
                }
                //  如果有高亮，就是高亮的名称！
                goodsList.add(goods);
            }
        }
        //  赋值goodsList集合
        searchResponseVo.setGoodsList(goodsList);
        //  获取到总条数
        searchResponseVo.setTotal(hits.totalHits);

        //  返回数据：
        return searchResponseVo;
    }

    /**
     * 生产dsl 语句 根据dsl 语句！
     * @param searchParam
     * @return
     */
    private SearchRequest buildQueryDsl(SearchParam searchParam) {
        //  查询器{}
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //  {query--- bool }
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //  判断用户是否根据分类Id 进行查询
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())){
            //  query--- bool --- filter --- term
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id",searchParam.getCategory1Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())){
            //  query--- bool --- filter --- term
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id",searchParam.getCategory2Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())){
            //  query--- bool --- filter --- term
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id",searchParam.getCategory3Id()));
        }
        //  有可能会根据品牌Id 进行过滤
        //  前端传递数据格式： trademark=4:小米
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)){
            //  进行分割 坑：注意如果你使用的是工具类进行分割的话，不要使用spring框架的！ StringUtils.split()
            String[] split = trademark.split(":");
            if (split!=null && split.length==2){
                boolQueryBuilder.filter(QueryBuilders.termQuery("tmId",split[0]));
            }

        }

        //  看用户是否通过平台属性值进行过滤！
        //  props=23:8G:运行内存33&props=107:华为:二级手机
        String[] props = searchParam.getProps();
        if (props!=null && props.length>0){
            //  循环遍历
            for (String prop : props) {
                //  prop 每个平台属性值过滤的数据！ prop = 23:8G:运行内存
                String[] split = prop.split(":");
                if (split!=null && split.length==3){
                    //  split[0]; 属性Id
                    //  split[1]; 属性值名称
                    //  创建两个boolQuery
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    // 嵌套查询子查询
                    BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();
                    //  设置平台属性Id
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",split[0]));
                    //  设置平台属性值名称
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue",split[1]));
                    //  bool --- must --- nested
                    boolQuery.must(QueryBuilders.nestedQuery("attrs",subBoolQuery, ScoreMode.None));
                    //  将boolQuery 放入最外层
                    boolQueryBuilder.filter(boolQuery);
                }
            }
        }

        //  判断用户是否根据keyword 检索
        if (!StringUtils.isEmpty(searchParam.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.matchQuery("title",searchParam.getKeyword()).operator(Operator.AND));
        }
        //  {query }
        searchSourceBuilder.query(boolQueryBuilder);
        //  分页 10 条数据 0,3  3,3  6,3
        int from = (searchParam.getPageNo()-1)*searchParam.getPageSize();
        searchSourceBuilder.from(from); //  从结果集中第几条数据开始显示！
        searchSourceBuilder.size(searchParam.getPageSize());    //  默认值3

        //  排序：order=1:asc order=1:desc
        //  1: 表示按照哪个字段进行排序！ 1 表示热度排序hotScore 2 表示价格排序 priece
        //  asc : 表示升序  desc: 表示降序
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)){
            //  声明一个字段
            String field = "";
            //  分割
            String[] split = order.split(":");
            if (split!=null && split.length==2){
                //  判断按照什么字段进行排序！
                switch (split[0]){
                    case "1":
                        field="hotScore";
                        break;
                    case "2":
                        field="price";
                        break;
                }
                //  如果传递过来的是asc ,那么就是升序，如果desc 就是降序 表达式?值1:值2
                searchSourceBuilder.sort(field,"asc".equals(split[1])?SortOrder.ASC:SortOrder.DESC);
            }else {
                //  不走if 给默认值！
                searchSourceBuilder.sort("hotScore",SortOrder.DESC);
            }

        }
        //  编写高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style=color:red>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);

        //  聚合！
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        //  品牌聚合
        searchSourceBuilder.aggregation(aggregationBuilder);
        //  销售属性聚合
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","attrs")
                            .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                            .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                            .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

        //  设置哪些字段显示，哪些字段不显示！
        searchSourceBuilder.fetchSource(new String []{"id","title","defaultImg","price"},null);
        //  searchSourceBuilder 构建完成！
        SearchRequest searchRequest = new SearchRequest("goods");
        //  GET /goods/info/_search
        searchRequest.types("info");

        searchRequest.source(searchSourceBuilder);
        //  dsl 语句在哪 searchSourceBuilder
        String dsl = searchSourceBuilder.toString();
        System.out.println("DSL:\t"+dsl);
        //  返回数据
        return searchRequest;
    }
}
