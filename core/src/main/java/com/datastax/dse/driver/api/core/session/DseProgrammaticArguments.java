/*
 * Copyright DataStax, Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.driver.api.core.session;

import com.datastax.dse.driver.api.core.DseSessionBuilder;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.UUID;

/**
 * The DSE-specific arguments that can be set programmatically when building a session.
 *
 * <p>This is mostly for internal use, you only need to deal with this directly if you write custom
 * {@link DseSessionBuilder} subclasses.
 */
public class DseProgrammaticArguments {

  @NonNull
  public static Builder builder() {
    return new Builder();
  }

  private final UUID startupClientId;
  private final String startupApplicationName;
  private final String startupApplicationVersion;

  private DseProgrammaticArguments(
      @Nullable UUID startupClientId,
      @Nullable String startupApplicationName,
      @Nullable String startupApplicationVersion) {
    this.startupClientId = startupClientId;
    this.startupApplicationName = startupApplicationName;
    this.startupApplicationVersion = startupApplicationVersion;
  }

  @Nullable
  public UUID getStartupClientId() {
    return startupClientId;
  }

  @Nullable
  public String getStartupApplicationName() {
    return startupApplicationName;
  }

  @Nullable
  public String getStartupApplicationVersion() {
    return startupApplicationVersion;
  }

  public static class Builder {

    private UUID startupClientId;
    private String startupApplicationName;
    private String startupApplicationVersion;

    @NonNull
    public Builder withStartupClientId(@Nullable UUID startupClientId) {
      this.startupClientId = startupClientId;
      return this;
    }

    @NonNull
    public Builder withStartupApplicationName(@Nullable String startupApplicationName) {
      this.startupApplicationName = startupApplicationName;
      return this;
    }

    @NonNull
    public Builder withStartupApplicationVersion(@Nullable String startupApplicationVersion) {
      this.startupApplicationVersion = startupApplicationVersion;
      return this;
    }

    @NonNull
    public DseProgrammaticArguments build() {
      return new DseProgrammaticArguments(
          startupClientId, startupApplicationName, startupApplicationVersion);
    }
  }
}