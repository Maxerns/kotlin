/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.internal.testing.tcsmc

import jetbrains.buildServer.messages.serviceMessages.TestFinished
import jetbrains.buildServer.messages.serviceMessages.TestStarted
import jetbrains.buildServer.messages.serviceMessages.TestSuiteFinished
import jetbrains.buildServer.messages.serviceMessages.TestSuiteStarted
import kotlin.test.Test
import kotlin.test.assertEquals

class TestFailureTest : TCServiceMessagesClientTest() {
    @Test
    fun testJs() {
        assertEvents(
            """
STARTED SUITE root // root
  STARTED SUITE  // root/
    STARTED TEST displayName: Test, classDisplayName: , className: , name: Test // root//Test
      FAILURE AssertionError: Expected value to be true.
    at AssertionError_init_0 (mpplib2/build/tmp/expandedArchives/kotlin-stdlib-js-1.3-SNAPSHOT.jar_730a1b227513cf16a9b639e009a985fc/kotlin/exceptions.kt:102:37)
    at DefaultJsAsserter.failWithMessage_0 (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/src/main/kotlin/kotlin/test/DefaultJsAsserter.kt:80:19)
    at DefaultJsAsserter.assertTrue_o10pc4${'$'} (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/src/main/kotlin/kotlin/test/DefaultJsAsserter.kt:60:13)
    at DefaultJsAsserter.assertTrue_4mavae${'$'} (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/src/main/kotlin/kotlin/test/DefaultJsAsserter.kt:67:9)
    at assertTrue_0 (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/Assertions.kt:36:21)
    at SampleTestsJS.testHello (mpplib2/src/jsTest/kotlin/sample/SampleTestsJS.kt:9:9)
    at mpplib2/build/js_test_node_modules/mpplib2_test.js:59:38
    at Object.fn [as test] (mpplib2/build/tmp/expandedArchives/src/KotlinTestRunner.ts:12:25)
    at Object.test (mpplib2/build/tmp/expandedArchives/src/KotlinTestTeamCityReporter.ts:80:28)
    at test (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/src/main/kotlin/kotlin/test/TestApi.kt:57:15) // root//Test
    COMPLETED FAILURE // root//Test
  COMPLETED FAILURE // root/
COMPLETED FAILURE // root
        """
        ) {
            serviceMessage(TestSuiteStarted(""))
            serviceMessage(TestStarted("Test", false, null))
            serviceMessage(
                "testFailed",
                mapOf(
                    "name" to "Test",
                    "message" to "Expected value to be true",
                    "details" to """AssertionError: Expected value to be true.
    at AssertionError_init_0 (mpplib2/build/tmp/expandedArchives/kotlin-stdlib-js-1.3-SNAPSHOT.jar_730a1b227513cf16a9b639e009a985fc/kotlin/exceptions.kt:102:37)
    at DefaultJsAsserter.failWithMessage_0 (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/src/main/kotlin/kotlin/test/DefaultJsAsserter.kt:80:19)
    at DefaultJsAsserter.assertTrue_o10pc4$ (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/src/main/kotlin/kotlin/test/DefaultJsAsserter.kt:60:13)
    at DefaultJsAsserter.assertTrue_4mavae$ (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/src/main/kotlin/kotlin/test/DefaultJsAsserter.kt:67:9)
    at assertTrue_0 (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/Assertions.kt:36:21)
    at SampleTestsJS.testHello (mpplib2/src/jsTest/kotlin/sample/SampleTestsJS.kt:9:9)
    at mpplib2/build/js_test_node_modules/mpplib2_test.js:59:38
    at Object.fn [as test] (mpplib2/build/tmp/expandedArchives/src/KotlinTestRunner.ts:12:25)
    at Object.test (mpplib2/build/tmp/expandedArchives/src/KotlinTestTeamCityReporter.ts:80:28)
    at test (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/src/main/kotlin/kotlin/test/TestApi.kt:57:15)"""
                )
            )
            serviceMessage(TestFinished("Test", 0))
            serviceMessage(TestSuiteFinished(""))
        }
    }

