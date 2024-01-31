package frc.robot.subsystems;

import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.SwerveConstants;

public class SwerveSubsystem extends SubsystemBase {

  private final SwerveModule m_frontLeftSwerveModule = new SwerveModule(
    SwerveConstants.kFrontLeftDriveMotorPort,
    SwerveConstants.kFrontLeftDriveMotorReversed,
    SwerveConstants.kFrontLeftTurningMotorPort,
    SwerveConstants.kFrontLeftTurningMotorReversed,
    SwerveConstants.kFrontLeftCANcoderPort,
    SwerveConstants.kFrontLeftCANcoderDirection,
    SwerveConstants.kFrontLeftCANcoderMagnetOffset,
    "FL"
  );
  private final SwerveModule m_frontRightSwerveModule = new SwerveModule(
    SwerveConstants.kFrontRightDriveMotorPort,
    SwerveConstants.kFrontRightDriveMotorReversed,
    SwerveConstants.kFrontRightTurningMotorPort,
    SwerveConstants.kFrontRightTurningMotorReversed,
    SwerveConstants.kFrontRightCANcoderPort,
    SwerveConstants.kFrontRightCANcoderDirection,
    SwerveConstants.kFrontRightCANcoderMagnetOffset,
    "FR"
  );
  private final SwerveModule m_rearLeftSwerveModule = new SwerveModule(
    SwerveConstants.kRearLeftDriveMotorPort,
    SwerveConstants.kRearLeftDriveMotorReversed,
    SwerveConstants.kRearLeftTurningMotorPort,
    SwerveConstants.kRearLeftTurningMotorReversed,
    SwerveConstants.kRearLeftCANcoderPort,
    SwerveConstants.kRearLeftCANcoderDirection,
    SwerveConstants.kRearLeftCANcoderMagnetOffset,
    "RL"
  );
  private final SwerveModule m_rearRightSwerveModule = new SwerveModule(
    SwerveConstants.kRearRightDriveMotorPort,
    SwerveConstants.kRearRightDriveMotorReversed,
    SwerveConstants.kRearRightTurningMotorPort,
    SwerveConstants.kRearRightTurningMotorReversed,
    SwerveConstants.kRearRightCANcoderPort,
    SwerveConstants.kRearRightCANcoderDirection,
    SwerveConstants.kRearRightCANcoderMagnetOffset,
    "RR"
  );

  //the gyroscope
  AHRS m_gyro;

  double desiredAngle;

  //the Shuffleboard tab and entries
  private String sb_name;
  private ShuffleboardTab sb_tab;
  public GenericEntry sb_frontLeftSpeed, sb_frontRightSpeed, sb_rearLeftSpeed, sb_rearRightSpeed, sb_frontLeftAngle, sb_frontRightAngle, sb_rearLeftAngle, sb_rearRightAngle, sb_NAVXPitch, sb_NAVXYaw, sb_NAVXRoll; //yaw appears to be the axis for horizontal rotation, (-180, 180)

  /**
   * Constructor method for SwerveSubsystem
   */
  public SwerveSubsystem() {
    createShuffleboardTab();

    m_gyro = new AHRS(I2C.Port.kMXP);
  }

  /**
   * Deadbands each of the values for joystick input before calling the drive method.
   *
   * @param x The left to right translation of the robot. Domain: [-1, 1]
   * @param y The backward to forward translation of the robot. Domain: [-1, 1]
   * @param r The rotational movement of the robot. Domain: [-1, 1]
   */
  public void joystickDrive(double x, double y, double r) {
    x =
      deadband(
        x,
        SwerveConstants.kXSpeedDeadband,
        SwerveConstants.kXSpeedMinValue
      );
    y =
      deadband(
        y,
        SwerveConstants.kYSpeedDeadband,
        SwerveConstants.kYSpeedMinValue
      );
    r = deadband(r, SwerveConstants.kRotDeadband, SwerveConstants.kRotMinValue);

    drive(x, y, r);
  }

