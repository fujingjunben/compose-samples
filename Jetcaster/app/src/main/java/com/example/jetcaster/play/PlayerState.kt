package com.example.jetcaster.play


sealed class PlayerState

object PlayerReady : PlayerState()
class Playing(val position: Long): PlayerState()
class PlayerPause(val position: Long): PlayerState()
class PlayerSeek(val position: Long): PlayerState()
object PlayerError : PlayerState()
