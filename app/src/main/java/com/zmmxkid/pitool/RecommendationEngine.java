package com.zmmxkid.pitool;

import java.util.*;

/**
 * 与 bestQuery.html 中推荐逻辑一致的离线实现：基于场景列表 + Bests 成绩生成推荐。
 */
public final class RecommendationEngine {

    public static final int RECOMMENDATIONS_COUNT = 10;

    private RecommendationEngine() {
    }

    public static String normalizeScenarioId(String id) {
        if (id == null) return "";
        try {
            return String.valueOf(Integer.parseInt(id.trim()));
        } catch (NumberFormatException e) {
            return id.trim();
        }
    }

    public static ScenarioInfo parseScenarioLine(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] data = line.split(",");
        if (data.length < 7) return null;
        try {
            ScenarioInfo s = new ScenarioInfo();
            s.idRaw = data[0].trim();
            s.id = normalizeScenarioId(s.idRaw);
            s.name = data[1].trim();
            s.beilv = data.length > 2 ? data[2].trim() : "";
            s.type = data[3].trim();
            s.level = data[4].trim();
            s.constantDisplay = Float.parseFloat(data[5].trim());
            s.constantStored = Math.round(s.constantDisplay * 10f);
            s.author = data[6].trim();
            s.featureLine = data.length > 7 ? data[7].trim() : "";
            s.pack = data.length > 8 ? data[8].trim() : "";
            return s;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<ScenarioInfo> parseScenarioList(String text) {
        List<ScenarioInfo> list = new ArrayList<>();
        if (text == null) return list;
        for (String line : text.split("\n")) {
            ScenarioInfo s = parseScenarioLine(line);
            if (s != null) list.add(s);
        }
        return list;
    }

    public static List<PlayerScoreRow> parseBestsContent(String content) {
        List<PlayerScoreRow> rows = new ArrayList<>();
        if (content == null || content.trim().isEmpty()) return rows;
        String[] lines = content.split("\n");
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            String[] row = line.split(",");
            if (row.length < 7) continue;
            try {
                PlayerScoreRow r = new PlayerScoreRow();
                r.scenarioId = normalizeScenarioId(row[1].trim());
                r.name = row[2].trim();
                r.score = Integer.parseInt(row[3].trim().replace(",", ""));
                r.type = row[4].trim();
                r.level = row[5].trim();
                r.ptt = Float.parseFloat(row[6].trim());
                rows.add(r);
            } catch (Exception ignored) {
            }
        }
        return rows;
    }

    public static class ScenarioInfo {
        String idRaw;
        String id;
        String name;
        String beilv;
        String type;
        String level;
        float constantDisplay;
        int constantStored;
        String author;
        String featureLine;
        String pack;
    }

    public static class PlayerScoreRow {
        String scenarioId;
        String name;
        int score;
        String type;
        String level;
        float ptt;
    }

    public static class PlayerProfile {
        float averageRating;
        final Set<String> playedScenarios = new HashSet<>();
        final Map<String, Integer> preferredTypes = new HashMap<>();
        final Map<String, Integer> preferredFeatures = new HashMap<>();
        final Map<String, Integer> preferredAuthors = new HashMap<>();
    }

    public static class RecommendationItem {
        public final ScenarioInfo scenario;
        public final String category;
        public final String reason;
        public final String predictedLine;

        RecommendationItem(ScenarioInfo scenario, String category, String reason, String predictedLine) {
            this.scenario = scenario;
            this.category = category;
            this.reason = reason;
            this.predictedLine = predictedLine;
        }
    }

    public static PlayerProfile analyzePlayerData(
            List<PlayerScoreRow> scoreRows,
            Map<String, ScenarioInfo> scenarioById) {
        PlayerProfile p = new PlayerProfile();
        List<Float> validPtTs = new ArrayList<>();

        for (PlayerScoreRow row : scoreRows) {
            if (row.score > 0) {
                p.playedScenarios.add(row.scenarioId);
            }
            if (row.score > 50000) {
                validPtTs.add(row.ptt);
                ScenarioInfo sc = scenarioById.get(row.scenarioId);
                if (sc != null) {
                    String t = sc.type;
                    p.preferredTypes.put(t, p.preferredTypes.getOrDefault(t, 0) + 1);
                    if (sc.author != null && !sc.author.isEmpty()) {
                        p.preferredAuthors.put(sc.author, p.preferredAuthors.getOrDefault(sc.author, 0) + 1);
                    }
                    for (String f : splitFeatures(sc.featureLine)) {
                        if (!f.isEmpty()) {
                            p.preferredFeatures.put(f, p.preferredFeatures.getOrDefault(f, 0) + 1);
                        }
                    }
                }
            }
        }

        if (!validPtTs.isEmpty()) {
            validPtTs.sort((a, b) -> Float.compare(b, a));
            int n = Math.min(45, validPtTs.size());
            float sum = 0;
            for (int i = 0; i < n; i++) {
                sum += validPtTs.get(i);
            }
            p.averageRating = sum / n - 1.5f;
        }
        return p;
    }

