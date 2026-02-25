package com.zmmxkid.pitool;

import android.graphics.Color;

public class SceneScoreItem {
    public String sceneName;
    public String iconUrl;  // 图标URL
    public long score;
    public int rank;
    public int totalPlayers;
    public String category;
    public int categoryIndex; // 分类内序号（从1开始）

    public SceneScoreItem(String sceneName, long score, int rank, int totalPlayers, String category, int categoryIndex) {
        this.sceneName = sceneName;
        this.score = score;
        this.rank = rank;
        this.totalPlayers = totalPlayers;
        this.category = category;
        this.categoryIndex = categoryIndex;
        // 构建图标URL（注意：如果场景名包含特殊字符可能需要URL编码）
        this.iconUrl = "http://47.93.57.125:521/WebAPI/icons/" + sceneName + ".png";
    }

    // 获取分类显示颜色
    public int getCategoryColor() {
        switch (category) {
            case "MainGame": return Color.parseColor("#FF4444"); // 红色
            case "Scenario": return Color.parseColor("#00C853"); // 绿色
            case "SpeedRun": return Color.parseColor("#FF9800"); // 橙色
            case "Cure": return Color.parseColor("#2196F3");     // 蓝色
            default: return Color.GRAY;
        }
    }

    // 获取分类显示名称
    public String getCategoryLabel() {
        switch (category) {
            case "MainGame": return "[主游戏]";
            case "Scenario": return "[剧情]";
            case "SpeedRun": return "[急速]";
            case "Cure": return "[解药]";
            default: return "[未知]";
        }
    }

    public String getPercentage() {
        if (totalPlayers <= 0) return "0.00";
        if (totalPlayers == 1) return "1.00";
        double percentage = 1 + (rank - 1) * (99.0 / (totalPlayers - 1));
        return String.format("%.2f", percentage);
    }

    public int getColorByPercentage() {
        double p = Double.parseDouble(getPercentage());
        if (p <= 1) return Color.parseColor("#FFD700");
        if (p <= 5) return Color.parseColor("#FF0000");
        if (p <= 10) return Color.parseColor("#FF6B00");
        if (p <= 20) return Color.parseColor("#FFA500");
        if (p <= 30) return Color.parseColor("#00FFFF");
        if (p <= 50) return Color.parseColor("#00CC44");
        if (p <= 70) return Color.parseColor("#8800BB");
        return Color.parseColor("#999999");
    }
}