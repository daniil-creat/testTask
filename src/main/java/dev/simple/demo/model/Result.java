package dev.simple.demo.model;

import lombok.Data;

import java.util.List;

@Data
public class Result {
    private List<String> userLowTwentyAge;

    private long count;

    public Result(){};

    public Result(List<String> userLowTwentyAge, long count){
        this.count = count;
        this.userLowTwentyAge = userLowTwentyAge;
    }
}
