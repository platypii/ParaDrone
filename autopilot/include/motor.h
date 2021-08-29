#ifndef _MOTOR_H
#define _MOTOR_H

/**
 * Represents a paradrone motor
 */
class Motor {
  public:
    Motor(int, int, int, int, void (*)());
    void set_speed(short left);
    void update(int dt);

    // Target motor position
    // 0 = no deflection, 255 = full deflection
    short target;

    // Current motor position estimate
    // 0 = no deflection, 255 = full deflection
    // TODO: Gaussian estimate
    float position;

    // Current motor speed
    // -255 = full speed up, 255 = full speed down
    short speed;

    // Encoder ticks
    int ticks;
  private:
    const int pwm_channel1;
    const int pwm_channel2;
    float ticks_speed; // ticks / sec
    void set_speed_hw(const int drive, uint8_t speed);
};

#endif
