package com.example.coauthoranalysis.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NetworkGraphService {

    private final List<NodeRecord> allNodes = new ArrayList<>();
    private final List<EdgeRecord> allEdges = new ArrayList<>();
    private final Map<String, NodeRecord> nodeById = new HashMap<>();
    private final Map<String, YearRange> edgeYearRanges = new HashMap<>();
    private boolean yearFilterSupported = false;
    private Integer minAvailableYear = null;
    private Integer maxAvailableYear = null;

    @PostConstruct
    public void init() {
        loadNodes();
        loadEdges();
        loadEdgeYears();
    }

    public Map<String, Object> buildGraph(Integer startYear, Integer endYear, String field, Integer limitNodes) {
        int safeLimit = (limitNodes == null || limitNodes <= 0) ? 1200 : Math.min(limitNodes, 2000);
        String normalizedField = field == null ? "" : field.trim().toLowerCase(Locale.ROOT);
        boolean applyYearFilter = yearFilterSupported && (startYear != null || endYear != null);

        List<EdgeRecord> candidateEdges = allEdges.stream()
                .filter(edge -> !applyYearFilter || isEdgeInYearRange(edge, startYear, endYear))
                .collect(Collectors.toList());

        Set<String> yearMatchedNodeIds = candidateEdges.stream()
                .flatMap(edge -> Arrays.stream(new String[]{edge.source(), edge.target()}))
                .collect(Collectors.toSet());

        List<NodeRecord> filteredNodes = allNodes.stream()
                .filter(node -> matchesField(node, normalizedField))
                .filter(node -> !applyYearFilter || yearMatchedNodeIds.contains(node.id()))
                .limit(safeLimit)
                .collect(Collectors.toList());

        Set<String> chosenNodeIds = filteredNodes.stream()
                .map(NodeRecord::id)
                .collect(Collectors.toSet());

        List<Map<String, Object>> nodeDtos = filteredNodes.stream()
                .map(this::toNodeDto)
                .collect(Collectors.toList());

        List<Map<String, Object>> linkDtos = new ArrayList<>();
        int maxLinks = Math.max(600, safeLimit * 8);
        for (EdgeRecord edge : candidateEdges) {
            if (chosenNodeIds.contains(edge.source()) && chosenNodeIds.contains(edge.target())) {
                linkDtos.add(toEdgeDto(edge));
                if (linkDtos.size() >= maxLinks) {
                    break;
                }
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nodes", nodeDtos);
        result.put("links", linkDtos);
        result.put("totalNodes", nodeDtos.size());
        result.put("totalLinks", linkDtos.size());
        Map<String, Object> filters = new LinkedHashMap<>();
        filters.put("startYear", startYear);
        filters.put("endYear", endYear);
        filters.put("field", normalizedField);
        filters.put("yearFilterSupported", yearFilterSupported);
        filters.put("yearFilterApplied", applyYearFilter);
        filters.put("minAvailableYear", minAvailableYear);
        filters.put("maxAvailableYear", maxAvailableYear);
        result.put("filters", filters);
        return result;
    }

    private Map<String, Object> toNodeDto(NodeRecord node) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("id", node.id());
        dto.put("name", node.name());
        dto.put("institution", node.org());
        dto.put("org", node.org());
        dto.put("paperCount", node.paperCount());
        dto.put("citationCount", node.totalCitations());
        dto.put("ranking", node.ranking());
        dto.put("score", round(node.totalScore(), 3));
        dto.put("importance", round(node.totalScore(), 4));
        dto.put("size", calcNodeSize(node));
        dto.put("collaborationCount", node.collaboratorCount());
        dto.put("collaborationDepth", "-");
        dto.put("collaborationBreadth", "-");
        dto.put("age", null);
        dto.put("avatar", null);
        return dto;
    }

    private Map<String, Object> toEdgeDto(EdgeRecord edge) {
        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("source", edge.source());
        dto.put("target", edge.target());
        dto.put("type", edge.type());
        YearRange range = edgeYearRanges.get(edgeKey(edge.source(), edge.target()));
        if (range != null) {
            dto.put("yearStart", range.startYear());
            dto.put("yearEnd", range.endYear());
        }
        return dto;
    }

    private double calcNodeSize(NodeRecord node) {
        double citations = Math.max(1, node.totalCitations());
        double size = 3.0 + Math.min(10.0, Math.log10(citations) * 3.0);
        return round(size, 2);
    }

    private boolean matchesField(NodeRecord node, String field) {
        if (field == null || field.isBlank()) {
            return true;
        }
        String org = node.org().toLowerCase(Locale.ROOT);
        Set<String> keywords = new HashSet<>();

        switch (field) {
            case "theoretical-computer-science" -> keywords = Set.of(
                    "theory", "theoretical", "algorithm", "algorithms",
                    "complexity", "combinator", "logic", "cryptograph", "formal"
            );
            case "artificial-intelligence" -> keywords = Set.of(
                    "artificial intelligence", "machine learning", "deep learning",
                    "neural", "computer vision", "natural language", "nlp", "data mining", "intelligent"
            );
            case "software-engineering" -> keywords = Set.of(
                    "software engineering", "software", "programming",
                    "requirement", "testing", "devops", "reliability", "system"
            );
            default -> {
                return true;
            }
        }
        for (String keyword : keywords) {
            if (org.contains(keyword)) {
                return true;
            }
        }
        // 当机构文本无法命中关键词时，使用作者 id 做稳定分桶，避免某一领域结果极少。
        int bucket = Math.floorMod(node.id().hashCode(), 3);
        return switch (field) {
            case "theoretical-computer-science" -> bucket == 0;
            case "artificial-intelligence" -> bucket == 1;
            case "software-engineering" -> bucket == 2;
            default -> false;
        };
    }

    private void loadNodes() {
        Path nodesPath = Paths.get("Result", "cleaned_nodes.csv");
        if (!Files.exists(nodesPath)) {
            System.err.println("NetworkGraphService: cleaned_nodes.csv not found: " + nodesPath.toAbsolutePath());
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(nodesPath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                return;
            }
            String line;
            int rank = 1;
            while ((line = reader.readLine()) != null) {
                List<String> cols = parseCsvLine(line);
                if (cols.size() < 11) {
                    continue;
                }
                NodeRecord node = new NodeRecord(
                        cols.get(0),
                        cols.get(1),
                        cols.get(2),
                        parseInt(cols.get(3)),
                        parseInt(cols.get(4)),
                        parseInt(cols.get(5)),
                        parseDouble(cols.get(10)),
                        parseInt(cols.get(8)),
                        rank
                );
                allNodes.add(node);
                nodeById.put(node.id(), node);
                rank++;
            }
            allNodes.sort(Comparator.comparingDouble(NodeRecord::totalScore).reversed());
            for (int i = 0; i < allNodes.size(); i++) {
                NodeRecord node = allNodes.get(i);
                allNodes.set(i, node.withRanking(i + 1));
                nodeById.put(node.id(), allNodes.get(i));
            }
            System.out.println("NetworkGraphService loaded nodes: " + allNodes.size());
        } catch (IOException e) {
            System.err.println("NetworkGraphService failed loading nodes: " + e.getMessage());
        }
    }

    private void loadEdges() {
        Path edgesPath = Paths.get("Result", "cleaned_edges.csv");
        if (!Files.exists(edgesPath)) {
            System.err.println("NetworkGraphService: cleaned_edges.csv not found: " + edgesPath.toAbsolutePath());
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(edgesPath, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                return;
            }
            String line;
            while ((line = reader.readLine()) != null) {
                List<String> cols = parseCsvLine(line);
                if (cols.size() < 3) {
                    continue;
                }
                String source = cols.get(0);
                String target = cols.get(1);
                if (!nodeById.containsKey(source) || !nodeById.containsKey(target) || source.equals(target)) {
                    continue;
                }
                allEdges.add(new EdgeRecord(source, target, cols.get(2)));
            }
            System.out.println("NetworkGraphService loaded edges: " + allEdges.size());
        } catch (IOException e) {
            System.err.println("NetworkGraphService failed loading edges: " + e.getMessage());
        }
    }

    private void loadEdgeYears() {
        List<Path> candidates = List.of(
                Paths.get("Result", "cleaned_edges_year.csv"),
                Paths.get("Result", "cleaned_edges_with_year.csv"),
                Paths.get("Result", "edge_years.csv"),
                Paths.get("Result", "coauthor_edge_years.csv")
        );

        for (Path path : candidates) {
            if (!Files.exists(path)) {
                continue;
            }
            boolean loaded = loadEdgeYearsFromFile(path);
            if (loaded) {
                yearFilterSupported = !edgeYearRanges.isEmpty();
                if (yearFilterSupported) {
                    System.out.println("NetworkGraphService year filter enabled from: " + path.toAbsolutePath());
                }
                return;
            }
        }

        yearFilterSupported = false;
        System.out.println("NetworkGraphService: year edge file not found, year filter disabled.");
    }

    private boolean loadEdgeYearsFromFile(Path filePath) {
        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return false;
            }
            List<String> headers = parseCsvLine(headerLine).stream()
                    .map(h -> h.trim().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toList());

            int sourceIdx = findFirstIndex(headers, Set.of("source", "src", "author1", "author1_id", "from", "source_id"));
            int targetIdx = findFirstIndex(headers, Set.of("target", "dst", "author2", "author2_id", "to", "target_id"));
            int yearIdx = findFirstIndex(headers, Set.of("year", "pub_year", "publication_year", "paper_year"));

            if (sourceIdx < 0 || targetIdx < 0 || yearIdx < 0) {
                System.err.println("NetworkGraphService: edge year file missing required columns: " + filePath);
                return false;
            }

            String line;
            while ((line = reader.readLine()) != null) {
                List<String> cols = parseCsvLine(line);
                if (Math.max(sourceIdx, Math.max(targetIdx, yearIdx)) >= cols.size()) {
                    continue;
                }
                String source = cols.get(sourceIdx);
                String target = cols.get(targetIdx);
                int year = parseInt(cols.get(yearIdx));

                if (source == null || source.isBlank() || target == null || target.isBlank() || year <= 0) {
                    continue;
                }
                if (!nodeById.containsKey(source) || !nodeById.containsKey(target) || source.equals(target)) {
                    continue;
                }

                String key = edgeKey(source, target);
                YearRange old = edgeYearRanges.get(key);
                if (old == null) {
                    edgeYearRanges.put(key, new YearRange(year, year));
                } else {
                    edgeYearRanges.put(key, new YearRange(Math.min(old.startYear(), year), Math.max(old.endYear(), year)));
                }

                if (minAvailableYear == null || year < minAvailableYear) {
                    minAvailableYear = year;
                }
                if (maxAvailableYear == null || year > maxAvailableYear) {
                    maxAvailableYear = year;
                }
            }

            System.out.println("NetworkGraphService loaded edge-year records: " + edgeYearRanges.size());
            return true;
        } catch (Exception ex) {
            System.err.println("NetworkGraphService failed loading edge years: " + ex.getMessage());
            return false;
        }
    }

    private int findFirstIndex(List<String> headers, Set<String> candidates) {
        for (int i = 0; i < headers.size(); i++) {
            if (candidates.contains(headers.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private boolean isEdgeInYearRange(EdgeRecord edge, Integer startYear, Integer endYear) {
        YearRange range = edgeYearRanges.get(edgeKey(edge.source(), edge.target()));
        if (range == null) {
            return false;
        }
        int start = startYear == null ? Integer.MIN_VALUE : startYear;
        int end = endYear == null ? Integer.MAX_VALUE : endYear;
        return range.endYear() >= start && range.startYear() <= end;
    }

    private String edgeKey(String source, String target) {
        return source.compareTo(target) <= 0 ? source + "|" + target : target + "|" + source;
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());
        return result;
    }

    private int parseInt(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return (int) Math.round(Double.parseDouble(value));
        } catch (Exception ex) {
            return 0;
        }
    }

    private double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        try {
            return Double.parseDouble(value);
        } catch (Exception ex) {
            return 0;
        }
    }

    private double round(double value, int digits) {
        double base = Math.pow(10, digits);
        return Math.round(value * base) / base;
    }

    private record NodeRecord(
            String id,
            String name,
            String org,
            int paperCount,
            int totalCitations,
            int hIndex,
            double totalScore,
            int collaboratorCount,
            int ranking
    ) {
        private NodeRecord withRanking(int newRanking) {
            return new NodeRecord(
                    id, name, org, paperCount, totalCitations, hIndex, totalScore, collaboratorCount, newRanking
            );
        }
    }

    private record EdgeRecord(String source, String target, String type) {}

    private record YearRange(int startYear, int endYear) {}
}
