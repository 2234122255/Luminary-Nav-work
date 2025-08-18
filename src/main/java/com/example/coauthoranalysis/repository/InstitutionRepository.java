package com.example.coauthoranalysis.repository;

import com.example.coauthoranalysis.model.Institution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstitutionRepository extends JpaRepository<Institution, Long> {

    // 机构排名 - 按平均分降序
    @Query("SELECT i FROM Institution i ORDER BY i.averageScore DESC")
    Page<Institution> findTopNByOrderByAverageScoreDesc(Pageable pageable);

    // 根据名称查找机构
    Optional<Institution> findByName(String name);
    List<Institution> findTop10ByOrderByAverageScoreDesc();
    Page<Institution> findAllByOrderByAverageScoreDesc(Pageable pageable);
}