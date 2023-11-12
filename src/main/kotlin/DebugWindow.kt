import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import javax.swing.JFrame
import javax.swing.JPanel


interface Figure {
    val rect: Rectangle
    val color: Color

    fun draw(g: Graphics)
}

data class RectangleDebugFigure(override val rect: Rectangle, override val color: Color): Figure {
    override fun draw(g: Graphics) {
        g.color = color
        g.drawRect(rect.x, rect.y, rect.width, rect.height)
    }
}
data class CircleDebugFigure(override val rect: Rectangle, override val color: Color): Figure {
    override fun draw(g: Graphics) {
        g.color = color
        g.drawOval(rect.x, rect.y, rect.width, rect.height)
    }
}

class DebugPanel: JPanel() {
    init {
        val screen = Toolkit.getDefaultToolkit().screenSize
        size = Dimension(screen.width, screen.width)
        preferredSize = Dimension(screen.width, screen.width)
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
        g.drawString("${mouseLocation.x}:${mouseLocation.y}", mouseLocation.x, mouseLocation.y)
    }
}

class DebugWindow: JFrame() {
    private var panel: DebugPanel
    init {
        isUndecorated = true
        background = Color(0, 0, 0, 0)
        setAlwaysOnTop(true)
        setDefaultCloseOperation(EXIT_ON_CLOSE)
        val screen = Toolkit.getDefaultToolkit().screenSize
        size = Dimension(screen.width, screen.width)

        panel = DebugPanel()
        panel.setOpaque(false)
        this.add(panel)

        setExtendedState(MAXIMIZED_BOTH)
        setResizable(false)
        isVisible = true
    }

    fun addFigure(figure: Figure) {
        panel.addFigure(figure)
    }
}
