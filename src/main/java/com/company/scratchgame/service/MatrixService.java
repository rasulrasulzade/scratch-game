package com.company.scratchgame.service;

import com.company.scratchgame.model.GameConfig;
import com.company.scratchgame.model.StandardSymbolProbability;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class MatrixService {
    private final GameConfig config;

    private static final Random random = new Random();

    public MatrixService(GameConfig config) {
        this.config = config;
    }

    public String[][] generateMatrix() {
        int rows = config.getRows();
        int columns = config.getColumns();
        String[][] matrix = new String[rows][columns];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                Map<String, Integer> symbolProb = getProbabilityForCell(r, c);
                matrix[r][c] =  getWeightedRandomSymbol(symbolProb);
            }
        }


        int numBonusToInsert = random.nextInt(2);
        if (numBonusToInsert == 1) {
            Map<String, Integer> bonusSymbolProbabilities = config.getProbabilities().getBonusSymbols().getSymbols();
            String bonusSymbol = getWeightedRandomSymbol(bonusSymbolProbabilities);
            matrix[random.nextInt(rows)][random.nextInt(columns)] = bonusSymbol;
        }

        return matrix;
    }

    public Map<String, Integer> getProbabilityForCell(int row, int column) {
        List<StandardSymbolProbability> symbolProbList = config.getProbabilities().getStandardSymbols();

        for (StandardSymbolProbability prob : symbolProbList) {
            if (prob.getRow() == row && prob.getColumn() == column) {
                return prob.getSymbols();
            }
        }

        if (!symbolProbList.isEmpty()) {
            return symbolProbList.get(0).getSymbols();
        }

        throw new RuntimeException("No probabilities for this symbol defined in config.");
    }

    public String getWeightedRandomSymbol(Map<String, Integer> symbolProbabilities) {
        int totalWeight = symbolProbabilities.values().stream().mapToInt(Integer::intValue).sum();
        int randomNumber = random.nextInt(totalWeight) + 1;

        int cumulative = 0;
        for (Map.Entry<String, Integer> entry : symbolProbabilities.entrySet()) {
            cumulative += entry.getValue();
            if (randomNumber <= cumulative) {
                return entry.getKey();
            }
        }

        throw new RuntimeException("Failed to pick symbol based on weight.");
    }

}
