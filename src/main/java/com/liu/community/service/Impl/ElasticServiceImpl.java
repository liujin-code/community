package com.liu.community.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.liu.community.entity.DiscussPost;
import com.liu.community.service.ElasticService;
import com.liu.community.entity.SearchResult;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ElasticServiceImpl implements ElasticService {

    @Autowired
    private ElasticsearchRepository elasticsearchRepository;

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public void save(DiscussPost discussPost) {
        elasticsearchRepository.save(discussPost);
    }

    @Override
    public void delete(int id) {
        elasticsearchRepository.deleteById(id);
    }

    @Override
    public SearchResult searchDiscussPost(String keyword, int current, int limit) throws IOException {
        SearchRequest searchRequest = new SearchRequest("discusspost");

        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("title")
                .field("content")
                //如果要多个字段高亮,这项要为false
                .requireFieldMatch(false)
                .preTags("<em>")
                .postTags("</em>");

        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(current)// 指定从哪条开始查询
                .size(limit)// 需要查出的总记录条数
                .highlighter(highlightBuilder);//高亮

        searchRequest.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        List<DiscussPost> list = new ArrayList<>();
        long total = searchResponse.getHits().getTotalHits().value;
        for (SearchHit hit : searchResponse.getHits().getHits()) {
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);

            // 处理高亮显示的结果
            HighlightField titleField = hit.getHighlightFields().get("title");
            if (titleField != null) {
                discussPost.setTitle(titleField.getFragments()[0].toString());
            }
            HighlightField contentField = hit.getHighlightFields().get("content");
            if (contentField != null) {
                discussPost.setContent(contentField.getFragments()[0].toString());
            }
//                System.out.println(discussPost);
            list.add(discussPost);
        }

        return new SearchResult(list, total);

    }
}