  /**
   * Calculates the angles and speeds for the swerve modules, based on x, y, and z. Then it sends the commands to the modules.
   *
   * @param x The left to right translation of the robot. Domain: [-1, 1]
   * @param y The backward to forward translation of the robot. Domain: [-1, 1]
   * @param r The rotational movement of the robot. Domain: [-1, 1]
   */
  public void drive(double x, double y, double r) {
    //adjusting for field relativity if necessary
    if (SwerveConstants.kFieldRelative) {
      double gyroAngle = m_gyro.getAngle() / 360; //this gets the angle and puts it from -1/2 to 1/2
      double nonFieldRelativeAngle = Math.atan2(x, y) / (2 * Math.PI); //again, the return value is from -1/2 to 1/2
      double fieldRelativeAngle = nonFieldRelativeAngle - gyroAngle;

      double magnitude = Math.sqrt((x * x) + (y * y));

      x = Math.sin(fieldRelativeAngle * 2 * Math.PI) * magnitude;
      y = Math.cos(fieldRelativeAngle * 2 * Math.PI) * magnitude;
    }

    //dimensions required for doing math
    final double L = SwerveConstants.kDriveBaseLength / 2;
    final double W = SwerveConstants.kDriveBaseWidth / 2;
    final double R = Math.sqrt((L * L) + (W * W));

    //doing some math
    double a = x - r * (L / R);
    double b = x + r * (L / R);
    double c = y - r * (W / R);
    double d = y + r * (W / R);

    //calculates the wheel speeds
    double frontLeftSpeed = Math.sqrt((b * b) + (d * d));
    double frontRightSpeed = Math.sqrt((b * b) + (c * c));
    double rearLeftSpeed = Math.sqrt((a * a) + (d * d));
    double rearRightSpeed = Math.sqrt((a * a) + (c * c));

    //Because wheel outputs should be from -1 to 1, desaturates all of the wheels speeds if any
    //of them exceed +-1, while maintaining the ratio between the wheels.
    double denominator = max(
      Math.abs(frontLeftSpeed),
      Math.abs(frontRightSpeed),
      Math.abs(rearLeftSpeed),
      Math.abs(rearRightSpeed),
      1.0
    );
    frontLeftSpeed = frontLeftSpeed / denominator;
    frontRightSpeed = frontRightSpeed / denominator;
    rearLeftSpeed = rearLeftSpeed / denominator;
    rearRightSpeed = rearRightSpeed / denominator;

    //calculates the wheel angles, -1/2 to 1/2, with 0 representing forward
    double frontLeftAngle = Math.atan2(b, d) / Math.PI / 2;
    double frontRightAngle = Math.atan2(b, c) / Math.PI / 2;
    double rearLeftAngle = Math.atan2(a, d) / Math.PI / 2;
    double rearRightAngle = Math.atan2(a, c) / Math.PI / 2;

    //sending the wheel and angle speeds to the motor controllers
    m_frontLeftSwerveModule.drive(frontLeftSpeed, frontLeftAngle);
    m_frontRightSwerveModule.drive(frontRightSpeed, frontRightAngle);
    m_rearLeftSwerveModule.drive(rearLeftSpeed, rearLeftAngle);
    m_rearRightSwerveModule.drive(rearRightSpeed, rearRightAngle);

    updateShuffleboardTab(
      frontLeftSpeed,
      frontRightSpeed,
      rearLeftSpeed,
      rearRightSpeed,
      frontLeftAngle,
      frontRightAngle,
      rearLeftAngle,
      rearRightAngle
    );
  }

  public void reset() {
    m_gyro.reset();

    m_frontLeftSwerveModule.reset();
    m_frontRightSwerveModule.reset();
    m_rearLeftSwerveModule.reset();
    m_rearRightSwerveModule.reset();
  }

  public void createShuffleboardTab() {
    //setting up the Shuffleboard tab
    sb_name = "SwerveSubsystem";
    sb_tab = Shuffleboard.getTab(sb_name);

    sb_frontLeftSpeed = sb_tab.add("frontLeftSpeed", 0).getEntry();
    sb_frontRightSpeed = sb_tab.add("frontRightSpeed", 0).getEntry();
    sb_rearLeftSpeed = sb_tab.add("rearLeftSpeed", 0).getEntry();
    sb_rearRightSpeed = sb_tab.add("rearRightSpeed", 0).getEntry();

    sb_frontLeftAngle = sb_tab.add("frontLeftAngle", 0).getEntry();
    sb_frontRightAngle = sb_tab.add("frontRightAngle", 0).getEntry();
    sb_rearLeftAngle = sb_tab.add("rearLeftAngle", 0).getEntry();
    sb_rearRightAngle = sb_tab.add("rearRightAngle", 0).getEntry();

    sb_NAVXPitch = sb_tab.add("NAVXPitch", 0).getEntry();
    sb_NAVXYaw = sb_tab.add("NAVXYaw", 0).getEntry();
    sb_NAVXRoll = sb_tab.add("NAVXAngle", 0).getEntry();
  }

  public void updateShuffleboardTab(
    double frontLeftSpeed,
    double frontRightSpeed,
    double rearLeftSpeed,
    double rearRightSpeed,
    double frontLeftAngle,
    double frontRightAngle,
    double rearLeftAngle,
    double rearRightAngle
  ) {
    sb_frontLeftSpeed.setDouble(frontLeftSpeed);
    sb_frontRightSpeed.setDouble(frontRightSpeed);
    sb_rearLeftSpeed.setDouble(rearLeftSpeed);
    sb_rearRightSpeed.setDouble(rearRightSpeed);

    sb_frontLeftAngle.setDouble(frontLeftAngle);
    sb_frontRightAngle.setDouble(frontRightAngle);
    sb_rearLeftAngle.setDouble(rearLeftAngle);
    sb_rearRightAngle.setDouble(rearRightAngle);

    sb_NAVXPitch.setDouble(m_gyro.getPitch());
    sb_NAVXYaw.setDouble(m_gyro.getYaw());
    sb_NAVXRoll.setDouble(m_gyro.getRoll());
  }

  public double[] optimize(double speed, double angle, double encoderAngle) {
    if (
      Math.abs(angle - encoderAngle) < 90 ||
      Math.abs(angle - encoderAngle) > 270
    ) {
      return new double[] { speed, angle };
    }

    return new double[] { -speed, ((angle + 1) % 1) - 0.5 };
  }

  public double max(double... values) {
    double m = values[0];

    for (double value : values) {
      if (value > m) {
        m = value;
      }
    }

    return m;
  }

  public double deadband(double input, double deadband, double minValue) {
    double output;
    double m = (1.0 - minValue) / (1.0 - deadband);

    if (Math.abs(input) < deadband) {
      output = 0;
    } else if (input > 0) {
      output = m * (input - deadband) + minValue;
    } else {
      output = m * (input + deadband) - minValue;
    }

    return output;
  }
}