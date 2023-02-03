package com.liu.community;

import com.alibaba.fastjson.JSONObject;
import com.liu.community.dao.DiscussPostMapper;
import com.liu.community.dao.elasticsearch.DiscussPostRepository;
import com.liu.community.entity.DiscussPost;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JsonbTester;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class ElasticsearchTests {

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    //    @Autowired
//    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Test
    public void testInsert() {
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(241));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(242));
        discussPostRepository.save(discussPostMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133, 0, 100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134, 0, 100,0));
    }

    @Test
    public void testUpdate() {
        DiscussPost post = discussPostMapper.selectDiscussPostById(231);
        post.setContent("我是新人,使劲灌水.");
        discussPostRepository.save(post);
    }

    @Test
    public void testSearch() throws IOException {
        //discusspost是索引名，就是表名
        SearchRequest searchRequest = new SearchRequest("discusspost");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                //在discusspost索引的title和content字段中都查询“互联网寒冬”
                .query(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content"))
        // matchQuery是模糊查询，会对key进行分词：searchSourceBuilder.query(QueryBuilders.matchQuery(key,value));
        // termQuery是精准查询：searchSourceBuilder.query(QueryBuilders.termQuery(key,value));
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                //一个可选项，用于控制允许搜索的时间：searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
                .from(0)// 指定从哪条开始查询
                .size(1);// 需要查出的总记录条数

        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

//        System.out.println(JSONObject.toJSON(response));

        System.out.println("=========================");
        List<DiscussPost> list = new LinkedList<>();
        for (SearchHit hit : response.getHits()){
            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);
            System.out.println(discussPost);
            list.add(discussPost);
        }
        System.out.println(list.size());
    }

    @Test
    public void testHighField() throws IOException {
        SearchRequest searchRequest = new SearchRequest("discusspost");

        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("title")
                .field("content")
                .requireFieldMatch(false)
                .preTags("<em>")
                .postTags("</em>");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                .query(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                .sort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .sort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .from(0)
                .size(100)
                .highlighter(highlightBuilder);

        searchRequest.source(searchSourceBuilder);
        SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        List<DiscussPost> list = new LinkedList<>();

        for (SearchHit hit : response.getHits()){

            DiscussPost discussPost = JSONObject.parseObject(hit.getSourceAsString(), DiscussPost.class);

            HighlightField title = hit.getHighlightFields().get("title");
            if (title!=null){
                System.out.println("---------title---------");
                System.out.println(Arrays.toString(title.getFragments()));
                discussPost.setTitle(title.getFragments()[0].toString());
            }

            HighlightField content = hit.getHighlightFields().get("content");
            if (content!=null){
                System.out.println("---------content---------");
                System.out.println(Arrays.toString(content.getFragments()));
                discussPost.setContent(content.getFragments()[0].toString());
            }
            System.out.println(discussPost);
            list.add(discussPost);
        }

    }


}
