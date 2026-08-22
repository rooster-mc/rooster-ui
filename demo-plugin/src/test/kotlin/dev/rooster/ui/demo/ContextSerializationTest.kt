package dev.rooster.ui.demo

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.rooster.ui.context.YmlInterfaceContextProvider
import dev.rooster.ui.demo.ui.TestScrollInterface
import dev.rooster.ui.interfaces.constructors.indexed_content.ScrollContext
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ContextSerializationTest : InterfaceTestBase() {
    @Test
    fun `gson round-trips tracked values`() {
        val ctx = CounterContext()
        ctx.value = 7
        val json = Gson().toJson(ctx.trackedValues())
        val values: Map<String, Any> = Gson().fromJson(json, object : TypeToken<Map<String, Any>>() {}.type)
        val restored = CounterContext()
        restored.restoreTrackedValues(values)
        assertEquals(7, restored.value)
    }

    @Test
    fun `yml provider persists and restores context`() {
        val provider = YmlInterfaceContextProvider()
        val ctx = ScrollContext().apply { position = 3 }
        provider.updateContext(player, TestScrollInterface, ctx)

        val loaded = provider.getContext<ScrollContext>(player, TestScrollInterface)
        assertEquals(3, loaded?.position)
    }
}
