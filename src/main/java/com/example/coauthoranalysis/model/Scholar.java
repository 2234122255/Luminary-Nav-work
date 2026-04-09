package com.example.coauthoranalysis.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
// 核心：即便 JSON 里有 Java 类里没写的字段，也直接忽略，不要报错
@JsonIgnoreProperties(ignoreUnknown = true)
public class Scholar {
    private String id;
    private String name;
    private String org;
    
    // 确保这个字段存在，它是你画雷达图的核心
    private double[] metrics;
    
 // ... 其他字段 ...

    @JsonProperty("totalScore") // 确保发给前端的 JSON Key 叫 totalScore
    @JsonAlias({"final_score", "finalScore"})
    private double totalScore;

    // 如果你用了 Lombok @Data，下面的手动 Getter 可以不写
    // 但如果还是 0，请加上这个手动 Getter 尝试强制覆盖
    public double getTotalScore() {
        return totalScore;
    }

    // 这个字段如果 JSON 里没有，Jackson 会给默认值 0
    private int paperCount;
}