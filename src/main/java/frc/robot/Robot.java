/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
//import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;

// import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
// import edu.wpi.first.wpilibj.Timer;0
import edu.wpi.first.wpilibj.drive.DifferentialDrive;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot 
{
    private Joystick stick = new Joystick(0);
    // private Joystick buttons = new Joystick(1);

    // Define AnalogInput distance
    private AnalogInput distance1 = new AnalogInput(0);
    private double kValueToInches = 0.125;

    // Initialize Talon SRX Drive Motors
    private WPI_TalonSRX driveMotor_RightMaster = new WPI_TalonSRX(0);
    private WPI_TalonSRX driveMotor_RightSlave  = new WPI_TalonSRX(1);
    private WPI_TalonSRX driveMotor_LeftMaster  = new WPI_TalonSRX(2);
    private WPI_TalonSRX driveMotor_LeftSlave   = new WPI_TalonSRX(3);
    private DifferentialDrive _drive  = new DifferentialDrive(driveMotor_LeftMaster, driveMotor_RightMaster);

    // Initialize Talon SRX Climber Arm Motors
    private WPI_TalonSRX armMotor_Left  = new WPI_TalonSRX(4);
    private WPI_TalonSRX armMotor_Right = new WPI_TalonSRX(6);

    // Initialize Solenoids
    Solenoid pcmSolenoid_BucketArm = new Solenoid(PneumaticsModuleType.CTREPCM, 0);
    Solenoid pcmSolenoid_Bucket = new Solenoid(PneumaticsModuleType.CTREPCM, 1);


    // Variable to bypass trigger pull and release to keep bucket raised
    private boolean _keepBucketArmRaised = false;
    // Variable to help determine state of bucket arm raised or lowered 
    private boolean _IsBucketArmRaised = false;
    
    // private double startTime;
    //private static final double kHoldDistance = 12.0; //These are for Distance Sensors
    //private static final double kValueToInches = 0.125;

    /**
     * This function is run when the robot is first started up and should be used
     * for any initialization code.
     */
    @Override
    public void robotInit() {
        // This is where you can adjust the ramp up speed in seconds
        driveMotor_LeftMaster.configClosedloopRamp(0.3);
        driveMotor_LeftSlave.configClosedloopRamp(0.3);
        driveMotor_RightMaster.configClosedloopRamp(0.3);
        driveMotor_RightSlave.configClosedloopRamp(0.3);

        // Set the slave to follow master
        driveMotor_LeftSlave.follow(driveMotor_LeftMaster);
        driveMotor_RightSlave.follow(driveMotor_RightMaster); 

        
        // This is where you can adjust the ramp up speed in seconds for the Climber Arms
        armMotor_Left.configClosedloopRamp(0.5);
        armMotor_Right.configClosedloopRamp(0.5);

        // Set motor neutral mode to Break when motor speed is 0.0
        armMotor_Left.setNeutralMode(NeutralMode.Brake);
        armMotor_Right.setNeutralMode(NeutralMode.Brake);

        // CameraServer.getInstance();
    }

    @Override
    public void teleopInit() {
    }

    @Override
    public void teleopPeriodic() 
    {
      double currentDistance = distance1.getValue() * kValueToInches;


      // Manually Drive Robot
      ManualDrive(stick);



      // Set Bucket Arm State from Trigger pulled and released Event
      if (!_keepBucketArmRaised)
      {
          SetBucketArmState(stick.getRawButton(1));
      }

      // Handle Button-11 Event (Raise bucket arm)
      if (stick.getRawButton(7))
      {
        _keepBucketArmRaised = true;
          SetBucketArmState(true);
      }
      // Handle Button-12 Event (Lower bucket arm)
      if (stick.getRawButton(8))
      {
        _keepBucketArmRaised = false;
        SetBucketArmState(false);
      }

      // Handle Button-5 Pushed event to Lower and Raise Bucket
      SetBucketState(stick.getRawButton(5));

      // Set Arm State from Button-6 and Button-4
      HandleHangArmState(stick);

      // Set Arm State from Button-9
      HandleHangArmDownSLOWLY(stick);

      // Handle POV Button Events (Not being used)
      HandlePOVButton(stick);


      // 
      // int stickButtonCount = stick.getButtonCount();
      // System.out.println("Total Buttons: " + stickButtonCount);

      // for(int btnCtr = 1; btnCtr <= stickButtonCount; btnCtr++)
      // {
      //   if (stick.getRawButton(btnCtr))
      //   {
      //     System.out.println("Button " + btnCtr + " Pressed.");
      //   }
      // }
    }


    // Command Functions for the Teleop

    // 
    // Set Manual Drive Move and Turn Speeds
    //
    private void ManualDrive(Joystick stick) 
    {
        //Drive Train
        final double speed = 1;
        double turn = (-stick.getY()); // Not sure why, getY() is functioning as the Z-Axis
        double move = (stick.getZ());  // Not sure why, getZ is functioning as the Y-Axis

      
        // Adjust speed to tune movements
        turn = turn * speed;
        move = move * speed;

        //  System.out.println("Move Speed: " + (move));
        //  System.out.println("Turn Speed: " + (turn));

        _drive.arcadeDrive(move, turn);
        _drive.setDeadband(0.05);
    }


    // 
    // Handle Hang Arm State for buttons 7 & 8
    //
    private void HandleHangArmState(Joystick stick) 
    {
        double moveSpeed = 45.0;

        // Only activate forward/reverse motors when buttons are pressed:
        // Button-4 - Lower arms
        // Button-6 - Raise arms
        if (stick.getRawButton(6) || stick.getRawButton(4))
        {
            //Move the motors Up
            if (stick.getRawButton(6))
            {
                armMotor_Right.set(moveSpeed);
                armMotor_Left.set(moveSpeed/7.5);
                // _armDrive.arcadeDrive(moveSpeed, 0);
                // _armDrive.setDeadband(0.05);
                System.out.println("UP Arm Speed: L(" + armMotor_Left.get() + ") R(" + armMotor_Right.get() + ")");
            }

            //Move the motors down
            if (stick.getRawButton(4))
            {
                armMotor_Right.set(-moveSpeed);
                armMotor_Left.set(-moveSpeed/7.5);
                System.out.println("DOWN Arm Speed: L(" + armMotor_Left.get() + ") R(" + armMotor_Right.get() + ")");
            }
        }
        else
        {
            armMotor_Right.set(0);
            armMotor_Left.set(0);
            // System.out.println("NO Arm Speed: L(" + armMotor_Left.get() + ") R(" + armMotor_Right.get() + ")");
        }
    }

    // 
    // Run Hanging motor slow while hanging to prevent slow dropping
    //
    private void HandleHangArmDownSLOWLY(Joystick stick) 
    {
        double moveSpeed = 4.0;

        // Only activate forward/reverse motors when buttons are pressed:
        // Button-9 - Toggle 
        if (stick.getRawButton(9))
        {
            //Move the hang arm motors down (Collapse arms)
            armMotor_Right.set(-moveSpeed);
            armMotor_Left.set(-moveSpeed/4);
            System.out.println("DOWN Arm Speed: L(" + armMotor_Left.get() + ") R(" + armMotor_Right.get() + ")");
        }
        else
        {
            armMotor_Right.set(0);
            armMotor_Left.set(0);
            //System.out.println("NO Arm Speed: L(" + armMotor_Left.get() + ") R(" + armMotor_Right.get() + ")");
        }
    }

    //
    // Method to raise and lower the bucket
    // True  - Raise bucket
    // False - Lower bucket
    //
    private void SetBucketArmState(boolean buttonPushed)
    {
        pcmSolenoid_BucketArm.set(buttonPushed);
        if (buttonPushed)
        {
            _IsBucketArmRaised = true;
            System.out.println("Trigger Pulled - Executing Raise Bucket Solenoid");
        }
        else
        {
            _IsBucketArmRaised = false;
        }
        //  else
        //      System.out.println("Trigger Released - Releasing Raise Bucket Solenoid");
    }

    //
    // Method to raise and lower the bucket
    // True  - Lowers bucket (Dump)
    // False - Raises bucket
    //
    private void SetBucketState(boolean buttonPushed)
    {
        if (!_IsBucketArmRaised)
            return;

        pcmSolenoid_Bucket.set(buttonPushed);
        if (buttonPushed)
            System.out.println("Button-5 Pushed - Lowering Bucket");
        // else
        //     System.out.println("Button-5 Released - Raising Bucket");
      }


    
    //
    // Metod to handle POV Button state
    //
    private void HandlePOVButton(Joystick stick)
    {
        int povValue = stick.getPOV(0);
        //System.out.println("POV Stick: " + povValue);

        // Possible Values
        // Center     = -1
        // Up         = 0
        // Up-Right   = 45
        // Right      = 90
        // Down-Right = 135
        // Down       = 180
        // Down-Left  = 225
        // Left       = 270
        // Up-Left    = 315
        switch(povValue)
        {
          case 0: // Up position
              break;
          case 180: // Down position
              break;
          default: // Off position is any position we don't care about
              break;
        }
    }



    //
    // Start Autonomous Drive Logic
    //
    private long _autonomousStartPauseTime;
    private long _autonomousBackupDriveTime;
    private double _startTime;

    @Override
    public void autonomousInit() 
    {
        //
        // Initialize NEW Autonomous routine
        //
        _startTime = Timer.getFPGATimestamp();
        SetBucketState(false);


        // 
        // Initialize OLD Autonomous routine
        //
        // Set an initial 1 second pause before begining autonomous drive (Used only for original AutonomousBackup)
        _autonomousStartPauseTime = System.currentTimeMillis() + 1000;

        // Set the drive stop time to 4 seconds after 5 second pause (aka 9 seconds in future)
        _autonomousBackupDriveTime = _autonomousStartPauseTime + 4000; // Original value 6000
    }

    @Override
    public void autonomousPeriodic()
    {
        // New Backup and Score Autonomous Routine
        AutonomousBackupAndScore();

        // Original Autonomous Routine
        // AutonomousBackup();
    }

    //
    // New  Autonomous Backup and Score routine
    //
    private void AutonomousBackupAndScore()
    {
        double driveSpeed = 0.5;
        double currentTime = Timer.getFPGATimestamp();
        double time = currentTime - _startTime;
    
        // Pause for 1 second
        if (time < 1)
        {
            _drive.arcadeDrive(0, 0);
            System.out.println("Pausing for 1 second");
        }
        // Reverse for 4 seconds
        else if (time >=1 && time < 5)
        {
            _drive.arcadeDrive(0, -driveSpeed);
            System.out.println("Autonomous drive (speed:" + -driveSpeed + ") running for " + (long)time + " seconds");
        }
        // Raise bucket arm drive forward for 3.5 seconds
        else if (time >= 5 && time < 8.5)
        {
            SetBucketArmState(true);
            _drive.arcadeDrive(0, driveSpeed);
            System.out.println("Autonomous drive (speed:" + driveSpeed + ") running for " + (long)time + " seconds");
        }
        // Pause for 0.5 seconds
        else if (time >= 8.5 && time < 9)
        {
            _drive.arcadeDrive(0, 0);
            System.out.println(" Stopped and pausing for 0.5 seconds");
        }
        // Dump the bucket
        else if (time >= 9 & time < 10)
        {
            SetBucketState(true);
        }
        // Pause for 1 second
        else if (time >= 10 && time < 11)
        {
            SetBucketState(false);
            System.out.println("Pausing for 1 seconds");
        }
        // Dump the bucket
        else if (time >= 11 & time < 12)
        {
            SetBucketState(true);
        }
        // Pause for 1 second
        else if (time >= 12 && time < 13)
        {
            SetBucketState(false);
            System.out.println("Pausing for 1 seconds");
        }
        // Reverse for 4 seconds
        else if (time >=13 && time < 17.5)
        {
            _drive.arcadeDrive(0, -driveSpeed); // Backup for 4 seconds
            SetBucketState(false);
            SetBucketArmState(false);
            System.out.println("Autonomous drive (speed:" + -driveSpeed + ") running for " + (long)time + " seconds");
        }
        else {
            _drive.arcadeDrive(0, 0);
            SetBucketState(false);
            SetBucketArmState(false);
            // System.out.println("Autonomous drive complete");
        }
    }

    //
    // Orriginal Autonomous Backup routine
    //
    private void AutonomousBackup()
    {
        double reverseSpeed = -0.5;
        long timeDiffSecs = 0;
        long currentTime = System.currentTimeMillis();


        // if we are still in 5 second wait, then set drive speed to 0, log message and return;
        if (currentTime < _autonomousStartPauseTime)
        {
          timeDiffSecs = (_autonomousStartPauseTime - currentTime) / 1000;
          System.out.println("Autonomous drive pausing for " + timeDiffSecs + " seconds");

          _drive.arcadeDrive(0, 0);
          return;
        }


        // 
        // If we're past 5 second pause, drive in reverse for 4 seconds then stop
        //
        timeDiffSecs = (_autonomousBackupDriveTime - currentTime) / 1000;
        if (currentTime < _autonomousBackupDriveTime)
        {
                             //Turn, Drive speed
            _drive.arcadeDrive(0, reverseSpeed);
            System.out.println("Autonomous drive (speed:" + reverseSpeed + ") running for " + timeDiffSecs + " seconds");
        }
        else {
            _drive.arcadeDrive(0, 0);
            System.out.println("Autonomous drive complete");
        }
    }


    @Override
    public void testInit() 
    {
    }

    @Override
    public void testPeriodic() 
    {
    }
}