    @Test
    fun testNative() {
        treatFailedTestOutputAsStacktrace = true

        assertEvents(
            """
STARTED SUITE root // root
  STARTED SUITE  // root/
    STARTED TEST displayName: Test, classDisplayName: , className: , name: Test // root//Test
      FAILURE Expected <7>, actual <42>
AssertionError: Expected value to be true.
    at AssertionError_init_0 (mpplib2/build/tmp/expandedArchives/kotlin-stdlib-js-1.3-SNAPSHOT.jar_730a1b227513cf16a9b639e009a985fc/kotlin/exceptions.kt:102:37)
    at DefaultJsAsserter.failWithMessage_0 (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/src/main/kotlin/kotlin/test/DefaultJsAsserter.kt:80:19)
    at DefaultJsAsserter.assertTrue_o10pc4${'$'} (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/src/main/kotlin/kotlin/test/DefaultJsAsserter.kt:60:13)
    at DefaultJsAsserter.assertTrue_4mavae${'$'} (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/src/main/kotlin/kotlin/test/DefaultJsAsserter.kt:67:9)
    at assertTrue_0 (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/Assertions.kt:36:21)
    at SampleTestsJS.testHello (mpplib2/src/jsTest/kotlin/sample/SampleTestsJS.kt:9:9)
    at mpplib2/build/js_test_node_modules/mpplib2_test.js:59:38
    at Object.fn [as test] (mpplib2/build/tmp/expandedArchives/src/KotlinTestRunner.ts:12:25)
    at Object.test (mpplib2/build/tmp/expandedArchives/src/KotlinTestTeamCityReporter.ts:80:28)
    at test (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/src/main/kotlin/kotlin/test/TestApi.kt:57:15) // root//Test
    COMPLETED FAILURE // root//Test
  COMPLETED FAILURE // root/
COMPLETED FAILURE // root
        """
        ) {
            serviceMessage(TestSuiteStarted(""))
            serviceMessage(TestStarted("Test", false, null))
            regularText(
                """AssertionError: Expected value to be true.
    at AssertionError_init_0 (mpplib2/build/tmp/expandedArchives/kotlin-stdlib-js-1.3-SNAPSHOT.jar_730a1b227513cf16a9b639e009a985fc/kotlin/exceptions.kt:102:37)
    at DefaultJsAsserter.failWithMessage_0 (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/src/main/kotlin/kotlin/test/DefaultJsAsserter.kt:80:19)
    at DefaultJsAsserter.assertTrue_o10pc4$ (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/src/main/kotlin/kotlin/test/DefaultJsAsserter.kt:60:13)
    at DefaultJsAsserter.assertTrue_4mavae$ (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/src/main/kotlin/kotlin/test/DefaultJsAsserter.kt:67:9)
    at assertTrue_0 (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/Assertions.kt:36:21)
    at SampleTestsJS.testHello (mpplib2/src/jsTest/kotlin/sample/SampleTestsJS.kt:9:9)
    at mpplib2/build/js_test_node_modules/mpplib2_test.js:59:38
    at Object.fn [as test] (mpplib2/build/tmp/expandedArchives/src/KotlinTestRunner.ts:12:25)
    at Object.test (mpplib2/build/tmp/expandedArchives/src/KotlinTestTeamCityReporter.ts:80:28)
    at test (mpplib2/build/tmp/expandedArchives/kotlin-test-js-1.3-SNAPSHOT.jar_d60f1e6d0dd94843a03bf98a569bbb73/src/main/kotlin/kotlin/test/TestApi.kt:57:15)"""
            )
            serviceMessage(
                "testFailed",
                mapOf(
                    "name" to "Test",
                    "message" to "Expected <7>, actual <42>"
                )
            )
            serviceMessage(TestFinished("Test", 0))
            serviceMessage(TestSuiteFinished(""))
        }
    }

