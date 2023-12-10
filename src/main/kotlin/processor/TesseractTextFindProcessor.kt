package processor

import CircleDebugFigure
import DebugWindow
import RectangleDebugFigure
import database.core.H2Database
import net.sourceforge.tess4j.Tesseract
import processor.actions.ActionFactory
import processor.configs.ListTargets
import java.awt.Color
import java.awt.Point
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities
import kotlin.io.path.Path
import kotlin.system.exitProcess

class TesseractTextFindProcessor {
    private var scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private val executor: ExecutorService = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors() * 2
    )
    private val propertiesPath: File = Path("config.yaml").toFile()
    private val properties: ListTargets = PropertiesParser.parseProperties(propertiesPath)
    private val actionFactory = ActionFactory()
    private val database = H2Database("screen_scanner", "h2user", "")
    fun textFindOnImage(bufferedImage: BufferedImage): String {
        //Это не хорошо, но сделано для потокобезопасности
        val instance: Tesseract = Tesseract()
        return instance.doOCR(bufferedImage)
    }

    fun run() {
        database.deleteAllDataForLastWeek()
        SwingUtilities.invokeLater {
            //FIXME всё ещё по JFrame создаётся на каждый объект
            // производительность это не решает, но выглядит стрёмно
            val window = DebugWindow(
                properties,
                this::starScanning,
                this::stopScanning,
                this::stopApplication,
                this::printLogs
            )

            properties.targets.forEach { target ->
               window.addFigure(
                   RectangleDebugFigure(
                    Rectangle(target.areaX, target.areaY, target.areaWidth, target.areaHeight),
                    Color.GREEN
                   )
               )
                window.addFigure(
                    CircleDebugFigure(
                        Rectangle(target.action.locationX, target.action.locationY, 10, 10),
                        Color.BLUE
                    )
                )
            }
            properties.beforeActionsList.listActions.forEach { action ->
                window.addFigure(
                    CircleDebugFigure(
                        Rectangle(action.locationX, action.locationY, 10, 10),
                        Color.RED
                    )
                )
            }
            properties.afterActionsList.listActions.forEach { action ->
                window.addFigure(
                    CircleDebugFigure(
                        Rectangle(action.locationX, action.locationY, 10, 10),
                        Color.YELLOW
                    )
                )
            }
        }
    }

    fun starScanning() {
        scheduler.scheduleWithFixedDelay({
            val beforeDelay = properties.beforeActionsList.delayBetweenActions
            properties.beforeActionsList.listActions.forEach { action ->
                val customAction = actionFactory.getAction(action.actionType)
                customAction.make(Point(action.locationX + 5, action.locationY + 5))
                Thread.sleep(beforeDelay)
            }

            val countDownLatch = CountDownLatch(properties.targets.size)
            properties.targets.forEach { target ->
                executor.submit {
                    try {
                        val resultText = textFindOnImage(target.getBufferedImage())
                        println("Result: { $resultText }")
                        executor.submit { database.addObject(resultText.trim()) }
                        //extends on others types search substring
                        if (resultText.contains(target.action.targetText)) {
                            val action = actionFactory.getAction(target.action.actionType)
                            action.make(Point(target.action.locationX + 5, target.action.locationY + 5))
                        }
                    } finally {
                        countDownLatch.countDown()
                    }
                }
            }

            countDownLatch.await()
            val afterDelay = properties.beforeActionsList.delayBetweenActions
            properties.afterActionsList.listActions.forEach { action ->
                val customAction = actionFactory.getAction(action.actionType)
                customAction.make(Point(action.locationX + 5, action.locationY + 5))
                Thread.sleep(afterDelay)
            }
        }, 0 ,properties.delay, TimeUnit.MILLISECONDS)
    }

    fun stopScanning() {
        scheduler.shutdown()
        if(!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
            scheduler.shutdownNow()
        }
        scheduler = Executors.newScheduledThreadPool(1)
    }

    fun stopApplication() {
        PropertiesParser.writeProperties(propertiesPath, properties)
        exitProcess(-1)
    }

    fun printLogs() {
        executor.submit { println("______________________\n${database.getObjectsPeerToday()}\n____________________") }
    }
}