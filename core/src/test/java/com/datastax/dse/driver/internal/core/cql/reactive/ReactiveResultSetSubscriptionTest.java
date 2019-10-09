/*
 * Copyright DataStax, Inc.
 *
 * This software can be used solely with DataStax Enterprise. Please consult the license at
 * http://www.datastax.com/terms/datastax-dse-driver-license-terms
 */
package com.datastax.dse.driver.internal.core.cql.reactive;

import static org.assertj.core.api.Assertions.assertThat;

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow;
import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.ColumnDefinitions;
import com.datastax.oss.driver.api.core.cql.ExecutionInfo;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.servererrors.UnavailableException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.junit.Test;

public class ReactiveResultSetSubscriptionTest {

  @Test
  public void should_retrieve_entire_result_set() {
    CompletableFuture<AsyncResultSet> future1 = new CompletableFuture<>();
    CompletableFuture<AsyncResultSet> future2 = new CompletableFuture<>();
    CompletableFuture<AsyncResultSet> future3 = new CompletableFuture<>();
    MockAsyncResultSet page1 = new MockAsyncResultSet(3, future2);
    MockAsyncResultSet page2 = new MockAsyncResultSet(3, future3);
    MockAsyncResultSet page3 = new MockAsyncResultSet(3, null);
    TestSubscriber<ReactiveRow> mainSubscriber = new TestSubscriber<>();
    TestSubscriber<ColumnDefinitions> colDefsSubscriber = new TestSubscriber<>();
    TestSubscriber<ExecutionInfo> execInfosSubscriber = new TestSubscriber<>();
    TestSubscriber<Boolean> wasAppliedSubscriber = new TestSubscriber<>();
    ReactiveResultSetSubscription<AsyncResultSet> subscription =
        new ReactiveResultSetSubscription<>(
            mainSubscriber, colDefsSubscriber, execInfosSubscriber, wasAppliedSubscriber);
    mainSubscriber.onSubscribe(subscription);
    subscription.start(() -> future1);
    future1.complete(page1);
    future2.complete(page2);
    future3.complete(page3);
    mainSubscriber.awaitTermination();
    List<Row> expected = new ArrayList<>(page1.currentPage());
    expected.addAll(page2.currentPage());
    expected.addAll(page3.currentPage());
    assertThat(mainSubscriber.getElements()).extracting("row").isEqualTo(expected);
    assertThat(colDefsSubscriber.getElements())
        .hasSize(1)
        .containsExactly(page1.getColumnDefinitions());
    assertThat(execInfosSubscriber.getElements())
        .hasSize(3)
        .containsExactly(
            page1.getExecutionInfo(), page2.getExecutionInfo(), page3.getExecutionInfo());
    assertThat(wasAppliedSubscriber.getElements()).hasSize(1).containsExactly(true);
  }

  @Test
  public void should_report_error_on_first_page() {
    CompletableFuture<AsyncResultSet> future1 = new CompletableFuture<>();
    TestSubscriber<ReactiveRow> mainSubscriber = new TestSubscriber<>();
    TestSubscriber<ColumnDefinitions> colDefsSubscriber = new TestSubscriber<>();
    TestSubscriber<ExecutionInfo> execInfosSubscriber = new TestSubscriber<>();
    TestSubscriber<Boolean> wasAppliedSubscriber = new TestSubscriber<>();
    ReactiveResultSetSubscription<AsyncResultSet> subscription =
        new ReactiveResultSetSubscription<>(
            mainSubscriber, colDefsSubscriber, execInfosSubscriber, wasAppliedSubscriber);
    mainSubscriber.onSubscribe(subscription);
    subscription.start(() -> future1);
    future1.completeExceptionally(new UnavailableException(null, null, 0, 0));
    mainSubscriber.awaitTermination();
    assertThat(mainSubscriber.getError()).isNotNull().isInstanceOf(UnavailableException.class);
    assertThat(colDefsSubscriber.getError()).isNotNull().isInstanceOf(UnavailableException.class);
    assertThat(execInfosSubscriber.getError()).isNotNull().isInstanceOf(UnavailableException.class);
    assertThat(wasAppliedSubscriber.getError())
        .isNotNull()
        .isInstanceOf(UnavailableException.class);
  }

  @Test
  public void should_report_synchronous_failure_on_first_page() {
    TestSubscriber<ReactiveRow> mainSubscriber = new TestSubscriber<>();
    TestSubscriber<ColumnDefinitions> colDefsSubscriber = new TestSubscriber<>();
    TestSubscriber<ExecutionInfo> execInfosSubscriber = new TestSubscriber<>();
    TestSubscriber<Boolean> wasAppliedSubscriber = new TestSubscriber<>();
    ReactiveResultSetSubscription<AsyncResultSet> subscription =
        new ReactiveResultSetSubscription<>(
            mainSubscriber, colDefsSubscriber, execInfosSubscriber, wasAppliedSubscriber);
    mainSubscriber.onSubscribe(subscription);
    subscription.start(
        () -> {
          throw new IllegalStateException();
        });
    mainSubscriber.awaitTermination();
    assertThat(mainSubscriber.getError()).isNotNull().isInstanceOf(IllegalStateException.class);
    assertThat(colDefsSubscriber.getError()).isNotNull().isInstanceOf(IllegalStateException.class);
    assertThat(execInfosSubscriber.getError())
        .isNotNull()
        .isInstanceOf(IllegalStateException.class);
    assertThat(wasAppliedSubscriber.getError())
        .isNotNull()
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void should_report_error_on_intermediary_page() {
    CompletableFuture<AsyncResultSet> future1 = new CompletableFuture<>();
    CompletableFuture<AsyncResultSet> future2 = new CompletableFuture<>();
    MockAsyncResultSet page1 = new MockAsyncResultSet(3, future2);
    TestSubscriber<ReactiveRow> mainSubscriber = new TestSubscriber<>();
    TestSubscriber<ColumnDefinitions> colDefsSubscriber = new TestSubscriber<>();
    TestSubscriber<ExecutionInfo> execInfosSubscriber = new TestSubscriber<>();
    TestSubscriber<Boolean> wasAppliedSubscriber = new TestSubscriber<>();
    ReactiveResultSetSubscription<AsyncResultSet> subscription =
        new ReactiveResultSetSubscription<>(
            mainSubscriber, colDefsSubscriber, execInfosSubscriber, wasAppliedSubscriber);
    mainSubscriber.onSubscribe(subscription);
    subscription.start(() -> future1);
    future1.complete(page1);
    future2.completeExceptionally(new UnavailableException(null, null, 0, 0));
    mainSubscriber.awaitTermination();
    assertThat(mainSubscriber.getElements()).extracting("row").isEqualTo(page1.currentPage());
    assertThat(mainSubscriber.getError()).isNotNull().isInstanceOf(UnavailableException.class);
    // colDefsSubscriber completed normally when page1 arrived
    assertThat(colDefsSubscriber.getError()).isNull();
    assertThat(colDefsSubscriber.getElements())
        .hasSize(1)
        .containsExactly(page1.getColumnDefinitions());
    // execInfosSubscriber completed with error, but should have emitted 1 item for page1
    assertThat(execInfosSubscriber.getElements())
        .hasSize(1)
        .containsExactly(page1.getExecutionInfo());
    assertThat(execInfosSubscriber.getError()).isNotNull().isInstanceOf(UnavailableException.class);
    // colDefsSubscriber completed normally when page1 arrived
    assertThat(wasAppliedSubscriber.getElements()).hasSize(1).containsExactly(true);
    assertThat(wasAppliedSubscriber.getError()).isNull();
  }
}