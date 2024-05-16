package com.utils;

import java.util.Random;

public class Point24Generator {

    Random random;

    public Point24Generator() {
        random = new Random();
    }

    private int[] generate() {
        int[] nums = new int[4];
        for (int i = 0; i < 4; i++) {
            nums[i] = random.nextInt(13) + 1;
        }
        return nums;
    }

    public int[] generateCards() {
        Point24Judge judge = new Point24Judge();
        int[] nums = generate();
        while (!judge.isSolvable(nums)) {
            nums = generate();
        }
        return nums;
    }
}