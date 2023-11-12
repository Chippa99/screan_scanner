import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import processor.configs.ListTargets
import java.io.File

object PropertiesParser {
    private const val DEFAULT_PROPERTIES_FILE_NAME = "config.yaml"

    fun parseProperties(filePath: File): ListTargets {
        val path = if (filePath.exists())
            filePath
        else
            File(this.javaClass.getResource(DEFAULT_PROPERTIES_FILE_NAME)!!.toURI())
        val objectMapper = ObjectMapper(YAMLFactory())
        val config = objectMapper.readValue(path, ListTargets::class.java)
        println("Application config info $config")
        return config
    }
}