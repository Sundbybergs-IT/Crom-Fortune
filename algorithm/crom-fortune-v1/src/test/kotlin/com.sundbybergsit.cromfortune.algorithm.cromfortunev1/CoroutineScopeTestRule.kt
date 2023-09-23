package com.sundbybergsit.cromfortune.algorithm.cromfortunev1

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.*
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class CoroutineScopeTestRule(private val setMain: Boolean = false) : TestRule {

    private lateinit var coroutineScopeTestRuleBeforeAfter: CoroutineScopeTestRuleBeforeAfter

    val testDispatcher get() = coroutineScopeTestRuleBeforeAfter.testDispatcher
    val testScope get() = coroutineScopeTestRuleBeforeAfter.testScope

    override fun apply(base: Statement, description: Description): CoroutineScopeTestRuleBeforeAfter {
        coroutineScopeTestRuleBeforeAfter = CoroutineScopeTestRuleBeforeAfter(base, setMain)
        return coroutineScopeTestRuleBeforeAfter
    }

    class CoroutineScopeTestRuleBeforeAfter(private val base: Statement, private val setMain: Boolean) : Statement() {

        lateinit var testDispatcher: TestDispatcher
        lateinit var testScope: TestScope

        @Throws(Throwable::class)
        override fun evaluate() {
            // Before
            testDispatcher = UnconfinedTestDispatcher()
            testScope = TestScope(testDispatcher)
            if (setMain) {
                Dispatchers.setMain(testDispatcher)
            }
            try {
                // Execute test
                base.evaluate()
            } finally {
                // After
                testScope.cancel()
                testDispatcher.cancel()
                if (setMain) {
                    Dispatchers.resetMain()
                }
            }
        }

    }

}
