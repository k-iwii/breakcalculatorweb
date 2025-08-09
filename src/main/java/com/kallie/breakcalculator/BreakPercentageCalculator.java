package com.kallie.breakcalculator;

public class BreakPercentageCalculator {

    final int[] values = {0, 1, 2, 3};
    public int N, M; // N = number of rounds, M = minimum points to break
    private int[] result; // result[0] = cases in which the user's team breaks; result[1] = total cases

    public BreakPercentageCalculator(int N, int M) {
        this.N = N;
        this.M = M;
    }

    public double calculateBreakPercentage() {
        int[] combo = new int[N];
        generateMultisets(0, 0, combo, new int[]{0,0});
        return (result[1] == 0) ? 0.0 : (100.0 * result[0] / result[1]);
    }    

    private void generateMultisets(int startIndex, int depth, int[] combo, int[] dummy) {
        if (depth == 0) // first call: reset result array
            result = new int[]{0, 0};

        if (depth == N) {
            int sum = 0;
            for (int x : combo) sum += x;
            result[1]++; // total sets
            if (sum >= M) result[0]++;
            return;
        }

        for (int i = startIndex; i < values.length; i++) {
            combo[depth] = values[i];
            generateMultisets(i, depth + 1, combo, dummy);
        }
    }
}