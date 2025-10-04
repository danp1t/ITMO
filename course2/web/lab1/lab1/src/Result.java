import java.time.LocalDateTime;

public class Result {
    private double x;
    private double y;
    private double r;
    private String status;
    private Long time;
    private String now;

    public Result(double x, double y, double r, String status, String now, long time) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.status = status;
        this.time = time;
        this.now = now;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getR() {
        return r;
    }
    public String getStatus() {
        return status;
    }
    public Long getTime() {
        return time;
    }
    public String getNow() {return now;}
}
