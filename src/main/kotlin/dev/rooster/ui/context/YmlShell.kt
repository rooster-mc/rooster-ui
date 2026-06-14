package dev.rooster.ui.context

import dev.rooster.ui.RoosterUI
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import kotlin.collections.forEach
import kotlin.text.isNotEmpty

interface YmlOperations {
    val file: File
    val config: FileConfiguration

    fun saveConfig() {
        config.save(file)
    }

    fun changeConfig(action: () -> Unit) {
        action()
        saveConfig()
    }

    companion object {
        /**
         * Finds a file in the plugin folder. Note, use `.exists()` to check
         * whether that directory is filled.
         */
        fun findFile(fileName: String, directory: String = "", baseDirectory: String = RoosterUI.pluginFolder.path): File =
            File(baseDirectory, if (directory.isNotEmpty()) "$directory/$fileName" else fileName)

        fun delete(target: File): Boolean {
            if (target.isDirectory) {
                target.listFiles()?.forEach { delete(it) }
            }
            return target.delete()
        }

        fun move(source: File, destination: File): Boolean {
            if (source.isDirectory) {
                destination.mkdirs() // Ensure destination directories are created
                source.listFiles()?.forEach { file ->
                    move(file, File(destination, file.name))
                }
                return delete(source) // Optionally, delete the source after moving
            }
            return source.renameTo(destination)
        }

        fun rename(source: File, newName: String): Boolean {
            val destination = File(source.parentFile, newName)
            return move(source, destination)
        }
    }
}

open class YmlShell(
    fileName: String,
    directory: String = "",
    baseDirectory: String = RoosterUI.pluginFolder.path
) : YmlOperations {
    override val file = File(baseDirectory, if (directory.isNotEmpty()) "$directory/$fileName" else fileName)
    override val config: FileConfiguration by lazy { YamlConfiguration.loadConfiguration(file) }
}
