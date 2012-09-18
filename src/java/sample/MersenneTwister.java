/*
 * Copyright (c) 2012 BigML, Inc
 * All rights reserved.
 * Author: Adam Ashenfelter <ashenfad@bigml.com>
 * Start date: Sept. 17, 2012
 */
package sample;

import cern.jet.random.tdouble.engine.MersenneTwister64;
import java.util.Random;

/**
 * A simple wrapper for MersenneTwister64
 */
public class MersenneTwister extends Random {

  public MersenneTwister(int seed) {
    _twister = new MersenneTwister64(seed);
  }
  
  @Override
  public double nextDouble() {
    return _twister.nextDouble();
  }

  @Override
  public float nextFloat() {
    return _twister.nextFloat();
  }

  @Override
  public int nextInt() {
    return _twister.nextInt();
  }

  @Override
  public int nextInt(int i) {
    return (int) (i * _twister.nextDouble());
  }

  @Override
  public long nextLong() {
    return _twister.nextLong();
  }

  @Override
  public boolean nextBoolean() {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public void nextBytes(byte[] bytes) {
    throw new UnsupportedOperationException("Not supported.");
  }

  @Override
  public synchronized double nextGaussian() {
    throw new UnsupportedOperationException("Not supported.");
  }
  
  @Override
  public synchronized void setSeed(long l) {
  }
  
  private MersenneTwister64 _twister;
}
