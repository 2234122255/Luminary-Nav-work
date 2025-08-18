package com.example.coauthoranalysis.service;

import com.example.coauthoranalysis.model.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AuthorService {
    // 获取领军人才榜单
    Page<Author> getLeaderAuthors(Pageable pageable);

    // 获取潜力新星榜单
    Page<Author> getRisingStarAuthors(Pageable pageable);

    // 获取作者详情
    Author getAuthorById(String id);

    // 搜索作者
    List<Author> searchAuthors(String query);

    // 批量获取作者
    List<Author> getAuthorsByIds(List<String> ids);

    @Transactional(readOnly = true)
    Page<Author> searchAuthors(String query, Pageable pageable);
}