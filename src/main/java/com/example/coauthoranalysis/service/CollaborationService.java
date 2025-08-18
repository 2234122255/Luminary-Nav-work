package com.example.coauthoranalysis.service;

import com.example.coauthoranalysis.model.Collaboration;
import com.example.coauthoranalysis.model.CollaborationNetwork;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface CollaborationService {
    // 获取作者合作关系
    List<Collaboration> getAuthorCollaborations(String authorId);

    // 获取权重超过阈值的合作关系
    List<Collaboration> getCollaborationsAboveWeight(double minWeight);

    // 获取合著网络
    CollaborationNetwork getCollaborationNetwork(String centralAuthorId, int depth);

    // 获取两个作者之间的合作关系
    List<Collaboration> getCollaborationsBetweenAuthors(String author1Id, String author2Id);

    @Transactional(readOnly = true)
    List<Collaboration> getCollaborationsByAuthorAndMinWeight(String authorId, double minWeight);

    @Transactional(readOnly = true)
    Map<String, Integer> getCollaborationCounts(List<String> authorIds);

    @Transactional
    void updateCollaborationWeight(String author1Id, String author2Id, double newWeight);
}