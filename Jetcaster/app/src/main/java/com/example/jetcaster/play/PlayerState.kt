package com.example.jetcaster.play


sealed class PlayerState

class PlayerReady(val position: Long) : PlayerState()
class Playing(val position: Long): PlayerState()
class PlayerPause(val position: Long): PlayerState()
class PlayerSeek(val position: Long): PlayerState()
object PlayerError : PlayerState()
object PlayerSeekBack: PlayerState()
object PlayerSeekForward: PlayerState()
