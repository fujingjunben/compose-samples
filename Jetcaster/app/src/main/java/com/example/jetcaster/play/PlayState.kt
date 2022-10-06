package com.example.jetcaster.play


sealed class PlayState

object PlayReady : PlayState()
class Playing(val position: Long): PlayState()
class PlayPause(val position: Long): PlayState()
class PlaySeek(val position: Long): PlayState()
object PlayError : PlayState()
