/*
 * Copyright DataStax, Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.driver.api.core.graph;

import com.datastax.dse.driver.internal.core.graph.DefaultScriptGraphStatement;
import com.datastax.oss.driver.shaded.guava.common.collect.Maps;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Map;
import net.jcip.annotations.NotThreadSafe;

/**
 * A builder to create a script graph statement.
 *
 * <p>This class is mutable and not thread-safe.
 */
@NotThreadSafe
public class ScriptGraphStatementBuilder
    extends GraphStatementBuilderBase<ScriptGraphStatementBuilder, ScriptGraphStatement> {

  private String script;
  private Boolean isSystemQuery;
  private Map<String, Object> queryParams;

  public ScriptGraphStatementBuilder(String script) {
    this.script = script;
    this.queryParams = Maps.newHashMap();
  }

  public ScriptGraphStatementBuilder(ScriptGraphStatement template) {
    super(template);
    this.script = template.getScript();
    this.queryParams = Maps.newHashMap(template.getQueryParams());
    this.isSystemQuery = template.isSystemQuery();
  }

  /** @see ScriptGraphStatement#isSystemQuery() */
  @NonNull
  public ScriptGraphStatementBuilder setSystemQuery(@Nullable Boolean isSystemQuery) {
    this.isSystemQuery = isSystemQuery;
    return this;
  }

  /**
   * Set a value for a parameter defined in the script query.
   *
   * @see ScriptGraphStatement#setQueryParam(String, Object)
   */
  @NonNull
  public ScriptGraphStatementBuilder setQueryParam(@NonNull String name, @Nullable Object value) {
    this.queryParams.put(name, value);
    return this;
  }

  /**
   * Set multiple values for named parameters defined in the script query.
   *
   * @see ScriptGraphStatement#setQueryParam(String, Object)
   */
  @NonNull
  public ScriptGraphStatementBuilder setQueryParams(@NonNull Map<String, Object> params) {
    this.queryParams.putAll(params);
    return this;
  }

  /**
   * Removes a parameter.
   *
   * <p>This is useful if the builder was {@linkplain
   * ScriptGraphStatement#builder(ScriptGraphStatement) initialized with a template statement} that
   * has more parameters than desired.
   *
   * @see ScriptGraphStatement#setQueryParam(String, Object)
   * @see #clearQueryParams()
   */
  @NonNull
  public ScriptGraphStatementBuilder removeQueryParam(@NonNull String name) {
    this.queryParams.remove(name);
    return this;
  }

  /** Clears all the parameters previously added to this builder. */
  public ScriptGraphStatementBuilder clearQueryParams() {
    this.queryParams.clear();
    return this;
  }

  @NonNull
  @Override
  public ScriptGraphStatement build() {
    return new DefaultScriptGraphStatement(
        this.script,
        this.queryParams,
        this.isSystemQuery,
        isIdempotent,
        timeout,
        node,
        timestamp,
        executionProfile,
        executionProfileName,
        buildCustomPayload(),
        graphName,
        traversalSource,
        subProtocol,
        consistencyLevel,
        readConsistencyLevel,
        writeConsistencyLevel);
  }
}