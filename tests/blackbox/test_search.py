import requests
import pytest
import time

# 基础测试 URL
BASE_URL = "http://localhost:8080/api/rankings/search"

class TestScholarSearchAPI:
    """
    针对导航搜索输入框功能的黑盒测试
    使用等价类划分与边界值分析设计测试用例
    """

    def test_valid_input_name(self):
        """测试目的：验证有效姓名输入是否能正确返回结果"""
        params = {"name": "Li"}
        start_time = time.time()
        response = requests.get(BASE_URL, params=params)
        end_time = time.time()
        
        assert response.status_code == 200, "接口状态码应为 200"
        assert end_time - start_time < 1.0, "接口响应时间应在 1 秒内"
        
        data = response.json()
        assert isinstance(data, list), "返回数据应为列表"
        # 如果有数据，验证名字是否包含 Li
        if data:
            assert any("li" in scholar["name"].lower() for scholar in data), "返回结果中应包含输入的姓名"

    def test_valid_input_org(self):
        """测试目的：验证有效机构输入是否能正确返回结果"""
        params = {"org": "University"}
        response = requests.get(BASE_URL, params=params)
        assert response.status_code == 200
        data = response.json()
        if data:
            assert any("university" in scholar["org"].lower() for scholar in data), "返回结果中应包含输入的机构"

    def test_invalid_input(self):
        """测试目的：验证数据库中不存在的输入是否返回空列表（无数据）"""
        params = {"name": "XxxYyyZzzUnknownScholar123"}
        response = requests.get(BASE_URL, params=params)
        assert response.status_code == 200
        data = response.json()
        assert len(data) == 0, "不存在的数据应返回空列表"

    def test_boundary_value_pagination(self):
        """测试目的：验证分页边界值（例如请求第 1 页和极大的页码）"""
        # 测试有效边界
        params_valid = {"page": 1, "size": 1}
        res_valid = requests.get(BASE_URL, params=params_valid)
        assert res_valid.status_code == 200
        assert len(res_valid.json()) <= 1, "size=1 时最多返回 1 条数据"

        # 测试极大页码（越界）
        params_invalid = {"page": 999999, "size": 20}
        res_invalid = requests.get(BASE_URL, params=params_invalid)
        assert res_invalid.status_code == 200
        assert len(res_invalid.json()) == 0, "页码越界应返回空列表"

    def test_special_characters(self):
        """测试目的：验证包含特殊字符（防 SQL 注入或解析错误）的输入处理"""
        params = {"name": "' OR '1'='1", "org": "<script>alert(1)</script>"}
        response = requests.get(BASE_URL, params=params)
        assert response.status_code == 200, "接口应对特殊字符有良好兼容性，不应抛出 500"
        data = response.json()
        assert isinstance(data, list), "特殊字符应被正常当作字符串搜索，返回列表"

    def test_super_long_input(self):
        """测试目的：验证超长字符串输入的鲁棒性"""
        long_str = "a" * 5000
        params = {"name": long_str}
        response = requests.get(BASE_URL, params=params)
        # 根据服务器配置，可能会返回 200 空列表或 400 Bad Request
        assert response.status_code in [200, 400, 414], f"超长输入状态码异常: {response.status_code}"
        if response.status_code == 200:
            assert len(response.json()) == 0, "超长输入在正常情况下应匹配不到结果"
