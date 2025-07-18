/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.extannotations;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;

import io.opentelemetry.instrumentation.testing.junit.AgentInstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.code.SemconvCodeStabilityUtil;
import io.opentelemetry.sdk.testing.assertj.AttributeAssertion;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** This test verifies that Otel supports various 3rd-party trace annotations */
class TraceProvidersTest {
  @RegisterExtension
  static final InstrumentationExtension testing = AgentInstrumentationExtension.create();

  @ParameterizedTest
  @EnumSource(TraceProvider.class)
  void testShouldSupportProvider(TraceProvider provider) {
    provider.test();

    List<AttributeAssertion> attributeAssertions =
        SemconvCodeStabilityUtil.codeFunctionAssertions(
            SayTracedHello.class, provider.testMethodName());
    attributeAssertions.add(equalTo(stringKey("providerAttr"), provider.name()));

    testing.waitAndAssertTraces(
        trace ->
            trace.hasSpansSatisfyingExactly(
                span ->
                    span.hasName("SayTracedHello." + provider.testMethodName())
                        .hasAttributesSatisfyingExactly(attributeAssertions)));
  }

  @SuppressWarnings("ImmutableEnumChecker")
  private enum TraceProvider {
    AppOptics((sayTracedHello) -> sayTracedHello.appoptics()),
    Datadog((sayTracedHello) -> sayTracedHello.datadog()),
    Dropwizard((sayTracedHello) -> sayTracedHello.dropwizard()),
    KamonOld((sayTracedHello) -> sayTracedHello.kamonold()),
    KamonNew((sayTracedHello) -> sayTracedHello.kamonnew()),
    NewRelic((sayTracedHello) -> sayTracedHello.newrelic()),
    SignalFx((sayTracedHello) -> sayTracedHello.signalfx()),
    Sleuth((sayTracedHello) -> sayTracedHello.sleuth()),
    Tracelytics((sayTracedHello) -> sayTracedHello.tracelytics());

    final Consumer<SayTracedHello> test;

    TraceProvider(Consumer<SayTracedHello> test) {
      this.test = test;
    }

    void test() {
      test.accept(new SayTracedHello());
    }

    String testMethodName() {
      return name().toLowerCase(Locale.ROOT);
    }
  }
}
