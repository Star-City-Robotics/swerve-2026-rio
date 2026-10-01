// Copyright (c) 2025-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by an MIT-style
// license that can be found in the LICENSE file at
// the root directory of this project.
package frc.robot.util;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.util.Color;
import java.util.Optional;
import org.littletonrobotics.junction.Logger;

public class MatchInfo {
  private static final double[] PHASE_TIMESTAMP = {130, 105, 80, 55, 30};

  private static final Color RED = new Color(255, 80, 80);
  private static final Color BLUE = new Color(80, 80, 255);
  private static final Color WHITE = new Color(255, 255, 255);

  public static void update() {
    boolean hubActive = isHubActive();
    double timeUntilSwitch = getTimeUntilNextSwitch();

    Logger.recordOutput("MatchInfo/HubIsActive", hubActive);
    Logger.recordOutput("MatchInfo/TimeUntilSwitch", timeUntilSwitch);
    Logger.recordOutput("MatchInfo/CurrentPhase", getCurrentPhase());
    Logger.recordOutput("MatchInfo/GameData", DriverStation.getGameSpecificMessage());
    Logger.recordOutput("MatchInfo/AutoWinner", getAutoWinner().toHexString());
  }

  public static boolean isHubActive() {
    Optional<DriverStation.Alliance> alliance = DriverStation.getAlliance();

    // No alliance = not enabled = no hub
    if (alliance.isEmpty()) return false;

    // Hub always active in auto
    if (DriverStation.isAutonomousEnabled()) return true;

    // Not teleop enabled = no hub
    if (!DriverStation.isTeleopEnabled()) return false;

    double matchTime = DriverStation.getMatchTime();
    String gameData = DriverStation.getGameSpecificMessage();

    // No game data yet, assume active (early teleop)
    if (gameData.isEmpty()) return true;

    boolean redInactiveFirst;
    switch (gameData.charAt(0)) {
      case 'R' -> redInactiveFirst = true;
      case 'B' -> redInactiveFirst = false;
      default -> {
        return true;
      } // corrupt data, assume active
    }

    // Shift 1 is active for blue if red won auto, or red if blue won auto
    boolean shift1Active =
        switch (alliance.get()) {
          case Red -> !redInactiveFirst;
          case Blue -> redInactiveFirst;
        };

    if (matchTime > 130) return true; // Transition shift
    else if (matchTime > 105) return shift1Active; // Shift 1
    else if (matchTime > 80) return !shift1Active; // Shift 2
    else if (matchTime > 55) return shift1Active; // Shift 3
    else if (matchTime > 30) return !shift1Active; // Shift 4
    else return true; // End game
  }

  public static Color getAutoWinner() {
    String gameData = DriverStation.getGameSpecificMessage();
    if (gameData.isEmpty()) return WHITE;
    switch (gameData.charAt(0)) {
      case 'R':
        return RED;
      case 'B':
        return BLUE;
      default:
        return WHITE;
    }
  }

  public static double getTimeUntilNextSwitch() {
    if (!DriverStation.isTeleopEnabled()) return -1;

    double matchTime = DriverStation.getMatchTime();

    for (double boundary : PHASE_TIMESTAMP) {
      if (matchTime > boundary) {
        return matchTime - boundary; // seconds until this boundary
      }
    }
    return 0; // end game, no more switches
  }

  public static String getCurrentPhase() {
    if (!DriverStation.isTeleopEnabled()) return "NOT_TELEOP";

    double matchTime = DriverStation.getMatchTime();

    if (matchTime > 130) return "TRANSITION";
    else if (matchTime > 105) return "SHIFT_1";
    else if (matchTime > 80) return "SHIFT_2";
    else if (matchTime > 55) return "SHIFT_3";
    else if (matchTime > 30) return "SHIFT_4";
    else return "END_GAME";
  }
}
