package frc.robot.util;

import edu.wpi.first.hal.HALUtil;

public class HALTime {

  public static HALTime instance;

  public static HALTime getInstance() {
    if (instance == null) {
      instance = new HALTime();
    }
    return instance;
  }

  public static void updateTimer() {
    getInstance().update();
  }

  /**
   * Read only the elapsed time (does not update timer state)
   *
   * @return elapsed time in "radians"
   */
  public static double readElapsed() {
    return readElapsed(1.0);
  }

  /**
   * Read only the scaled elapsed time (does not update timer state)
   *
   * @param scale scale result
   * @return elapsed time in "radians"
   */
  public static double readElapsed(double scale) {
    var timer = getInstance();
    var micros = HALUtil.getFPGATime();
    var elapsedMicros = micros - timer.getInitial();
    var elapsed = ((double) elapsedMicros) / (1000000.0);

    return elapsed;
  }

  /**
   * Get elapsed time
   *
   * @return elapsed time in "radians"
   */
  public static double elapsed() {
    return elapsed(1.0);
  }

  /**
   * Scale the elapsed time
   *
   * @param scale a value to scale the elapsed time
   * @return scaled elapsed time in "radians"
   */
  public static double elapsed(double scale) {
    return scale * getInstance().elapsed;
  }

  /**
   * Get the elapsed time in "degrees"
   *
   * @return elapsed time in "degrees"
   */
  public static double elapsedDeg() {
    return elapsed(180 / Math.PI);
  }

  /**
   * Get the delta-t since the last timestep
   *
   * @return delta-t in "radians"
   */
  public static double dt() {
    return dt(1.0);
  }

  /**
   * Scale the delta-t since the last timestep
   *
   * @param scale a value to scale the delta-t
   * @return scaled delta-t in "radians"
   */
  public static double dt(double scale) {
    return scale * getInstance().dt;
  }

  /**
   * Get the delta-t time in "degrees"
   *
   * @return the scaled delta-t in "degrees"
   */
  public static double dtDeg() {
    return dt(180 / Math.PI);
  }

  private final long initial;
  private long elapsedMicros;
  private long prevMicros;
  private long dtMicros;
  private double dt;
  private double elapsed;

  public HALTime() {
    initial = HALUtil.getFPGATime();
    prevMicros = initial;
    dt = prevMicros;
  }

  /** Update the stored elapsed times and deltas */
  public void update() {
    var micros = HALUtil.getFPGATime();
    this.elapsedMicros = micros - initial;
    this.prevMicros = micros;
    this.elapsed = ((double) this.elapsedMicros) / (1000000.0);
    this.dtMicros = micros - prevMicros;
    this.dt = ((double) this.dtMicros) / (1000000.0);
  }

  public long getInitial() {
    return initial;
  }
}
