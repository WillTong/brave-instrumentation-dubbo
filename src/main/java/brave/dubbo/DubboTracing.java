package brave.dubbo;

import brave.Tracing;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class DubboTracing {
  public static DubboTracing create(Tracing tracing) {
    return newBuilder(tracing).build();
  }

  public static Builder newBuilder(Tracing tracing) {
    return new AutoValue_DubboTracing.Builder()
        .tracing(tracing)
        .serverName("")
        .clientParser(new DubboClientParser())
        .serverParser(new DubboServerParser());
  }

  public abstract Tracing tracing();

  public abstract DubboClientParser clientParser();

  public abstract String serverName();

  public DubboTracing clientOf(String serverName) {
    return toBuilder().serverName(serverName).build();
  }

  public abstract DubboServerParser serverParser();

  public abstract Builder toBuilder();

  @AutoValue.Builder
  public static abstract class Builder {
    public abstract Builder tracing(Tracing tracing);

    public abstract Builder clientParser(DubboClientParser clientParser);

    public abstract Builder serverParser(DubboServerParser serverParser);

    public abstract DubboTracing build();

    abstract Builder serverName(String serverName);

    Builder() {
    }
  }

  DubboTracing() {
  }
}
