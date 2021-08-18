
class Motor {
  public:
    Motor(int, int, int, int, void (*)(), int*);
    void set_speed(short left);
    void set_speed_hw(const int drive, uint8_t speed);
  private:
    const int pwm_channel1;
    const int pwm_channel2;
};
