package com.liu.community.utils;

import org.apache.commons.lang3.CharUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

@Component
public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    private final static String REPLACEMENT = "***";

    TrieNode rootNode = new TrieNode();

    @PostConstruct
    public void init() {
        try (
                InputStream resourceAsStream = SensitiveFilter.class.getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resourceAsStream));
        ) {
            String readLine;
            while ((readLine = bufferedReader.readLine()) != null) {
                this.addNode(readLine);
            }
        } catch (Exception e) {
            logger.error("加载敏感词文件失败: " + e.getMessage());
        }

    }

    //    添加敏感词
    public void addNode(String text) {
        TrieNode current = rootNode;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
//            单词已添加
            if (current.getSubNode(c) != null) {
                current = current.getSubNode(c);
            }else{
//            单词未添加
                TrieNode subNode = new TrieNode();
                current.addSubNode(c, subNode);
                current = subNode;
            }

            if (i == text.length() - 1) {
                current.setKeywordEnd(true);
            }
        }
    }

    // 开始过滤
    public String filter(String text) {
//        前缀树
        TrieNode currentNode = rootNode;
//        过滤字符串排查指针
        int begin = 0;
//        过滤字符串遍历指针
        int position = 0;
//        结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);

//            是特殊字符且不是敏感单词
            if (isSymbol(c) && currentNode.getSubNode(c) == null) {
//                不在敏感词中的特殊字符
                if (currentNode == rootNode) {
                    sb.append(c);
//                    指针后移
                    begin++;
                }
                position++;
                continue;
            }
            currentNode = currentNode.getSubNode(c);
            if (currentNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                position = ++begin;
//                重新指向root结点
                currentNode = rootNode;
            }else if (currentNode.isKeywordEnd()){
                sb.append(REPLACEMENT);
                begin = ++position;
                currentNode =rootNode;
            }else {
                position++;
            }
        }
        sb.append(text.substring(begin));
        return sb.toString();
    }

    // 判断是否为特殊符号
    public boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }
}

class TrieNode {
    //    是否是关键字结束
    private boolean isKeywordEnd = false;

    // 子节点(key是下级字符,value是下级节点)
    private HashMap<Character, TrieNode> subNodes = new HashMap<>();

    public boolean isKeywordEnd() {
        return isKeywordEnd;
    }

    public void setKeywordEnd(boolean keywordEnd) {
        isKeywordEnd = keywordEnd;
    }

    // 添加子节点
    public void addSubNode(Character c, TrieNode node) {
        subNodes.put(c, node);
    }

    //    获取子节点
    public TrieNode getSubNode(Character c) {
        return subNodes.get(c);
    }
}