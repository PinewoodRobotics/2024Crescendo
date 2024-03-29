// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.Constants.OperatorConstants;
import frc.robot.Constants.RobotContainerConstants;
import frc.robot.Constants.VisionConstants;
import frc.robot.commands.*;
import frc.robot.error.LimitException;
import frc.robot.subsystems.*;
import frc.robot.util.PinCommunication;
import frc.robot.util.controller.FlightModule;
import frc.robot.util.controller.FlightStick;
import frc.robot.util.controller.FlightStick.AxisEnum;
import frc.robot.util.controller.LogitechController;
import frc.robot.util.controller.LogitechController.ButtonEnum;
import frc.robot.util.identity.Identity;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer {

  // The robot's subsystems and commands are defined here...
  private final VisionSubsystem m_vision = new VisionSubsystem("limelight");
  // private final LocalizationSubsystem localizationSubsystem = new LocalizationSubsystem(
  //   null
  // );
  private final SwerveSubsystem m_swerveSubsystem = new SwerveSubsystem();
  private final ArmSubsystem m_armSubsystem = new ArmSubsystem();
  private final IntakeSubsystem m_intake = new IntakeSubsystem();
  private ClimbArmSubsystem m_climbArmSubsystem = new ClimbArmSubsystem(
    Constants.ClimbArmConstants.kClimbArmMotorPort
  );

  final LogitechController m_driverController = new LogitechController(
    OperatorConstants.kDriverControllerPort
  );

  final LogitechController m_operatorController = new LogitechController(
    OperatorConstants.kOperatorControllerPort
  );

  final FlightModule m_flightModule = new FlightModule(
    OperatorConstants.kFlightPortLeft,
    OperatorConstants.kFlightPortRight
  );

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    /*try {
      Identity identity = new Identity();
      this.getClass().getMethod(identity + "Bindings").invoke(this);
      //if (Objects.equals(identity.getIdentity(), "godbrigero")) return;
    } catch (Exception e) {
      e.printStackTrace();
    }*/

    //PinCommunication.sendOnline();

    // m_intake.setDefaultCommand(
    //   new RunCommand(
    //       () -> m_intake.setMotor(0), m_intake
    //   )
    // };

    // if the Swerve is enabled, lets set the default command that the scheduler runs to a RunCommand, that depends on the
    // driver controllers joysticks
    if (RobotContainerConstants.kSwerveEnabled) {
      m_swerveSubsystem.setDefaultCommand(
        // 4765: Controller commands converted for various joysticks
        new RunCommand(
          () ->
            m_swerveSubsystem.joystickDrive(
              m_flightModule.rightFlightStick.getRawAxis(
                FlightStick.AxisEnum.JOYSTICKX.value
              ) *
              1,
              m_flightModule.rightFlightStick.getRawAxis(
                FlightStick.AxisEnum.JOYSTICKY.value
              ) *
              -1,
              m_flightModule.leftFlightStick.getRawAxis(
                FlightStick.AxisEnum.JOYSTICKROTATION.value
              ) *
              1
            ),
          m_swerveSubsystem
        )
      );
    }
    // if the arm is enabled, lets
    if (RobotContainerConstants.kArmEnabled) {
      m_armSubsystem.setDefaultCommand(
        new RunCommand(() -> m_armSubsystem.updateFF(), m_armSubsystem)
      );
    }

    // try {
    //   climbArmSubsystem =
    //     new ClimbArmSubsystem(
    //       Constants.ClimbArmConstants.kClimbArmMotorPort,
    //       Constants.ClimbArmConstants.kClimbArmMotorIsBrushless
    //     );
    // } catch (NoChannelFoundException e) {
    //   e.printStackTrace();
    // }

    // climbArmSubsystem.setDefaultCommand(
    //   new RunCommand(
    //     () -> climbArmSubsystem.tick(false, controller.getBButton()),
    //     climbArmSubsystem
    //   )
    // );

    // Configure the trigger bindings
    //godbrigeroBindings();
    configureOperatorLogitech();
    //configureDriverLogitech();
    configureFlightStickLeft();
    configureFlightStickRight();
  }

  /**
   *
   * Use this method to define your trigger->command mappings. Triggers can be created via the
   * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with an arbitrary
   * predicate, or via the named factories in {@link
   * edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for {@link
   * CommandXboxController Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller
   * PS4} controllers or {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight
   * joysticks}.
   */
  private void configureFlightStickLeft() {
    ShuffleboardTab sb_tab = Shuffleboard.getTab("test");

    sb_tab.add("kDriveP", 1).getEntry();

    if (RobotContainerConstants.kArmEnabled) {
      new JoystickButton(
        m_flightModule.leftFlightStick,
        FlightStick.ButtonEnum.B16.value
      )
        .whileTrue(new OutputPrepCommand(m_armSubsystem, m_swerveSubsystem));
      new JoystickButton(
        m_flightModule.leftFlightStick,
        FlightStick.ButtonEnum.TRIGGER.value
      )
        .whileTrue(new OutputCommand(m_intake, m_armSubsystem));
    }
  }

  private void configureFlightStickRight() {
    if (RobotContainerConstants.kArmEnabled) {
      // When the x button on the LogitechController is pressed, we reset the position of the arm
      new JoystickButton(
        m_flightModule.rightFlightStick,
        FlightStick.ButtonEnum.TRIGGER.value
      )
        .whileTrue(
          new IntakeCommand(m_intake, m_armSubsystem, m_swerveSubsystem)
        );
    }
    if (RobotContainerConstants.kSwerveEnabled) {
      new JoystickButton(
        m_flightModule.rightFlightStick,
        FlightStick.ButtonEnum.B5.value
      )
        .onTrue(m_swerveSubsystem.runOnce(m_swerveSubsystem::reset));
      new JoystickButton(
        m_flightModule.rightFlightStick,
        FlightStick.ButtonEnum.X.value
      )
        // JARED, you need to make a xconfig, which makes the wheels into a x shape to prevent being pushed by other robots.
        .onTrue(m_swerveSubsystem.runOnce(m_swerveSubsystem::reset));
    }
  }

  private void configureOperatorLogitech() {
    new JoystickButton(
      m_operatorController,
      LogitechController.ButtonEnum.A.value
    )
      .toggleOnTrue(
        new GoToAprilTag(
          m_driverController,
          m_swerveSubsystem,
          m_vision,
          VisionConstants.kAmpXGoal,
          VisionConstants.kAmpYGoal,
          VisionConstants.kAmpRotGoal
        )
      );

    if (RobotContainerConstants.kArmEnabled) {
      // When the x button on the LogitechController is pressed, we reset the position of the arm
      new JoystickButton(
        m_operatorController,
        LogitechController.ButtonEnum.RIGHTBUTTON.value
      )
        .whileTrue(
          new ClimberUp(m_climbArmSubsystem,Constants.ClimbArmConstants.kClimberArmMotorSpeed,m_armSubsystem.getArmInterface())
        );

      new JoystickButton(
        m_operatorController,
        LogitechController.ButtonEnum.RIGHTTRIGGER.value
      )
        .whileTrue(
          new ClimberDown(
            m_climbArmSubsystem,
            Constants.ClimbArmConstants.kClimberArmMotorSpeed,
            m_armSubsystem.getArmInterface()
          )
        );

      new JoystickButton(
        m_operatorController,
        LogitechController.ButtonEnum.LEFTTRIGGER.value
      )
        .whileTrue(
          new Flush(m_intake, m_armSubsystem)
        );

      // When the x button on the LogitechController is pressed, we reset the position of the arm
      new JoystickButton(
        m_operatorController,
        LogitechController.ButtonEnum.Y.value
      )
        .onTrue(m_armSubsystem.setPositionCommand(0.25));
      // when the left trigger on the logitech controller is pressed, lets set the position of the arm to 0.125
      new JoystickButton(
        m_operatorController,
        LogitechController.ButtonEnum.B.value
      )
        .onTrue(m_armSubsystem.setPositionCommand(0.11));
      // When the y button is pressed on the logitech controller, lets set the position of the arm to 0.25
      new JoystickButton(
        m_operatorController,
        LogitechController.ButtonEnum.A.value
      )
        .onTrue(m_armSubsystem.setPositionCommand(0));
    }
    // If the swerve drive is enabled, we should make it so that the start button resets the swerveSubsystem if it's getting buggy
    // We should make it so that the back button of the logitech controller the intake runs once
    // new JoystickButton(
    //   m_operatorController,
    //   LogitechController.ButtonEnum.Y.value
    // )
    //   .whileTrue(m_intake.runOnce(() -> m_intake.setMotor(-0.5)));

    // We should make it so that when the right trigger is pressed, the IntakeMotors start moving
    new JoystickButton(
      m_operatorController,
      LogitechController.ButtonEnum.LEFTTRIGGER.value
    )
      .toggleOnTrue(
        new IntakeCommand(m_intake, m_armSubsystem, m_swerveSubsystem)
      );

    // We should make it so that when the right button is pressed, the IntakeMotors shoot out the note
    new JoystickButton(
      m_operatorController,
      LogitechController.ButtonEnum.LEFTBUTTON.value
    )
      .toggleOnTrue(new OutputCommand(m_intake, m_armSubsystem));
    // Intake Button TBD
    //new Trigger(controller::getBButton)
    //  .toggleOnTrue(new IntakeCommand(m_intake, m_armSubsystem));
    // new Trigger(controller::getAButton)
    //  .toggleOnTrue(new ClimbArmCommand(climbArmSubsystem, 10, 0, "Climb Arm"));

    // m_chooser.addOption("Auton", new ExampleCommand(m_exampleSubsystem));

    // localizationSubsystem.setDefaultCommand(
    //   new RunCommand(
    //     () -> {
    //       localizationSubsystem.updatePosition();
    //       Pose2d pos = localizationSubsystem.getPosition();
    //       System.out.println(
    //         pos.getX() + " | " + pos.getY() + " | " + pos.getRotation()
    //       );
    //     },
    //     localizationSubsystem
    //   )
    // );
  }

  private void configureDriverLogitech() {
    if (RobotContainerConstants.kArmEnabled) {
      // We should make it so that when the right trigger is pressed, the IntakeMotors start moving
      new JoystickButton(
        m_driverController,
        LogitechController.ButtonEnum.RIGHTTRIGGER.value
      )
        .whileTrue(
          new IntakeCommand(m_intake, m_armSubsystem, m_swerveSubsystem)
        );
      new JoystickButton(
        m_driverController,
        LogitechController.ButtonEnum.RIGHTTRIGGER.value
      )
        .whileTrue(new OutputCommand(m_intake, m_armSubsystem));
    }
    // If the swerve drive is enabled, we should make it so that the start button resets the swerveSubsystem if it's getting buggy
    if (RobotContainerConstants.kSwerveEnabled) {
      new JoystickButton(
        m_driverController,
        LogitechController.ButtonEnum.STARTBUTTON.value
      )
        .onTrue(m_swerveSubsystem.runOnce(m_swerveSubsystem::reset));
    }
  }

  void godbrigeroBindings() {
    new JoystickButton(m_operatorController, ButtonEnum.A.value)
      .whileTrue(
        new RunCommand(
          () -> {
            try {
              // Account for some error
              m_climbArmSubsystem.setSpeed(
                m_armSubsystem.getCurrentPosition() > 0.01 ||
                  m_armSubsystem.getCurrentPosition() < -0.01
                  ? 0
                  : 50
              );
            } catch (LimitException e) {
              throw new RuntimeException(e);
            }
          },
          m_climbArmSubsystem
        )
      )
      .whileFalse(
        new RunCommand(
          () -> {
            try {
              m_climbArmSubsystem.setSpeed(
                m_operatorController.getRawButton(ButtonEnum.B.value) ? -50 : 0
              );
            } catch (LimitException e) {
              throw new RuntimeException(e);
            }
          },
          m_climbArmSubsystem
        )
      );
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An example command will be run in autonomous
    System.out.println(m_operatorController.getPOV());
    switch(m_operatorController.getPOV()) {
      case 0:
        return new ClimberDown(m_climbArmSubsystem, 0, null);
      case 45:
        // code block
      case 90:
      case 135:
      case 180:
        return new ClimberUp(m_climbArmSubsystem, 0, null);
      case 225:
      case 270:
      case 315:
      default:
        return new IntakeCommand(m_intake, m_armSubsystem, m_swerveSubsystem);   
    }
  }
}
