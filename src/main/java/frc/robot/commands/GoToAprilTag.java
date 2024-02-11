package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.SwerveSubsystem;
import frc.robot.subsystems.VisionSubsystem;

public class GoToAprilTag extends Command {

  VisionSubsystem m_vision;
  SwerveSubsystem m_swerveDrive;

  double m_desiredX, m_desiredY, m_desiredRot;

  public GoToAprilTag(
    SwerveSubsystem swerveDrive,
    VisionSubsystem vision,
    double x,
    double y,
    double rot
  ) {
    m_vision = vision;
    m_swerveDrive = swerveDrive;

    m_desiredX = x;
    m_desiredY = y;
    m_desiredRot = rot;

    addRequirements(swerveDrive);
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {}

  @Override
  public void execute() {
    var target = m_vision.findBestTarget();
    if (target != null) {
      double forwardSpeedX = m_vision.calculateSidewaysSpeedX(
        target,
        m_desiredX
      );
      double forwardSpeedSpeedY = m_vision.calculateForwardSpeedY(
        target,
        m_desiredY
      );
      double rotationSpeed = m_vision.calculateRotationSpeed(
        target,
        m_desiredRot
      );
      m_swerveDrive.drive(forwardSpeedX, forwardSpeedSpeedY, rotationSpeed);
    }
  }

  @Override
  public boolean isFinished() {
    return false;
  }
}