package org.mitre.synthea.engine;

import java.io.Serializable;
import java.util.HashMap;

import org.mitre.synthea.world.agents.Person;

/**
 * Representation of different types of distributions that can be used to represent
 * random variables used in Synthea States.
 */
public class Distribution implements Serializable {
  public enum Kind {
    EXACT, GAUSSIAN, UNIFORM, EXPONENTIAL
  }

  public Kind kind;
  public Boolean round;
  public HashMap<String, Double> parameters;

  /**
   * Generate a sample from the random variable.
   * @param person The place to obtain a repeatable source of randomness
   * @return The value
   */
  public double generate(Person person) {
    double value;
    switch (this.kind) {
      case EXACT:
        value = this.parameters.get("value");
        break;
      case UNIFORM:
        value = person.rand(this.parameters.get("low"), this.parameters.get("high"));
        break;
      case GAUSSIAN:
        value = (this.parameters.get("standardDeviation") * person.randGaussian())
            + this.parameters.get("mean");
        if (this.parameters.containsKey("min")) {
          double min = this.parameters.get("min");
          if (value < min) {
            value = min;
          }
        }
        if (this.parameters.containsKey("max")) {
          double max = this.parameters.get("max");
          if (value > max) {
            value = max;
          }
        }
        break;
      case EXPONENTIAL:
        double average = this.parameters.get("mean");
        double lambda = (1 / average);
        value = 1 + Math.log(1 - person.rand()) / (-1 * lambda);
        break;
      default:
        value = -1;
    }
    if (round != null && round.booleanValue()) {
      value = Math.round(value);
    }
    return value;
  }

  /**
   * Determine whether the Distribution has all of the information it needs to generate a sample
   * value.
   * @return True if it is valid, false otherwise
   */
  public boolean validate() {
    if (parameters == null) {
      return false;
    }
    switch (this.kind) {
      case EXACT:
        return this.parameters.containsKey("value");
      case UNIFORM:
        return this.parameters.containsKey("low") && this.parameters.containsKey("high");
      case GAUSSIAN:
        return this.parameters.containsKey("mean")
            && this.parameters.containsKey("standardDeviation");
      case EXPONENTIAL:
        return this.parameters.containsKey("mean");
      default:
        return false;
    }
  }
}