    private static List<String> splitFeatures(String line) {
        if (line == null || line.isEmpty()) return Collections.emptyList();
        String[] parts = line.split("[,，、]");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isEmpty() && !"null".equalsIgnoreCase(t) && !"[null]".equalsIgnoreCase(t)) {
                out.add(t);
            }
        }
        return out;
    }

    public static List<RecommendationItem> generate(
            List<ScenarioInfo> allScenarios,
            List<PlayerScoreRow> scoreRows,
            Map<String, ScenarioInfo> scenarioById,
            Random random) {

        PlayerProfile profile = analyzePlayerData(scoreRows, scenarioById);

        // 与 bestQuery.html 一致：averageRating 为 0 时用入门随机推荐
        if (Math.abs(profile.averageRating) < 1e-5f) {
            return getBasicRecommendations(allScenarios, random);
        }

        Map<String, PlayerScoreRow> scoreById = new HashMap<>();
        for (PlayerScoreRow r : scoreRows) {
            scoreById.put(r.scenarioId, r);
        }

        String favoriteType = topKey(profile.preferredTypes);
        String favoriteFeature = topKey(profile.preferredFeatures);
        String favoriteAuthor = topKey(profile.preferredAuthors);

        List<Candidate> candidateScores = new ArrayList<>();
        for (ScenarioInfo scenario : allScenarios) {
            PlayerScoreRow existing = scoreById.get(scenario.id);
            if (existing != null && existing.score >= 75000) continue;

            float score = 0;
            float levelDiff = Math.abs(scenario.constantDisplay - profile.averageRating);
            if (levelDiff < 0.5f) score += 30;
            else if (levelDiff < 1.0f) score += 25;
            else if (levelDiff < 1.5f) score += 15;
            else if (levelDiff < 2.0f) score += 5;

            if (favoriteType != null && favoriteType.equals(scenario.type)) score += 15;
            if (favoriteFeature != null && scenario.featureLine != null) {
                String fl = scenario.featureLine.toLowerCase(Locale.ROOT);
                if (fl.contains(favoriteFeature.toLowerCase(Locale.ROOT))) score += 10;
            }
            if (favoriteAuthor != null && favoriteAuthor.equals(scenario.author)) score += 10;
            if (!profile.playedScenarios.contains(scenario.id)) score += 20;
            score += random.nextFloat() * 20f;

            candidateScores.add(new Candidate(scenario, score));
        }

        candidateScores.sort((a, b) -> Float.compare(b.score, a.score));

        List<RecommendationItem> recommendations = new ArrayList<>();
        Set<String> selectedIds = new LinkedHashSet<>();

        addRec(recommendations, selectedIds, candidateScores, profile, random, "提升挑战",
                c -> c.scenario.constantDisplay > profile.averageRating, 2);
        addRec(recommendations, selectedIds, candidateScores, profile, random, "巩固训练",
                c -> Math.abs(c.scenario.constantDisplay - profile.averageRating) < 0.3f, 2);
        if (favoriteType != null) {
            addRec(recommendations, selectedIds, candidateScores, profile, random, "类型喜好",
                    c -> favoriteType.equals(c.scenario.type), 2);
        }
        if (favoriteFeature != null) {
            addRec(recommendations, selectedIds, candidateScores, profile, random, "特性喜好", c -> {
                if (c.scenario.featureLine == null) return false;
                String fl = c.scenario.featureLine.toLowerCase(Locale.ROOT);
                return fl.contains(favoriteFeature.toLowerCase(Locale.ROOT));
            }, 1);
        }
        if (favoriteAuthor != null) {
            addRec(recommendations, selectedIds, candidateScores, profile, random, "作者精选",
                    c -> favoriteAuthor.equals(c.scenario.author), 1);
        }
        addRec(recommendations, selectedIds, candidateScores, profile, random, "全新体验",
                c -> !profile.playedScenarios.contains(c.scenario.id), 1);

        while (recommendations.size() < RECOMMENDATIONS_COUNT) {
            Candidate cand = null;
            for (Candidate c : candidateScores) {
                if (!selectedIds.contains(c.scenario.id)) {
                    cand = c;
                    break;
                }
            }
            if (cand == null) break;
            selectedIds.add(cand.scenario.id);
            recommendations.add(wrap(cand.scenario, "个性推荐", profile, random));
        }

        return recommendations;
    }

    private static String topKey(Map<String, Integer> map) {
        if (map == null || map.isEmpty()) return null;
        String best = null;
        int bestV = Integer.MIN_VALUE;
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            if (e.getValue() > bestV) {
                bestV = e.getValue();
                best = e.getKey();
            }
        }
        return best;
    }

    private static void addRec(
            List<RecommendationItem> out,
            Set<String> selectedIds,
            List<Candidate> candidates,
            PlayerProfile profile,
            Random random,
            String category,
            java.util.function.Predicate<Candidate> filter,
            int count) {
        for (int i = 0; i < count; i++) {
            if (!addDiverseRecommendation(out, selectedIds, candidates, profile, random, category, filter)) break;
        }
    }

    private static boolean addDiverseRecommendation(
            List<RecommendationItem> recommendations,
            Set<String> selectedIds,
            List<Candidate> candidates,
            PlayerProfile profile,
            Random random,
            String category,
            java.util.function.Predicate<Candidate> filterFn) {
        for (Candidate c : candidates) {
            if (!selectedIds.contains(c.scenario.id) && filterFn.test(c)) {
                recommendations.add(wrap(c.scenario, category, profile, random));
                selectedIds.add(c.scenario.id);
                return true;
            }
        }
        return false;
    }

    private static RecommendationItem wrap(ScenarioInfo scenario, String category, PlayerProfile profile, Random random) {
        String reason = getReason(scenario, category);
        String predicted = "预估您有" + getScorePrediction(scenario, profile, random) + "% 的几率获得 EX 或更高评级";
        return new RecommendationItem(scenario, category, reason, predicted);
    }

    private static String getReason(ScenarioInfo s, String category) {
        switch (category) {
            case "提升挑战":
                return "定数略高于您的水平，适合突破";
            case "巩固训练":
                return "定数接近您的单项水平，适合稳定发挥";
            case "类型喜好":
                return "与您常玩类型一致";
            case "特性喜好":
                return "与您偏好的场景特征相符";
            case "作者精选":
                return "您常玩该作者的作品";
            case "全新体验":
                return "您尚未游玩过该场景";
            case "个性推荐":
                return "综合匹配度较高";
            case "入门推荐":
                return "适合入门的官方类场景";
            default:
                return category;
        }
    }

    private static int getScorePrediction(ScenarioInfo scenario, PlayerProfile profile, Random random) {
        float avg = profile != null ? profile.averageRating : 0f;
        float levelDiff = avg - scenario.constantDisplay;
        int baseRate;
        if (levelDiff > 1.5f) baseRate = 90;
        else if (levelDiff > 0.5f) baseRate = 70;
        else if (levelDiff > -0.5f) baseRate = 50;
        else if (levelDiff > -1.5f) baseRate = 30;
        else baseRate = 10;
        return Math.min(99, Math.max(5, baseRate + random.nextInt(30) - 5));
    }

    private static List<RecommendationItem> getBasicRecommendations(List<ScenarioInfo> all, Random random) {
        List<ScenarioInfo> pool = new ArrayList<>();
        for (ScenarioInfo s : all) {
            if (s.constantStored >= 30 && s.constantStored <= 60
                    && ("PST".equals(s.type) || "PRS".equals(s.type))) {
                pool.add(s);
            }
        }
        if (pool.isEmpty()) {
            for (ScenarioInfo s : all) {
                if ("PST".equals(s.type) || "PRS".equals(s.type)) pool.add(s);
            }
        }
        List<RecommendationItem> out = new ArrayList<>();
        Set<String> sel = new HashSet<>();
        int guard = 0;
        while (out.size() < RECOMMENDATIONS_COUNT && !pool.isEmpty() && guard++ < 500) {
            ScenarioInfo s = pool.get(random.nextInt(pool.size()));
            if (sel.add(s.id)) {
                PlayerProfile dummy = new PlayerProfile();
                out.add(new RecommendationItem(s, "入门推荐",
                        getReason(s, "入门推荐"),
                        "预估您有" + getScorePrediction(s, dummy, random) + "% 的几率获得 EX 或更高评级"));
            }
        }
        return out;
    }

    private static class Candidate {
        final ScenarioInfo scenario;
        final float score;

        Candidate(ScenarioInfo scenario, float score) {
            this.scenario = scenario;
            this.score = score;
        }
    }
}
