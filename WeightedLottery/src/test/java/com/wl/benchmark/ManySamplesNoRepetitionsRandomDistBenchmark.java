package com.wl.benchmark;

import com.wl.SimpleIntWeightedLotteryNoRepetitions;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ManySamplesNoRepetitionsRandomDistBenchmark {

  @Benchmark
  public void simple_05() {
    WeightedLotteryBenchmark.INSTANCE.mTimesDrawKTimes(true, weights -> new SimpleIntWeightedLotteryNoRepetitions(5, weights, 0.5, ThreadLocalRandom::current));
  }

  @Benchmark
  public void simple_07() {
    WeightedLotteryBenchmark.INSTANCE.mTimesDrawKTimes(true, weights -> new SimpleIntWeightedLotteryNoRepetitions(5, weights, 0.7, ThreadLocalRandom::current));
  }

  @Benchmark
  public void simple_09() {
    WeightedLotteryBenchmark.INSTANCE.mTimesDrawKTimes(true, weights -> new SimpleIntWeightedLotteryNoRepetitions(5, weights, 0.9, ThreadLocalRandom::current));
  }
}