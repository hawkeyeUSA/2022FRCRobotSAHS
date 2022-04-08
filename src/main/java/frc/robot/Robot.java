/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
//import com.fasterxml.jackson.databind.ser.std.StdKeySerializers.Default;

// import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.TimedRobot;
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

  double tiem;
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
    private boolean keepBucketArmRaised = false;
    // Variable to help determine state of bucket arm raised or lowered 
    private boolean IsBucketArmRaised = false;
    
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
      if (!keepBucketArmRaised)
      {
          SetBucketArmState(stick.getRawButton(1));
      }

      // Handle Button-11 Event (Raise bucket arm)
      if (stick.getRawButton(7))
      {
        keepBucketArmRaised = true;
          SetBucketArmState(true);
      }
      // Handle Button-12 Event (Lower bucket arm)
      if (stick.getRawButton(8))
      {
        keepBucketArmRaised = false;
        SetBucketArmState(false);
      }

      // Handle Button-5 Pushed event to Lower and Raise Bucket
      SetBucketState(stick.getRawButton(5));

      // Set Arm State from Button-6 and Button-4
      HandleHangArmState(stick);

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


      // Believe this to be a separate controller (joystick) with 
      // an array of buttons
      // if(buttons.getRawButton(4)){
      //   speed = 0.5;
      // }

      //End Drive Train


      //Railgun Commands
      // if(buttons.getRawButton(10)){
      //   startFront(-0.7);
      // }else if(buttons.getRawButton(9)){
      //   startFront(-1);
      // }else if(buttons.getRawButton(11)){
      //   startFront(0);
      // }

      // if(buttons.getRawButton(1)){
      //   manualShoot(-0.3);
      // }else if(buttons.getRawButton(5)){
      //   manualShoot(0.3);
      // }else{
      //   manualShoot(0);
      // }
      //End Railgun Commands

      //Start Colorwheel Commands Test
      // if(buttons.getRawButton(3)){
      //   wheelSpinner.set(0.3);
      // 
      // }// else if(buttons.getRawButton(7)){
      //   wheelSpinner.set(0.2);
      // 
      // }else{
      //   wheelSpinner.set(0);
      // }

      // if(buttons.getRawButton(2)){
      //   wheelArm.set(0.4);
      // }else if(buttons.getRawButton(6)){
      //   wheelArm.set(-0.4);
      // }else{
      //   wheelArm.set(0);
      // }

      
      //End Colorwheel Commands Stuff
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
    // Set Manual Drive Move and Turn Speeds
    //
    private void HandleHangArmState(Joystick stick) 
    {
        double moveSpeed = 4.5;

        // Only activate forward/reverse motors when buttons are pressed:
        // Button-4 - Lower arms
        // Button-6 - Raise arms
        if (stick.getRawButton(6) || stick.getRawButton(4))
        {
            //Move the motors Up
            if (stick.getRawButton(6))
            {
                armMotor_Right.set(moveSpeed/4);
                armMotor_Left.set(moveSpeed);
                // _armDrive.arcadeDrive(moveSpeed, 0);
                // _armDrive.setDeadband(0.05);
                System.out.println("UP Arm Speed: L(" + armMotor_Left.get() + ") R(" + armMotor_Right.get() + ")");
              }

            //Move the motors down
            if (stick.getRawButton(4))
            {
                armMotor_Right.set(-moveSpeed/4 );
                armMotor_Left.set(-moveSpeed);
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
    // Method to raise and lower the bucket
    // True  - Raise bucket
    // False - Lower bucket
    //
    private void SetBucketArmState(boolean buttonPushed)
    {
        pcmSolenoid_BucketArm.set(buttonPushed);
        if (buttonPushed)
        {
            IsBucketArmRaised = true;
            System.out.println("Trigger Pulled - Executing Raise Bucket Solenoid");
        }
        else
        {
            IsBucketArmRaised = false;
        }
        //  else
        //      System.out.println("Trigger Released - Releasing Raise Bucket Solenoid");
    }

    //
    // Method to raise and lower the bucket
    // True  - Raise bucket
    // False - Lower bucket
    //
    private void SetBucketState(boolean buttonPushed)
    {
        if (!IsBucketArmRaised)
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

    public void manualShoot(final double shoot) 
    {
      // leftBallMotor2.set(-shoot); //Port 6
      // rightBallMotor2.set(shoot); //Port 7
    }

    public void startFront(final double shoot)
    {
      // leftBallMotor1.set(shoot); //Port 4
      // rightBallMotor1.set(-shoot); //Port 5
    }



    @Override
    public void autonomousInit() 
    {
      // startTime = Timer.getFPGATimestamp();
     tiem = System.currentTimeMillis();


    }

    @Override
    public void autonomousPeriodic() 
    {
      while (System.currentTimeMillis()> tiem +4000) { _drive.arcadeDrive(.40, 0);
          wai````
      } 
      // double currentTime = Timer.getFPGATimestamp();
      // double time = currentTime - startTime;

      // if(time < 2){
      //   manualDrive(0.5, 0);

      // }else if(time < 3){
      //   manualDrive(0, 0.3);

      // }else{
      //   manualDrive(0, 0);
      // }
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