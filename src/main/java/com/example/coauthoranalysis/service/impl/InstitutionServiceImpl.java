package com.example.coauthoranalysis.service.impl;

import com.example.coauthoranalysis.exception.ResourceNotFoundException;
import com.example.coauthoranalysis.model.Institution;
import com.example.coauthoranalysis.repository.AuthorRepository;
import com.example.coauthoranalysis.repository.InstitutionRepository;
import com.example.coauthoranalysis.service.InstitutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InstitutionServiceImpl implements InstitutionService {

    private final InstitutionRepository institutionRepository;
    private final AuthorRepository authorRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<Institution> getInstitutionRankings(Pageable pageable) {
        return institutionRepository.findTopNByOrderByAverageScoreDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Institution getInstitutionByName(String name) {
        return institutionRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Institution", "name", name));
    }

    @Override
    @Transactional
    public void calculateInstitutionStats() {
        // 清空现有统计数据
        institutionRepository.deleteAll();

        // 从作者数据中聚合机构信息
        Map<String, Institution> institutionMap = new HashMap<>();

        authorRepository.findAll().forEach(author -> {
            if (author.getOrg() == null || author.getOrg().trim().isEmpty()) {
                return;
            }

            String orgName = author.getOrg().length() > 1000 ?
                    author.getOrg().substring(0, 1000) : author.getOrg();

            Institution institution = institutionMap.computeIfAbsent(orgName, name -> {
                Institution inst = new Institution();
                inst.setName(name);
                inst.setPaperCount(0);
                inst.setTotalCitations(0);
                inst.setAverageScore(0.0);
                return inst;
            });

            institution.setPaperCount(institution.getPaperCount() + author.getPaperCount());
            institution.setTotalCitations(institution.getTotalCitations() + author.getTotalCitations());
        });

        // 计算平均分并保存
        institutionMap.forEach((name, institution) -> {
            Double avgScore = authorRepository.getAverageScoreByOrg(name);
            institution.setAverageScore(avgScore != null ? avgScore : 0.0);
            institutionRepository.save(institution);
        });
    }
}