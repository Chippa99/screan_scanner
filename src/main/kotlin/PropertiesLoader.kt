import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import processor.configs.ListTargets
import java.io.File

object PropertiesParser {
    private const val DEFAULT_PROPERTIES_FILE_NAME = "config.yaml"
    private val objectMapper = ObjectMapper(YAMLFactory())

    fun parseProperties(filePath: File): ListTargets {
        val path = if (filePath.exists())
            filePath
        else
            File(this.javaClass.getResource(DEFAULT_PROPERTIES_FILE_NAME)!!.toURI())

        val config = objectMapper.readValue(path, ListTargets::class.java)
        println("Application config from $path")
        return config
    }

    fun writeProperties(filePath: File, properties: ListTargets) {
        val path = if (filePath.exists())
            filePath
        else
            File(this.javaClass.getResource(DEFAULT_PROPERTIES_FILE_NAME)!!.toURI())

        objectMapper.writeValue(path, properties)
        println("Application config saved on path: $path")
    }
}