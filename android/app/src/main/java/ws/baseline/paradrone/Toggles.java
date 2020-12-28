package ws.baseline.paradrone;

/**
 * Represents toggle position for a paraglider
 */
public class Toggles {
    // Toggle controls
    public double motor_position_left = 0;
    public double motor_position_right = 0;
    public double motor_target_left = 0;
    public double motor_target_right = 0;

    /**
     * If the current position is not the target position, engage the motors
     */
    public void tick(double dt) {
        // Calculate target delta, and motor speed to get there
        final double speedLeft = motorSpeed(motor_target_left - motor_position_left);
        final double speedRight = motorSpeed(motor_target_right - motor_position_right);
        // Update position estimate
        motor_position_left += speedLeft * dt / 8;
        motor_position_right += speedRight * dt / 8;
        motor_position_left = normalizePosition(motor_position_left);
        motor_position_right = normalizePosition(motor_position_right);
    }

    public void setTarget(double motor_target_left, double motor_target_right) {
        this.motor_target_left = motor_target_left;
        this.motor_target_right = motor_target_right;
    }

    /**
     * Sustained rate of speed for current toggle position
     */
    public double turnSpeed() {
        final double minSpeed = 6; // m/s
        final double maxSpeed = 12; // m/s
        return maxSpeed - (motor_position_left + motor_position_right) / 512 * (maxSpeed - minSpeed);
    }

    /**
     * Left/right balance of current toggle position
     */
    public double turnBalance() {
        return (motor_position_right - motor_position_left) / 255; // [-1..1]
    }

    /**
     * Return motor speed for a given position delta
     * @return motor speed in range -255..255
     */
    private double motorSpeed(double delta) {
        // Start slowing down when delta < 23
        double speed = delta * 10;
        // Minimum speed 32
        if (speed < 0) speed -= 32;
        if (speed > 0) speed += 32;
        // Max speed 255
        if (speed < -255) return -255;
        else if (speed > 255) return 255;
        else return speed;
    }

    private double normalizePosition(double position) {
        return position < 0 ? 0 : (position > 255 ? 255 : position);
    }
}
