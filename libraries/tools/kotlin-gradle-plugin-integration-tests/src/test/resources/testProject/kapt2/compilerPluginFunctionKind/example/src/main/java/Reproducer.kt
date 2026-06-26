package repro

import repro.processor.Trigger

@Target(AnnotationTarget.TYPE)
annotation class PluginFunction

@Trigger
interface TestInterface {
    fun test(block: @PluginFunction () -> Unit)
}
