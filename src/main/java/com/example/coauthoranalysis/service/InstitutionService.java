package com.example.coauthoranalysis.service;

import com.example.coauthoranalysis.model.Institution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InstitutionService {
    // 获取机构排名
    Page<Institution> getInstitutionRankings(Pageable pageable);

    // 根据名称获取机构
    Institution getInstitutionByName(String name);

    // 计算并更新机构统计数据
    void calculateInstitutionStats();
}