    @Test
    fun `closing failed test suite with a started test attaches failure to the test node`() {
        assertEvents(
            $$"""
            STARTED SUITE root // root
              STARTED SUITE  // root/
                STARTED TEST displayName: Test, classDisplayName: , className: , name: Test // root//Test
                  FAILURE java.lang.RuntimeException: Test timed out
            	at org.jetbrains.kotlin.gradle.internal.testing.tcsmc.TestFailureTest.closing_failed_test_suite_with_a_started_test_attaches_failure_to_the_test_node$lambda$0(TestFailureTest.kt:239)
            	at org.jetbrains.kotlin.gradle.internal.testing.tcsmc.TCServiceMessagesClientTest.assertEvents$kotlin_gradle_plugin_test(TCServiceMessagesClientTest.kt:20)
            	at org.jetbrains.kotlin.gradle.internal.testing.tcsmc.TestFailureTest.closing failed test suite with a started test attaches failure to the test node(TestFailureTest.kt:119)
            	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
            	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
            	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
            	at java.base/java.lang.reflect.Method.invoke(Method.java:569)
            	at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:775)
            	at org.junit.platform.commons.support.ReflectionSupport.invokeMethod(ReflectionSupport.java:479)
            	at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)
            	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131)
            	at org.jetbrains.kotlin.test.MuteInInvocationInterceptor.interceptWithMuteInDatabase(muteWithDatabaseJunit5.kt:122)
            	at org.jetbrains.kotlin.test.MuteInInvocationInterceptor.interceptTestMethod(muteWithDatabaseJunit5.kt:87)
            	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:112)
            	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:94)
            	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106)
            	at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:161)
            	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:152)
            	at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:91)
            	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:112)
            	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:94)
            	at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106)
            	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64)
            	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45)
            	at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37)
            	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:93)
            	at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:87)
            	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$7(TestMethodTestDescriptor.java:216)
            	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
            	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:212)
            	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:137)
            	at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:69)
            	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:156)
            	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
            	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146)
            	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
            	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144)
            	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
            	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143)
            	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100)
            	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
            	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41)
            	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160)
            	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
            	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146)
            	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
            	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144)
            	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
            	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143)
            	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100)
            	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
            	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41)
            	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:160)
            	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
            	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:146)
            	at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
            	at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:144)
            	at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
            	at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:143)
            	at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:100)
            	at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35)
            	at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57)
            	at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54)
            	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:201)
            	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:170)
            	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:94)
            	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:59)
            	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:142)
            	at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:58)
            	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:103)
            	at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:85)
            	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47)
            	at org.junit.platform.launcher.core.InterceptingLauncher.lambda$execute$1(InterceptingLauncher.java:39)
            	at org.junit.platform.launcher.core.ClasspathAlignmentCheckingLauncherInterceptor.intercept(ClasspathAlignmentCheckingLauncherInterceptor.java:25)
            	at org.junit.platform.launcher.core.InterceptingLauncher.execute(InterceptingLauncher.java:38)
            	at org.junit.platform.launcher.core.DelegatingLauncher.execute(DelegatingLauncher.java:47)
            	at org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestDefinitionProcessor$CollectThenExecuteTestDefinitionConsumer.processAllTestDefinitions(JUnitPlatformTestDefinitionProcessor.java:179)
            	at org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestDefinitionProcessor$CollectThenExecuteTestDefinitionConsumer.access$000(JUnitPlatformTestDefinitionProcessor.java:122)
            	at org.gradle.api.internal.tasks.testing.junitplatform.JUnitPlatformTestDefinitionProcessor.stop(JUnitPlatformTestDefinitionProcessor.java:116)
            	at org.gradle.api.internal.tasks.testing.SuiteTestDefinitionProcessor.stop(SuiteTestDefinitionProcessor.java:63)
            	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
            	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:77)
            	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
            	at java.base/java.lang.reflect.Method.invoke(Method.java:569)
            	at org.gradle.internal.dispatch.MethodInvocation.invokeOn(MethodInvocation.java:77)
            	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:28)
            	at org.gradle.internal.dispatch.ReflectionDispatch.dispatch(ReflectionDispatch.java:19)
            	at org.gradle.internal.dispatch.ContextClassLoaderDispatch.dispatch(ContextClassLoaderDispatch.java:33)
            	at org.gradle.internal.dispatch.ProxyDispatchAdapter$DispatchingInvocationHandler.invoke(ProxyDispatchAdapter.java:88)
            	at jdk.proxy1/jdk.proxy1.$Proxy4.stop(Unknown Source)
            	at org.gradle.api.internal.tasks.testing.worker.TestWorker$3.run(TestWorker.java:195)
            	at org.gradle.api.internal.tasks.testing.worker.TestWorker.executeAndMaintainThreadName(TestWorker.java:126)
            	at org.gradle.api.internal.tasks.testing.worker.TestWorker.execute(TestWorker.java:103)
            	at org.gradle.api.internal.tasks.testing.worker.TestWorker.execute(TestWorker.java:63)
            	at org.gradle.process.internal.worker.child.ActionExecutionWorker.execute(ActionExecutionWorker.java:56)
            	at org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call(SystemApplicationClassLoaderWorker.java:122)
            	at org.gradle.process.internal.worker.child.SystemApplicationClassLoaderWorker.call(SystemApplicationClassLoaderWorker.java:72)
            	at worker.org.gradle.process.internal.worker.GradleWorkerMain.run(GradleWorkerMain.java:69)
            	at worker.org.gradle.process.internal.worker.GradleWorkerMain.main(GradleWorkerMain.java:74)
             // root//Test
                COMPLETED FAILURE // root//Test
              COMPLETED FAILURE // root/
            COMPLETED FAILURE // root
        """.trimIndent()
        ) {
            serviceMessage(TestSuiteStarted(""))
            serviceMessage(TestStarted("Test", false, null))

            closeSuiteWithFailingTestCause(
                suiteNode = SuiteNode(
                    parent = RootNode(),
                    name = "",
                ),
                ts = System.currentTimeMillis(),
                failingTestCause = RuntimeException("Test timed out"),
            )
        }
    }

    @Test
    fun `closing failed test suite with no started tests re-throws failure`() = runWithClient {
        serviceMessage(TestSuiteStarted(""))

        val failingTestCause = RuntimeException("Test timed out")
        try {
            closeSuiteWithFailingTestCause(
                suiteNode = SuiteNode(
                    parent = RootNode(),
                    name = "",
                ),
                ts = System.currentTimeMillis(),
                failingTestCause = failingTestCause,
            )
        } catch (t: Throwable) {
            assertEquals(failingTestCause, t)
        }
    }
}
