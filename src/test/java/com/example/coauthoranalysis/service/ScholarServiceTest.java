package com.example.coauthoranalysis.service;

import com.example.coauthoranalysis.model.Scholar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 核心算法 白盒测试：ScholarService.searchScholars
 *
 * 环形复杂度计算（Cyclomatic Complexity）:
 * 基础复杂度: 1
 * nameMatch条件:
 *   - name == null || name.trim().isEmpty() (2个判定点)
 *   - s.getName().toLowerCase().contains(...) (1个判定点)
 * orgMatch条件:
 *   - org == null || org.trim().isEmpty() (2个判定点)
 *   - s.getOrg().toLowerCase().contains(...) (1个判定点)
 * nameMatch && orgMatch (1个判定点)
 *
 * 总环形复杂度 CC = 8
 *
 * 基本路径测试用例设计：
 * 1. name 为空，org 为空 -> 返回所有匹配的学者（相当于不筛选）
 * 2. name 不为空且存在，org 为空 -> 仅通过姓名筛选
 * 3. name 为空，org 不为空且存在 -> 仅通过机构筛选
 * 4. name 和 org 均不为空且存在 -> 姓名与机构共同筛选
 * 5. name 不匹配任何项，org 匹配 -> 返回空（验证 nameMatch && orgMatch 的短路逻辑）
 * 6. 分页与数量边界测试 -> page 越界或 size 边界
 */
class ScholarServiceTest {

    private ScholarService scholarService;

    @BeforeEach
    void setUp() throws Exception {
        scholarService = new ScholarService();

        // 构造测试数据
        List<Scholar> mockScholars = new ArrayList<>();
        Scholar s1 = new Scholar();
        s1.setId("1");
        s1.setName("Alice Smith");
        s1.setOrg("Stanford University");
        s1.setTotalScore(100.0);

        Scholar s2 = new Scholar();
        s2.setId("2");
        s2.setName("Bob Johnson");
        s2.setOrg("MIT");
        s2.setTotalScore(90.0);

        Scholar s3 = new Scholar();
        s3.setId("3");
        s3.setName("Charlie Smith");
        s3.setOrg("MIT");
        s3.setTotalScore(80.0);

        mockScholars.add(s1);
        mockScholars.add(s2);
        mockScholars.add(s3);

        // 使用反射注入私有字段 allScholars
        Field allScholarsField = ScholarService.class.getDeclaredField("allScholars");
        allScholarsField.setAccessible(true);
        allScholarsField.set(scholarService, mockScholars);
    }

    @Test
    void testSearch_BothEmpty_ShouldReturnAll() {
        // 路径1: name 和 org 均为空白
        List<Scholar> results = scholarService.searchScholars("", "   ", 1, 10);
        assertEquals(3, results.size());
    }

    @Test
    void testSearch_NameMatchOnly_ShouldReturnMatched() {
        // 路径2: 仅根据 name 匹配 (包含 "smith"，忽略大小写)
        List<Scholar> results = scholarService.searchScholars("smith", null, 1, 10);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(s -> s.getName().contains("Smith")));
    }

    @Test
    void testSearch_OrgMatchOnly_ShouldReturnMatched() {
        // 路径3: 仅根据 org 匹配 (包含 "mit")
        List<Scholar> results = scholarService.searchScholars(null, "mit", 1, 10);
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(s -> s.getOrg().equals("MIT")));
    }

    @Test
    void testSearch_BothMatch_ShouldReturnIntersection() {
        // 路径4: name 和 org 同时匹配
        List<Scholar> results = scholarService.searchScholars("charlie", "mit", 1, 10);
        assertEquals(1, results.size());
        assertEquals("Charlie Smith", results.get(0).getName());
    }

    @Test
    void testSearch_NameNotMatch_ShouldReturnEmpty() {
        // 路径5: name 不匹配，org 匹配 -> 验证 && 逻辑
        List<Scholar> results = scholarService.searchScholars("David", "MIT", 1, 10);
        assertEquals(0, results.size());
    }

    @Test
    void testSearch_Pagination_ShouldReturnCorrectPage() {
        // 路径6: 分页逻辑测试
        // 第一页，每页 2 条
        List<Scholar> page1 = scholarService.searchScholars(null, null, 1, 2);
        assertEquals(2, page1.size());
        assertEquals("Alice Smith", page1.get(0).getName());
        assertEquals("Bob Johnson", page1.get(1).getName());

        // 第二页，每页 2 条
        List<Scholar> page2 = scholarService.searchScholars(null, null, 2, 2);
        assertEquals(1, page2.size());
        assertEquals("Charlie Smith", page2.get(0).getName());

        // 第三页，越界
        List<Scholar> page3 = scholarService.searchScholars(null, null, 3, 2);
        assertEquals(0, page3.size());
    }
}
