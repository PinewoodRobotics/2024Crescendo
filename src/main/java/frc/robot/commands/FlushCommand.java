package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants.IntakeConstants;
import frc.robot.subsystems.IntakeSubsystem;

public class FlushCommand extends Command {
    
    @SuppressWarnings({ "PMD.UnusedPrivateField", "PMD.SingularField" })
    private final IntakeSubsystem m_intakeSubsystem;

    private final double outputSpeed = IntakeConstants.kOutputSpeed;

    /**
     * @param intakeSubsystem The subsystem used by this command.
     */
    public FlushCommand(IntakeSubsystem intakeSubsystem) {
        this.m_intakeSubsystem = intakeSubsystem;
        // Use addRequirements() here to declare subsystem dependencies.
        addRequirements(intakeSubsystem);
    }

    @Override
    public void initialize() {}

    @Override
    public void execute() {

        // setting the motor speed to outputSpeed
        m_intakeSubsystem.setMotor(-outputSpeed);
    }

    @Override
    public void end(boolean interrupted) {
        m_intakeSubsystem.setMotor(0);
    }

    @Override
    public boolean isFinished() {
        return false;
    }
}