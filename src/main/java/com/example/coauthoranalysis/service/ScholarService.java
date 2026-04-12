package com.example.coauthoranalysis.service;

import com.example.coauthoranalysis.model.Scholar;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScholarService {
    private List<Scholar> allScholars = new ArrayList<>();

    @PostConstruct
public void init() {
    // 尝试使用绝对路径，排除“相对路径到底在哪”的干扰
    String path = "frontend/frontend/src/assets/data/scholars.json";
    File file = new File(path);
    
    System.out.println("--- 数据加载调试开始 ---");
    System.out.println("检测路径: " + file.getAbsolutePath());
    System.out.println("文件是否存在: " + file.exists());
    
    if (!file.exists()) {
        System.err.println("❌ 错误：找不到文件，请确认路径是否正确！");
        return;
    }

    try {
        ObjectMapper mapper = new ObjectMapper();
        // 如果字段不匹配也想强制加载，可以开启下面这行（但不推荐，最好用 @JsonProperty）
        // mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        this.allScholars = mapper.readValue(file, new TypeReference<List<Scholar>>(){});

// 打印第一条数据看看
if (!allScholars.isEmpty()) {
    Scholar first = allScholars.get(0);
    System.out.println("--- 调试信息 ---");
    System.out.println("姓名: " + first.getName());
    System.out.println("总分 (totalScore): " + first.getTotalScore()); 
    // 如果这里打印出来是 0.0，说明是第一层注解没写对
}


        allScholars.forEach(s -> {
    if (s.getMetrics() != null && s.getMetrics().length > 2) {
        // 假设 metrics[2] 是论文数
        s.setPaperCount((int) s.getMetrics()[2]);
    }
});
        System.out.println("✅ 成功！已从 JSON 加载学者数量: " + this.allScholars.size());
        
        // 排序
       // 在 init 方法或者 search 方法中的排序部分
allScholars.sort((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()));

// 在 searchScholars 方法的过滤逻辑中，如果用到了分数，也请改为 s.getTotalScore()
    } catch (Exception e) {
        System.err.println("❌ 加载数据时发生异常: " + e.getMessage());
        e.printStackTrace();
    }
    System.out.println("--- 数据加载调试结束 ---");
}
   public List<Scholar> searchScholars(String name, String org, int page, int size) {
    return allScholars.stream()
        .filter(s -> {
            // 姓名模糊匹配（不区分大小写）
            boolean nameMatch = (name == null || name.trim().isEmpty()) || 
                                s.getName().toLowerCase().contains(name.toLowerCase().trim());
            // 机构模糊匹配（不区分大小写）
            boolean orgMatch = (org == null || org.trim().isEmpty()) || 
                               s.getOrg().toLowerCase().contains(org.toLowerCase().trim());
            return nameMatch && orgMatch;
        })
        .skip((long) (page - 1) * size)
        .limit(size)
        .collect(Collectors.toList());
}
// 在 ScholarService 类中添加，让 Controller 能拿到全量数据
public List<Scholar> getAllScholars() {
    return allScholars;
}
}