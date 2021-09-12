#ifndef _MOTOR_H
#define _MOTOR_H

#include <stdint.h>

// Motor parameters
#define spool_circumference 0.05 // meters
#define motor_rpm 220 // no-load motor speed
#define gear_ratio 27 // 1:27 gear reduction ratio
#define encoder_ppr 12 // pulses per rotation
#define stroke_len 1.2 // meters // TODO: Use motor_config.stroke

const float ticks_per_unit = gear_ratio * encoder_ppr * stroke_len / (255 * spool_circumference);
const float units_per_second = 255 * spool_circumference * motor_rpm / (60 * stroke_len);

/**
 * Represents a paradrone motor
 */
class Motor {
  public:
    Motor(int pin_hall_a, int pin_hall_b, int pin_pwm1, int pin_pwm2, short *config_direction, void hall_isr());
    void set_speed(short left);
    void update(int dt);

    // Target motor position
    // 0 = no deflection, 255 = full deflection
    int target;

    // Current motor position estimate
    // 0 = no deflection, 255 = full deflection
    // TODO: Gaussian estimate
    float position;

    // Current motor speed
    // -255 = full speed up, 255 = full speed down
    int speed;

    // Encoder ticks
    int ticks;
  private:
    const int pwm_channel1;
    const int pwm_channel2;
    short *config_direction;
    int last_ticks;
    void set_speed_hw(const int drive, uint8_t speed);
};

#endif
