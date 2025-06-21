package com.company.scratchgame;

import com.company.scratchgame.model.GameConfig;
import com.company.scratchgame.model.GameResult;
import com.company.scratchgame.service.GameService;
import com.company.scratchgame.service.MatrixService;
import com.company.scratchgame.util.JSONUtil;

public class App {
    public static void main(String[] args) {
        try {
            String fileName = null;
            Double bettingAmount = null;
            for (int i = 0; i < args.length; i++) {
                if ("--config".equals(args[i]) && i < args.length - 1) {
                    fileName = args[i + 1];
                }
                if ("--betting-amount".equals(args[i]) && i < args.length - 1) {
                    bettingAmount = Double.parseDouble(args[i + 1]);
                }
            }
            if (fileName == null) {
                System.out.println("File name should be present");
                return;
            }
            if (bettingAmount == null || bettingAmount == 0) {
                System.out.println("Betting amount should be present");
                return;
            }

            GameConfig config = JSONUtil.readFromFile(fileName, GameConfig.class);
            MatrixService matrixService = new MatrixService(config);
            GameService gameService = new GameService(config);

            String[][] matrix = matrixService.generateMatrix();
            GameResult gameResult = gameService.winOrLose(matrix, bettingAmount);
            System.out.println(JSONUtil.convertToJSON(gameResult));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
