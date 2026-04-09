import csv
import json
import os

# 1. 设置文件路径（根据你的实际路径修改）
csv_file_path = 'E:\hezhu\Luminary-Nav-1\Result\Final_scores.csv'
json_output_path = 'frontend/src/assets/data/scholars.json'

# 确保输出目录存在
os.makedirs(os.path.dirname(json_output_path), exist_ok=True)

scholars = []

# 2. 读取 CSV 并转换
with open(csv_file_path, encoding='utf-8') as f:
    # 如果你的 CSV 没有表头，可以用下面这种方式手动指定字段名
    reader = csv.reader(f)
    for row in reader:
        if not row: continue
        
        # 假设你的 CSV 格式正如你之前发我的：
        # ID, Name, Institution, Score1, Score2, Count, Metric1, Metric2, Metric3, FinalScore
        try:
            item = {
                "id": row[0],
                "name": row[1],
                "org": row[2],
                # 把中间的评分指标存成数组，方便以后画雷达图
                "metrics": [float(row[3]), float(row[4]), int(row[5]), 
                            float(row[6]), float(row[7]), float(row[8])],
                "final_score": float(row[9])
            }
            scholars.append(item)
        except (ValueError, IndexError):
            # 跳过表头或格式不对的行
            continue

# 3. 写入 JSON
with open(json_output_path, 'w', encoding='utf-8') as f:
    json.dump(scholars, f, ensure_ascii=False, indent=2)

print(f"成功转换 {len(scholars)} 条数据到 {json_output_path}")