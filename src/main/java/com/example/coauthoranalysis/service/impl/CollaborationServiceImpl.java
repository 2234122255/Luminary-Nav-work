package com.example.coauthoranalysis.service.impl;

import com.example.coauthoranalysis.exception.ResourceNotFoundException; // 添加这行
import com.example.coauthoranalysis.model.Author;
import com.example.coauthoranalysis.model.Collaboration;
import com.example.coauthoranalysis.model.CollaborationNetwork;
import com.example.coauthoranalysis.repository.AuthorRepository;
import com.example.coauthoranalysis.repository.CollaborationRepository;
import com.example.coauthoranalysis.service.CollaborationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CollaborationServiceImpl implements CollaborationService {

    private final CollaborationRepository collaborationRepository;
    private final AuthorRepository authorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Collaboration> getAuthorCollaborations(String authorId) {
        // 验证作者存在
        if (!authorRepository.existsById(authorId)) {
            throw new ResourceNotFoundException("Author", "id", authorId);
        }
        return collaborationRepository.findByAuthor1IdOrAuthor2IdAndWeightGreaterThanEqual(authorId, authorId, 0.1);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Collaboration> getCollaborationsAboveWeight(double minWeight) {
        return collaborationRepository.findByWeightGreaterThanEqual(minWeight);
    }

    @Override
    @Transactional(readOnly = true)
    public CollaborationNetwork getCollaborationNetwork(String centralAuthorId, int depth) {
        // 验证中心作者存在
        Author centralAuthor = authorRepository.findById(centralAuthorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author", "id", centralAuthorId));

        // 获取所有相关合作关系
        List<Collaboration> allCollaborations = new ArrayList<>();
        Set<String> processedAuthorIds = new HashSet<>();
        Queue<String> authorQueue = new LinkedList<>();

        // 初始化队列
        authorQueue.add(centralAuthorId);
        processedAuthorIds.add(centralAuthorId);

        // 广度优先搜索(BFS)获取合作关系网络
        for (int currentDepth = 0; currentDepth < depth && !authorQueue.isEmpty(); currentDepth++) {
            int levelSize = authorQueue.size();
            for (int i = 0; i < levelSize; i++) {
                String currentAuthorId = authorQueue.poll();
                List<Collaboration> currentCollaborations = collaborationRepository
                        .findByAuthor1IdOrAuthor2IdAndWeightGreaterThanEqual(currentAuthorId, currentAuthorId, 0.3);

                allCollaborations.addAll(currentCollaborations);

                // 将合作作者加入队列（如果未处理过）
                currentCollaborations.forEach(collab -> {
                    String otherAuthorId = collab.getAuthor1().getId().equals(currentAuthorId) ?
                            collab.getAuthor2().getId() : collab.getAuthor1().getId();
                    if (!processedAuthorIds.contains(otherAuthorId)) {
                        processedAuthorIds.add(otherAuthorId);
                        authorQueue.add(otherAuthorId);
                    }
                });
            }
        }

        // 获取所有相关作者信息
        Set<String> relatedAuthorIds = allCollaborations.stream()
                .flatMap(collab -> Stream.of(collab.getAuthor1().getId(), collab.getAuthor2().getId()))
                .filter(id -> !id.equals(centralAuthorId))
                .collect(Collectors.toSet());

        List<Author> relatedAuthors = authorRepository.findByIds(new ArrayList<>(relatedAuthorIds));

        // 构建网络对象
        CollaborationNetwork network = new CollaborationNetwork();
        network.setCentralAuthorId(centralAuthorId);
        network.setCollaborations(allCollaborations);
        network.setRelatedAuthors(relatedAuthors);

        return network;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Collaboration> getCollaborationsBetweenAuthors(String author1Id, String author2Id) {
        // 验证作者存在
        if (!authorRepository.existsById(author1Id)) {
            throw new ResourceNotFoundException("Author", "id", author1Id);
        }
        if (!authorRepository.existsById(author2Id)) {
            throw new ResourceNotFoundException("Author", "id", author2Id);
        }
        return collaborationRepository.findCollaborationBetweenAuthors(author1Id, author2Id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Collaboration> getCollaborationsByAuthorAndMinWeight(String authorId, double minWeight) {
        if (!authorRepository.existsById(authorId)) {
            throw new ResourceNotFoundException("Author", "id", authorId);
        }
        return collaborationRepository.findByAuthor1IdOrAuthor2IdAndWeightGreaterThanEqual(authorId, authorId, minWeight);
    }

    @Transactional(readOnly = true)
    @Override
    public Map<String, Integer> getCollaborationCounts(List<String> authorIds) {
        List<Object[]> results = collaborationRepository.countCollaborationsByAuthorIds(authorIds);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> ((Number) result[1]).intValue()
                ));
    }

    @Transactional
    @Override
    public void updateCollaborationWeight(String author1Id, String author2Id, double newWeight) {
        List<Collaboration> collaborations = collaborationRepository
                .findCollaborationBetweenAuthors(author1Id, author2Id);

        if (collaborations.isEmpty()) {
            throw new ResourceNotFoundException("Collaboration between authors",
                    "authorIds", author1Id + " and " + author2Id);
        }

        collaborations.forEach(collab -> collab.setWeight(newWeight));
        collaborationRepository.saveAll(collaborations);
    }
}