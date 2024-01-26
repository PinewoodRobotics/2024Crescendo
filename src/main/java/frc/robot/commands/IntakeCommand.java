// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.IntakeConstants;
import frc.robot.subsystems.IntakeSubsystem;
import edu.wpi.first.wpilibj.XboxController;

public class IntakeCommand extends Command {

  @SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
  private final IntakeSubsystem m_intakeSubsystem;

  private final int intakeSpeed = (int) IntakeConstants.kIntakeSpeed;
  private final XboxController controller; 

  private boolean isFinished = false;
  private boolean previousButton = true;
  private boolean currentButton = false;

  /**
   * @param intakeSubsystem The subsystem used by this command.
   */
  public IntakeCommand(IntakeSubsystem intakeSubsystem, XboxController controller) {
    this.m_intakeSubsystem = intakeSubsystem;
    this.controller = controller;
    // Use addRequirements() here to declare subsystem dependencies.
    addRequirements(intakeSubsystem);
  }

  @Override
  public void initialize() {}

  @Override
  public void execute() {
    m_intakeSubsystem.setMotor(intakeSpeed);
  }

  @Override
  public void end(boolean interrupted) {
    m_intakeSubsystem.setMotor(0);
  }

  @Override
  public boolean isFinished() {
    previousButton = currentButton; 
    currentButton = controller.getBButtonPressed(); // Button mapping TBD

    // if currentButton = true and previousButton = false 
    isFinished = (currentButton && !previousButton); 

    // if isFinsihed already true by the if statement above or if the intake senses if the note is inside the intake
    return (isFinished || m_intakeSubsystem.isDetected()); 
  }
}
