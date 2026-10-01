// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;
import frc.robot.util.LoggedProfiler.LoggedProfilerConfig;
import frc.robot.util.LoggedProfiler.LoggedProfilerLevel;

/**
 * This class defines the runtime mode used by AdvantageKit. The mode is always "real" when running
 * on a roboRIO. Change the value of "simMode" to switch between "sim" (physics sim) and "replay"
 * (log replay from a file).
 */
public final class Constants {
  public static final Mode simMode = Mode.SIM;
  public static final Mode currentMode = RobotBase.isReal() ? Mode.REAL : simMode;

  public static enum Mode {
    /** Running on a real robot. */
    REAL,

    /** Running a physics simulator. */
    SIM,

    /** Replaying from a log file. */
    REPLAY
  }

  /**
   * Enable DataLogManager. AdvantageKit doesn't capture everything. This results in writing two
   * files, one with /AdvantageKit outputs, the other with everything that's visible to
   * NetworkTables. Capturing everything may not be the correct answer. Need to determine how REPLAY
   * works
   */
  public static final boolean loggingMode = true;

  /**
   * Enable DataLogManager even in simulation. This tests writing the same logs to the project
   * directory.
   */
  public static final boolean logInSim = false;

  // disabling diagnostics prevents PheonixTuner from functioning! and requires a rebuild/redeploy
  // to start it. only disable if necessary. May improve cycle time (reduce loop overruns)
  public static final boolean usePheonixDiagnostics = true;
  public static final boolean usePheonixLogging = false;

  // memory profiling
  public static final LoggedProfilerLevel kProfilerLevel =
      RobotBase.isReal() ? LoggedProfilerLevel.Regular : LoggedProfilerLevel.Robot;
  public static final LoggedProfilerConfig kProfilerConfig =
      new LoggedProfilerConfig(kProfilerLevel, 3.000, 2000.0);
}
