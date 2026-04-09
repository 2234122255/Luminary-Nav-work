package com.example.coauthoranalysis.controller;

import com.example.coauthoranalysis.model.Scholar;
import com.example.coauthoranalysis.service.ScholarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rankings")
@CrossOrigin(origins = "*") 
public class ScholarController {

    @Autowired
    private ScholarService scholarService;

    @GetMapping("/search")
    public List<Scholar> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String org,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return scholarService.searchScholars(name, org, page, size);
    }
// 新增这个接口，解决你刚才遇到的 404 错误
    @GetMapping("/top10")
    public List<Scholar> getTop10() {
        // 直接复用 search 方法，不带参数即为获取前 10 名
        return scholarService.searchScholars(null, null, 1, 10);
    }
@GetMapping("/hot-orgs")
public List<String> getHotOrgs() {
    // 获取出现频率最高的 50 个机构名
    return scholarService.getAllScholars().stream()
            .map(Scholar::getOrg)
            .filter(org -> org != null && !org.isEmpty())
            .collect(Collectors.groupingBy(org -> org, Collectors.counting()))
            .entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(50)
            .map(java.util.Map.Entry::getKey)
            .collect(Collectors.toList());
}
// 1. 机构排名接口
    @GetMapping("/institutions/top10")
    public Map<String, Object> getTopInstitutions() {
        List<Map<String, Object>> result = scholarService.getAllScholars().stream()
                .filter(s -> s.getOrg() != null && !s.getOrg().isEmpty())
                .collect(Collectors.groupingBy(Scholar::getOrg))
                .entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", entry.getKey());
                    map.put("paperCount", entry.getValue().size());
                    // 修复报错：在这里定义并计算 avg
                    double average = entry.getValue().stream()
                            .mapToDouble(Scholar::getTotalScore)
                            .average().orElse(0);
                    map.put("averageScore", average); 
                    return map;
                })
                .sorted((a, b) -> Double.compare((double)b.get("averageScore"), (double)a.get("averageScore")))
                .limit(10)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", result);
        return response;
    }

    // 2. 潜力新星接口 (基于算法：论文数少但总分高的“潜力股”)
    @GetMapping("/rising-stars")
    public List<Scholar> getRisingStars() {
        return scholarService.getAllScholars().stream()
                // 筛选逻辑：论文数在 5-20 之间，且总分排名前列的
                .filter(s -> s.getPaperCount() >= 5 && s.getPaperCount() <= 20)
                .sorted((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()))
                .limit(10)
                .collect(Collectors.toList());
    }

   @GetMapping("/leaders")
public Map<String, Object> getFullLeaders(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String name) {

    List<Scholar> all = scholarService.getAllScholars();
    
    // 1. 过滤逻辑
    List<Scholar> filtered = all;
    if (name != null && !name.trim().isEmpty()) {
        filtered = all.stream()
                .filter(s -> s.getName().toLowerCase().contains(name.toLowerCase().trim()))
                .collect(Collectors.toList());
    }

    // 2. 分页逻辑
    int start = page * size;
    int end = Math.min(start + size, filtered.size());

    List<Scholar> content = new ArrayList<>();
    if (start < filtered.size()) {
        content = filtered.subList(start, end);
    }

    // 3. 构建 Page 对象结构（前端需要 totalElements 来计算 totalPages）
    Map<String, Object> response = new HashMap<>();
    response.put("content", content);
    response.put("totalElements", filtered.size()); // 关键：这是搜索后的总数，不是 10
    response.put("totalPages", (int) Math.ceil((double) filtered.size() / size));
    response.put("size", size);
    response.put("number", page);

    return response;
}

@GetMapping("/institutions")
public Map<String, Object> getPaginatedInstitutions(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String name) {

    // 1. 获取全量聚合数据
    List<Map<String, Object>> allOrgs = scholarService.getAllScholars().stream()
            .filter(s -> s.getOrg() != null && !s.getOrg().isEmpty())
            .collect(Collectors.groupingBy(s -> processOrgName(s.getOrg()))) // 统一处理引号名
            .entrySet().stream()
            .map(entry -> {
                Map<String, Object> map = new HashMap<>();
                map.put("name", entry.getKey());
                map.put("paperCount", entry.getValue().size());
                // 计算该机构平均分
                double avg = entry.getValue().stream()
                        .mapToDouble(Scholar::getTotalScore).average().orElse(0);
                map.put("averageScore", avg);
                // 扩展：统计该机构学者人数
                map.put("authorCount", entry.getValue().size()); 
                return map;
            })
            // 2. 关键词过滤
            .filter(m -> name == null || ((String)m.get("name")).toLowerCase().contains(name.toLowerCase()))
            // 3. 排序
            .sorted((a, b) -> Double.compare((double)b.get("averageScore"), (double)a.get("averageScore")))
            .collect(Collectors.toList());

    // 4. 手动分页
    int start = page * size;
    int end = Math.min(start + size, allOrgs.size());
    List<Map<String, Object>> content = (start < allOrgs.size()) ? allOrgs.subList(start, end) : new ArrayList<>();

    Map<String, Object> response = new HashMap<>();
    response.put("content", content);
    response.put("totalElements", allOrgs.size());
    response.put("totalPages", (int) Math.ceil((double) allOrgs.size() / size));
    
    return response;
}

// 辅助方法：确保后端处理名称时也去掉引号，与前端一致
private String processOrgName(String org) {
    if (org == null) return "Unknown";
    return org.replace("\"", "").replace("'", "").trim();
}

@GetMapping("/stars")
public Map<String, Object> getRisingStars(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String name) {

    List<Scholar> all = scholarService.getAllScholars();

    // 1. 潜力新星定义逻辑：论文数在 (5, 20) 之间，且按分数排序
    List<Scholar> filtered = all.stream()
            .filter(s -> s.getPaperCount() >= 5 && s.getPaperCount() <= 20)
            .filter(s -> name == null || s.getName().toLowerCase().contains(name.toLowerCase().trim()))
            .sorted((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()))
            .collect(Collectors.toList());

    // 2. 分页截取
    int start = page * size;
    int end = Math.min(start + size, filtered.size());
    List<Scholar> content = (start < filtered.size()) ? filtered.subList(start, end) : new ArrayList<>();

    // 3. 返回 Page 结构
    Map<String, Object> response = new HashMap<>();
    response.put("content", content);
    response.put("totalElements", filtered.size());
    response.put("totalPages", (int) Math.ceil((double) filtered.size() / size));
    
    return response;
}

@GetMapping("/detail/{id}")
public Scholar getScholarDetail(@PathVariable String id) {
    return scholarService.getAllScholars().stream()
            .filter(s -> s.getId().equals(id))
            .findFirst()
            .orElse(null);
}
}
