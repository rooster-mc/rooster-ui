package dev.rooster.ui.demo

import dev.rooster.ui.interfaces.Context
import dev.rooster.ui.interfaces.InterfaceInfo
import dev.rooster.ui.tracking.CachableLambda
import dev.rooster.ui.tracking.track
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CounterContext : Context() {
    var value by track(0)
}

class DependencyTrackingTest : InterfaceTestBase() {
    @Test
    fun `caches while tracked context is unchanged and re-evaluates on change`() {
        val ctx = CounterContext()
        var evaluations = 0
        val lambda = CachableLambda<CounterContext, Int> {
            evaluations++
            ctx.value * 2
        }
        val info = InterfaceInfo(0, ctx, player)

        assertEquals(0, lambda(info))
        assertEquals(1, evaluations)
        assertEquals(0, lambda(info))
        assertEquals(1, evaluations)

        ctx.value = 5
        assertEquals(10, lambda(info))
        assertEquals(2, evaluations)
        assertEquals(10, lambda(info))
        assertEquals(2, evaluations)
    }

    @Test
    fun `constant lambda evaluates once`() {
        var evaluations = 0
        val lambda = CachableLambda<CounterContext, Int> {
            evaluations++
            42
        }
        val info = InterfaceInfo(0, CounterContext(), player)

        assertEquals(42, lambda(info))
        assertEquals(42, lambda(info))
        assertEquals(1, evaluations)
    }

    @Test
    fun `re-evaluates when slot dependency changes`() {
        var evaluations = 0
        val lambda = CachableLambda<CounterContext, Int> {
            evaluations++
            slot * 10
        }
        val infoA = InterfaceInfo(1, CounterContext(), player)
        val infoB = InterfaceInfo(2, CounterContext(), player)

        assertEquals(10, lambda(infoA))
        assertEquals(20, lambda(infoB))
        assertEquals(2, evaluations)
    }
}
