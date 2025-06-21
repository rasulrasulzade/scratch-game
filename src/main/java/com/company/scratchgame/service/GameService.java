package com.company.scratchgame.service;

import com.company.scratchgame.model.GameResult;
import com.company.scratchgame.model.WinCombination;
import com.company.scratchgame.model.GameConfig;
import com.company.scratchgame.model.Symbol;

import java.util.*;

public class GameService {
    private final GameConfig config;

    public GameService(GameConfig config) {
        this.config = config;
    }


    public GameResult winOrLose(String[][] matrix, Double bettingAmount) {
        Map<Integer, List<String>> symbolCounts = getSymbolByCountMap(matrix);
        Map<String, List<String>> appliedWinningCombinations = new HashMap<>();

        for (Map.Entry<String, WinCombination> pair : config.getWinCombinations().entrySet()) {
            final WinCombination combination = pair.getValue();
            checkCoveringAreas(matrix, pair, appliedWinningCombinations);

            if ("same_symbols".equals(combination.getWhen())) {
                List<String> symbols = symbolCounts.get(combination.getCount());
                if (symbols == null) continue;

                for (String symbol : symbols) {
                    appliedWinningCombinations.computeIfAbsent(symbol, k -> new ArrayList<>())
                            .add(pair.getKey()); // combination name
                }
            }
        }

        double reward = 0;
        for (Map.Entry<String, List<String>> entry : appliedWinningCombinations.entrySet()) {
            String symbol = entry.getKey();
            double symbolReward = bettingAmount * config.getSymbols().get(symbol).getRewardMultiplier();
            for (String winCombName : entry.getValue()) {
                WinCombination winComb = config.getWinCombinations().get(winCombName);
                symbolReward *= winComb.getRewardMultiplier();
            }
            reward += symbolReward;
        }

        String appliedBonusSymbolName = null;
        AbstractMap.SimpleEntry<String, Symbol> bonusEntry = extractBonusSymbol(matrix, appliedWinningCombinations);
        if (bonusEntry != null) {
            appliedBonusSymbolName = bonusEntry.getKey();
            reward = applyBonus(reward, bonusEntry.getValue());
        }

        return new GameResult(matrix, reward, appliedWinningCombinations, appliedBonusSymbolName);
    }

    private AbstractMap.SimpleEntry<String, Symbol> extractBonusSymbol(String[][] matrix, Map<String, List<String>> appliedWinningCombinations) {
        if (!appliedWinningCombinations.isEmpty()) {
            Map<String, Symbol> symbolMap = config.getSymbols();
            for (String[] symbols : matrix) {
                for (int j = 0; j < matrix[0].length; j++) {
                    Symbol symbol = symbolMap.get(symbols[j]);
                    if ("bonus".equals(symbol.getType())) {
                        return new AbstractMap.SimpleEntry<>(symbols[j], symbol);
                    }
                }
            }
        }
        return null;
    }

    private void checkCoveringAreas(String[][] matrix,
                                    Map.Entry<String, WinCombination> pair,
                                    Map<String, List<String>> appliedWinningCombinations) {
        final String combinationName = pair.getKey();
        WinCombination combination = pair.getValue();

        if (combination.getCoveredAreas() != null) {
            for (List<String> area : combination.getCoveredAreas()) {
                String symbol = checkArea(matrix, area);
                if (symbol != null) {
                    List<String> list = appliedWinningCombinations.computeIfAbsent(symbol, k -> new ArrayList<>());
                    list.add(combinationName);
                }
            }
        }
    }

    private String checkArea(String[][] matrix, List<String> area) {
        String s = null;
        for (String point : area) {
            int x = Character.getNumericValue(point.charAt(0));
            int y = Character.getNumericValue(point.charAt(2));
            if (s == null) {
                s = matrix[x][y];
            } else if (!s.equals(matrix[x][y])) {
                return null;
            }
        }
        return s;
    }

    private Map<Integer, List<String>> getSymbolByCountMap(String[][] matrix) {
        Map<String, Integer> map = new HashMap<>();
        for (String[] symbols : matrix) {
            for (int j = 0; j < matrix[0].length; j++) {
                map.compute(symbols[j], (k, count) -> count == null ? 1 : count + 1);
            }
        }

        Map<Integer, List<String>> result = new HashMap<>();
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            result.computeIfAbsent(entry.getValue(), k -> new ArrayList<>()).add(entry.getKey());
        }
        return result;
    }

    public double applyBonus(Double reward, Symbol symbol) {
        if ("multiply_reward".equals(symbol.getImpact())) {
            return reward * symbol.getRewardMultiplier();
        } else if ("extra_bonus".equals(symbol.getImpact())) {
            return reward + symbol.getExtra();
        } else if ("miss".equals(symbol.getImpact())) {
            return reward;
        } else {
            return reward;
        }
    }
}
