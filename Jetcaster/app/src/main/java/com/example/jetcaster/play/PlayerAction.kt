package com.example.jetcaster.play


sealed class PlayerAction

object Ready : PlayerAction()
object Playing : PlayerAction()
object Pause : PlayerAction()
class SeekTo(val position: Long): PlayerAction()
object Error : PlayerAction()
object SeekBack: PlayerAction()
object SeekForward: PlayerAction()
object Default: PlayerAction()

enum class PlayState {
    PREPARE,
    PLAYING,
    PAUSE;

    fun toPlayerAction(): PlayerAction{
        return when(this) {
            PREPARE -> Ready
            PLAYING -> Playing
            PAUSE -> Pause
        }
    }
}
