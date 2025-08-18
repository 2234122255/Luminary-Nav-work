package com.example.coauthoranalysis.repository;

import com.example.coauthoranalysis.model.Collaboration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollaborationRepository extends JpaRepository<Collaboration, Long> {

    // 查询与特定作者相关的合作关系
    @Query("SELECT c FROM Collaboration c WHERE c.author1.id = :authorId OR c.author2.id = :authorId ORDER BY c.weight DESC")
    List<Collaboration> findByAuthorId(String authorId);



    // 查询特定作者对之间的合作关系
    @Query("SELECT c FROM Collaboration c WHERE " +
            "(c.author1.id = :author1Id AND c.author2.id = :author2Id) OR " +
            "(c.author1.id = :author2Id AND c.author2.id = :author1Id)")
    List<Collaboration> findCollaborationBetweenAuthors(String author1Id, String author2Id);

    // 复杂查询：获取与特定作者相关的合作关系（用于合著网络）
    @Query("SELECT c FROM Collaboration c WHERE " +
            "(c.author1.id = :authorId OR c.author2.id = :authorId) AND " +
            "c.weight >= :minWeight")
    List<Collaboration> findByAuthor1IdOrAuthor2IdAndWeightGreaterThanEqual(
            String authorId, String id, double minWeight);

    @Query("SELECT c FROM Collaboration c WHERE " +
            "(c.author1.id = :authorId OR c.author2.id = :authorId) AND " +
            "c.weight >= :minWeight")
    List<Collaboration> findByAuthor1IdOrAuthor2IdAndWeightGreaterThanEqual(
            @Param("authorId") String authorId,
            @Param("minWeight") double minWeight);

    @Query("SELECT c.author1.id AS authorId, COUNT(c) AS collabCount " +
            "FROM Collaboration c WHERE c.author1.id IN :authorIds GROUP BY c.author1.id " +
            "UNION ALL " +
            "SELECT c.author2.id AS authorId, COUNT(c) AS collabCount " +
            "FROM Collaboration c WHERE c.author2.id IN :authorIds GROUP BY c.author2.id")
    List<Object[]> countCollaborationsByAuthorIds(@Param("authorIds") List<String> authorIds);

    List<Collaboration> findByWeightGreaterThanEqual(double minWeight);
}