package it.units.erallab.hmsrobots.core.controllers.snn.learning;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.units.erallab.hmsrobots.util.Parametrized;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.IntStream;

public abstract class STDPLearningRule implements Parametrized, Serializable {

  @JsonProperty
  protected double aPlus;
  @JsonProperty
  protected double aMinus;

  public STDPLearningRule() {
  }

  @JsonCreator
  public STDPLearningRule(
      @JsonProperty("aPlus") double aPlus,
      @JsonProperty("aMinus") double aMinus
  ) {
    this.aPlus = aPlus;
    this.aMinus = aMinus;
  }

  private static STDPLearningRule createFromNumbers(double[] parameters) {
    if (parameters.length != 6) {
      throw new IllegalArgumentException(String.format("Expected 6 parameters, received %d", parameters.length));
    }
    STDPLearningRule stdpLearningRule;
    // first parameter regulates symmetry, second regulates hebbian/antihebbian
    if (parameters[0] > 0 && parameters[1] > 0) {
      stdpLearningRule = new SymmetricHebbianLearningRule();
    } else if (parameters[0] > 0 && parameters[1] < 0) {
      stdpLearningRule = new SymmetricAntiHebbianLearningRule();
    } else if (parameters[0] < 0 && parameters[1] > 0) {
      stdpLearningRule = new AsymmetricHebbianLearningRule();
    } else {
      stdpLearningRule = new AsymmetricAntiHebbianLearningRule();
    }
    stdpLearningRule.setParams(Arrays.copyOfRange(parameters, 2, parameters.length));
    return stdpLearningRule;
  }

  public static STDPLearningRule[] createLearningRules(double[][] params){
    STDPLearningRule[] stdpLearningRules = new STDPLearningRule[params.length];
    IntStream.range(0, stdpLearningRules.length).forEach(i -> stdpLearningRules[i] = STDPLearningRule.createFromNumbers(params[i]));
    return stdpLearningRules;
  }

  public abstract double computeDeltaW(double deltaT);

}
