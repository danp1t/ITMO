public class Result {
    private double x;
    private double y;
    private double r;
    private String status;

    public Result(double x, double y, double r, String status) {
        this.x = x;
        this.y = y;
        this.r = r;
        this.status = status;
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
}
