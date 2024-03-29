package frc.robot.subsystems;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
import edu.wpi.first.wpilibj.shuffleboard.ShuffleboardTab;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.VisionConstants;
import java.lang.annotation.Target;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonUtils;
import org.photonvision.targeting.PhotonPipelineResult;
import org.photonvision.targeting.PhotonTrackedTarget;

public class VisionSubsystem extends SubsystemBase {

  private String m_cameraname;
  PhotonCamera m_camera;
  PhotonCamera m_result;
  PhotonPipelineResult m_pipline = new PhotonPipelineResult();
  private ShuffleboardTab m_tab;
  private GenericEntry sb_x, sb_y, sb_rot;

  private final PIDController m_xController = new PIDController(
    VisionConstants.kXP,
    0,
    VisionConstants.kXD
  );

  private final PIDController m_yController = new PIDController(
    VisionConstants.kYP,
    0,
    VisionConstants.kYD
  );

  private final PIDController m_rotationController = new PIDController(
    VisionConstants.kRP,
    0,
    VisionConstants.kRD
  );

  /**
   * Constructor class for VisionSubsystem
   */
  public VisionSubsystem(String cameraname) {
    m_camera = new PhotonCamera(cameraname);
    // tabForShuffleboard();
  }

  public VisionTarget findBestTarget() {
    VisionTarget target = null;
    PhotonPipelineResult pipelineResult = m_camera.getLatestResult();
    if (pipelineResult != null) {
      if (!isTargetBroken(pipelineResult.getBestTarget())) {
        target = new VisionTarget(pipelineResult.getBestTarget());
      } else {
        System.out.println("!!!");
      }
    }
    return target;
  }

  public double calculateSidewaysSpeedX(VisionTarget target, double goal) {
    double axis = target.getX();
    
    return m_xController.calculate(axis, goal);
  }

  public double calculateForwardSpeedY(VisionTarget target, double goal) {
    double axis = target.getY();
    
    // -1.0 required to fit to swerveDrive standards
    return -m_yController.calculate(axis, goal);
  }

  public double calculateRotationSpeed(VisionTarget target, double goal) {
    double axis = target.getYaw();
    
    return m_rotationController.calculate(axis, goal);
  }

  public boolean isTargetBroken(PhotonTrackedTarget target) {
    return target == null || target.getBestCameraToTarget() == null;
  }

  // public double getRange(VisionTarget target) {
  //   // First calculate range
  //   double range = PhotonUtils.calculateDistanceToTargetMeters(
  //     VisionConstants.kCAMERA_HEIGHT_METERS,
  //     VisionConstants.kTARGET_HEIGHT_METERS,
  //     VisionConstants.kCAMERA_PITCH_RADIANS,
  //     Units.degreesToRadians(target.getPitch())
  //   );
  //   return range;
  // }

  // public void tabForShuffleboard() {
  //   m_tab = Shuffleboard.getTab(m_cameraname);
  //   sb_x = m_tab.add("x", 0).getEntry();
  //   sb_y = m_tab.add("y", 0).getEntry();
  //   sb_rot = m_tab.add("rot", 0).getEntry();
  // }

  // public void updateShuffleboardTab(double x, double y, double rot) {
  //   sb_x.setDouble(x);
  //   sb_y.setDouble(y);
  //   sb_rot.setDouble(rot);
  // }

  // Avoid exposing PhotonVision classes outside of this subsystem
  public class VisionTarget {

    private PhotonTrackedTarget m_target;
    Transform3d m_transform3d;
    double m_pitch, m_yaw;

    VisionTarget(PhotonTrackedTarget target) {
      m_transform3d = target.getBestCameraToTarget();
      m_yaw = target.getYaw();
      this.m_target = target;
    }

    public double getX() {
      return m_transform3d.getY();
    }

    public double getY() {
      return m_transform3d.getX();
    }

    // public double getPitch() {
    //   double m_pitch = m_target.getPitch() / 360.0;
    //   m_pitch = (m_pitch % 1.0) - 0.5;
    //   return m_pitch;
    // }

    public double getYaw() {
      double m_yaw = -m_target.getYaw();
      return m_yaw;
    }

    public int getID(){
      return m_target.getFiducialId();
    }
  }
}
