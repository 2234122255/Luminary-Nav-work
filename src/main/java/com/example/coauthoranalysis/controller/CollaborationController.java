package com.example.coauthoranalysis.controller;

import com.example.coauthoranalysis.model.Collaboration;
import com.example.coauthoranalysis.repository.CollaborationRepository;
import com.example.coauthoranalysis.service.CollaborationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
// CollaborationController.java
@RestController
@RequestMapping("/api/collaborations")
public class CollaborationController {
    private final CollaborationRepository collaborationRepository;

    public CollaborationController(CollaborationRepository collaborationRepository) {
        this.collaborationRepository = collaborationRepository;
    }

    // 获取合著网络数据
    @GetMapping("/network")
    public List<Collaboration> getCollaborationNetwork(
            @RequestParam(required = false) String authorId,
            @RequestParam(defaultValue = "1") double minWeight) {
        if (authorId != null) {
            return collaborationRepository.findByAuthor1IdOrAuthor2IdAndWeightGreaterThanEqual(
                    authorId, authorId, minWeight);
        }
        return collaborationRepository.findByWeightGreaterThanEqual(minWeight);
    }
}