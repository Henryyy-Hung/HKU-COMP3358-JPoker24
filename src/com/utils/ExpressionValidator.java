package com.utils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExpressionValidator {

    public ExpressionValidator() {
    }

    public boolean allNumbersUsed(String expression, int[] nums) {
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(expression);
        List<Integer> numbersList = new ArrayList<>();
        while (matcher.find()) {
            numbersList.add(Integer.parseInt(matcher.group()));
        }
        int[] extractedNums = numbersList.stream().mapToInt(i -> i).toArray();
        if (extractedNums.length != nums.length) {
            return false;
        }
        Arrays.sort(extractedNums);
        Arrays.sort(nums);
        for (int i = 0; i < extractedNums.length; i++) {
            if (extractedNums[i] != nums[i]) {
                return false;
            }
        }
        return true;
    }
    
}