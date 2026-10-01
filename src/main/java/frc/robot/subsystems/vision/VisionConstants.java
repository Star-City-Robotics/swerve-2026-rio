// Copyright (c) 2021-2026 Littleton Robotics
// http://github.com/Mechanical-Advantage
//
// Use of this source code is governed by a BSD
// license that can be found in the LICENSE file
// at the root directory of this project.

package frc.robot.subsystems.vision;

import static edu.wpi.first.math.util.Units.degreesToRadians;
import static edu.wpi.first.math.util.Units.inchesToMeters;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.apriltag.AprilTagFields;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;

public class VisionConstants {
  // AprilTag layout
  public static AprilTagFieldLayout aprilTagLayout =
      AprilTagFieldLayout.loadField(AprilTagFields.k2026RebuiltAndymark);

  // Camera names, must match names configured on coprocessor
  public static String camera0Name = "RGB1_OV9782 (TopLeft)";
  public static String camera1Name = "RGB2_OV9782 (BottomLeft)";

  // Robot to camera transforms
  // (Not used by Limelight, configure in web UI instead)

  // Cam @ Kicker
  public static Transform3d robotToCamera0 =
      new Transform3d(
          inchesToMeters(-3 + (1.0 / 8.0)),
          inchesToMeters(13 + (3.0 / 4.0)),
          inchesToMeters(8.5),
          new Rotation3d(0.0, degreesToRadians(-15), degreesToRadians(90)));
  public static Transform3d robotToCamera1 =
      new Transform3d(
          inchesToMeters(-7 + (2.5 / 8.0)),
          inchesToMeters(-13 + (1.5 / 8.0)),
          inchesToMeters(9.5),
          new Rotation3d(0.0, degreesToRadians(-15), degreesToRadians(-90)));

  // Basic filtering thresholds
  public static double maxAmbiguity = 0.3;
  public static double maxZError = 0.75;

  // Standard deviation baselines, for 1 meter distance and 1 tag
  // (Adjusted automatically based on distance and # of tags)
  public static double linearStdDevBaseline = 0.02; // Meters
  public static double angularStdDevBaseline = 0.06; // Radians

  // Standard deviation multipliers for each camera
  // (Adjust to trust some cameras more than others)
  public static double[] cameraStdDevFactors =
      new double[] {
        1.0, // Camera 0
        1.0 // Camera 1
      };

  // Multipliers to apply for MegaTag 2 observations
  public static double linearStdDevMegatag2Factor = 0.5; // More stable than full 3D solve
  public static double angularStdDevMegatag2Factor =
      Double.POSITIVE_INFINITY; // No rotation data available
}
