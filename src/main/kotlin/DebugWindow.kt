import processor.configs.ListTargets
import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JFrame
import javax.swing.JPanel
import kotlin.math.pow
import kotlin.reflect.KFunction0

interface Figure {
    val rect: Rectangle
    val color: Color

    fun draw(g: Graphics)
    fun isHere(clickPoint: Point): Boolean
}

data class RectangleDebugFigure(override val rect: Rectangle, override val color: Color): Figure {
    override fun draw(g: Graphics) {
        g.color = color
        g.drawRect(rect.x, rect.y, rect.width, rect.height)
    }

    override fun isHere(clickPoint: Point): Boolean {
        TODO("Not yet implemented")
    }
}
data class CircleDebugFigure(override val rect: Rectangle, override val color: Color): Figure {
    override fun draw(g: Graphics) {
        g.color = color
        g.drawOval(rect.x, rect.y, rect.width, rect.height)
    }

    override fun isHere(clickPoint: Point): Boolean {
        TODO("Not yet implemented")
    }
}

class DebugPanel(val properties: ListTargets): JPanel() {
    init {
        val screen = Toolkit.getDefaultToolkit().screenSize
        size = Dimension(screen.width, screen.width)
        preferredSize = Dimension(screen.width, screen.width)

        this.addMouseListener(object : MouseAdapter() {
            var selectedFigure: Figure? = null
            var collisRadius: Double = 10.0
            override fun mousePressed(e: MouseEvent) {
                print("Mouse down")
                figuresList.forEach { figure ->
//                    collisRadius = figure.rect.width / 2.0
                    val powX = (e.x.toDouble() - figure.rect.x.toDouble()).pow(2.0)
                    val powY = (e.y.toDouble() - figure.rect.y.toDouble()).pow(2.0)
                    if (collisRadius.pow(2.0) >= powX + powY) {
                        println("powX: $powX; powY: $powY " +
                                "${if (collisRadius.pow(2.0) >= powX + powY) "=" 
                                else "><"} ${collisRadius.pow(2.0)}"
                        )
                        println("pow = (${e.x.toDouble()} - ${figure.rect.x.toDouble()})^2")
                        println("Found figure: $figure")
                        selectedFigure = figure
                        return@forEach
                    }
                }
            }

            override fun mouseReleased(e: MouseEvent) {
                selectedFigure?.also {
                    val target = properties.foundTarget(Point(it.rect.x, it.rect.y))
                    val action = properties.foundActions(Point(it.rect.x, it.rect.y))
                    println("11111. target: $target or action: $action")
                    if (target != null) {
                        target.areaX = e.x
                        target.areaY = e.y
                    } else if (action != null) {
                        action.locationX = e.x
                        action.locationY = e.y
                    } else {
                        println("NOT FOUND TARGETS OR ACTIONS")
                    }

                    selectedFigure!!.rect.x = e.x
                    selectedFigure!!.rect.y = e.y
//                  Ищем в пропертис это таргет и меняем его координаты тоже
                    println("New figure point: $selectedFigure")
                    //Сука костыль потому что эта панель ёбнутая
                    //Она перерисовывает только если что-то меняется в панели
                    if (background == Color(0, 0, 0, 128)) {
                        background = Color(0, 0, 0, 129)
                    } else {
                        background = Color(0, 0, 0, 128)
                    }
                    paintComponent(graphics)
                    selectedFigure = null
                }
            }
        })

        println("Size: $size")
        println("Preferred: $preferredSize")
        println("Bounds: $bounds")
    }

    private val figuresList: MutableList<Figure> = mutableListOf()
    private var mouseLocation: Point = Point(0, 0)

    fun addFigure(rec: Figure) {
        figuresList.add(rec)
        paintComponent(graphics)
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        figuresList.forEach { figure ->
            println("Redraw: $figure")
            figure.draw(g)
        }
    }
}

class DebugWindow(
    val properties: ListTargets,
    val start: KFunction0<Unit>,
    val stop: KFunction0<Unit>,
    val stopAppl: KFunction0<Unit>,
    val logs: KFunction0<Unit>
): JFrame() {
    private var panel: DebugPanel
    init {
        isUndecorated = true
        background = Color(0, 0, 0, 128)
        setAlwaysOnTop(true)
        setDefaultCloseOperation(EXIT_ON_CLOSE)
        val screen = Toolkit.getDefaultToolkit().screenSize
        size = Dimension(screen.width, screen.width)

        panel = DebugPanel(properties)
        panel.setOpaque(false)
        this.add(panel)

        this.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: java.awt.event.KeyEvent) {
                if (java.awt.event.KeyEvent.VK_S == e.keyCode || java.awt.event.KeyEvent.VK_Y == e.keyCode) {
                    start()
                    background = Color(0, 0, 0, 0)
                } else if (java.awt.event.KeyEvent.VK_D == e.keyCode || java.awt.event.KeyEvent.VK_V == e.keyCode)  {
                    stop()
                    background = Color(0, 0, 0, 128)
                }
                else if (java.awt.event.KeyEvent.VK_L == e.keyCode)  {
                    logs()
                }
                else if (java.awt.event.KeyEvent.VK_ESCAPE == e.keyCode)  {
                    stopAppl()
                }
            }

            override fun keyReleased(e: java.awt.event.KeyEvent) {

            }
        })

        setExtendedState(MAXIMIZED_BOTH)
        setResizable(false)
        isVisible = true
    }

    fun addFigure(figure: Figure) {
        panel.addFigure(figure)
    }
}
