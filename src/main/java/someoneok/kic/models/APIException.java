package someoneok.kic.models;

public class APIException extends RuntimeException {
  private final Long resetAt;
  private final Long hardResetAt;
  private final int status;

  public APIException(String message, int status) {
    super(message);
    this.resetAt = null;
    this.hardResetAt = null;
    this.status = status;
  }

  public APIException(String message, int status, Long resetAt, Long hardResetAt) {
    super(message);
    this.resetAt = resetAt;
    this.hardResetAt = hardResetAt;
    this.status = status;
  }

  public Long getResetAt() {
    return resetAt;
  }

  public Long getHardResetAt() {
    return hardResetAt;
  }

  public int getStatus() {
    return status;
  }
}
