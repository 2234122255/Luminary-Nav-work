package com.example.coauthoranalysis.repository;

import com.example.coauthoranalysis.model.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuthorRepository extends JpaRepository<Author, String> {

    // 领军人才榜单 - 按总评分降序
    @Query("SELECT a FROM Author a ORDER BY a.totalScore DESC")
    Page<Author> findTopNByOrderByTotalScoreDesc(Pageable pageable);

    // 潜力新星榜单 - 按调和平均数降序
    @Query("SELECT a FROM Author a ORDER BY a.harmonicMean DESC")
    Page<Author> findTopNByOrderByHarmonicMeanDesc(Pageable pageable);

    // 根据机构名称获取作者平均分
    @Query("SELECT AVG(a.totalScore) FROM Author a WHERE a.org = :orgName")
    Double getAverageScoreByOrg(String orgName);

    // 姓名搜索
    List<Author> findByNameContainingIgnoreCase(String name);

    // 根据ID列表批量查询作者
    @Query("SELECT a FROM Author a WHERE a.id IN :authorIds")
    List<Author> findByIds(List<String> authorIds);

    List<Author> findTop10ByOrderByTotalScoreDesc();
    // 添加分页搜索方法
    @Query("SELECT a FROM Author a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Author> searchAuthors(String query, Pageable pageable);
}