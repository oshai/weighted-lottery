package com.wl.bingo;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class PartialSumsTree implements IntLotteryDelegator {
  private final double[] tree;
  private final double[] weights;
  private final boolean[] selected;
  private final int n;
  private final ThreadLocalRandom random;
  private int size;
  private double sum;

  public PartialSumsTree(final double[] weights, final boolean[] selected, final int size, final ThreadLocalRandom random) {
    this.n = weights.length;

    // We add a dummy element at index 0 to simplify child indexing.
    // We also make the tree full by adding a dummy last element in case #weights is even.
    this.tree = new double[n + 2 - n % 2];
    System.arraycopy(weights, 0, tree, 1, n);

    // We piggy back on the tree creation to verify the weights instead of using a dedicated loop.
    for (int i = n >>> 1; i > n >>> 2; i--) {
      final int left = i << 1;
      tree[i] = verify(tree[i]) + verify(tree[left]) + verify(tree[left + 1]);
    }

    // Same loop as above, but we need to verify only internal nodes.
    for (int i = n >>> 2; i > 0; i--) {
      final int left = i << 1;
      tree[i] = verify(tree[i]) + tree[left] + tree[left + 1];
    }
    sum = tree[1];

    this.random = random;
    this.size = size;
    this.weights = weights;
    this.selected = selected;
  }

  private double verify(final double weight) {
    // !(weight >= 0) covers both weight < 0 and Double.isNaN(weight)
    if (!(weight >= 0)) {
      throw new IllegalArgumentException("weights contain invalid weight: " + weight);
    }
    return weight;
  }

  public int draw() {
    double r = random.nextDouble(sum);
    int root = 1;
    while (root <= n >>> 1) {
      final int left = root << 1;
      if (r < tree[left]) {
        root = left;
      } else {
        r -= tree[left];
        final double rootWeight = tree[root] - tree[left] - tree[left + 1];
        if (r < rootWeight) {
          break;
        } else {
          r -= rootWeight;
          root = left + 1;
        }
      }
    }

    remove(root);
    return root - 1;
  }

  private void remove(int root) {
    final double rootWeight = weights[root - 1];
    selected[root - 1] = true;
    for (; root >= 1; root >>>= 1) {
      tree[root] -= rootWeight;
    }
    size--;
    sum -= rootWeight;
  }

  @Override
  public boolean empty() {
    return size == 0;
  }

  @Override
  public int remaining() {
    return size;
  }

  @Override
  public IntLotteryDelegator delegate() {
    if (sum > 0) {
      return this;
    }

    int numZeros = 0;
    int[] zeroWeightsIndexes = new int[n];

    for (int i = 0; i < n; i++) {
      if (!selected[i] && weights[i] == 0) {
        zeroWeightsIndexes[numZeros++] = i;
        if (numZeros == size) {
          return new UniformLottery(zeroWeightsIndexes, size, random);
        }
      }
    }

    // Can happen due to numeric instability issues.
    // For example if the weights are {1E+100, 1} then 1E+100 + 1 == 1E+100
    final double[] selectedWeightsZeroed = Arrays.copyOf(weights, n);
    for (int i = 0; i < n; i++) {
      if (selected[i]) {
        selectedWeightsZeroed[i] = 0;
      }
    }

    return new PartialSumsTree(selectedWeightsZeroed, selected, size, random);
  }
}
