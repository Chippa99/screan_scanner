package processor

import CircleDebugFigure
import DebugWindow
import RectangleDebugFigure
import net.sourceforge.tess4j.Tesseract
import processor.actions.ActionFactory
import processor.configs.ListTargets
import java.awt.Color
import java.awt.Point
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import java.lang.Exception
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.swing.SwingUtilities
import kotlin.io.path.Path

class TesseractTextFindProcessor() {
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(
        1
    )
    private val executor: ExecutorService = Executors.newFixedThreadPool(
        Runtime.getRuntime().availableProcessors() * 2
    )
    private val propertiesPath: File = Path("config.yaml").toFile()
    private val properties: ListTargets = PropertiesParser.parseProperties(propertiesPath)
    private val actionFactory = ActionFactory()

    fun textFindOnImage(bufferedImage: BufferedImage): String {
        //Это не хорошо, но сделано для потокобезопасности
        val instance: Tesseract = Tesseract()
        return instance.doOCR(bufferedImage)
    }

    fun run() {
        SwingUtilities.invokeLater {
            //FIXME всё ещё по JFrame создаётся на каждый объект
            // производительность это не решает, но выглядит стрёмно
            val window = DebugWindow()
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
        scheduler.scheduleWithFixedDelay({
            val beforeDelay = properties.beforeActionsList.delayBetweenActions
            properties.beforeActionsList.listActions.forEach { action ->
                val customAction = actionFactory.getAction(action.actionType)
                customAction.make(Point(action.locationX + 5, action.locationY + 5))
                Thread.sleep(beforeDelay)
            }

            val start = System.currentTimeMillis()
            val countDownLatch = CountDownLatch(properties.targets.size)
            properties.targets.forEach { target ->
                executor.submit {
                    try {
                        val resultText = textFindOnImage(target.getBufferedImage())
                        println("Result: $resultText")
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
            val end = System.currentTimeMillis()
            println("Execution time: ${end-start}")
            val afterDelay = properties.beforeActionsList.delayBetweenActions
            properties.afterActionsList.listActions.forEach { action ->
                val customAction = actionFactory.getAction(action.actionType)
                customAction.make(Point(action.locationX + 5, action.locationY + 5))
                Thread.sleep(afterDelay)
            }
        }, 0 ,properties.delay, TimeUnit.MILLISECONDS)
    }
}