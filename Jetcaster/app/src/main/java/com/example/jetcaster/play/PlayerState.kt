package com.example.jetcaster.play


sealed class PlayerState

object Ready : PlayerState()
object Playing : PlayerState()
object Pause : PlayerState()
class SeekTo(val position: Long): PlayerState()
object Error : PlayerState()
object SeekBack: PlayerState()
object SeekForward: PlayerState()
object None: PlayerState()
