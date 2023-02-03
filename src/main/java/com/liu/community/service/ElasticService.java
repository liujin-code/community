package com.liu.community.service;

import com.liu.community.entity.DiscussPost;
import com.liu.community.entity.SearchResult;

import java.io.IOException;

public interface ElasticService {

    public void save(DiscussPost discussPost);

    public void delete(int id);

    public SearchResult searchDiscussPost(String keyword, int current, int limit) throws IOException;

}
