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
 * @author Yuehong Zhang
 */
@Service
public class SearchServiceImpl implements SearchService {

    // The service layer calls the client! GoodsRepository custom data interface! Inherit ElasticsearchRepository<T, ID> The current data interface of this class has CRUD methods!
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
        // Declare a Goods object
        Goods goods = new Goods();

        // Asynchronous orchestration!
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        if (skuInfo!=null){
            goods.setId(skuId);
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());
            goods.setTitle(skuInfo.getSkuName());
            goods.setPrice(skuInfo.getPrice().doubleValue());
            goods.setCreateTime(new Date());

            // Assign brand data
            BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
            goods.setTmId(skuInfo.getTmId());
            goods.setTmName(trademark.getTmName());
            goods.setTmLogoUrl(trademark.getLogoUrl());

            // Assignment classification data:
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            goods.setCategory1Id(categoryView.getCategory1Id());
            goods.setCategory2Id(categoryView.getCategory2Id());
            goods.setCategory3Id(categoryView.getCategory3Id());
            goods.setCategory1Name(categoryView.getCategory1Name());
            goods.setCategory2Name(categoryView.getCategory2Name());
            goods.setCategory3Name(categoryView.getCategory3Name());

            // Assign platform attribute set 0
            List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuId);
            // Function R apply(T t)
            // Stream() streaming programming
            List<SearchAttr> searchAttrList = attrList.stream().map(baseAttrInfo -> {
                // Create an object
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
        // delete according to id
        goodsRepository.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {
        // What must be considered first when using redis with redis? Data type! Zset ZINCRBY salary 2000 tom # tom Salary increase! Name the key! Three common problems with caching!
        String hotScoreKey = "hotScore"; // key=hotScore score incremented data skuId:45

        Double count = redisTemplate.opsForZSet().incrementScore(hotScoreKey, "skuId:" + skuId, 1);
        // The result of hotScore accumulated from the cache in es!
        if (count%10==0){
            // Update es once!
            Optional<Goods> optional = this.goodsRepository.findById(skuId);
            Goods goods = optional.get();
            goods.setHotScore(count.longValue());
            // Save!
            this.goodsRepository.save(goods);
        }
    }

    @SneakyThrows
    @Override
    public SearchResponseVo search(SearchParam searchParam) {
        /*
        1. Generate the corresponding dsl statement according to the user's search conditions {method}
        2. Execute the dsl statement and get the result set {how to operate es, introduce the client}
        3. Encapsulate the query result set {method}
         */
        SearchRequest searchRequest = this.buildQueryDsl(searchParam);
        // Call search to query data to get the result set
        SearchResponse searchResponse = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        // Encapsulate the result set of the query
        // The total number of total records is assigned in parseSearchResult!
        SearchResponseVo responseVO = this.parseSearchResult(searchResponse);

        responseVO.setPageNo(searchParam.getPageNo()); // 1
        responseVO.setPageSize(searchParam.getPageSize()); // 3
        // 10 3 4 9 3 3
        // Total% number of items displayed on each page==0? Total/number of items displayed on each page: total/number of items displayed on each page+1
        long totalPages = (responseVO.getTotal()+responseVO.getPageSize()-1)/responseVO.getPageSize();
        responseVO.setTotalPages(totalPages); // total number of pages
        return responseVO;
    }

    /**
     * Conversion of query data result set
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
        // Brand data is obtained from the aggregation!
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();
        // Data type conversion: get to the collection of buckets
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregationMap.get("tmIdAgg");
        List<SearchResponseTmVo> trademarkList = tmIdAgg.getBuckets().stream().map((bucket) -> {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            // Get brand Id
            String keyAsString = ((Terms.Bucket) bucket).getKeyAsString();
            searchResponseTmVo.setTmId(Long.parseLong(keyAsString));
            // Get the brand name
            ParsedStringTerms tmNameAgg = ((Terms.Bucket) bucket).getAggregations().get("tmNameAgg");
            String tmName = tmNameAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmName(tmName);
            // Get the URL of the brand
            ParsedStringTerms tmLogoUrlAgg = ((Terms.Bucket) bucket).getAggregations().get("tmLogoUrlAgg");
            String tmLogoUrl = tmLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchResponseTmVo.setTmLogoUrl(tmLogoUrl);
            // return the current object
            return searchResponseTmVo;
        }).collect(Collectors.toList());
        // Set brand data
        searchResponseVo.setTrademarkList(trademarkList);

        // Get platform attribute collection: aggregation attrAgg --- nested
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attrIdAgg");
        List<SearchResponseAttrVo> attrsList = attrIdAgg.getBuckets().stream().map((bucket) -> {
            SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
            Number keyAsNumber = ((Terms.Bucket) bucket).getKeyAsNumber();
            // attrId
            searchResponseAttrVo.setAttrId(keyAsNumber.longValue());
            // attrName
            ParsedStringTerms attrNameAgg = ((Terms.Bucket) bucket).getAggregations().get("attrNameAgg");
            // attrNameAgg.getBuckets().get(0).getKeyAsString() == "key": "price",
            searchResponseAttrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            // attrValueList
            ParsedStringTerms attrValueAgg = ((Terms.Bucket) bucket).getAggregations().get("attrValueAgg");
            // Get a collection of platform attribute values
            // Streaming programming! Terms.Bucket::getKeyAsString means to get the data inside by key!
            List<String> stringList = attrValueAgg.getBuckets().stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
            // List<String> strings = new ArrayList<>();
            // List<? extends Terms.Bucket> buckets = attrValueAgg.getBuckets();
            // for (Terms.Bucket bucket1: buckets) {
            // String values ​​= bucket1.getKeyAsString();
            // strings.add(values);
            //}
            // searchResponseAttrVo.setAttrValueList(strings);
            // Assign platform attribute value collection
            searchResponseAttrVo.setAttrValueList(stringList);
            // return data
            return searchResponseAttrVo;
        }).collect(Collectors.toList());

        // Assignment platform attribute collection
        searchResponseVo.setAttrsList(attrsList);

        SearchHits hits = searchResponse.getHits();
        // Get inner hits
        SearchHit[] subHits = hits.getHits();
        // Create a goodsList collection
        List<Goods> goodsList = new ArrayList<>();
        // Assign goodsList;
        if (subHits!=null && subHits.length>0){
            // loop traversal
            for (SearchHit subHit: subHits) {
                // sourceAsString this is the json data format
                String sourceAsString = subHit.getSourceAsString();
                // Convert it to a java object The name of goods is not highlighted!
                Goods goods = JSON.parseObject(sourceAsString, Goods.class);
                // If the data in the highlight is not empty, the name of the highlight should be obtained
                if(subHit.getHighlightFields().get("title")!=null){
                    // Get the highlighted data
                    Text title = subHit.getHighlightFields().get("title").getFragments()[0];
                    goods.setTitle(title.toString());
                }
                // If there is a highlight, it is the name of the highlight!
                goodsList.add(goods);
            }
        }
        // Assign goodsList collection
        searchResponseVo.setGoodsList(goodsList);
        // Get the total number
        searchResponseVo.setTotal(hits.totalHits);

        // Return data:
        return searchResponseVo;
    }

    /**
     * Produce dsl statement according to dsl statement!
     * @param searchParam
     * @return
     */
    private SearchRequest buildQueryDsl(SearchParam searchParam) {
        // Queryer{}
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // {query--- bool}
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // Determine whether the user queries according to the category Id
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())){
            // query--- bool --- filter --- term
            boolQueryBuilder.filter(QueryBuilders.termQuery("category1Id",searchParam.getCategory1Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())){
            // query--- bool --- filter --- term
            boolQueryBuilder.filter(QueryBuilders.termQuery("category2Id",searchParam.getCategory2Id()));
        }
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())){
            // query--- bool --- filter --- term
            boolQueryBuilder.filter(QueryBuilders.termQuery("category3Id",searchParam.getCategory3Id()));
        }
        // It is possible to filter according to the brand Id
        // The front-end transfer data format: trademark=4: Xiaomi
        String trademark = searchParam.getTrademark();
        if (!StringUtils.isEmpty(trademark)){
            // Perform segmentation Pit: Note that if you are using a tool class for segmentation, do not use the spring framework! StringUtils.split()
            String[] split = trademark.split(":");
            if (split!=null && split.length==2){
                boolQueryBuilder.filter(QueryBuilders.termQuery("tmId",split[0]));
            }

        }

        // See if the user is filtered by the platform attribute value!
        // props=23:8G: running memory 33&props=107: Huawei: secondary phone
        String[] props = searchParam.getProps();
        if (props!=null && props.length>0){
            // loop traversal
            for (String prop: props) {
                // prop data filtered by the property value of each platform! prop = 23: 8G: running memory
                String[] split = prop.split(":");
                if (split!=null && split.length==3){
                    // split[0]; attribute Id
                    // split[1]; attribute value name
                    // Create two boolQuery
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    // Nested query subquery
                    BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();
                    // Set platform property Id
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId",split[0]));
                    // Set the platform attribute value name
                    subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue",split[1]));
                    // bool --- must --- nested
                    boolQuery.must(QueryBuilders.nestedQuery("attrs",subBoolQuery, ScoreMode.None));
                    // put boolQuery in the outermost layer
                    boolQueryBuilder.filter(boolQuery);
                }
            }
        }

        // Determine whether the user is searching based on keyword
        if (!StringUtils.isEmpty(searchParam.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.matchQuery("title",searchParam.getKeyword()).operator(Operator.AND));
        }
        // {query}
        searchSourceBuilder.query(boolQueryBuilder);
        // Paging 10 pieces of data 0,3 3,3 6,3
        int from = (searchParam.getPageNo()-1)*searchParam.getPageSize();
        searchSourceBuilder.from(from); // Start displaying from the first few data in the result set!
        searchSourceBuilder.size(searchParam.getPageSize()); // default value 3

        // Sort: order=1:asc order=1:desc
        // 1: Indicates which field to sort according to! 1 indicates the popularity ranking hotScore 2 indicates the price ranking priece
        // asc: means ascending order desc: means descending order
        String order = searchParam.getOrder();
        if (!StringUtils.isEmpty(order)){
            // Declare a field
            String field = "";
            // split
            String[] split = order.split(":");
            if (split!=null && split.length==2){
                // Determine which field to sort by!
                switch (split[0]){
                    case "1":
                        field="hotScore";
                        break;
                    case "2":
                        field="price";
                        break;
                }
                // If asc is passed, then it is in ascending order, if desc is in descending order, expression? value 1: value 2
                searchSourceBuilder.sort(field,"asc".equals(split[1])?SortOrder.ASC:SortOrder.DESC);
            }else {
                // Don't go if and give the default value!
                searchSourceBuilder.sort("hotScore",SortOrder.DESC);
            }

        }
        // write highlight
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.preTags("<span style=color:red>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);

        // Convergence!
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        // Brand aggregation
        searchSourceBuilder.aggregation(aggregationBuilder);
        // Sales attribute aggregation
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg","attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));

        // Set which fields are displayed and which fields are not displayed!
        searchSourceBuilder.fetchSource(new String []{"id","title","defaultImg","price"},null);
        // searchSourceBuilder is built!
        SearchRequest searchRequest = new SearchRequest("goods");
        // GET /goods/info/_search
        searchRequest.types("info");

        searchRequest.source(searchSourceBuilder);
        // Where is the dsl statement searchSourceBuilder
        String dsl = searchSourceBuilder.toString();
        System.out.println("DSL:\t"+dsl);
        // return data
        return searchRequest;
    }
}
