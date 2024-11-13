package app.tinygiants.getalife.domain.model

typealias EmptyProgress = Progress

data class Progress(

    val bar2VisibilityState: Boolean = false,
    val optionalText: String = "",

    val bar1: Float = 0f,
    val bar1Lite: Float = 0f,
    val bar2: Float = 0f,
    val bar2Lite: Float = 0f,

    val bar1Color: ProgressColor = ProgressColor.Unknown,
    val bar1LiteColor: ProgressColor = ProgressColor.Unknown,
    val bar2Color: ProgressColor = ProgressColor.Unknown,
    val bar2LiteColor: ProgressColor = ProgressColor.Unknown
)

enum class ProgressColor {
    Unknown,
    Grey,
    Green, Yellow, Red, Primary,
    GreenLite, YellowLite, PrimaryLite
}